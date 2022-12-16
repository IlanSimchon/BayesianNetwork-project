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
    public ArrayList<BayesianNetworkNode> BayesianNodes = new ArrayList();
    // public HashMap<String , BayesianNetworkNode> BayesianNodes;
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


    public void readXml(String input) {
        ArrayList<Variable> variables = new ArrayList<>();

        try {
            URL fileURL= Main.class.getResource(input);
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

    public Object[] Simple_deduction(HashMap<String , String> queries , String name) {
        for (int i = 0; i < this.BayesianNodes.size(); i++) {
            if (this.BayesianNodes.get(i).getVariable().getName().equals(name)) {
                Object[] answer = this.BayesianNodes.get(i).Simple_deduction(queries, this.BayesianNodes, name);
                DecimalFormat temp = new DecimalFormat("#0.00000");
                answer[0] = temp.format(answer[0]);
                return answer;
            }
        }
        return null;
    }
    public Object[] VariableElimination(HashMap<String , String> queries , String name , char algorithm){
        int count_sum = 0;
        int count_multi = 0;
      //  ArrayList<CPT> querie = new ArrayList<>();
        ArrayList<String> evidence= new ArrayList<>();
        ArrayList<String> hidden = new ArrayList<>();

        boolean delete = true;
        while (delete) {
            delete = delete_nodes(queries);
        }

        ArrayList<CPT> all_cpt = new ArrayList<>(); // נבנה את הcpt מועתק של הרשת
        for (int i = 0; i < this.BayesianNodes.size(); i++) {
            all_cpt.add(this.BayesianNodes.get(i).getCpt());
        }

        //  part 1
        for (int i = 0; i < this.BayesianNodes.size() ; i++) { // נחלק את המשתנים כל אחד לקטגוריה שלו
            if (!this.BayesianNodes.get(i).getVariable().getName().equals(name)) {
                if (queries.containsKey(this.BayesianNodes.get(i).getVariable().getName())) {
                    evidence.add(this.BayesianNodes.get(i).getVariable().getName());
                } else  {
                    hidden.add(this.BayesianNodes.get(i).getVariable().getName());
                }
            }
        }
        // part 3
        for (int i = 0; i < evidence.size(); i++) { // עובר על כל אחד מהevidence ובו מוחק את כל השורות הלא רלוונטיות
            ArrayList<CPT> all = all_factors(evidence.get(i), all_cpt);
            for (int j = 0; j < all.size(); j++) {
                all.get(j).del_rows(evidence.get(i), queries.get(evidence.get(i)));
                all.get(j).delete_col(evidence.get(i));
                if(all.get(j).getName_of_parents().size() == 0){
                    all_cpt.remove(all_cpt.indexOf(all.get(j)));
                }
            }
        }

        // part 4
        Object[] keys_hidden = sort_hidden(hidden , algorithm , all_cpt); // נמיין את הhidden לפי השיטה הנתונה ונשמור את הסדר במערך

        for (int i = 0; i < keys_hidden.length; i++) {
           ArrayList<CPT> all_contains = all_factors((String) keys_hidden[i] , all_cpt) ;
           sort_cpt(all_contains);

            count_multi += Join(all_contains, all_cpt);
           count_sum += Eliminate(all_contains , (String) keys_hidden[i]);


        }
       ArrayList<CPT> finish  = all_factors(name , all_cpt);
            count_multi += Join(finish, all_cpt);
            count_sum += Eliminate(all_cpt , name);

        CPT answer = finish.get(0);
        count_sum += normalize(answer);
        Object[] ret = new Object[3];
        for (int i = 0; i < answer.getP().size(); i++) {
            if(answer.getGiven().get(0).get(i).equals(queries.get(name))) {
                ret[0] = answer.getP().get(i);
            }
        }

//        double temp = (Math.round((double)ret[0] * 100000d));
//        ret[0] = temp / 100000d;
//String temp = String.format("%.5g%n" , ret[0]);
//        ret[0] = temp;
        DecimalFormat temp = new DecimalFormat("#0.00000");
       ret[0] = temp.format(ret[0]);
        ret[1] = String.valueOf(count_sum);
        ret[2] = String.valueOf(count_multi);
        return ret;
    }
private boolean delete_nodes(HashMap queries){
        boolean delaeted = false;
    for (int i = 0; i < this.BayesianNodes.size(); i++) {
        String name = this.BayesianNodes.get(i).getVariable().getName();
        if (! queries.containsKey(name) && just_one(name)){
           // System.out.println(this.BayesianNodes.get(i).getVariable().getName());
            this.BayesianNodes.remove(i);
            i--;
            delaeted = true;
        }
    }
    return delaeted;
}

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
    private ArrayList all_factors(String k , ArrayList<CPT> all_cpt)   { // מביא את כל הפקטורים שהמשתנה k נממא בהם
        ArrayList<CPT> all = new ArrayList<>();
        for (int i = 0; i < all_cpt.size() ; i++) {
            if (all_cpt.get(i).getName_of_parents().contains(k)){
                all.add(all_cpt.get(i));
            }
          sort_cpt(all);
        }
return all;
    }
    public void sort_cpt(ArrayList<CPT> factors){
       Collections.sort(factors);
    }

    public Object[] sort_hidden(ArrayList<String> hidden,  char algorithm , ArrayList all_cpt){
        Object[] sort_hid = hidden.toArray();
        switch(algorithm) {
            case '2': {
                Arrays.sort(sort_hid);
                break;
            }
            case '3': {
                ArrayList count_cpt = new ArrayList<>();
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
                for (int i = 0; i < count_cpt.size(); i++) {
                    sort_hid[i] = count_cpt.get(i);
                }
            }
        }
return sort_hid;
    }
    public int Join(ArrayList<CPT> factors , ArrayList<CPT> all_cpt) {
        CPT joined = null;
        int count_multi = 0;
           while (factors.size() > 1) {
//               factors.get(0).ShowCpt();
//               factors.get(1).ShowCpt();


            ArrayList all = new ArrayList();
            ArrayList both = new ArrayList();
            for (int i = 0; i < factors.get(0).getName_of_parents().size(); i++) {
                all.add(factors.get(0).getName_of_parents().get(i));
            }
            for (int i = 0; i < factors.get(1).getName_of_parents().size(); i++) {
                if (! all.contains(factors.get(1).getName_of_parents().get(i))) {
                    all.add(factors.get(1).getName_of_parents().get(i));
                } else {
                    both.add(factors.get(1).getName_of_parents().get(i));
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
            ArrayList P = new ArrayList<>();
            for (int i = 0; i < num_of_row; i++) {
                P.add(1);
            }

            joined = new CPT(given, P, all);
            HashMap<String, String> row = new HashMap<>();
            ArrayList<HashMap> all_rows = new ArrayList<>();
            for (int i = 0; i < num_of_row; i++) { // נגדיר hashmap לכל שורה בטבלה
                row = new HashMap<>();
                for (int j = 0; j < joined.getGiven().size(); j++) {
                    row.put(joined.getName_of_parents().get(j), joined.getGiven().get(j).get(i));
                }
                all_rows.add(row);
            }
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
//            factors.get(0).ShowCpt();
//            factors.get(1).ShowCpt();
//               System.out.println("the join");
//joined.ShowCpt();


       if(all_cpt.contains(factors.get(1))) {
                   all_cpt.remove(all_cpt.indexOf(factors.get(1)));
               }
            if(all_cpt.contains(factors.get(0))) {
                all_cpt.remove(all_cpt.indexOf(factors.get(0)));
            }


                factors.remove(1);
                factors.remove(0);
                factors.add(joined);

            sort_cpt(factors);
        }

        if(joined != null) {
            all_cpt.add(joined);
        }
        return count_multi;
    }

    private int Eliminate(ArrayList<CPT> factor, String s) {
        //System.out.println("start eliminate");
        int count_sum = 0;
        if(factor.get(0).getName_of_parents().size() > 1) {
            factor.get(0).delete_col(s);
            for (int i = 0; i < factor.get(0).getP().size(); i++) {
                String[] row = new String[factor.get(0).getName_of_parents().size()];
                for (int j = 0; j < factor.get(0).getGiven().size(); j++) {
                    row[j] = factor.get(0).getGiven().get(j).get(i);
                }
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
//        factor.get(0).ShowCpt();
//        System.out.println("finish eliminate");
        return  count_sum;
    }

    public int normalize(CPT cpt){
        double sum = 0;
        int count_sum = 0;
        for (int i = 0; i < cpt.getP().size(); i++) {
            sum += cpt.getP().get(i);
            if(i != 0) count_sum++;
        }
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
