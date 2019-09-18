import java.util.ArrayList;
public class Node {
    private double lat;
    private double lon;
    private long id;
    private String name;
    private ArrayList<Long> neighborIDs;

    public Node(long id, double lat, double lon) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.name = "";
        this.neighborIDs = new ArrayList<>();
    }

    public long getID() {
        return id;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addNeighbor(Long x) {
        neighborIDs.add(x);
    }

    public ArrayList<Long> getNeighborIDs() {
        return neighborIDs;
    }
}
