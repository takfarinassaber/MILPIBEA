import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern;

/* Object that allow us to read and write in given files */

public class Data {

	private int nbVar;							// Number of variables (i.e. number of feature)
	private ArrayList<int[]> fm; 				// Constraints
	private HashSet<Integer> mandatory; 		// Mandatory variables
	private ArrayList<double[]> augment;		// Objectives values of each variable
	private HashSet<Integer> dead;				// Dead variables
	private ArrayList<Individual> seed;			// Richseed

	public Data() {
		nbVar = 0;
		fm = new ArrayList<int[]>();
		mandatory = new HashSet<Integer>();
		augment = new ArrayList<double[]>();
		dead = new HashSet<Integer>();
		seed = new ArrayList<Individual>();
	}
	
	public ArrayList<int[]> getFm() {
		return fm;
	}

	public HashSet<Integer> getMandatory() {
		return mandatory;
	}
	
	public HashSet<Integer> getDead(){
		return dead;
	}

	public ArrayList<double[]> getAugment() {
		return augment;
	}
	
	public ArrayList<Individual> getRichseed(){
		return seed;
	}
	
	public int getNbVar(){
		return nbVar;
	}

	// Launch the reading of all files
	public void readData(String path) {
		System.out.println("Read data ...");
		readFM(path);
		readAugment(path + ".augment");
		readMandatory(path + ".mandatory");
		readDead(path + ".dead");
		//readRichseed(path + ".correctedSolution");
		System.out.println("Done\n");
	}

	private void readFM(String path) {
		BufferedReader bf = null;
		try {
			bf = new BufferedReader(new FileReader(path));
			String currentLine = "";
			String[] elements = null;
			Pattern p = Pattern.compile(" ");
			while ((currentLine = bf.readLine()) != null) {
				if (Character.isDigit(currentLine.charAt(0)) || currentLine.charAt(0) == '-') {
					elements = p.split(currentLine);
					int[] constraint = new int[elements.length - 1];
					for (int i = 0; i < elements.length - 1; i++) {
						constraint[i] = Integer.parseInt(elements[i]);
					}
					fm.add(constraint);
				}
				else if(currentLine.charAt(0) == 'p'){
					elements = p.split(currentLine);
					nbVar = Integer.parseInt(elements[2]);
				}
			}
			bf.close();
		} catch (Exception e) {
			e.printStackTrace();
			//System.out.println("Error, cannot read " + path + " file");
		} finally {
			try {
				if (bf != null) {
					bf.close();
				}
			} catch (Exception e) {
				System.out.println("Error, cannot close BufferedReader");
			}
		}
	}

	// Read augment file : cost, used before, defects
	private void readAugment(String path) {
		BufferedReader bf = null;
		try {
			bf = new BufferedReader(new FileReader(path));
			String currentLine = "";
			String[] elements = null;
			Pattern p = Pattern.compile(" ");
			while ((currentLine = bf.readLine()) != null) {
				if (Character.isDigit(currentLine.charAt(0))) {
					elements = p.split(currentLine);
					double[] obj_values = new double[elements.length - 1];
					for (int i = 1; i < elements.length; i++) {
						obj_values[i-1] = Double.parseDouble(elements[i]);
					}
					augment.add(obj_values);
				}
			}
			bf.close();
		} catch (Exception e) {
			System.out.println("Error, cannot read " + path + " file");
		} finally {
			try {
				if (bf != null) {
					bf.close();
				}
			} catch (Exception e) {
				System.out.println("Error, cannot close BufferedReader");
			}
		}
	}

	private void readMandatory(String path) {
		BufferedReader bf = null;
		try {
			bf = new BufferedReader(new FileReader(path));
			String currentLine = "";
			while ((currentLine = bf.readLine()) != null) {
				if (Character.isDigit(currentLine.charAt(0))) {
					mandatory.add(Integer.parseInt(currentLine));
				}
			}
			bf.close();
		} catch (Exception e) {
			System.out.println("Error, cannot read " + path + " file");
		} finally {
			try {
				if (bf != null) {
					bf.close();
				}
			} catch (Exception e) {
				System.out.println("Error, cannot close BufferedReader");
			}
		}
	}

	private void readDead(String path) {
		BufferedReader bf = null;
		try {
			bf = new BufferedReader(new FileReader(path));
			String currentLine = "";
			while ((currentLine = bf.readLine()) != null) {
				if (Character.isDigit(currentLine.charAt(0))) {
					dead.add(Integer.parseInt(currentLine));
				}
			}
			bf.close();
		} catch (Exception e) {
			System.out.println("Error, cannot read " + path + " file");
		} finally {
			try {
				if (bf != null) {
					bf.close();
				}
			} catch (Exception e) {
				System.out.println("Error, cannot close BufferedReader");
			}
		}
	}
	
	private void readRichseed(String path){
		BufferedReader bf = null;
		try {
			bf = new BufferedReader(new FileReader(path));
			String currentLine = "";
			while ((currentLine = bf.readLine()) != null) {
				boolean[] temp = new boolean[currentLine.length()];
				for (int i = 0; i < currentLine.length(); i++) {
					temp[i] = (currentLine.charAt(i) == '1') ? true : false;
				}
				seed.add(new Individual(temp));
			}
			bf.close();
		} catch (Exception e) {
			System.out.println("Error, cannot read " + path + " file");
		} finally {
			try {
				if (bf != null) {
					bf.close();
				}
			} catch (Exception e) {
				System.out.println("Error, cannot close BufferedReader");
			}
		}
	}
}
