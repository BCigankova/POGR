package sem5;

import utils.ImageReaderWriter;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;

public class BasicImageColorTransformer {
    private int width;
    private int height;
    private int[] histogram;

    void main(String[] args) {
        if (args.length != 3 && !args[0].equalsIgnoreCase("e")) {
            System.out.println(args[0].equalsIgnoreCase("e"));
            System.out.println("Wrong number of arguments, usage:\njava sem5.BasicImageColorTransformer.java  <l>uminosity <byHowMuch> from -1 to 1 OR <c>ontrast <byHowMuch> OR <t>reshold <whereToTreshold> from -1 to 1  OR <g>amma correction <gammaValue> 0 - inf OR <e>qualize histogram  /path/to/image");
            return;
        }

        ImageReaderWriter imageReaderWriter = new ImageReaderWriter();
        BufferedImage original;
        try {
            original = imageReaderWriter.readImage(args[2]);
        } catch (ArrayIndexOutOfBoundsException e) {
            try {
                original = imageReaderWriter.readImage(args[1]);
            } catch (IOException e1) {
                System.out.println("Error reading image " + args[1]);
                return;
            }
        } catch (IOException e) {
            System.out.println("Error reading image " + args[2]);
            return;
        }

        this.width = original.getWidth();
        this.height = original.getHeight();
        BufferedImage transformedImage;

        switch (args[0].toLowerCase()) {
            case "l":
                transformedImage = changeLuminosity(original, Double.parseDouble(args[1]));
                break;
            case "c":
                transformedImage = changeContrast(original, Double.parseDouble(args[1]));
                break;
            case "t":
                transformedImage = treshold(original, Double.parseDouble(args[1]));
                break;
            case "g":
                transformedImage = gammaCorrect(original, Double.parseDouble(args[1]));
                break;
            case "e":
                transformedImage = equalizeHistogram(original);
                break;
            default:
                System.out.println("Wrong argument " + args[0]);
                System.out.println("usage:\njava sem4.AntiAliaser.java <rnd> or <rot> /path/to/aliased/image");
                return;
        }

        try {
            imageReaderWriter.writeImage(transformedImage);
        } catch (IOException e) {
            System.out.println("Error writing image");
        }
    }

    private BufferedImage changeLuminosity(BufferedImage original, double byHowMuch) {
        /*
        pvuodni primka y=x, nova y = x + parametr
        nova barva = stara + parametr NEBO 255 co bude mensi
        -1 = -255, 1 = 255, 0.5 = 127,5... atd
         */
        int parameter = (int) (255 * byHowMuch);
        WritableRaster raster = original.getRaster();
        int[] pixel = new int[4];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                raster.getPixel(x, y, pixel);
                pixel[0] = decideColor(pixel[0] + parameter);
                pixel[1] = decideColor(pixel[1] + parameter);
                pixel[2] = decideColor(pixel[2] + parameter);
                raster.setPixel(x, y, pixel);
            }
        }
        return original;
    }

    private int decideColor(int color) {
        if (color > 255) {
            return 255;
        } else if (color < 0) {
            return 0;
        }
        return color;
    }

    private BufferedImage changeContrast(BufferedImage original, double byHowMuch) {
        /*
        y=x na y=ax, zmena rotace
        -1 obraceny kontrast jen 1 hodnota, 1 to same ... jako a = oo
        1... 90, 0... 45, -1... 0, 0.5... 45 + (45 * 0.5), -0.5 ... 45 + (45 * -0.5) uhel pro parametr
        jeste posun na stred... y = (tan uhel) x + 127

         */
        double slope = Math.tan(Math.toRadians(45 + (byHowMuch * 45)));
        if (slope == Double.POSITIVE_INFINITY) {
            return treshold(original, 0.5);
        }
        if (slope == Double.NEGATIVE_INFINITY) {
            return treshold(original, -0.5);
        }

        WritableRaster raster = original.getRaster();
        int[] pixel = new int[4];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                raster.getPixel(x, y, pixel);
                pixel[0] = decideColor((int) (pixel[0] * slope) + 127);
                pixel[1] = decideColor((int) (pixel[1] * slope) + 127);
                pixel[2] = decideColor((int) (pixel[2] * slope) + 127);
                raster.setPixel(x, y, pixel);
            }
        }
        return original;

    }

    private BufferedImage treshold(BufferedImage original, double byHowMuch) {
        /*
        howMuch je prah od ktereho se to prahuje * 255
         */
        int treshold = (int) (byHowMuch * 255);
        WritableRaster raster = original.getRaster();
        int[] pixel = new int[4];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                raster.getPixel(x, y, pixel);
                if (treshold >= 0) {
                    pixel[0] = pixel[0] > treshold ? 255 : 0;
                    pixel[1] = pixel[1] > treshold ? 255 : 0;
                    pixel[2] = pixel[2] > treshold ? 255 : 0;
                }
                if (treshold < 0) {
                    pixel[0] = pixel[0] < -treshold ? 255 : 0;
                    pixel[1] = pixel[1] < -treshold ? 255 : 0;
                    pixel[2] = pixel[2] < -treshold ? 255 : 0;
                }
                raster.setPixel(x, y, pixel);
            }
        }
        return original;
    }

    private BufferedImage gammaCorrect(BufferedImage original, double gamma) {
        WritableRaster raster = original.getRaster();
        int[] pixel = new int[4];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                raster.getPixel(x, y, pixel);
                pixel[0] = (int) (255 * Math.pow(((double) pixel[0] / 255), gamma));
                pixel[1] = (int) (255 * Math.pow(((double) pixel[1] / 255), gamma));
                pixel[2] = (int)  (255 * Math.pow(((double) pixel[2] / 255), gamma));
                raster.setPixel(x, y, pixel);
            }
        }
        return original;
    }

    private BufferedImage equalizeHistogram(BufferedImage original) {
        this.histogram = computeImageHistogram(original);
        WritableRaster raster = original.getRaster();
        int[] pixel = new int[4];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                raster.getPixel(x, y, pixel);
                pixel[0] = (int) (255 * computeCumulativeNormalizedHistogram(pixel[0]));
                pixel[1] = pixel[0];
                pixel[2] = pixel[0];
                raster.setPixel(x, y, pixel);
            }
        }
        return original;
    }

    private double computeCumulativeNormalizedHistogram(int color) {
        double sum = 0;
        for(int j = 0; j < color; j++) {
            sum += computeNormalizedHistogram(j);
        }
        return sum;
    }

    private double computeNormalizedHistogram(int color) {
        return (double) histogram[color] / (width * height);
    }

    private int[] computeImageHistogram(BufferedImage image) {
        int[] histogram = new int[256];
        WritableRaster raster = image.getRaster();
        int[] pixel = new int[4];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                raster.getPixel(x, y, pixel);
                histogram[pixel[0]]++;
            }
        }
        return histogram;
    }

}