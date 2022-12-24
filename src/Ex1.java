import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class Ex1 {
    public static void main(String[] args) throws URISyntaxException {
        try {
            URL fileURL = Ex1.class.getResource("input.txt");
            File URI = new File(fileURL.toURI());
            FileReader fr = new FileReader(URI);
            BufferedReader br = new BufferedReader(fr);
            String input = br.readLine();
            BayesianNetwork bs = new BayesianNetwork(input);
            String str = "";

            FileWriter writeFile = new FileWriter("output.txt");
            PrintWriter outs = new PrintWriter(writeFile);

            // As long as the file is not over
            for (int i = 1; str != null; i++) {
                str = br.readLine();
                if (str != null) {
                    char algorithm = str.charAt(str.length() - 1); // Saves which algorithm should be used
                    str = str.substring(2, str.length() - 3); // Cuts unnecessary parts from the row
                    // Saves the variable name of the query
                    String name = "";
                    for (int j = 0; str.charAt(j) != '='; j++) {
                        name += str.charAt(j);
                    }
                    HashMap<String, String> Querie = cleanQueries(str); // cleaning the line

                    // Checks whether the result can be returned without calculation directly from the table
                    double direct = is_direct(bs, Querie, name);
                    if (direct != Double.MIN_VALUE) {
                        outs.println(direct + ",0,0");
                    }
                    // Otherwise, sends the query to the requested algorithm
                    else {
                        if (algorithm == '1') {
                            outs.println(toFile(bs.Simple_conclusion(Querie, name)));
                        } else {
                            BayesianNetwork bs_copy = new BayesianNetwork(bs);
                            outs.println(toFile(bs_copy.VariableElimination(Querie, name, algorithm)));
                        }

                    }
                }
            }
            br.close();

            outs.close();
            writeFile.close();
        }
        catch (IOException ex) {
            System.out.print("Error reading file\n" + ex);
            System.exit(2);
        }
    }


    /**
     * this function cleaning the string of the query
     * @param s the query
     * @return hashmap with key = name , value = outcome
     */
    public static HashMap<String, String> cleanQueries(String s) {
        ArrayList<String> temp = new ArrayList();
        HashMap<String, String> querie = new HashMap<>();

        int start = 0, end = 0;
        String t = "";
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != '(' && s.charAt(i) != '=' && s.charAt(i) != '|' && s.charAt(i) != ',') {
                t = t + s.charAt(i);
            } else {
                temp.add(t);
                t = "";
            }
        }
        temp.add(t);

        for (int i = 0; i < temp.size(); i += 2) {
            querie.put(temp.get(i), temp.get(i + 1));
        }
        return querie;
    }

    /**
     * This function checks whether the query asked appears directly in the table and the answer can be retrieved without calculation
     * @param bs the BayesianNetwork
     * @param querie the querie
     * @param name the name of the query
     * @return The value from the table if it is indeed possible to copy it directly, otherwise double min value
     */
    public static double is_direct(BayesianNetwork bs, HashMap<String, String> querie, String name) {
        BayesianNetworkNode this_node = null;
        // search the node of the query
        for (int i = 0; i < bs.BayesianNodes.size(); i++) {
            if (bs.BayesianNodes.get(i).getVariable().getName().equals(name)) {
                this_node = bs.BayesianNodes.get(i);
            }
        }
        // If the amount of data is different from the amount of columns in the table of our variable will return false
        if (querie.size() != this_node.getParents().size() + 1) {
            return Double.MIN_VALUE;
        }
        // We will build a line of values corresponding to the data from the query and extract the corresponding value from the table
        String[] row = new String[this_node.getCpt().getName_of_parents().size() + 1];
        row[row.length - 1] = querie.get(name);
        for (int i = 0; i < this_node.getCpt().getName_of_parents().size(); i++) {
            // Makes sure that the datum is a parent of the qeury variable
            if (querie.containsKey(this_node.getCpt().getName_of_parents().get(i))) {
                row[i] = querie.get(this_node.getCpt().getName_of_parents().get(i));
            } else {
                return Double.MIN_VALUE;
            }
        }
        return this_node.get_percent(row);

    }

    /**\
     * This function writes the answer in the desired structure to the output file
     * @param arr the answer
     * @return the desired structure of the answer
     */
    public static String toFile(Object[] arr) {
        String s = (String) arr[0];
        for (int i = 1; i < arr.length; i++) {
            s = s + ',' + arr[i];
        }
        return s;
    }
}


