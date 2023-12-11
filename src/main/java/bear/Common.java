package bear;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

class Common {

    private Common() {
    }

    /**
     * Java classloader uses null to signal non-existing resource.
     * This is a bit strange for me, since class loading generally throws {@link ClassNotFoundException}.
     * This null special value here is transformed to a {@link NoSuchFileException}
     * to be consistent with {@link Files#newInputStream(Path, OpenOption...)} that is used in this project.
     */
    static InputStream loadResource(String name) throws IOException {
        var in = Common.class.getResourceAsStream(name);
        if (in == null) {
            throw new NoSuchFileException(name);
        }
        return in;
    }

    /**
     * This loads an image from the supplied stream.
     * Only {@link NoSuchFileException} is propagated as checked, because only that case of error is interesting from the example's point of view.
     */
    static BufferedImage loadImage(InputStreamSupplier inSupplier) throws NoSuchFileException {
        try (var in = inSupplier.get()) {
            return ImageIO.read(in);
        } catch (NoSuchFileException e) {
            throw e;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * We do not expect any errors from here,
     * that's why the {@link IOException} is caught here and propagated as {@link UncheckedIOException}.
     */
    static byte[] writeToByteArray(BufferedImage image) {
        var imageStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", imageStream);
            return imageStream.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static BufferedImage concatenateImages(BufferedImage... images) {
        return concatenateImages(Arrays.asList(images));
    }

    /**
     * Concatenates images, each below the other.
     */
    static BufferedImage concatenateImages(List<BufferedImage> images) {
        return images.stream().reduce(
                new ImageConcat(
                        images.stream().mapToInt(BufferedImage::getWidth).max().orElse(0),
                        images.stream().mapToInt(BufferedImage::getHeight).sum()),
                (c, i) -> {
                    c.image().createGraphics().drawImage(i, 0, c.endOfLastImage(), null);
                    return new ImageConcat(c.image(), c.endOfLastImage() + i.getHeight());
                },
                (c1, c2) -> {
                    c1.image().createGraphics().drawImage(c2.image(), 0, c1.endOfLastImage(), null);
                    return new ImageConcat(c1.image(), c1.endOfLastImage() + c2.image().getHeight());
                }).image();
    }

    private record ImageConcat(BufferedImage image, int endOfLastImage) {
        public ImageConcat(int width, int height) {
            this(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), 0);
        }
    }

    static String joinS(String... strings) {
        return String.join(" ", strings);
    }

    static String joinN(String... strings) {
        return String.join(System.lineSeparator(), strings);
    }
}
