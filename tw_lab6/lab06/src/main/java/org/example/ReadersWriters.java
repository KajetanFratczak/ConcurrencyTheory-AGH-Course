package org.example;

//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.Semaphore;
//import java.util.concurrent.locks.Condition;
//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.ReentrantLock;
//
//public class ReadersWriters {
//    // IMPLEMENTACJA Z SEMAFORAMI
//    static class SemaphoreReadersWriters {
//        private final Semaphore writeLock = new Semaphore(1);
//        private final Semaphore mutex = new Semaphore(1);
//        private int readCount = 0;
//
//        class Reader extends Thread {
//            private long waitingTime;
//
//            @Override
//            public void run() {
//                long startWait = System.currentTimeMillis();
//                try {
//                    mutex.acquire();
//                    readCount++;
//                    if (readCount == 1) {
//                        writeLock.acquire();
//                    }
//                    mutex.release();
//
//                    waitingTime = System.currentTimeMillis() - startWait;
//
//                    // Symulacja czytania
//                    Thread.sleep(50);
//
//                    mutex.acquire();
//                    readCount--;
//                    if (readCount == 0) {
//                        writeLock.release();
//                    }
//                    mutex.release();
//
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                }
//            }
//
//            public long getWaitingTime() {
//                return waitingTime;
//            }
//        }
//
//        class Writer extends Thread {
//            private long waitingTime;
//
//            @Override
//            public void run() {
//                long startWait = System.currentTimeMillis();
//                try {
//                    writeLock.acquire();
//
//                    waitingTime = System.currentTimeMillis() - startWait;
//
//                    // Symulacja pisania
//                    Thread.sleep(100);
//
//                    writeLock.release();
//
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                }
//            }
//
//            public long getWaitingTime() {
//                return waitingTime;
//            }
//        }
//
//        public void reset() {
//            readCount = 0;
//        }
//    }
//
//    // IMPLEMENTACJA ZE ZMIENNYMI WARUNKOWYMI
//    static class CondVarReadersWriters {
//        private final Lock lock = new ReentrantLock();
//        private final Condition canRead = lock.newCondition();
//        private final Condition canWrite = lock.newCondition();
//        private int readers = 0;
//        private int writers = 0;
//        private int waitingWriters = 0;
//
//        class Reader extends Thread {
//            private long waitingTime;
//
//            @Override
//            public void run() {
//                long startWait = System.currentTimeMillis();
//                try {
//                    lock.lock();
//                    try {
//                        // Czekaj jeśli są pisarze lub czekają pisarze (priorytet dla pisarzy)
//                        while (writers > 0 || waitingWriters > 0) {
//                            canRead.await();
//                        }
//                        readers++;
//                        waitingTime = System.currentTimeMillis() - startWait;
//                    } finally {
//                        lock.unlock();
//                    }
//
//                    // Symulacja czytania
//                    Thread.sleep(50);
//
//                    lock.lock();
//                    try {
//                        readers--;
//                        if (readers == 0) {
//                            canWrite.signal(); // Obudź pisarza
//                        }
//                    } finally {
//                        lock.unlock();
//                    }
//
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                }
//            }
//
//            public long getWaitingTime() {
//                return waitingTime;
//            }
//        }
//
//        class Writer extends Thread {
//            private long waitingTime;
//
//            @Override
//            public void run() {
//                long startWait = System.currentTimeMillis();
//                try {
//                    lock.lock();
//                    try {
//                        waitingWriters++;
//                        // Czekaj jeśli są czytelnicy lub inny pisarz
//                        while (readers > 0 || writers > 0) {
//                            canWrite.await();
//                        }
//                        waitingWriters--;
//                        writers++;
//                        waitingTime = System.currentTimeMillis() - startWait;
//                    } finally {
//                        lock.unlock();
//                    }
//
//                    // Symulacja pisania
//                    Thread.sleep(100);
//
//                    lock.lock();
//                    try {
//                        writers--;
//                        // Priorytet dla pisarzy
//                        if (waitingWriters > 0) {
//                            canWrite.signal();
//                        } else {
//                            canRead.signalAll(); // Obudź wszystkich czytelników
//                        }
//                    } finally {
//                        lock.unlock();
//                    }
//
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                }
//            }
//
//            public long getWaitingTime() {
//                return waitingTime;
//            }
//        }
//
//        public void reset() {
//            readers = 0;
//            writers = 0;
//            waitingWriters = 0;
//        }
//    }
//
//    // TESTY I POMIARY
//
//    public static void main(String[] args) throws InterruptedException, IOException {
//        // Zakres testów zgodnie z poleceniem
//        int[] readersArray = new int[91]; // 10-100
//        for (int i = 0; i < readersArray.length; i++) {
//            readersArray[i] = i + 10;
//        }
//
//        int[] writersArray = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
//
//        System.out.println("=".repeat(70));
//        System.out.println("TEST 1: IMPLEMENTACJA Z SEMAFORAMI");
//        System.out.println("=".repeat(70));
//        testImplementation(readersArray, writersArray, "semaphore","readers_writers_semaphore.csv");
//
//        System.out.println("\n" + "=".repeat(70));
//        System.out.println("TEST 2: IMPLEMENTACJA ZE ZMIENNYMI WARUNKOWYMI");
//        System.out.println("=".repeat(70));
//        testImplementation(readersArray, writersArray, "condvar","readers_writers_condvar.csv");
//
//        System.out.println("\n" + "=".repeat(70));
//        System.out.println("Testy zakończone!");
//        System.out.println("Wyniki zapisane w plikach CSV");
//        System.out.println("=".repeat(70));
//    }
//
//    private static void testImplementation(int[] readersArray, int[] writersArray, String type, String filename) throws InterruptedException, IOException {
//        try (FileWriter writer = new FileWriter(filename)) {
//            writer.write("Readers,Writers,TotalTime(ms),AvgWaitingTime(ms)\n");
//
//            int totalTests = readersArray.length * writersArray.length;
//            int currentTest = 0;
//
//            for (int readers : readersArray) {
//                for (int writers : writersArray) {
//                    currentTest++;
//
//                    System.out.printf("[%d/%d] Testowanie: %d czytelników, %d pisarzy... ",
//                            currentTest, totalTests, readers, writers);
//
//                    long totalTime;
//                    long avgWaitingTime;
//
//                    if (type.equals("semaphore")) {
//                        SemaphoreReadersWriters rw = new SemaphoreReadersWriters();
//                        rw.reset();
//
//                        List<Thread> threads = new ArrayList<>();
//                        List<SemaphoreReadersWriters.Reader> readersList = new ArrayList<>();
//                        List<SemaphoreReadersWriters.Writer> writersList = new ArrayList<>();
//
//                        for (int i = 0; i < readers; i++) {
//                            SemaphoreReadersWriters.Reader reader = rw.new Reader();
//                            threads.add(reader);
//                            readersList.add(reader);
//                        }
//
//                        for (int i = 0; i < writers; i++) {
//                            SemaphoreReadersWriters.Writer writerThread = rw.new Writer();
//                            threads.add(writerThread);
//                            writersList.add(writerThread);
//                        }
//
//                        long startTime = System.currentTimeMillis();
//
//                        for (Thread thread : threads) {
//                            thread.start();
//                        }
//
//                        for (Thread thread : threads) {
//                            thread.join();
//                        }
//
//                        totalTime = System.currentTimeMillis() - startTime;
//
//                        long totalWaitingTime = 0;
//                        for (SemaphoreReadersWriters.Reader reader : readersList) {
//                            totalWaitingTime += reader.getWaitingTime();
//                        }
//                        for (SemaphoreReadersWriters.Writer writerThread : writersList) {
//                            totalWaitingTime += writerThread.getWaitingTime();
//                        }
//
//                        avgWaitingTime = totalWaitingTime / (readers + writers);
//
//                    } else { // condvar
//                        CondVarReadersWriters rw = new CondVarReadersWriters();
//                        rw.reset();
//
//                        List<Thread> threads = new ArrayList<>();
//                        List<CondVarReadersWriters.Reader> readersList = new ArrayList<>();
//                        List<CondVarReadersWriters.Writer> writersList = new ArrayList<>();
//
//                        for (int i = 0; i < readers; i++) {
//                            CondVarReadersWriters.Reader reader = rw.new Reader();
//                            threads.add(reader);
//                            readersList.add(reader);
//                        }
//
//                        for (int i = 0; i < writers; i++) {
//                            CondVarReadersWriters.Writer writerThread = rw.new Writer();
//                            threads.add(writerThread);
//                            writersList.add(writerThread);
//                        }
//
//                        long startTime = System.currentTimeMillis();
//
//                        for (Thread thread : threads) {
//                            thread.start();
//                        }
//
//                        for (Thread thread : threads) {
//                            thread.join();
//                        }
//
//                        totalTime = System.currentTimeMillis() - startTime;
//
//                        long totalWaitingTime = 0;
//                        for (CondVarReadersWriters.Reader reader : readersList) {
//                            totalWaitingTime += reader.getWaitingTime();
//                        }
//                        for (CondVarReadersWriters.Writer writerThread : writersList) {
//                            totalWaitingTime += writerThread.getWaitingTime();
//                        }
//
//                        avgWaitingTime = totalWaitingTime / (readers + writers);
//                    }
//
//                    writer.write(String.format("%d,%d,%d,%d\n",
//                            readers, writers, totalTime, avgWaitingTime));
//
//                    System.out.printf("Czas: %d ms, Śr. oczekiwanie: %d ms\n",
//                            totalTime, avgWaitingTime);
//                }
//            }
//        }
//    }
//}
