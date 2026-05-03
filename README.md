# Chromatic Number - Problema NP-Completo

Trabajo grupal del curso de Optimización, Universidad Andrés Bello.
Implementación y análisis del problema del Número Cromático (problema #12
de la lista de Karp), uno de los 21 problemas NP-completos clásicos.

## Descripción del problema

El problema del Número Cromático consiste en colorear los nodos de un grafo
de manera que ningún par de nodos conectados por una arista comparta el mismo
color, utilizando la menor cantidad posible de colores. A este mínimo se le
denomina número cromático y se denota como X(G).

Formalmente, dado un grafo G = (V, E), se busca encontrar:

    X(G) = min{ k : existe una k-coloracion valida de G }

El problema pertenece a la clase NP-completo, lo que significa que no existe
algoritmo eficiente conocido para resolverlo exactamente en el caso general.

## Algoritmos implementados

### Solucion base: Greedy Welsh-Powell
Ordena los nodos de mayor a menor grado y asigna a cada uno el menor color
disponible que no use ninguno de sus vecinos.

- Complejidad: O(n^2)
- Ventaja: muy rapido, escala bien para grafos grandes
- Desventaja: no garantiza el numero minimo de colores

### Solucion mejorada: Backtracking exacto
Busca exhaustivamente el numero cromatico minimo iterando desde k=1 y usando
recursion con retroceso para explorar todas las coloraciones posibles.

- Complejidad: O(k^n) en el peor caso
- Ventaja: garantiza encontrar X(G) exacto
- Desventaja: inviable para grafos con mas de 30-35 nodos

## Estructura del repositorio

```
chromatic-number/
├── python/
│   └── Chromatic_number_python.py       # implementacion en Python
├── cpp/
│   └── chromatic_number_cpp.cpp     # implementacion en C++
├── java/
│   └── Chromatic_number_java.java     # implementacion en Java
├── resultados/
│   ├── resultados_cpp.csv       # tiempos de ejecucion C++
│   ├── resultados_java.csv      # tiempos de ejecucion Java
│   ├── comparacion_lenguajes.png
│   ├── crecimiento_exponencial.png
│   └── calidad_solucion.png
├── informe/
│   └── Numero cromatico.pdf                # informe en pdf
└── README.md
```

## Instrucciones de ejecucion

### Python
Requiere Python 3.8 o superior y las siguientes dependencias:
```bash
pip install networkx matplotlib
```
Ejecutar:
```bash
cd python
python ChromaticNumber.py
```

### C++
Requiere compilador g++ con soporte C++17:
```bash
cd cpp
g++ -O2 -std=c++17 -o chromatic chromatic_number.cpp
./chromatic
```
En Windows:
```bash
chromatic.exe
```

### Java
Requiere JDK 11 o superior:
```bash
cd java
javac ChromaticNumber.java
java ChromaticNumber
```

## Casos de prueba

Cada implementacion incluye dos tipos de casos de prueba:

**Correctitud:** grafos pequenos con numero cromatico conocido matematicamente.

| Caso | Grafo | X esperado | Resultado |
|------|-------|------------|-----------|
| 1 | Completo K4 | 4 | PASS |
| 2 | Ciclo par C4 | 2 | PASS |
| 3 | Ciclo impar C5 | 3 | PASS |
| 4 | Sin aristas (n=5) | 1 | PASS |
| 5 | Estrella K1,4 | 2 | PASS |

**Escalabilidad:** grafos grandes donde se demuestra la inviabilidad del
Backtracking y la naturaleza NP-hard del problema.

| n | Greedy (s) | Colores G | Backtracking |
|---|------------|-----------|--------------|
| 50 | 0.000105 | 10 | TIMEOUT (>10s) |
| 100 | 0.000291 | 16 | TIMEOUT (>10s) |
| 200 | 0.001207 | 26 | TIMEOUT (>10s) |
| 500 | 0.005419 | 56 | TIMEOUT (>10s) |

## Resultados experimentales

Se compararon ambos algoritmos en los tres lenguajes usando grafos aleatorios
Erdos-Renyi G(n, p=0.4) con semilla fija (seed=42) para reproducibilidad.

Principales hallazgos:

- El Greedy mantiene tiempos en microsegundos incluso para n=500.
- El Backtracking crece exponencialmente: pasa de 0.000001s (n=5) a 608s (n=35) en C++.
- Java supera a C++ para instancias grandes gracias al compilador JIT de la JVM.
- Python es el mas lento pero ofrece la implementacion mas legible.

## Integrantes

- Vicente Díaz
- Eduardo Zepeda
- Fernando Chavez
