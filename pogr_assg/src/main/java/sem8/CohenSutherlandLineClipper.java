package sem8;

import utils.Line;

import javax.swing.*;
import java.awt.*;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;

// predpoklada pohyb jen na prvim kvadrantu
public class CohenSutherlandLineClipper {
    private Rectangle clipRectangle;
    private Line[] lines;
    private WritableRaster raster;
    private int rasterWidth;
    private int rasterHeight;

    void main(String[] args) {
        if (args.length % 2 != 0) {
            System.out.println("Wrong number of arguments, usage:\njava sem8.CohenSutherlandLineClipper.java <xr> rectangle upperleft X <yr> rectangle upperleft Y <width> <height> of rectangle\n  <x1> <y1> <x2> <y2> ... coordinates of line endpoints ");
            return;
        }

        this.clipRectangle =  new Rectangle(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
        lines = new Line[(args.length - 4) / 4];
        for(int i = 4; i < args.length; i += 4) {
            lines[(i - 4) / 4] = new Line(new Point(Integer.parseInt(args[i]), Integer.parseInt(args[i + 1])), new Point(Integer.parseInt(args[i + 2]), Integer.parseInt(args[i + 3])));
        }

        // SwingUtilities.invokeLater(this::displayUnclippedLines);
        SwingUtilities.invokeLater(this::clip);
    }

    private void clip() {
        // nutny cyklus pro ty co se orizly jestli nemusime oriznout i jinde?

        int widthStarts = Arrays.stream(lines).mapToInt(line -> Math.abs(line.getStart().x)).max().getAsInt();
        int widthEnds = Arrays.stream(lines).mapToInt(line -> Math.abs(line.getEnd().x)).max().getAsInt();
        int heightStarts = Arrays.stream(lines).mapToInt(line -> Math.abs(line.getEnd().y)).max().getAsInt();
        int heightEnds = Arrays.stream(lines).mapToInt(line -> Math.abs(line.getEnd().y)).max().getAsInt();

        int width = Math.max(widthStarts, widthEnds);
        int height = Math.max(heightStarts, heightEnds);
        BufferedImage image = new BufferedImage(width + 50, height + 50, BufferedImage.TYPE_BYTE_BINARY);

        this.raster = image.getRaster();
        this.rasterWidth = image.getWidth();
        this.rasterHeight = image.getHeight();

        ArrayList<Line> recheckLines = new ArrayList<>();
        for (int y = 0; y < this.rasterHeight; y++) {
            for (int x = 0; x < this.rasterWidth; x++) {
                raster.setSample(x, y, 0, 255);
            }
        }
        for(Line line : lines) {
            int codeStart = getCode(line.getStart());
            int codeEnd = getCode(line.getEnd());
            int union = codeStart | codeEnd;
            int intersection = codeStart & codeEnd;
            if(union == 0) {
                drawLine(line);
            }
            else if(intersection == 0) {
                recheckLines.add(new Line(clipLine(line, codeStart), clipLine(line, codeEnd)));
            }
            // intersection != 0 cela usecka mimo
        }
        for(Line line : recheckLines) {
            int codeStart = getCode(line.getStart());
            int codeEnd = getCode(line.getEnd());
            int union = codeStart | codeEnd;
            int intersection = codeStart & codeEnd;
            if(union == 0) {
                drawLine(line);
            }
            else if(intersection == 0) {
                drawLine(new Line(clipLine(line, codeStart), clipLine(line, codeEnd)));
            }
        }
        drawRectangleIntoRaster();
        displayPanel(image);
    }

    // pres rasterizaci usecky? TODO
    private void drawLine(Line line) {
        for(int y = 0; y < this.rasterHeight; y++) {
            for(int x = 0; x < this.rasterWidth; x++) {
                if(line.isOnLineWithBoundary(new Point(x, rasterHeight - y))) {
                    raster.setSample(x, y, 0, 0);
                }
            }
        }
    }

    private Point clipLine(Line line, int code) {
        int newX = 0;
        int newY = 0;
        if((code & 1) == 1) {
            newX = (int) (line.getStart().x + (line.getEnd().x - line.getStart().x) * (clipRectangle.getMaxY() - line.getStart().y) / (line.getEnd().y - line.getStart().y));
            newY = (int) clipRectangle.getMaxY();
        }
        if((code & 2) == 2) {
            newX = (int) (line.getStart().x + (line.getEnd().x - line.getStart().x) * (clipRectangle.getMinY() - line.getStart().y) / (line.getEnd().y - line.getStart().y));
            newY = (int) clipRectangle.getMinY();
        }
        if((code & 4) == 4) {
            newX = (int) clipRectangle.getMaxX();
            newY = (int) (line.getStart().y + (line.getEnd().y - line.getStart().y) * (clipRectangle.getMaxX() - line.getStart().x) / (line.getEnd().x - line.getStart().x));
        }
        if((code & 8) == 8) {
            newX = (int) clipRectangle.getMinX();
            newY = (int) (line.getStart().y + (line.getEnd().y - line.getStart().y) * (clipRectangle.getMinX() - line.getStart().x) / (line.getEnd().x - line.getStart().x));

        }
        return new Point(newX, newY);
    }

    private int getCode(Point p) {
        int returnCode = 0;
        if(p.x < clipRectangle.getMinX()) {
            returnCode = returnCode | 8;
        }
        if(p.y < clipRectangle.getMinY()) {
            returnCode = returnCode | 2;
        }
        if(p.x > clipRectangle.getMaxX()) {
            returnCode = returnCode | 4;
        }
        if(p.y > clipRectangle.getMaxY()) {
            returnCode = returnCode | 1;
        }
        return returnCode;
    }

    private void displayUnclippedLines() {
        int widthStarts = Arrays.stream(lines).mapToInt(line -> Math.abs(line.getStart().x)).max().getAsInt();
        int widthEnds = Arrays.stream(lines).mapToInt(line -> Math.abs(line.getEnd().x)).max().getAsInt();
        int heightStarts = Arrays.stream(lines).mapToInt(line -> Math.abs(line.getEnd().y)).max().getAsInt();
        int heightEnds = Arrays.stream(lines).mapToInt(line -> Math.abs(line.getEnd().y)).max().getAsInt();

        int width = Math.max(widthStarts, widthEnds);
        int height = Math.max(heightStarts, heightEnds);
        BufferedImage image = new BufferedImage(width + 50, height + 50, BufferedImage.TYPE_BYTE_BINARY);

        this.raster = image.getRaster();
        this.rasterWidth = image.getWidth();
        this.rasterHeight = image.getHeight();

        for (int y = 0; y < this.rasterHeight; y++) {
            for (int x = 0; x < this.rasterWidth; x++) {
                raster.setSample(x, y, 0, 255);
            }
        }

        drawLinesIntoRaster();
        drawRectangleIntoRaster();
        displayPanel(image);
    }

    private void drawLinesIntoRaster() {
        for(int y = 0; y < this.rasterHeight; y++) {
            for(int x = 0; x < this.rasterWidth; x++) {
                int finalX = x;
                int finalY = y;

                if(Arrays.stream(lines).anyMatch(line -> line.isOnLineWithBoundary(new Point(finalX, rasterHeight - finalY)))) {
                    raster.setSample(x, y, 0, 0);
                }
            }
        }
    }

    private void drawRectangleIntoRaster() {
        for(int y = 0; y < this.rasterHeight; y++) {
            for(int x = 0; x < this.rasterWidth; x++) {
                if(isOnRectangleBorder(new Point(x, rasterHeight - y))) {
                    raster.setSample(x, y, 0, 0);
                }
            }
        }
    }

    private boolean isOnRectangleBorder(Point p) {
        if (!clipRectangle.contains(p) && !clipRectangle.contains(p.x - 1, p.y - 1)) return false;
        return p.x == clipRectangle.getMaxX() || p.x == clipRectangle.getMinX() || p.y == clipRectangle.getMaxY() || p.y == clipRectangle.getMinY();
    }


    private void displayPanel(BufferedImage image) {
        JLabel label = new JLabel(new ImageIcon(image));
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(label);
        frame.pack();
        frame.setVisible(true);
    }
}
