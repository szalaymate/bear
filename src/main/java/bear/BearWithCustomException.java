package bear;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static bear.Common.concatenateImages;
import static bear.Common.joinN;
import static bear.Common.joinS;
import static bear.Common.loadResource;
import static bear.Common.writeToByteArray;

@RestController
public class BearWithCustomException {

    private static final Path WORKING = Paths.get("");
    @Value("${directory:}")
    private final Path directory = WORKING;

    @GetMapping(
            value = "/bear-with-custom-exception/{head}/{body}/{leg}",
            produces = MediaType.IMAGE_JPEG_VALUE
    )
    public byte[] bear(@PathVariable String head, @PathVariable String body, @PathVariable String leg) {
        try {
            try {
                var loadedHead = readMember("heads", head);
                try {
                    var loadedBody = readMember("bodies", body);
                    try {
                        var loadedLeg = readMember("legs", leg);
                        return writeToByteArray(concatenateImages(loadedHead, loadedBody, loadedLeg));
                    } catch (NoSuchBearException e) {
                        throw new NoSuchBearException(joinN("There's no leg found:", e.getMessage()));
                    }
                } catch (NoSuchBearException e) {
                    throw new NoSuchBearException(joinN("There's no body found:", e.getMessage()));
                }
            } catch (NoSuchBearException e) {
                throw new NoSuchBearException(joinN("There's no head found", e.getMessage()));
            }
        } catch (NoSuchBearException e) {
            throw new NoSuchBearException(joinN("Couldn't create bear:", e.getMessage()));
        }
    }

    private BufferedImage readMember(String memberType, String name) {
        var fileName = name + ".jpg";
        try {
            return loadImageFromWorkingDir(memberType, fileName);
        } catch (NoSuchBearException e) {
            try {
                return loadImageFromSpecifiedDir(memberType, fileName);
            } catch (NoSuchBearException f) {
                try {
                    return loadImageFromResource(memberType, fileName);
                } catch (NoSuchBearException g) {
                    throw new NoSuchBearException(joinN(e.getMessage(), f.getMessage(), g.getMessage()));
                }
            }
        }
    }

    private BufferedImage loadImageFromWorkingDir(String subDir, String fileName) {
        try {
            return loadImage(() -> Files.newInputStream(WORKING.resolve(subDir).resolve(fileName)));
        } catch (NoSuchBearException e) {
            throw new NoSuchBearException(joinS("In working dir:", e.getMessage()));
        }
    }

    private BufferedImage loadImageFromSpecifiedDir(String subDir, String fileName) {
        try {
            return loadImage(() -> Files.newInputStream(directory.resolve(subDir).resolve(fileName)));
        } catch (NoSuchBearException e) {
            throw new NoSuchBearException(joinS("In specified dir:", e.getMessage()));
        }
    }

    private static BufferedImage loadImageFromResource(String subDir, String fileName) {
        try {
            return loadImage(() -> loadResource("/members/" + subDir + "/" + fileName));
        } catch (NoSuchBearException e) {
            throw new NoSuchBearException(joinS("In resources:", e.getMessage()));
        }
    }

    /**
     * Propagating not found error from here throwing {@link NoSuchBearException}.
     */
    private static BufferedImage loadImage(InputStreamSupplier inSupplier) {
        try {
            return Common.loadImage(inSupplier);
        } catch (NoSuchFileException e) {
            throw new NoSuchBearException(joinS("Image not found:", e.getMessage()));
        }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static final class NoSuchBearException extends RuntimeException {
        public NoSuchBearException(String message) {
            super(message);
        }
    }
}
