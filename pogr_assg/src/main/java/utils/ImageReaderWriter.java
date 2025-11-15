package utils;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class ImageReaderWriter {
    private BufferedImage image;
    private String format;
    private String pathToImage;

    /**
     * Reads image located at the path
     *
     * @param pathToImage path to the image
     * @return BufferedImage representation of the image
     */
    public BufferedImage readImage(String pathToImage) throws IOException {
        this.image = ImageIO.read(new File(pathToImage));
        this.pathToImage = pathToImage;
        getFormatFromPath();
        return this.image;
    }

    public void getFormatFromPath() throws IOException {
        Iterator<ImageReader> readers = ImageIO.getImageReaders(ImageIO.createImageInputStream(new File(pathToImage)));
        ImageReader reader = readers.next();
        this.format = reader.getFormatName();
    }

    public void writeImage(BufferedImage image) throws IOException {
        String[] splitName = pathToImage.split("\\.");
        String newName = splitName[0] + "_modified." + splitName[1];
        ImageIO.write(image, format.toLowerCase(), new File(newName));
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }
}