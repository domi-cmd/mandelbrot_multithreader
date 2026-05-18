package miniproject.mandelbrot_multithreader.gui;

import java.awt.*;
import javax.swing.*;

import miniproject.mandelbrot_multithreader.logic.FractalRenderer;

import java.awt.event.*; 

public class MandelDisplay extends JPanel {
    static JFrame frame;
    static JButton b, b1;
    static JLabel label;
    static JPanel display;

    private Point dragStart = null;
    private Point dragEnd = null;

    private FractalRenderer renderer;

    private Image image; // offscreen image for double buffering


    public MandelDisplay(int width, int height){
        frame = new JFrame("Mandelbrot Displayer");

        setPreferredSize(new Dimension(width, height));

        frame.add(this);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Logic for allowing zoom via mouse
        addZoomListeners();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            g.drawImage(image, 0, 0, null);
        }

        // draw rubber-band rectangle on top
        if (dragStart != null && dragEnd != null) {
            int x = Math.min(dragStart.x, dragEnd.x);
            int y = Math.min(dragStart.y, dragEnd.y);
            int w = Math.abs(dragEnd.x - dragStart.x);
            int h = Math.abs(dragEnd.y - dragStart.y);

            g.setColor(new Color(255, 255, 255, 60));  // translucent fill
            g.fillRect(x, y, w, h);

            g.setColor(Color.WHITE);  // border
            g.drawRect(x, y, w, h);
        }
    }

    public void setRenderer(FractalRenderer renderer){
        this.renderer = renderer;
    }

    public void setImage(Image image) {
        this.image = image;
    }



    private void addZoomListeners(){
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragStart = e.getPoint();
                dragEnd = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (dragStart != null) {
                    renderer.zoomToRect(dragStart, e.getPoint());
                    dragStart = null;
                    dragEnd = null;
                    repaint();
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                dragEnd = e.getPoint();
                repaint(); // redraws fractal + rectangle overlay
            }
        });
    }
}
