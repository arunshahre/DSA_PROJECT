import java.io.*;
import java.util.*;

/**
 * Course Prerequisite Planner (PS 54)
 * Concepts: Graph, DFS/BFS, Stack, Sorting, File Handling
 * A prerequisite edge A -> B means A must be completed before B.
 */
public class CoursePrerequisitePlanner {

    // Adjacency list: course -> list of courses that depend on it (sorted)
    static TreeMap<String, List<String>> graph = new TreeMap<>();
    static final String FILE_NAME = "prerequisites.txt";
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        loadData();
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt();
            switch (choice) {
                case 1: addCourse(); break;
                case 2: addPrerequisite(); break;
                case 3: showPrerequisites(); break;
                case 4: showDependents(); break;
                case 5:
                    if (hasCycle()) System.out.println("Cycle detected.");
                    else System.out.println("No cycle found.");
                    break;
                case 6: requestOrder(); break;
                case 7: viewGraph(); break;
                case 8:
                    saveData();
                    System.out.println("Data saved to " + FILE_NAME + ". Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Enter a number from 1 to 8.");
            }
        }
    }

    static void printMenu() {
        System.out.println("\n===== COURSE PREREQUISITE PLANNER =====");
        System.out.println("1. Add course");
        System.out.println("2. Add prerequisite (A -> B)");
        System.out.println("3. Show prerequisites of a course (BFS)");
        System.out.println("4. Show courses that depend on a course (DFS)");
        System.out.println("5. Detect cycle");
        System.out.println("6. Request course order");
        System.out.println("7. View graph");
        System.out.println("8. Exit and save");
        System.out.print("Enter choice: ");
    }

    // Reads a line and converts it to a trimmed, upper-case course code
    static String readCourse(String prompt) {
        System.out.print(prompt);
        if (!sc.hasNextLine()) return "";
        return sc.nextLine().trim().toUpperCase();
    }

    static int readInt() {
        if (!sc.hasNextLine()) return 8; // end of input: exit safely
        String line = sc.nextLine().trim();
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    static void addCourse() {
        String name = readCourse("Enter course code: ");
        if (name.isEmpty()) {
            System.out.println("Course code cannot be empty.");
        } else if (graph.containsKey(name)) {
            System.out.println("Course " + name + " already exists.");
        } else {
            graph.put(name, new ArrayList<>());
            System.out.println("Course " + name + " added.");
        }
    }

    static void addPrerequisite() {
        String a = readCourse("Enter prerequisite course (A): ");
        String b = readCourse("Enter course that needs it (B): ");
        if (!graph.containsKey(a)) {
            System.out.println("Course " + a + " not found. Add it first.");
            return;
        }
        if (!graph.containsKey(b)) {
            System.out.println("Course " + b + " not found. Add it first.");
            return;
        }
        if (a.equals(b)) {
            System.out.println("A course cannot be its own prerequisite.");
            return;
        }
        if (graph.get(a).contains(b)) {
            System.out.println("Prerequisite " + a + " -> " + b + " already exists.");
            return;
        }
        graph.get(a).add(b);
        Collections.sort(graph.get(a));
        System.out.println("Prerequisite added: " + a + " -> " + b);
        if (hasCycle()) {
            System.out.println("Warning: this prerequisite creates a cycle.");
        }
    }

    // BFS (with a Queue) over reversed edges: all direct and indirect prerequisites
    static void showPrerequisites() {
        String course = readCourse("Enter course code: ");
        if (!graph.containsKey(course)) {
            System.out.println("Course " + course + " not found.");
            return;
        }
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new LinkedHashSet<>();
        queue.add(course);
        while (!queue.isEmpty()) {
            String current = queue.poll();
            for (Map.Entry<String, List<String>> e : graph.entrySet()) {
                String node = e.getKey();
                if (e.getValue().contains(current) && !node.equals(course)
                        && !visited.contains(node)) {
                    visited.add(node);
                    queue.add(node);
                }
            }
        }
        if (visited.isEmpty()) {
            System.out.println(course + " has no prerequisites.");
        } else {
            System.out.println("Prerequisites of " + course + ": " + String.join(", ", visited));
        }
    }

    // DFS: all courses reachable from the given course (they depend on it)
    static void showDependents() {
        String course = readCourse("Enter course code: ");
        if (!graph.containsKey(course)) {
            System.out.println("Course " + course + " not found.");
            return;
        }
        Set<String> visited = new LinkedHashSet<>();
        dfsCollect(course, visited);
        visited.remove(course);
        if (visited.isEmpty()) {
            System.out.println("No course depends on " + course + ".");
        } else {
            System.out.println("Courses that depend on " + course + ": " + String.join(", ", visited));
        }
    }

    static void dfsCollect(String course, Set<String> visited) {
        if (!visited.add(course)) return;
        for (String next : graph.get(course)) {
            dfsCollect(next, visited);
        }
    }

    // Cycle detection using DFS: 0 = unvisited, 1 = in progress, 2 = finished
    static boolean hasCycle() {
        Map<String, Integer> state = new HashMap<>();
        for (String c : graph.keySet()) state.put(c, 0);
        for (String c : graph.keySet()) {
            if (state.get(c) == 0 && dfsCycle(c, state)) return true;
        }
        return false;
    }

    static boolean dfsCycle(String course, Map<String, Integer> state) {
        state.put(course, 1);
        for (String next : graph.get(course)) {
            if (state.get(next) == 1) return true;
            if (state.get(next) == 0 && dfsCycle(next, state)) return true;
        }
        state.put(course, 2);
        return false;
    }

    // Topological sort with a Stack. Returns null if a cycle exists.
    static List<String> topologicalOrder() {
        Stack<String> stack = new Stack<>();
        Set<String> visited = new HashSet<>();
        Set<String> onPath = new HashSet<>();
        for (String c : graph.keySet()) {
            if (!visited.contains(c) && !dfsTopo(c, visited, onPath, stack)) {
                return null;
            }
        }
        List<String> order = new ArrayList<>();
        while (!stack.isEmpty()) order.add(stack.pop());
        return order;
    }

    static boolean dfsTopo(String course, Set<String> visited, Set<String> onPath,
                           Stack<String> stack) {
        visited.add(course);
        onPath.add(course);
        for (String next : graph.get(course)) {
            if (onPath.contains(next)) return false; // cycle found
            if (!visited.contains(next) && !dfsTopo(next, visited, onPath, stack)) {
                return false;
            }
        }
        onPath.remove(course);
        stack.push(course); // pushed after all dependents are finished
        return true;
    }

    static void requestOrder() {
        if (graph.isEmpty()) {
            System.out.println("No courses available. Add courses first.");
            return;
        }
        int edges = 0;
        for (List<String> list : graph.values()) edges += list.size();
        if (edges == 0) {
            System.out.println("All courses can be taken in any order.");
            System.out.println("Courses: " + String.join(", ", graph.keySet()));
            return;
        }
        List<String> order = topologicalOrder();
        if (order == null) {
            System.out.println("Cycle detected. No valid course order.");
        } else {
            System.out.println("Valid order: " + String.join(", ", order));
        }
    }

    static void viewGraph() {
        if (graph.isEmpty()) {
            System.out.println("No courses added yet.");
            return;
        }
        System.out.println("Course -> courses that depend on it");
        for (Map.Entry<String, List<String>> e : graph.entrySet()) {
            String deps = e.getValue().isEmpty() ? "(none)" : String.join(", ", e.getValue());
            System.out.println(e.getKey() + " -> " + deps);
        }
    }

    // File format: "CS101" = course line, "CS101->CS201" = prerequisite line
    static void loadData() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            System.out.println("No saved data found. Starting with an empty planner.");
            return;
        }
        List<String> edgeLines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim().toUpperCase();
                if (line.isEmpty() || line.startsWith("#")) continue;
                if (line.contains("->")) edgeLines.add(line);
                else graph.putIfAbsent(line, new ArrayList<>());
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
            return;
        }
        for (String line : edgeLines) {
            String[] parts = line.split("->");
            if (parts.length != 2) {
                System.out.println("Skipped invalid line: " + line);
                continue;
            }
            String a = parts[0].trim();
            String b = parts[1].trim();
            if (!graph.containsKey(a) || !graph.containsKey(b) || a.equals(b)
                    || graph.get(a).contains(b)) {
                System.out.println("Skipped invalid line: " + line);
                continue;
            }
            graph.get(a).add(b);
            Collections.sort(graph.get(a));
        }
        System.out.println("Loaded " + graph.size() + " course(s) from " + FILE_NAME + ".");
    }

    static void saveData() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (String c : graph.keySet()) pw.println(c);
            for (Map.Entry<String, List<String>> e : graph.entrySet()) {
                for (String next : e.getValue()) {
                    pw.println(e.getKey() + "->" + next);
                }
            }
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }
}