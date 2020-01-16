import java.util.ArrayList;

public class DominatedFiltrage {

	private ArrayList<Individual> nonDominatedInd;
	private ArrayList<Individual> dominated;
		
	public DominatedFiltrage(){
		nonDominatedInd = new ArrayList<Individual>();
		dominated = new ArrayList<Individual>();
	}
		
	public ArrayList<Individual> getNonDominatedInd(){
		return nonDominatedInd;
	}
	
	public ArrayList<Individual> getDominated(){
		return dominated;
	}
	
	//Add a new individual to the actual non dominated set : this can switch individual from nonDominatedInd to dominated
	public void newInd(Individual c){
		boolean isDominated = false;
		int iterateur = 0;
		while(!isDominated && iterateur<nonDominatedInd.size() && !nonDominatedInd.isEmpty()){
			if(c.dominate(nonDominatedInd.get(iterateur)) == -1){
				isDominated=true;
			}
			else{
				if(c.dominate(nonDominatedInd.get(iterateur)) == 1){
					dominated.add(nonDominatedInd.get(iterateur));
					nonDominatedInd.remove(iterateur);
					iterateur--;
				}
			}
			iterateur++;
		}
		if(!isDominated){
			nonDominatedInd.add(c);
		}
		else{
			dominated.add(c);
		}
	}
}

