
/* Improvement of IBEA algorithm used to solve MO features selection in SPL :
 * the algorithm is improved using the MILP solver CPLEX
 * 
 * It takes the following arguments :
 * 1 : folders where are the different files (model, augment, mandatory, dead)
 * */


public class Main {

	public static void main(String[] args) {
		
		String currentFile = "PATH/TO/DATASET/INSTANCE/fiasco.dimacs";
		// Reading data
		Data data = new Data();
		data.readData(currentFile);
		
		// Initialization of the problem (i.e. creation of the objectives)
		Problem p = new Problem(data.getAugment(), data.getFm());
		
		/* Creation of CPLEX_IBEA and launch
		 * Parameters : 
		 * feature model, mandatory features, dead features, mutation probability, bit flip probability, CPLEX mutation probability,
		 * crossover probability, size of population, time, size of an individual (i.e. number of features)
		 * problem studied (i.e. objective functions
		 * */
		CPLEX_IBEA ga = new CPLEX_IBEA(data.getFm(), data.getMandatory(), data.getDead(), 0.01, 0.0005, 0.02, 1, 300, 1200000, data.getNbVar(), p, "original");
		// CPLEX_IBEA ga = new CPLEX_IBEA(data.getFm(), data.getMandatory(), data.getDead(), data.getRichseed(), 0.01, 0.0005, 0.02, 1, 300, 1200000, data.getNbVar(), p, files[i]);
		ga.launch();
		
		System.out.println("\n\n");
	}

}
