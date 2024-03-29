import java.util.ArrayList;

public class Variable {
    private String name;
    private ArrayList outcomes;

    public Variable(Variable other) {
    this.name = other.name;
    this.outcomes = new ArrayList<>(other.getOutcomes());
    }

    public Variable(String name , ArrayList outcomes){
        this.name = name;
        this.outcomes = outcomes;

    }

    public String getName() {
        return name;
    }

    public ArrayList getOutcomes() {
        return outcomes;


    }


    @Override
    public String toString() {
        return "Variable{" +
                "name='" + name + '\'' +
                ", outcomes=" + outcomes +
                '}';
    }

}
