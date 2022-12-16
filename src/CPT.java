import java.util.ArrayList;
import java.util.Comparator;

public class CPT implements Comparable<CPT> {
    private Variable variable;
    private ArrayList<ArrayList<String>> given;
    private ArrayList<Double> P;
    private ArrayList<String> name_of_parents;
    private int size;


    public CPT(Variable variable, ArrayList<BayesianNetworkNode> parents, String table) {
        this.P = new ArrayList();
        this.variable = variable;
        this.name_of_parents = new ArrayList<>();
        for (int i = 0; i < parents.size(); i++) {
            this.name_of_parents.add(parents.get(i).getVariable().getName());
        }
        this.name_of_parents.add(this.variable.getName());
        int num_of_parents = name_of_parents.size();
        String[] s = table.split(" ");
        int num_of_row = s.length;
        int set_count = num_of_row;
        this.given = new ArrayList<>();
        for (int i = 0; i < num_of_parents; i++) {
            this.given.add(new ArrayList<String>());
        }
        for (int j = 0; j < num_of_parents - 1; j++) {
            set_count = set_count / parents.get(j).getVariable().getOutcomes().size();
            int count = 0;
            int index = 0;
            for (int i = 0; i < num_of_row; i++) {
                this.given.get(j).add((String) parents.get(j).getVariable().getOutcomes().get(index));
                count++;
                if (count == set_count) {
                    index = (index + 1) % parents.get(j).getVariable().getOutcomes().size();
                    count = 0;
                }
            }
        }
        for (int i = 0; i < s.length; i++) {
            this.P.add(Double.parseDouble(s[i]));
        }
        for (int i = 0; i < num_of_row; i += this.variable.getOutcomes().size()) {
            for (int j = 0; j < this.variable.getOutcomes().size(); j++) {
                this.given.get(given.size() - 1).add((String) this.variable.getOutcomes().get(j));
            }
        }
        this.size = num_of_row * num_of_parents;
    }

    public CPT(ArrayList given ,ArrayList P ,  ArrayList name_of_parents){
        this.variable = null;
        this.given = given;
        this.name_of_parents = name_of_parents;
        this.P = P;
        this.size = given.size() * P.size();

    }

    public CPT(CPT other) {
        this.variable = new Variable(other.variable);
        this.given = new ArrayList<>();
        for (int i = 0; i < other.getGiven().size(); i++) {
            this.given.add(new ArrayList<>(other.getGiven().get(i)));
        }
        this.P = new ArrayList<>(other.getP());
        this.name_of_parents = new ArrayList<>(other.getName_of_parents());
        this.size = other.size;
    }
    public void set_to_other(CPT other){
        this.variable = other.variable;
        this.given = other.getGiven();
        this.P = other.getP();
        this.name_of_parents = other.getName_of_parents();
        this.size = other.size;
    }


    public void delete_row(int index) {
        for (int i = 0; i < this.given.size(); i++) {
            this.given.get(i).remove(index);
        }
        this.P.remove(index);
    }

    public void delete_col(int index) {
        this.given.remove(index);
        this.name_of_parents.remove(index);
    }

    public void delete_col(String key) {
        for (int i = 0; i < this.name_of_parents.size(); i++) {
            if (this.name_of_parents.get(i).equals(key)) {
                delete_col(i);
                return;
            }
        }
    }

    public void del_rows(String name, String value) {
            int index = this.name_of_parents.indexOf(name);
        for (int i = 0; i < this.given.get(index).size(); i++) {
            if (! this.given.get(index).get(i).equals(value)) {
                delete_row(i);
                i--;
            }
        }

    }


    public ArrayList<ArrayList<String>> getGiven() {
        return given;
    }

    public ArrayList<String> getName_of_parents() {
        return name_of_parents;
    }

    public ArrayList<Double> getP() {
        return P;
    }

    public Variable getVariable() {
        return variable;
    }

    public void ShowCpt() {
        System.out.println("_____CPT_____ ");
        for (int i = 0; i < this.name_of_parents.size(); i++) {
            System.out.print(this.name_of_parents.get(i) + " ");
        }
        System.out.println();
       // System.out.println("    " + "P(" + this.variable.getName() + ")");
        for (int i = 0; i < this.given.get(0).size(); i++) {
            for (int j = 0; j < this.given.size(); j++) {
                System.out.print(this.given.get(j).get(i) + " ");
            }
            System.out.println(" | " + this.P.get(i));
        }
    }

    @Override
    public int compareTo(CPT o) {
        if (this.size - o.size != 0) {
            return this.size - o.size;
        }
        int sum1 = 0, sum2 = 0;
        for (int i = 0; i < this.name_of_parents.size(); i++) {
            for (int j = 0; j < this.name_of_parents.get(i).length(); j++) {
                sum1 += this.name_of_parents.get(i).charAt(j);
            }
        }

        for (int i = 0; i < o.name_of_parents.size(); i++) {
            for (int j = 0; j < o.name_of_parents.get(i).length(); j++) {
                sum2 += o.name_of_parents.get(i).charAt(j);
            }
        }

        return sum1 - sum2;
    }
}