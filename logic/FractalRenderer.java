package miniproject.mandelbrot_multithreader.logic;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import miniproject.mandelbrot_multithreader.gui.MandelDisplay;
import miniproject.mandelbrot_multithreader.util.ColorPalette;

public class FractalRenderer {
    private ColorPalette colorPalette;
    private MandelDisplay mandelDisplay;

    static JFrame frame;
    static JButton b, b1;
    static JLabel label;
    static JPanel display;

    private Image image; // offscreen image for double buffering
    private Graphics graphics; // offscreen graphics for the offscreen image
    
    // Display size
    private int width;
    private int height;

    // currently visible relative window dimensions
    private double viewX = 0.0;
    private double viewY = 0.0;
    private double zoom = 1.0;

    private static final int[][] rows = {
        {0, 16, 8}, {8, 16, 8}, {4, 16, 4}, {12, 16, 4},
        {2, 16, 2}, {10, 16, 2}, {6, 16, 2}, {14, 16, 2},
        {1, 16, 1}, {9, 16, 1}, {5, 16, 1}, {13, 16, 1},
        {3, 16, 1}, {11, 16, 1}, {7, 16, 1}, {15, 16, 1},
    };

    static int maxIterations = 20;

    public FractalRenderer(MandelDisplay mandelDisplay){
        this.mandelDisplay = mandelDisplay;
        this.colorPalette = new ColorPalette();
    }


    public void run() {
        //while (thread != null) {
            while (draw());
            synchronized (this) {
                try {
                    wait();
                }
                catch (InterruptedException e) {}
            }
        //}
    }

    private boolean draw() {
        
        Dimension size = mandelDisplay.getSize();
        // create offscreen buffer for double buffering
        if (image == null || size.width != width || size.height != height) {
            width = size.width;
            height = size.height;
            this.image = mandelDisplay.createImage(width, height);
            mandelDisplay.setImage(this.image);
            graphics = this.image.getGraphics();
        }

        /**
        // fractal image pre-drawing
        for (int y = 0; y < height + 4; y += 8) {
            if (Thread.interrupted())
            return true;
            for (int x = 0; x < width + 4; x += 8) {
            double r = zoom / Math.min(width, height);
            double dx = 2.5 * (x * r + viewX) - 2;
            double dy = 1.25 - 2.5 * (y * r + viewY);
            Color color = color(dx, dy);
            graphics.setColor(color);
            graphics.fillRect(x - 4, y - 4, 8, 8);
            }
        }
        repaint();
        **/

        // fractal image drawing
        for (int row = 0; row < rows.length; row++) {
            for (int y = rows[row][0]; y < height; y += rows[row][1]) {
            if (Thread.interrupted())
                return true;
            for (int x = 0; x < width; x++) {
                double r = zoom / Math.min(width, height);
                double dx = 2.5 * (x * r + viewX) - 2;
                double dy = 1.25 - 2.5 * (y * r + viewY);

                // Calculate and render the color of the pixel
                Color color = renderColor(dx, dy);
                
                /**
                // computation of average color for antialiasing
                if (antialias) {
                Color c1 = color(dx - 0.25 * r, dy - 0.25 * r);
                Color c2 = color(dx + 0.25 * r, dy - 0.25 * r);
                Color c3 = color(dx + 0.25 * r, dy + 0.25 * r);
                Color c4 = color(dx - 0.25 * r, dy + 0.25 * r);
                int red = (color.getRed() + c1.getRed() + c2.getRed() + c3.getRed() + c4.getRed()) / 5;
                int green = (color.getGreen() + c1.getGreen() + c2.getGreen() + c3.getGreen() + c4.getGreen()) / 5;
                int blue = (color.getBlue() + c1.getBlue() + c2.getBlue() + c3.getBlue() + c4.getBlue()) / 5;
                color = new Color(red, green, blue);
                } **/
                graphics.setColor(color);
                graphics.fillRect(x, y - rows[row][2] / 2, 1, rows[row][2]);
            }
            }
            mandelDisplay.repaint();
        }
        return false;
    }


    // Computes a color for a given point
    private Color renderColor(double x, double y) {
        int count = MandelMath.calculateFractalPixel(0.0, 0.0, x, y, maxIterations);
     
        return colorPalette.getColor(count);
        /**
        if (smooth) {
            Color color2 = colors[currentPalette][(count / 256 + palSize - 1) % palSize];
            int k1 = count % 256;
            int k2 = 255 - k1;
            int red = (k1 * color.getRed() + k2 * color2.getRed()) / 255;
            int green = (k1 * color.getGreen() + k2 * color2.getGreen()) / 255;
            int blue = (k1 * color.getBlue() + k2 * color2.getBlue()) / 255;
            color = new Color(red, green, blue);
        } **/
    }
}
