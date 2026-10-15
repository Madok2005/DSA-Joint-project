package com.balmelibrary.gui;

import com.balmelibrary.algorithms.SearchAlgorithms;
import com.balmelibrary.algorithms.SearchOutcome;
import com.balmelibrary.algorithms.SortAlgorithms;
import com.balmelibrary.algorithms.SortOutcome;
import com.balmelibrary.io.CsvDatasetLoader;
import com.balmelibrary.model.LibraryLocation;
import com.balmelibrary.model.LibraryResource;
import com.balmelibrary.model.RoadSegment;
import com.balmelibrary.model.ServiceRequestRecord;
import com.balmelibrary.structures.BinaryHeap;
import com.balmelibrary.structures.DynamicArray;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Java Swing frontend for the Balme Library Smart Service Operations Optimizer.
 *
 * The GUI does not reimplement the team's algorithms. It calls the existing
 * SortAlgorithms, SearchAlgorithms, DynamicArray, BinaryHeap and
 * CsvDatasetLoader classes and displays their results.
 */
public class MainGUI extends JFrame {

    private static final String APP_TITLE = "Balme Library Smart Service Operations Optimizer";

    private final LibraryLocation[] locations;
    private final RoadSegment[] roads;
    private final ServiceRequestRecord[] requests;
    private final LibraryResource[] resources;

    private final DatasetPanel datasetPanel;
    private final SortSearchPanel sortSearchPanel;
    private final LibraryMapPanel mapPanel;
    private final UrgentRequestsPanel urgentPanel;

    public MainGUI(Path dataDir) throws IOException {
        super(APP_TITLE);

        locations = CsvDatasetLoader.loadLocations(dataDir.resolve("locations.csv"));
        roads = CsvDatasetLoader.loadRoads(dataDir.resolve("roads.csv"));
        requests = CsvDatasetLoader.loadServiceRequests(dataDir.resolve("service_requests.csv"));
        resources = CsvDatasetLoader.loadResources(dataDir.resolve("resources.csv"));

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 780);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);

        datasetPanel = new DatasetPanel();
        sortSearchPanel = new SortSearchPanel();
        mapPanel = new LibraryMapPanel();
        urgentPanel = new UrgentRequestsPanel();

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 14));
        tabs.addTab("Datasets", datasetPanel);
        tabs.addTab("Sort & Search", sortSearchPanel);
        tabs.addTab("Library Map", mapPanel);
        tabs.addTab("Urgent Requests", urgentPanel);

        add(tabs, BorderLayout.CENTER);

        JLabel status = new JLabel(String.format(
                "  Loaded: %d locations  |  %d roads  |  %d service requests  |  %d resources",
                locations.length, roads.length, requests.length, resources.length));
        status.setBorder(new EmptyBorder(5, 5, 5, 5));
        add(status, BorderLayout.SOUTH);
    }

    private class DatasetPanel extends JPanel {
        DatasetPanel() {
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(12, 12, 12, 12));

            JLabel heading = heading("Dataset Viewer");
            add(heading, BorderLayout.NORTH);

            JTabbedPane dataTabs = new JTabbedPane();
            dataTabs.addTab("Locations (" + locations.length + ")", tableScroll(
                    new String[]{"ID", "Name", "Area", "Type", "X (m)", "Y (m)"},
                    locationRows()));
            dataTabs.addTab("Roads (" + roads.length + ")", tableScroll(
                    new String[]{"ID", "From", "To", "Distance (km)", "Time (min)", "Condition"},
                    roadRows()));
            dataTabs.addTab("Service Requests (" + requests.length + ")", tableScroll(
                    new String[]{"ID", "Source", "Destination", "Category", "Urgency", "Submitted", "Deadline", "Status"},
                    requestRows()));
            dataTabs.addTab("Resources (" + resources.length + ")", tableScroll(
                    new String[]{"ID", "Type", "Home Location", "Capacity", "Availability"},
                    resourceRows()));

            add(dataTabs, BorderLayout.CENTER);
        }

        private Object[][] locationRows() {
            Object[][] rows = new Object[locations.length][6];
            for (int i = 0; i < locations.length; i++) {
                LibraryLocation x = locations[i];
                rows[i] = new Object[]{x.locationId(), x.name(), x.area(), x.locationType(), x.xCoordM(), x.yCoordM()};
            }
            return rows;
        }

        private Object[][] roadRows() {
            Object[][] rows = new Object[roads.length][6];
            for (int i = 0; i < roads.length; i++) {
                RoadSegment x = roads[i];
                rows[i] = new Object[]{x.roadId(), x.fromLocationId(), x.toLocationId(), x.distanceKm(), x.travelTimeMin(), x.conditionWeight()};
            }
            return rows;
        }

        private Object[][] requestRows() {
            Object[][] rows = new Object[requests.length][8];
            for (int i = 0; i < requests.length; i++) {
                ServiceRequestRecord x = requests[i];
                rows[i] = new Object[]{x.requestId(), x.sourceLocationId(), x.destinationLocationId(), x.category(), x.urgency(), x.timeSubmitted(), x.deadline(), x.status()};
            }
            return rows;
        }

        private Object[][] resourceRows() {
            Object[][] rows = new Object[resources.length][5];
            for (int i = 0; i < resources.length; i++) {
                LibraryResource x = resources[i];
                rows[i] = new Object[]{x.resourceId(), x.resourceType(), x.homeLocationId(), x.capacity(), x.availabilityStatus()};
            }
            return rows;
        }
    }

    private class SortSearchPanel extends JPanel {
        private final JComboBox<String> datasetBox = new JComboBox<>(new String[]{
                "Locations", "Roads", "Service Requests", "Resources"
        });
        private final JComboBox<String> operationBox = new JComboBox<>(new String[]{"Sort", "Search"});
        private final JComboBox<String> algorithmBox = new JComboBox<>();
        private final JComboBox<String> compareBox = new JComboBox<>();
        private final JComboBox<String> storageBox = new JComboBox<>(new String[]{"Plain Array", "DynamicArray"});
        private final JTextField searchField = new JTextField();
        private final JButton runButton = new JButton("Run");
        private final JButton clearButton = new JButton("Clear");
        private final JTable resultTable = new JTable();
        private final JLabel comparisonsLabel = new JLabel("Comparisons: —");
        private final JLabel writesLabel = new JLabel("Writes/Swaps: —");
        private final JLabel timeLabel = new JLabel("Time: —");
        private final JLabel messageLabel = new JLabel("Select an operation and click Run.");

        SortSearchPanel() {
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(12, 12, 12, 12));

            add(heading("Sort & Search"), BorderLayout.NORTH);

            JPanel controls = new JPanel(new GridBagLayout());
            controls.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("Operation Settings"),
                    new EmptyBorder(8, 8, 8, 8)));
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(5, 5, 5, 5);
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 0;

            addRow(controls, c, 0, "Dataset:", datasetBox);
            addRow(controls, c, 1, "Operation:", operationBox);
            addRow(controls, c, 2, "Algorithm:", algorithmBox);
            addRow(controls, c, 3, "Compare/Search By:", compareBox);
            addRow(controls, c, 4, "Storage:", storageBox);

            searchField.setColumns(20);
            addRow(controls, c, 5, "Search Value:", searchField);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            runButton.setPreferredSize(new Dimension(100, 32));
            clearButton.setPreferredSize(new Dimension(100, 32));
            buttons.add(runButton);
            buttons.add(clearButton);
            addRow(controls, c, 6, "", buttons);

            add(controls, BorderLayout.WEST);

            JPanel center = new JPanel(new BorderLayout(8, 8));
            center.setBorder(BorderFactory.createTitledBorder("Results"));
            center.add(new JScrollPane(resultTable), BorderLayout.CENTER);

            JPanel stats = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 6));
            stats.add(comparisonsLabel);
            stats.add(writesLabel);
            stats.add(timeLabel);
            center.add(stats, BorderLayout.SOUTH);
            add(center, BorderLayout.CENTER);

            messageLabel.setBorder(new EmptyBorder(4, 4, 4, 4));
            add(messageLabel, BorderLayout.SOUTH);

            operationBox.addActionListener(e -> updateControls());
            datasetBox.addActionListener(e -> updateControls());
            runButton.addActionListener(e -> runOperation());
            clearButton.addActionListener(e -> clearResults());

            updateControls();
        }

        private void addRow(JPanel panel, GridBagConstraints c, int row, String label, Component component) {
            c.gridy = row;
            c.gridx = 0;
            c.weightx = 0;
            panel.add(new JLabel(label), c);
            c.gridx = 1;
            c.weightx = 1;
            panel.add(component, c);
        }

        private void updateControls() {
            boolean sorting = "Sort".equals(operationBox.getSelectedItem());
            algorithmBox.removeAllItems();
            if (sorting) {
                algorithmBox.addItem("Insertion Sort");
                algorithmBox.addItem("Selection Sort");
                algorithmBox.addItem("Merge Sort");
            } else {
                algorithmBox.addItem("Linear Search");
                algorithmBox.addItem("Binary Search");
            }

            updateCompareOptions();
            searchField.setEnabled(!sorting);
            searchField.setToolTipText(sorting ? "Disabled while sorting" : "Enter an exact value for the selected field");
            writesLabel.setVisible(sorting);
        }

        private void updateCompareOptions() {
            String dataset = (String) datasetBox.getSelectedItem();
            boolean sorting = "Sort".equals(operationBox.getSelectedItem());
            String previous = (String) compareBox.getSelectedItem();
            compareBox.removeAllItems();

            if ("Locations".equals(dataset)) {
                compareBox.addItem("Location ID");
                compareBox.addItem("Name");
                compareBox.addItem("Area");
                compareBox.addItem("X Coordinate");
                compareBox.addItem("Y Coordinate");
            } else if ("Roads".equals(dataset)) {
                compareBox.addItem("Road ID");
                compareBox.addItem("Distance");
                compareBox.addItem("Travel Time");
                compareBox.addItem("Condition Weight");
            } else if ("Service Requests".equals(dataset)) {
                compareBox.addItem("Request ID");
                compareBox.addItem("Urgency");
                compareBox.addItem("Category");
                compareBox.addItem("Status");
            } else {
                compareBox.addItem("Resource ID");
                compareBox.addItem("Resource Type");
                compareBox.addItem("Capacity");
                compareBox.addItem("Availability");
            }

            if (previous != null) compareBox.setSelectedItem(previous);
            if (compareBox.getSelectedIndex() < 0 && compareBox.getItemCount() > 0) compareBox.setSelectedIndex(0);
        }

        private void runOperation() {
            try {
                if ("Sort".equals(operationBox.getSelectedItem())) {
                    runSort();
                } else {
                    runSearch();
                }
            } catch (Exception ex) {
                messageLabel.setText("Error: " + ex.getMessage());
                JOptionPane.showMessageDialog(MainGUI.this, ex.getMessage(), "Operation Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void runSort() {
            String dataset = (String) datasetBox.getSelectedItem();
            String criterion = (String) compareBox.getSelectedItem();
            String algorithm = (String) algorithmBox.getSelectedItem();
            String storage = (String) storageBox.getSelectedItem();

            if ("Locations".equals(dataset)) {
                Comparator<LibraryLocation> cmp = locationComparator(criterion);
                LibraryLocation[] copy = locations.clone();
                SortOutcome outcome = sortArray(copy, algorithm, cmp);
                if ("DynamicArray".equals(storage)) {
                    DynamicArray<LibraryLocation> da = toDynamicArray(locations);
                    outcome = sortDynamicArray(da, algorithm, cmp);
                    displayLocations(toArray(da), outcome, storage, algorithm, criterion);
                } else {
                    displayLocations(copy, outcome, storage, algorithm, criterion);
                }
                showSortStats(outcome);
            } else if ("Roads".equals(dataset)) {
                Comparator<RoadSegment> cmp = roadComparator(criterion);
                RoadSegment[] copy = roads.clone();
                SortOutcome outcome;
                if ("DynamicArray".equals(storage)) {
                    DynamicArray<RoadSegment> da = toDynamicArray(roads);
                    outcome = sortDynamicArray(da, algorithm, cmp);
                    displayRoads(toArray(da), outcome, storage, algorithm, criterion);
                } else {
                    outcome = sortArray(copy, algorithm, cmp);
                    displayRoads(copy, outcome, storage, algorithm, criterion);
                }
                showSortStats(outcome);
            } else if ("Service Requests".equals(dataset)) {
                Comparator<ServiceRequestRecord> cmp = requestComparator(criterion);
                ServiceRequestRecord[] copy = requests.clone();
                SortOutcome outcome;
                if ("DynamicArray".equals(storage)) {
                    DynamicArray<ServiceRequestRecord> da = toDynamicArray(requests);
                    outcome = sortDynamicArray(da, algorithm, cmp);
                    displayRequests(toArray(da), outcome, storage, algorithm, criterion);
                } else {
                    outcome = sortArray(copy, algorithm, cmp);
                    displayRequests(copy, outcome, storage, algorithm, criterion);
                }
                showSortStats(outcome);
            } else {
                Comparator<LibraryResource> cmp = resourceComparator(criterion);
                LibraryResource[] copy = resources.clone();
                SortOutcome outcome;
                if ("DynamicArray".equals(storage)) {
                    DynamicArray<LibraryResource> da = toDynamicArray(resources);
                    outcome = sortDynamicArray(da, algorithm, cmp);
                    displayResources(toArray(da), outcome, storage, algorithm, criterion);
                } else {
                    outcome = sortArray(copy, algorithm, cmp);
                    displayResources(copy, outcome, storage, algorithm, criterion);
                }
                showSortStats(outcome);
            }
        }

        private void runSearch() {
            String dataset = (String) datasetBox.getSelectedItem();
            String criterion = (String) compareBox.getSelectedItem();
            String algorithm = (String) algorithmBox.getSelectedItem();
            String storage = (String) storageBox.getSelectedItem();
            String value = searchField.getText().trim();

            if (value.isEmpty()) {
                throw new IllegalArgumentException("Enter a search value first.");
            }

            if ("Locations".equals(dataset)) {
                Comparator<LibraryLocation> cmp = locationComparator(criterion);
                LibraryLocation target = locationTarget(criterion, value);
                if ("DynamicArray".equals(storage)) {
                    DynamicArray<LibraryLocation> da = toDynamicArray(locations);
                    SearchOutcome outcome = searchDynamicArray(da, target, algorithm, cmp);
                    displaySearchResult(outcome, outcome.found() ? da.get(outcome.index()).toString() : "Not found");
                } else {
                    LibraryLocation[] copy = locations.clone();
                    SearchOutcome outcome = searchArray(copy, target, algorithm, cmp);
                    displaySearchResult(outcome, outcome.found() ? copy[outcome.index()].toString() : "Not found");
                }
            } else if ("Roads".equals(dataset)) {
                Comparator<RoadSegment> cmp = roadComparator(criterion);
                RoadSegment target = roadTarget(criterion, value);
                if ("DynamicArray".equals(storage)) {
                    DynamicArray<RoadSegment> da = toDynamicArray(roads);
                    SearchOutcome outcome = searchDynamicArray(da, target, algorithm, cmp);
                    displaySearchResult(outcome, outcome.found() ? da.get(outcome.index()).toString() : "Not found");
                } else {
                    RoadSegment[] copy = roads.clone();
                    SearchOutcome outcome = searchArray(copy, target, algorithm, cmp);
                    displaySearchResult(outcome, outcome.found() ? copy[outcome.index()].toString() : "Not found");
                }
            } else if ("Service Requests".equals(dataset)) {
                Comparator<ServiceRequestRecord> cmp = requestComparator(criterion);
                ServiceRequestRecord target = requestTarget(criterion, value);
                if ("DynamicArray".equals(storage)) {
                    DynamicArray<ServiceRequestRecord> da = toDynamicArray(requests);
                    SearchOutcome outcome = searchDynamicArray(da, target, algorithm, cmp);
                    displaySearchResult(outcome, outcome.found() ? da.get(outcome.index()).toString() : "Not found");
                } else {
                    ServiceRequestRecord[] copy = requests.clone();
                    SearchOutcome outcome = searchArray(copy, target, algorithm, cmp);
                    displaySearchResult(outcome, outcome.found() ? copy[outcome.index()].toString() : "Not found");
                }
            } else {
                Comparator<LibraryResource> cmp = resourceComparator(criterion);
                LibraryResource target = resourceTarget(criterion, value);
                if ("DynamicArray".equals(storage)) {
                    DynamicArray<LibraryResource> da = toDynamicArray(resources);
                    SearchOutcome outcome = searchDynamicArray(da, target, algorithm, cmp);
                    displaySearchResult(outcome, outcome.found() ? da.get(outcome.index()).toString() : "Not found");
                } else {
                    LibraryResource[] copy = resources.clone();
                    SearchOutcome outcome = searchArray(copy, target, algorithm, cmp);
                    displaySearchResult(outcome, outcome.found() ? copy[outcome.index()].toString() : "Not found");
                }
            }
        }

        private <T> SortOutcome sortArray(T[] data, String algorithm, Comparator<T> cmp) {
            return switch (algorithm) {
                case "Insertion Sort" -> SortAlgorithms.insertionSort(data, cmp);
                case "Selection Sort" -> SortAlgorithms.selectionSort(data, cmp);
                case "Merge Sort" -> SortAlgorithms.mergeSort(data, cmp);
                default -> throw new IllegalArgumentException("Unknown sorting algorithm: " + algorithm);
            };
        }

        private <T> SortOutcome sortDynamicArray(DynamicArray<T> data, String algorithm, Comparator<T> cmp) {
            return switch (algorithm) {
                case "Insertion Sort" -> SortAlgorithms.insertionSort(data, cmp);
                case "Selection Sort" -> SortAlgorithms.selectionSort(data, cmp);
                case "Merge Sort" -> SortAlgorithms.mergeSort(data, cmp);
                default -> throw new IllegalArgumentException("Unknown sorting algorithm: " + algorithm);
            };
        }

        private <T> SearchOutcome searchArray(T[] data, T target, String algorithm, Comparator<T> cmp) {
            return switch (algorithm) {
                case "Linear Search" -> SearchAlgorithms.linearSearch(data, target, cmp);
                case "Binary Search" -> SearchAlgorithms.binarySearch(data, target, cmp);
                default -> throw new IllegalArgumentException("Unknown search algorithm: " + algorithm);
            };
        }

        private <T> SearchOutcome searchDynamicArray(DynamicArray<T> data, T target, String algorithm, Comparator<T> cmp) {
            return switch (algorithm) {
                case "Linear Search" -> SearchAlgorithms.linearSearch(data, target, cmp);
                case "Binary Search" -> SearchAlgorithms.binarySearch(data, target, cmp);
                default -> throw new IllegalArgumentException("Unknown search algorithm: " + algorithm);
            };
        }

        private void showSortStats(SortOutcome o) {
            comparisonsLabel.setText("Comparisons: " + o.comparisons());
            writesLabel.setText("Writes/Swaps: " + o.swapsOrWrites());
            timeLabel.setText("Time: " + formatNanos(o.elapsedNanos()));
            messageLabel.setText("Sort completed successfully.");
        }

        private void displaySearchResult(SearchOutcome o, String result) {
            comparisonsLabel.setText("Comparisons: " + o.comparisons());
            writesLabel.setText("Writes/Swaps: —");
            timeLabel.setText("Time: " + formatNanos(o.elapsedNanos()));
            String[] columns = {"Found", "Index", "Result"};
            Object[][] data = {{o.found() ? "YES" : "NO", o.index(), result}};
            resultTable.setModel(new DefaultTableModel(data, columns) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            });
            messageLabel.setText(o.found() ? "Search found a matching record." : "Search completed: no matching record found.");
        }

        private void displayLocations(LibraryLocation[] data, SortOutcome o, String storage, String algorithm, String criterion) {
            Object[][] rows = new Object[data.length][6];
            for (int i = 0; i < data.length; i++) {
                LibraryLocation x = data[i];
                rows[i] = new Object[]{x.locationId(), x.name(), x.area(), x.locationType(), x.xCoordM(), x.yCoordM()};
            }
            setResultModel(new String[]{"ID", "Name", "Area", "Type", "X (m)", "Y (m)"}, rows);
            messageLabel.setText(algorithm + " sorted Locations by " + criterion + " using " + storage + ".");
        }

        private void displayRoads(RoadSegment[] data, SortOutcome o, String storage, String algorithm, String criterion) {
            Object[][] rows = new Object[data.length][6];
            for (int i = 0; i < data.length; i++) {
                RoadSegment x = data[i];
                rows[i] = new Object[]{x.roadId(), x.fromLocationId(), x.toLocationId(), x.distanceKm(), x.travelTimeMin(), x.conditionWeight()};
            }
            setResultModel(new String[]{"ID", "From", "To", "Distance (km)", "Time (min)", "Condition"}, rows);
            messageLabel.setText(algorithm + " sorted Roads by " + criterion + " using " + storage + ".");
        }

        private void displayRequests(ServiceRequestRecord[] data, SortOutcome o, String storage, String algorithm, String criterion) {
            Object[][] rows = new Object[data.length][8];
            for (int i = 0; i < data.length; i++) {
                ServiceRequestRecord x = data[i];
                rows[i] = new Object[]{x.requestId(), x.sourceLocationId(), x.destinationLocationId(), x.category(), x.urgency(), x.timeSubmitted(), x.deadline(), x.status()};
            }
            setResultModel(new String[]{"ID", "Source", "Destination", "Category", "Urgency", "Submitted", "Deadline", "Status"}, rows);
            messageLabel.setText(algorithm + " sorted Service Requests by " + criterion + " using " + storage + ".");
        }

        private void displayResources(LibraryResource[] data, SortOutcome o, String storage, String algorithm, String criterion) {
            Object[][] rows = new Object[data.length][5];
            for (int i = 0; i < data.length; i++) {
                LibraryResource x = data[i];
                rows[i] = new Object[]{x.resourceId(), x.resourceType(), x.homeLocationId(), x.capacity(), x.availabilityStatus()};
            }
            setResultModel(new String[]{"ID", "Type", "Home Location", "Capacity", "Availability"}, rows);
            messageLabel.setText(algorithm + " sorted Resources by " + criterion + " using " + storage + ".");
        }

        private void setResultModel(String[] columns, Object[][] data) {
            resultTable.setModel(new DefaultTableModel(data, columns) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            });
            resultTable.setAutoCreateRowSorter(true);
        }

        private void clearResults() {
            resultTable.setModel(new DefaultTableModel());
            comparisonsLabel.setText("Comparisons: —");
            writesLabel.setText("Writes/Swaps: —");
            timeLabel.setText("Time: —");
            messageLabel.setText("Select an operation and click Run.");
            searchField.setText("");
        }
    }

    private class LibraryMapPanel extends JPanel {
        private final MapCanvas canvas = new MapCanvas();

        LibraryMapPanel() {
            setLayout(new BorderLayout(8, 8));
            setBorder(new EmptyBorder(12, 12, 12, 12));
            add(heading("Library Map"), BorderLayout.NORTH);
            add(canvas, BorderLayout.CENTER);
            JLabel note = new JLabel("Locations are drawn from the local X/Y coordinates in locations.csv; roads connect the stored location IDs.");
            note.setBorder(new EmptyBorder(4, 4, 4, 4));
            add(note, BorderLayout.SOUTH);
        }

        private class MapCanvas extends JPanel {
            MapCanvas() {
                setBackground(Color.WHITE);
                setPreferredSize(new Dimension(900, 600));
                setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (locations.length == 0) {
                    g2.dispose();
                    return;
                }

                double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
                double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
                for (LibraryLocation p : locations) {
                    minX = Math.min(minX, p.xCoordM());
                    maxX = Math.max(maxX, p.xCoordM());
                    minY = Math.min(minY, p.yCoordM());
                    maxY = Math.max(maxY, p.yCoordM());
                }

                double pad = 55;
                double width = Math.max(1, maxX - minX);
                double height = Math.max(1, maxY - minY);
                double scale = Math.min((getWidth() - 2 * pad) / width, (getHeight() - 2 * pad) / height);
                if (!Double.isFinite(scale) || scale <= 0) scale = 1;

                java.util.Map<String, Point2D.Double> points = new java.util.HashMap<>();
                for (LibraryLocation p : locations) {
                    double sx = pad + (p.xCoordM() - minX) * scale;
                    double sy = getHeight() - pad - (p.yCoordM() - minY) * scale;
                    points.put(p.locationId(), new Point2D.Double(sx, sy));
                }

                g2.setStroke(new BasicStroke(2f));
                g2.setColor(new Color(190, 190, 190));
                for (RoadSegment road : roads) {
                    Point2D a = points.get(road.fromLocationId());
                    Point2D b = points.get(road.toLocationId());
                    if (a != null && b != null) {
                        g2.drawLine((int) a.getX(), (int) a.getY(), (int) b.getX(), (int) b.getY());
                    }
                }

                g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
                for (LibraryLocation p : locations) {
                    Point2D point = points.get(p.locationId());
                    int x = (int) point.getX();
                    int y = (int) point.getY();
                    g2.setColor(new Color(45, 90, 150));
                    g2.fillOval(x - 5, y - 5, 10, 10);
                    g2.setColor(Color.DARK_GRAY);
                    g2.drawString(p.name() + " (" + p.locationId() + ")", x + 7, y - 7);
                }

                g2.dispose();
            }
        }
    }

    private class UrgentRequestsPanel extends JPanel {
        private final DefaultTableModel model = new DefaultTableModel(
                new String[]{"Rank", "Request ID", "Category", "Urgency", "Source", "Destination", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        private final JTable table = new JTable(model);

        UrgentRequestsPanel() {
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(12, 12, 12, 12));
            add(heading("Urgent Service Requests"), BorderLayout.NORTH);

            JButton refresh = new JButton("Refresh");
            refresh.setPreferredSize(new Dimension(110, 32));
            refresh.addActionListener(e -> refreshUrgentRequests());

            JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
            top.add(refresh);
            add(top, BorderLayout.WEST);

            table.setRowHeight(28);
            add(new JScrollPane(table), BorderLayout.CENTER);
            refreshUrgentRequests();
        }

        private void refreshUrgentRequests() {
            model.setRowCount(0);
            Comparator<ServiceRequestRecord> byUrgencyDescending =
                    Comparator.comparingInt(ServiceRequestRecord::urgency).reversed();
            BinaryHeap<ServiceRequestRecord> queue = new BinaryHeap<>(byUrgencyDescending);
            for (ServiceRequestRecord request : requests) queue.insert(request);

            for (int rank = 1; rank <= 3 && !queue.isEmpty(); rank++) {
                ServiceRequestRecord r = queue.extractMin();
                model.addRow(new Object[]{rank, r.requestId(), r.category(), r.urgency(),
                        r.sourceLocationId(), r.destinationLocationId(), r.status()});
            }
        }
    }

    private Comparator<LibraryLocation> locationComparator(String criterion) {
        return switch (criterion) {
            case "Location ID" -> Comparator.comparing(LibraryLocation::locationId);
            case "Name" -> Comparator.comparing(LibraryLocation::name, String.CASE_INSENSITIVE_ORDER);
            case "Area" -> Comparator.comparing(LibraryLocation::area, String.CASE_INSENSITIVE_ORDER);
            case "X Coordinate" -> Comparator.comparingDouble(LibraryLocation::xCoordM);
            case "Y Coordinate" -> Comparator.comparingDouble(LibraryLocation::yCoordM);
            default -> Comparator.naturalOrder();
        };
    }

    private Comparator<RoadSegment> roadComparator(String criterion) {
        return switch (criterion) {
            case "Road ID" -> Comparator.comparing(RoadSegment::roadId);
            case "Distance" -> Comparator.comparingDouble(RoadSegment::distanceKm);
            case "Travel Time" -> Comparator.comparingInt(RoadSegment::travelTimeMin);
            case "Condition Weight" -> Comparator.comparingDouble(RoadSegment::conditionWeight);
            default -> Comparator.naturalOrder();
        };
    }

    private Comparator<ServiceRequestRecord> requestComparator(String criterion) {
        return switch (criterion) {
            case "Request ID" -> Comparator.comparing(ServiceRequestRecord::requestId);
            case "Urgency" -> Comparator.comparingInt(ServiceRequestRecord::urgency);
            case "Category" -> Comparator.comparing(ServiceRequestRecord::category, String.CASE_INSENSITIVE_ORDER);
            case "Status" -> Comparator.comparing(ServiceRequestRecord::status, String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.naturalOrder();
        };
    }

    private Comparator<LibraryResource> resourceComparator(String criterion) {
        return switch (criterion) {
            case "Resource ID" -> Comparator.comparing(LibraryResource::resourceId);
            case "Resource Type" -> Comparator.comparing(LibraryResource::resourceType, String.CASE_INSENSITIVE_ORDER);
            case "Capacity" -> Comparator.comparingInt(LibraryResource::capacity);
            case "Availability" -> Comparator.comparing(LibraryResource::availabilityStatus, String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.naturalOrder();
        };
    }

    private LibraryLocation locationTarget(String criterion, String value) {
        return switch (criterion) {
            case "Location ID" -> new LibraryLocation(value, "", "", "", 0, 0);
            case "Name" -> new LibraryLocation("", value, "", "", 0, 0);
            case "Area" -> new LibraryLocation("", "", value, "", 0, 0);
            case "X Coordinate" -> new LibraryLocation("", "", "", "", Double.parseDouble(value), 0);
            case "Y Coordinate" -> new LibraryLocation("", "", "", "", 0, Double.parseDouble(value));
            default -> throw new IllegalArgumentException("Unsupported location search field.");
        };
    }

    private RoadSegment roadTarget(String criterion, String value) {
        return switch (criterion) {
            case "Road ID" -> new RoadSegment(value, "", "", 0, 0, 0);
            case "Distance" -> new RoadSegment("", "", "", Double.parseDouble(value), 0, 0);
            case "Travel Time" -> new RoadSegment("", "", "", 0, Integer.parseInt(value), 0);
            case "Condition Weight" -> new RoadSegment("", "", "", 0, 0, Double.parseDouble(value));
            default -> throw new IllegalArgumentException("Unsupported road search field.");
        };
    }

    private ServiceRequestRecord requestTarget(String criterion, String value) {
        return switch (criterion) {
            case "Request ID" -> new ServiceRequestRecord(value, "", "", "", 0, "", "", "");
            case "Urgency" -> new ServiceRequestRecord("", "", "", "", Integer.parseInt(value), "", "", "");
            case "Category" -> new ServiceRequestRecord("", "", "", value, 0, "", "", "");
            case "Status" -> new ServiceRequestRecord("", "", "", "", 0, "", "", value);
            default -> throw new IllegalArgumentException("Unsupported service request search field.");
        };
    }

    private LibraryResource resourceTarget(String criterion, String value) {
        return switch (criterion) {
            case "Resource ID" -> new LibraryResource(value, "", "", 0, "");
            case "Resource Type" -> new LibraryResource("", value, "", 0, "");
            case "Capacity" -> new LibraryResource("", "", "", Integer.parseInt(value), "");
            case "Availability" -> new LibraryResource("", "", "", 0, value);
            default -> throw new IllegalArgumentException("Unsupported resource search field.");
        };
    }

    private static <T> DynamicArray<T> toDynamicArray(T[] values) {
        DynamicArray<T> result = new DynamicArray<>();
        for (T value : values) result.add(value);
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T> T[] toArray(DynamicArray<T> values) {
        List<T> list = new ArrayList<>();
        for (T value : values) list.add(value);
        return list.toArray((T[]) java.lang.reflect.Array.newInstance(
                values.isEmpty() ? Object.class : values.get(0).getClass(), values.size()));
    }

    private static String formatNanos(long nanos) {
        if (nanos < 1_000) return nanos + " ns";
        if (nanos < 1_000_000) return String.format("%.3f µs", nanos / 1_000.0);
        return String.format("%.3f ms", nanos / 1_000_000.0);
    }

    private static JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 22));
        return label;
    }

    private static JScrollPane tableScroll(String[] columns, Object[][] data) {
        JTable table = new JTable(new DefaultTableModel(data, columns) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(24);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                            boolean focused, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, selected, focused, row, column);
                setBorder(new EmptyBorder(0, 5, 0, 5));
                return c;
            }
        });
        return new JScrollPane(table);
    }

    public static void main(String[] args) {
        Path dataDir = Path.of(args.length > 0 ? args[0] : "data");
        SwingUtilities.invokeLater(() -> {
            try {
                MainGUI gui = new MainGUI(dataDir);
                gui.setVisible(true);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null,
                        "Could not load the project datasets.\n" +
                                "Expected a data folder containing locations.csv, roads.csv,\n" +
                                "service_requests.csv and resources.csv.\n\n" + ex.getMessage(),
                        "Startup Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
