// ============================================================
// Problema del Numero Cromatico
//
// Algoritmos:
//   1. Greedy - Welsh Powell (solución base)
//   2. Backtracking           (solución exacta)
// ============================================================

#include <iostream>
#include <fstream>
#include <vector>
#include <algorithm>
#include <chrono>
#include <climits>
#include <iomanip>
#include <random>
#include <set>
#include <string>

using namespace std;

// ============================================================
//  Algoritmo greedy
// ============================================================
class GraphGreedy {
private:
    int V;
    vector<vector<int>> graph;

public:
    GraphGreedy(int vertices) {
        V = vertices;
        graph.resize(V);
    }

    void add_edge(int u, int v) {
        graph[u].push_back(v);
        graph[v].push_back(u);
    }

    pair<int, vector<int>> greedy_coloring() {
        vector<int> color_map(V, -1);

        // Ordenar nodos por grado descendente 
        vector<pair<int,int>> vertices_degrees;
        for (int i = 0; i < V; i++) {
            vertices_degrees.push_back({(int)graph[i].size(), i});
        }
        sort(vertices_degrees.begin(), vertices_degrees.end(), greater<pair<int,int>>());

        // Asignar menor color disponible a cada nodo
        for (auto& [degree, vertex] : vertices_degrees) {
            set<int> neighbor_colors;
            for (int neighbor : graph[vertex]) {
                if (color_map[neighbor] != -1) {
                    neighbor_colors.insert(color_map[neighbor]);
                }
            }
            int color = 0;
            while (neighbor_colors.count(color)) color++;
            color_map[vertex] = color;
        }

        int num_colors = *max_element(color_map.begin(), color_map.end()) + 1;
        return {num_colors, color_map};
    }
};


// ============================================================
//  Algoritmo backtracking
// ============================================================
class GraphBacktracking {
private:
    int V;
    vector<vector<int>> graph;

    bool is_safe(int vertex, int color) {
        for (int neighbor : graph[vertex]) {
            if (colors[neighbor] == color) return false;
        }
        return true;
    }

public:
    int chromatic_number;
    vector<int> optimal_coloring;
    vector<int> colors;

    GraphBacktracking(int vertices) {
        V = vertices;
        graph.resize(V);
        colors.resize(V, 0);
        chromatic_number = INT_MAX;
    }

    void add_edge(int u, int v) {
        graph[u].push_back(v);
        graph[v].push_back(u);
    }

    bool graph_color_util(int vertex, int k_limit) {
        if (vertex == V) return true;

        for (int color = 1; color <= k_limit; color++) {
            if (is_safe(vertex, color)) {
                colors[vertex] = color;
                if (graph_color_util(vertex + 1, k_limit)) return true;
                colors[vertex] = 0;  // backtrack
            }
        }
        return false;
    }

    int find_chromatic_number() {
        for (int k = 1; k <= V; k++) {
            fill(colors.begin(), colors.end(), 0);
            if (graph_color_util(0, k)) {
                chromatic_number = k;
                optimal_coloring = colors;
                return k;
            }
        }
        return V;
    }
};


// ============================================================
// FUNCION: Generar grafo aleatorio 
// ============================================================
vector<pair<int,int>> generate_random_graph(int n, double p, int seed) {
    vector<pair<int,int>> edges;
    mt19937 rng(seed);
    uniform_real_distribution<double> dist(0.0, 1.0);

    for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {
            if (dist(rng) < p) edges.push_back({i, j});
        }
    }
    return edges;
}


// ============================================================
// MAIN
// ============================================================
int main() {

    cout << "========================================" << endl;
    cout << " CHROMATIC NUMBER - Implementacion C++ " << endl;
    cout << "========================================" << endl;

    // ----------------------------------------------------------
    // 1. EJEMPLO BASICO con grafo de 5 nodos
    // ----------------------------------------------------------
    cout << "\n--- Ejemplo basico (5 nodos) ---" << endl;

    GraphGreedy g_greedy(5);
    g_greedy.add_edge(0, 1); g_greedy.add_edge(0, 2);
    g_greedy.add_edge(1, 2); g_greedy.add_edge(1, 3);
    g_greedy.add_edge(2, 3); g_greedy.add_edge(3, 4);

    auto [num_greedy, greedy_map] = g_greedy.greedy_coloring();
    cout << "Greedy -> colores usados: " << num_greedy << endl;
    cout << "Coloracion: ";
    for (int i = 0; i < 5; i++) cout << "nodo" << i << "=" << greedy_map[i] << " ";
    cout << endl;

    GraphBacktracking g_bt(5);
    g_bt.add_edge(0, 1); g_bt.add_edge(0, 2);
    g_bt.add_edge(1, 2); g_bt.add_edge(1, 3);
    g_bt.add_edge(2, 3); g_bt.add_edge(3, 4);

    int num_bt = g_bt.find_chromatic_number();
    cout << "Backtracking -> numero cromatico exacto: " << num_bt << endl;
    cout << "Coloracion optima: ";
    for (int i = 0; i < 5; i++) cout << "nodo" << i << "=" << g_bt.optimal_coloring[i] << " ";
    cout << endl;

    // ----------------------------------------------------------
    // 2. ANALISIS EXPERIMENTAL + CSV
    // ----------------------------------------------------------
    cout << "\n--- Analisis Experimental ---" << endl;

    vector<int> tamanios = {5, 8, 10, 12, 15, 18, 20, 25, 30, 35};
    double probabilidad = 0.4;
    int seed = 42;
    double TIMEOUT = 300.0;   // 5 minutos

    // Abrir archivo CSV para exportar resultados
    ofstream csv_file("resultados_cpp.csv");
    csv_file << "n,tiempo_greedy,tiempo_backtrack,colores_greedy,colores_bt,timeout" << endl;

    // Encabezado tabla consola
    cout << setw(5)  << "n"
         << setw(14) << "Greedy (s)"
         << setw(18) << "Backtrack (s)"
         << setw(12) << "Colores G"
         << setw(12) << "Colores BT"
         << endl;
    cout << string(65, '-') << endl;

    for (int n : tamanios) {
        auto edges = generate_random_graph(n, probabilidad, seed);

        // --- Greedy ---
        GraphGreedy g_g(n);
        for (auto& [u, v] : edges) g_g.add_edge(u, v);

        auto start_g = chrono::high_resolution_clock::now();
        auto [nc_g, _] = g_g.greedy_coloring();
        auto end_g = chrono::high_resolution_clock::now();
        double t_g = chrono::duration<double>(end_g - start_g).count();

        // --- Backtracking con timeout ---
        GraphBacktracking g_b(n);
        for (auto& [u, v] : edges) g_b.add_edge(u, v);

        auto start_bt = chrono::high_resolution_clock::now();
        int nc_bt = -1;
        bool timeout = false;

        for (int k = 1; k <= n; k++) {
            auto now = chrono::high_resolution_clock::now();
            double elapsed = chrono::duration<double>(now - start_bt).count();
            if (elapsed >= TIMEOUT) { timeout = true; break; }

            g_b.colors.assign(n, 0);
            if (g_b.graph_color_util(0, k)) {
                nc_bt = k;
                g_b.chromatic_number = k;
                g_b.optimal_coloring = g_b.colors;
                break;
            }
        }

        auto end_bt = chrono::high_resolution_clock::now();
        double t_bt = chrono::duration<double>(end_bt - start_bt).count();

        // Imprimir tabla en consola
        cout << setw(5) << n
             << setw(14) << fixed << setprecision(6) << t_g;
        if (timeout) {
            cout << setw(18) << "TIMEOUT(>5min)"
                 << setw(12) << nc_g
                 << setw(12) << "N/A";
        } else {
            cout << setw(18) << fixed << setprecision(6) << t_bt
                 << setw(12) << nc_g
                 << setw(12) << nc_bt;
        }
        cout << endl;

        // Exportar fila al CSV
        // Si hay timeout, guardamos -1 como tiempo y colores
        csv_file << n << ","
                 << fixed << setprecision(9) << t_g << ","
                 << fixed << setprecision(9) << t_bt << ","
                 << nc_g << ","
                 << (timeout ? -1 : nc_bt) << ","
                 << (timeout ? "true" : "false")
                 << endl;
    }

    csv_file.close();
    cout << "\nResultados exportados a: resultados_cpp.csv" << endl;

    // ----------------------------------------------------------
    // 3. CASOS DE PRUEBA - PARTE 1: Correctitud
    // ----------------------------------------------------------
    cout << "\n" << string(60, '=') << endl;
    cout << "CASOS DE PRUEBA" << endl;
    cout << string(60, '=') << endl;

    // Funcion auxiliar para correr un caso de prueba
    auto run_test = [](string nombre, int n,
                       vector<pair<int,int>> aristas, int esperado_bt) {
        GraphGreedy g_g(n);
        GraphBacktracking g_b(n);
        for (auto& [u, v] : aristas) {
            g_g.add_edge(u, v);
            g_b.add_edge(u, v);
        }
        auto [nc_g, _] = g_g.greedy_coloring();
        int nc_b = g_b.find_chromatic_number();
        string resultado = (nc_b == esperado_bt) ? "PASS" :
                           "FAIL (esperado " + to_string(esperado_bt) + ")";
        cout << "\n" << nombre << endl;
        cout << "  Greedy    -> " << nc_g << " colores" << endl;
        cout << "  Backtrack -> " << nc_b << " colores  [" << resultado << "]" << endl;
    };

    cout << "\n--- Parte 1: Correctitud (chi conocido) ---" << endl;

    // Caso 1: K4 - grafo completo 4 nodos -> chi = 4
    run_test("Caso 1: Grafo completo K4 (chi esperado = 4)", 4,
             {{0,1},{0,2},{0,3},{1,2},{1,3},{2,3}}, 4);

    // Caso 2: C4 - ciclo par -> chi = 2
    run_test("Caso 2: Ciclo par C4 (chi esperado = 2)", 4,
             {{0,1},{1,2},{2,3},{3,0}}, 2);

    // Caso 3: C5 - ciclo impar -> chi = 3
    run_test("Caso 3: Ciclo impar C5 (chi esperado = 3)", 5,
             {{0,1},{1,2},{2,3},{3,4},{4,0}}, 3);

    // Caso 4: Sin aristas -> chi = 1
    run_test("Caso 4: Grafo sin aristas, 5 nodos (chi esperado = 1)", 5,
             {}, 1);

    // Caso 5: Estrella K1,4 -> chi = 2
    run_test("Caso 5: Grafo estrella K1,4 (chi esperado = 2)", 5,
             {{0,1},{0,2},{0,3},{0,4}}, 2);

    // ----------------------------------------------------------
    // CASOS DE PRUEBA - PARTE 2: Escalabilidad
    // ----------------------------------------------------------
    cout << "\n--- Parte 2: Escalabilidad (grafos grandes) ---" << endl;
    cout << "(Solo Greedy: Backtracking inviable para estos tamanios)" << endl;

    double TIMEOUT_ESC = 10.0;
    vector<int> tamanios_esc = {50, 100, 200, 500};

    cout << setw(6)  << "n"
         << setw(14) << "Greedy (s)"
         << setw(12) << "Colores G"
         << setw(22) << "Backtrack"
         << endl;
    cout << string(56, '-') << endl;

    for (int n : tamanios_esc) {
        auto edges_esc = generate_random_graph(n, 0.4, 42);

        // Greedy
        GraphGreedy g_esc(n);
        for (auto& [u, v] : edges_esc) g_esc.add_edge(u, v);
        auto start_esc = chrono::high_resolution_clock::now();
        auto [nc_g_esc, __] = g_esc.greedy_coloring();
        double t_g_esc = chrono::duration<double>(
            chrono::high_resolution_clock::now() - start_esc).count();

        // Backtracking con timeout corto
        GraphBacktracking g_b_esc(n);
        for (auto& [u, v] : edges_esc) g_b_esc.add_edge(u, v);
        auto start_bt_esc = chrono::high_resolution_clock::now();
        bool to_esc = false;

        for (int k = 1; k <= n; k++) {
            double el = chrono::duration<double>(
                chrono::high_resolution_clock::now() - start_bt_esc).count();
            if (el >= TIMEOUT_ESC) { to_esc = true; break; }
            g_b_esc.colors.assign(n, 0);
            if (g_b_esc.graph_color_util(0, k)) break;
        }

        cout << setw(6)  << n
             << setw(14) << fixed << setprecision(6) << t_g_esc
             << setw(12) << nc_g_esc
             << setw(22) << (to_esc ? "TIMEOUT (>10s)" : "completado")
             << endl;
    }

    cout << "\nConclusion:" << endl;
    cout << "  - Greedy escala bien pero no garantiza el minimo." << endl;
    cout << "  - Backtracking es exacto pero inviable para n > 35." << endl;
    cout << "  - Confirma la naturaleza NP-hard del problema." << endl;
    cout << string(60, '=') << endl;

    cout << "\nPrograma finalizado correctamente." << endl;
    return 0;
}
