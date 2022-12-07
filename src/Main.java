import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, URISyntaxException {
        try {
            URL fileURL = Main.class.getResource("input.txt");
            File URI = new File(fileURL.toURI());
            FileReader fr = new FileReader(URI);
            BufferedReader br = new BufferedReader(fr);
            String input = br.readLine();
            BayesianNetwork bs = new BayesianNetwork(input);
//            System.out.println(bs.BayesianNodes.get(2).getVariable().getName());
//            System.out.println(bs.BayesianNodes.get(2).getCpt().getName_of_parents());
            String str = "";
            for (int i = 1; str != null; i++) {  // delete &&!
                str = br.readLine();
                System.out.println(str);
                if (str != null) {
                    char option = str.charAt(str.length() - 1);
                    str = str.substring(2, str.length() - 3);
                    String name = "";
                    for (int j = 0; str.charAt(j) != '='; j++) {
                        name += str.charAt(j);
                    }
                    HashMap<String, String> Querie = cleanQueries(str);
                    double direct = is_direct(bs , Querie , name);
                    if (direct != Double.MIN_VALUE){
                        System.out.println(direct);
                    }
                    else {
                        switch (option) {
                            case '1':
                                System.out.println(Arrays.toString(bs.Simple_deduction(Querie, name)));
                            case '2':
                                // bs.
                            case '3':
                                // bs.
                        }
                    }
                }
           }
            br.close();
        } catch (IOException ex) {
            System.out.print("Error reading file\n" + ex);
            System.exit(2);
        }
    }


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

    public static double is_direct(BayesianNetwork bs , HashMap <String , String> querie, String name){
        BayesianNetworkNode this_node = null;
        for (int i = 0; i < bs.BayesianNodes.size(); i++) {
            if (bs.BayesianNodes.get(i).getVariable().getName().equals(name)){
                this_node = bs.BayesianNodes.get(i);
            }
        }
        String[] row = new String[this_node.getCpt().getName_of_parents().size()+1];
        row[row.length-1] = querie.get(name);
            for (int i = 0; i < this_node.getCpt().getName_of_parents().size(); i++) {
                if (querie.containsKey(this_node.getCpt().getName_of_parents().get(i))) {
                    row[i] = querie.get(this_node.getCpt().getName_of_parents().get(i));
                } else {
                    return Double.MIN_VALUE;
                }
            }
                return this_node.get_precent(this_node.getCpt() , row);

    }
}


