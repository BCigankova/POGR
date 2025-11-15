package sem2;

import javax.swing.*;
import java.awt.*;

import static java.lang.Math.pow;

@Deprecated
public class PaletteDisplayer {
    public final int PIXELS_PER_SQUARE = 50;
    private int redBits;
    private int greenBits;
    private int blueBits;

    void main(String[] args) {
        if (args.length != 3 || !sumCheck(args)) {
            System.out.println("Wrong number of arguments, usage:\njava sem2.PaletteDisplayer.java <rBits> <gBits> <bBits>");
        }

        this.redBits = Integer.parseInt(args[0]);
        this.greenBits = Integer.parseInt(args[1]);
        this.blueBits = Integer.parseInt(args[2]);

        SwingUtilities.invokeLater(this::displayPalette);
    }

    public Color[] computePallette(int rbits, int gbits, int bbits) {
        this.redBits = rbits;
        this.greenBits = gbits;
        this.blueBits = bbits;

        Color[] colors = new Color[256];
        for (int i = 0; i < 256; i++) {
            colors[i] = new Color(computeRed(i), computeGreen(i), computeBlue(i));
        }

        return colors;
    }

    private boolean sumCheck(String[] args) {
        boolean result;
        try {
            result = Integer.parseInt(args[0]) + Integer.parseInt(args[1]) + Integer.parseInt(args[2]) == 8;
        } catch (Exception e) {
            return false;
        }
        return result;
    }

    private void displayPalette() {
        Color[] colors = new Color[256];
        for (int i = 0; i < 256; i++) {
            colors[i] = new Color(computeRed(i), computeGreen(i), computeBlue(i));
        }

        Panel palettePanel = new Panel();
        palettePanel.setPreferredSize(new Dimension(16 * PIXELS_PER_SQUARE, 16 * PIXELS_PER_SQUARE));
        palettePanel.setLayout(new GridLayout(16, 16));

        for (Color color : colors) {
            JPanel colorPanel = new JPanel();
            colorPanel.setBackground(color);
            colorPanel.setPreferredSize(new Dimension(PIXELS_PER_SQUARE, PIXELS_PER_SQUARE));
            palettePanel.add(colorPanel);
        }

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(palettePanel);
        frame.pack();
        frame.setVisible(true);
    }

    private int computeRed(int index) {
        return ((index >>> (8 - this.redBits)) * 255) / ((int) pow(2, this.redBits) - 1);
    }

    private int computeGreen(int index) {
        return (((index >>> blueBits) & (int) pow(2, greenBits) - 1) * 255) / ((int) pow(2, greenBits) - 1);
    }

    private int computeBlue(int index) {
        return ((index & (int) pow(2, blueBits) - 1) * 255) / ((int) pow(2, blueBits) - 1);
    }
}


// TODO: prepsat do IndexColorModel achjo
