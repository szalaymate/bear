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

import static bear.Common.concatenateImages;
import static bear.Common.loadResource;
import static bear.Common.writeToByteArray;

@RestController
public class BearWithNullObject {

	private static final Path WORKING = Paths.get("");
	@Value("${directory:}")
	private final Path directory = WORKING;

	@GetMapping(
			value = "/bear-with-null-object/{head}/{body}/{leg}",
			produces = MediaType.IMAGE_JPEG_VALUE
	)
	public byte[] bear(@PathVariable String head, @PathVariable String body, @PathVariable String leg) {
		return writeToByteArray(concatenateImages(
				readMember("heads", head),
				readMember("bodies", body),
				readMember("legs", leg)));
	}

	private BufferedImage readMember(String memberType, String name) {
		var fileName = name + ".jpg";
		return concatenateImages(
				loadImageFromWorkingDir(memberType, fileName),
				loadImageFromSpecifiedDir(memberType, fileName),
				loadImageFromResource(memberType, fileName));
	}

	private BufferedImage loadImageFromWorkingDir(String subDir, String fileName) {
		return loadImage(() -> Files.newInputStream(WORKING.resolve(subDir).resolve(fileName)));
	}

	private BufferedImage loadImageFromSpecifiedDir(String subDir, String fileName) {
		return loadImage(() -> Files.newInputStream(directory.resolve(subDir).resolve(fileName)));
	}

	private static BufferedImage loadImageFromResource(String subDir, String fileName) {
		return loadImage(() -> loadResource("/members/" + subDir + "/" + fileName));
	}

	/**
	 * Propagating not found error from here as a null object.
	 * This is a hack here, since {@link BufferedImage} doesn't support a zero image.
	 */
	private static BufferedImage loadImage(InputStreamSupplier inSupplier) {
		try {
			return Common.loadImage(inSupplier);
		} catch (NoSuchFileException e) {
			return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		}
	}
}
