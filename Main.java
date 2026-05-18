package miniproject.mandelbrot_multithreader;

import miniproject.mandelbrot_multithreader.gui.MandelDisplay;
import miniproject.mandelbrot_multithreader.logic.FractalRenderer;

public class Main {
    public static void main(String[] args){
        MandelDisplay display = new MandelDisplay();
        FractalRenderer renderer = new FractalRenderer(display);

        
        Thread thread = new Thread(renderer::run);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }
}
