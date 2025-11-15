package sem3;

import utils.ImageReaderWriter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;


// Nepouziva se posledni matice se samymi jednickami, protoze nemame takovou intenzitu
// Reseni: proste se nepouziva
public class MatrixDitherer {
    int height;
    int width;
    int[][] ditherMatrix;
    private final int MATRIX_DIMENSION = 16;

    void main(String[] args) {
        ImageReaderWriter imageRW = new ImageReaderWriter();
        BufferedImage blackNWhite8bppImage;
        try {
            blackNWhite8bppImage = imageRW.readImage(args[0]);
        } catch (Exception ex) {
            System.out.println("Error reading image " + args[0]);
            System.out.println("Usage: java sem3.MatrixDitherer.java path/to/8bpp/blackAndWhite/image");
            return;
        }

        BufferedImage ditheredImage = dither(blackNWhite8bppImage);
        try {
            imageRW.writeImage(ditheredImage);
        } catch (Exception ex) {
            System.out.println("Error writing image");
        }
    }

    private BufferedImage dither(BufferedImage original) {
        /*  novy obrazek bude 16x vetsi ig
            in = sedivy pixel, mezi 0 a max
            1 pixel s random sedivosti => 256 pixelu cernych/bilych
        */

        this.width = original.getWidth();
        this.height = original.getHeight();
        WritableRaster originalRaster = original.getRaster();

        BufferedImage ditheredImage = new BufferedImage(width * MATRIX_DIMENSION, height * MATRIX_DIMENSION, BufferedImage.TYPE_BYTE_BINARY);

        int[] pixels = new int[4];
        int[][] ditherMatrixForIntensity;

        this.ditherMatrix = generateDitherMatrix(MATRIX_DIMENSION);
        System.out.println("Dither Matrix:");
        for(int y =  0; y < MATRIX_DIMENSION; y++) {
            System.out.println(Arrays.toString(this.ditherMatrix[y]));
        }
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                // grayscale ma vsechny tri kanaly stejne, dostanu cislo in, podle nej udelam matici a v novem rasteru zmenim 16 pixelu
                originalRaster.getPixel(x, y, pixels);
                ditherMatrixForIntensity = getDitherMatrixForIntensity(pixels[0]);
                // musim projet pixely od xy do x+16, y+16
                for(int yNew = y * MATRIX_DIMENSION; yNew < (y + 1) * MATRIX_DIMENSION; yNew++) {
                    for(int xNew = x * MATRIX_DIMENSION; xNew < (x + 1) * MATRIX_DIMENSION; xNew++) {
                        if(ditherMatrixForIntensity[yNew - (y * MATRIX_DIMENSION)][xNew - (x * MATRIX_DIMENSION)] == 0)
                            ditheredImage.setRGB(xNew, yNew, Color.black.getRGB());
                        else
                            ditheredImage.setRGB(xNew, yNew, Color.white.getRGB());
                    }
                }
            }
        }
        return ditheredImage;
    }

    private int[][] getDitherMatrixForIntensity(int intensity) {
        int[][] ditherMatrixForIntensity = new int[MATRIX_DIMENSION][MATRIX_DIMENSION];
        for(int y = 0; y < MATRIX_DIMENSION; y++) {
            for(int x = 0; x < MATRIX_DIMENSION; x++) {
                ditherMatrixForIntensity[y][x] = intensity > this.ditherMatrix[y][x] ? 1 : 0;
            }
        }
        return ditherMatrixForIntensity;
    }

    private int[][] generateDitherMatrix(int order) {
        int[][] ditherMatrix = new int[order][order];
        if(order == 1)
            return new int[][]{{0}};

        // rozdelim matici na 4 kvadranty a podle tech jedu
        // I
        for(int y = 0; y < (order / 2); y++) {
            for(int x = 0; x < (order / 2); x++) {
                ditherMatrix[y][x] = 4 * generateDitherMatrix(order / 2)[y][x];
            }
        }
        // II
        for(int y = 0; y < (order / 2); y++) {
            for(int x = (order / 2); x < order; x++) {
                ditherMatrix[y][x] = 4 * generateDitherMatrix(order / 2)[y][x - (order / 2)] + 3 * filledWithOnesMatrix(order / 2)[y][x -  (order / 2)];
            }
        }

        // III
        for(int y = (order / 2); y < order; y++) {
            for(int x = 0; x < (order / 2); x++) {
                ditherMatrix[y][x] = 4 * generateDitherMatrix(order / 2)[y - (order / 2)][x] + 2 * filledWithOnesMatrix(order / 2)[y - (order / 2)][x];
            }
        }

        // IV
        for(int y = (order / 2); y < order; y++) {
            for(int x = (order / 2); x < order; x++) {
                ditherMatrix[y][x] = 4 * generateDitherMatrix(order / 2)[y - (order / 2)][x - (order / 2)] + filledWithOnesMatrix(order / 2)[y - (order / 2)][x - (order / 2)];
            }
        }
        return ditherMatrix;
    }

    private int[][] filledWithOnesMatrix(int order) {
        int[][] matrix = new int[order][order];
        for(int y = 0; y < order; y++){
            for(int x = 0; x < order; x++){
                matrix[x][y] = 1;
            }
        }
        return matrix;
    }
}