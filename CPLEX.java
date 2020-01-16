import java.util.ArrayList;
import java.util.HashSet;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class CPLEX {
	
	private HashSet<Integer> fixed_true_features;
	private HashSet<Integer> fixed_false_features;
	private HashSet<Integer> non_fixed_features;
	private ArrayList<Boolean> corrected_individual;
	private ArrayList<int[]> constraints;
	private int size_solution;

	public CPLEX (ArrayList<int[]> fm, ArrayList<Boolean> solution){
		size_solution = solution.size();
		constraints = fm;
		fixed_true_features = new HashSet<Integer>();
		fixed_false_features = new HashSet<Integer>();
		non_fixed_features = new HashSet<Integer>();
		corrected_individual = new ArrayList<Boolean>(size_solution);
		modelPreparation(solution);
		launch();
	}
	
	public CPLEX (ArrayList<int[]> fm, ArrayList<Boolean> solution, HashSet<Integer> new_features){
		size_solution = solution.size();
		constraints = fm;
		fixed_true_features = new HashSet<Integer>();
		fixed_false_features = new HashSet<Integer>();
		non_fixed_features = new_features;
		corrected_individual = new ArrayList<Boolean>(size_solution);
		modelPreparation(solution);
		long time = System.currentTimeMillis();
		launch();
		System.out.println(System.currentTimeMillis() - time);
	}
	
	private void modelPreparation(ArrayList<Boolean> solution){
		for(int[] constraint : constraints){
			if(isViolated(solution, constraint)){
				for(int i=0; i<constraint.length; i++){
					non_fixed_features.add(Math.abs(constraint[i])-1);
				}
			} else {
				for(int i=0; i<constraint.length; i++){
					if(solution.get(Math.abs(constraint[i])-1)){
						fixed_true_features.add(Math.abs(constraint[i])-1);
					} else {
						fixed_false_features.add(Math.abs(constraint[i])-1);
					}
				}
			}
		}
		for(Integer toRemove : non_fixed_features){
			fixed_false_features.remove(toRemove);
			fixed_true_features.remove(toRemove);
		}
	}
	
	private boolean isViolated(ArrayList<Boolean> solution, int[] constraint){
		boolean sign;
		for(int i=0; i<constraint.length; i++){
			sign = constraint[i] > 0 ? true : false;
			if(solution.get(Math.abs(constraint[i])-1) == sign){
				return false;
			}
		}
		return true;
	}
	
	private boolean isViable(){
		for(int[] constraint : constraints){
			if(isViolated(corrected_individual, constraint)){
				return false;
			}
		}
		return true;
	}
	
	public ArrayList<Boolean> getInd(){
		return corrected_individual;
	}
	
	private void launch(){
		try{
			IloCplex cplex = new IloCplex();
			cplex.setParam(IloCplex.DoubleParam.TiLim, 20);
			cplex.setOut(null);
			IloNumVar[] x = cplex.boolVarArray(size_solution);
			double[] objVals = new double[size_solution];
			
			for(int i=0; i<size_solution; i++){
				if(fixed_false_features.contains(i)){
					objVals[i] = 1;
				}
				else if(fixed_true_features.contains(i)){
					objVals[i] = -1;
				}
				else{
					objVals[i] = 0;
				}
			}
						
			cplex.addMinimize(cplex.scalProd(x, objVals));
			
			int constant;
			for(int[] constraint : constraints){
				constant = 0;
				double[] coef = new double[constraint.length];
				IloNumVar[] temp = cplex.boolVarArray(constraint.length);
				for(int i=0; i<constraint.length; i++){
					temp[i] = x[Math.abs(constraint[i])-1];
					if(constraint[i]>0){
						coef[i] = -1;
					} else {
						coef[i] = 1;
						constant++;
					}
				}
				cplex.addLe(cplex.scalProd(temp, coef), (constant-1));
			}
									
			if(cplex.solve()){
				cplex.output().println("Solution status = " + cplex.getStatus());
				cplex.output().println("Solution value = " + cplex.getObjValue());		
				double[] solution = cplex.getValues(x);
				for(int i=0; i<solution.length; i++){
					if(Math.abs(solution[i]) < 0.5){
						corrected_individual.add(false);
					} else {
						corrected_individual.add(true);
					}
				}								
				cplex.end();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}