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
public class BearWithNull {

	private static final Path WORKING = Paths.get("");
	@Value("${directory:}")
	private final Path directory = WORKING;

	@GetMapping(
			value = "/bear-with-null/{head}/{body}/{leg}",
			produces = MediaType.IMAGE_JPEG_VALUE
	)
	public byte[] bear(@PathVariable String head, @PathVariable String body, @PathVariable String leg) {
		var loadedHead = readMember("heads", head);
		if (loadedHead == null) {
			return null;
		}
		var loadedBody = readMember("bodies", body);
		if (loadedBody == null) {
			return null;
		}
		var loadedLeg = readMember("legs", leg);
		if (loadedLeg == null) {
			return null;
		}
		return Common.writeToByteArray(createBear(loadedHead, loadedBody, loadedLeg));
	}

	private BufferedImage readMember(String memberType, String name) {
		var fileName = name + ".jpg";
		var member = loadImageFromWorkingDir(memberType, fileName);
		if (member == null) {
			member = loadImageFromSpecifiedDir(memberType, fileName);
		}
		if (member == null) {
			member = loadImageFromResource(memberType, fileName);
		}
		return member;
	}

	private BufferedImage loadImageFromSpecifiedDir(String subDir, String fileName) {
		return loadImage(() -> Files.newInputStream(directory.resolve(subDir).resolve(fileName)));
	}

	private BufferedImage loadImageFromWorkingDir(String subDir, String fileName) {
		return loadImage(() -> Files.newInputStream(WORKING.resolve(subDir).resolve(fileName)));
	}

	private static BufferedImage loadImageFromResource(String subDir, String fileName) {
		return loadImage(() -> Common.loadResource("/members/" + subDir + "/" + fileName));
	}

	/**
	 * This is the basic low level image loader method.
	 * Now we propagate errors from here as a special value, null.
	 */
	private static BufferedImage loadImage(InputStreamSupplier inSupplier) {
		try (var in = inSupplier.get()) {
			return ImageIO.read(in);
		} catch (IOException e) {
			return null;
		}
	}
}
