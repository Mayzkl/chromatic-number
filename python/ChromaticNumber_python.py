import networkx as nx
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
import time
import random
import matplotlib

class GraphGreedy:
    def __init__(self, vertices):
        self.V = vertices
        self.graph = [[] for _ in range(vertices)]

    def add_edge(self, u, v):
        self.graph[u].append(v)
        self.graph[v].append(u)

    def greedy_coloring(self):
        color_map = {}

        # Ordenar nodos por grado descendente
        vertices_degrees = [(len(self.graph[i]), i) for i in range(self.V)]
        vertices_degrees.sort(reverse=True)

        # Asignar el menor color disponible a cada nodo
        for _, vertex in vertices_degrees:
            neighbor_colors = {color_map.get(neigh) for neigh in self.graph[vertex]}
            color_map[vertex] = next(
                color for color in range(self.V)
                if color not in neighbor_colors
            )

        # Retorna numero de colores usados Y el mapa de colores
        num_colors = len(set(color_map.values()))
        return num_colors, color_map


# Ejemplo de uso
g = GraphGreedy(5)
g.add_edge(0, 1)
g.add_edge(0, 2)
g.add_edge(1, 2)
g.add_edge(1, 3)
g.add_edge(2, 3)
g.add_edge(3, 4)

num_colors, coloring = g.greedy_coloring()
print(f"Colores usados (greedy): {num_colors}")
print(f"Coloracion: {coloring}")

class GraphBacktracking:
    def __init__(self, vertices):
        self.V = vertices
        self.graph = [[] for _ in range(vertices)]
        self.colors = [0] * vertices
        self.chromatic_number = float('inf')
        self.optimal_coloring_map = {}

    def add_edge(self, u, v):
        self.graph[u].append(v)
        self.graph[v].append(u)

    def _is_safe(self, vertex, color):
        """Verifica si asignar 'color' al 'vertex' es valido"""
        for neighbor in self.graph[vertex]:
            if self.colors[neighbor] == color:
                return False
        return True

    def _graph_color_util(self, vertex, k_limit):
        """Intenta colorear el grafo con k_limit colores usando backtracking"""
        # Caso base: todos los nodos coloreados
        if vertex == self.V:
            return True

        for color in range(1, k_limit + 1):
            if self._is_safe(vertex, color):
                self.colors[vertex] = color
                if self._graph_color_util(vertex + 1, k_limit):
                    return True
                # Backtrack
                self.colors[vertex] = 0
        return False

    def find_chromatic_number(self):
        """Encuentra el numero cromatico minimo iterando desde k=1"""
        for k in range(1, self.V + 1):
            self.colors = [0] * self.V
            if self._graph_color_util(0, k):
                self.chromatic_number = k
                self.optimal_coloring_map = {
                    i: self.colors[i] for i in range(self.V)
                }
                return k
        return self.V


# Ejemplo de uso
g_bt = GraphBacktracking(5)
g_bt.add_edge(0, 1)
g_bt.add_edge(0, 2)
g_bt.add_edge(1, 2)
g_bt.add_edge(1, 3)
g_bt.add_edge(2, 3)
g_bt.add_edge(3, 4)

num_bt = g_bt.find_chromatic_number()
print(f"Numero cromatico exacto (backtracking): {num_bt}")
print(f"Coloracion optima: {g_bt.optimal_coloring_map}")

# Generar grafo aleatorio
n_vertices = 10
probabilidad = 0.4
seed = 42

G_rand = nx.gnp_random_graph(n_vertices, probabilidad, seed=seed)
print(f"Grafo generado: {G_rand.number_of_nodes()} nodos, {G_rand.number_of_edges()} aristas")

# Aplicar Greedy
g_greedy = GraphGreedy(G_rand.number_of_nodes())
for u, v in G_rand.edges():
    g_greedy.add_edge(u, v)
num_greedy, greedy_map = g_greedy.greedy_coloring()

# Aplicar Backtracking
g_bt = GraphBacktracking(G_rand.number_of_nodes())
for u, v in G_rand.edges():
    g_bt.add_edge(u, v)
num_bt = g_bt.find_chromatic_number()
bt_map = g_bt.optimal_coloring_map

print(f"Greedy     -> colores usados: {num_greedy}")
print(f"Backtracking -> numero cromatico exacto: {num_bt}")
# Visualizacion comparativa
fig, axes = plt.subplots(1, 2, figsize=(16, 7))
pos = nx.spring_layout(G_rand, seed=seed)

# Paleta de colores
palette = matplotlib.colormaps.get_cmap('tab10')

# --- Greedy ---
nx.draw(
    G_rand, pos=pos, ax=axes[0],
    with_labels=True,
    node_color=[palette(greedy_map[n]) for n in G_rand.nodes()],
    node_size=800, font_size=11, font_weight='bold'
)
axes[0].set_title(f'Greedy (Welsh-Powell)\nColores usados: {num_greedy}', fontsize=13)

# --- Backtracking ---
nx.draw(
    G_rand, pos=pos, ax=axes[1],
    with_labels=True,
    node_color=[palette(bt_map[n]) for n in G_rand.nodes()],
    node_size=800, font_size=11, font_weight='bold'
)
axes[1].set_title(f'Backtracking (Exacto)\nNumero cromatico: {num_bt}', fontsize=13)

plt.suptitle('Comparacion de Algoritmos de Coloracion', fontsize=15, fontweight='bold')
plt.tight_layout()
plt.show()

# Analisis de tiempos para distintos tamaños de grafo
tamanios = [5, 8, 10, 12, 15, 18, 20, 30, 40]
probabilidad_exp = 0.4

tiempos_greedy = []
tiempos_bt = []
colores_greedy = []
colores_bt = []

print(f"{'n':>5} | {'Greedy (s)':>12} | {'Backtrack (s)':>14} | {'Colores G':>10} | {'Colores BT':>10}")
print("-" * 60)

for n in tamanios:
    G = nx.gnp_random_graph(n, probabilidad_exp, seed=42)
    edges = list(G.edges())

    # Greedy
    g = GraphGreedy(n)
    for u, v in edges:
        g.add_edge(u, v)
    start = time.time()
    nc_g, _ = g.greedy_coloring()
    t_g = time.time() - start

    # Backtracking
    g_bt = GraphBacktracking(n)
    for u, v in edges:
        g_bt.add_edge(u, v)
    start = time.time()
    nc_bt = g_bt.find_chromatic_number()
    t_bt = time.time() - start

    tiempos_greedy.append(t_g)
    tiempos_bt.append(t_bt)
    colores_greedy.append(nc_g)
    colores_bt.append(nc_bt)

    print(f"{n:>5} | {t_g:>12.6f} | {t_bt:>14.6f} | {nc_g:>10} | {nc_bt:>10}")

    # Grafico de tiempos de ejecucion
fig, axes = plt.subplots(1, 2, figsize=(16, 6))

# Grafico 1: Tiempos de ejecucion
axes[0].plot(tamanios, tiempos_greedy, 'o-', color='steelblue',
             linewidth=2, markersize=8, label='Greedy (Welsh-Powell)')
axes[0].plot(tamanios, tiempos_bt, 's-', color='tomato',
             linewidth=2, markersize=8, label='Backtracking (Exacto)')
axes[0].set_xlabel('Numero de nodos', fontsize=12)
axes[0].set_ylabel('Tiempo de ejecucion (segundos)', fontsize=12)
axes[0].set_title('Comparacion de Tiempos de Ejecucion', fontsize=13)
axes[0].legend(fontsize=11)
axes[0].grid(True, alpha=0.3)

# Grafico 2: Calidad de la solucion (colores usados)
x = range(len(tamanios))
width = 0.35
axes[1].bar([i - width/2 for i in x], colores_greedy, width,
            color='steelblue', alpha=0.8, label='Greedy')
axes[1].bar([i + width/2 for i in x], colores_bt, width,
            color='tomato', alpha=0.8, label='Backtracking')
axes[1].set_xlabel('Numero de nodos', fontsize=12)
axes[1].set_ylabel('Colores usados', fontsize=12)
axes[1].set_title('Calidad de la Solucion (Colores Usados)', fontsize=13)
axes[1].set_xticks(list(x))
axes[1].set_xticklabels(tamanios)
axes[1].legend(fontsize=11)
axes[1].grid(True, alpha=0.3, axis='y')

plt.suptitle('Analisis Experimental - Chromatic Number', fontsize=15, fontweight='bold')
plt.tight_layout()
plt.show()