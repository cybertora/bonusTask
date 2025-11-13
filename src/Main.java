import java.io.*;
import java.util.*;

class Edge implements Comparable<Edge> {
    int u, v, w;
    Edge(int u, int v, int w) {
        this.u = u;
        this.v = v;
        this.w = w;
    }
    @Override
    public int compareTo(Edge other) {
        return Integer.compare(this.w, other.w);
    }
    @Override
    public String toString() {
        return "(" + u + " — " + v + ", w=" + w + ")";
    }
}

class UnionFind {
    int[] parent, rank;
    UnionFind(int n) {
        parent = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;
    }
    int find(int x) {
        if (parent[x] != x) parent[x] = find(parent[x]);
        return parent[x];
    }
    boolean union(int x, int y) {
        int px = find(x), py = find(y);
        if (px == py) return false;
        if (rank[px] < rank[py]) {
            parent[px] = py;
        } else if (rank[px] > rank[py]) {
            parent[py] = px;
        } else {
            parent[py] = px;
            rank[px]++;
        }
        return true;
    }
}

public class Main {
    public static void main(String[] args) {
        List<Edge> edges = readGraph("input.txt");
        if (edges == null) return;

        int V = getVertexCount(edges);
        System.out.println("=== original graph ===");
        printEdges(edges);

        List<Edge> mst = kruskal(edges, V);
        System.out.println("\n=== MST ===");
        printEdges(mst);
        System.out.println("total weight MST: " + totalWeight(mst));

        if (mst.isEmpty()) {
            System.out.println("the graph is not connected — the MST does not exist.");
            return;
        }

        Edge removed = mst.remove(mst.size() - 1);
        System.out.println("\n=== delete edge ===");
        System.out.println("deleted: " + removed);

        UnionFind uf = new UnionFind(V);
        for (Edge e : mst) uf.union(e.u, e.v);

        List<Set<Integer>> components = new ArrayList<>();
        Map<Integer, Integer> compId = new HashMap<>();
        int id = 0;
        for (int i = 0; i < V; i++) {
            int root = uf.find(i);
            if (!compId.containsKey(root)) {
                compId.put(root, id++);
                components.add(new HashSet<>());
            }
            components.get(compId.get(root)).add(i);
        }

        System.out.println("\n=== сonnectivity components after deletion ===");
        for (int i = 0; i < components.size(); i++) {
            System.out.println("components " + i + ": " + components.get(i));
        }

        Edge replacement = findReplacementEdge(edges, removed, components, compId);
        if (replacement == null) {
            System.out.println("\nNO replacement edge WAS FOUND. The graph has become incoherent beyond repair.");
        } else {
            System.out.println("\n=== replacement edge ===");
            System.out.println("adding: " + replacement);
            mst.add(replacement);
            Collections.sort(mst);

            System.out.println("\n=== new MST ===");
            printEdges(mst);
            System.out.println("new total weight MST: " + totalWeight(mst));
        }
    }

    private static Edge findReplacementEdge(List<Edge> allEdges, Edge removed, List<Set<Integer>> components, Map<Integer, Integer> compId) {
        Edge best = null;
        int minWeight = Integer.MAX_VALUE;

        UnionFind uf = new UnionFind(components.size());
        for (Edge e : allEdges) {
            if (e.u == removed.u && e.v == removed.v && e.w == removed.w) continue;

            int c1 = compId.get(e.u);
            int c2 = compId.get(e.v);
            if (c1 != c2 && e.w < minWeight) {
                minWeight = e.w;
                best = e;
            }
        }
        return best;
    }

    private static List<Edge> kruskal(List<Edge> edges, int V) {
        List<Edge> mst = new ArrayList<>();
        Collections.sort(edges);
        UnionFind uf = new UnionFind(V);

        for (Edge e : edges) {
            if (uf.union(e.u, e.v)) {
                mst.add(e);
                if (mst.size() == V - 1) break;
            }
        }
        return mst;
    }

    private static int totalWeight(List<Edge> edges) {
        return edges.stream().mapToInt(e -> e.w).sum();
    }

    private static void printEdges(List<Edge> edges) {
        edges.forEach(e -> System.out.println("  " + e));
    }

    private static List<Edge> readGraph(String filename) {
        try (Scanner sc = new Scanner(new File(filename))) {
            int V = sc.nextInt();
            int E = sc.nextInt();
            List<Edge> edges = new ArrayList<>();
            for (int i = 0; i < E; i++) {
                int u = sc.nextInt();
                int v = sc.nextInt();
                int w = sc.nextInt();
                edges.add(new Edge(u, v, w));
            }
            System.out.println("viewed: " + V + " vertexes, " + E + " edges.");
            return edges;
        } catch (FileNotFoundException e) {
            System.err.println("file " + filename + " not found!");
            return null;
        }
    }

    private static int getVertexCount(List<Edge> edges) {
        int max = 0;
        for (Edge e : edges) {
            max = Math.max(max, Math.max(e.u, e.v));
        }
        return max + 1;
    }
}