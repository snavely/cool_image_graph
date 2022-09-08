import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import javax.imageio.*;
import java.net.*;

public class Graph {
    private ArrayList<Node> graph;
    private int selectedNode = -1;
    private String imagesDirectory;
    private boolean isURL;
    private double minWeight = Double.MAX_VALUE;
    private double maxWeight = 0;
    private int dragX1 = -1, dragY1 = -1, dragX2 = -1, dragY2 = -1;
    private Hashtable indexHash;                
    private Vector imageNames;

    private static final int NODE_SIZE = 15;
    
    // Constructor.
    public Graph(String graphFile, String listFile, String imagesDirectory, boolean isURL) {
        this.imagesDirectory = imagesDirectory;
        this.isURL = isURL;
        graph = new ArrayList<Node>();
        indexHash = new Hashtable();
        imageNames = new Vector();

        try {
            BufferedReader graphReader;
            BufferedReader listReader;
            if(isURL) {
                graphReader = new BufferedReader(new InputStreamReader(new URL(graphFile).openConnection().getInputStream()));
                listReader = new BufferedReader(new InputStreamReader(new URL(listFile).openConnection().getInputStream()));
            } else {
                graphReader = new BufferedReader(new FileReader(graphFile));
                listReader = new BufferedReader(new FileReader(listFile));
            }
            String nextGraphLine;
            String nextListLine;
            int count = 0;

            // Read image name from list file.
            while ((nextListLine = listReader.readLine()) != null) {
               imageNames.addElement(nextListLine);
            }

            while((nextGraphLine = graphReader.readLine()) != null) {
                // Read nodes.
                if(nextGraphLine.contains("image") && 
                   (nextGraphLine.contains("label=") || nextGraphLine.contains("width="))) {
                    Node n = new Node(nextGraphLine);
                    n.setName((String) imageNames.get(n.imageIndex));
                    graph.add(n);
                    indexHash.put(n.imageIndex, new Integer(count));
                    count++;
                }
                
                // Read edges.
                if(nextGraphLine.contains("--")) {
                    int node1 = Integer.parseInt(nextGraphLine.substring(nextGraphLine.indexOf("image") + 5, nextGraphLine.indexOf(" --")));
                    int node2 = Integer.parseInt(nextGraphLine.substring(nextGraphLine.indexOf("-- image") + 8, nextGraphLine.indexOf(" [")));
                    
                    double weight;

                    try {
                       weight = Double.parseDouble(nextGraphLine.substring(nextGraphLine.indexOf("weight=\"") + 8, nextGraphLine.indexOf("\", pos=")));                       
                    } catch (Exception e) {
                       weight = 0.0;
                    }

                    int idx1 = (Integer) indexHash.get(node1);
                    int idx2 = (Integer) indexHash.get(node2);

                    //try {
                    //} catch (Exception e) {
                    //   System.out.println("Error getting index of " + node1);
                    //}

                    graph.get(idx1).addNeighbor(idx2, weight);
                    graph.get(idx2).addNeighbor(idx1, weight);

                    if(weight < minWeight) minWeight = weight;
                    if(weight > maxWeight) maxWeight = weight;
                }
            }
            
            graphReader.close();
            
            System.out.println("Total Nodes: " + graph.size());
            for(int i = 0; i < graph.size(); i++) {
                if(graph.get(i).neighbors.size() == 0) System.out.println("Node " + i + " has no neighbors.");
                if(graph.get(i).neighbors.size() == 1 && graph.get(graph.get(i).neighbors.get(0).intValue()).neighbors.size() == 1)
                    System.out.println("Node " + i + " and " + graph.get(i).neighbors.get(0).intValue() +  " have only each other as neighbors.");
            }
         } catch(Exception e) {
             System.out.println("Error reading input.");
             e.printStackTrace();
         }
    }
    
    // Returns the coordinates of the bounding box that will fit all the nodes in the graph.
    public double[] getRange() {
        double[] range = {Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};
        for(int i = 0; i < graph.size(); i++) {
            if(graph.get(i).x < range[0]) range[0] = graph.get(i).x;
            if(graph.get(i).y < range[1]) range[1] = graph.get(i).y;
            if(graph.get(i).x > range[2]) range[2] = graph.get(i).x;
            if(graph.get(i).y > range[3]) range[3] = graph.get(i).y;
        }
        return range;
    }
    
    // Sets the selected node from the given mouse coordinates.
    public void setSelected(int mouseX, int mouseY, double startX, double startY, double endX, double endY, int panelWidth, int panelHeight) {
        double multiplierX = panelWidth / (endX - startX);
        double multiplierY =  panelHeight / (endY - startY);
        
        double closestDistance = Double.MAX_VALUE;
        int closestNode = -1;
        for(int i = 0; i < graph.size(); i++) {
            if(!(graph.get(i).neighbors.size() == 0 ||
                 (graph.get(i).neighbors.size() == 1 && graph.get(graph.get(i).neighbors.get(0).intValue()).neighbors.size() == 1))) {
                int drawX = (int)((graph.get(i).x - startX) * multiplierX);
                int drawY = (int)((graph.get(i).y - startY) * multiplierY);
                
                double distance = Math.sqrt((drawX - mouseX) * (drawX - mouseX) + (drawY - mouseY) * (drawY - mouseY));
                if(distance < closestDistance) {
                    closestDistance = distance;
                    closestNode = i;
                }
            }
        }
        
        if(closestDistance < NODE_SIZE) selectedNode = closestNode;
        else selectedNode = -1;
    }
    
    // Returns the BufferedImage of the image for the selected node.
    public BufferedImage getSelectedImage() {
        if(selectedNode < 0) return null;
        try {
            if(isURL) {
                System.out.println("image url: " + imagesDirectory + graph.get(selectedNode).imageName);
                return ImageIO.read(new URL(imagesDirectory + graph.get(selectedNode).imageName));
            } else {
                return ImageIO.read(new File(imagesDirectory + graph.get(selectedNode).imageName));
            }
        } catch(Exception e) {
           System.out.println("Error while getting image " + selectedNode + "(" + graph.get(selectedNode).imageName + ")");
        }
        return null;
    }
    
    // Returns the basename of the image for the selected node.
    public String getSelectedBasename() {
        if(selectedNode < 0) return "";
        return graph.get(selectedNode).imageName;
    }
    
    // Updates the coordinates of the dragging box.
    public void updateDragBox(int x1, int y1, int x2, int y2) {
        dragX1 = x1;
        dragY1 = y1;
        dragX2 = x2;
        dragY2 = y2;
    }
    
    // Draws the graph.
    public void draw(Graphics g, double startX, double startY, double endX, double endY, int panelWidth, int panelHeight) {
        double multiplierX = panelWidth / (endX - startX);
        double multiplierY =  panelHeight / (endY - startY);
        double normalizer = Math.log(Math.pow(maxWeight - minWeight, 1 / 255.0));
        
        // Draw edges.
        for(int i = 0; i < graph.size(); i++) {
            int drawX = (int)((graph.get(i).x - startX) * multiplierX);
            int drawY = (int)((graph.get(i).y - startY) * multiplierY);
            
            for(int j = 0; j < graph.get(i).neighbors.size(); j++) {
                if(i < graph.get(i).neighbors.get(j).intValue() &&
                   !(graph.get(i).neighbors.size() == 1 && graph.get(graph.get(i).neighbors.get(0).intValue()).neighbors.size() == 1)) {
                    Node neighbor = graph.get(graph.get(i).neighbors.get(j).intValue());
                    int neighborX = (int)((neighbor.x - startX) * multiplierX);
                    int neighborY = (int)((neighbor.y - startY) * multiplierY);
                    
                    int colorValue = (int)(Math.log(graph.get(i).weights.get(j).intValue() - minWeight + 1) / normalizer);
                    g.setColor(new Color(0, 0, colorValue));
                    g.drawLine(drawX, drawY, neighborX, neighborY);
                }
            }
        }
        
        // Draw selected edges.
        if(selectedNode >= 0) {
            int drawX = (int)((graph.get(selectedNode).x - startX) * multiplierX);
            int drawY = (int)((graph.get(selectedNode).y - startY) * multiplierY);
            
            for(int j = 0; j < graph.get(selectedNode).neighbors.size(); j++) {
                Node neighbor = graph.get(graph.get(selectedNode).neighbors.get(j).intValue());
                int neighborX = (int)((neighbor.x - startX) * multiplierX);
                int neighborY = (int)((neighbor.y - startY) * multiplierY);
                
                int colorValue = (int)(Math.log(graph.get(selectedNode).weights.get(j).intValue() - minWeight + 1) / normalizer);
                g.setColor(new Color(0, colorValue, 0));
                g.drawLine(drawX, drawY, neighborX, neighborY);
            }
        }
        
        // Draw nodes.
        for(int i = 0; i < graph.size(); i++) {
            if(!(graph.get(i).neighbors.size() == 0 ||
                 (graph.get(i).neighbors.size() == 1 && graph.get(graph.get(i).neighbors.get(0).intValue()).neighbors.size() == 1))) {
                int drawX = (int)((graph.get(i).x - startX) * multiplierX);
                int drawY = (int)((graph.get(i).y - startY) * multiplierY);
                
                // int NODE_SIZE = graph.get(i).neighbors.size() / 10 + 6;
                int NODE_SIZE = (int) (Math.log(graph.get(i).neighbors.size()) + 6.0);
                g.setColor(Color.red);
                g.fillOval(drawX - NODE_SIZE / 2, drawY - NODE_SIZE / 2, NODE_SIZE, NODE_SIZE);
                g.setColor(Color.black);
                g.drawOval(drawX - NODE_SIZE / 2, drawY - NODE_SIZE / 2, NODE_SIZE - 1, NODE_SIZE - 1);
            }
        }
        
        if(selectedNode >= 0) {
            // Draw selected node neighbors.
            for(int j = 0; j < graph.get(selectedNode).neighbors.size(); j++) {
                Node neighbor = graph.get(graph.get(selectedNode).neighbors.get(j).intValue());
                int neighborX = (int)((neighbor.x - startX) * multiplierX);
                int neighborY = (int)((neighbor.y - startY) * multiplierY);
                
                int NODE_SIZE = (int) (Math.log(neighbor.neighbors.size()) + 6.0);
                // int NODE_SIZE = neighbor.neighbors.size() / 10 + 6;
                int colorValue = (int)(Math.log(graph.get(selectedNode).weights.get(j).intValue() - minWeight + 1) / normalizer);
                g.setColor(new Color(0, colorValue, 0));
                g.fillOval(neighborX - NODE_SIZE / 2, neighborY - NODE_SIZE / 2, NODE_SIZE, NODE_SIZE);
                g.setColor(Color.orange);
                g.drawOval(neighborX - NODE_SIZE / 2, neighborY - NODE_SIZE / 2, NODE_SIZE - 1, NODE_SIZE - 1);
            }
            
            // Draw selected node.
            int drawX = (int)((graph.get(selectedNode).x - startX) * multiplierX);
            int drawY = (int)((graph.get(selectedNode).y - startY) * multiplierY);
            
            int NODE_SIZE = (int) (Math.log(graph.get(selectedNode).neighbors.size()) + 6.0);
            // int NODE_SIZE = graph.get(selectedNode).neighbors.size() / 10 + 6;
            g.setColor(Color.magenta);
            g.fillOval(drawX - NODE_SIZE / 2, drawY - NODE_SIZE / 2, NODE_SIZE, NODE_SIZE);
            g.setColor(Color.black);
            g.drawOval(drawX - NODE_SIZE / 2, drawY - NODE_SIZE / 2, NODE_SIZE - 1, NODE_SIZE - 1);
            
            // Draw selected node image
            /*
            String filename = imagesDirectory + graph.get(selectedNode).imageName;
            try {
                BufferedImage image = ImageIO.read(new File(filename));
                g.drawImage(image, drawX, drawY, 400, 400, Color.black, null);
            } catch(Exception e) {
                System.out.println("Error while drawing image: " + filename);
            }
            */
        }
        
        // Draw drag box.
        if(dragX1 != -1) {
            g.setColor(Color.black);
            g.drawRect(dragX1, dragY1, dragX2, dragY2);
        }
    }
    
    // This class represents a node in the graph.
    private class Node {
        public ArrayList<Integer> neighbors;
        public ArrayList<Double> weights;
        public int x;
        public int y;
        public String imageName;
        public int imageIndex;

        public Node(String nodeLine) {
            int startIdx = nodeLine.indexOf("pos=\"") + 5;
            int endIdx = startIdx + nodeLine.substring(startIdx).indexOf("\"");
            String pos = nodeLine.substring(startIdx, endIdx);
            // System.out.println(pos);
            // x = Integer.parseInt(pos.substring(0, pos.indexOf(",")));
            // y = Integer.parseInt(pos.substring(pos.indexOf(",") + 1, pos.length()));
            x = Math.round(Float.parseFloat(pos.substring(0, pos.indexOf(","))));
            y = Math.round(Float.parseFloat(pos.substring(pos.indexOf(",") + 1, pos.length())));
            neighbors = new ArrayList<Integer>();
            weights = new ArrayList<Double>();
            imageIndex = Integer.parseInt(nodeLine.substring(nodeLine.indexOf("i")+5, nodeLine.indexOf("[")-1));
        }

        public Node(String nodeLine, String nameLine) {
            String pos = nodeLine.substring(nodeLine.indexOf("pos=\"") + 5, nodeLine.indexOf("\", width="));
            x = Integer.parseInt(pos.substring(0, pos.indexOf(",")));
            y = Integer.parseInt(pos.substring(pos.indexOf(",") + 1, pos.length()));
            neighbors = new ArrayList<Integer>();
            weights = new ArrayList<Double>();

            // System.out.println(nodeLine);
            imageName = nameLine.substring(0, nameLine.lastIndexOf(".")) + ".jpg";
            imageIndex = Integer.parseInt(nodeLine.substring(nodeLine.indexOf("i")+5, nodeLine.indexOf("[")-1));
        }
        
        public void setName(String nameLine) {
           if (nameLine.contains(".pgm")) {
              imageName = nameLine.substring(0, nameLine.lastIndexOf(".pgm")) + ".jpg";
           } else {
              imageName = nameLine.substring(0, nameLine.lastIndexOf(".jpg")) + ".jpg";
           }
        }

        public void addNeighbor(int neighbor, double weight) {
            neighbors.add(new Integer(neighbor));
            weights.add(new Double(weight));
        }
    }
}