package serialize;

import java.util.List;
import java.util.TreeMap;

public class Metro {

    TreeMap<String, List<String>> stations;
    List<Line> lines;
    List<List<Connection>> connections;

    public Metro(TreeMap<String, List<String>> stations,
                 List<Line> lines,
                 List<List<Connection>> connections) {
        this.stations = stations;
        this.lines = lines;
        this.connections = connections;
    }

}
