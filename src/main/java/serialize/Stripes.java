package serialize;

import java.util.Map;

public class Stripes {
    private String indexOfStation = "";
    private String lineNumber;
    private String lineName;
    private String stationName;
    private String stationClosed;
    private Map<String, String> connections;




    public Stripes (String indSt, String lNum, String lName, String sName, String sClosed, Map<String, String> conn) {
        this.indexOfStation = indSt;
        this.lineNumber = lNum;
        this.lineName = lName;
        this.stationName = sName;
        this.stationClosed = sClosed;
        this.connections = conn;
    }

    public String getIndexOfStation() {
        return indexOfStation;
    }

    public String getLineNumber() {
        return lineNumber;
    }
    public String getLineName() {
        return lineName;
    }
    public String getStationName() {
        return stationName;
    }
    public String getStationClosed() {
        return stationClosed;
    }

    public Map<String, String> getConnections() {
        return connections;
    }


}
