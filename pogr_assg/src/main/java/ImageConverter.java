import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

import static java.lang.Math.abs;

/*****************************************************************************************
 // USAGE (unix) //
 // java /path/to/ImageConverter.java <option> <number> /path/to/image //
 // <option> = g for grayscale, d 0.5 for desaturation of 50%, e for saturation equalizer
 // <number> = scale 0 - 1
 // example:  java ImageConverter.java d 0.25 /home/myusername/pictures/cutepicture.jpg
 // output: cutepicture_modified.jpg
 ****************************************************************************************/


public class ImageConverter {

    public final double GRAYSCALE_RED_COEF = 0.299;
    public final double GRAYSCALE_GREEN_COEF = 0.587;
    public final double GRAYSCALE_BLUE_COEF = 0.114;

    private int height;
    private int width;


    void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java ImageConverter.java option <number> /path/to/file");
            return;
        }
        String option = args[0];
        String desaturationParameter = "";
        String pathToImage = args[1];

        if (option.equalsIgnoreCase("d")) {
            desaturationParameter = args[1];
            pathToImage = args[2];
        }

        BufferedImage image;

        try {
            image = ImageIO.read(new File(pathToImage));
        } catch (Exception e) {
            IO.println("Unable to get image - is the path correct?");
            return;
        }

        String format;
        try {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(ImageIO.createImageInputStream(new File(pathToImage)));
            ImageReader reader = readers.next();
            format = reader.getFormatName();
        } catch (Exception e) {
            IO.println("Unknown format");
            return;
        }

        WritableRaster raster = image.getRaster();
        this.height  = raster.getHeight();
        this.width  = raster.getWidth();


        switch (option.toLowerCase()) {
            case "g":
                convertToGrayScale(image.getRaster());
                break;
            case "d":
                desaturate(image.getRaster(), Double.parseDouble(desaturationParameter));
                break;
            case "e":
                equalize(image.getRaster());
                break;
            default:
                IO.println("Unrecognized option\nUsage: option parameter (for desaturation) /path/to/image\ng - convert image to grayscale\nd <number> - desaturate image with parameter number\ne - equalize saturation ");
                return;
        }

        try {
            String[] splitName = pathToImage.split("\\.");
            String newName = splitName[0] + "_modified." + splitName[1];

            ImageIO.write(image, format.toLowerCase(), new File(newName));
        } catch (Exception e) {
            System.out.println("Unable to write image");
        }
    }

    private void convertToGrayScale(WritableRaster raster) {
        int[] pixelColors = new int[4];

        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                raster.getPixel(x, y, pixelColors);
                int gray = getGray(pixelColors);
                pixelColors = new int[]{gray, gray, gray, pixelColors[3]};
                raster.setPixel(x, y, pixelColors);
            }
        }
    }

    private void desaturate(WritableRaster raster, double desaturationParameter) {
        int[] pixelColors = new int[4];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                raster.getPixel(x, y, pixelColors);
                int gray = getGray(pixelColors);
                pixelColors[0] = (int) ((1 - desaturationParameter) * gray + desaturationParameter * pixelColors[0]);
                pixelColors[1] = (int) ((1 - desaturationParameter) * gray + desaturationParameter * pixelColors[1]);
                pixelColors[2] = (int) ((1 - desaturationParameter) * gray + desaturationParameter * pixelColors[2]);
                raster.setPixel(x, y, pixelColors);
            }
        }
    }

    private void equalize(WritableRaster raster) {
    /*
     <76, 47, 69>   -> <13, 13, 13>    vzdalenost: <63, 34, 56>
     <26, 243, 65>  ->  <54, 54, 54>    vzdalenost: <28, 189, 11>    abs hodnoty?
     ...
                                        prumerna vzdalenost: <63+28/pocet_pixelu = 35,5, 34+189/...>
                                        vysl <35,5 + gray, ...>
     */

        int[] pixelColors = new int[4];
        int[][] pixelColorsDistances = new int[width * height][4];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                raster.getPixel(x, y, pixelColors);
                int gray = getGray(pixelColors);
                pixelColorsDistances[height * y + x][0] = abs(pixelColors[0] - gray);
                pixelColorsDistances[height * y + x][1] = abs(pixelColors[1] - gray);
                pixelColorsDistances[height * y + x][2] = abs(pixelColors[2] - gray);
            }
        }

        int[] averageDistances = Arrays.stream(Arrays.stream(pixelColorsDistances)
                        .reduce(new int[]{0, 0, 0}, (pixelColor1, pixelColor2) -> new int[]{pixelColor1[0] + pixelColor2[0], pixelColor1[1] + pixelColor2[1], pixelColor1[2], pixelColor2[2]}))
                .map(pixelColor -> pixelColor / (width * height)).toArray();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                raster.getPixel(x, y, pixelColors);
                int gray = getGray(pixelColors);
                pixelColors[0] = averageDistances[0] + gray;
                pixelColors[1] = averageDistances[1] + gray;
                pixelColors[2] = averageDistances[2] + gray;
                raster.setPixel(x, y, pixelColors);
            }
        }
    }

    private int getGray(int[] pixelColors) {
        return (int) (GRAYSCALE_RED_COEF * pixelColors[0] + GRAYSCALE_GREEN_COEF * pixelColors[1] + GRAYSCALE_BLUE_COEF * pixelColors[2]);
    }
}