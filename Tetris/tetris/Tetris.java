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

    public void grid() {
        for(int g = 0; g < Row; g++) {
            Arrays.fill(grid[g], EMPTY);
            for(int b = 0; b <Col; b++) {
                if(b == 0 || b == Col - 1 || g == Row - 1)
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

        if(fShape != null)
            fShape.reset();
    }

    public Tetris() {
        grid();
        choiceShape();

        setFocusable(true);
        setPreferredSize(dim);
        setBackground(backgroundColor);


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
                        if(movable(fShape))
                            move(Direction.right);

                    case KeyEvent.VK_LEFT:
                        if(movable(fShape))
                            move(Direction.left);
                        break;

                    case KeyEvent.VK_DOWN:
                        if(!hardDrop) {
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

    

}
