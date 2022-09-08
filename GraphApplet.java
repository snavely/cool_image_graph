import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;

public class GraphApplet extends JApplet {
    // Called when the applet is initialized.
    public void init() {
        Graph graph = new Graph(getParameter("graphURL"), getParameter("listURL"), getParameter("imageURL") + "/", true);
        JPanel graphViewer = new GraphViewer(graph);
        add(graphViewer, BorderLayout.CENTER);
    }
    
    // Called when the applet is started.
    public void start() {
    }
    
    //
    public void stop() {
    }
    
    //
    public void destroy() {
    }
}