package sem4;

import utils.ImageReaderWriter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.Arrays;

import static java.lang.Math.*;


public class AntiAliaser {
    private final int SUPERSAMPLE_COEFFICIENT_X = 2;
    private final int SUPERSAMPLE_COEFFICIENT_Y = 2;
    private final int NUMBER_OF_SAMPLES = 4;
    private final int ROTATION_ANGLE = 30;

    private int originalHeight;
    private int originalWidth;
    private WritableRaster originalImage;

    private int newHeight;
    private int newWidth;
    private BufferedImage newImage;


    void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Wrong number of arguments, usage:\njava sem4.AntiAliaser.java <rnd> or <rot> <newWidth> <newHeight> /path/to/highres/image");
            return;
        }

        ImageReaderWriter imageReaderWriter = new ImageReaderWriter();
        BufferedImage image;
        try {
            image = imageReaderWriter.readImage(args[3]);
        } catch (IOException e) {
            System.out.println("Error reading image " + args[3]);
            return;
        }
        this.originalHeight = image.getHeight();
        this.originalWidth = image.getWidth();
        this.originalImage = image.getRaster();

        this.newHeight = Integer.parseInt(args[2]);
        this.newWidth = Integer.parseInt(args[1]);

        BufferedImage antiAliasedImage = null;

        switch (args[0].toLowerCase()) {
            case "rnd":
                antiAliasedImage = antiAliaseRandomGrid(createAlias(image, newWidth, newHeight));
                break;
            case "rot":
                antiAliasedImage = antiAliaseRotatedGrid(createAlias(image, newWidth, newHeight));
                break;
            default:
                System.out.println("Wrong argument " + args[0]);
                System.out.println("usage:\njava sem4.AntiAliaser.java <rnd> or <rot> /path/to/aliased/image");
                return;
        }

        try {
            imageReaderWriter.writeImage(antiAliasedImage);
        } catch (IOException e) {
            System.out.println("Error writing image");
        }
    }

    private BufferedImage createAlias(BufferedImage original, int newWidth, int newHeight) {
        BufferedImage aliasedImage = new BufferedImage(newWidth, newHeight, original.getType());
        Graphics2D g = aliasedImage.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);

        g.drawImage(original, 0, 0, newWidth, newHeight, null);
        g.dispose();
        return aliasedImage;
    }

    private BufferedImage antiAliaseRandomGrid(BufferedImage image) {

        int[][] supersampledPixels = new int[SUPERSAMPLE_COEFFICIENT_X * SUPERSAMPLE_COEFFICIENT_Y][4];

        WritableRaster raster = image.getRaster();
        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                // tady jsme na levelu pixelu a jedeme pres nx ny subpixely v tom 1 pixelu
                for (int ny = 0; ny < SUPERSAMPLE_COEFFICIENT_Y; ny++) {
                    for (int nx = 0; nx < SUPERSAMPLE_COEFFICIENT_X; nx++) {
                        // TODO: predelat koeficienty aby se propocitavaly podle poctu supersamplu
                        double xPosition = random();
                        double yPosition = random();
                        // na x y pozici se podivam v puvodnim obrazku
                        supersampledPixels[ny * SUPERSAMPLE_COEFFICIENT_Y + nx] = superSample(xPosition + x, yPosition + y);
                    }
                }
                // vsechny subpixely jsou projete... udelam aritmeticky prumer a vysledek zapisu
                raster.setPixel(x, y, averagePixels(supersampledPixels));
            }
        }
        return image;
    }

    private BufferedImage antiAliaseRotatedGrid(BufferedImage image) {
        /*
            nejdriv Grid potom se otoci
            Puvodni obrazek navzorkuju s vetsi frekvenci ... roughly vezmu pixel v origo kam mi padne sample
            otocim tak o 30 stupnu? 45 to pada na hranu
            pixel 1 x 1... tak by grid padl na 0,25 0,25    0,75 0.25
                                               0,75 0.25    0.75 0.75
        */
        // { {pixel1}, {pixel2}, {pixel3}, {pixel4} } ... pocet pixelu = nx * nx
        int[][] supersampledPixels = new int[SUPERSAMPLE_COEFFICIENT_X * SUPERSAMPLE_COEFFICIENT_Y][4];

        WritableRaster raster = image.getRaster();
        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                // tady jsme na levelu pixelu a jedeme pres nx ny subpixely v tom 1 pixelu
                for (int ny = 0; ny < SUPERSAMPLE_COEFFICIENT_Y; ny++) {
                    for (int nx = 0; nx < SUPERSAMPLE_COEFFICIENT_X; nx++) {
                        // TODO: predelat koeficienty aby se propocitavaly podle poctu supersamplu
                        //grid
                        double xPositionGrid = 0.25 + nx * (0.5);
                        double yPositionGrid = 0.25 + ny * (0.5);
                        // orotovany grid
                        double xPositionRotated = calculateRotation(xPositionGrid, yPositionGrid, 0.5, 0.5)[0];
                        double yPositionRotated = calculateRotation(xPositionGrid, yPositionGrid, 0.5, 0.5)[1];
                        // na x y pozici se podivam v puvodnim obrazku
                        supersampledPixels[ny * SUPERSAMPLE_COEFFICIENT_Y + nx] = superSample(xPositionRotated + x, yPositionRotated + y);
                    }
                }
                // vsechny subpixely jsou projete... udelam aritmeticky prumer a vysledek zapisu
                raster.setPixel(x, y, averagePixels(supersampledPixels));
            }
        }
        return image;
    }

    // potrebuju pozici pixelu a pozici V pixelu
    private int[] superSample(double xPosition, double yPosition) {
        double ratioWidth = (double) this.originalWidth / this.newWidth;
        double ratioHeight = (double) this.originalHeight / this.newHeight;

        // pozice v obrazku = ratio * pozice, jak zaokrouhlit?
        int[] pixel = new int[4];
        originalImage.getPixel((int) (xPosition * ratioWidth), (int) (yPosition * ratioHeight), pixel);
        return pixel;
    }

    private int[] averagePixels(int[][] supersampledPixels) {
        int[] pixels = new int[4];
        for (int i = 0; i < 3; i++) {
            int sum = 0;
            for (int j = 0; j < SUPERSAMPLE_COEFFICIENT_Y * SUPERSAMPLE_COEFFICIENT_X; j++) {
                sum += supersampledPixels[j][i];
            }
            pixels[i] = sum / (SUPERSAMPLE_COEFFICIENT_Y * SUPERSAMPLE_COEFFICIENT_X);
        }
        return pixels;
    }

    private double[] calculateRotation(double oldXPosition, double oldYPosition, double pixelCenterX,  double pixelCenterY) {
        // posuneme aby nebyly zaporny
        double xRelative = oldXPosition - pixelCenterX;
        double yRelative = oldYPosition - pixelCenterY;
        double xRotated = xRelative * cos(Math.toRadians(ROTATION_ANGLE)) - yRelative * sin(Math.toRadians(ROTATION_ANGLE));
        double yRotated = xRelative * sin(Math.toRadians(ROTATION_ANGLE)) + yRelative * cos(Math.toRadians(ROTATION_ANGLE));
        return new double[]{pixelCenterX + xRotated, pixelCenterY + yRotated};
    }
}
