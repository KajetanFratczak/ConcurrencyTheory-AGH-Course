package pl.edu.agh.to;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        PerformanceLogger logger = new PerformanceLogger("results.csv");
        logger.init();

        // Testuj różne konfiguracje
        int[] producerCounts = {1, 2, 4, 10, 50};
        int[] consumerCounts = {1, 2, 4, 10, 50};
        int[] bufferSizes = {1, 2, 4, 5, 10, 50, 100};
        int messagesPerProducer = 1000;

        for (int producers : producerCounts) {
            for (int consumers : consumerCounts) {
                for (int bufferSize : bufferSizes) {
                    long duration = runTest(producers, consumers, bufferSize, messagesPerProducer);
                    String config = producers + "P-" + consumers + "C";
                    logger.log(config, bufferSize, duration);
                    System.out.println(config + ", Buffer: " + bufferSize + ", Time: " + duration + "ms");
                }
            }
        }

        System.out.println("Testy zakonczone. Wyniki w results.csv");
    }

    private static long runTest(int producerCount, int consumerCount,
                                int bufferSize, int messagesPerProducer) throws InterruptedException {
        Servant servant = new Servant(bufferSize);
        ActivationQueue queue = new ActivationQueue();
        Proxy proxy = new Proxy(servant, queue);
        Scheduler scheduler = new Scheduler(queue);

        Thread schedulerThread = new Thread(scheduler);
        scheduler.setThreadReference(schedulerThread);
        schedulerThread.start();

        long start = System.currentTimeMillis();

        // Producenci
        Thread[] producers = new Thread[producerCount];
        for (int i = 0; i < producerCount; i++) {
            final int id = i;
            producers[i] = new Thread(() -> {
                for (int j = 0; j < messagesPerProducer; j++) {
                    try {
                        proxy.put(id * 1000 + j).get();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
            producers[i].start();
        }

        // Konsumenci
        int totalMessages = producerCount * messagesPerProducer;
        int messagesPerConsumer = totalMessages / consumerCount;
        Thread[] consumers = new Thread[consumerCount];
        for (int i = 0; i < consumerCount; i++) {
            consumers[i] = new Thread(() -> {
                for (int j = 0; j < messagesPerConsumer; j++) {
                    try {
                        proxy.take().get();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
            consumers[i].start();
        }

        // Czekaj na zakończenie
        for (Thread p : producers) p.join();
        for (Thread c : consumers) c.join();

        long end = System.currentTimeMillis();

        scheduler.shutdown();
        schedulerThread.join();

        return end - start;
    }
}

// Servant - pracownik - implementuje funkcjonalność bufora bez synchronizacji
class Servant {
    private final int[] buffer;
    private final int capacity;
    private int head = 0;
    private int tail = 0;
    private int count = 0;

    public Servant(int capacity) {
        this.capacity = capacity;
        this.buffer = new int[capacity];
    }

    public void put(int value){
        buffer[tail] = value;
        tail = (tail + 1) % capacity;
        count++;
    }

    public int take() {
        int value = buffer[head];
        head = (head + 1) % capacity;
        count--;
        return value;
    }

    // Metody pomocnicze
    public boolean  isFull(){
        return count == capacity;
    }

    public boolean isEmpty(){
        return count == 0;
    }

    public int size(){
        return count;
    }
}

// Future - zmienna terminowa
class Future<T> {
    private T result;
    private boolean ready = false;

    public synchronized void set(T value){
        if (ready)  return;
        this.result = value;
        this.ready = true;
        notifyAll();
    }

    public synchronized T get() throws InterruptedException{
        while (!ready){
            wait();
        }
        return result;
    }
}

// Method request - żądanie metody
abstract class MethodRequest {
    // Strażnik - sprawdza czy można wykonać operację
    public abstract boolean guard();
    // Wykonanie operacji
    public abstract void call();
}

class PutRequest extends MethodRequest {
    private final Servant servant;
    private final int value;
    private final Future<Void> future;

    public PutRequest(Servant servant, int value, Future<Void> future) {
        this.servant = servant;
        this.value = value;
        this.future = future;
    }

    @Override
    public boolean guard() {
        // można ustawić tylko, gdy bufor nie jest pełny
        return !servant.isFull();
    }

    @Override
    public void call() {
        servant.put(value);
        // sygnalizacja zakończenia
        future.set(null);
    }
}

class TakeRequest extends MethodRequest {
    private final Servant servant;
    private final Future<Integer> future;

    public TakeRequest(Servant servant, Future<Integer> future) {
        this.servant = servant;
        this.future = future;
    }

    @Override
    public boolean guard() {
        // Można pobrać tylko, gdy bufor nie jest pusty
        return !servant.isEmpty();
    }

    @Override
    public void call() {
        int value = servant.take();
        future.set(value);
    }
}

// ActivationQueue - kolejka aktywacji
class ActivationQueue {
    private final BlockingQueue<MethodRequest> queue;

    public ActivationQueue() {
        this.queue = new LinkedBlockingQueue<>();
    }

    // Dodaj żądanie do kolejki (wywoływane z wątku klienta)
    public void enqueue(MethodRequest methodRequest) {
        queue.offer(methodRequest);
    }

    // Pobierz żądanie z kolejki (wywoływane z wątku Scheduler)
    public MethodRequest dequeue() throws InterruptedException {
        return queue.take();
    }

    // Wstaw żądanie z powrotem (gdy guard nie jest spełniony)
    public void requeue(MethodRequest methodRequest) {
        queue.offer(methodRequest);
    }

    public boolean  isEmpty() {
        return queue.isEmpty();
    }

    public int size() {
        return queue.size();
    }
}

// Scheduler - zarządca
class Scheduler implements Runnable {
    private final ActivationQueue activationQueue;
    private volatile  boolean running = true;
    private Thread selfThread;

    public void setThreadReference(Thread thread) {
        this.selfThread = thread;
    }

    public Scheduler(ActivationQueue activationQueue) {
        this.activationQueue = activationQueue;
    }

    public void shutdown() {
        running = false;
        if (selfThread != null) {
            selfThread.interrupt();
        }
    }

    @Override
    public void run() {
        try {
            while (running || !activationQueue.isEmpty()) {
                // Pobieram żądanie z kolejki
                MethodRequest request = activationQueue.dequeue();

                // Sprawdzam guard-a
                if (request.guard()) {
                    // strażnik spełniony
                    request.call();
                } else {
                    // nie spełniony - wstawiam spowrotem do kolejki
                    activationQueue.requeue(request);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Scheduler zakonczyl dzialanie.");
    }
}

// Proxy - pośrednik
class Proxy{
    private final Servant servant;
    private final ActivationQueue activationQueue;

    public Proxy(Servant servant, ActivationQueue activationQueue) {
        this.servant = servant;
        this.activationQueue = activationQueue;
    }

    public Future<Void> put(int value) {
        Future<Void> future = new Future<>();
        // tworzę żądanie
        MethodRequest request = new PutRequest(servant, value, future);
        // umieszczam w kolejce
        activationQueue.enqueue(request);
        // zwracam Future
        return future;
    }

    public Future<Integer> take() {
        Future<Integer> future = new Future<Integer>();
        // tworzę żądanie
        MethodRequest request = new TakeRequest(servant, future);
        // umieszczam w kolejce
        activationQueue.enqueue(request);
        // zwracam future
        return future;
    }
}

// PerformanceLogger
class PerformanceLogger {
    private final String fileName;

    public PerformanceLogger(String fileName) {
        this.fileName = fileName;
    }

    public void log(String configuration, int bufferSize, long duration) {
        try (FileWriter writer = new FileWriter(fileName, true)) {
            writer.write(configuration + "," + bufferSize + "," + duration + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void init() {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("Configuration,BufferSize,Duration(ms)\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}