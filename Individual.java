import java.util.HashSet;
import java.util.Random;

/* Representation of an individual :
 * boolean representation
 * objectives value : correctness, richness of features, features that were used before, known defects, cost
 * fitness value
 * random generation
 * */

public class Individual {

	private boolean[] features_selections;	// Representation of the individual in IBEA algorithm
	private double[] objective_values;
	private double fitness;
	private int rank;
	
	public Individual (int nb_features){
		features_selections = new boolean[nb_features];
		fitness = 0;
	}
	
	public Individual(boolean[] ind){
		features_selections = new boolean[ind.length];
		for(int i=0; i<ind.length; i++){
			features_selections[i] = ind[i];
		}
		fitness = 0;
	}
	
	public boolean[] getFeaturesSelection(){
		return features_selections;
	}
	
	public void setRank(int new_rank){
		rank = new_rank;
	}
	
	public int getRank(){
		return rank;
	}
	
	public void setObjectiveValues(double[] new_values){
		objective_values = new double[new_values.length];
		for(int i=0; i<new_values.length; i++){
			objective_values[i] = new_values[i];
		}
	}
	
	public double[] getObjectivesValues(){
		return objective_values;
	}
	
	public void setFitness(double fitness){
		this.fitness = fitness;
	}
	
	public double getFitness(){
		return fitness;
	}

	public void setFeaturesSelectionValues(boolean[] new_values){
		for(int i=0; i<new_values.length; i++){
			features_selections[i] = new_values[i];
		}
	}
	
	// Random generation of features selection given a chance of "true" in percent ( between 0 and 100 )
	public void RandomGeneration(double percentage_of_true, HashSet<Integer> mandatory, HashSet<Integer> dead){
		if( (percentage_of_true < 0) || (percentage_of_true > 100) ){
			System.out.println("Percentage should be between 0 and 100, program cannot continue.");
			System.exit(1);
		} else {
			Random rand = new Random();
			for(int i=0; i<features_selections.length; i++){
				if(mandatory.contains(i+1)){
					features_selections[i] = true;
				}
				else if(dead.contains(i+1)){
					features_selections[i] = false;
				}
				else {
					features_selections[i] = rand.nextDouble()*100 < percentage_of_true ? true : false;
					//features_selections[i] = rand.nextBoolean();
				}
			}
		}
	}
	
	// If compare is dominated return 1, if he dominates return -1, otherwise return 0
	public int dominate(Individual compare){
		int dominance = 0;
		int pos = 0;
		while((pos < objective_values.length) && (objective_values[pos] == compare.getObjectivesValues()[pos])){pos++;}
		if(pos == objective_values.length){return 0;}
		if(objective_values[pos] < compare.getObjectivesValues()[pos]){
			dominance = 1;
		}
		else{
			dominance = -1;
		}
		if(dominance == 1){
			while((pos < objective_values.length) && (objective_values[pos] <= compare.getObjectivesValues()[pos])){pos++;}
		}
		else{
			while((pos < objective_values.length) && (objective_values[pos] >= compare.getObjectivesValues()[pos])){pos++;}
		}
		if(pos == objective_values.length){
			return dominance;
		}
		return 0;
	}
	
	public String toString(){
		String result = "Objective values : ";
		/*for(int i=0; i<objective_values.length; i++){
			result += objective_values[i] + " ";
		}
		result += "\n" + "Fitness value : " + fitness + "\n";*/
		for(int i=0; i<features_selections.length; i++){
			result += features_selections[i]==true?1:0;
		}
		return result;
	}
}
