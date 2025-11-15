package sem2;

import javax.swing.*;
import java.awt.*;
import java.awt.image.IndexColorModel;

import static java.lang.Math.pow;

public class IndexColorModelPaletteDisplayer {
    private final int PIXELS_PER_SQUARE = 50;
    private final int SQUARE_DIMENSION = 16;
    private final int NUMBER_OF_BITS = 8;
    private final int NUMBER_OF_COLORS = (int) pow(2, NUMBER_OF_BITS);
    private int redBits;
    private int greenBits;
    private int blueBits;

    // stara se jen o check spravneho zadani a vyvolani metody v separe vlakne ig
    void main(String[] args) {
        if (args.length != 3 || !sumCheck(args)) {
            System.out.println("Wrong number of arguments, usage:\njava sem2.IndexColorModelPaletteDisplayer.java <rBits> <gBits> <bBits>");
            return;
        }

        SwingUtilities.invokeLater(() -> displayPalette(computePalette(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]))));
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

    public IndexColorModel computePalette(int redBits, int greenBits, int blueBits) {
        this.redBits = redBits;
        this.greenBits = greenBits;
        this.blueBits = blueBits;

        byte[] red = new byte[NUMBER_OF_COLORS];
        byte[] green = new byte[NUMBER_OF_COLORS];
        byte[] blue = new byte[NUMBER_OF_COLORS];

        // pro kazdou barvu (indexove cislo) vypocitam cislo co odpovida intenzite barvy ... asi?
        // napr 3 bity = 8 barev pro cervenou, 0 - 7 cislo
        for (int i = 0; i < NUMBER_OF_COLORS; i++) {
            red[i] = (byte) computeRed(i);
            green[i] = (byte) computeGreen(i);
            blue[i] = (byte) computeBlue(i);
        }
        return new IndexColorModel(NUMBER_OF_BITS, NUMBER_OF_COLORS, red, green, blue);
    }

    private void displayPalette(IndexColorModel palette) {
        Panel palettePanel = new Panel();
        palettePanel.setPreferredSize(new Dimension(SQUARE_DIMENSION * PIXELS_PER_SQUARE, SQUARE_DIMENSION * PIXELS_PER_SQUARE));
        palettePanel.setLayout(new GridLayout(SQUARE_DIMENSION, SQUARE_DIMENSION));

        for(int i = 0; i < NUMBER_OF_COLORS; i++) {
        JPanel colorPanel = new JPanel();
        colorPanel.setBackground(new Color(palette.getRGB(i)));
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
        return ((index >>> (NUMBER_OF_BITS - this.redBits)) * (NUMBER_OF_COLORS - 1)) / ((int) pow(2, this.redBits) - 1);
    }

    private int computeGreen(int index) {
        return (((index >>> blueBits) & (int) pow(2, greenBits) - 1) * (NUMBER_OF_COLORS - 1)) / ((int) pow(2, greenBits) - 1);
    }

    private int computeBlue(int index) {
        return ((index & (int) pow(2, blueBits) - 1) * (NUMBER_OF_COLORS - 1)) / ((int) pow(2, blueBits) - 1);
    }

    public int getNUMBER_OF_COLORS() {
        return NUMBER_OF_COLORS;
    }

    public int getNUMBER_OF_BITS() {
        return NUMBER_OF_BITS;
    }
}