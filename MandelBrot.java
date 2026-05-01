package miniproject.mandelbrot_multithreader;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MandelBrot extends JPanel {
    static JFrame frame;
    static JButton b, b1;
    static JLabel label;
    static JPanel display;

    private Image image; // offscreen image for double buffering
    private Graphics graphics; // offscreen graphics for the offscreen image
    
    // Display size
    static int width;
    static int height;

    // currently visible relative window dimensions
    private double viewX = 0.0;
    private double viewY = 0.0;
    private double zoom = 1.0;

    private Color[][] colors; // palettes
    private int currentPalette = 0; // current palette
    private static final int[][][] colpal = { // palette colors
        { {12, 0, 10, 20}, {12, 50, 100, 240}, {12, 20, 3, 26}, {12, 230, 60, 20},
            {12, 25, 10, 9}, {12, 230, 170, 0}, {12, 20, 40, 10}, {12, 0, 100, 0},
            {12, 5, 10, 10}, {12, 210, 70, 30}, {12, 90, 0, 50}, {12, 180, 90, 120},
            {12, 0, 20, 40}, {12, 30, 70, 200} },
        { {10, 70, 0, 20}, {10, 100, 0, 100}, {14, 255, 0, 0}, {10, 255, 200, 0} },
        { {8, 40, 70, 10}, {9, 40, 170, 10}, {6, 100, 255, 70}, {8, 255, 255, 255} },
        { {12, 0, 0, 64}, {12, 0, 0, 255}, {10, 0, 255, 255}, {12, 128, 255, 255}, {14, 64, 128, 255} },
        { {16, 0, 0, 0}, {32, 255, 255, 255} },
    };

    private static final int[][] rows = {
        {0, 16, 8}, {8, 16, 8}, {4, 16, 4}, {12, 16, 4},
        {2, 16, 2}, {10, 16, 2}, {6, 16, 2}, {14, 16, 2},
        {1, 16, 1}, {9, 16, 1}, {5, 16, 1}, {13, 16, 1},
        {3, 16, 1}, {11, 16, 1}, {7, 16, 1}, {15, 16, 1},
    };

    static int maxIterations = 20;
    
    public static void main(String[] args){
        frame = new JFrame("Mandelbrot");

        /**
        display = new JPanel();
        display.setBackground(Color.black);


        frame.add(display);
        frame.setSize(300, 300);
        frame.setVisible(true); **/

        MandelBrot mandelBrot = new MandelBrot();
        mandelBrot.setPreferredSize(new Dimension(800, 600));

        frame.add(mandelBrot);
        frame.pack();
        frame.setVisible(true);
        
        mandelBrot.init();
        //mandelBrot.run();
        new Thread(mandelBrot::run).start();
    }

    /**
    public void start() {
        redraw();
    }

    public void destroy() {
        Thread t = thread;
        thread = null;
        t.interrupt(); 
    }

    private void redraw() {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
        else {
            thread = new Thread(this);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }
    } **/

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

    public void init() {
        /**
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        **/
        // initialize color palettes
        colors = new Color[colpal.length][];
        for (int p = 0; p < colpal.length; p++) { // process all palettes
            int n = 0;
            for (int i = 0; i < colpal[p].length; i++) // get the number of all colors
            n += colpal[p][i][0];
            colors[p] = new Color[n]; // allocate pallete
            n = 0;
            for (int i = 0; i < colpal[p].length; i++) { // interpolate all colors
            int[] c1 = colpal[p][i]; // first referential color
            int[] c2 = colpal[p][(i + 1) % colpal[p].length]; // second ref. color
            for (int j = 0; j < c1[0]; j++) // linear interpolation of RGB values
                colors[p][n + j] = new Color(
                    (c1[1] * (c1[0] - 1 - j) + c2[1] * j) / (c1[0] - 1),
                    (c1[2] * (c1[0] - 1 - j) + c2[2] * j) / (c1[0] - 1),
                    (c1[3] * (c1[0] - 1 - j) + c2[3] * j) / (c1[0] - 1));
            n += c1[0];
            }
        }
        //thread = null;
    }

    private boolean draw() {
        
        Dimension size = getSize();
        // create offscreen buffer for double buffering
        if (image == null || size.width != width || size.height != height) {
            width = size.width;
            height = size.height;
            this.image = createImage(width, height);
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
                Color color = color(dx, dy);
                
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
            repaint();
        }
        return false;
    }


    // Computes a color for a given point
    private Color color(double x, double y) {
        int count = mandel(0.0, 0.0, x, y);
        int palSize = colors[currentPalette].length;
        Color color = colors[currentPalette][count / 256 % palSize];
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
        return color;
    }



    // Computes a value for a given complex number
    private int mandel(double zRe, double zIm, double pRe, double pIm) {
        double zRe2 = zRe * zRe;
        double zIm2 = zIm * zIm;
        double zM2 = 0.0;
        int count = 0;
        while (zRe2 + zIm2 < 4.0 && count < maxIterations) {
            zM2 = zRe2 + zIm2;
            zIm = 2.0 * zRe * zIm + pIm;
            zRe = zRe2 - zIm2 + pRe;
            zRe2 = zRe * zRe;
            zIm2 = zIm * zIm;
            count++;
        }
        if (count == 0 || count == maxIterations)
            return 0;
        // transition smoothing
        zM2 += 0.000000001;
        return 256 * count + (int)(255.0 * Math.log(4 / zM2) / Math.log((zRe2 + zIm2) / zM2));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            g.drawImage(image, 0, 0, null);
        }
    }
}
