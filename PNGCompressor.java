import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class PNGCompressor {
    public static void main(String[] args) {
        String inputFolder = ".";
        String outputFolder = "compressed_images";

        File inputDir = new File(inputFolder);
        File outputDir = new File(outputFolder);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File[] files = inputDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        if (files != null) {
            for (File file : files) {
                try {
                    BufferedImage image = ImageIO.read(file);
                    long originalSize = file.length();

                    byte[] compressedData = compressImage(image);
                    if (compressedData.length < originalSize) {
                        File compressedFile = new File(outputDir, file.getName());
                        saveImageWithoutMetadata(image, compressedFile);
                        System.out.println("Saved: " + file.getName() + " (reduced by " + (originalSize - compressedData.length) + " bytes)");
                    } else {
                        System.out.println("Skipping: " + file.getName() + " (not smaller " + (originalSize - compressedData.length) + " bytes bigger)");
                    }
                } catch (IOException e) {
                    System.err.println("Error processing file: " + file.getName());
                    e.printStackTrace();
                }
            }
        }
    }

    private static byte[] compressImage(BufferedImage image) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
        if (!writers.hasNext()) {
            throw new IOException("No PNG writers available");
        }
        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            writer.setOutput(ios);
            writer.write(null, new javax.imageio.IIOImage(image, null, null), param);
            writer.dispose();
            return baos.toByteArray();
        }
    }

    private static void saveImageWithoutMetadata(BufferedImage image, File output) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
        if (!writers.hasNext()) {
            throw new IOException("No PNG writers available");
        }
        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        IIOMetadata metadata = writer.getDefaultImageMetadata(new javax.imageio.ImageTypeSpecifier(image), param);
        
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(output)) {
            writer.setOutput(ios);
            writer.write(null, new javax.imageio.IIOImage(image, null, metadata), param);
        } finally {
            writer.dispose();
        }
    }
}
