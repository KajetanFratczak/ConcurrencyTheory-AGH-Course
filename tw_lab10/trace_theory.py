# Trace Theory

# Program ma za zadanie:
# Wyznaczać relację niezależności I (poprawka - ma wyznaczać D - relację zależności, czyli dopełnienie I)
# Wyznaczać ślad [w] względem relacji I
# Wyznaczać postać normalną Foaty FNF([w]) śladu [w]
# Wyznaczać graf zależności dla słowa w
# Wyznaczać postać normalną Foaty na podstawie grafu

# Dane testowe nr 1
# A = {'a', 'b', 'c', 'd'}
# I = {('a', 'd'), ('d', 'a'), ('b', 'c'), ('c', 'b')}
# w = "baadcb"

# Dane testowe nr 2
A = {'a', 'b', 'c', 'd', 'e', 'f'}
I = {('a', 'd'), ('d', 'a'), ('b', 'e'), ('e', 'b'), ('c', 'd'), ('d', 'c'), ('c', 'f'), ('f', 'c')}
w = "acdcfbbe"

# 1. Wyznaczanie D na podstawie alfabetu oraz relacji niezależności
# D - relacja ta określa które pary akcji nie mogą być zamieniane kolejnością
D = set()
for x in A:
    for y in A:
        # jeśli para nie jest w I, to jest w D
        if (x,y) not in I:
            D.add((x,y))
D = sorted(list(D))

print("D: ", D)
print("\n")

# 2. Wyznaczanie śladu [w] względem relacji niezależności I
# Możemy zamieniać kolejnościa w 'w' jedynie pary symboli z I
trace = {w}

queue = [w]

while(queue):
    current_word = queue.pop(0)
    for i in range(len(current_word)-1):
        x,y = current_word[i], current_word[i+1]
        if (x,y) in I or (y,x) in I:
            #print(i, i+1)
            new_word = current_word[:i] + y + x + current_word[i+2:]
            if new_word not in trace:
                  trace.add(new_word)
                  queue.append(new_word)
print("Ślad: ", trace)
print("\n")

# 3. Wyznaczanie postaci normalnej Foaty FNF([w]) śladu [w]

# Najpierw zgodnie z algorytmem opisanym w artykule tworzymy stosy dla każdej litery alfabetu 
stacks = {letter: [] for letter in A}

# Przechodzimy przez słowo od prawej do lewej
for char in reversed(w):
    if char not in A:
        print("Error: litera spoza alfabetu.")
    stacks[char].append(char)
    for other_char in A:
        if other_char != char and ((char, other_char) in D or (other_char, char) in D):
            stacks[other_char].append('*')

#print(stacks)
 
# Zdejmowanie ze stosów i budowa FNF
fnf = []

while True:
    # kandydat - litera na szczycie swojego stosu
    curr_step = []
    all_empty = True

    for char in A:
        if stacks[char]:
            all_empty = False
            top = stacks[char][-1]
            if top != '*':
                curr_step.append(top)
    
    if all_empty:
        break

    # Sortujemy litery w kroku 
    curr_step.sort()
    fnf.append("".join(curr_step))

    # Zdejmujemy wybrane litery i odpowiadające im znaczniki
    for char_to_pop in curr_step:
        stacks[char_to_pop].pop()

        # Zdejmuje znaczniki '*' ze stosów liter zależnych
        for other_char in A:
            if other_char != char_to_pop and ((char_to_pop, other_char) in D or (other_char, char_to_pop) in D):
                stacks[other_char].pop()
            else:
                pass
fnf = "(" + ")(".join(fnf) + ")"
print("FNF: ", fnf)
print("\n")

# 4. Budowa grafu zależności dla słowa w
n = len(w)
adj = {i: [] for i in range(n)}

# Krok 1: Budujemy pełny graf zależności
# Łączymy każde i z j (gdzie i < j), jeśli litery są zależne
for i in range(n):
    for j in range(i+1, n):
        if (w[i], w[j]) not in I:
            adj[i].append(j)

# Pomocniczy BFS do sprawdzania odległości
from collections import deque

def can_reach(u, v, graph):
    visited = set([u])
    queue = deque([u])
    while queue:
        curr = queue.popleft()
        if curr == v:
            return True
        for nxt in graph[curr]:
            if nxt not in visited:
                visited.add(nxt)
                queue.append(nxt)
    return False

# Krok 2: redukcja (usuwanie nadmiarowych krawędzi)
for u in range(n):
    neighbors_copy = list(adj[u])
    for v in neighbors_copy:
        # Tymczasowo usuwamy krawędź u -> v
        adj[u].remove(v)
        # Sprawdzamy, czy nadal da się dojść z u do v
        if not can_reach(u, v, adj):
            # Jeśli się NIE da, to krawędź była niezbędna - przywracamy ją
            adj[u].append(v)
            # Sortujemy, żeby zachować kolejność (opcjonalne, ale ładniej wygląda)
            adj[u].sort()
        # Jeśli can_reach zwróciło True, to znaczy że istnieje inna droga (np. u -> k -> v),
        # więc krawędź bezpośrednia u -> v jest zbędna i pozostaje usunięta.

nodes = []
edges = []

for i in range(n):
    nodes.append((i+1, w[i]))
    for target in adj[i]:
        edges.append((i+1, target+1))

print("Węzły (id, label):", nodes)
print("\n")
print("Krawędzie (src, dst):", edges)
print("\n")

# W formacie dot 
dot_code = "digraph g {\n"
for src, dst in edges:
    dot_code += f' {src} -> {dst}\n'
for node_id, label in nodes:
    dot_code += f' {node_id} [label="{label}"]\n'
dot_code += "}"

print("Kod dot: ")
print(dot_code)
print("\n")

# 5. Wyznaczamy postać normalną Foaty na podstawie grafu
# Krok 1 - obliczenie stopni wejściowych (ile krawędzi wchodzi do wierzchołka)
in_degree = {i: 0 for i in range(n)}
for u in range(n):
    for v in adj[u]:
        in_degree[v] += 1

# Krok 2 - warstwy
foata_layers = []
current_layer = [i for i in range(n) if in_degree[i] == 0]

while current_layer:
    # sortowanie indeksów w warstwie według liter, aby było ad a nie da
    current_layer.sort(key=lambda index: w[index])
    # tworzymy napis dla obecnej warstwy
    layer_str = "".join(w[i] for i in current_layer)
    foata_layers.append(layer_str)
    # przygotowujemy następną warstwę
    next_layer = []
    for u in current_layer:
        for v in adj[u]:
            in_degree[v] -= 1
            if in_degree[v] == 0:
                next_layer.append(v)
    # przechodzimy do kolejnej warstwy
    current_layer = next_layer

fnf_from_graph = "(" + ")(".join(foata_layers) + ")"
print("FNF wyznaczona z grafu:", fnf_from_graph)