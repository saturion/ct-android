package com.bitjockey.colorthief;

import com.bitjockey.colorthief.MMCQ.CMap;
import android.graphics.Bitmap;
import java.util.Arrays;



public class ColorThief {

    private static final int DEFAULT_QUALITY = 10;
    private static final boolean DEFAULT_IGNORE_WHITE = true;


    /**
     * Use the median cut algorithm to cluster similar colors.
     *
     * @param sourceImage
     *            the source image
     * @param colorCount
     *            the size of the palette; the number of colors returned (minimum 2, maximum 256)
     *
     * @return the color map
     */
    public static CMap getColorMap(Bitmap sourceImage, int colorCount) {
        return getColorMap(sourceImage, colorCount, DEFAULT_QUALITY, DEFAULT_IGNORE_WHITE);
    }

    /**
     * Use the median cut algorithm to cluster similar colors.
     *
     * @param sourceImage
     *            the source image
     * @param colorCount
     *            the size of the palette; the number of colors returned (minimum 2, maximum 256)
     * @param quality
     *            1 is the highest quality settings. 10 is the default. There is a trade-off between
     *            quality and speed. The bigger the number, the faster the palette generation but
     *            the greater the likelihood that colors will be missed.
     * @param ignoreWhite
     *            if <code>true</code>, white pixels are ignored
     *
     * @return the color map
     * @throws IllegalArgumentException
     *             if quality is &lt; 1
     */
    public static CMap getColorMap(
            Bitmap sourceImage,
            int colorCount,
            int quality,
            boolean ignoreWhite) {
        if (colorCount < 2 || colorCount > 256) {
            throw new IllegalArgumentException("Specified colorCount must be between 2 and 256.");
        }
        if (quality < 1) {
            throw new IllegalArgumentException("Specified quality should be greater then 0.");
        }

        int[][] pixelArray;

        pixelArray = getPixelsFast(sourceImage, quality, ignoreWhite);

        // Send array to quantize function which clusters values using median cut algorithm
        CMap cmap = MMCQ.quantize(pixelArray, colorCount);
        return cmap;
    }

    public static int packColor(int[] components) {
        return (255 << 24) | (components[0] << 16) | (components[1] << 8) | components[2];
    }

    public static int packVBox(MMCQ.VBox box) {
        return Colorizr.packColor(box.avg(false));
    }

    public static int getRed(int col) {
        return (col >> 16) & 0xff;
    }

    public static float getRedF(int col) {
        return (float)((col >> 16) & 0xff);
    }

    public static int getGreen(int col) {
        return (col >> 8) & 0xff;
    }

    public static float getGreenF(int col) {
        return (float)((col >> 8) & 0xff);
    }

    public static int getBlue(int col) {
        return (col) & 0xff;
    }

    public static float getBlueF(int col) {
        return (float)((col) & 0xff);
    }


    /**
     * Gets the image's pixels via BufferedImage.getRaster().getDataBuffer(). Fast, but doesn't work
     * for all color models.
     *
     * @param sourceImage
     *            the source image
     * @param quality
     *            1 is the highest quality settings. 10 is the default. There is a trade-off between
     *            quality and speed. The bigger the number, the faster the palette generation but
     *            the greater the likelihood that colors will be missed.
     * @param ignoreWhite
     *            if <code>true</code>, white pixels are ignored
     *
     * @return an array of pixels (each an RGB int array)
     */
    private static int[][] getPixelsFast(
            Bitmap sourceImage,
            int quality,
            boolean ignoreWhite) {

        int[] rgbPixels = new int[sourceImage.getHeight() * sourceImage.getWidth()];
        sourceImage.getPixels(rgbPixels, 0, sourceImage.getWidth(), 0, 0, sourceImage.getWidth(), sourceImage.getHeight());
        int pixelCount = sourceImage.getWidth() * sourceImage.getHeight();

        int expectedDataLength = sourceImage.getWidth() * sourceImage.getHeight();
        if (expectedDataLength != rgbPixels.length) {
            throw new IllegalArgumentException(
                    "(expectedDataLength = " + expectedDataLength + ") != (pixels.length = "
                            + rgbPixels.length + ")");
        }

        // Store the RGB values in an array format suitable for quantize function

        // numRegardedPixels must be rounded up to avoid an ArrayIndexOutOfBoundsException if all
        // pixels are good.
        int numRegardedPixels = (pixelCount + quality - 1) / quality;

        int numUsedPixels = 0;
        int[][] pixelArray = new int[numRegardedPixels][];
        int r, g, b, a;

        for (int i = 0; i < pixelCount; i += quality) {
            a = rgbPixels[i] >> 24 & 0xFF;
            b = rgbPixels[i] & 0xFF;
            g = rgbPixels[i] >> 8 & 0xFF;
            r = rgbPixels[i] >> 16 & 0xFF;

            // If pixel is mostly opaque and not white
            if (a >= 125 && !(ignoreWhite && r > 250 && g > 250 && b > 250)) {
                pixelArray[numUsedPixels] = new int[] {r, g, b};
                numUsedPixels++;
            }
        }

        // Remove unused pixels from the array
        return Arrays.copyOfRange(pixelArray, 0, numUsedPixels);
    }

}
