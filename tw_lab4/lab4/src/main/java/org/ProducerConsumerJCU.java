package org;

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

// wersja z użyciem Java Concurrency Utilities

class ProducerTask implements Runnable {
    private BufferJCU buffer;
    private int producerId;
    private int M;
    private Random rand;

    public ProducerTask(BufferJCU buffer, int producerId, int M ) {
        this.buffer = buffer;
        this.producerId =producerId;
        this.M = M;
        this.rand = new Random();
    }

    @Override
    public void run() {
        while (true) {
            int random_size = rand.nextInt(M) + 1;
            boolean success = buffer.put(random_size, producerId);
            if(!success) break; // produkcja zakończona
            try {
                Thread.sleep(rand.nextInt(200));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("Producer " + producerId + " finished");
    }
}

class ConsumerTask implements Runnable {
    private BufferJCU buffer;
    private int consumerId;
    private int M;
    private Random rand;

    public ConsumerTask(BufferJCU buffer, int consumerId, int M) {
        this.buffer = buffer;
        this.consumerId = consumerId;
        this.M = M;
        this.rand = new Random();
    }

    @Override
    public void run() {
        while (true) {
            int random_size = rand.nextInt(M) + 1;
            boolean success = buffer.get(random_size, consumerId);
            if(!success) break; // konsumpcja zakończona
            try {
                Thread.sleep(rand.nextInt(200));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("Consumer " + consumerId + " finished");
    }
}

class BufferJCU {
    // BlockingQueue - automaycznie blokuje przy próbie dodania do pełnej kolejki lub pobrania z pustej kolejki
    private ArrayBlockingQueue<Integer> queue;
    private int size;

    // AtomicInteger - bezpieczne operacje atomowe bez synchronizacji
    private AtomicInteger produced =  new AtomicInteger(0);
    private AtomicInteger consumed =  new AtomicInteger(0);
    private int totalToProduce;

    // ReentrantLock - dla operacji wymagających złożonej synchronizacji
    private ReentrantLock producerLock = new ReentrantLock();
    private ReentrantLock consumerLock = new ReentrantLock();

    private boolean productionFinished = false;

    BufferJCU(int size, int totalToProduce) {
        this.size = size;
        this.totalToProduce = totalToProduce;
        // ograniczona kolejka FIFO
        this.queue = new ArrayBlockingQueue<>(size);
    }

    public boolean put(int quantity, int producerId) {
        producerLock.lock();
        try {
            // sprawdzam, czy trzeba jeszcze coś produkować
            int currentProduced = produced.get();
            if (currentProduced >= totalToProduce) {
                productionFinished = true;
                return false;
            }

            // przycinam jeśli mamy za dużo produkcji
            if (currentProduced + quantity > totalToProduce) {
                quantity = totalToProduce - currentProduced;
            }

            // rezerwuje miejsce w liczniku
            produced.addAndGet(quantity);
        } finally {
            producerLock.unlock();
        }

        try {
            // wstawiam elementy do BlockinQueue
            for (int i = 0; i < quantity; i++) {
                // put() automatycznie blokuje jeśli kolejka jest pełna
                queue.put(producerId * 1000 + i);
            }
            System.out.println("Producer " + producerId + " put: " + quantity + " items (buffer: " + queue.size() + "/" + size + ", total produced: " + produced.get() + ")");
            return produced.get() < totalToProduce;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // cofam rezerwacje w wyniku przerwania
            produced.addAndGet(-quantity);
            return false;
        }
    }

    public boolean get(int quantity, int consumerId) {
        // jeśli produkcja zakończona i kolejka pusta - kończymy
        if  (productionFinished && queue.isEmpty()) {
            return false;
        }

        consumerLock.lock();
        int actualQuantity = quantity;
        try {
            // jeśli produkcja zakończona - pobierz tylko to co jest
            if (productionFinished && queue.size() < quantity) {
                actualQuantity = queue.size();
                if (actualQuantity == 0) return false;
            }
        } finally {
            consumerLock.unlock();
        }

        try {
            // pobieranie elementów z BlockingQueue
            for (int i = 0; i < actualQuantity; i++) {
                // poll() z timeoutem - czeka max 1 sekundę na element
                Integer item = queue.poll(1, TimeUnit.SECONDS);
                if (item == null) {
                    // timeout - sprawdź czy produkcja zakończona
                    if (productionFinished && queue.isEmpty()) {
                        actualQuantity = i; // pobrano tylko i elementów
                        break;
                    }
                }
            }

            consumed.addAndGet(actualQuantity);

            System.out.println("Consumer " + consumerId + " got: " + actualQuantity + " items (buffer: " + queue.size() + "/" + size + ", total consumed: " + consumed.get() + ")");
            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    public int getConsumed() {
        return consumed.get();
    }

    public int getProduced() {
        return produced.get();
    }
}

public class ProducerConsumerJCU {
    public static void main(String[] args) throws InterruptedException {
        // synchronizacja przy pomocy Java Concurrency Utilities
        // m producentów n konsumentów
        // 2M - rozmiar bufora
        // Producent wstawia do bufora losowa liczbę elementów (nie więcej niż M)
        // Konsument pobiera losowa liczbę elementów (nie więcej niż M)
        int M = 100;
        int totalElements = 1000; // całkowita liczba elementów do wyprodukowania
        int m = 3; // liczba producentów
        int n = 2; // liczba konsumentów

        // pomiar czasu
        long startTime = System.nanoTime();

        BufferJCU bufferJCU = new BufferJCU(2*M, totalElements);

        // ExecutorService - zarządza pulą wątków
        // Tworzę osobne pule dla producentów i konsumentów
        ExecutorService producerExecutor = Executors.newFixedThreadPool(m);
        ExecutorService consumerExecutor = Executors.newFixedThreadPool(n);

        CountDownLatch producerLatch = new CountDownLatch(m);
        CountDownLatch consumerLatch = new CountDownLatch(n);

        for (int i = 0; i < m; i++) {
            int producerId = i;
            producerExecutor.submit(() -> {
                try {
                    new ProducerTask(bufferJCU, producerId, M).run();
                } finally {
                    producerLatch.countDown();
                }
            });
        }

        for (int i = 0; i < n; i++) {
            int consumerId = i;
            consumerExecutor.submit(() -> {
                try {
                    new ConsumerTask(bufferJCU, consumerId, M).run();
                } finally {
                    consumerLatch.countDown();
                }
            });

        }

        producerExecutor.shutdown();
        consumerExecutor.shutdown();

        producerLatch.await();
        System.out.println("\n>>> All producers finished <<<\n");

        consumerLatch.await();
        System.out.println("\n>>> All consumers finished <<<\n");

        producerExecutor.awaitTermination(5, TimeUnit.SECONDS);
        consumerExecutor.awaitTermination(5, TimeUnit.SECONDS);

        long endTime = System.nanoTime();
        long exeTime  = (endTime - startTime) / 1_000_000;


        System.out.println("=== Podsumowanie ===");
        System.out.println("Wyprodukowano: " + bufferJCU.getProduced());
        System.out.println("Skonsumowano: " + bufferJCU.getConsumed());
        System.out.println("Czas wykonania: " + exeTime + " ms");

    }
}