package tetris;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import static java.lang.String.format;
import static java.lang.Math.*;
import static tetris.Settings.*;

public class Tetris extends JPanel implements Runnable {
    static final int EMPTY = -1;
    static final int BORDER = -2;

    final int[][] grid = new int[Row][Col];

    Thread fThread;
    
    static final Random rand = new Random();

    enum Direction {
        right(1, 0), down(0, 1), left(-1, 0);

        Direction(int x, int y) {
            this.x = x;
            this.y = y;
        }
        final int x, y;
    };

    //落ちるミノの形状
    Shape fShape;
    Shape nShape;

    //ミノの落ちる座標
    int fShapeRow;
    int fShapeCol;

    //ミノの形状を設定
    enum Shape {
        IShape(new int[][]{{0, -1}, {0, 0}, {0, 1}, {0, 2}}),
        JShape(new int[][]{{1, -1}, {0, -1}, {0, 0}, {0, 1}}),
        LShape(new int[][]{{-1, -1}, {0, -1}, {0, 0}, {0, 1}}),
        SShape(new int[][]{{0, -1}, {0, 0}, {1, 0}, {1, 1}}),
        TShape(new int[][]{{-1, 0}, {0, 0}, {1, 0}, {0, 1}}),
        ZShape(new int[][]{{0, -1}, {0, 0}, {-1, 0}, {-1, 1}}),
        OShape(new int[][]{{0, 0}, {1, 0}, {0, 1}, {1, 1}});

        private Shape(int[][] shape) {
            this.shape = shape;
            pos = new int[4][2];
            reset();
        }

        void reset() {
            for(int i = 0; i < pos.length; i++) {
                pos[i] = shape[i].clone();
            }
        }

        final int[][] pos, shape;
    }

    public void startGame() {
        stop();
        grid();
        choiceShape();
        (fThread = new Thread(this)).start();
    }

    public void stop() {
        if (fThread != null) {
            Thread tmp = fThread;
            fThread = null;
            tmp.interrupt();
        }
    }

    public void grid() {
        for (int g = 0; g < Row; g++) {
            Arrays.fill(grid[g], EMPTY);
            for (int b = 0; b <Col; b++) {
                if (b == 0 || b == Col - 1 || g == Row - 1)
                    grid[g][b] = BORDER;
            }
        }
    }
    
    public void choiceShape() {
        fShapeRow = 1;
        fShapeCol = 5;
        fShape = nShape;

        Shape[] shapes = Shape.values();
        nShape = shapes[rand.nextInt(shapes.length)];

        if (fShape != null)
            fShape.reset();
    }

    public void startScreen(Graphics2D g) {
        g.setColor(titleBackGroundColor);
        g.setFont(mainFont);
        g.fill(titleRect);
        g.fill(pressRect);

        g.setColor(textColor);
        g.setFont(subFont);
        g.drawString("Press any key to start", startX, startY);
    }

    public void drawSquare(Graphics2D g, int colorIndex, int r, int c) {
        g.setColor(colors[colorIndex]);
        g.fillRect(leftSpace + c * blockSize, topSpace + r * blockSize, blockSize, blockSize);  
        g.setStroke(smallStroke);
        g.setColor(border);
        g.drawRect(leftSpace + c * blockSize, topSpace + r * blockSize, blockSize, blockSize);
    }

    public void drawFShape(Graphics2D g) {
        int idx = fShape.ordinal();
        for (int[] p : fShape.pos)
            drawSquare(g, idx, fShapeRow + p[1], fShapeCol + p[0]);
    }


    public Tetris() {
        grid();
        choiceShape();

        setFocusable(true);
        setPreferredSize(dim);
        setBackground(backGroundColor);


        addKeyListener(new KeyAdapter() {
            boolean hardDrop;

            @Override
            public void keyPressed(KeyEvent e) {
                startGame();
                repaint();

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        if(rotatable(fShape))
                            rotate(fShape);
                        break;

                    case KeyEvent.VK_RIGHT:
                        if (movable(fShape, Direction.right))
                            move(Direction.right);

                    case KeyEvent.VK_LEFT:
                        if (movable(fShape, Direction.left))
                            move(Direction.left);
                        break;

                    case KeyEvent.VK_DOWN:
                        if (!hardDrop) {
                            hardDrop = true;
                            while (movable(fShape, Direction.down)) {
                                move(Direction.down);
                                repaint();
                            }
                        }
                }
                repaint();
            }
            @Override
            public void keyReleased(KeyEvent e) {
                hardDrop = false;
            }
        });
    }

    @Override
    public void run() {
        while (Thread.currentThread() == fThread) {
            try {
                Thread.sleep(setSpeed());
            } catch (InterruptedException e) {
                return;
            }

            if (!gameOver()) {
                if (movable(fShape, Direction.down)) {
                    move(Direction.down);
                } else {
                    landed();
                }
                repaint();
            }
        }
    }

    @Override
    public void paintComponent(Graphics gg) {
        super.paintComponent(gg);
        Graphics2D g = (Graphics2D) gg;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        if (!gameOver()) {
            startScreen(g);
        } else {
            drawFShape(g);
        }
    }

    public boolean rotatable(Shape s) {
        if (s == Shape.OShape)
            return false;

        int[][] pos = new int[4][2];
        for (int i = 0; i < pos.length; i++) {
            pos[i] = s.pos[i].clone();
        }

        for (int[] row : pos) {
            int tmp = row[0];
            row[0] = row[1];
            row[1] = -tmp;
        }

        for (int[] p : pos) {
            int newCol = fShapeCol + p[0];
            int newRow = fShapeRow + p[1];
            if (grid[newRow][newCol] != EMPTY) {
                return false;
            }
        }
        return true;
    }

    public void rotate(Shape s) {
        if (s == Shape.OShape)
            return;

        for (int[] row : s.pos) {
            int tmp = row[0];
            row[0] = row[1];
            row[1] = -tmp;
        }
    }

    public void move(Direction dir) {
        fShapeRow += dir.y;
        fShapeCol += dir.x;
    }

    public boolean movable(Shape s, Direction direction) {
        for (int[] p : s.pos) {
            int newCol = fShapeCol + direction.x + p[0];
            int newRow = fShapeRow + direction.y + p[1];
            if (grid[newRow][newCol] != EMPTY)
                return false;
        }
        return true;
    }

    public void landed() {
        addShape(fShape);
        if (fShapeRow < 2) {
            gameOver();
            stop();
        } else {
            removeLines();
        }
        choiceShape();
    }

    public void addShape(Shape s) {
        for (int[] p : s.pos)
            grid[fShapeRow + p[1]][fShapeCol + p[0]] = s.ordinal();
    }

    public int removeLines() {
        int count = 0;
        for (int r = 0; r < Row - 1; r++) {
            for (int c = 1; c < Col - 1; c++) {
                if (grid[r][c] == EMPTY)
                    break;
                if (c == Col - 2) {
                    count++;
                    removeLine(r);
                }
            }
        }
        return count;
    }

    public void removeLine(int line) {
        for (int c = 0; c < Col; c++)
            grid[line][c] = EMPTY;

        for (int c = 0; c < Col; c++) {
            for (int r = line; r > 0; r--)
                grid[r][c] = grid[r - 1][c];
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame();
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setTitle("Tetris");
            f.setResizable(false);
            f.add(new Tetris(), BorderLayout.CENTER);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }

}
