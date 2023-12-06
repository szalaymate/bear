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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Function;

import static bear.BearWithEither.Either.left;
import static bear.BearWithEither.Either.right;
import static bear.Common.createBear;
import static bear.Common.joinN;
import static bear.Common.joinS;

@RestController
public class BearWithEither {

	private static final Path WORKING = Paths.get("");
	@Value("${directory:}")
	private final Path directory = WORKING;

	@GetMapping(
			value = "/bear-with-either/{head}/{body}/{leg}",
			produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.TEXT_PLAIN_VALUE}
	)
	public ResponseEntity<Object> bear(@PathVariable String head, @PathVariable String body, @PathVariable String leg) {
		Either<BufferedImage, String> bearOrNoBear = readMember("heads", head)
				.either(
						loadedHead -> readMember("bodies", body).either(
								loadedBodies -> readMember("legs", leg).either(
										loadedLegs -> left(createBear(loadedHead, loadedBodies, loadedLegs)),
										noLegs -> right(joinN("There's no leg found:", noLegs))),
								noBodies -> right(joinN("There's no bodies found:", noBodies))),
						noHead -> right(joinN("There's no head found:", noHead)));
		return bearOrNoBear
				.mapLeft(Common::writeToByteArray)
				.either(bear -> ResponseEntity.ok()
								.contentType(MediaType.IMAGE_JPEG)
								.body(bear),
						noBear -> ResponseEntity.status(HttpStatus.NOT_FOUND)
								.contentType(MediaType.TEXT_PLAIN)
								.body(joinN("Couldn't create bear:", noBear)));
	}

	private Either<BufferedImage, String> readMember(String memberType, String name) {
		var fileName = name + ".jpg";
		return loadImageFromWorkingDir(memberType, fileName)
				.either(Either::left, notInWorking -> loadImageFromSpecifiedDir(memberType, fileName)
						.either(Either::left, notInSpecified -> loadImageFromResource(memberType, fileName)
								.either(Either::left, notInResource -> right(joinN(notInWorking, notInSpecified, notInResource)))));
	}

	private Either<BufferedImage, String> loadImageFromWorkingDir(String subDir, String fileName) {
		return loadImage(() -> Files.newInputStream(WORKING.resolve(subDir).resolve(fileName)))
				.mapRight(noImage -> joinS("In working dir:", noImage));
	}

	private Either<BufferedImage, String> loadImageFromSpecifiedDir(String subDir, String fileName) {
		return loadImage(() -> Files.newInputStream(directory.resolve(subDir).resolve(fileName)))
				.mapRight(noImage -> joinS("In specified dir:", noImage));
	}

	private static Either<BufferedImage, String> loadImageFromResource(String subDir, String fileName) {
		return loadImage(() -> Common.loadResource("/members/" + subDir + "/" + fileName))
				.mapRight(noImage -> joinS("In resources:", noImage));
	}

	/**
	 * This is the basic low level image loader method.
	 * Now we propagate errors from here as an {@link Optional} containing the result, or empty if not found.
	 */
	private static Either<BufferedImage, String> loadImage(InputStreamSupplier inSupplier) {
		try (var in = inSupplier.get()) {
			return left(ImageIO.read(in));
		} catch (IOException e) {
			return right(joinS("Image not found: ", e.getMessage()));
		}
	}

	interface Either<L, R> {

		<C> C either(Function<L, C> onLeft, Function<R, C> onRight);

		<C> Either<C, R> mapLeft(Function<L, C> onLeft);

		<C> Either<L, C> mapRight(Function<R, C> onRight);

		static <L, R> Either<L, R> left(L left) {
			return new Either<>() {
				@Override
				public <C> C either(Function<L, C> onLeft, Function<R, C> onRight) {
					return onLeft.apply(left);
				}

				@Override
				public <C> Either<C, R> mapLeft(Function<L, C> onLeft) {
					return left(onLeft.apply(left));
				}

				@Override
				public <C> Either<L, C> mapRight(Function<R, C> onRight) {
					return left(left);
				}
			};
		}

		static <L, R> Either<L, R> right(R right) {
			return new Either<>() {
				@Override
				public <C> C either(Function<L, C> onLeft, Function<R, C> onRight) {
					return onRight.apply(right);
				}

				@Override
				public <C> Either<C, R> mapLeft(Function<L, C> onLeft) {
					return right(right);
				}

				@Override
				public <C> Either<L, C> mapRight(Function<R, C> onRight) {
					return right(onRight.apply(right));
				}
			};
		}
	}
}
