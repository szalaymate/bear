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

@RestController
public class BearWithExceptions {

    private static final Path WORKING = Paths.get("");
    @Value("${directory:}")
    private final Path directory = WORKING;

    @GetMapping(
            value = "/bear-with-exceptions/{head}/{body}/{leg}",
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
            return loadImageFromFile(directory.resolve(memberType).resolve(fileName));
        } catch (IOException e) {
            try {
                return loadImageFromFile(WORKING.resolve(memberType).resolve(fileName));
            } catch (IOException f) {
                return loadImageFromResource("/members/" + memberType + "/" + fileName);
            }
        }
    }

    private static BufferedImage loadImageFromFile(Path file) throws IOException {
        return loadImage(() -> Files.newInputStream(file));
    }

    private static BufferedImage loadImageFromResource(String resource) throws IOException {
        return loadImage(() -> Common.loadResource(resource));
    }


    private static BufferedImage loadImage(InputStreamSupplier inSupplier) throws IOException {
        try (var in = inSupplier.get()) {
            return ImageIO.read(in);
        }
    }

    private BufferedImage createBear(BufferedImage head, BufferedImage body, BufferedImage leg) {
        int height = head.getHeight() + body.getHeight() + leg.getHeight();
        int width = head.getWidth();

        var bear = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        bear.createGraphics().drawImage(head, 0, 0, null);
        bear.createGraphics().drawImage(body, 0, head.getHeight(), null);
        bear.createGraphics().drawImage(leg, 0, head.getHeight() + body.getHeight(), null);

        return bear;
    }
}
