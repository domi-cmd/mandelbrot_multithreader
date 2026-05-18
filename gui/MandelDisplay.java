package miniproject.mandelbrot_multithreader.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MandelDisplay extends JPanel {
    static JFrame frame;
    static JButton b, b1;
    static JLabel label;
    static JPanel display;

    private Image image; // offscreen image for double buffering
    
    // Display size
    static int width = 800;
    static int height = 600;

    // currently visible relative window dimensions
    private double viewX = 0.0;
    private double viewY = 0.0;
    private double zoom = 1.0;


    public MandelDisplay(){
        frame = new JFrame("Mandelbrot Displayer");

        setPreferredSize(new Dimension(width, height));

        frame.add(this);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            g.drawImage(image, 0, 0, null);
        }
    }

    public void setImage(Image image) {
        this.image = image;
    }
}
