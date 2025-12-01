package sem6;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class BresenhamCircleDrawer {
    private int xCenterCoordinate;
    private int yCenterCoordinate;
    private int radius;
    private int width;
    private int height;
    private BufferedImage image;
    private WritableRaster raster;

    void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Wrong number of arguments, usage:\njava sem6.BresenhamCircleDrawer.java <radius> in pixels <xCenterCoordinate> <yCenterCoordinate>");
            return;
        }

        // TODO check ze to je mozny vykreslit

        this.radius = Integer.parseInt(args[0]);
        this.xCenterCoordinate = Integer.parseInt(args[1]);
        this.yCenterCoordinate = Integer.parseInt(args[2]);

        SwingUtilities.invokeLater(this::drawCircle);
    }

    private void drawCircle() {
        this.image = new BufferedImage(this.radius * 2, this.radius * 2, BufferedImage.TYPE_BYTE_BINARY);
        this.raster = image.getRaster();
        this.width = image.getWidth();
        this.height = image.getHeight();

        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                raster.setSample(x, y, 0, 255);
            }
        }

        drawBresenhamCircle();
        displayPanel();
    }

    private void drawBresenhamCircle() {
        int x = 0;
        int y = radius - 1;
        int xToDraw;
        int yToDraw;
        int currentPrediction = 1 - radius;

        try {
            raster.setSample(x, radius - y, 0, 0);
        } catch (Exception e) {
            System.out.println("outta bounds:(");
        }

        while (x < xCoordinate(45)) {
            if (currentPrediction <= 0) {
                currentPrediction = currentPrediction + 2 * x + 3;
            } else {
                currentPrediction = currentPrediction + 2 * x + 3 - 2 * y + 2;
                y = y - 1;
            }
            x = x + 1;
            System.out.println("x: " + x);
            System.out.println("y: " + y);
            for (int i = 0; i < 8; i++) {
                xToDraw = translateToOctant(x, y, i)[0];
                yToDraw = translateToOctant(x, y, i)[1];
                try {
                    raster.setSample(xToDraw + xCenterCoordinate + radius, radius - yToDraw - yCenterCoordinate, 0, 0);
                } catch (Exception e) {
                    System.out.println("outta bounds:(");
                }
            }
        }
    }

    private int[] translateToOctant(int x, int y, int octant) {
        return switch (octant) {
            case 1 -> new int[]{y, x};
            case 2 -> new int[]{-y, x};
            case 3 -> new int[]{-x, y};
            case 4 -> new int[]{-x, -y};
            case 5 -> new int[]{-y, -x};
            case 6 -> new int[]{y, -x};
            case 7 -> new int[]{x, -y};
            default -> new int[]{x, y};
        };
    }

    private void displayPanel() {
        JLabel label = new JLabel(new ImageIcon(image));
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(label);
        frame.pack();
        frame.setVisible(true);
    }

    private double xCoordinate(int degree) {
        return radius * Math.sin(Math.toRadians(degree));
    }

    private double yCoordinate(int degree) {
        return radius * Math.cos(Math.toRadians(degree));
    }
}
