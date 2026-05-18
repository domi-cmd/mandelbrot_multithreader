package miniproject.mandelbrot_multithreader.gui;

import java.awt.*;
import javax.swing.*;

public class MandelDisplay extends JPanel {
    static JFrame frame;
    static JButton b, b1;
    static JLabel label;
    static JPanel display;

    private Image image; // offscreen image for double buffering


    public MandelDisplay(int width, int height){
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
