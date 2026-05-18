package miniproject.mandelbrot_multithreader.logic;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import miniproject.mandelbrot_multithreader.gui.MandelDisplay;
import miniproject.mandelbrot_multithreader.util.ColorPalette;

public class FractalRenderer {
    private ColorPalette colorPalette;
    private MandelDisplay mandelDisplay;

    private BufferedImage image; 
    
    private int width;
    private int height;

    private double viewX = 0.0;
    private double viewY = 0.0;
    private double zoom = 1.0;

    static int maxIterations = 20;
    private int threadCount = 4; 
    private ExecutorService threadPool;

    private volatile boolean cancelled = false;
    private volatile boolean dirty = true;

    public FractalRenderer(MandelDisplay mandelDisplay){
        this.mandelDisplay = mandelDisplay;
        this.colorPalette = new ColorPalette();
    }

    public synchronized void setThreadCount(int count) {
        this.threadCount = count;
        redraw();
    }

    public synchronized void redraw() {
        cancelled = true;
        if (threadPool != null) {
            threadPool.shutdownNow();
        }
        dirty = true;
        notifyAll();
    }

    public void run() {
        while (true) {
            cancelled = false;
            draw();
            synchronized (this) {
                if (!dirty) {
                    try { wait(); }
                    catch (InterruptedException e) { break; }
                }
            }
        }
    }

    private boolean draw() {
        Dimension size = mandelDisplay.getSize();
        
        if (image == null || size.width != width || size.height != height || dirty) {
            width = size.width;
            height = size.height;
            this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            mandelDisplay.setImage(this.image);
            dirty = false; 
            
            // Keep the label synced up on initial draw passes
            mandelDisplay.updateIterationDisplay(maxIterations);
        }

        long startTime = System.currentTimeMillis();
        threadPool = Executors.newFixedThreadPool(threadCount);

        // Linear Striping: We submit tasks based on the number of threads allocated.
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            
            threadPool.submit(() -> {
                // Each thread skips rows based on total thread count (Thread 0 handles 0, 4, 8...)
                // This ensures threads NEVER cross paths or overwrite each other's memory space.
                for (int y = threadId; y < height; y += threadCount) {
                    if (cancelled || Thread.interrupted()) return;
                    
                    for (int x = 0; x < width; x++) {
                        double r = zoom / Math.min(width, height);
                        double dx = 2.5 * (x * r + viewX) - 2;
                        double dy = 1.25 - 2.5 * (y * r + viewY);

                        Color color = renderColor(dx, dy);
                        
                        image.setRGB(x, y, color.getRGB());
                    }
                }
                mandelDisplay.repaint();
            });
        }

        threadPool.shutdown();
        try {
            threadPool.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
        }

        long endTime = System.currentTimeMillis();
        mandelDisplay.updateRenderTime(endTime - startTime);

        return false;
    }

    private Color renderColor(double x, double y) {
        // Change back to picking up the int return safely
        int count = MandelMath.calculateFractalPixel(0.0, 0.0, x, y, maxIterations);
        return colorPalette.getColor(count);
    }

    public synchronized void zoomToRect(Point p1, Point p2) {
        maxIterations = (int)(200 + Math.log10(3.0 / zoom) * 150);
        
        // Keep the text display synchronized when mouse triggers an auto-scaling increase
        mandelDisplay.updateIterationDisplay(maxIterations);

        int x = Math.min(p1.x, p2.x);
        int y = Math.min(p1.y, p2.y);
        int w = Math.abs(p2.x - p1.x);
        int h = Math.abs(p2.y - p1.y);

        if (w < 4 || h < 4) return;

        double r = zoom / Math.min(width, height);
        double newX = x * r + viewX;
        double newY = y * r + viewY;
        zoom *= (double) w / width;
        viewX = newX;
        viewY = newY;
        redraw();
    }

    // Public method to adjust processing limit depth step-by-step
    public synchronized void changeIterations(int amount) {
        // Enforce a minimum floor constraint of 5 iterations so calculations don't break
        maxIterations = Math.max(5, maxIterations + amount);
        
        // Push update to GUI view tracking state label
        mandelDisplay.updateIterationDisplay(maxIterations);
        
        // Force the layout thread system pool to cleanly reconstruct structural details
        redraw();
    }

    /**
     * Resets the fractal coordinates and iterations back to their starting values
     */
    public synchronized void resetView() {
        // Bring coordinates back to original framing bounds
        this.viewX = 0.0;
        this.viewY = 0.0;
        this.zoom = 1.0;
        
        // Reset iterations back to baseline baseline detail profile
        maxIterations = 20;
        
        // Synchronize the text indicator on the panel display
        mandelDisplay.updateIterationDisplay(maxIterations);
        
        // Wipe current calculations and trigger a fresh render
        redraw();
    }

    /**
     * Instructs the color engine to cycle palettes and triggers an instant screen refresh
     */
    public synchronized void cycleColorPalette() {
        colorPalette.cyclePalette();
        redraw();
    }
}