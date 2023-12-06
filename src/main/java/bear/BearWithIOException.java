package bear;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
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
    public byte[] bear(@PathVariable String head, @PathVariable String body, @PathVariable String leg) throws NoSuchFileException {
        return Common.writeToByteArray(createBear(
                readMember("heads", head),
                readMember("bodies", body),
                readMember("legs", leg)));
    }

    private BufferedImage readMember(String memberType, String name) throws NoSuchFileException {
        var fileName = name + ".jpg";
        try {
            return loadImageFromWorkingDir(memberType, fileName);
        } catch (NoSuchFileException e) {
            try {
                return loadImageFromSpecifiedDir(memberType, fileName);
            } catch (NoSuchFileException f) {
                return loadImageFromResource(memberType, fileName);
            }
        }
    }

    private BufferedImage loadImageFromWorkingDir(String subDir, String fileName) throws NoSuchFileException {
        return loadImage(() -> Files.newInputStream(WORKING.resolve(subDir).resolve(fileName)));
    }

    private BufferedImage loadImageFromSpecifiedDir(String subDir, String fileName) throws NoSuchFileException {
        return loadImage(() -> Files.newInputStream(directory.resolve(subDir).resolve(fileName)));
    }

    private static BufferedImage loadImageFromResource(String subDir, String fileName) throws NoSuchFileException {
        return loadImage(() -> Common.loadResource("/members/" + subDir + "/" + fileName));
    }

    /**
     * Propagating not found error from here without conversion.
     */
    private static BufferedImage loadImage(InputStreamSupplier inSupplier) throws NoSuchFileException {
        return Common.loadImage(inSupplier);
    }
}
