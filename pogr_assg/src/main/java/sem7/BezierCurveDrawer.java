package sem7;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;

public class BezierCurveDrawer {

    private WritableRaster raster;
    private int width;
    private int height;

    private Point[] bezierPoints;
    private int numberOfBezierPoints;
    private final double T_SENSITIVITY = 0.05;       // 1 / 0.1 ... 10 kroku
    private Point[] curvePoints;

    void main(String[] args) {
        if (args.length % 2 != 1) {
            System.out.println("Wrong number of arguments, usage:\njava sem7.BezierCurveDrawer.java  <C>asteljau OR <D>egreeElevation <x1> <y1> <x2> <y2> ... <xn> <yn> coordinates of the Bezier points");
            return;
        }

        // TODO check ze to je mozny vykreslit

        this.numberOfBezierPoints = (args.length - 1) / 2;
        initializePoints(Arrays.stream(args).skip(1).toArray(String[]::new));

        switch (args[0]) {
            case "C":
                SwingUtilities.invokeLater(this::drawCurveCasteljau);
                break;
            case "D":
                SwingUtilities.invokeLater(this::drawCurveDegreeElevation);
                break;
            default:
                System.out.println("Wrong arguments, usage:\njava sem7.BezierCurveDrawer.java  <C>asteljau OR <D>egreeElevation <x1> <y1> <x2> <y2> ... <xn> <yn> coordinates of the Bezier points");
        }
    }

    private void initializePoints(String[] points) {
        this.bezierPoints = new Point[numberOfBezierPoints];
        for (int i = 0; i < points.length; i += 2) {
            bezierPoints[i / 2] = new Point(Integer.parseInt(points[i]), Integer.parseInt(points[i + 1]));
        }
    }

    private void drawCurveDegreeElevation() {
        computeDegreeElevationCurve();
        int widthCurve = Arrays.stream(curvePoints).mapToInt(point -> point.x).max().getAsInt();
        int widthBezier = Arrays.stream(bezierPoints).mapToInt(point -> point.x).max().getAsInt();
        int heightCurve = Arrays.stream(curvePoints).mapToInt(point -> point.y).max().getAsInt();
        int heightBezier = Arrays.stream(bezierPoints).mapToInt(point -> point.y).max().getAsInt();

        int width = Math.max(widthBezier, widthCurve);
        int height = Math.max(heightBezier, heightCurve);
        BufferedImage image = new BufferedImage(width + 50, height + 50, BufferedImage.TYPE_BYTE_BINARY);

        this.raster = image.getRaster();
        this.width = image.getWidth();
        this.height = image.getHeight();

        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                raster.setSample(x, y, 0, 255);
            }
        }

        drawCurveIntoRaster();
        displayPanel(image);
    }

    private void computeDegreeElevationCurve() {
        int numberOfNewBezierPoints = (int) (1 / T_SENSITIVITY);
        int numberOfPoints = numberOfNewBezierPoints + numberOfBezierPoints;
        this.curvePoints = new Point[numberOfPoints];   // 4 + 100
        if (numberOfBezierPoints >= 0) System.arraycopy(bezierPoints, 0, curvePoints, 0, numberOfBezierPoints);
        // c0 = b0, c1=b1 ... cn = bn a zbytek null

        for(int i = 0; i < numberOfNewBezierPoints; i++) {
            Point[] oldCurvePoints = new Point[numberOfBezierPoints + i + 1];
            System.arraycopy(curvePoints, 0, oldCurvePoints, 0, numberOfBezierPoints + i);

            for (int j = 1; j < numberOfBezierPoints + i; j++) {
                double alfa = (double) j / (numberOfBezierPoints + i);
                int newXCoordinate = (int) (oldCurvePoints[j - 1].getX() * alfa + oldCurvePoints[j].getX() * (1 - alfa));
                int newYCoordinate = (int) (oldCurvePoints[j - 1].getY() * alfa + oldCurvePoints[j].getY() * (1 - alfa));

                curvePoints[j] = new Point(newXCoordinate, newYCoordinate);

            }
            curvePoints[numberOfBezierPoints + i] = oldCurvePoints[numberOfBezierPoints + i - 1];
        }
    }

    private void drawCurveCasteljau() {
        computeDeCasteljauCurve();

        int widthCurve = Arrays.stream(curvePoints).mapToInt(point -> point.x).max().getAsInt();
        int widthBezier = Arrays.stream(bezierPoints).mapToInt(point -> point.x).max().getAsInt();
        int heightCurve = Arrays.stream(curvePoints).mapToInt(point -> point.y).max().getAsInt();
        int heightBezier = Arrays.stream(bezierPoints).mapToInt(point -> point.y).max().getAsInt();

        int width = Math.max(widthBezier, widthCurve);
        int height = Math.max(heightBezier, heightCurve);
        BufferedImage image = new BufferedImage(width + 50, height + 50, BufferedImage.TYPE_BYTE_BINARY);

        this.raster = image.getRaster();
        this.width = image.getWidth();
        this.height = image.getHeight();

        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                raster.setSample(x, y, 0, 255);
            }
        }

        drawCurveIntoRaster();
        displayPanel(image);
    }

    private void drawCurveIntoRaster() {
        for (Point curvePoint : curvePoints) {
            raster.setSample(curvePoint.x, curvePoint.y, 0, 0);
        }
        for (Point bezierPoint : bezierPoints) {
            for (int i = -3; i < 3; i++) {
                for (int j = -3; j < 3; j++) {
                    raster.setSample(bezierPoint.x + i, bezierPoint.y + j, 0, 0);
                }
            }
        }
    }

    private void computeDeCasteljauCurve() {
        // { {b0 b1 b2 ... bn} { b0 b1 b2...  bn} ... }
        // Point[0] = pole pomocnych bodu pro prvni bezieruv bod
        Point[][] helperBezierPoints = new Point[numberOfBezierPoints][numberOfBezierPoints];
        this.curvePoints = new Point[(int) (1 / T_SENSITIVITY)];

        for (int k = 0; k < (1 / T_SENSITIVITY); k++) {
            double t = T_SENSITIVITY * (k + 1);
            for (int i = 0; i < numberOfBezierPoints; i++) {
                for (int j = 0; j < numberOfBezierPoints; j++) {
                    helperBezierPoints[i][j] = new Point(0, 0);
                }
                helperBezierPoints[i][0] = bezierPoints[i];
            }

            for (int j = 1; j < numberOfBezierPoints; j++) {
                for (int i = j; i < numberOfBezierPoints; i++) {
                    int newXCoordinate = (int) (helperBezierPoints[i - 1][j - 1].getX() * (1 - t) + helperBezierPoints[i][j - 1].getX() * t);
                    int newYCoordinate = (int) (helperBezierPoints[i - 1][j - 1].getY() * (1 - t) + helperBezierPoints[i][j - 1].getY() * t);
                    helperBezierPoints[i][j].move(newXCoordinate, newYCoordinate);
                }
            }
            curvePoints[k] = helperBezierPoints[numberOfBezierPoints - 1][numberOfBezierPoints - 1];
        }
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
