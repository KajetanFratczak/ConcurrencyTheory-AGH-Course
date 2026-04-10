class Semafor {
    private boolean _stan = true; // mówi, czy semafor jest wolny
    private int _czeka = 0; // liczba czekających wątków

    public Semafor() {

    }

    // Semafor binarny - w skrócie to zmienna sterująca dostępem do sekcji krytycznej (0 lub 1).
    // wait() - usypia wątek i zwalnia monitor obiektu
    // notify() - budzi wątek
    // P - opuszczenie, V - podniesienie
    // w P zajmujemy sekcję krytyczną, a w V zwalniamy
    // P: jeśli S == 1, ustaw na 0 i kontynuuj, jeśli S==0, zablokuj wątek (czekaj)
    // V: jeśli jakieś wątki oczekują (1 lub 2) - obudź jeden z nich, w przeciwnym razie ustaw S=1

    public synchronized void P() {
        while (!_stan) {
            _czeka++;
            try {
                wait();
            } catch (InterruptedException e) {}
            _czeka--;
        }
        _stan = false;
    }

    public synchronized void V() {
        if (_czeka > 0) {
            notify();
        }
        _stan = true;
    }
}

// init - ilość dostępnego zasobu - tutaj ilość możliwych wątków
// V - inkrementujemy licznik i jeśli ktoś czeka to budzimy
// P - dekrementujemy licznik i wpuszczamy, a jeśli licznik == 0 wątek musi czekać

class SemaforZliczajacy {
    private int _dost_zasoby = 0; // licznik dostępnych zasobów
    private Semafor mutex; // mutex do ochrony zmiennej _dost_zasoby

    public SemaforZliczajacy(int init) {
        _dost_zasoby = init;
        mutex = new Semafor();
    }

    public void P() {
        mutex.P();
        while (_dost_zasoby == 0) {
            mutex.V();
            try {
                synchronized (this) {
                    wait();
                }
            } catch (InterruptedException e) {}
            mutex.P();
        }
        _dost_zasoby--;
        mutex.V();
    }

    public void V() {
        mutex.P();
        _dost_zasoby++;
        if (_dost_zasoby > 0){
            synchronized (this) {
                notify();
            }
        }
        mutex.V();
    }
}

class Counter {
    private int _val;
    SemaforZliczajacy s = new SemaforZliczajacy(1);

    public Counter(int n) {
        _val = n;
    }
    public void inc() {
        s.P();
        _val++;
        s.V();
    }
    public void dec() {
        s.P();
        _val--;
        s.V();

    }
    public int value() {
        return _val;
    }
}

class IThread extends Thread {
    private Counter _cnt;
    public IThread(Counter c) {
        _cnt = c;
    }
    public void run() {
        for (int i = 0; i < 100000000; ++i) {
//		try { this.sleep(50); }
//			catch(Exception e) {}
            _cnt.inc();
        }
    }
}

class DThread extends Thread {
    private Counter _cnt;
    public DThread(Counter c) {
        _cnt = c;
    }
    public void run() {
        for (int i = 0; i < 100000000; ++i) {
            _cnt.dec();
//		try { this.sleep(1); }
//			catch(Exception e) {}
        }
    }
}

class Race2 {
    public static void main(String[] args) {
        Counter cnt = new Counter(0);
        IThread it = new IThread(cnt);
        DThread dt = new DThread(cnt);

        it.start();
        dt.start();

        try {
            it.join();
            dt.join();
        } catch(InterruptedException ie) { }

        System.out.println("value=" + cnt.value());
    }
}