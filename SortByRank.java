import java.util.Comparator;

public class SortByRank implements Comparator<Individual> {
	@Override
    public int compare(Individual ind1, Individual ind2) {
		if(ind1.getRank() > ind2.getRank()){
			return -1;
		}
		else if (ind1.getRank() < ind2.getRank()){
			return 1;
		}
        return 0;
    }
}
