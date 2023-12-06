package bear;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static bear.Common.createBear;

@RestController
public class BearWithOptional {

	private static final Path WORKING = Paths.get("");
	@Value("${directory:}")
	private final Path directory = WORKING;

	@GetMapping(
			value = "/bear-with-optional/{head}/{body}/{leg}",
			produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.TEXT_PLAIN_VALUE}
	)
	public ResponseEntity<Object> bear(@PathVariable String head, @PathVariable String body, @PathVariable String leg) {
		return readMember("heads", head)
				.flatMap(loadedHead -> readMember("bodies", body)
						.flatMap(loadedBodies -> readMember("legs", leg)
								.map(loadedLegs -> createBear(loadedHead, loadedBodies, loadedLegs))))
				.map(Common::writeToByteArray)
				.map(bear -> ResponseEntity.ok()
						.contentType(MediaType.IMAGE_JPEG)
						.<Object>body(bear))
				.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
						.contentType(MediaType.TEXT_PLAIN)
						.body("Couldn't create bear"));
	}

	private Optional<BufferedImage> readMember(String memberType, String name) {
		var fileName = name + ".jpg";
		return loadImageFromWorkingDir(memberType, fileName)
				.or(() -> loadImageFromSpecifiedDir(memberType, fileName))
				.or(() -> loadImageFromResource(memberType, fileName));
	}

	private Optional<BufferedImage> loadImageFromWorkingDir(String subDir, String fileName) {
		return loadImage(() -> Files.newInputStream(WORKING.resolve(subDir).resolve(fileName)));
	}

	private Optional<BufferedImage> loadImageFromSpecifiedDir(String subDir, String fileName) {
		return loadImage(() -> Files.newInputStream(directory.resolve(subDir).resolve(fileName)));
	}

	private static Optional<BufferedImage> loadImageFromResource(String subDir, String fileName) {
		return loadImage(() -> Common.loadResource("/members/" + subDir + "/" + fileName));
	}

	/**
	 * Propagating not found error from here as an empty {@link Optional}.
	 */
	private static Optional<BufferedImage> loadImage(InputStreamSupplier inSupplier) {
		try (var in = inSupplier.get()) {
			return Optional.of(ImageIO.read(in));
		} catch (NoSuchFileException e) {
			return Optional.empty();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
