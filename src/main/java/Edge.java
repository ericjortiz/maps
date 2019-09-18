import java.util.ArrayList;
public class Edge {

    private long id;
    private String name;
    private ArrayList<Long> possibleConnections;
    public Edge(long id) {
        this.id = id;
        this.name = "";
        this.possibleConnections = new ArrayList<>();
    }

    public long getID() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addPossible(Long x) {
        possibleConnections.add(x);
    }

    public ArrayList<Long> getPossibleConnections() {
        return possibleConnections;
    }
}
