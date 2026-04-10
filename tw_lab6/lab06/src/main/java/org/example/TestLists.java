package org.example;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// ==================== FINE-GRAINED LIST ====================
class FineGrainedList {
    private class Node {
        Object value;
        Node next;
        Lock lock = new ReentrantLock();

        Node(Object value) {
            this.value = value;
        }
    }

    private final Node head;

    public FineGrainedList() {
        head = new Node(null);
    }

    public boolean contains(Object o) {
        if (o == null) return false;
        Node pred = head;
        pred.lock.lock();
        try {
            Node curr = pred.next;
            if (curr == null) return false;

            curr.lock.lock();
            try {
                while (curr != null) {
                    if (o.equals(curr.value)) {
                        return true;
                    }
                    Node next = curr.next;
                    if (next == null) {
                        return false;
                    }
                    next.lock.lock();
                    pred.lock.unlock();
                    pred = curr;
                    curr = next;
                }
                return false;
            } finally {
                curr.lock.unlock();
                pred.lock.unlock();
            }
        } catch (Exception e) {
            pred.lock.unlock();
            throw e;
        }
    }

    public boolean add(Object o) {
        Node pred = head;
        pred.lock.lock();
        try {
            Node curr = pred.next;
            while (curr != null) {
                curr.lock.lock();
                pred.lock.unlock();
                pred = curr;
                curr = curr.next;
            }
            pred.next = new Node(o);
            return true;
        } finally {
            pred.lock.unlock();
        }
    }

    public boolean remove(Object o) {
        if (o == null) return false;
        Node pred = head;
        pred.lock.lock();
        try {
            Node curr = pred.next;
            if (curr == null) return false;

            curr.lock.lock();
            try {
                while (curr != null) {
                    if (o.equals(curr.value)) {
                        pred.next = curr.next;
                        return true;
                    }
                    Node next = curr.next;
                    if (next == null) {
                        return false;
                    }
                    next.lock.lock();
                    pred.lock.unlock();
                    pred = curr;
                    curr = next;
                }
                return false;
            } finally {
                curr.lock.unlock();
                pred.lock.unlock();
            }
        } catch (Exception e) {
            pred.lock.unlock();
            throw e;
        }
    }

    public int size() {
        int count = 0;
        Node pred = head;
        pred.lock.lock();
        try {
            Node curr = pred.next;
            while (curr != null) {
                curr.lock.lock();
                pred.lock.unlock();
                pred = curr;
                count++;
                curr = curr.next;
            }
        } finally {
            pred.lock.unlock();
        }
        return count;
    }
}

// ==================== COARSE-GRAINED LIST ====================
class CoarseGrainedList {
    private class Node {
        Object value;
        Node next;

        Node(Object value) {
            this.value = value;
        }
    }

    private final Node head;
    private final Lock lock = new ReentrantLock();

    public CoarseGrainedList() {
        head = new Node(null);
    }

    public boolean contains(Object o) {
        lock.lock();
        try {
            Node current = head.next;
            while (current != null) {
                if (o.equals(current.value)) return true;
                current = current.next;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    public boolean remove(Object o) {
        lock.lock();
        try {
            Node current = head;
            while (current.next != null) {
                if (o.equals(current.next.value)) {
                    current.next = current.next.next;
                    return true;
                }
                current = current.next;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    public boolean add(Object o) {
        lock.lock();
        try {
            Node current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = new Node(o);
            return true;
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            int count = 0;
            Node current = head.next;
            while (current != null) {
                count++;
                current = current.next;
            }
            return count;
        } finally {
            lock.unlock();
        }
    }
}

// ==================== EXPENSIVE OBJECT ====================
class ExpensiveObject {
    private final int value;
    private final int cost;

    public ExpensiveObject(int value, int cost) {
        this.value = value;
        this.cost = cost;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ExpensiveObject)) return false;
        ExpensiveObject other = (ExpensiveObject) o;

        // Symulacja kosztownego porównania
        try {
            Thread.sleep(cost);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return this.value == other.value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

    @Override
    public String toString() {
        return "ExpObj(" + value + ")";
    }
}

// ==================== TEST CLASS ====================
public class TestLists {

    static class TestResult {
        long totalTime;
        long avgOperationTime;
        int operations;

        TestResult(long totalTime, int operations) {
            this.totalTime = totalTime;
            this.operations = operations;
            this.avgOperationTime = operations > 0 ? totalTime / operations : 0;
        }
    }

    // Test dla FineGrainedList
    static TestResult testFineGrained(int threads, int listSize, int operations, int costMs)
            throws InterruptedException {
        FineGrainedList list = new FineGrainedList();

        // Wypełnij listę początkowymi elementami
        for (int i = 0; i < listSize; i++) {
            list.add(new ExpensiveObject(i, costMs));
        }

        List<Thread> threadList = new ArrayList<>();
        Random rand = new Random();

        long startTime = System.currentTimeMillis();

        for (int t = 0; t < threads; t++) {
            final int threadId = t;
            Thread thread = new Thread(() -> {
                Random localRand = new Random(threadId);
                for (int i = 0; i < operations; i++) {
                    int op = localRand.nextInt(3);
                    int value = localRand.nextInt(listSize * 2);

                    ExpensiveObject obj = new ExpensiveObject(value, costMs);

                    switch (op) {
                        case 0: // contains
                            list.contains(obj);
                            break;
                        case 1: // add
                            list.add(obj);
                            break;
                        case 2: // remove
                            list.remove(obj);
                            break;
                    }
                }
            });
            threadList.add(thread);
            thread.start();
        }

        for (Thread thread : threadList) {
            thread.join();
        }

        long totalTime = System.currentTimeMillis() - startTime;
        return new TestResult(totalTime, threads * operations);
    }

    // Test dla CoarseGrainedList
    static TestResult testCoarseGrained(int threads, int listSize, int operations, int costMs)
            throws InterruptedException {
        CoarseGrainedList list = new CoarseGrainedList();

        // Wypełnij listę początkowymi elementami
        for (int i = 0; i < listSize; i++) {
            list.add(new ExpensiveObject(i, costMs));
        }

        List<Thread> threadList = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (int t = 0; t < threads; t++) {
            final int threadId = t;
            Thread thread = new Thread(() -> {
                Random localRand = new Random(threadId);
                for (int i = 0; i < operations; i++) {
                    int op = localRand.nextInt(3);
                    int value = localRand.nextInt(listSize * 2);

                    ExpensiveObject obj = new ExpensiveObject(value, costMs);

                    switch (op) {
                        case 0: // contains
                            list.contains(obj);
                            break;
                        case 1: // add
                            list.add(obj);
                            break;
                        case 2: // remove
                            list.remove(obj);
                            break;
                    }
                }
            });
            threadList.add(thread);
            thread.start();
        }

        for (Thread thread : threadList) {
            thread.join();
        }

        long totalTime = System.currentTimeMillis() - startTime;
        return new TestResult(totalTime, threads * operations);
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("=" .repeat(70));
        System.out.println("PORÓWNANIE WYDAJNOŚCI LIST");
        System.out.println("Fine-Grained vs Coarse-Grained Locking");
        System.out.println("=".repeat(70));

        // Parametry testów
        int[] threadCounts = {1, 2, 4, 8, 16};
        int[] costs = {0, 1, 5, 10, 20}; // Koszt operacji w ms
        int listSize = 20;
        int operationsPerThread = 50;

        try (FileWriter writer = new FileWriter("list_comparison.csv")) {
            writer.write("Threads,Cost(ms),FineGrained(ms),CoarseGrained(ms),Speedup\n");

            for (int cost : costs) {
                System.out.println("\n" + "-".repeat(70));
                System.out.println("Koszt operacji: " + cost + " ms");
                System.out.println("-".repeat(70));

                for (int threads : threadCounts) {
                    System.out.printf("Testowanie z %d wątkami... ", threads);

                    TestResult fineResult = testFineGrained(threads, listSize,
                            operationsPerThread, cost);
                    TestResult coarseResult = testCoarseGrained(threads, listSize,
                            operationsPerThread, cost);

                    double speedup = (double) coarseResult.totalTime / fineResult.totalTime;

                    writer.write(String.format("%d,%d,%d,%d,%.2f\n",
                            threads, cost, fineResult.totalTime,
                            coarseResult.totalTime, speedup));

                    System.out.printf("Fine: %d ms, Coarse: %d ms, Przyspieszenie: %.2fx\n",
                            fineResult.totalTime, coarseResult.totalTime, speedup);
                }
            }
        }

        System.out.println("\n" + "=".repeat(70));
        System.out.println("Testy zakończone!");
        System.out.println("Wyniki zapisane w pliku: list_comparison.csv");
        System.out.println("=".repeat(70));
    }
}