import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

/* Augmented IBEA by a MILP solver (CPLEX) :
 * Genetic algorithm
 * */

public class CPLEX_IBEA {

	private ArrayList<int[]> fm;							// Constraints of our feature model
	private HashSet<Integer> mandatory;						// Mandatory features (i.e. features that need to be "true")
	private HashSet<Integer> dead;							// Dead features (i.e. features that need to be "false")
	private ArrayList<Individual> population;				// Current population of our genetic algorithm
	private ArrayList<Individual> all_nonDominated_found;	// Objectives values of all non dominated points found
	private double mutation_probability;					// Probability of doing a mutation
	private double bit_flip_probability;					// Probability of switching a bit when doing a classical mutation
	private double CPLEX_mutation_probability;				// Probability of using CPLEX in the mutation
	private double crossover_probability;					// Probability of doing a crossover
	private int time;										// Number of generations to perform
	private int sizePop;									// Size of the population
	private int sizeInd;									// Size of an individual
	private Problem p;										// Problem to optimize (i.e. objective functions)
	private double[] reference_point;						// Reference point for fitness computation
	private String nbFile;
	private long elapsed;
	
	// Constructor without initial population
	public CPLEX_IBEA(ArrayList<int[]> fm, HashSet<Integer> mandatory, HashSet<Integer> dead, double mutation_probability, double bit_flip_probability,
			double CPLEX_mutation_probability, double crossover_probability, int sizePop, int time, int sizeInd, Problem p, String nbFile){
		this.fm = fm;
		this.mandatory = mandatory;
		this.dead = dead;
		this.mutation_probability = mutation_probability;
		this.bit_flip_probability = bit_flip_probability;
		this.CPLEX_mutation_probability = CPLEX_mutation_probability;
		this.crossover_probability = crossover_probability;
		this.time = time;
		this.sizePop = sizePop;
		this.sizeInd = sizeInd;
		this.p = p;
		this.nbFile = nbFile;
		this.elapsed = 0;
		population = new ArrayList<Individual>(sizePop*2);
		all_nonDominated_found = new ArrayList<Individual>();
		try{
			FileWriter file = new FileWriter("./objectives_evolution.txt"); //overide non-dominated solutions from previous run
			file.close();
		} catch(Exception e){
			System.out.println("Error, cannot the erase content");
		}
	}
	
	// Constructor with initial population
	public CPLEX_IBEA(ArrayList<int[]> fm, HashSet<Integer> mandatory, HashSet<Integer> dead, ArrayList<Individual> seed, double mutation_probability, double bit_flip_probability,
			double CPLEX_mutation_probability, double crossover_probability, int sizePop, int time, int sizeInd, Problem p, String nbFile){
		this.fm = fm;
		this.mandatory = mandatory;
		this.dead = dead;
		this.mutation_probability = mutation_probability;
		this.bit_flip_probability = bit_flip_probability;
		this.CPLEX_mutation_probability = CPLEX_mutation_probability;
		this.crossover_probability = crossover_probability;
		this.time = time;
		this.sizePop = sizePop;
		this.sizeInd = sizeInd;
		this.p = p;
		this.nbFile = nbFile;
		this.elapsed = 0;
		population = seed;
		for(Individual ind : population){
			p.compute(ind);
		}
		all_nonDominated_found = new ArrayList<Individual>();
		try{
			FileWriter file = new FileWriter("./objectives_evolution");
			file.close();
		} catch(Exception e){
			System.out.println("Error, cannot the erase content");
		}
	}
	
	public void launch(){
		
		int currentGeneration = 0;
		ArrayList<Individual> parents;
		ArrayList<Individual> childs;
		Random rand = new Random();
		
		// Generation of initial population
		this.initialPopulationGeneration();
		this.write();
		long starting_time = System.currentTimeMillis();
		
		while(elapsed < time){
			currentGeneration++;
			parents = new ArrayList<Individual>(sizePop);
			childs = new ArrayList<Individual>(sizePop);
			
			//Selection of individuals to cross
			for(int i=0; i<sizePop/2; i++){
				Individual parent1 = population.get(this.binTournament());
				Individual parent2 = population.get(this.binTournament());
				while(parent1.equals(parent2)){
					parent2 = population.get(this.binTournament());
				}
				parents.add(parent1);
				parents.add(parent2);
			}
			
			//Crossover
			for(int i=0; i<parents.size(); i+=2){
				if(rand.nextDouble() <= crossover_probability){
					childs.add(this.crossover(parents.get(i), parents.get(i+1)));
					childs.add(this.crossover(parents.get(i), parents.get(i+1)));	
				}
			}
			
			//Normal mutation + CPLEX mutation (also compute the objectives values after last changes)
			for(Individual child : childs){
				if(rand.nextDouble() <= mutation_probability){
					this.mutation(child);
				}
				else if(rand.nextDouble() <= CPLEX_mutation_probability){
					this.CPLEX_mutation(child);
				}
				p.compute(child);
			}

			//Compute reference point
			population.addAll(childs);
			all_nonDominated_found.addAll(childs);
			this.computeReferencePoint();
			
			//Compute fitness + domination + elitism
			for(int pos=0; pos<population.size(); pos++){
				this.fitness(pos);
			}
			this.computeDomination();
			this.elitism();
			
			System.out.println("Generation " + currentGeneration + " done");
			elapsed = System.currentTimeMillis() - starting_time;
			
			this.write();
		}
		
		this.filterNonDominated();
	}
	
	private void initialPopulationGeneration(){
		Random rand = new Random();
		int remaining = sizePop - population.size();
		for(int i=0; i<remaining; i++){
			Individual new_ind = new Individual(sizeInd);
			new_ind.RandomGeneration(rand.nextInt(101), mandatory, dead);
			p.compute(new_ind);
			population.add(new_ind);
		}
		this.computeReferencePoint();
		for(int i=0; i<population.size(); i++){
			this.fitness(i);
		}
		this.computeDomination();
	}
	
	private void computeReferencePoint(){
		reference_point = new double[population.get(0).getObjectivesValues().length];
		for(int i=0; i<reference_point.length; i++){
			reference_point[i] = 0;
		}
		for(int j=0; j<population.size(); j++){
			for(int i=0; i<reference_point.length; i++){
				double value = population.get(j).getObjectivesValues()[i];
				if(reference_point[i] < value){
					reference_point[i] = value;
				}
			}	
		}
	}
	
	private void fitness(int pos){
		try{
			// Write the population in a file to use C++ hypervolume on it
			FileWriter file = new FileWriter(new File("./temp"));
			for(int i=0; i<population.size(); i++){
				if( i != pos ){
					String to_write = "";
					for(int j=0; j<population.get(i).getObjectivesValues().length-1; j++){
						to_write += population.get(i).getObjectivesValues()[j] + " ";
					}
					to_write += population.get(i).getObjectivesValues()[population.get(i).getObjectivesValues().length-1] + "\n";
					file.write(to_write);
				}
			}
			file.close();
			
			// Launch the C++ hypervolume code
			Process proc = Runtime.getRuntime().exec("/PATH/TO/HYPERVOLUME/CALCULATOR/BINARY/hv ./temp");
			
			// Read the output
	        BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
	        String currentLine = "";
	        while(!Character.isDigit((currentLine = reader.readLine()).charAt(0))){}
	        population.get(pos).setFitness(Double.parseDouble(currentLine));
	        reader.close();
	        proc.waitFor();
	        			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Return the pos in the ArrayList population of the selected individual
	private int binTournament(){
		Random pos = new Random();
		int pos1 = pos.nextInt(sizePop);
		int pos2 = pos.nextInt(sizePop);
		Individual ind1 = population.get(pos1);
		Individual ind2 = population.get(pos2);
		if(ind1.getRank() > ind2.getRank()){
			return pos2;
		}
		else if(ind1.getRank() < ind2.getRank()){
			return pos1;
		}
		else if(ind1.getFitness() > ind2.getFitness()){
			return pos2;
		}
		else {
			return pos1;
		}
	}
	
	// Return a child representing a crossover between the parents (one point crossover)
	private Individual crossover(Individual parent1, Individual parent2){
		Random rand = new Random();
		int crossover_pos = rand.nextInt(sizeInd);
		boolean[] crossover_values = new boolean[sizeInd];
		
		for(int i=0; i<crossover_pos; i++){
			crossover_values[i] = parent1.getFeaturesSelection()[i];
		}
		for(int i=crossover_pos; i<sizeInd; i++){
			crossover_values[i] = parent2.getFeaturesSelection()[i];
		}
		return new Individual(crossover_values) ;
	}
	
	//Mutate the child with a chance for each bit to be flip
	private void mutation(Individual child){
		Random rand = new Random();
		for(int i=0; i<child.getFeaturesSelection().length; i++){
			if((rand.nextDouble() <= bit_flip_probability) && !mandatory.contains(i+1) && !dead.contains(i+1)){
				child.getFeaturesSelection()[i] ^= true;
			}
		}
	}
	
	private void CPLEX_mutation(Individual child){
		ArrayList<Boolean> to_correct = new ArrayList<Boolean>(child.getFeaturesSelection().length);
		for(int i=0; i<child.getFeaturesSelection().length; i++){
			to_correct.add(child.getFeaturesSelection()[i]);
		}
		CPLEX cplex = new CPLEX(fm, to_correct);
		ArrayList<Boolean> corrected = cplex.getInd();
		boolean[] convert = new boolean[corrected.size()];
		for(int i=0; i<corrected.size(); i++){
			convert[i] = corrected.get(i);
		}
		child.setFeaturesSelectionValues(convert);
	}
	
	private void elitism(){
		Collections.sort(population, new SortByRank());
		
		// We want to rank by fitness only a sub part of the ranked sort individuals
		int rankToSortByFitness = population.get(sizePop-1).getRank();
		int starting_pos = 0;
		int ending_pos = 0;
		boolean b = true;
		int position = 0;
		while(b){
			if(population.get(position).getRank() == rankToSortByFitness){
				b = false;
				starting_pos = position;
			}
			position++;
		}
		while(position < population.size() && !b){
			if(population.get(position).getRank() != rankToSortByFitness){
				b = true;
				ending_pos = position;
			}
			position++;
		}
		if(position == population.size()){
			ending_pos = position-1;
		}
		Collections.sort(population.subList(starting_pos, ending_pos), new SortByFitness());
		
		// Select the individuals to keep for the next generation
		int nbToRemove = population.size()-sizePop;
		for(int i=0; i<nbToRemove; i++){
			population.remove(0);
		}
	}
	
	private void write(){
		try{
			FileWriter file = new FileWriter("./objectives_evolution" + nbFile, true);
			for(Individual ind : population){
				String temp = "";
				for(int i=0; i<ind.getObjectivesValues().length; i++){
					temp += ind.getObjectivesValues()[i] + " ";
				}
				file.write(temp + "\n");
			}
			file.write("\n");
			file.close();
			
			FileWriter file2 = new FileWriter("./time" + nbFile, true);
			file2.write(elapsed + "\n");
			file2.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void computeDomination() {
		DominatedFiltrage df;
		int rank = 0;
		ArrayList<Individual> temp = new ArrayList<Individual>();
		temp.addAll(population);
		while (!temp.isEmpty()) {
			df = new DominatedFiltrage();
			rank++;
			for (Individual ind : temp) {
				df.newInd(ind);
			}
			ArrayList<Individual> nonDominated = df.getNonDominatedInd();
			df.getDominated().size();
			temp = df.getDominated();
			for (Individual ind : nonDominated) {
				ind.setRank(rank);
			}
		}
	}
	
	private void filterNonDominated(){
		DominatedFiltrage df;
		int rank = 0;
		ArrayList<Individual> temp = new ArrayList<Individual>();
		temp.addAll(all_nonDominated_found);
		while (!temp.isEmpty()) {
			df = new DominatedFiltrage();
			rank++;
			for (Individual ind : temp) {
				df.newInd(ind);
			}
			ArrayList<Individual> nonDominated = df.getNonDominatedInd();
			df.getDominated().size();
			temp = df.getDominated();
			for (Individual ind : nonDominated) {
				ind.setRank(rank);
			}
		}
		for(int i=0; i<all_nonDominated_found.size(); i++){
			if(all_nonDominated_found.get(i).getRank() != 1){
				all_nonDominated_found.remove(i);
				i--;
			}
		}
	}
}
