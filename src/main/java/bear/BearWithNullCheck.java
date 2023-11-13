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
public class BearWithNullCheck {

	private static final Path WORKING = Paths.get("");
	@Value("${directory:}")
	private final Path directory = WORKING;

	@GetMapping(
			value = "/bear-with-null-checks/{head}/{body}/{leg}",
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

	private BufferedImage createBear(BufferedImage head, BufferedImage body, BufferedImage leg) {
		int height = head.getHeight() + body.getHeight() + leg.getHeight();
		int width = head.getWidth();

		var bear = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		bear.createGraphics().drawImage(head, 0, 0, null);
		bear.createGraphics().drawImage(body, 0, head.getHeight(), null);
		bear.createGraphics().drawImage(leg, 0, head.getHeight() + body.getHeight(), null);

		return bear;
	}

	private BufferedImage readMember(String memberType, String name) {
		var fileName = name + ".jpg";
		var member = loadImageFromFile(directory.resolve(memberType).resolve(fileName));
		if (member == null) {
			member = loadImageFromFile(WORKING.resolve(memberType).resolve(fileName));
		}
		if (member == null) {
			member = loadImageFromResource("/members/" + memberType + "/" + fileName);
		}
		return member;
	}

	private static BufferedImage loadImageFromFile(Path file) {
		return loadImage(() -> Files.newInputStream(file));
	}

	private static BufferedImage loadImageFromResource(String resource) {
		return loadImage(() -> Common.loadResource(resource));
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
