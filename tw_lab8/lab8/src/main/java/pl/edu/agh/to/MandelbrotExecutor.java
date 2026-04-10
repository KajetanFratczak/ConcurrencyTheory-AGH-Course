package pl.edu.agh.to;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import javax.swing.JFrame;

public class MandelbrotExecutor extends JFrame {

    private final int MAX_ITER;
    private final double ZOOM = 150;
    private BufferedImage I;
    private int width = 800;
    private int height = 600;

    // dodałem maxIter, żebym mógł modyfikować pod testy
    public MandelbrotExecutor(int maxIter) {
        super("Mandelbrot Set - Executor Service");
        this.MAX_ITER = maxIter;
        setBounds(100, 100, width, height);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        I = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
    }

    // Zadanie obliczające jeden wiersz obrazu
    private class RowTask implements Callable<Void> {
        private int y;

        public RowTask(int y) {
            this.y = y;
        }

        @Override
        public Void call(){
            double zx, zy, cX, cY, tmp;
            for (int x = 0; x < width; x++) {
                zx = zy = 0;
                cX = (x - 400) / ZOOM;
                cY = (y - 300) / ZOOM;
                int iter = MAX_ITER;
                while (zx * zx + zy * zy < 4 && iter > 0) {
                    tmp = zx * zx - zy * zy + cX;
                    zy = 2.0 * zx * zy + cY;
                    zx = tmp;
                    iter--;
                }
                // Zapis do bufora (wiersze są rozłączne, więc tak mogę zrobić)
                I.setRGB(x, y, iter | (iter << 8));
            }
            return null;
        }
    }

    public void runTest(ExecutorService pool, String poolName) {
        // Reset obrazu
        I = new  BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);

        long startTime = System.currentTimeMillis();
        List<Future<Void>> futures = new ArrayList<>();

        // Zlecanie zadań - 1 zadanie = 1 wiersz
        for(int y = 0; y < height; y++){
            futures.add(pool.submit(new RowTask(y)));
        }

        // Oczekiwanie na zakończenie wszystkich zadań
        for(Future<Void> future : futures){
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e){
                e.printStackTrace();
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Pool: " +  poolName);
        System.out.println("Total time taken: " + (endTime - startTime) + " ms");

        pool.shutdown();
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(I, 0, 0, this);
    }

    public static void main(String[] args) throws InterruptedException {

        // Scenariusze testowe
        int[] threadCounts = {1, 2, 5, 10, 15, 20};
        int[] maxIters = {100, 250, 500, 1000, 5000};

        System.out.println("=== Mandelbrot Set Performance Test ===");

        for (int maxIter : maxIters) {
            System.out.println("Max iterations: " + maxIter);

            // Nowy executor dla danej liczby iteracji
            MandelbrotExecutor executor = new MandelbrotExecutor(maxIter);
            //executor.setVisible(true);

            for (int threads : threadCounts) {
                System.out.println();
                System.out.println("Thread count: " + threads);
                // 1. Single Thread
                if (threads == 1) {
                    executor.runTest(Executors.newSingleThreadExecutor(), "SingleThread");
                    Thread.sleep(1000);
                }

                // 2. Fixed Thread Pool
                executor.runTest(Executors.newFixedThreadPool(threads), "FixedThreadPool");
                Thread.sleep(1000);

                // 3. Cached Thread Pool
                executor.runTest(Executors.newCachedThreadPool(), "CachedThreadPool");
                Thread.sleep(1000);

                // 4. Scheduled Thread Pool
                executor.runTest(Executors.newScheduledThreadPool(threads), "ScheduledThreadPool");
                Thread.sleep(1000);

                // 5. Work Stealing Thread Pool
                executor.runTest(Executors.newWorkStealingPool(threads), "WorkStealingThreadPool");
                Thread.sleep(1000);
            }
        }

    }
}
