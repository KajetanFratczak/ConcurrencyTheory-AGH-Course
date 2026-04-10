//class Producer extends Thread {
//    private Buffer _buf;
//
//    public Producer(Buffer _buf) {
//        this._buf = _buf;
//    }
//
//    public void run() {
//        for (int i = 0; i < 100; ++i) {
//            _buf.put(i);
//        }
//    }
//}
//
//class Consumer extends Thread {
//    private Buffer _buf;
//
//    public Consumer(Buffer _buf) {
//        this._buf = _buf;
//    }
//
//    public void run() {
//        for (int i = 0; i < 100; ++i) {
//            _buf.get();
//        }
//    }
//}
//
//class Buffer {
//    private int item; // bufor przechowuje jeden element
//    private boolean isFull = false; // flaga stanu bufora
//
//    // synchronizacja przy pomocy wait() oraz notify()
//    public synchronized void put(int i) {
//        while (isFull) {
//            try {
//                wait(); // czekaj jeśli bufor jest pełny
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//                return;
//            }
//        }
//        item = i;
//        isFull = true;
//        System.out.println("Producer put: " + i);
//        notify(); // powiadom konsumenta
//    }
//
//    public synchronized int get() {
//        while (!isFull) {
//            try {
//                wait(); // czekaj jeśli bufor pusty
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//                return -1;
//            }
//        }
//        int value  = item;
//        isFull = false;
//        System.out.println("Consumer got: " + value);
//        notify(); // powiadom producenta
//        return value;
//    }
//}
//
//public class PKmon {
//    public static void main(String[] args) {
//        // podpunkt a)
//        // 1 producent 1 konsument
//        Buffer buffer = new Buffer();
//        Producer producer = new Producer(buffer);
//        Consumer consumer = new Consumer(buffer);
//
//        producer.start();
//        consumer.start();
//
//        try {
//            producer.join();
//            consumer.join();
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//    }
//}

// ---------------------------------------------------------------------------------------------------------------------
//
//class Producer extends Thread {
//    private Buffer _buf;
//    private int producerId;
//
//    public Producer(Buffer _buf, int producerId) {
//        this._buf = _buf;
//        this.producerId =producerId;
//    }
//
//    public void run() {
//        for (int i = 0; i < 100; ++i) {
//            _buf.put(i, producerId);
//        }
//    }
//}
//
//class Consumer extends Thread {
//    private Buffer _buf;
//    private int consumerId;
//
//    public Consumer(Buffer _buf, int consumerId) {
//        this._buf = _buf;
//        this.consumerId = consumerId;
//    }
//
//    public void run() {
//        for (int i = 0; i < 100; ++i) {
//            _buf.get(consumerId);
//        }
//    }
//}
//
//class Buffer {
//    private int item; // bufor przechowuje jeden element
//    private boolean isFull = false; // flaga stanu bufora
//
//    // synchronizacja przy pomocy wait() oraz notify()
//    public synchronized void put(int i, int producerId) {
//        while (isFull) {
//            try {
//                wait(); // czekaj jeśli bufor jest pełny
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//                return;
//            }
//        }
//        item = i;
//        isFull = true;
//        System.out.println("Producer " + producerId + " put: " + i);
//        notifyAll();
//    }
//
//    public synchronized int get(int consumerId) {
//        while (!isFull) {
//            try {
//                wait(); // czekaj jeśli bufor pusty
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//                return -1;
//            }
//        }
//        int value  = item;
//        isFull = false;
//        System.out.println("Consumer " + consumerId + " got: " + value);
//        notifyAll();
//        return value;
//    }
//}
//
//public class PKmon {
//    public static void main(String[] args) {
//        // podpunkt b)
//        // n1 producentów n2 konsumentów
//        Buffer buffer = new Buffer();
//
//        // n1=n2
//        int n1 = 3; // liczba producentów
//        int n2 = 2; // liczba konsumentów
//
//        Producer[] producers = new Producer[n1];
//        for (int i = 0; i < n1; i++) {
//            producers[i] = new Producer(buffer, i);
//            producers[i].start();
//        }
//
//        Consumer[] consumers = new Consumer[n2];
//        for (int i = 0; i < n2; i++) {
//            consumers[i] = new Consumer(buffer, i);
//            consumers[i].start();
//        }
//
//        try {
//            for (int i = 0; i < n1; i++) {
//                producers[i].join();
//            }
//            for (int i = 0; i < n2; i++) {
//                consumers[i].join();
//            }
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//    }
//}

// ---------------------------------------------------------------------------------------------------------------------

//class Producer extends Thread {
//    private Buffer _buf;
//    private int producerId;
//    private long sleepTime;
//    private long startTime;
//    private int iterations;
//
//    public Producer(Buffer _buf, int producerId, long sleepTime, long startTime, int iterations) {
//        this._buf = _buf;
//        this.producerId =producerId;
//        this.sleepTime = sleepTime;
//        this.startTime = startTime;
//        this.iterations = iterations;
//    }
//
//    public void run() {
//        for (int i = 0; i < iterations; ++i) {
//            try {
//                long elapsed = System.currentTimeMillis() - startTime;
//                System.out.printf("[%5d ms] Producer %d put: %d%n", elapsed, producerId, i);
//                _buf.put(i, producerId);
//                Thread.sleep(sleepTime);
//            } catch (InterruptedException e){
//                Thread.currentThread().interrupt();
//                break;
//            }
//        }
//        System.out.printf("Producer %d zakonczyl prace%n", producerId);
//    }
//}
//
//class Consumer extends Thread {
//    private Buffer _buf;
//    private int consumerId;
//    private long sleepTime;
//    private long startTime;
//    private int iterations;
//
//    public Consumer(Buffer _buf, int consumerId,  long sleepTime, long startTime,  int iterations) {
//        this._buf = _buf;
//        this.consumerId = consumerId;
//        this.sleepTime = sleepTime;
//        this.startTime = startTime;
//        this.iterations = iterations;
//    }
//
//    public void run() {
//        for (int i = 0; i < iterations; ++i) {
//            try {
//                long value = _buf.get(consumerId);
//                long elapsed = System.currentTimeMillis() - startTime;
//                System.out.printf("[%5d ms] Consumer %d got: %d%n", elapsed, consumerId, value);
//                Thread.sleep(sleepTime);
//            } catch (InterruptedException e){
//                Thread.currentThread().interrupt();
//                break;
//            }
//        }
//        System.out.printf("Consumer %d zakonczyl prace%n", consumerId);
//    }
//}
//
//class Buffer {
//    private int item; // bufor przechowuje jeden element
//    private boolean isFull = false; // flaga stanu bufora
//    private int putCount = 0;
//    private int getCount = 0;
//
//    // synchronizacja przy pomocy wait() oraz notify()
//    public synchronized void put(int i, int producerId) {
//        while (isFull) {
//            try {
//                System.out.printf("  [WAIT] Producer %d czeka (buffer pelny)%n", producerId);
//                wait(); // czekaj jeśli bufor jest pełny
//                System.out.printf("  [RESUME] Producer %d wznawia prace%n", producerId);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//                return;
//            }
//        }
//        item = i;
//        isFull = true;
//        putCount++;
//        notifyAll(); // powiadom wszystkich czekających
//    }
//
//    public synchronized int get(int consumerId) {
//        while (!isFull) {
//            try {
//                System.out.printf("  [WAIT] Consumer %d czeka (buffer pusty)%n", consumerId);
//                wait();
//                System.out.printf("  [RESUME] Consumer %d wznawia prace%n", consumerId);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//                return -1;
//            }
//        }
//        int value  = item;
//        isFull = false;
//        getCount++;
//        notifyAll(); // powiadom wszystkich czekających
//        return value;
//    }
//
//    public synchronized void printStatistics() {
//        System.out.println("\n=== STATYSTYKA BUFORA ===");
//        System.out.println("Lacznie wstawiono: " + putCount);
//        System.out.println("Lacznie pobrano: " + getCount);
//    }
//}
//
//public class PKmon {
//    public static void main(String[] args) {
//        long startTime = System.currentTimeMillis();
//        // podpunkt c)
//        // n1 producentów n2 konsumentów
//
//        // n1>n2
//        int n1 = 3; // liczba producentów
//        int n2 = 2; // liczba konsumentów
//
//        long producerSleep = 100;
//        long consumerSleep = 100;
//
//        int totalItems = 300;
//        int producerIterations = totalItems / n1;
//        int consumerIterations = totalItems / n2;
//
//        Buffer buffer = new Buffer();
//
//        System.out.println("=== KONFIGURACJA ===");
//        System.out.println("Liczba producentow: " + n1);
//        System.out.println("Liczba konsumentow: " + n2);
//        System.out.println("Sleep producenta: " + producerSleep + " ms");
//        System.out.println("Sleep konsumenta: " + consumerSleep + " ms");
//        System.out.println("=====================================\n");
//
//        Producer[] producers = new Producer[n1];
//        for (int i = 0; i < n1; i++) {
//            producers[i] = new Producer(buffer, i, producerSleep, startTime, producerIterations);
//            producers[i].start();
//        }
//
//        Consumer[] consumers = new Consumer[n2];
//        for (int i = 0; i < n2; i++) {
//            consumers[i] = new Consumer(buffer, i,  consumerSleep, startTime, consumerIterations);
//            consumers[i].start();
//        }
//
//        try {
//            for (int i = 0; i < n1; i++) {
//                producers[i].join();
//            }
//            for (int i = 0; i < n2; i++) {
//                consumers[i].join();
//            }
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//
//        long endTime = System.currentTimeMillis();
//        long totalTime = endTime - startTime;
//        buffer.printStatistics();
//        System.out.println("Calkowity czas wykonania: " + totalTime + " ms");
//        System.out.println("Wszystkie watki zakonczone");
//    }
//}

// ---------------------------------------------------------------------------------------------------------------------

//class Producer extends Thread {
//    private Buffer _buf;
//    private int iterations;
//    private int producerId;
//
//    public Producer(Buffer _buf, int producerId, int iterations) {
//        this._buf = _buf;
//        this.producerId = producerId;
//        this.iterations = iterations;
//    }
//
//    public void run() {
//        for (int i = 0; i < iterations; ++i) {
//            _buf.put(producerId, i);
//        }
//    }
//}
//
//class Consumer extends Thread {
//    private Buffer _buf;
//    private int consumerId;
//    private int iterations;
//
//    public Consumer(Buffer _buf, int consumerId, int iterations) {
//        this._buf = _buf;
//        this.consumerId = consumerId;
//        this.iterations = iterations;
//    }
//
//    public void run() {
//        for (int i = 0; i < iterations; ++i) {
//            int value = _buf.get(consumerId);
//            System.out.println("Konsument " + consumerId + " pobral: " + value);
//        }
//    }
//}
//
//class Buffer {
//    private final int[] buffer;
//    private int in = 0, out = 0;
//    private Semaphore mutex = new Semaphore(1);
//    private Semaphore full = new Semaphore(0);
//    private Semaphore empty;
//
//    public Buffer(int size) {
//        buffer = new int[size];
//        empty = new Semaphore(size);
//    }
//
//    public void put(int id, int value) {
//        empty.P(); // P(empty)
//        mutex.P(); // P(mutex)
//        System.out.println("Producent " + id + " wlozyl: " + value);
//        buffer[in] = value;
//        in = (in + 1) % buffer.length;
//        mutex.V(); // V(mutex)
//        full.V(); // V(full)
//    }
//
//    public int get(int id) {
//        full.P(); // P(full)
//        mutex.P(); // P(mutex)
//        int value = buffer[out];
//        out = (out + 1) % buffer.length;
//        mutex.V(); // V(mutex)
//        empty.V(); // V(empty)
//        return value;
//    }
//}
//
//class Semaphore {
//    private int value;
//
//    public Semaphore(int value) {
//        this.value = value;
//    }
//
//    public synchronized void P() {
//        while (value <= 0) {
//            try {
//                wait();
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//        }
//        value--;
//    }
//
//    public synchronized void V() {
//        value++;
//        notify();
//    }
//}
//
//public class PKmon {
//    public static void main(String[] args) {
//        Buffer buffer = new Buffer(100);
//        int n1 = 3;
//        int n2 = 2;
//        int iter1 = 100;
//        int iter2 = 150;
//
//        Producer[] producers = new Producer[n1];
//        Consumer[] consumers = new Consumer[n2];
//
//        for (int i = 0; i < n1; i++) {
//            producers[i] = new Producer(buffer, i, iter1);
//            producers[i].start();
//        }
//
//        for (int i = 0; i < n2; i++) {
//            consumers[i] = new Consumer(buffer, i, iter2);
//            consumers[i].start();
//        }
//
//        try {
//            for (Producer p : producers) {
//                p.join();
//            }
//            for (Consumer c : consumers) {
//                c.join();
//            }
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//    }
//}

// ---------------------------------------------------------------------------------------------------------------------

//import java.util.LinkedList;
//
//// przetwarzanie potokowe z buforem
//class Semaphore {
//    private int value;
//
//    public Semaphore(int value) {
//        this.value = value;
//    }
//
//    public synchronized void P() {
//        while (value <= 0) {
//            try {
//                wait();
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//        }
//        value--;
//    }
//
//    public synchronized void V() {
//        value++;
//        notifyAll();
//    }
//}
//
//class PipelineBuffer {
//    private final int[] buffer;
//    private final int[] outs;
//    private int in = 0;
//    private final Semaphore mutex = new Semaphore(1);
//    private final Semaphore empty;
//    private final Semaphore[] ready;
//    private final int size;
//
//    public PipelineBuffer(int size, int stages) {
//        this.size = size;
//        buffer = new int[size];
//        outs = new int[stages + 1];
//        empty = new Semaphore(size);
//        ready = new Semaphore[stages + 1];
//        for (int i = 0; i < ready.length; i++) ready[i] = new Semaphore(0);
//    }
//
//    public void put(int value) {
//        empty.P();  // Czekaj na wolne miejsce
//        mutex.P();
//        int slot = in;
//        in = (in + 1) % size;
//        buffer[slot] = value;
//        mutex.V();
//        ready[0].V();
//        System.out.println("[PRODUCENT] → slot " + slot + " = " + value);
//    }
//
//    public void process(int stage) {
//        ready[stage].P();  // Czekaj na dane z poprzedniego etapu
//
//        // Sekcja krytyczna 1: Odczyt z bufora
//        int slot;
//        int val;
//        mutex.P();
//        slot = outs[stage];
//        outs[stage] = (outs[stage] + 1) % size;
//        val = buffer[slot];  // Kopiuj wartość
//        mutex.V();
//
//        val += (stage + 1) * 100;
//        try {
//            Thread.sleep((stage + 1) * 10);  // Symulacja czasu pracy
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//
//        // Sekcja krytyczna 2: Zapis wyniku
//        mutex.P();
//        buffer[slot] = val;
//        mutex.V();
//
//        ready[stage + 1].V();  // Sygnalizuj następnemu etapowi
//        System.out.println("[ETAP " + stage + "] slot " + slot + " = " + val);
//    }
//
//    public void get() {
//        ready[ready.length - 1].P();  // Czekaj na dane z ostatniego etapu
//        mutex.P();
//        int slot = outs[outs.length - 1];
//        outs[outs.length - 1] = (outs[outs.length - 1] + 1) % size;
//        int val = buffer[slot];
//        mutex.V();
//        System.out.println("[KONSUMENT] ← slot " + slot + " = " + val);
//        empty.V();  // Zwolnij miejsce w buforze
//    }
//}
//
//class Producer extends Thread {
//    private final PipelineBuffer buf;
//    private final int count;
//    public Producer(PipelineBuffer b, int c) { buf = b; count = c; }
//    public void run() {
//        for (int i = 0; i < count; i++) {
//            buf.put(i);
//            try { Thread.sleep(20); } catch (InterruptedException ignored) {}
//        }
//        System.out.println("[PRODUCENT] Koniec");
//    }
//}
//
//class ProcessingStage extends Thread {
//    private final PipelineBuffer buf;
//    private final int stage, count;
//    public ProcessingStage(PipelineBuffer b, int s, int c) {
//        buf = b; stage = s; count = c; setName("Stage-" + s);
//    }
//    public void run() {
//        for (int i = 0; i < count; i++) buf.process(stage);
//        System.out.println("[ETAP " + stage + "] Koniec");
//    }
//}
//
//class Consumer extends Thread {
//    private final PipelineBuffer buf;
//    private final int count;
//    public Consumer(PipelineBuffer b, int c) { buf = b; count = c; }
//    public void run() {
//        for (int i = 0; i < count; i++) buf.get();
//        System.out.println("[KONSUMENT] Koniec");
//    }
//}
//
//// ---------- Main ----------
//public class PKmon {
//    public static void main(String[] args) {
//        int bufferSize = 100, stages = 5, items = 50;
//        System.out.println("=== PRZETWARZANIE POTOKOWE ===\n");
//
//        PipelineBuffer buf = new PipelineBuffer(bufferSize, stages);
//        Producer prod = new Producer(buf, items);
//        Consumer cons = new Consumer(buf, items);
//        ProcessingStage[] proc = new ProcessingStage[stages];
//
//        long start = System.currentTimeMillis();
//
//        prod.start();
//        for (int i = 0; i < stages; i++) {
//            proc[i] = new ProcessingStage(buf, i, items);
//            proc[i].start();
//        }
//        cons.start();
//
//        try {
//            prod.join();
//            cons.join();
//            for (ProcessingStage p : proc) p.join();
//        } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
//
//        long end = System.currentTimeMillis();
//        System.out.printf("\n=== WYNIKI ===\n");
//        System.out.printf("Czas wykonania: %d ms\n", (end - start));
//        System.out.printf("Przepustowość: %.2f elem/s\n",
//                (items * 1000.0 / (end - start)));
//    }
//}

// ---------------------------------------------------------------------------------------------------------------------

// przetwarzanie potokowe z buforem - WERSJA Z MONITORAMI (wait/notify)
//class PipelineBuffer {
//    private final int[] buffer;
//    private final int[] outs;
//    private int in = 0;
//    private final int size;
//
//    // Warunki (conditions)
//    private boolean[] dataReady;
//    private boolean allEmpty;
//
//    public PipelineBuffer(int size, int stages) {
//        this.size = size;
//        this.buffer = new int[size];
//        this.outs = new int[stages + 1];
//        this.dataReady = new boolean[stages + 1];
//        this.allEmpty = true;
//    }
//
//    // PRODUCENT - wstawia dane
//    public synchronized void put(int value) {
//        // Czekaj dopóki bufor pełny
//        while (!allEmpty) {
//            try {
//                wait();  // Czeka i zwalnia lock
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//        }
//
//        int slot = in;
//        in = (in + 1) % size;
//        buffer[slot] = value;
//
//        allEmpty = false;  // Bufor już nie pusty
//        dataReady[0] = true;
//
//        notifyAll();  // Powiadom wszystkie czekające wątki
//        System.out.println("[PRODUCENT] → slot " + slot + " = " + value);
//    }
//
//    // PROCESOR - przetwarzanie w etapie
//    public void process(int stage) {
//        // Czekaj na dane z poprzedniego etapu
//        synchronized(this) {
//            while (!dataReady[stage]) {
//                try {
//                    wait();
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                }
//            }
//
//            // Odczyt z bufora
//            int slot = outs[stage];
//            outs[stage] = (outs[stage] + 1) % size;
//            int val = buffer[slot];
//
//            // Resetuj flagę dla tego etapu
//            dataReady[stage] = false;
//
//            val += (stage + 1) * 100;
//            try {
//                Thread.sleep((stage + 1) * 10);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//
//            // Zapis wyniku - znowu w synchronized
//            synchronized(this) {
//                buffer[slot] = val;
//                dataReady[stage + 1] = true;  // Sygnalizuj następnemu etapowi
//                notifyAll();
//            }
//
//            System.out.println("[ETAP " + stage + "] slot " + slot + " = " + val);
//        }
//    }
//
//    // KONSUMENT - pobiera rezultat
//    public synchronized void get() {
//        // Czekaj na dane z ostatniego etapu
//        while (!dataReady[dataReady.length - 1]) {
//            try {
//                wait();
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//        }
//
//        int slot = outs[outs.length - 1];
//        outs[outs.length - 1] = (outs[outs.length - 1] + 1) % size;
//        int val = buffer[slot];
//
//        // Resetuj flagę dla konsumenta
//        dataReady[dataReady.length - 1] = false;
//
//        // Jeśli to była ostatnia dana, możemy znowu pisać
//        allEmpty = true;
//
//        notifyAll();
//        System.out.println("[KONSUMENT] ← slot " + slot + " = " + val);
//    }
//}
//
//class Producer extends Thread {
//    private final PipelineBuffer buf;
//    private final int count;
//    public Producer(PipelineBuffer b, int c) {
//        buf = b;
//        count = c;
//    }
//    public void run() {
//        for (int i = 0; i < count; i++) {
//            buf.put(i);
//            try { Thread.sleep(20); } catch (InterruptedException ignored) {}
//        }
//        System.out.println("[PRODUCENT] Koniec");
//    }
//}
//
//class ProcessingStage extends Thread {
//    private final PipelineBuffer buf;
//    private final int stage, count;
//    public ProcessingStage(PipelineBuffer b, int s, int c) {
//        buf = b;
//        stage = s;
//        count = c;
//        setName("Stage-" + s);
//    }
//    public void run() {
//        for (int i = 0; i < count; i++) buf.process(stage);
//        System.out.println("[ETAP " + stage + "] Koniec");
//    }
//}
//
//class Consumer extends Thread {
//    private final PipelineBuffer buf;
//    private final int count;
//    public Consumer(PipelineBuffer b, int c) {
//        buf = b;
//        count = c;
//    }
//    public void run() {
//        for (int i = 0; i < count; i++) buf.get();
//        System.out.println("[KONSUMENT] Koniec");
//    }
//}
//
//// ---------- Main ----------
//public class PKmon {
//    public static void main(String[] args) {
//        int bufferSize = 100, stages = 5, items = 50;
//        System.out.println("=== PRZETWARZANIE POTOKOWE (MONITORY) ===\n");
//
//        PipelineBuffer buf = new PipelineBuffer(bufferSize, stages);
//        Producer prod = new Producer(buf, items);
//        Consumer cons = new Consumer(buf, items);
//        ProcessingStage[] proc = new ProcessingStage[stages];
//
//        long start = System.currentTimeMillis();
//
//        prod.start();
//        for (int i = 0; i < stages; i++) {
//            proc[i] = new ProcessingStage(buf, i, items);
//            proc[i].start();
//        }
//        cons.start();
//
//        try {
//            prod.join();
//            cons.join();
//            for (ProcessingStage p : proc) p.join();
//        } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
//
//        long end = System.currentTimeMillis();
//        System.out.printf("\n=== WYNIKI ===\n");
//        System.out.printf("Czas wykonania: %d ms\n", (end - start));
//        System.out.printf("Przepustowosc: %.2f elem/s\n",
//                (items * 1000.0 / (end - start)));
//    }
//}