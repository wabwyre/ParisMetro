import java.util.*;

// Graph class to represent the metro network
class Graph {
    private Map<String, Vertex> vertices;

    public Graph() {
        this.vertices = new HashMap<>();
    }

    // Add a new vertex to the graph
    public void addVertex(String vertexName) {
        vertices.putIfAbsent(vertexName, new Vertex(vertexName));
    }

    // Add a new edge to the graph
    public void addEdge(String sourceName, String destinationName, String lineName) {
        addEdge(sourceName, destinationName, lineName, 1); // Default time is 1
    }

    // Add a new edge to the graph with a specified time
    public void addEdge(String sourceName, String destinationName, String lineName, int time) {
        Vertex source = vertices.get(sourceName);
        Vertex destination = vertices.get(destinationName);

        if (source != null && destination != null) {
            source.addEdge(new Edge(destination, lineName, time));
            destination.addEdge(new Edge(source, lineName, time)); // Assuming bidirectional edges
        }
    }

    // Get a vertex by its name
    public Vertex getVertex(String vertexName) {
        return vertices.get(vertexName);
    }

    // Get the names of neighbors of a vertex
    public Set<String> getNeighborsNames(Vertex vertex) {
        Set<String> neighborNames = new HashSet<>();
        for (Edge edge : vertex.getEdges()) {
            neighborNames.add(edge.getDestination().getName());
        }
        return neighborNames;
    }

    // Find all stations belonging to the same line as a given station
    public Set<String> findStationsInSameLine(String stationName) {
        Vertex source = getVertex(stationName);
        if (source != null) {
            Set<String> stationsInSameLine = new HashSet<>();
            for (Edge edge : source.getEdges()) {
                stationsInSameLine.add(edge.getDestination().getName());
            }
            return stationsInSameLine;
        }
        return Collections.emptySet();
    }

    // Find the shortest path between two stations
    public List<String> findShortestPath(String sourceStation, String destinationStation) {
        return findShortestPathWithLineOut(sourceStation, destinationStation, null);
    }

    // Find the shortest path between two stations with a malfunctioning line
    public List<String> findShortestPathWithLineOut(String sourceStation, String destinationStation, String malfunctioningLine) {
        Vertex source = getVertex(sourceStation);
        Vertex destination = getVertex(destinationStation);

        if (source != null && destination != null) {
            Map<Vertex, PathInfo> pathInfoMap = new HashMap<>();
            PriorityQueue<PathInfo> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(PathInfo::getTotalTime));
            Set<Vertex> visited = new HashSet<>();

            priorityQueue.add(new PathInfo(source, 0, null));

            while (!priorityQueue.isEmpty()) {
                PathInfo currentPath = priorityQueue.poll();
                Vertex currentVertex = currentPath.getVertex();

                if (!visited.contains(currentVertex)) {
                    visited.add(currentVertex);
                    pathInfoMap.put(currentVertex, currentPath);

                    if (currentVertex == destination) {
                        // Destination reached, return the path
                        return constructPath(source, destination, pathInfoMap);
                    }

                    for (Edge edge : currentVertex.getEdges()) {
                        Vertex neighbor = edge.getDestination();
                        int time = currentPath.getTotalTime() + getTime(edge.getLineName(), malfunctioningLine);
                        PathInfo newPath = new PathInfo(neighbor, time, currentVertex);

                        if (!visited.contains(neighbor) && (!pathInfoMap.containsKey(neighbor) || time < pathInfoMap.get(neighbor).getTotalTime())) {
                            priorityQueue.add(newPath);
                        }
                    }
                }
            }
        }
        return Collections.emptyList(); // No path found
    }

    // Construct the path from source to destination based on pathInfoMap
    private List<String> constructPath(Vertex source, Vertex destination, Map<Vertex, PathInfo> pathInfoMap) {
        List<String> path = new ArrayList<>();
        Vertex currentVertex = destination;

        while (currentVertex != source) {
            path.add(currentVertex.getName());
            currentVertex = pathInfoMap.get(currentVertex).getPreviousVertex();
        }

        path.add(source.getName());
        Collections.reverse(path);
        return path;
    }

    // Get the time for an edge, considering a malfunctioning line
    private int getTime(String lineName, String malfunctioningLine) {
        // You can customize this method based on the actual time calculation
        // For simplicity, assume a constant time for each edge (station-to-station)
        if (malfunctioningLine != null && lineName.equals(malfunctioningLine)) {
            // If the line is not functioning, increase the time
            return 9999; // Some large value indicating unavailability
        }
        return 1; // Default time
    }

    // Find the minimum number of broken lines required to disconnect at least two stations
    public void findMinimumBrokenLines() {
        Set<String> brokenLines = new HashSet<>();
        Set<String> connectedStations = new HashSet<>();
        Set<String> allStations = vertices.keySet();

        for (String station : allStations) {
            if (!connectedStations.contains(station)) {
                Set<String> connectedStationsInLine = new HashSet<>();
                connectedStationsInLine.add(station);
                dfs(station, connectedStationsInLine, connectedStations);

                if (connectedStationsInLine.size() < allStations.size()) {
                    // This line needs to be broken
                    brokenLines.add(getLineForStation(station));
                }
            }
        }

        System.out.println("Minimum number of subway lines that must be broken: " + brokenLines.size());
        for (String brokenLine : brokenLines) {
            System.out.println("Broken Line: " + brokenLine);
            findDisconnectedStations(brokenLine);
        }
    }

    // Get the line associated with a station
    private String getLineForStation(String station) {
        for (Vertex vertex : vertices.values()) {
            if (vertex.getName().equals(station) && !vertex.getEdges().isEmpty()) {
                return vertex.getEdges().get(0).getLineName();
            }
        }
        return "";
    }

    // Depth-first search to find connected stations in a line
    private void dfs(String station, Set<String> connectedStationsInLine, Set<String> connectedStations) {
        connectedStations.add(station);

        for (Edge edge : getVertex(station).getEdges()) {
            String neighborStation = edge.getDestination().getName();
            if (!connectedStations.contains(neighborStation) && edge.getLineName().equals(getLineForStation(station))) {
                connectedStationsInLine.add(neighborStation);
                dfs(neighborStation, connectedStationsInLine, connectedStations);
            }
        }
    }

    // Find disconnected stations in a malfunctioning line
    private void findDisconnectedStations(String malfunctioningLine) {
        Set<String> allStations = vertices.keySet();
        for (String station1 : allStations) {
            for (String station2 : allStations) {
                if (!station1.equals(station2) && !isConnected(station1, station2, malfunctioningLine)) {
                    System.out.println("Disconnected Stations: " + station1 + " and " + station2);
                    return;
                }
            }
        }
    }

    // Check if two stations are connected considering a malfunctioning line
    private boolean isConnected(String station1, String station2, String malfunctioningLine) {
        List<String> path = findShortestPathWithLineOut(station1, station2, malfunctioningLine);
        return !path.isEmpty();
    }
}

// Class to store information about a path
class PathInfo {
    private Vertex vertex;
    private int totalTime;
    private Vertex previousVertex;

    public PathInfo(Vertex vertex, int totalTime, Vertex previousVertex) {
        this.vertex = vertex;
        this.totalTime = totalTime;
        this.previousVertex = previousVertex;
    }

    public Vertex getVertex() {
        return vertex;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public Vertex getPreviousVertex() {
        return previousVertex;
    }
}