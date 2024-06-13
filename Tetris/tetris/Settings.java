package tetris;

import java.awt.*;

public final class Settings {
    final static int Row = 18;
    final static int Col = 12;

    final static Dimension dim = new Dimension(700, 700); 

    final static Color[] colors = {Color.magenta, Color.pink, Color.red, Color.green, Color.blue, Color.cyan};
    final static Color backGroundColor = new Color(0xB3B8BB);
    final static Color gridColor = new Color(0xBECFEA);
    final static Color titleBackGroundColor = Color.white;
    final static Color textColor = Color.black;
    final static Color border = Color.white;

    final static int startX = 120;
    final static int startY = 400;

    final static int topSpace = 50;
    final static int leftSpace = 20;
    final static int blockSize = 30;

    final static Stroke largeStroke = new BasicStroke(5);
    final static Stroke smallStroke = new BasicStroke(2);

    final static Rectangle titleRect = new Rectangle(100, 85, 252, 100);
    final static Rectangle pressRect = new Rectangle(50, 375, 252, 40);
    final static Rectangle gridRect = new Rectangle(46, 47, 308, 517);

    final static Font mainFont = new Font("Arial", Font.BOLD, 50);
    final static Font subFont = mainFont.deriveFont(Font.BOLD, 18);
}
