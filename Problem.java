import java.util.ArrayList;

/* Representation of the problem studied :
 * objectives value functions
 * */

public class Problem {
	
	private ArrayList<double[]> augment;
	private ArrayList<int[]> fm;

	public Problem(ArrayList<double[]> augment, ArrayList<int[]> fm){
		this.augment = augment;
		this.fm = fm;
	}
	
	// Compute objectives of an individual : correctness, richness of features, features that were not used before, known defects and cost
	public void compute(Individual ind){
		double[] temp = new double[5];
		temp[0] = correctness(ind.getFeaturesSelection());
		temp[1] = richness(ind.getFeaturesSelection());
		temp[2] = used_before(ind.getFeaturesSelection());
		temp[3] = defects(ind.getFeaturesSelection());
		temp[4] = cost(ind.getFeaturesSelection());
		ind.setObjectiveValues(temp);
	}
	
	private double correctness(boolean[] ind){
		double cpt = 0;
		for(int[] constraint : fm){
			boolean sign;
			for(int i=0; i<constraint.length; i++){
				sign = constraint[i] > 0 ? true : false;
				if(ind[Math.abs(constraint[i])-1] == sign){
					cpt--;
					break;
				}
			}
			cpt++;
		}
		return cpt;
	}
	
	private double richness(boolean[] ind){
		double cpt = 0;
		for(int i=0; i<ind.length; i++){
			if(!ind[i]){
				cpt++;
			}
		}
		return cpt;
	}
	
	private double used_before(boolean[] ind){
		double cpt = 0;
		for(int i=0; i<ind.length; i++){
			if(ind[i]){
				if(augment.get(i)[1] == 0){
					cpt++;
				}
			}
		}
		return cpt;
	}
	
	private double defects(boolean[] ind){
		double cpt = 0;
		for(int i=0; i<ind.length; i++){
			if((augment.get(i)[1] != 0) && (ind[i])){
				cpt += augment.get(i)[2];
			}
		}
		return cpt;
	}
	
	private double cost(boolean[] ind){
		double cpt = 0;
		for(int i=0; i<ind.length; i++){
			if(ind[i]){
				cpt += augment.get(i)[0];
			}
		}
		return cpt;
	}
}
