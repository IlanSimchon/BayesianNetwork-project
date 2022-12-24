import java.util.ArrayList;
import java.util.HashMap;

public class BayesianNetworkNode {
    private Variable variable;
    private CPT cpt;
    private ArrayList<BayesianNetworkNode> parents;


    public BayesianNetworkNode(Variable variable,String table) {
        this.variable = variable;
        this.parents = new ArrayList<>();;
        this.cpt = new CPT(this.variable, this.parents, table);
    }

    public BayesianNetworkNode(BayesianNetworkNode other){
        this.variable = new Variable(other.getVariable());
        this.cpt = new CPT(other.getCpt());
        this.parents = new ArrayList<>(other.getParents());
    }


    /**
     * This function receives a query and returns the probability corresponding to that query by simple inference
     * @param queries  contain the query
     * @param all_bs_net the bayesiaentworkNode
     * @param name  the name of the query variable
     * @return The probability obtained plus the number of addition and multiplication operations done during the algorithm
     */
    public Object[] Simple_conclusion(HashMap<String , String> queries , ArrayList<BayesianNetworkNode> all_bs_net , String name) {
        int index_of_querie = Integer.MAX_VALUE;
        BayesianNetworkNode Node_of_querie = null;
        int count_sum = 0 , count_multi = 0;
        for (int i = 0; i < all_bs_net.size(); i++) {
            if(all_bs_net.get(i).variable.getName().equals(name))   {
                index_of_querie = i;
                Node_of_querie = all_bs_net.get(i);
            }
        }
        HashMap<BayesianNetworkNode, ArrayList> Unknown = new HashMap<>();
        int num_of_option = 1;
        // Calculates the number of possible combinations
        for (int i = 0; i < all_bs_net.size(); i++) {
            if (!queries.containsKey(all_bs_net.get(i).variable.getName())) {
                Unknown.put(all_bs_net.get(i), all_bs_net.get(i).variable.getOutcomes());
                num_of_option *= all_bs_net.get(i).variable.getOutcomes().size();
            }
        }
        ArrayList<ArrayList<ArrayList>> all_combination = new ArrayList<>();
        // Inserts the already given values into all the lists of the combinations
        for (int i = 0; i < num_of_option; i++) {
            ArrayList<BayesianNetworkNode> names = new ArrayList<>();
            ArrayList<String> data = new ArrayList<>();
            for (int j = 0; j < queries.size(); j++) {
                for (int k = 0; k < all_bs_net.size(); k++) {
                    if (queries.keySet().toArray()[j].equals(all_bs_net.get(k).getVariable().getName())) {
                        names.add(all_bs_net.get(k));
                        data.add(String.valueOf(queries.get(queries.keySet().toArray()[j])));
                    }
                }
            }
            ArrayList<ArrayList> combination = new ArrayList<>();
            combination.add(names);
            combination.add(data);
            all_combination.add(combination);

        }
        // Finishes filling all the lists with combinations by inserting values of the non-given variables
        int set_count = all_combination.size();
        for (int i = 0; i < Unknown.size(); i++) {
            set_count = set_count / Unknown.get(Unknown.keySet().toArray()[i]).size();
            int index = 0;
            int count = 0;
            for (int j = 0; j < all_combination.size(); j++) {
                all_combination.get(j).get(0).add(Unknown.keySet().toArray()[i]);
                all_combination.get(j).get(1).add(Unknown.get(Unknown.keySet().toArray()[i]).get(index));
                count++;
                if (set_count == count) {
                    index = (index + 1) % Unknown.get(Unknown.keySet().toArray()[i]).size();
                    count = 0;

                }
            }
        }
        // Loops through each combination and calculates the probability for this data
        double sum_of_mone = 0.0;
        double sum_of_mechane = 0.0;
        for (int i = 0; i < all_combination.size(); i++) {
            Object ret_mone[] = P(all_combination.get(i).get(0), all_combination.get(i).get(1));
            if (sum_of_mone != 0) count_sum++;
            sum_of_mone += (double) ret_mone[0];

            count_sum += (int) ret_mone[1];
            count_multi += (int) ret_mone[2];

            // Adds the combinations with the additional values of the query variable for the denominator
            for (int j = 0; j < all_bs_net.get(index_of_querie).variable.getOutcomes().size(); j++) {
                if (!all_bs_net.get(index_of_querie).variable.getOutcomes().get(j).equals(queries.get(name))) {
                    all_combination.get(i).get(1).remove(all_combination.get(i).get(0).indexOf(Node_of_querie));
                    all_combination.get(i).get(1).add(all_combination.get(i).get(0).indexOf(Node_of_querie), all_bs_net.get(index_of_querie).variable.getOutcomes().get(j));
                    Object[] ret_mechane = P(all_combination.get(i).get(0), all_combination.get(i).get(1));
                    if (sum_of_mechane != 0) count_sum++;
                    sum_of_mechane += (double) ret_mechane[0];

                    count_sum += (int) ret_mechane[1];
                    count_multi += (int) ret_mechane[2];

                }
            }
        }
        // Adds the numerator to the denominator
        sum_of_mechane += sum_of_mone;
        count_sum++;

        // divides the numerator by the denominator
        Object[] ret = {sum_of_mone / sum_of_mechane , count_sum , count_multi};

        return ret;

    }

    /**
     * This function that calculates the probability for a list of a certain combination of values for variables
     * @param names the variables name
     * @param data the given cousbination
     * @return A list containing the probability of this combination, the amount of addition operations and the amount of multiplication operations done in order to calculate
     */
    public Object[] P(ArrayList<BayesianNetworkNode> names , ArrayList<String> data){
        int count_multi = 0;
        double sum = 1;
        for (int i = 0; i < names.size(); i++) {
            String[] same_row = new String[names.get(i).cpt.getGiven().size()];
            // Inserting the value itself into the last cell according to the cpt definition
            same_row[same_row.length-1] = data.get(i);
            // Filling in a row of relevant values for the question variable according to the position of the variables in the table
            for (int j = 0; j < names.size(); j++) {
                if(names.get(i).getParents().contains(names.get(j))) {
                    same_row[names.get(i).cpt.getName_of_parents().indexOf(names.get(j).getVariable().getName())] = data.get(j);
                }
            }
            // find the same row in the cpt
            double temp = names.get(i).get_percent(same_row);
            sum *= temp;
            if (i != 0) count_multi++;
        }
        Object[] ret = {sum, 0 , count_multi};
        return ret;
    }

    /**
     * Gets a row of values and looks for the probability found in this row in the table
     * @param same_row the row of values
     * @return The row probability corresponding to the given row
     */
    public Double get_percent(String[] same_row){
        for (int i = 0; i < this.cpt.getP().size(); i++) {
            boolean same = true;
            for (int j = 0; j < this.cpt.getName_of_parents().size() && same; j++) {
                if (! this.cpt.getGiven().get(j).get(i).equals(same_row[j])) {
                    same = false;
                }
            }
            if (same == true) {
                return this.cpt.getP().get(i);

            }
        }
        return Double.MIN_VALUE;
    }


    public CPT getCpt() {
        return cpt;
    }

    public Variable getVariable() {
        return this.variable;
    }

    public void setParents(BayesianNetworkNode node){
        this.parents.add(node);
    }

    public void setCpt(Variable variable, ArrayList<BayesianNetworkNode> parents, String table){
        this.cpt = new CPT(variable , parents , table);
    }

    public ArrayList<BayesianNetworkNode> getParents() {
        return parents;
    }
    @Override
    public String toString() {
        return "BayesianNetworkNode{" +
                "variable=" + variable +
                ", parents=" + parents.size() +
                '}';
    }

}


