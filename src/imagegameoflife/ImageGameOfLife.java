/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imagegameoflife;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.util.ArrayList;
import static java.lang.Math.min;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

/**
 *
 * @author Fernando
 */
public class ImageGameOfLife extends JPanel {

    enum State {
        start, running, over
    }
    
    enum Mode {
        normal, degraded
    }
    
    //private static final int FRAME_RATE = 60;
    private static final Color GRID_COLOR = new Color(0xBBADA0);
    //private static final Color EMPTY_COLOR = new Color(0xCDC1B4);
    private static final int CROSS_RATIO = 80; // 80% probabilidad de cruce
    private static final double MAX_DISTANCE = Math.floor(Math.sqrt(Math.pow(255,2) + Math.pow(255,2) + Math.pow(255,2)));
    private static final int MAX_SIDE = 1000;
    private static final int MIN_SIDE = 4;
    
    private final Color startColor;
    private Color objective;
    private BufferedImage image; 
    
    private Timer timer;
    private State gamestate;
    private Mode mode;
    private int generation;
    private double fitness;
    private int side;
    private Tile[][] tiles;
    private final Random rand = new Random();
    
    public ImageGameOfLife() {
        startColor = new Color(0xFFEBCD);
        objective = new Color(255,0,0);
        gamestate = State.start;
        mode = Mode.normal;
        image = null;
        
        side = 400;
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (gamestate != State.running &&
                   (e.getX() >= (int)(getWidth()/4.5) &&
                    e.getX() <= (int)(getWidth()/4.5) + (int)(getWidth()/1.8) &&
                    e.getY() >= (int)(getHeight()/7.0) &&
                    e.getY() <= (int)(getHeight()/7.0) + (int)(getHeight()/1.4))) {
                    startGame();
                }
            }
        });
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (gamestate != State.running) {
                    if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
                        side = Math.min(side+1,MAX_SIDE);
                        repaint();
                    } else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) {
                        side = Math.max(side-1,MIN_SIDE);
                        repaint();
                    } else if (e.getKeyCode() == KeyEvent.VK_M) {
                        if (mode == Mode.normal) {
                            mode = Mode.degraded;
                        } else {
                            mode = Mode.normal;
                        }
                        repaint();
                    } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                        new Thread () {
                            @Override
                            public void run() {
                                JFileChooser seleccionado = new JFileChooser();

                                if(seleccionado.showSaveDialog(null)==seleccionado.APPROVE_OPTION){ 
                                    try {
                                        File file = seleccionado.getSelectedFile();
                                        image = ImageIO.read(file);
                                        side = Math.min (image.getHeight(), image.getWidth());
                                    } catch (IOException ex) {
                                        Logger.getLogger(ImageGameOfLife.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                } 
                                repaint();
                            }
                        }.start();
                        
                    }
                } else {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        gamestate = State.over;
                        repaint();
                    }
                }
            }
        });
    }
    
    @Override
    public void paintComponent(Graphics gg) {
        super.paintComponent(gg);
        Graphics2D g = (Graphics2D) gg;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        
        drawGrid(g);
    }
    
    private static Color getContrastColor(Color color) {
        double y = (299 * color.getRed() + 587 * color.getGreen() + 114 * color.getBlue()) / 1000;
        return y >= 128 ? Color.black : Color.white;
    }
    
    private void drawGrid(Graphics2D g) {
        g.setColor(GRID_COLOR);
        
        if (gamestate != State.running)
            g.fillRoundRect((int)(this.getWidth()/4.5), (int)(this.getHeight()/7.0), (int)(this.getWidth()/1.8), (int)(this.getHeight()/1.4), 15, 15);

        g.setColor(objective);
        g.fillRoundRect((int)(this.getWidth()/4.5), (int)(this.getHeight()/1.1), (int)(this.getWidth()/1.8), (int)(this.getHeight()/20), 15, 15);
        
        FontMetrics metrics;
        
        g.setColor(getContrastColor(objective));
        g.setFont(new Font("SansSerif", Font.BOLD, min((int)(this.getWidth()/30),(int)(this.getHeight()/25))));

        metrics = g.getFontMetrics();
        
        int widthString = metrics.charsWidth((this.side + "x" + this.side).toCharArray(), 0, (this.side + "x" + this.side).length());
        g.drawString(this.side + "x" + this.side, (int)(this.getWidth()/2 - widthString/2), (int)(this.getHeight()/1.05));
        
        g.setColor(GRID_COLOR.darker());
        
        if (mode == Mode.normal) {
            widthString = metrics.charsWidth(("Normal mode").toCharArray(), 0, ("Normal mode").length());
            g.drawString("Normal mode", (int)(this.getWidth()/2 - widthString/2), (int)(this.getHeight()/1.12));
        } else {
            widthString = metrics.charsWidth(("Degraded mode").toCharArray(), 0, ("Degraded mode").length());
            g.drawString("Degraded mode", (int)(this.getWidth()/2 - widthString/2), (int)(this.getHeight()/1.12));
        }
        
        if (gamestate != State.start) {
            g.setColor(GRID_COLOR.darker());
            g.setFont(new Font("SansSerif", Font.BOLD, min((int)(this.getWidth()/30),(int)(this.getHeight()/25))));
            
            metrics = g.getFontMetrics();
            widthString = metrics.charsWidth(("Generation: " + this.generation).toCharArray(), 0, ("Generation: " + this.generation).length());
            
            g.drawString("Generation: " + this.generation, (int)(this.getWidth()/2.8 - widthString/2), (int)(this.getHeight()/8.75));
            
            widthString = metrics.charsWidth(("Fitness: " + String.format("%.4f", this.fitness)).toCharArray(), 0, ("Fitness: " + String.format("%.4f", this.fitness)).length());
            
            g.drawString("Fitness: " + String.format("%.4f", this.fitness), (int)(this.getWidth()/1.6 - widthString/2), (int)(this.getHeight()/8.75));
            
        }
        
        if (gamestate == State.running) {
            g.setColor(GRID_COLOR.darker());
            g.setFont(new Font("SansSerif", Font.BOLD, min((int)(this.getWidth()/30),(int)(this.getHeight()/25))));
            
            for (int r = 0; r < side; r++) {
                for (int c = 0; c < side; c++) {
                    drawTile(g, r, c);
                }
            }
        } else {
            g.setColor(startColor);
            g.fillRoundRect((int)(this.getWidth()/4.186), (int)(this.getHeight()/6.0869565), (int)(this.getWidth()/1.91897654), (int)(this.getHeight()/1.4925373), 7, 7);
 
            g.setColor(GRID_COLOR.darker());
            g.setFont(new Font("SansSerif", Font.BOLD, min((int)(this.getWidth()/20),(int)(this.getHeight()/15))));
            
            metrics = g.getFontMetrics();
            widthString = metrics.charsWidth(("Image Game of Life").toCharArray(), 0, ("Image Game of Life").length());
            
            g.drawString("Image Game of Life", (int)(this.getWidth()/2 - widthString/2), (int)(this.getHeight()/2.59259259259));
 
            g.setFont(new Font("SansSerif", Font.BOLD, min((int)(this.getWidth()/45),(int)(this.getHeight()/35))));
            metrics = g.getFontMetrics();
            
            if (gamestate == State.over) {
                widthString = metrics.charsWidth(("game over").toCharArray(), 0, ("game over").length());
                g.drawString("Game Over", (int)(this.getWidth()/2 - widthString/2), (int)(this.getHeight()/2));
            }
            
            g.setColor(GRID_COLOR);
            widthString = metrics.charsWidth(("Click to start a new game").toCharArray(), 0, ("Click to start a new game").length());
            g.drawString("Click to start a new game", (int)(this.getWidth()/2 - widthString/2), (int)(this.getHeight()/1.8));
            
            widthString = metrics.charsWidth(("Up / Down to resize").toCharArray(), 0, ("Up / Down to resize").length());
            g.drawString("Up / Down to resize", (int)(this.getWidth()/2 - widthString/2), (int)(this.getHeight()/1.59));
            
            widthString = metrics.charsWidth(("M to switch modes").toCharArray(), 0, ("M to switch modes").length());
            g.drawString("M to switch modes", (int)(this.getWidth()/2 - widthString/2), (int)(this.getHeight()/1.4));
            
            widthString = metrics.charsWidth(("Space to load new image").toCharArray(), 0, ("Space to load new image").length());
            g.drawString("Space to load new image", (int)(this.getWidth()/2 - widthString/2), (int)(this.getHeight()/1.25));
        }
    }
    
    private void drawTile(Graphics2D g, int r, int c) {
        g.setColor(tiles[r][c].getColor());
        g.fillRect((int)Math.ceil(this.getWidth()/4.5) + (int)Math.ceil(c * (this.getWidth()/(1.8*side))), ((int)Math.ceil(this.getHeight()/7.0) + (int)Math.ceil (r * (this.getHeight()/(1.4*side)))),(int)Math.ceil(this.getWidth()/(1.8*side)), (int)Math.ceil(this.getHeight()/(1.4*side)));
    }
    
    private void cleanCross() {
        for (int i = 0; i < side; i++ ){
            for (int j = 0; j < side; j++) {
                tiles[i][j].setCross(false);
            }
        }
    }
    
    private void step() {
        cleanCross();
        
        int temporalFitness = 0;
        
        for (int i = 0; i < side; i++) {
            for (int j = 0; j < side; j++) {
                if (!tiles[i][j].getCross() && rand.nextInt(100) <= CROSS_RATIO) {
                    Pair<Integer,Integer> other = selectWhomCross(i,j);
                    if (other != null) {
                        tiles[i][j].setCross(true);
                        tiles[other.getL()][other.getR()].setCross(true);
                        
                        if (mode == Mode.normal) {
                            cross(i,j,other);
                        } else {
                            progresiveCross(i,j,other);
                        }
                    }
                }
                
                int[] color1 = tiles[i][j].getRGB();

                double deadRatio = Math.sqrt(Math.pow(color1[0]- tiles[i][j].getObjective().getRed(),2)
                                          + Math.pow(color1[1]- tiles[i][j].getObjective().getGreen(),2)
                                          + Math.pow(color1[2]- tiles[i][j].getObjective().getBlue(),2));
                
                temporalFitness += Math.floor((deadRatio*100)/MAX_DISTANCE);
                
                if (rand.nextInt(100) < Math.floor((deadRatio*100)/MAX_DISTANCE)) {
                    tiles[i][j].setCross(true);
                    //tiles[i][j].setColor(new Color(rand.nextInt(255),rand.nextInt(255),rand.nextInt(255)));
                    newTile(i,j);
                }
            }
        }
        fitness = 1/((temporalFitness/(double)(side*side))+1);
        generation++;
        
        if (fitness == 1)
            gamestate = State.over;
        
        repaint();
    }
    
    private void newTile(int i, int j) {
        int colorR = 0;
        int colorG = 0;
        int colorB = 0;
        int count = 0;
        
        for (int k = i-1; k <= i+1; k++) {
            for (int z = j-1; z <= j+1; z++) {
                if ((k >= 0 && k < side && z >= 0 && z < side) &&(k != i || z != j)) {
                   colorR += tiles[i][j].getRGB()[0];
                   colorG += tiles[i][j].getRGB()[1];
                   colorB += tiles[i][j].getRGB()[2];
                   count++;
                }
            }
        }
        
        int addR = tiles[i][j].getObjective().getRed() > colorR ? 1:-1;
        int addG = tiles[i][j].getObjective().getGreen() > colorG ? 1:-1;
        int addB = tiles[i][j].getObjective().getBlue() > colorB ? 1:-1;
        
        colorR = (int)Math.ceil((double)colorR/count);
        colorG = (int)Math.ceil((double)colorG/count);
        colorB = (int)Math.ceil((double)colorB/count);
        
        colorR = Math.max(Math.min(addR+(colorR+tiles[i][j].getObjective().getRed())/2,255),0);
        colorG = Math.max(Math.min(addR+(colorG+tiles[i][j].getObjective().getGreen())/2,255),0);
        colorB = Math.max(Math.min(addR+(colorB+tiles[i][j].getObjective().getBlue())/2,255),0);
        
        tiles[i][j].setRGB(colorR, colorG, colorB);
    }
    
    private void cross (int i, int j, Pair<Integer,Integer> other) {
        int[] color1 = tiles[i][j].getRGB();
        int[] color2 = tiles[other.getL()][other.getR()].getRGB();

        color1[0] = Math.abs(color1[0]-tiles[i][j].getObjective().getRed()) < Math.abs(color2[0]-tiles[i][j].getObjective().getRed()) ? color1[0]:color2[0];
        color1[1] = Math.abs(color1[1]-tiles[i][j].getObjective().getGreen()) < Math.abs(color2[1]-tiles[i][j].getObjective().getGreen()) ? color1[1]:color2[1];
        color1[2] = Math.abs(color1[2]-tiles[i][j].getObjective().getBlue()) < Math.abs(color2[2]-tiles[i][j].getObjective().getBlue()) ? color1[2]:color2[2];
        
        tiles[i][j].setRGB(color1[0], color1[1], color1[2]);
        tiles[other.getL()][other.getR()].setRGB(color1[0], color1[1], color1[2]);
    }
    
    private void progresiveCross(int i, int j, Pair <Integer,Integer> other) {
        int[] color1 = tiles[i][j].getRGB();
        int[] color2 = tiles[other.getL()][other.getR()].getRGB();
        
        int distR1 = Math.abs(color1[0]-tiles[i][j].getObjective().getRed());
        int distG1 = Math.abs(color1[1]-tiles[i][j].getObjective().getGreen());
        int distB1 = Math.abs(color1[2]-tiles[i][j].getObjective().getBlue());
        
        int distR2 = Math.abs(color2[0]-tiles[other.getL()][other.getR()].getObjective().getRed());
        int distG2 = Math.abs(color2[1]-tiles[other.getL()][other.getR()].getObjective().getGreen());
        int distB2 = Math.abs(color2[2]-tiles[other.getL()][other.getR()].getObjective().getBlue());
        
        int addR = tiles[i][j].getObjective().getRed() > (color1[0]+color2[0])/2 ? 1:-1;
        int addG = tiles[i][j].getObjective().getGreen() > (color1[1]+color2[1])/2 ? 1:-1;
        int addB = tiles[i][j].getObjective().getBlue() > (color1[2]+color2[2])/2 ? 1:-1;
        
        int addR2 = tiles[other.getL()][other.getR()].getObjective().getRed() > (color1[0]+color2[0])/2 ? 1:-1;
        int addG2 = tiles[other.getL()][other.getR()].getObjective().getGreen() > (color1[1]+color2[1])/2 ? 1:-1;
        int addB2 = tiles[other.getL()][other.getR()].getObjective().getBlue() > (color1[2]+color2[2])/2 ? 1:-1;
        
        color1[0] = distR1 < distR2 ? color1[0]:Math.max(Math.min(addR+(color1[0]+color2[0])/2,255),0);
        color1[1] = distG1 < distG2 ? color1[1]:Math.max(Math.min(addG+(color1[1]+color2[1])/2,255),0);
        color1[2] = distB1 < distB2 ? color1[2]:Math.max(Math.min(addB+(color1[2]+color2[2])/2,255),0);
        
        color2[0] = distR2 < distR1 ? color2[0]:Math.max(Math.min(addR2+(color1[0]+color2[0])/2,255),0);
        color2[1] = distG2 < distG1 ? color2[1]:Math.max(Math.min(addG2+(color1[1]+color2[1])/2,255),0);
        color2[2] = distB2 < distB1 ? color2[2]:Math.max(Math.min(addB2+(color1[2]+color2[2])/2,255),0);
        
        tiles[i][j].setRGB(color1[0], color1[1], color1[2]);
        tiles[other.getL()][other.getR()].setRGB(color2[0], color2[1], color2[2]);
    }
            
    private Pair<Integer, Integer> selectWhomCross(int i, int j) {
        ArrayList<Pair> positions = new ArrayList();

        if (i+1 < side) {
            positions.add(new Pair(i+1,j));
            if (j-1 >= 0) {
                positions.add(new Pair(i+1,j-1));
            }
        }
        
        if (j+1 < side) {
            positions.add(new Pair(i,j+1));
            if (i+1 < side) {
                positions.add(new Pair(i+1,j+1));
            }
        }
        
        if (positions.isEmpty()) {
            return null;
        }

        return positions.get(rand.nextInt(positions.size()));
    }
    
    private void startGame() {        
        gamestate = State.running;
        generation = 0;
        fitness = 0;
        tiles = new Tile[side][side];
        
        initTiles();
        repaint();
        playing();
    }
    
    private void initTiles() {
        for(int i = 0; i < side; i++) {
            for (int j = 0; j < side; j++) {
                if (image != null)
                    tiles[i][j] = new Tile(rand.nextInt(255),rand.nextInt(255),rand.nextInt(255),new Color (image.getRGB(j,i)));
                else
                    tiles[i][j] = new Tile(rand.nextInt(255),rand.nextInt(255),rand.nextInt(255),new Color(Math.max(255-i,0),Math.max(255-i,0),Math.max(255-i,0)));
            }
        }
    }
    
    private void playing() {//1000/FRAME_RATE // 500
        timer = new Timer(1000/120, (ActionEvent evt) -> {
            step();
            if (gamestate != State.running) {
                timer.stop();
            }
        });
        
        timer.start();
    }
}

class Tile {
    private int r;
    private int g;
    private int b;
    private Color color;
    private Color objective;
    
    private boolean cross;
 
    Tile(int r, int g, int b, Color objective) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.color = new Color(r,g,b);
        this.cross = false;
        this.objective = objective;
    }
    
    void setRGB(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.color = new Color(r,g,b);
    }
    
    int[] getRGB() {
        int [] rgb = new int [3];
        rgb[0] = this.r;
        rgb[1] = this.g;
        rgb[2] = this.b;
        return rgb;
    }
    
    Color getObjective() {
        return this.objective;
    }
    
    Color getColor() {
        return this.color;
    }
    
    void setColor (Color color) {
        this.color = color;
        this.r = color.getRed();
        this.g = color.getGreen();
        this.b = color.getBlue();
    }
    
    void setCross(boolean cross) {
        this.cross = cross;
    }
    
    boolean getCross() {
        return this.cross; 
    }
}

class Pair<L,R> {
    private L l;
    private R r;
    public Pair(L l, R r){
        this.l = l;
        this.r = r;
    }
    public L getL(){ return l; }
    public R getR(){ return r; }
    public void setL(L l){ this.l = l; }
    public void setR(R r){ this.r = r; }
}