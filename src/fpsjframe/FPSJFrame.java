
package fpsjframe;

import java.io.File; 
import java.io.IOException; 
import java.util.Scanner; 

import javax.sound.sampled.AudioInputStream; 
import javax.sound.sampled.AudioSystem; 
import javax.sound.sampled.Clip; 
import javax.sound.sampled.LineUnavailableException; 
import javax.sound.sampled.UnsupportedAudioFileException; 

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;

public class FPSJFrame extends JPanel implements KeyListener, Runnable {
    
    private final int nScreenWidth = 800;
    private final int nScreenHeight = 600;
    private final int nMapWidth = 16;
    private final int nMapHeight = 16;
    private final float fFOV = (float) (Math.PI / 4.0);
    private final float fDepth = 16.0f;
    private final float fSpeed = 5.0f;

    private float fPlayerX = 1.5f; // Starting position
    private float fPlayerY = 1.5f;
    private float fPlayerA = 0.0f;

    private boolean[] keys = new boolean[4]; // W, A, S, D
    private String map;

    private enum GameState { STARTUP, IN_GAME, CONGRATS }
    private GameState gameState = GameState.STARTUP;

    // Ending position
    private final float fEndX = 14.5f;
    private final float fEndY = 14.5f;

    public FPSJFrame() {
        Audio audio = new Audio();
        setPreferredSize(new Dimension(nScreenWidth, nScreenHeight));
        setFocusable(true);
        addKeyListener(this);

        // Create Map with Start (S) and End (E)
        map = "S.......#.......";
        map += "#...............";
        map += "#.......########";
        map += "#..............#";
        map += "#......##......#";
        map += "#......##......#";
        map += "#..............#";
        map += "###............#";
        map += "##.............#";
        map += "#......####..###";
        map += "#......#.......#";
        map += "#......#.......#";
        map += "#..............#";
        map += "#......#########";
        map += "#..............E";
        map += "################";
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (gameState == GameState.STARTUP) {
            drawStartupScreen(g);
        } else if (gameState == GameState.IN_GAME) {
            drawGame(g);
            drawMap(g);
        } else if (gameState == GameState.CONGRATS) {
            drawCongratsScreen(g);
        }
    }

    private void drawStartupScreen(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, nScreenWidth, nScreenHeight);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("FPS Game", nScreenWidth / 2 - 100, nScreenHeight / 2 - 50);
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString("Press ENTER to Start", nScreenWidth / 2 - 130, nScreenHeight / 2);
    }

    private void drawCongratsScreen(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, nScreenWidth, nScreenHeight);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("Congratulations!", nScreenWidth / 2 - 150, nScreenHeight / 2 - 50);
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString("You reached the end!", nScreenWidth / 2 - 120, nScreenHeight / 2);
        g.drawString("Press enter to exit", nScreenWidth / 2 - 100, nScreenHeight / 2 + 50);
    }

    private void drawGame(Graphics g) {
        // Clear screen
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, nScreenWidth, nScreenHeight);

        // Draw the map
        drawMap(g);

        // Draw Player as an Arrow pointing in the direction of movement
        drawPlayerArrow(g, fPlayerX, fPlayerY, fPlayerA);

        // Perform raycasting for 3D effect
        for (int x = 0; x < nScreenWidth; x++) {
            float fRayAngle = (fPlayerA - fFOV / 2.0f) + ((float) x / nScreenWidth) * fFOV;
            float fStepSize = 0.1f;
            float fDistanceToWall = 0.0f;
            boolean bHitWall = false;
            float fEyeX = (float) Math.sin(fRayAngle);
            float fEyeY = (float) Math.cos(fRayAngle);

            while (!bHitWall && fDistanceToWall < fDepth) {
                fDistanceToWall += fStepSize;
                int nTestX = (int) (fPlayerX + fEyeX * fDistanceToWall);
                int nTestY = (int) (fPlayerY + fEyeY * fDistanceToWall);

                if (nTestX < 0 || nTestX >= nMapWidth || nTestY < 0 || nTestY >= nMapHeight) {
                    bHitWall = true;
                    fDistanceToWall = fDepth;
                } else {
                    if (map.charAt(nTestY * nMapWidth + nTestX) == '#') {
                        bHitWall = true;
                    }
                }
            }

            // Calculate column height and draw
            int nCeiling = (int) ((nScreenHeight / 2.0) - nScreenHeight / ((float) fDistanceToWall));
            int nFloor = nScreenHeight - nCeiling;

            for (int y = 0; y < nScreenHeight; y++) {
                if (y < nCeiling) {
                    g.setColor(Color.BLACK);
                } else if (y > nCeiling && y <= nFloor) {
                    g.setColor(Color.GRAY);
                } else {
                    g.setColor(Color.WHITE);
                }
                g.drawLine(x, y, x, y);
            }
        }

        // Check if player reached the endpoint
        if (Math.abs(fPlayerX - fEndX) < 0.5 && Math.abs(fPlayerY - fEndY) < 0.5) {
            gameState = GameState.CONGRATS;
        }
    }

    private void drawMap(Graphics g) {
    int tileSize = 12; // Reduced size of each tile
    int mapOffsetX = 10; // Offset from the left
    int mapOffsetY = 10; // Offset from the top

    for (int x = 0; x < nMapWidth; x++) {
        for (int y = 0; y < nMapHeight; y++) {
            char tile = map.charAt(y * nMapWidth + x);
            Color color;
            switch (tile) {
                case '#':
                    color = Color.BLACK;
                    break;
                case 'S':
                    color = Color.GREEN;
                    break;
                case 'E':
                    color = Color.RED;
                    break;
                default:
                    color = Color.WHITE;
                    break;
            }
            g.setColor(color);
            g.fillRect(mapOffsetX + x * tileSize, mapOffsetY + y * tileSize, tileSize, tileSize); // Draw the tile
        }
    }

    // Draw the player's position on the map
    int playerTileSize = 6; // Size of the player indicator on the map
    int playerXMap = (int) (fPlayerX);
    int playerYMap = (int) (fPlayerY);
    g.setColor(Color.BLUE);
    g.fillRect(mapOffsetX + playerXMap * tileSize + (tileSize - playerTileSize) / 2,
               mapOffsetY + playerYMap * tileSize + (tileSize - playerTileSize) / 2,
               playerTileSize, playerTileSize); // Draw the player indicator
}



    private void drawPlayerArrow(Graphics g, float x, float y, float angle) {
    Graphics2D g2d = (Graphics2D) g;
    int arrowSize = 20;

    Polygon arrow = new Polygon();
    arrow.addPoint(0, -arrowSize);
    arrow.addPoint(-arrowSize / 2, arrowSize / 2);
    arrow.addPoint(arrowSize / 2, arrowSize / 2);

    AffineTransform transform = new AffineTransform();
    transform.translate(x * 40 + 20, y * 40 + 20); // Adjust center position
    transform.rotate(angle);

    Shape transformedArrow = transform.createTransformedShape(arrow);
    g2d.setColor(Color.BLUE);
    g2d.fill(transformedArrow);
}


    public void updateGame(float fElapsedTime) {
        if (keys[0]) { // W key for forward movement
            float newPlayerX = fPlayerX + (float) Math.sin(fPlayerA) * fSpeed * fElapsedTime;
            float newPlayerY = fPlayerY + (float) Math.cos(fPlayerA) * fSpeed * fElapsedTime;
            if (map.charAt((int) (newPlayerY) * nMapWidth + (int) (newPlayerX)) != '#') {
                fPlayerX = newPlayerX;
                fPlayerY = newPlayerY;
            }
        }
        if (keys[1]) { // A key for left rotation
            fPlayerA -= (fSpeed * 0.75f) * fElapsedTime;
        }
        if (keys[2]) { // S key for backward movement
            float newPlayerX = fPlayerX - (float) Math.sin(fPlayerA) * fSpeed * fElapsedTime;
            float newPlayerY = fPlayerY - (float) Math.cos(fPlayerA) * fSpeed * fElapsedTime;
            if (map.charAt((int) (newPlayerY) * nMapWidth + (int) (newPlayerX)) != '#') {
                fPlayerX = newPlayerX;
                fPlayerY = newPlayerY;
            }
        }
        if (keys[3]) { // D key for right rotation
            fPlayerA += (fSpeed * 0.75f) * fElapsedTime;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameState == GameState.STARTUP && e.getKeyCode() == KeyEvent.VK_ENTER) {
            gameState = GameState.IN_GAME;
            new Thread(this).start(); // Start the game loop
            Audio audioPlayer = new Audio();
        try {
            audioPlayer.loadAudio("EWF.wav"); // Change this to your actual file path
            audioPlayer.playSound(); // Play the audio
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
            ex.printStackTrace(); // Handle exceptions
        }
        }

        if (gameState == GameState.IN_GAME) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_W: keys[0] = true; break;
                case KeyEvent.VK_A: keys[1] = true; break;
                case KeyEvent.VK_S: keys[2] = true; break;
                case KeyEvent.VK_D: keys[3] = true; break;
            }
        }
        
        if (gameState == GameState.CONGRATS && e.getKeyCode() == KeyEvent.VK_ENTER) {
            Audio aud = new Audio();
            aud.stopSound();
            System.exit(0);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameState == GameState.IN_GAME) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_W: keys[0] = false; break;
                case KeyEvent.VK_A: keys[1] = false; break;
                case KeyEvent.VK_S: keys[2] = false; break;
                case KeyEvent.VK_D: keys[3] = false; break;
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double nsPerTick = 1000000000.0 / 60.0; // 60 ticks per second
        double delta = 0;
        while (gameState == GameState.IN_GAME) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerTick;
            lastTime = now;
            while (delta >= 1) {
                updateGame(1.0f / 60.0f);
                delta--;
            }
            repaint();
            try {
                Thread.sleep(16); // Roughly 60 FPS
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        
        JFrame frame = new JFrame("FPS Game");
        FPSJFrame game = new FPSJFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(game);
        frame.pack();
        frame.setVisible(true);
    }

}