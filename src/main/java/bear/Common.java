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
}
