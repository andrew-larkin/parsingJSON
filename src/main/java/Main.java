import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import deserialize.UserObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import serialize.Connection;
import serialize.Line;
import serialize.Metro;
import serialize.Stripes;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    private static int TABLE_VARIANT = 0;
    private static int TABLE_FIRST = 3;
    private static int TABLE_LAST = 5;
    private static int RANGES_INDEX_NULL = 0;
    private static int RANGES_INDEX_ONE = 1;
    private static int RANGES_INDEX_THREE = 3;

    private static Map<String, String> specialCases = new TreeMap<>(); //специальные случаи

    private static String jsonFile = "src/main/resources/map.json"; //путь к json файлу

    public static void main(String[] args) throws IOException {
        //заполнение мэпа специальными случаями
        specialCases.put("011А", "11А");
        specialCases.put("8А 11", "11");

        String url = "https://ru.wikipedia.org/wiki/%D0%A1%D0%BF%D0%B8%D1%81%D0%BE%D0%BA_" +
                "%D1%81%D1%82%D0%B0%D0%BD%D1%86%D0%B8%D0%B9_%D0%9C%D0%BE%D1%81%D0%BA%D0%" +
                "BE%D0%B2%D1%81%D0%BA%D0%BE%D0%B3%D0%BE_%D0%BC%D0%B5%D1%82%D1%80%D0%BE%D0%" +
                "BF%D0%BE%D0%BB%D0%B8%D1%82%D0%B5%D0%BD%D0%B0"; //ссылка на статью википедии
        Document doc = Jsoup.connect(url).get();
        List<Stripes> stripes = new ArrayList<>();
        createStripesOfStations(doc, stripes); //парсинг HTML
        serialize(stripes); //сериализация
        deserialize(); //десериализация
     }

    private static String getJsonFile(String url)
    {
        StringBuilder builder = new StringBuilder();
        try {
            List<String> lines = Files.readAllLines(Paths.get(url));
            lines.forEach(line -> builder.append(line));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return builder.toString();
    }
    private static void createStripesOfStations (Document doc, List<Stripes> stripes) {

        for (int tableNumber = TABLE_FIRST; tableNumber <= TABLE_LAST; tableNumber++) { //парсинг всех 3-х таблиц
            Element table = doc.select("table").get(tableNumber); //выделяем одну таблицу
            Elements rows = table.select("tr"); //выделяем строки в таблице

            if (tableNumber !=3) {TABLE_VARIANT = 1;} //МЦК и монорельс в хэдере имеют на одну строку больше

            for (int i = 1; i < rows.size() - TABLE_VARIANT; i++) { //проход по строкам таблицы
                Element row = rows.get(i + TABLE_VARIANT); //строка в таблице
                Elements ranges = row.select("td"); //разделение строки на ячейки

                String indexOfStation = ranges.get(RANGES_INDEX_NULL).text(); //индекс станции
                String lineNumber = "";
                for (Map.Entry<String, String> oneOfCases : specialCases.entrySet()) {
                    if (ranges.get(RANGES_INDEX_NULL).select("span").first().text().equals(oneOfCases.getKey())) {
                        lineNumber = oneOfCases.getValue();
                        break;
                } else {
                    lineNumber = ranges.get(RANGES_INDEX_NULL).select("span").first().text(); //номер линии
                } }
                String lineName = ranges.get(RANGES_INDEX_NULL).select("a").attr("title"); //название линии
                String stationName = ranges.get(RANGES_INDEX_ONE).select("a").first().text(); //название станции
                String stationOption = ranges.get(RANGES_INDEX_ONE).select("small").text(); //опциональная информация
                HashMap<String, String> connection = new HashMap<>(); //переходы между станциями
                parseConnections(ranges, connection, indexOfStation, stationName); //парсинг переходов между станциями

                //добавляем собранную информацию по каждой станции в список объектов Stripes
                stripes.add(new Stripes(indexOfStation, lineNumber, lineName, stationName, stationOption, connection));
            } }
    }
    private static void parseConnections(Elements ranges, HashMap<String, String> connection,
                                         String indexOfStation, String stationName) {

        if (!ranges.get(RANGES_INDEX_THREE).select("td")
                .attr("data-sort-value").equals("Infinity")) { //не рассматриваем станции без переходов
            int amountOfConnections = ranges.get(RANGES_INDEX_THREE).children().size(); //кол-во переходов
            Elements stripesOfConnections = ranges.get(RANGES_INDEX_THREE).children(); //выделяем составные части

            for (int t = 0; t < amountOfConnections; t += 2) {
                String connectionStationNumber = stripesOfConnections.get(t).text(); //выделяем индекс станции перехода
                String connectionName = stripesOfConnections.get(t+1).select("span")
                        .attr("title"); //выделяем наименование перехода, которое содержит название станции
                String [] nameToArray = connectionName.split("\\s"); //разделение наименования перехода по словам
                int ind = 0;
                if(connectionName.contains("кольца")) {
                    ind = nameToArray.length - 3; //случай для МЦК
                } else {
                    ind = nameToArray.length - 2; //случай для всех остальных
                }
                StringBuilder nameOfConnection = new StringBuilder(); //собираем название станции перехода
                for (int j = 3; j < ind; j ++) {
                    nameOfConnection.append(nameToArray[j]);
                    nameOfConnection.append(" ");
                }
                String connectionStationName = nameOfConnection.toString().trim(); //избавляемся от лишних пробелам
                connection.put(connectionStationNumber, connectionStationName); //добавляем переход в Map
                for (Map.Entry<String, String> oneOfCases : specialCases.entrySet()) {
                if (indexOfStation.contains(oneOfCases.getKey())) {
                    connection.put(oneOfCases.getValue(), stationName);
                    break;
                } }
            } }

    }
    private static void serialize(List<Stripes> stripes) {

        //приведение к необходимому формату Lines
        TreeMap<String, Line> linesTemp = new TreeMap<>(); //для линий
        for (Stripes stripe : stripes) { //добавляем в тримэп линии без станций
            linesTemp.put(stripe.getLineNumber(), new Line(stripe.getLineNumber(), stripe.getLineName()));
        }
        for (Stripes stripe : stripes) { //добавляем станции
            linesTemp.get(stripe.getLineNumber()).addStation(stripe.getStationName());
        }
        List<Line> lines = new ArrayList<>();
        lines.addAll(linesTemp.values());

        //приведение к необходимому формату Stations
        TreeMap<String, List<String>> stations = new TreeMap<>();
        for (Stripes stripe : stripes) {
            stations.put(stripe.getLineNumber(), linesTemp.get(stripe.getLineNumber()).getStations());
        }

        //приведение к необходимому формату Connections
        List<List<Connection>> connections = new ArrayList<>();
        for (Stripes stripe : stripes) {
            if (stripe.getConnections().size() > 0) {
                List<Connection> connectionOnStation = new ArrayList<>();
                connectionOnStation.add(new Connection(stripe.getLineNumber(), stripe.getStationName()));
                for (Map.Entry<String, String> conn : stripe.getConnections().entrySet()) {
                    connectionOnStation.add(new Connection(conn.getKey(), conn.getValue()));
                }
                connections.add(connectionOnStation);
            }
        }

        Metro metro = new Metro(stations, lines, connections); //объединение станций, переходов и линий к единый объект
        createJSON(jsonFile, metro); //создаем файл JSON
    }

    private static void createJSON(String jsonFile, Metro metro) {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        try(BufferedWriter bw = new BufferedWriter(new FileWriter(jsonFile,false)))
        {
            StringBuilder mapFiles = new StringBuilder();
            mapFiles.append(gson.toJson(metro));
            bw.write(String.valueOf(mapFiles));
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
        }
    }
    private static void deserialize() {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        UserObject fromJSON = gson.fromJson(getJsonFile(jsonFile), UserObject.class);
        fromJSON.printLinesAndStations();
    }
}
