import java.util.Comparator;

public class SortByFitness implements Comparator<Individual> {
	@Override
    public int compare(Individual ind1, Individual ind2) {
		if(ind1.getFitness() < ind2.getFitness()){
			return 1;
		}
		else if (ind1.getFitness() > ind2.getFitness()){
			return -1;
		}
        return 0;
    }
}
