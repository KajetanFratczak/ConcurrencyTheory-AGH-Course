# 🧵 Concurrency Theory (Teoria Współbieżności) - AGH University

> Laboratory projects, algorithms, and performance analyses for the **Concurrency Theory** (Teoria Współbieżności) course at AGH University.

This repository focuses on concurrent programming, multithreading, synchronization mechanisms, and avoiding common pitfalls like race conditions and deadlocks. It covers both practical implementations in various languages and theoretical models of concurrency.

## 👨‍💻 Author
* **Kajetan Frątczak**

---

## 🚀 Technologies & Concepts
* **Languages:** Java, Node.js / JavaScript, Python
* **Java Concurrency:** `synchronized`, `wait()`/`notify()`, Semaphores, Monitors, Java Concurrency Utilities (JCU), `ExecutorService`, `Callable`/`Future`
* **Advanced Paradigms:** CSP (Communicating Sequential Processes) using the **JCSP** library
* **Node.js Async:** Callbacks, Promises, `async` library, non-blocking I/O
* **Theoretical Models:** Trace Theory (Diekert's Dependency Graphs, Foata Normal Form), Petri Nets (Reachability Graphs, P/T-Invariants)

---

## 📁 Repository Structure

### 🔹 `lab1_thread_races/`
* Introduction to multithreading and the **Race Condition** problem.
* Implementing and analyzing **Peterson's Algorithm** for mutual exclusion.

### 🔹 `lab2_semaphores/`
* Implementation of a binary semaphore using `wait()` and `notify()`.
* Solving synchronization issues using general (counting) semaphores.

### 🔹 `lab3_pipeline_processing/`
* The **Producer-Consumer** problem.
* Implementation of pipeline processing using Java Monitors and Semaphores. Analysis of throughput based on buffer size and thread balance.

### 🔹 `lab4_jcu/`
* Solving the Producer-Consumer problem with random batch sizes using **Java Concurrency Utilities (JCU)**.
* Performance benchmarking: JCU vs. standard Java Monitors.

### 🔹 `lab5_dining_philosophers/`
* Classical synchronization problem: **The Dining Philosophers**.
* Dealing with **Deadlock** and **Starvation**.
* Implementations including naive approaches, simultaneous fork lifting, and the Arbitrator (Waiter) solution.

### 🔹 `lab6_efficiency_and_locks/`
* The **Readers-Writers problem**.
* Implementation and performance analysis of **Fine-Grained Locking** for concurrent list modifications.

### 🔹 `lab7_active_object/`
* Design patterns for concurrency: Implementation of the **Active Object** pattern.
* Separation of method execution from method invocation using Proxy, Servant, Method Request, and Scheduler.

### 🔹 `lab8_executors_futures/`
* Asynchronous task execution using thread pools.
* Concurrent generation of the **Mandelbrot Fractal**.
* Performance analysis of various executors: `FixedThreadPool`, `CachedThreadPool`, `ScheduledThreadPool`, and `WorkStealingPool`.

### 🔹 `lab9_nodejs_async/`
* Introduction to asynchronous I/O in **Node.js**.
* Sequential vs. Asynchronous execution of file processing tasks (calculating line counts in multiple files).
* Managing execution flow using `async.waterfall` and Promises.

### 🔹 `lab10_trace_theory/`
* Theoretical foundations of concurrency: **Trace Theory**.
* **Part 1:** Manual determination of dependency/independency relations, Foata Normal Form, and drawing Diekert's Dependency Graphs.
* **Part 2:** **Python** implementation of an algorithm to automatically find dependency relations (D), evaluate traces, and generate FNF and Graphviz DOT formats for words based on a given alphabet.

### 🔹 `lab11_petri_nets/`
* Modeling concurrent systems using **Petri Nets**.
* Simulation and analysis of basic state machines, mutual exclusion, and the bounded buffer Producer-Consumer problem.
* Mathematical analysis: Reachability Graphs, P-Invariants (boundedness), and T-Invariants (reversibility and liveness).

### 🔹 `lab12_csp/`
* Concurrency based on message passing: **Communicating Sequential Processes (CSP)**.
* Solving the Producer-Consumer problem with an N-element buffer utilizing the **JCSP** library for Java.
* Implementing a pipeline architecture where each buffer element acts as an independent process, eliminating the need for traditional locking mechanisms (no `synchronized`, `wait`, or `notify`).

---

## ⚙️ How to run
* **Java Projects (Labs 1-8, 12):** Standalone Java programs. Ensure you have JDK 8+ installed. Lab 12 requires the JCSP library to be included in the classpath.
* **Node.js (Lab 9):** Requires Node.js installed. Run scripts via `node script_name.js`.
* **Python (Lab 10):** Requires Python 3.x. Run via `python script.py`.
