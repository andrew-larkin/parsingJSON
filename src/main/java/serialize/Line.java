package serialize;

import java.util.ArrayList;
import java.util.List;

public class Line {

    private String number;
    private String name;
    private transient List<String> stations;

    public Line(String number, String name) {
        this.number = number;
        this.name = name;
        stations = new ArrayList<>();
    }

    public String getNumber()
    {
        return number;
    }

    public String getName()
    {
        return name;
    }

    public void addStation(String station)
    {
       stations.add(station);
    }

    public List<String> getStations()
    {
        return stations;
    }
}
