package bear;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.stream.Stream;

import static bear.BearWithEither.Either.left;
import static bear.BearWithEither.Either.right;
import static bear.Common.concatenateImages;
import static bear.Common.joinN;
import static bear.Common.joinS;
import static bear.Common.loadResource;
import static java.util.function.Function.identity;

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
		return bearFlatMap(head, body, leg)
				.mapLeft(Common::writeToByteArray)
				.either(bear -> ResponseEntity.ok()
								.contentType(MediaType.IMAGE_JPEG)
								.body(bear),
						noBear -> ResponseEntity.status(HttpStatus.NOT_FOUND)
								.contentType(MediaType.TEXT_PLAIN)
								.body(joinN("Couldn't create bear:", noBear)));
	}

	private Either<BufferedImage, String> bearFlatMap(String head, String body, String leg) {
		return readMember("heads", head).mapRight(r -> joinN("There's no head found:", r))
				.flatMapLeft(loadedHead -> readMember("bodies", body).mapRight(r -> joinN("There's no body found:", r))
						.flatMapLeft(loadedBody -> readMember("legs", leg).mapRight(r -> joinN("There's no leg found:", r))
								.mapLeft(loadedLeg -> concatenateImages(loadedHead, loadedBody, loadedLeg))));
	}

	private Either<BufferedImage, String> readMember(String memberType, String name) {
		var fileName = name + ".jpg";
		return loadImageFromWorkingDir(memberType, fileName)
				.flatMapRight(notInWorking -> loadImageFromSpecifiedDir(memberType, fileName)
						.flatMapRight(notInSpecified -> loadImageFromResource(memberType, fileName)
								.mapRight(notInResource -> joinN(notInWorking, notInSpecified, notInResource))));
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
		return loadImage(() -> loadResource("/members/" + subDir + "/" + fileName))
				.mapRight(noImage -> joinS("In resources:", noImage));
	}

	/**
	 * Propagating not found error from here as an {@link Either} containing a message on error.
	 */
	private static Either<BufferedImage, String> loadImage(InputStreamSupplier inSupplier) {
		try {
			return left(Common.loadImage(inSupplier));
		} catch (NoSuchFileException e) {
			return right(joinS("Image not found:", e.getMessage()));
		}
	}

	interface Either<L, R> {

		<C> C either(Function<L, C> onLeft, Function<R, C> onRight);

		default <L1, R1> Either<L1, R1> biMap(Function<L, L1> onLeft, Function<R, R1> onRight) {
			return either(l -> left(onLeft.apply(l)), r -> right(onRight.apply(r)));
		}

		default <L1> Either<L1, R> mapLeft(Function<L, L1> onLeft) {
			return biMap(onLeft, identity());
		}

		default <R1> Either<L, R1> mapRight(Function<R, R1> onRight) {
			return biMap(identity(), onRight);
		}

		default <L1> Either<L1, R> flatMapLeft(Function<L, Either<L1, R>> onLeft) {
			return either(onLeft, Either::right);
		}

		default <R1> Either<L, R1> flatMapRight(Function<R, Either<L, R1>> onRight) {
			return either(Either::left, onRight);
		}

		static <L, R> Either<L, R> left(L left) {
			return new Either<>() {
				@Override
				public <C> C either(Function<L, C> onLeft, Function<R, C> onRight) {
					return onLeft.apply(left);
				}
			};
		}

		static <L, R> Either<L, R> right(R right) {
			return new Either<>() {
				@Override
				public <C> C either(Function<L, C> onLeft, Function<R, C> onRight) {
					return onRight.apply(right);
				}
			};
		}
	}

	// An alternative solution using some sane concatenation of Either.

	private Either<BufferedImage, String> bearConcat(String head, String body, String leg) {
		return Stream.of(
						readMember("heads", head).mapRight(r -> joinN("There's no head found:", r)),
						readMember("bodies", body).mapRight(r -> joinN("There's no body found:", r)),
						readMember("legs", leg).mapRight(r -> joinN("There's no leg found:", r)))
				.map(m -> m.mapLeft(Stream::of))
				.reduce(left(Stream.of()), BearWithEither::concat)
				.mapLeft(Stream::toList)
				.mapLeft(Common::concatenateImages);
	}

	private static <T> Either<Stream<T>, String> concat(Either<Stream<T>, String> e1, Either<Stream<T>, String> e2) {
		return e1.either(
				l1 -> e2.either(
						l2 -> left(Stream.concat(l1, l2)),
						r2 -> right(r2)),
				r1 -> e2.either(
						l2 -> right(r1),
						r2 -> right(joinN(r1, r2))));
	}
}
