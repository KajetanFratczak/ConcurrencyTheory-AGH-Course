package org.example;

//// 1 rozwiązanie - trywialne z symetrycznymi filozofami
//// problem - deadlock (blokada)
//// w tym samym momencie podniosą lewy widelec - każdy z nich czeka na prawy widelec, ale ten jest już trzymany przez sąsiada
//// nikt nie może kontynuować - wszyscy czekają wiecznie
//
//class Widelec {
//    private boolean zajety = false;
//
//    public synchronized void podnies() {
//        // dopóki widelec jest zajęty, to reszta czeka - widelec może być zajęty na raz przez jednego filozofa
//        while (zajety){
//            try {
//                wait();
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        zajety = true;
//    }
//    public synchronized void odloz() {
//        zajety = false;
//        notify();
//    }
//}
//
//// każdy filozof to osobny wątek
//class Filozof extends Thread {
//    private final Widelec widelecLewy, widelecPrawy;
//    private int _licznik = 0;
//
//    public Filozof(Widelec widelecLewy, Widelec widelecPrawy) {
//        this.widelecLewy = widelecLewy;
//        this.widelecPrawy = widelecPrawy;
//    }
//
//    public void run() {
//        while (true) {
//            // myślenie
//            try {
//                Thread.sleep((int)(Math.random()*30));
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//
//            // jedzenie
//            // do jedzenia filozof podnosi oba widelce
//            widelecLewy.podnies();
//            // czas między podniesieniem widelca prawego i lewego
//            // losowe opóźnienie - jest to po to, aby pokazać możliwość deadlocka
//            try {
//                if (Math.random() < 0.1) {
//                    Thread.sleep(10);
//                }
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//            widelecPrawy.podnies();
//
//            ++_licznik;
//            if (_licznik % 100 == 0) {
//                System.out.println("Filozof: " + Thread.currentThread().getName() +
//                        " jadlem " + _licznik + " razy");
//            }
//            // czas jedzenia
//            try {
//                Thread.sleep((int)(Math.random()*30));
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//
//            widelecLewy.odloz();
//            widelecPrawy.odloz();
//            // koniec jedzenia
//        }
//    }
//}
//
//public class Fil5mon {
//    public static void main(String[] args) {
//        int liczbaFilozofow = 5;
//        Widelec[] widelce = new Widelec[liczbaFilozofow];
//        Filozof[] filozofowie  = new Filozof[liczbaFilozofow];
//        for (int i = 0; i < liczbaFilozofow; i++) {
//            widelce[i] = new Widelec();
//        }
//        for (int i = 0; i < liczbaFilozofow; i++) {
//            Widelec widelecLewy = widelce[i];
//            Widelec widelecPrawy = widelce[(i+1)%liczbaFilozofow];
//            filozofowie[i] = new Filozof(widelecLewy, widelecPrawy);
//            filozofowie[i].start();
//        }
//    }
//}

// --------------------------------------------------------------------------------------------------------------------------------

//////// 2 rozwiazanie - widelce podnoszone jednocześnie
//////// zapobiega to deadlockowi
//////// jaki problem może tu wystąpić ? - problem zagłodzenia
//class Widelec {
//    private boolean zajety = false;
//
//    public synchronized void podnies() {
//        // dopóki widelec jest zajęty, to reszta czeka - widelec może być zajęty na raz przez jednego filozofa
//        while (zajety){
//            try {
//                wait();
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        zajety = true;
//    }
//    public synchronized void odloz() {
//        zajety = false;
//        notify();
//    }
//}
//
//// każdy filozof to osobny wątek
//class Filozof extends Thread {
//    private final Widelec widelecLewy, widelecPrawy;
//    private int _licznik = 0;
//
//    // shared lock na cały stół
//    private final Object lock;
//
//    public Filozof(Widelec widelecLewy, Widelec widelecPrawy, Object lock) {
//        this.widelecLewy = widelecLewy;
//        this.widelecPrawy = widelecPrawy;
//        this.lock = lock;
//    }
//
//    public void run() {
//        while (true) {
//            // myślenie
//            try {
//                Thread.sleep((int)(Math.random()*30));
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//            // jedzenie
//            // do jedzenia filozof podnosi oba widelce jednocześnie
//            synchronized (lock) {
//                widelecLewy.podnies();
//                widelecPrawy.podnies();
//
//                ++_licznik;
//                if (_licznik % 100 == 0) {
//                    System.out.println("Filozof: " + Thread.currentThread().getName() +
//                            " jadlem " + _licznik + " razy");
//                }
//                // czas jedzenia
//                try {
//                    Thread.sleep((int) (Math.random() * 30));
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//                widelecLewy.odloz();
//                widelecPrawy.odloz();
//            }
//            // koniec jedzenia
//        }
//    }
//}
//
//public class Fil5mon {
//    public static void main(String[] args) {
//        int liczbaFilozofow = 5;
//        Widelec[] widelce = new Widelec[liczbaFilozofow];
//        Filozof[] filozofowie  = new Filozof[liczbaFilozofow];
//        Object sharedLock = new Object();
//
//        for (int i = 0; i < liczbaFilozofow; i++) {
//            widelce[i] = new Widelec();
//        }
//
//        for (int i = 0; i < liczbaFilozofow; i++) {
//            Widelec widelecLewy = widelce[i];
//            Widelec widelecPrawy = widelce[(i+1)%liczbaFilozofow];
//            filozofowie[i] = new Filozof(widelecLewy, widelecPrawy, sharedLock);
//            filozofowie[i].start();
//        }
//    }
//}

// ---------------------------------------------------------------------------------------------------------------------------
//// 3 rozwiazanie - z lokajem
//// lokaj to taki strażnik przy stole
//// dba o to, aby max 4 filozfów na raz było przy stole
//// zastosowanie lokaja powoduje usunięcie problemu zakleszczenia i zagłodzenia
//
//class Lokaj {
//    private int liczbaPrzyStole = 0;
//
//    public synchronized void wejdz() throws InterruptedException {
//        while (liczbaPrzyStole >= 4) {
//            wait();
//        }
//        liczbaPrzyStole++;
//    }
//    public synchronized void wyjdz() throws InterruptedException {
//        liczbaPrzyStole--;
//        notifyAll();
//    }
//}
//
//class Widelec {
//    private boolean zajety = false;
//
//    public synchronized void podnies() {
//        // dopóki widelec jest zajęty, to reszta czeka - widelec może być zajęty na raz przez jednego filozofa
//        while (zajety){
//            try {
//                wait();
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        zajety = true;
//    }
//    public synchronized void odloz() {
//        zajety = false;
//        notify();
//    }
//}
//
//// każdy filozof to osobny wątek
//class Filozof extends Thread {
//    private final Widelec widelecLewy, widelecPrawy;
//    private int _licznik = 0;
//    private final Lokaj lokaj;
//
//    public Filozof(Widelec widelecLewy, Widelec widelecPrawy, Lokaj lokaj) {
//        this.widelecLewy = widelecLewy;
//        this.widelecPrawy = widelecPrawy;
//        this.lokaj = lokaj;
//    }
//
//    public void run() {
//        while (true) {
//            // myślenie
//            try {
//                Thread.sleep((int)(Math.random()*30));
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//
//            // prośba o pozwolenia na jedzenie
//            try {
//                lokaj.wejdz();
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//
//            // jedzenie
//            widelecLewy.podnies();
//            widelecPrawy.podnies();
//
//            ++_licznik;
//            if (_licznik % 100 == 0) {
//                System.out.println("Filozof: " + Thread.currentThread().getName() +
//                        " jadlem " + _licznik + " razy");
//            }
//            // czas jedzenia
//            try {
//                Thread.sleep((int)(Math.random()*30));
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//
//            widelecLewy.odloz();
//            widelecPrawy.odloz();
//
//            // zwolnienie miejsca przy stole
//            try {
//                lokaj.wyjdz();
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//            // koniec jedzenia
//        }
//    }
//}
//
//public class Fil5mon {
//    public static void main(String[] args) {
//        int liczbaFilozofow = 5;
//        Widelec[] widelce = new Widelec[liczbaFilozofow];
//        Filozof[] filozofowie  = new Filozof[liczbaFilozofow];
//        Lokaj lokaj = new Lokaj();
//
//        for (int i = 0; i < liczbaFilozofow; i++) {
//            widelce[i] = new Widelec();
//        }
//
//        for (int i = 0; i < liczbaFilozofow; i++) {
//            Widelec widelecLewy = widelce[i];
//            Widelec widelecPrawy = widelce[(i+1)%liczbaFilozofow];
//            filozofowie[i] = new Filozof(widelecLewy, widelecPrawy,  lokaj);
//            filozofowie[i].start();
//        }
//    }
//}

//----------------------------------------------------------------------------------------------------------------------------------
// 4 rozwiązanie - autorskie
// ustalam odgórnie pewną wartość widelca (od 1 do 5) (taka hierarchia)
// filozof zawsze będzie chciał podnieść widelec o jak najmniejszym numerze
// deadlocka nie będzie dzięki takiemu rozwiązaniu
// tylko chyba może w tym przypadku dojść do zagłodzenia, a na pewno nie jest to sprawiedliwe rozwiązanie, ponieważ
// jedni będą jeść częściej niż inni

class Widelec {
    private boolean zajety = false;
    private final int numer;

    public Widelec(int numer) {
        this.numer = numer;
    }

    public int getNumer() {
        return numer;
    }

    public synchronized void podnies() {
        // dopóki widelec jest zajęty, to reszta czeka - widelec może być zajęty na raz przez jednego filozofa
        while (zajety){
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        zajety = true;
    }
    public synchronized void odloz() {
        zajety = false;
        notify();
    }
}

// każdy filozof to osobny wątek
class Filozof extends Thread {
    private final Widelec widelecLewy, widelecPrawy;
    private int _licznik = 0;

    public Filozof(Widelec widelecLewy, Widelec widelecPrawy) {
        this.widelecLewy = widelecLewy;
        this.widelecPrawy = widelecPrawy;
    }

    public void run() {
        while (true) {
            // myślenie
            try {
                Thread.sleep((int)(Math.random()*30));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // wybieramy który widelec podnosimy jako pierwszy
            Widelec pierwszy, drugi;
            if (widelecLewy.getNumer() < widelecPrawy.getNumer()) {
                pierwszy = widelecLewy;
                drugi = widelecPrawy;
            } else{
                pierwszy = widelecPrawy;
                drugi = widelecLewy;
            }

            pierwszy.podnies();
            // czas między podniesieniem widelca prawego i lewego
            try {
                if (Math.random() < 0.1) {
                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            drugi.podnies();

            ++_licznik;
            if (_licznik % 100 == 0) {
                System.out.println("Filozof: " + Thread.currentThread().getName() +
                        " jadlem " + _licznik + " razy");
            }
            // czas jedzenia
            try {
                Thread.sleep((int)(Math.random()*30));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // odkładnie widelców - kolejność bez znaczenia
            widelecLewy.odloz();
            widelecPrawy.odloz();
            // koniec jedzenia
        }
    }
}

public class Fil5mon {
    public static void main(String[] args) {
        int liczbaFilozofow = 5;
        Widelec[] widelce = new Widelec[liczbaFilozofow];
        Filozof[] filozofowie  = new Filozof[liczbaFilozofow];

        for (int i = 0; i < liczbaFilozofow; i++) {
            widelce[i] = new Widelec(i+1);
        }

        for (int i = 0; i < liczbaFilozofow; i++) {
            Widelec widelecLewy = widelce[i];
            Widelec widelecPrawy = widelce[(i+1)%liczbaFilozofow];
            filozofowie[i] = new Filozof(widelecLewy, widelecPrawy);
            filozofowie[i].start();
        }
    }
}