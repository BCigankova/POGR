package sem8;

import utils.ImageReaderWriter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;


public class polygonFiller {

    private final int BLACK = 0;

    private int width;
    private int height;
    private WritableRaster raster;
    private Stack<Point> seedStack;

    void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Wrong number of arguments, usage:\njava sem8.polygonFiller.java  path/to/polygon");
            return;
        }

        ImageReaderWriter imageReaderWriter = new ImageReaderWriter();
        BufferedImage image;
        try {
            image = imageReaderWriter.readImage(args[0]);
        } catch (IOException e) {
            System.out.println("Error reading image from file: " + args[0]);
            return;
        }

        this.raster = image.getRaster();
        this.height = raster.getHeight();
        this.width = raster.getWidth();

        fillOut(raster);

        try {
            imageReaderWriter.writeImage(image);
        } catch (IOException e) {
            System.out.println("Error writing image to file");
        }
    }

    private void fillOut(WritableRaster raster) {
        /*
        najde seed uvnitr pomoci scanline vlevo nahore
        pojede doprava dokud nenarazi na cernou
        zapamatuje si delky
        pak pojede o jedno niz (pokud tam neni hranice) coz je novy seed
        takhle pujde dokud bude moct dolu
         */

        // pocatecni seed, mel by byt nejlevejsi nejhorejsi nezabarveny pixel
        Point seed = findStartingSeed(0);
        if (seed == null) {
            //konec alg, neni nic uvnitr k vyplneni
            return;
        }

        this.seedStack = new Stack<>();
        seedStack.push(seed);

        while (!seedStack.isEmpty()) {
            Point point = seedStack.pop();
            int leftmostX =  (int) point.getX();


            for (; leftmostX > 0 && !isBlack(leftmostX, point.y); leftmostX--) {
                raster.setPixel(leftmostX, point.y, new int[]{65535, 0, 0});
            }
            // musim se vratit protoze jsem na cerny
            leftmostX++;

            int rightmostX = point.x + 1;
            for(; rightmostX < width && !isBlack(rightmostX, point.y); rightmostX++) {
                raster.setPixel(rightmostX, point.y, new int[]{65535, 0, 0});
            }
            rightmostX--;

            // nova seminka pod tim co jsme vyplnili pro kazdy intercept na tom levelu
            addSeedsBelowLevel(point.y + 1, leftmostX, rightmostX);
        }

    }

    private boolean isBlack(int x, int y) {
        int[] rgb = new int[3];
        raster.getPixel(x, y, rgb);
        System.out.println("rgb: " + rgb[0] + ", " + rgb[1] + ", " + rgb[2]);
        return (rgb[0] == 0 && rgb[1] == 0 && rgb[2] == 0);
    }

    private void addSeedsBelowLevel(int level, int leftMostX, int rightMostX) {
        int x = leftMostX;
        while(x < rightMostX) {
            if(raster.getSample(x, level, 0) != BLACK) {
                seedStack.push(new Point(x, level));
                for(; raster.getSample(x, level, 0) != BLACK; x++);
                // ted jsem na konci bile plochy x = cerna
            }
            else {
                x++;
            }
        }
    }

    private Point findStartingSeed(int startY) {
        for (int y = startY; y < height; y++) {
            ArrayList<Point> intercepts = scanLine(y);
            if (!intercepts.isEmpty() && intercepts.get(1) != null) {
                return new Point(intercepts.getFirst().x + 1, intercepts.getFirst().y + 1);
            }
        }
        return null;
    }

    private ArrayList<Point> scanLine(int level) {
        ArrayList<Point> lineIntercepts = new ArrayList<>();
        for (int x = 0; x < width - 1; x++) {
            int sample = raster.getSample(x, level, 0);
            int nextSample = raster.getSample(x + 1, level, 0);

            // dostaneme se na hranici cerne a necerne (seda atd... antialiasing hran)
            if (sample == BLACK && nextSample != BLACK) {
                lineIntercepts.add(new Point(x, level));
                lineIntercepts.add(findAreaEnd(x, level));
            }
        }
        return lineIntercepts;
    }

    private Point findAreaEnd(int x, int level) {
        for (int newX = x; newX < width - 1; newX++) {
            int sample = raster.getSample(newX, level, 0);
            int nextSample = raster.getSample(newX + 1, level, 0);
            // hledam kde konci bila plocha
            if (sample != BLACK && nextSample == BLACK) {
                return new Point(newX, level);
            }
        }
        return null;
    }
}
