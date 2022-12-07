import java.util.ArrayList;

public class CPT {
    private Variable variable;
    private ArrayList<ArrayList<String>> given;
    private ArrayList<Double> P;
    private ArrayList<String> name_of_parents;


    public ArrayList<ArrayList<String>> getGiven() {
        return given;
    }

    public ArrayList<String> getName_of_parents() {
        return name_of_parents;
    }

    public ArrayList<Double> getP() {
        return P;
    }

    public CPT(Variable variable, ArrayList<BayesianNetworkNode> parents, String table) {
        this.P = new ArrayList();
        this.variable = variable;
        this.name_of_parents = new ArrayList<>();
        for (int i = 0; i < parents.size(); i++) {
            this.name_of_parents.add(parents.get(i).getVariable().getName());
        }
        int num_of_parents = parents.size();
        String[] s = table.split(" ");
        int num_of_row = s.length;
        int set_count = num_of_row;
        this.given = new ArrayList<>();
        for (int i = 0; i < num_of_parents + 1; i++) {
            this.given.add(new ArrayList<String>());
        }
        for (int j = 0; j < num_of_parents; j++) {
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
        for (int i = 0; i < num_of_row; i+= this.variable.getOutcomes().size()) {
            for (int j = 0; j < this.variable.getOutcomes().size(); j++) {
                this.given.get(given.size()-1).add((String)this.variable.getOutcomes().get(j));
            }
        }
    }


    public void ShowCpt() {
        System.out.println("_____CPT_____ ");
        for (int i = 0; i < this.name_of_parents.size() ; i++) {
            System.out.print(this.name_of_parents.get(i)+ " ");
        }
        System.out.println(this.variable.getName() + "    " + "P(" + this.variable.getName() + ")");
            for (int i = 0; i < this.given.get(0).size() ; i++) {
                for (int j = 0; j < this.given.size(); j++) {
                System.out.print(this.given.get(j).get(i) + " ");
            }
            System.out.println(" | " + this.P.get(i));
        }
    }
}
