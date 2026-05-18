package miniproject.mandelbrot_multithreader;

import miniproject.mandelbrot_multithreader.gui.MandelDisplay;
import miniproject.mandelbrot_multithreader.logic.FractalRenderer;

public class Main {
    public static void main(String[] args){
        MandelDisplay display = new MandelDisplay(800, 600);
        FractalRenderer renderer = new FractalRenderer(display);
        display.setRenderer(renderer);

        Thread thread = new Thread(renderer::run);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }
}
