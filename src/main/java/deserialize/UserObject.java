package deserialize;

import serialize.Connection;
import serialize.Line;
import java.util.List;
import java.util.TreeMap;

public class UserObject {

    TreeMap<String, List<String>> stations;
    List<Line> lines;
    List<List<Connection>> connections;

    public UserObject(TreeMap<String, List<String>> stations, List<Line> lines, List<List<Connection>> connections) {
        this.stations = stations;
        this.lines = lines;
        this.connections = connections;
    }

    public void printLinesAndStations () {

        System.out.println("Список линий Московского метрополитена | Кол-во станций");
        System.out.println("-------------------------------------------------------");

        for (Line line : lines) {
           String nameOfLine = line.getNumber().concat(" - ").concat(line.getName());
           System.out.printf("%-38s | %d станций\n",
            nameOfLine,
            stations.get(line.getNumber()).size());
        }
    }
}
