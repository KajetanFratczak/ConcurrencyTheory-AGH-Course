package org.example;

class Counter {
    private int _val;
    private PetersonLock _lock;

    public Counter(int n) {
        _val = n;
        _lock = new PetersonLock();
    }
    public void inc(int threadId) {
        _lock.lock(threadId);
        _val++;
        _lock.unlock(threadId);
    }
    public void dec(int threadId) {
        _lock.lock(threadId);
        _val--;
        _lock.unlock(threadId);
    }
    public int value() {
        return _val;
    }
}

class PetersonLock {
    private volatile boolean[] flag = new boolean[2];
    private volatile int turn;

    public PetersonLock() {
        flag[0] = false;
        flag[1] = false;
        turn = 0;
    }

    public void lock(int threadId) {
        int other = 1 - threadId;
        flag[threadId] = true;
        turn = other;
        while (flag[other] && turn == other) {
            // busy waiting
        }
    }

    public void unlock(int threadId) {
        flag[threadId] = false;
    }
}

// Watek, ktory inkrementuje licznik 100.000 razy
class IThread extends Thread {
    private Counter _counter;
    private static final int THREAD_ID = 0;

    public IThread(Counter counter) {
        _counter = counter;
    }
    @Override
    public void run() {
        for (int i = 0; i < 100000; i++) {
            _counter.inc(THREAD_ID);
        }
    }
}

// Watek, ktory dekrementuje licznik 100.000 razy
class DThread extends Thread {
    private Counter _counter;
    private static final int THREAD_ID = 1;
    public DThread(Counter counter) {
        _counter = counter;
    }
    @Override
    public void run() {
        for (int i = 0; i < 100000; i++) {
            _counter.dec(THREAD_ID);
        }
    }
}

public class Race {
    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            Counter cnt = new Counter(0);

            Thread t1 = new IThread(cnt);
            Thread t2 = new DThread(cnt);

            t1.start();
            t2.start();

            try{
                t1.join();
                t2.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("stan=" + cnt.value());
        }
    }
}