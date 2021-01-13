package serialize;

public class Connection {
    private String line;
    private String stationName;

    public Connection(String line, String stationName) {
        this.line = line;
        this.stationName = stationName;
    }

    public String getLine() {
        return line;
    }

    public String getStationName() {
        return stationName;
    }
}
