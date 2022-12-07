import java.util.ArrayList;
import java.util.Arrays;
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


    public Variable getVariable() {
        return this.variable;
    }

    public void setParents(BayesianNetworkNode node){
        this.parents.add(node);
    }
    public CPT getCpt() {
        return cpt;
    }
    public void setCpt(Variable variable, ArrayList<BayesianNetworkNode> parents, String table){
        this.cpt = new CPT(variable , parents , table);
    }

    public ArrayList<BayesianNetworkNode> getParents() {
        return parents;
    }
    // כלום לא ברור כאן עדיין, צריך לראות כיצד ניתן להרכיב את כל הקומבינציות האפשריות בצורה קלה ולשלוח אותם לP ולסכום את כל התוצאות
    public Object[] Simple_deduction(HashMap<String , String> queries , ArrayList<BayesianNetworkNode> all_bs_net , String name) {
        int index_of_querie = Integer.MAX_VALUE;
        BayesianNetworkNode Node_of_querie = null;
        int count_sum = -1 , count_multi = 0;
        for (int i = 0; i < all_bs_net.size(); i++) {
            if(all_bs_net.get(i).variable.getName().equals(name))   {
                index_of_querie = i;
                Node_of_querie = all_bs_net.get(i);
            }
        }
        HashMap<BayesianNetworkNode, ArrayList> Unknown = new HashMap<>();
        int num_of_option = 1;
        for (int i = 0; i < all_bs_net.size(); i++) {
            if (!queries.containsKey(all_bs_net.get(i).variable.getName())) {
                Unknown.put(all_bs_net.get(i), all_bs_net.get(i).variable.getOutcomes()); // בודק כמה אופציות לקומבינציות יש
                num_of_option *= all_bs_net.get(i).variable.getOutcomes().size();
            }
        }
        ArrayList<ArrayList<ArrayList>> all_combination = new ArrayList<>();
        for (int i = 0; i < num_of_option; i++) {    // מכניס את כל הערכים הנתונים
            ArrayList<BayesianNetworkNode> names = new ArrayList<>();
            ArrayList<String> data = new ArrayList<>();
            for (int j = 0; j < queries.size(); j++) {
                for (int k = 0; k < all_bs_net.size(); k++) { // ממיר את זה לנוד דרך מציאתו ברשת
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
        int set_count = all_combination.size();
        for (int i = 0; i < Unknown.size(); i++) { // צריך להכניס את כל הקומבינציות האפשריות של המשתנים הלא נתונים
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
        double sum_of_mone = 0.0;
        double sum_of_mechane = 0.0;
        for (int i = 0; i < all_combination.size(); i++) {
            Object ret_mone[] =  P(all_combination.get(i).get(0), all_combination.get(i).get(1));
            sum_of_mone += (double)ret_mone[0];
            if(i != 0) count_sum++;
            count_sum += (int)ret_mone[1];
            count_multi += (int)ret_mone[2];
            for (int j = 0; j < all_bs_net.get(index_of_querie).variable.getOutcomes().size(); j++) {
                if (! all_bs_net.get(index_of_querie).variable.getOutcomes().get(j).equals(queries.get(name))) {
                    all_combination.get(i).get(1).remove(all_combination.get(i).get(0).indexOf(Node_of_querie));
                    all_combination.get(i).get(1).add(all_combination.get(i).get(0).indexOf(Node_of_querie), all_bs_net.get(index_of_querie).variable.getOutcomes().get(j));
                    Object[] ret_mechane = P(all_combination.get(i).get(0), all_combination.get(i).get(1));
                    sum_of_mechane += (double)ret_mechane[0];
                    if(j != 0) count_sum++;
                    count_sum += (int)ret_mechane[1];
                    count_multi += (int)ret_mechane[2];

                }
            }
        }
        sum_of_mechane += sum_of_mone;
        count_sum++;
        Object[] ret = {sum_of_mone / sum_of_mechane , count_sum , count_multi};

        return ret;

    }
    // מקבל רשימת נודים ורשימת ערכים תואמת (כל ערך מותאם לנוד) ומחזיר את כפולת ההסתברויות של כל נוד בהינתן הוריו
    public Object[] P(ArrayList<BayesianNetworkNode> names , ArrayList<String> data){
        int count_multi = 0;
        double sum = 1;
        for (int i = 0; i < names.size(); i++) {
            String[] same_row = new String[names.get(i).cpt.getGiven().size()]; // מערך באורך הרשימה של ההורים + הערך עצמו
            same_row[same_row.length-1] = data.get(i); // הכנסת הערך עצמו למיקום האחרון במערך בהתאם להגדרת הcpt
            for (int j = 0; j < names.size(); j++) {
                if(names.get(i).getParents().contains(names.get(j))) {
                    same_row[names.get(i).cpt.getName_of_parents().indexOf(names.get(j).getVariable().getName())] = data.get(j);
                }
            }
            double temp = get_precent(names.get(i).cpt , same_row);
            sum *= temp;
            if (i != 0) count_multi++;
        }
        Object[] ret = {sum, 0 , count_multi};
        return ret;
    }


    // מקבל את השורה שאנו מחפשים ומחזיר את הערך שלהשורה התואמת לה בדיוק בטבלה
    public Double get_precent(CPT cpt , String[] same_row){
        for (int i = 0; i < cpt.getP().size(); i++) {
            boolean same = true;
            for (int j = 0; j < cpt.getName_of_parents().size()+1 && same; j++) {
                if (! cpt.getGiven().get(j).get(i).equals(same_row[j])) {
                    same = false;
                }
            }
            if (same == true) {
                return cpt.getP().get(i);

            }
        }
        return Double.MIN_VALUE;
    }




    @Override
    public String toString() {
        return "BayesianNetworkNode{" +
                "variable=" + variable +
                ", parents=" + parents.size() +
                '}';
    }




    public ArrayList indexHM(HashMap<String , ArrayList> h , int index){
        return h.get(h.keySet().toArray()[index]);
    }

}


