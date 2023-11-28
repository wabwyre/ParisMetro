import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ParisMetro {

    private final Graph metroGraph;

    public ParisMetro() {
        this.metroGraph = new Graph();
    }

    public void readFromFile(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                processLine(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processLine(String line) {
        // Assuming the format is "lineName station1 station2 station3 ..."
        String[] parts = line.split("\\s+");

        String lineName = parts[0];
        List<String> stations = Arrays.asList(parts).subList(1, parts.length);

        for (int i = 0; i < stations.size(); i++) {
            String currentStationName = stations.get(i);

            // Add the station if it doesn't exist
            metroGraph.addVertex(currentStationName);

            if (i > 0) {
                // Connect the current station with the previous one
                String previousStationName = stations.get(i - 1);
                metroGraph.addEdge(currentStationName, previousStationName, lineName);
                metroGraph.addEdge(previousStationName, currentStationName, lineName);
            }
        }
    }

    public Set<String> findStationsInSameLine(String stationName) {
        Vertex source = metroGraph.getVertex(stationName);
        if (source != null) {
            Set<String> stationsInSameLine = new HashSet<>();
            for (Edge edge : source.getEdges()) {
                stationsInSameLine.add(edge.getDestination().getName());
            }
            return stationsInSameLine;
        }
        return Collections.emptySet();
    }

    public void findShortestPath(String sourceStation, String destinationStation) {
        metroGraph.findShortestPath(sourceStation, destinationStation);
    }

    public void findShortestPathWithLineOut(String sourceStation, String destinationStation, String malfunctioningLine) {
        metroGraph.findShortestPathWithLineOut(sourceStation, destinationStation, malfunctioningLine);
    }

    public static void main(String[] args) {
        ParisMetro parisMetro = new ParisMetro();
        parisMetro.readFromFile("metro.txt"); // Replace "metro.txt" with your file path

        // Example usage for (i) Find stations in the same line
        Set<String> stationsInSameLine = parisMetro.findStationsInSameLine("StationA");
        System.out.println("Stations in the same line as StationA: " + stationsInSameLine);

        // Example usage for (ii) Find the shortest path
        System.out.println("Shortest path from StationA to StationB:");
        parisMetro.findShortestPath("StationA", "StationB");

        // Example usage for (iii) Find the shortest path with a malfunctioning line
        System.out.println("Shortest path from StationA to StationB with Line1 out:");
        parisMetro.findShortestPathWithLineOut("StationA", "StationB", "Line1");

        // Determine the minimum number of subway lines that must be broken
        System.out.println("Minimum number of subway line that must be broken:");
        parisMetro.findMinimumBrokenLines();
    }

    private void findMinimumBrokenLines() {
    }
}

class Vertex {
    private String name;
    private List<Edge> edges;

    public Vertex(String name) {
        this.name = name;
        this.edges = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }
}

class Edge {
    private Vertex destination;
    private String lineName;
    private int time; // Assuming time is the weight of the edge

    public Edge(Vertex destination, String lineName, int time) {
        this.destination = destination;
        this.lineName = lineName;
        this.time = time;
    }

    public Vertex getDestination() {
        return destination;
    }

    public String getLineName() {
        return lineName;
    }

    public int getTime() {
        return time;
    }
}