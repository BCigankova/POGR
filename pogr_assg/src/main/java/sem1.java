import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import static java.lang.Math.abs;

public final double GRAYSCALE_RED_COEF = 0.299;
public final double GRAYSCALE_GREEN_COEF = 0.587;
public final double GRAYSCALE_BLUE_COEF = 0.114;



void main(String[] args) {
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


    switch (option.toLowerCase()) {
        case "g":
            image = convertToGrayScale(image);
            break;
        case "d":
            image = desaturate(image, Double.parseDouble(desaturationParameter));
            break;
        case "e":
            image = equalize(image);
            break;
        default:
            IO.println("Unrecognized option\nUsage: option parameter (for desaturation) /path/to/image\ng - convert image to grayscale\nd <number> - desaturate image with parameter number\ne - equalize saturation ");

    }

    try {
        String[] splitName = pathToImage.split("\\.");
        String newName = splitName[0] + "modified." + splitName[1];

        ImageIO.write(image, format.toLowerCase(), new File(newName));
    } catch (Exception e) {
        System.out.println("Unable to write image");
    }
}

BufferedImage convertToGrayScale(BufferedImage image) {
    WritableRaster raster = image.getRaster();

    int width = raster.getWidth();
    int height = raster.getHeight();

    int[] pixelColors = new int[4];

    for(int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
            raster.getPixel(x, y, pixelColors);
            int gray = (int) (GRAYSCALE_RED_COEF * pixelColors[0] + GRAYSCALE_GREEN_COEF * pixelColors[1] + GRAYSCALE_BLUE_COEF * pixelColors[2]);
            pixelColors = new int[]{gray, gray, gray, pixelColors[3]};
            raster.setPixel(x, y, pixelColors);
        }
    }

    return image;
}

BufferedImage desaturate(BufferedImage image, double desaturationParameter) {
    WritableRaster raster = image.getRaster();

    int width = raster.getWidth();
    int height = raster.getHeight();

    int[] pixelColors = new int[4];

    for(int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
            raster.getPixel(x, y, pixelColors);
            int gray = (int) (GRAYSCALE_RED_COEF * pixelColors[0] + GRAYSCALE_GREEN_COEF * pixelColors[1] + GRAYSCALE_BLUE_COEF * pixelColors[2]);
            pixelColors[0] = (int) ((1 - desaturationParameter) * gray + desaturationParameter * pixelColors[0]);
            pixelColors[1] = (int) ((1 - desaturationParameter)  * gray + desaturationParameter * pixelColors[1]);
            pixelColors[2] = (int) ((1 - desaturationParameter) * gray + desaturationParameter * pixelColors[2]);
            raster.setPixel(x, y, pixelColors);
        }
    }
    return image;
}

BufferedImage equalize(BufferedImage image) {
    /*
     <76, 47, 69>   -> <13, 13, 13>    vzdalenost: <63, 34, 56>
     <26, 243, 65>  ->  <54, 54, 54>    vzdalenost: <28, 189, 11>    abs hodnoty?
     ...
                                        prumerna vzdalenost: <63+28/pocet_pixelu = 35,5, 34+189/...>
                                        vysl <35,5 + gray, ...>
     */

    WritableRaster raster = image.getRaster();

    int width = raster.getWidth();
    int height = raster.getHeight();

    int[] pixelColors = new int[4];
    int[][] pixelColorsDistances = new int[width * height][4];

    for(int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
            raster.getPixel(x, y, pixelColors);
            int gray = (int) (GRAYSCALE_RED_COEF * pixelColors[0] + GRAYSCALE_GREEN_COEF * pixelColors[1] + GRAYSCALE_BLUE_COEF * pixelColors[2]);
            pixelColorsDistances[height * y + x][0] = abs(pixelColors[0] - gray);
            pixelColorsDistances[height * y + x][1] = abs(pixelColors[1] - gray);
            pixelColorsDistances[height * y + x][2] = abs(pixelColors[2] - gray);
        }
    }

    int[] averageDistances = Arrays.stream(Arrays.stream(pixelColorsDistances)
                    .reduce(new int[]{0, 0, 0}, (pixelColor1, pixelColor2) -> new int[]{pixelColor1[0] + pixelColor2[0], pixelColor1[1] + pixelColor2[1], pixelColor1[2], pixelColor2[2]}))
                    .map(pixelColor -> pixelColor/(width * height)).toArray();

    for(int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
            raster.getPixel(x, y, pixelColors);
            int gray = (int) (GRAYSCALE_RED_COEF * pixelColors[0] + GRAYSCALE_GREEN_COEF * pixelColors[1] + GRAYSCALE_BLUE_COEF * pixelColors[2]);
            pixelColors[0] = averageDistances[0] + gray;
            pixelColors[1] = averageDistances[1] + gray;
            pixelColors[2] = averageDistances[2] + gray;
            raster.setPixel(x, y, pixelColors);
        }
    }
    return image;
}
