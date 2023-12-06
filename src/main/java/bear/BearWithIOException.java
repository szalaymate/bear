package bear;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static bear.Common.createBear;

@RestController
public class BearWithIOException {

    private static final Path WORKING = Paths.get("");
    @Value("${directory:}")
    private final Path directory = WORKING;

    @GetMapping(
            value = "/bear-with-io-exception/{head}/{body}/{leg}",
            produces = MediaType.IMAGE_JPEG_VALUE
    )
    public byte[] bear(@PathVariable String head, @PathVariable String body, @PathVariable String leg) throws IOException {
        return Common.writeToByteArray(createBear(
                readMember("heads", head),
                readMember("bodies", body),
                readMember("legs", leg)));
    }

    private BufferedImage readMember(String memberType, String name) throws IOException {
        var fileName = name + ".jpg";
        try {
            return loadImageFromWorkingDir(memberType, fileName);
        } catch (IOException e) {
            try {
                return loadImageFromSpecifiedDir(memberType, fileName);
            } catch (IOException f) {
                return loadImageFromResource(memberType, fileName);
            }
        }
    }

    private BufferedImage loadImageFromWorkingDir(String subDir, String fileName) throws IOException {
        return loadImage(() -> Files.newInputStream(WORKING.resolve(subDir).resolve(fileName)));
    }

    private BufferedImage loadImageFromSpecifiedDir(String subDir, String fileName) throws IOException {
        return loadImage(() -> Files.newInputStream(directory.resolve(subDir).resolve(fileName)));
    }

    private static BufferedImage loadImageFromResource(String subDir, String fileName) throws IOException {
        return loadImage(() -> Common.loadResource("/members/" + subDir + "/" + fileName));
    }

    /**
     * This is the basic low level image loader method.
     * Now we propagate errors from here throwing {@link java.io.IOException}
     */
    private static BufferedImage loadImage(InputStreamSupplier inSupplier) throws IOException {
        try (var in = inSupplier.get()) {
            return ImageIO.read(in);
        }
    }
}
