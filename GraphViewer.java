import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;

public class GraphViewer extends JPanel {
    private Graph graph;
    private double startX, startY;
    private double endX, endY;
    private GraphPanel graphPanel;
    private ImagePanel imagePanel;
    
    private static final int LEFT_PANEL_WIDTH = 400;
    
    // Constructor.
    public GraphViewer(Graph graph) {
        this.graph = graph;
        double[] range = graph.getRange();
        startX = range[0];
        startY = range[1];
        endX = range[2];
        endY = range[3];
        
        setLayout(new BorderLayout());
        
        graphPanel = new GraphPanel();
        graphPanel.setPreferredSize(new Dimension(900, 800));
        add(graphPanel, BorderLayout.CENTER);
        
        imagePanel = new ImagePanel();
        imagePanel.setPreferredSize(new Dimension(LEFT_PANEL_WIDTH, 0));
        add(imagePanel, BorderLayout.WEST);
        
        MouseController mouseController = new MouseController();
        graphPanel.addMouseListener(mouseController);
        graphPanel.addMouseMotionListener(mouseController);
    }
    
    // Graph Panel.
    private class GraphPanel extends JPanel {
        // Repaint.
        public void paintComponent(Graphics g) {
            super.paintComponent(g);            
            graph.draw(g, startX, startY, endX, endY, getWidth(), getHeight());
        }
    }
    
    // Image Panel.
    private class ImagePanel extends JPanel {
        // Repaint.
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.black);
            g.fillRect(0, 0, getWidth(), getHeight());
            BufferedImage image = graph.getSelectedImage();
            String basename = graph.getSelectedBasename();
            if(image != null) {
                int x = 0;
                int y = 0;
                int w = getWidth();
                int h = getHeight();
                
                if(getWidth() >= (getHeight() * image.getWidth()) / image.getHeight()) {
                    w = (getHeight() * image.getWidth()) / image.getHeight();
                    x = (getWidth() - w) / 2;
                } else {
                    h = (getWidth() * image.getHeight()) / image.getWidth();
                    y = (getHeight() - h) / 2;
                }
                g.drawImage(image, x, y, w, h, Color.black, null);
                
                g.setColor(Color.white);
                FontMetrics fm = g.getFontMetrics();
                int startX = (LEFT_PANEL_WIDTH - fm.stringWidth(basename)) / 2;
                g.drawString(basename, startX, y + h + 15);
            }
        }
    }
    
    // Mouse Controller.
    private class MouseController implements MouseListener, MouseMotionListener {
        private boolean dragging;
        private int downX;
        private int downY;
        
        public void mouseMoved(MouseEvent e) {
            graph.setSelected(e.getX(), e.getY(), startX, startY, endX, endY, graphPanel.getWidth(), graphPanel.getHeight());
            graphPanel.repaint();
            imagePanel.repaint();
        }
        
        public void mouseDragged(MouseEvent e) {
            if(e.getButton() == MouseEvent.BUTTON1) {
                System.out.println("drag");
                graph.updateDragBox(downX, downY, e.getX(), e.getY());
                graphPanel.repaint();
            }
        }
        
        public void mousePressed(MouseEvent e) {
            if(e.getButton() == MouseEvent.BUTTON1) {
                downX = e.getX();
                downY = e.getY();
            }
        }
        
        public void mouseReleased(MouseEvent e) {
            if(e.getButton() == MouseEvent.BUTTON1) {
                int startMouseX = e.getX() < downX ? e.getX(): downX;
                int startMouseY = e.getY() < downY ? e.getY(): downY;
                int endMouseX = e.getX() < downX ? downX: e.getX();
                int endMouseY = e.getY() < downY ? downY: e.getY();
                double rangeX = endX - startX;
                double rangeY = endY - startY;
                startX += (startMouseX * rangeX) / graphPanel.getWidth();
                endX -= ((graphPanel.getWidth() - endMouseX) * rangeX) / graphPanel.getWidth();
                startY += (startMouseY * rangeY) / graphPanel.getHeight();
                endY -= ((graphPanel.getHeight() - endMouseY) * rangeY) / graphPanel.getHeight();
                graph.updateDragBox(-1, -1, -1, -1);
                graphPanel.repaint();
                imagePanel.repaint();
            }
        }
        
        public void mouseClicked(MouseEvent e) {
            if(e.getButton() == MouseEvent.BUTTON3) {
                double[] range = graph.getRange();
                startX = range[0];
                startY = range[1];
                endX = range[2];
                endY = range[3];
                graphPanel.repaint();
                imagePanel.repaint();
            }
        }
        
        public void mouseEntered(MouseEvent e) {
        }
        
        public void mouseExited(MouseEvent e) {
        }
    }
    
    // Main.
    public static void main(String args[]) {
        if(args.length != 3) {
            System.out.println("usage: java -jar GraphViewer.jar graph list images_dir");
            System.out.println("graph - input file describing graph.");
            System.out.println("list  - input file listing image filenames.");
            System.out.println("images_dir - directory with images.");
            System.exit(1);
        }
        
        Graph graph = new Graph(args[0], args[1], args[2] + System.getProperty("file.separator"), false);
        
        JPanel graphViewer = new GraphViewer(graph);
        JFrame f = new JFrame();
        f.add(graphViewer, BorderLayout.CENTER);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);
        
    }
}