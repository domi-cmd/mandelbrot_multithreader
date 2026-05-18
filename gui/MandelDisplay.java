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

    private JLabel timeLabel;
    private JLabel iterLabel;

    private Point dragStart = null;
    private Point dragEnd = null;

    private FractalRenderer renderer;

    private java.awt.image.BufferedImage image;


    public MandelDisplay(int width, int height){
        frame = new JFrame("Mandelbrot Displayer");

        setPreferredSize(new Dimension(width, height));

        JPanel inputPanel = setupInputPanel();

        // 4. Mount everything cleanly to the Frame using explicit regions
        frame.add(this, BorderLayout.CENTER);       // Fractal takes up the remaining core window
        frame.add(inputPanel, BorderLayout.EAST);   // Button panel stays strictly on the right side

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Center the frame
        frame.setLocationRelativeTo(null);
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

    public void setImage(java.awt.image.BufferedImage image) {
        this.image = image;
    }

    // thread-safe helper method so the Renderer can push updates to the UI
    public void updateRenderTime(long milliseconds) {
        // Swing components must be updated on the Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            timeLabel.setText("Render time: " + milliseconds + " ms");
        });
    }

    // Public helper to update the iteration text dynamically from the renderer
    public void updateIterationDisplay(int currentIterations) {
        SwingUtilities.invokeLater(() -> {
            iterLabel.setText("Iterations: " + currentIterations);
        });
    }


    private JPanel setupInputPanel(){
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        inputPanel.setBackground(Color.DARK_GRAY);

        // --- Reset Button ---
        JButton resetBtn = new JButton("Reset Zoom");
        resetBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // --- Change Palette Button ---
        JButton paletteBtn = new JButton("Change Palette");
        paletteBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        paletteBtn.addActionListener(e -> {
            if (renderer != null) {
                renderer.cycleColorPalette();
            }
        });
        
        // --- Thread Selection ---
        JLabel threadLabel = new JLabel("Threads to use:");
        threadLabel.setForeground(Color.WHITE);
        threadLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        Integer[] threadOptions = { 1, 2, 4, 8, 12, 16 };
        JComboBox<Integer> threadBox = new JComboBox<>(threadOptions);
        threadBox.setSelectedItem(4); 
        threadBox.setMaximumSize(new Dimension(150, 30));
        threadBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        threadBox.addActionListener(e -> {
            if (renderer != null) {
                renderer.setThreadCount((Integer) threadBox.getSelectedItem());
            }
        });

        // --- Iteration Control Controls ---
        JLabel iterTitleLabel = new JLabel("Detail Level:");
        iterTitleLabel.setForeground(Color.WHITE);
        iterTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Displays current iteration depth status
        iterLabel = new JLabel("Iterations: 20");
        iterLabel.setForeground(Color.YELLOW); // Distinct color
        iterLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Panel to hold plus and minus buttons side-by-side
        JPanel iterButtonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        iterButtonPanel.setMaximumSize(new Dimension(150, 30));
        iterButtonPanel.setOpaque(false); // Translucent container background

        JButton minusBtn = new JButton("-");
        JButton plusBtn = new JButton("+");

        // Connect the button click directly to the renderer's new reset method
        resetBtn.addActionListener(e -> {
            if (renderer != null) {
                renderer.resetView();
            }
        });

        minusBtn.addActionListener(e -> {
            if (renderer != null) {
                renderer.changeIterations(-50); // Drop iteration details
            }
        });

        plusBtn.addActionListener(e -> {
            if (renderer != null) {
                renderer.changeIterations(50); // Push iteration details higher
            }
        });

        iterButtonPanel.add(minusBtn);
        iterButtonPanel.add(plusBtn);

        // --- Performance Timing Label ---
        timeLabel = new JLabel("Render time: 0 ms");
        timeLabel.setForeground(Color.WHITE);
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Stack everything cleanly down the right-hand panel
        inputPanel.add(resetBtn);
        inputPanel.add(Box.createVerticalStrut(10)); // Gap between window control buttons
        inputPanel.add(paletteBtn);               // Add the palette button here!
        inputPanel.add(Box.createVerticalStrut(20));
        inputPanel.add(threadLabel);
        inputPanel.add(Box.createVerticalStrut(5));
        inputPanel.add(threadBox);
        inputPanel.add(Box.createVerticalStrut(20));
        inputPanel.add(iterTitleLabel);
        inputPanel.add(Box.createVerticalStrut(2));
        inputPanel.add(iterLabel);
        inputPanel.add(Box.createVerticalStrut(5));
        inputPanel.add(iterButtonPanel); // Add the button row container
        inputPanel.add(Box.createVerticalStrut(25));
        inputPanel.add(timeLabel);


        return inputPanel;
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
