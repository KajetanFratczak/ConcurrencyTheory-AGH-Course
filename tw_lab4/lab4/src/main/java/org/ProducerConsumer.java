package org;

import java.util.Random;

class Producer extends Thread {
    private Buffer _buf;
    private int producerId;
    private int M;
    private Random rand;

    public Producer(Buffer _buf, int producerId, int M ) {
        this._buf = _buf;
        this.producerId =producerId;
        this.M = M;
        this.rand = new Random();
    }

    public void run() {
        while (true) {
            int random_size = rand.nextInt(M) + 1;
            boolean success = _buf.put(random_size, producerId);
            if(!success) break; // produkcja zakończona
            try {
                Thread.sleep(rand.nextInt(200));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}

class Consumer extends Thread {
    private Buffer _buf;
    private int consumerId;
    private int M;
    private Random rand;

    public Consumer(Buffer _buf, int consumerId, int M) {
        this._buf = _buf;
        this.consumerId = consumerId;
        this.M = M;
        this.rand = new Random();
    }

    public void run() {
        while (true) {
            int random_size = rand.nextInt(M) + 1;
            boolean success = _buf.get(random_size, consumerId);
            if(!success) break; // konsumpcja zakończona
            try {
                Thread.sleep(rand.nextInt(200));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}

class Buffer {
    private int[] items;
    private int items_now = 0; // aktualna liczba elementów w buforze
    private int size;

    private int totalToProduce;
    private int produced = 0;
    private int consumed = 0;

    Buffer(int size, int totalToProduce) {
        this.size = size;
        this.items = new int[size];
        this.totalToProduce = totalToProduce;
    }

    // synchronizacja przy pomocy wait() oraz notify()
    public synchronized boolean put(int quantity, int producerId) {
        if (produced >= totalToProduce) return false; // wszystko już wyprodukowano - zakończ

        // przycięcie, jeśli brakuje miejsca do końca całkowitej produkcji
        if (produced + quantity > totalToProduce) {
            quantity = totalToProduce -  produced;
        }

        while (items_now + quantity > size) {
            try {
                wait(); // czekaj jeśli bufor jest pełny
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        for (int i = 0; i < quantity; i++) {
            items[items_now + i] = producerId * 1000 + i;
        }
        items_now += quantity;
        produced += quantity;

        System.out.println("Producer " + producerId + " put: " + quantity + " items (buffer: " + items_now + "/" + size + ")");
        notifyAll();
        return produced < totalToProduce;
    }

    public synchronized boolean get(int quantity, int consumerId) {
        while (items_now < quantity && produced < totalToProduce) {
            try {
                wait(); // czekaj jeśli bufor nie ma wystarczająco elementów
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        // jeśli nic nie ma i już nie będzie to kończymy
        if (items_now == 0 && produced >= totalToProduce) return false;

        if (quantity > items_now) {
            quantity = items_now;
        }

        items_now -= quantity;
        consumed += quantity;

        System.out.println("Consumer " + consumerId + " got: " + quantity + " items (buffer: " + items_now + "/" + size + ")");
        notifyAll();
        return true;
    }

    public int getProduced() {
        return produced;
    }
    public int getConsumed() {
        return consumed;
    }
}

public class ProducerConsumer {
    public static void main(String[] args) {
        // synchronizacja przy pomocy monitora: wait(), notify()
        // m producentów n konsumentów
        // 2M - rozmiar bufora
        // Producent wstawia do bufora losowa liczbę elementów (nie więcej niż M)
        // Konsument pobiera losowa liczbę elementów (nie więcej niż M)
        int M = 100;
        int totalElements = 1000; // całkowita liczba elementów do wyprodukowania
        int m = 3; // liczba producentów
        int n = 2; // liczba konsumentów

        // int totalOperations = 300; // tylko, że nie wiemy ile wstawią i pobiorą elementów (bo to jest losowe) - i wtedy zrobi się deadlock (zakleszczenie)

        long startTime = System.nanoTime();

        Buffer buffer = new Buffer(2*M, totalElements);

        Producer[] producers = new Producer[m];
        for (int i = 0; i < m; i++) {
            producers[i] = new Producer(buffer, i, M);
            producers[i].start();
        }

        Consumer[] consumers = new Consumer[n];
        for (int i = 0; i < n; i++) {
            consumers[i] = new Consumer(buffer, i, M);
            consumers[i].start();
        }

        try {
            for (int i = 0; i < m; i++) {
                producers[i].join();
            }
            System.out.println("\n>>> All producers finished <<<\n");
            for (int i = 0; i < n; i++) {
                consumers[i].join();
            }
            System.out.println("\n>>> All consumers finished <<<\n");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long endTime = System.nanoTime();
        long exeTime = (endTime - startTime) / 1_000_000;
        System.out.println("=== Podsumowanie ===");
        System.out.println("Wyprodukowano: " + buffer.getProduced());
        System.out.println("Skonsumowano: " + buffer.getConsumed());
        System.out.println("Czas wykonania: " + exeTime + " ms");

    }
}
