// ============================================================
// Problema del Numero Cromatico 
//
// Algoritmos:
//   1. Greedy                 (solución base)
//   2. Backtracking           (solución exacta)
// ============================================================

import java.util.*;
import java.io.*;
import java.util.Locale;

public class ChromaticNumber {

    // ============================================================
    // Algoritmo greedy
    // ============================================================
    static class GraphGreedy {
        int V;                          // numero de vertices
        List<List<Integer>> graph;      // lista de adyacencia

        // Constructor
        GraphGreedy(int vertices) {
            V = vertices;
            graph = new ArrayList<>();
            for (int i = 0; i < V; i++) {
                graph.add(new ArrayList<>());
            }
        }

        // Agregar arista entre u y v (no dirigido)
        void addEdge(int u, int v) {
            graph.get(u).add(v);
            graph.get(v).add(u);
        }

        // Algoritmo Greedy 
        // Retorna: int[] donde index=nodo, value=color asignado
        int[] greedyColoring() {
            int[] colorMap = new int[V];
            Arrays.fill(colorMap, -1);  // -1 = sin color

            // Paso 1: crear lista (grado, nodo) y ordenar por grado descendente
            Integer[] order = new Integer[V];
            for (int i = 0; i < V; i++) order[i] = i;
            Arrays.sort(order, (a, b) -> graph.get(b).size() - graph.get(a).size());

            // Paso 2: asignar menor color disponible a cada nodo
            for (int vertex : order) {
                // Recoger colores de los vecinos
                Set<Integer> neighborColors = new HashSet<>();
                for (int neighbor : graph.get(vertex)) {
                    if (colorMap[neighbor] != -1) {
                        neighborColors.add(colorMap[neighbor]);
                    }
                }

                // Asignar el menor color no usado por vecinos
                int color = 0;
                while (neighborColors.contains(color)) color++;
                colorMap[vertex] = color;
            }
            return colorMap;
        }

        // Contar numero de colores distintos usados
        int countColors(int[] colorMap) {
            int max = 0;
            for (int c : colorMap) if (c > max) max = c;
            return max + 1;
        }
    }


    // ============================================================
    // Algoritmo backtracking
    // ============================================================
    static class GraphBacktracking {
        int V;
        List<List<Integer>> graph;
        int[] colors;                   // color asignado a cada nodo
        int chromaticNumber;
        int[] optimalColoring;

        // Constructor
        GraphBacktracking(int vertices) {
            V = vertices;
            graph = new ArrayList<>();
            for (int i = 0; i < V; i++) graph.add(new ArrayList<>());
            colors = new int[V];
            chromaticNumber = Integer.MAX_VALUE;
        }

        // Agregar arista entre u y v
        void addEdge(int u, int v) {
            graph.get(u).add(v);
            graph.get(v).add(u);
        }

        // Verifica si asignar 'color' al 'vertex' es valido
        boolean isSafe(int vertex, int color) {
            for (int neighbor : graph.get(vertex)) {
                if (colors[neighbor] == color) return false;
            }
            return true;
        }

        // Funcion recursiva de backtracking
        boolean graphColorUtil(int vertex, int kLimit) {
            // Caso base: todos los nodos coloreados
            if (vertex == V) return true;

            // Probar cada color del 1 al kLimit
            for (int color = 1; color <= kLimit; color++) {
                if (isSafe(vertex, color)) {
                    colors[vertex] = color;
                    if (graphColorUtil(vertex + 1, kLimit)) return true;
                    colors[vertex] = 0;  // backtrack
                }
            }
            return false;
        }

        // Encuentra el numero cromatico minimo
        int findChromaticNumber() {
            for (int k = 1; k <= V; k++) {
                Arrays.fill(colors, 0);
                if (graphColorUtil(0, k)) {
                    chromaticNumber = k;
                    optimalColoring = colors.clone();
                    return k;
                }
            }
            return V;
        }
    }


    // ============================================================
    // FUNCION: Generar grafo aleatorio 
    // ============================================================
    static List<int[]> generateRandomGraph(int n, double p, long seed) {
        List<int[]> edges = new ArrayList<>();
        Random rng = new Random(seed);

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (rng.nextDouble() < p) {
                    edges.add(new int[]{i, j});
                }
            }
        }
        return edges;
    }


    // ============================================================
    // MAIN
    // ============================================================
    public static void main(String[] args) throws IOException {

        System.out.println("==========================================");
        System.out.println(" CHROMATIC NUMBER - Implementacion Java  ");
        System.out.println("==========================================");

        // ----------------------------------------------------------
        // 1. EJEMPLO BASICO con grafo de 5 nodos
        // ----------------------------------------------------------
        System.out.println("\n--- Ejemplo basico (5 nodos) ---");

        // Greedy
        GraphGreedy g = new GraphGreedy(5);
        g.addEdge(0, 1); g.addEdge(0, 2);
        g.addEdge(1, 2); g.addEdge(1, 3);
        g.addEdge(2, 3); g.addEdge(3, 4);

        int[] greedyMap = g.greedyColoring();
        int numGreedy = g.countColors(greedyMap);
        System.out.println("Greedy -> colores usados: " + numGreedy);
        System.out.print("Coloracion: ");
        for (int i = 0; i < 5; i++) System.out.print("nodo" + i + "=" + greedyMap[i] + " ");
        System.out.println();

        // Backtracking
        GraphBacktracking gBt = new GraphBacktracking(5);
        gBt.addEdge(0, 1); gBt.addEdge(0, 2);
        gBt.addEdge(1, 2); gBt.addEdge(1, 3);
        gBt.addEdge(2, 3); gBt.addEdge(3, 4);

        int numBt = gBt.findChromaticNumber();
        System.out.println("Backtracking -> numero cromatico exacto: " + numBt);
        System.out.print("Coloracion optima: ");
        for (int i = 0; i < 5; i++) System.out.print("nodo" + i + "=" + gBt.optimalColoring[i] + " ");
        System.out.println();

        // ----------------------------------------------------------
        // 2. ANALISIS EXPERIMENTAL + EXPORTAR CSV
        // ----------------------------------------------------------
        System.out.println("\n--- Analisis Experimental ---");

        int[] tamanios = {5, 8, 10, 12, 15, 18, 20, 25, 30, 35};
        double probabilidad = 0.4;
        long seed = 42;
        double TIMEOUT = 300.0;   // 5 minutos en segundos

        // Abrir archivo CSV
        PrintWriter csvFile = new PrintWriter(new FileWriter("resultados_java.csv"));
        csvFile.println("n,tiempo_greedy,tiempo_backtrack,colores_greedy,colores_bt,timeout");

        // Encabezado tabla consola
        System.out.printf("%-5s %-14s %-18s %-12s %-12s%n",
            "n", "Greedy (s)", "Backtrack (s)", "Colores G", "Colores BT");
        System.out.println("-".repeat(65));

        for (int n : tamanios) {
            List<int[]> edges = generateRandomGraph(n, probabilidad, seed);

            // --- Greedy ---
            GraphGreedy gG = new GraphGreedy(n);
            for (int[] e : edges) gG.addEdge(e[0], e[1]);

            long startG = System.nanoTime();
            int[] gMap = gG.greedyColoring();
            long endG = System.nanoTime();
            double tG = (endG - startG) / 1e9;
            int ncG = gG.countColors(gMap);

            // --- Backtracking con timeout ---
            GraphBacktracking gB = new GraphBacktracking(n);
            for (int[] e : edges) gB.addEdge(e[0], e[1]);

            long startBt = System.nanoTime();
            int ncBt = -1;
            boolean timeout = false;

            for (int k = 1; k <= n; k++) {
                double elapsed = (System.nanoTime() - startBt) / 1e9;
                if (elapsed >= TIMEOUT) { timeout = true; break; }

                Arrays.fill(gB.colors, 0);
                if (gB.graphColorUtil(0, k)) {
                    ncBt = k;
                    gB.chromaticNumber = k;
                    gB.optimalColoring = gB.colors.clone();
                    break;
                }
            }

            long endBt = System.nanoTime();
            double tBt = (endBt - startBt) / 1e9;

            // Imprimir tabla en consola
            System.out.printf(Locale.US, "%-5d %-14.6f ", n, tG);
            if (timeout) {
                System.out.printf(Locale.US, "%-18s %-12d %-12s%n", "TIMEOUT(>5min)", ncG, "N/A");
            } else {
                System.out.printf(Locale.US, "%-18.6f %-12d %-12d%n", tBt, ncG, ncBt);
            }

            // Exportar fila al CSV - Locale.US fuerza punto decimal (no coma)
            csvFile.printf(Locale.US, "%d,%.9f,%.9f,%d,%d,%s%n",
                n, tG, tBt, ncG,
                timeout ? -1 : ncBt,
                timeout ? "true" : "false");
        }

        csvFile.close();
        System.out.println("\nResultados exportados a: resultados_java.csv");

        // ----------------------------------------------------------
        // 3. CASOS DE PRUEBA - PARTE 1: Correctitud
        // ----------------------------------------------------------
        System.out.println("\n" + "=".repeat(60));
        System.out.println("CASOS DE PRUEBA");
        System.out.println("=".repeat(60));
        System.out.println("\n--- Parte 1: Correctitud (chi conocido) ---");

        runTest("Caso 1: Grafo completo K4 (chi esperado = 4)", 4,
                new int[][]{{0,1},{0,2},{0,3},{1,2},{1,3},{2,3}}, 4);
        runTest("Caso 2: Ciclo par C4 (chi esperado = 2)", 4,
                new int[][]{{0,1},{1,2},{2,3},{3,0}}, 2);
        runTest("Caso 3: Ciclo impar C5 (chi esperado = 3)", 5,
                new int[][]{{0,1},{1,2},{2,3},{3,4},{4,0}}, 3);
        runTest("Caso 4: Grafo sin aristas, 5 nodos (chi esperado = 1)", 5,
                new int[][]{}, 1);
        runTest("Caso 5: Grafo estrella K1,4 (chi esperado = 2)", 5,
                new int[][]{{0,1},{0,2},{0,3},{0,4}}, 2);

        // ----------------------------------------------------------
        // CASOS DE PRUEBA - PARTE 2: Escalabilidad
        // ----------------------------------------------------------
        System.out.println("\n--- Parte 2: Escalabilidad (grafos grandes) ---");
        System.out.println("(Solo Greedy: Backtracking inviable para estos tamanios)");

        double TIMEOUT_ESC = 10.0;
        int[] tamaniosEsc = {50, 100, 200, 500};

        System.out.printf("%-6s %-14s %-10s %-22s%n",
            "n", "Greedy (s)", "Colores G", "Backtrack");
        System.out.println("-".repeat(56));

        for (int n : tamaniosEsc) {
            List<int[]> edgesEsc = generateRandomGraph(n, 0.4, 42);

            GraphGreedy gEsc = new GraphGreedy(n);
            for (int[] e : edgesEsc) gEsc.addEdge(e[0], e[1]);
            long startEsc = System.nanoTime();
            int[] mapEsc = gEsc.greedyColoring();
            double tEsc = (System.nanoTime() - startEsc) / 1e9;
            int ncEsc = gEsc.countColors(mapEsc);

            GraphBacktracking gBEsc = new GraphBacktracking(n);
            for (int[] e : edgesEsc) gBEsc.addEdge(e[0], e[1]);
            long startBtEsc = System.nanoTime();
            boolean toEsc = false;

            for (int k = 1; k <= n; k++) {
                double el = (System.nanoTime() - startBtEsc) / 1e9;
                if (el >= TIMEOUT_ESC) { toEsc = true; break; }
                Arrays.fill(gBEsc.colors, 0);
                if (gBEsc.graphColorUtil(0, k)) break;
            }

            String btStr = toEsc ? "TIMEOUT (>10s)" : "completado";
            System.out.printf(Locale.US, "%-6d %-14.6f %-10d %-22s%n",
                n, tEsc, ncEsc, btStr);
        }

        System.out.println("\nConclusion:");
        System.out.println("  - Greedy escala bien pero no garantiza el minimo.");
        System.out.println("  - Backtracking es exacto pero inviable para n > 35.");
        System.out.println("  - Confirma la naturaleza NP-hard del problema.");
        System.out.println("=".repeat(60));

        System.out.println("\nPrograma finalizado correctamente.");
    }

    // Metodo auxiliar para casos de prueba
    static void runTest(String nombre, int n, int[][] aristas, int esperado) {
        GraphGreedy gG = new GraphGreedy(n);
        GraphBacktracking gB = new GraphBacktracking(n);
        for (int[] e : aristas) { gG.addEdge(e[0], e[1]); gB.addEdge(e[0], e[1]); }
        int ncG = gG.countColors(gG.greedyColoring());
        int ncB = gB.findChromaticNumber();
        String res = (ncB == esperado) ? "PASS" : "FAIL (esperado " + esperado + ")";
        System.out.println("\n" + nombre);
        System.out.println("  Greedy    -> " + ncG + " colores");
        System.out.println("  Backtrack -> " + ncB + " colores  [" + res + "]");
    }
}
