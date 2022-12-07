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
import java.util.ArrayList;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import javax.xml.parsers.ParserConfigurationException;

public class BayesianNetwork {
    public ArrayList<BayesianNetworkNode> BayesianNodes = new ArrayList();
    // public HashMap<String , BayesianNetworkNode> BayesianNodes;
    public BayesianNetwork(String input) {
        this.BayesianNodes = new ArrayList<>();
        readXml(input);
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

//    public String Simple_deduction(HashMap<String , String> queries) {
//        String name = queries.keySet().toArray()[0].toString();
//        for (int i = 0; i < this.BayesianNodes.size(); i++) {
//            if (this.BayesianNodes.get(i).getVariable().getName().equals(name)) {
//               return (this.BayesianNodes.get(i).Simple_deduction(queries, this.BayesianNodes).toString());
//
//            }
//        }
//        return "not found";
//    }
    public Object[] Simple_deduction(HashMap<String , String> queries , String name) {
        for (int i = 0; i < this.BayesianNodes.size(); i++) {
            if (this.BayesianNodes.get(i).getVariable().getName().equals(name)) {
               Object[] answer = this.BayesianNodes.get(i).Simple_deduction(queries, this.BayesianNodes, name);
               double temp = (Math.round((double)answer[0] * 100000));
               answer[0] = temp / 100000;
               return answer;
            }
        }
        return null;
        }



        @Override
        public String toString () {
            return "BayesianNetwork{" +
                    "variables=" + BayesianNodes +
                    '}';
        }

    }
