package sem2;

import utils.ImageReaderWriter;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;

import static java.lang.Math.pow;

public class ImageColorRepresentationConverter {

    private int height;
    private int width;
    private int redBits;
    private int greenBits;
    private int blueBits;

    void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Wrong number of arguments, usage:\njava sem2.ImageColorRepresentationConverter.java <Rbits> <Gbits> <Bbits> /path/to/32bpp/image");
        }

        try {
            this.redBits = Integer.parseInt(args[0]);
            this.greenBits = Integer.parseInt(args[1]);
            this.blueBits = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            System.out.println("Wrong arguments, usage:\njava sem2.ImageColorRepresentationConverter.java <Rbits> <Gbits> <Bbits> /path/to/32bpp/image");
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
        this.height = image.getHeight();
        this.width = image.getWidth();

        // tady se to deje, ostatni je I/O omacka
        BufferedImage newImage = convertColorRepresentationToPalette(image);

        try {
            imageReaderWriter.writeImage(newImage);
        } catch (IOException e) {
            System.out.println("Error writing image " + image);
        }
    }

    private BufferedImage convertColorRepresentationToPalette(BufferedImage original) {

        /*
        r stara: 0 - 255 (11111111)2 (8bitu)

        r nova: 0 - 2^rbits (tolik bitu kolik zadam na cervenou)

        paleta 16*16 = 256 barev... index 0-255 pro bavry ... 111 111 11     8 bitu na index

        pixelBuffer[0] = cervena.... red * 8 / 256... red * 2^rbits / pocet_barev
         */

        // vytvorim uplne novy obrazek typovany jako indexed od zacatku s objektem palety
        IndexColorModelPaletteDisplayer paletteDisplayer = new IndexColorModelPaletteDisplayer();
        BufferedImage image = new BufferedImage(width,
                height,
                BufferedImage.TYPE_BYTE_INDEXED,
                paletteDisplayer.computePalette(redBits, greenBits, blueBits));
        WritableRaster raster = image.getRaster();
        WritableRaster originalRaster = original.getRaster();

        int[] pixelBuffer = new int[4];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                originalRaster.getPixel(x, y, pixelBuffer);
                pixelBuffer[0] = (pixelBuffer[0] * (int) pow(2, redBits)) / paletteDisplayer.getNUMBER_OF_COLORS();
                pixelBuffer[1] = (pixelBuffer[1] * (int)  pow(2, greenBits)) / paletteDisplayer.getNUMBER_OF_COLORS();
                pixelBuffer[2] = (pixelBuffer[2] * (int)  pow(2, blueBits)) / paletteDisplayer.getNUMBER_OF_COLORS();

                // ted mam pixel kde jsou prepocitane hodnoty na nove 8bpp, to je index v palete, red = vysoke bity, green stredni blue nizke
                //red: 111 111 11 ... 0000 0111 bit posun doprava o 8-3 ... 5
                //green: 0001 1100 ... posun o bluebits
                raster.setSample(x, y, 0, (pixelBuffer[0] << paletteDisplayer.getNUMBER_OF_BITS() - redBits) + (pixelBuffer[1] << blueBits ) + pixelBuffer[2]);
            }
        }
        return image;
    }
}