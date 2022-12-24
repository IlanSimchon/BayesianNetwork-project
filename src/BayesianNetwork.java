import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.io.File;
import javax.xml.parsers.ParserConfigurationException;

public class BayesianNetwork {

    public ArrayList<BayesianNetworkNode> BayesianNodes;
    public BayesianNetwork(String input) {
        this.BayesianNodes = new ArrayList<>();
        readXml(input);
    }

    public  BayesianNetwork(BayesianNetwork other){
        this.BayesianNodes = new ArrayList<>();
        for (int i = 0; i < other.getBayesianNodes().size() ; i++) {
            this.BayesianNodes.add(new BayesianNetworkNode(other.getBayesianNodes().get(i)));
        }
    }

    /**
     *This function receives a path to the xml file and builds from it all the nodes of the Bayesian network
     * @param input - path to xml file
     */
    public void readXml(String input) {
        ArrayList<Variable> variables = new ArrayList<>();

        try {
            URL fileURL= BayesianNetwork.class.getResource(input);
            File xml = new File(fileURL.toURI());
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xml);
            doc.getDocumentElement();
            NodeList var = doc.getElementsByTagName("VARIABLE");
            for (int i = 0; i < var.getLength(); i++) {
                Node node = var.item(i);
                if (node.getNodeType() == node.ELEMENT_NODE) {
                    Element nodeElement = (Element) node;
                    String name = nodeElement.getElementsByTagName("NAME").item(0).getTextContent();
                    ArrayList outcomes = new ArrayList<>();
                    NodeList temp = nodeElement.getElementsByTagName("OUTCOME");
                    for (int j = 0; j < temp.getLength(); j++) {
                        outcomes.add(temp.item(j).getTextContent());
                    }
                    variables.add(new Variable(name, outcomes));
                }
            }
            NodeList definition = doc.getElementsByTagName("DEFINITION");
            ArrayList<NodeList> givens = new ArrayList();
            ArrayList<String> tables = new ArrayList<>();
            for (int i = 0; i < definition.getLength(); i++) {
                Node node = definition.item(i);
                if (node.getNodeType() == node.ELEMENT_NODE) {
                    Element nodeElement = (Element) node;
                    String name = nodeElement.getElementsByTagName("FOR").item(0).getTextContent();
                    NodeList given = nodeElement.getElementsByTagName("GIVEN");
                    givens.add(given);
                    String table = nodeElement.getElementsByTagName("TABLE").item(0).getTextContent();
                    tables.add(table);
                    this.BayesianNodes.add(new BayesianNetworkNode(variables.get(i), table));
                }


            }
            for (int i = 0; i < this.BayesianNodes.size(); i++) {
                set_parents(this.BayesianNodes.get(i), givens.get(i));
                this.BayesianNodes.get(i).setCpt(this.BayesianNodes.get(i).getVariable(), this.BayesianNodes.get(i).getParents(), tables.get(i));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SAXException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }


    /**
     * This function works after the construction of all the nodes is finished and its job is to collect for each node all the nodes that are its parents
     * @param node  The node for him is checking who his parents are
     * @param given  nodelist containing the parents
     */
    public void set_parents(BayesianNetworkNode node, NodeList given) {
        for (int i = 0; i < given.getLength(); i++) {
            for (int j = 0; j < this.BayesianNodes.size(); j++) {
                if (given.item(i).getTextContent().equals(this.BayesianNodes.get(j).getVariable().getName())) {
                    node.setParents(this.BayesianNodes.get(j));
                    break;
                }

            }
        }
    }
    /**
     * This function receives a query and returns the probability corresponding to that query by simple inference
     * @param queries  contain the query
     * @param name  the name of the query variable
     * @return The probability obtained plus the number of addition and multiplication operations done during the algorithm
     */
    public Object[] Simple_conclusion(HashMap<String , String> queries , String name) {
        for (int i = 0; i < this.BayesianNodes.size(); i++) {
            if (this.BayesianNodes.get(i).getVariable().getName().equals(name)) {
                Object[] answer = this.BayesianNodes.get(i).Simple_conclusion(queries, this.BayesianNodes, name);
                DecimalFormat temp = new DecimalFormat("#0.00000");
                answer[0] = temp.format(answer[0]);
                return answer;
            }
        }
        return null;
    }

    /**
     * This function receives a query and returns the probability corresponding to this query by variable elimination
     * @param queries contain the query
     * @param name the name of the query variable
     * @param algorithm Receives 1 or 2 and sends to the appropriate algorithm depending on input from the user
     * @return The probability obtained plus the number of addition and multiplication operations done during the algorithm
     */
    public Object[] VariableElimination(HashMap<String , String> queries , String name , char algorithm){
        int count_sum = 0;
        int count_multi = 0;
        ArrayList<String> evidence= new ArrayList<>();
        ArrayList<String> hidden = new ArrayList<>();

        // Unnecessary variables will be deleted from the network
        boolean delete = true;
        while (delete) {
            delete = delete_nodes(queries);
        }
        // We will build the copied cpt of the network
        ArrayList<CPT> all_cpt = new ArrayList<>();
        for (int i = 0; i < this.BayesianNodes.size(); i++) {
            all_cpt.add(this.BayesianNodes.get(i).getCpt());
        }

        // Let's catalog the variables each into its own category
        for (int i = 0; i < this.BayesianNodes.size() ; i++) {
            if (! this.BayesianNodes.get(i).getVariable().getName().equals(name)) {
                if (queries.containsKey(this.BayesianNodes.get(i).getVariable().getName())) {
                    evidence.add(this.BayesianNodes.get(i).getVariable().getName());
                } else  {
                    hidden.add(this.BayesianNodes.get(i).getVariable().getName());
                }
            }
        }
        // We will go through all the given variables and delete the different lines from the given to that variable
        for (int i = 0; i < evidence.size(); i++) {
            ArrayList<CPT> all = all_factors(evidence.get(i), all_cpt);
            for (int j = 0; j < all.size(); j++) {
                all.get(j).del_rows(evidence.get(i), queries.get(evidence.get(i)));
                all.get(j).delete_col(evidence.get(i));
                if(all.get(j).getName_of_parents().size() == 0) {
                    all_cpt.remove(all_cpt.indexOf(all.get(j)));
                }
            }
        }

        // We will sort the hidden variable according to the type requested by the user
        Object[] keys_hidden = sort_hidden(hidden , algorithm , all_cpt);


        // We will go through the hidden variable in a loop and in each of them we will activate
        // join until there is one factor left, on which we will eliminate
        for (int i = 0; i < keys_hidden.length; i++) {
            ArrayList<CPT> all_contains = all_factors((String) keys_hidden[i] , all_cpt) ;
            sort_cpt(all_contains);

            count_multi += Join(all_contains, all_cpt);
            count_sum += Eliminate(all_contains , (String) keys_hidden[i]);


        }

        // We will collect all the remaining factors and the query variable is contained in them
        ArrayList<CPT> finish  = all_factors(name , all_cpt);
        count_multi += Join(finish, all_cpt);
        count_sum += Eliminate(all_cpt , name);

        // We will normalize the last factor
        CPT answer = finish.get(0);
        count_sum += normalize(answer);
        Object[] ret = new Object[3];

        // We will extract from the factor the value requested in the query
        for (int i = 0; i < answer.getP().size(); i++) {
            if(answer.getGiven().get(0).get(i).equals(queries.get(name))) {
                ret[0] = answer.getP().get(i);
            }
        }

        DecimalFormat temp = new DecimalFormat("#0.00000");
        ret[0] = temp.format(ret[0]);
        ret[1] = String.valueOf(count_sum);
        ret[2] = String.valueOf(count_multi);

        return ret;
    }

    /**
     * This function deletes from the network all the nodes that are defined as leaf and are not evidence or query
     * @param queries contain the query
     * @return true if any node is deleted, otherwise false
     */
    private boolean delete_nodes(HashMap queries){
        boolean delaeted = false;
        for (int i = 0; i < this.BayesianNodes.size(); i++) {
            String name = this.BayesianNodes.get(i).getVariable().getName();
            if (! queries.containsKey(name) && just_one(name)){
                this.BayesianNodes.remove(i);
                i--;
                delaeted = true;
            }
        }
        return delaeted;
    }

    /**
     * This function examines whether a certain variable appears in only one factor
     * @param name the variable name
     * @return true if it appears in only one factor, otherwise false
     */
    private boolean just_one(String name){
        int count = 0;
        for (int i = 0; i < this.BayesianNodes.size() ; i++) {
            if(this.BayesianNodes.get(i).getCpt().getName_of_parents().contains(name)){
                count ++;
            }
        }
        if (count == 1) return true;
        return false;
    }

    /**
     * This function collects all the factors in which a certain variable appears
     * @param k the variable name
     * @param all_cpt all the factors
     * @return A list of all the factors that the variable contains
     */
    private ArrayList all_factors(String k , ArrayList<CPT> all_cpt)   {
        ArrayList<CPT> all = new ArrayList<>();
        for (int i = 0; i < all_cpt.size() ; i++) {
            if (all_cpt.get(i).getName_of_parents().contains(k)){
                all.add(all_cpt.get(i));
            }
            sort_cpt(all);
        }
        return all;
    }

    /**
     * This function that sorts a list of factors according to our implementation of the comparable interface
     * @param factors list of factors
     */
    public void sort_cpt(ArrayList<CPT> factors){
        Collections.sort(factors);
    }

    /**
     * This function that sorts a list of variables according to the sort type the user instructed us
     * @param hidden list of variables
     * @param algorithm the sort type from the user
     * @param all_cpt list of cpt
     * @return the sort list
     */
    public Object[] sort_hidden(ArrayList<String> hidden,  char algorithm , ArrayList all_cpt){
        Object[] sort_hid = hidden.toArray();
        switch(algorithm) {
            case '2': {
                Arrays.sort(sort_hid);
                break;
            }
            case '3': {
                ArrayList count_cpt = new ArrayList<>();
                // For each variable we will check the number of factors in which it appears and
                // insert it accordingly in its place in the list
                for (int i = 0; i < hidden.size(); i++) {
                    int size = all_factors(hidden.get(i) , all_cpt).size();
                    boolean flag = true;
                    for (int j = 0; j < i; j++) {
                        int temp = all_factors(hidden.get(j) , all_cpt).size();
                        if(size < temp && flag){
                            count_cpt.add(j ,hidden.get(i));
                            flag = false;
                        }
                    }
                    if(flag)
                        count_cpt.add(hidden.get(i));
                }
                // copy the arraylist to array
                for (int i = 0; i < count_cpt.size(); i++) {
                    sort_hid[i] = count_cpt.get(i);
                }
            }
        }
        return sort_hid;
    }

    /**
     * This function combines all the factors in which a certain variable appears into one factor
     * @param factors  All the factors in which a certain variable appears
     * @param all_cpt all the factors
     * @return The number of multiplication operations performed in the function
     */
    public int Join(ArrayList<CPT> factors , ArrayList<CPT> all_cpt) {
        CPT joined = null;
        int count_multi = 0;
        // We will enter a loop that runs as long as there is more than one factor containing the current variable
        while (factors.size() > 1) {

            // We will build the new factor
            ArrayList all = new ArrayList();
            for (int i = 0; i < factors.get(0).getName_of_parents().size(); i++) {
                all.add(factors.get(0).getName_of_parents().get(i));
            }
            for (int i = 0; i < factors.get(1).getName_of_parents().size(); i++) {
                if (! all.contains(factors.get(1).getName_of_parents().get(i))) {
                    all.add(factors.get(1).getName_of_parents().get(i));
                }
            }
            int num_of_parents = all.size();
            int num_of_row = 1;
            ArrayList<BayesianNetworkNode> all_nodes = new ArrayList<>();
            for (int j = 0; j < all.size(); j++) {
                for (int i = 0; i < this.BayesianNodes.size(); i++) {
                    if (all.get(j).equals(this.BayesianNodes.get(i).getVariable().getName())) {
                        all_nodes.add(this.BayesianNodes.get(i));
                        num_of_row *= this.BayesianNodes.get(i).getVariable().getOutcomes().size();
                    }
                }
            }
            ArrayList<ArrayList<String>> given = new ArrayList<>();
            for (int i = 0; i < num_of_parents; i++) {
                given.add(new ArrayList<>());
            }
            // We will fill in all possible combinations
            int set_count = num_of_row;
            for (int j = 0; j < num_of_parents; j++) {
                set_count = set_count / all_nodes.get(j).getVariable().getOutcomes().size();
                int count = 0;
                int this_index = 0;
                for (int i = 0; i < num_of_row; i++) {
                    given.get(j).add((String) all_nodes.get(j).getVariable().getOutcomes().get(this_index));
                    count++;
                    if (count == set_count) {
                        this_index = (this_index + 1) % all_nodes.get(j).getVariable().getOutcomes().size();
                        count = 0;
                    }
                }
            }
            // We will fill the probabilities with 1 so that we can then multiply them by the results we get
            ArrayList P = new ArrayList<>();
            for (int i = 0; i < num_of_row; i++) {
                P.add(1);
            }

            joined = new CPT(given, P, all);
            HashMap<String, String> row;
            ArrayList<HashMap> all_rows = new ArrayList<>();
            for (int i = 0; i < num_of_row; i++) {
                row = new HashMap<>(); // hashmap for every row of the factor
                for (int j = 0; j < joined.getGiven().size(); j++) {
                    row.put(joined.getName_of_parents().get(j), joined.getGiven().get(j).get(i));
                }
                all_rows.add(row);
            }
            // We will find the corresponding row in the first factor
            for (int i = 0; i < joined.getP().size(); i++) {
                double multi = 0;
                for (int k = 0; k < factors.get(0).getP().size(); k++) {
                    boolean flag = true;
                    for (int j = 0; j < factors.get(0).getGiven().size(); j++) {
                        if (! factors.get(0).getGiven().get(j).get(k).equals(all_rows.get(i).get(factors.get(0).getName_of_parents().get(j)))) {
                            flag = false;
                        }
                    }
                    if (flag == true) {
                        multi += factors.get(0).getP().get(k);
                        break;
                    }
                }
                // We will find the corresponding row in the second factor
                for (int k = 0; k < factors.get(1).getP().size(); k++) {
                    boolean flag = true;
                    for (int j = 0; j < factors.get(1).getGiven().size(); j++) {
                        if (!factors.get(1).getGiven().get(j).get(k).equals(all_rows.get(i).get(factors.get(1).getName_of_parents().get(j)))) {
                            flag = false;
                        }
                    }
                    if (flag == true) {
                        multi *= factors.get(1).getP().get(k);
                        count_multi++;
                        break;
                    }
                }
                joined.getP().set(i, multi);
            }

            // remove the old factors from the main list
            if(all_cpt.contains(factors.get(1))) {
                all_cpt.remove(all_cpt.indexOf(factors.get(1)));
            }
            if(all_cpt.contains(factors.get(0))) {
                all_cpt.remove(all_cpt.indexOf(factors.get(0)));
            }

            // remove the old factors from the current list
            factors.remove(1);
            factors.remove(0);
            factors.add(joined);

            // We will re-sort the list for the next iteration
            sort_cpt(factors);
        }

        if(joined != null) {
            all_cpt.add(joined);
        }
        return count_multi;
    }

    /**
     * \This function reduces a certain factor by concatenating matching rows other than the current hidden variable
     * @param factor the factor to reduce
     * @param s the variable name
     * @return The number of connection operations made in the function
     */
    private int Eliminate(ArrayList<CPT> factor, String s) {
        int count_sum = 0;
        if(factor.get(0).getName_of_parents().size() > 1) {
            // We will delete the column of the current hidden variable
            factor.get(0).delete_col(s);
            // We will unite identical rows in the table
            for (int i = 0; i < factor.get(0).getP().size(); i++) {
                String[] row = new String[factor.get(0).getName_of_parents().size()];
                for (int j = 0; j < factor.get(0).getGiven().size(); j++) {
                    row[j] = factor.get(0).getGiven().get(j).get(i);
                }
                // For each row we will look for all rows identical to it
                for (int j = i + 1; j < factor.get(0).getP().size(); j++) {
                    boolean flag = true;
                    for (int k = 0; k < factor.get(0).getGiven().size(); k++) {
                        if (!factor.get(0).getGiven().get(k).get(j).equals(row[k])) {
                            flag = false;
                        }
                    }
                    if (flag) {
                        factor.get(0).getP().set(i, factor.get(0).getP().get(i) + factor.get(0).getP().get(j));
                        count_sum++;
                        factor.get(0).delete_row(j);
                        j--;
                    }


                }
            }
        }
        return  count_sum;
    }

    /**
     * This function gets a factor and normalizes it by dividing each of the cells by the sum of all cells
     * @param cpt the factor to normalize
     * @return The number of connection operations made in the function
     */
    public int normalize(CPT cpt){
        double sum = 0;
        int count_sum = 0;
        // sum of all rows
        for (int i = 0; i < cpt.getP().size(); i++) {
            sum += cpt.getP().get(i);
            if(i != 0) count_sum++;
        }
        // Divide each row by the amount
        for (int i = 0; i < cpt.getP().size(); i++) {
            cpt.getP().set(i , cpt.getP().get(i) / sum);
        }
        return  count_sum;
    }


    public ArrayList<BayesianNetworkNode> getBayesianNodes() {
        return BayesianNodes;
    }

    @Override
    public String toString () {
        return "BayesianNetwork{" +
                "variables=" + BayesianNodes +
                '}';
    }

}
