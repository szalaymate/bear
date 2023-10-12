package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@RestController
@SpringBootApplication
public class BearClothing {

	@Value("${directory:}")
	private final String directory = Paths.get("").toString();

	@GetMapping(
			value = "/bear/{head}/{body}/{leg}",
			produces = MediaType.IMAGE_JPEG_VALUE
	)
	public byte[] index(@PathVariable String head, @PathVariable String body, @PathVariable String leg) throws IOException {
		InputStream headInpS = read(head);
		InputStream bodyInpS = read(body);
		InputStream legInpS = read(leg);

		BufferedImage bear = createBear(headInpS, bodyInpS, legInpS);

		ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
		ImageIO.write(bear, "jpg", imageStream);

		return imageStream.toByteArray();
	}

	private InputStream read(String typeOfBodyParts) throws IOException {
		Path workingDirectory = Paths.get("").resolve(typeOfBodyParts + ".jpg");
		Path argument = Paths.get(directory).resolve(typeOfBodyParts + ".jpg");
		InputStream BodyParts;

		if (Files.exists(workingDirectory)) {
			BodyParts = Files.newInputStream(workingDirectory);
			return  BodyParts;
		} else if (Files.exists(argument)) {
			BodyParts = Files.newInputStream(argument);
			return BodyParts;
		} else {
			BodyParts = getClass().getClassLoader().getResourceAsStream(typeOfBodyParts + ".jpg");
			return BodyParts;
		}
	}

	private BufferedImage createBear(InputStream head, InputStream body, InputStream leg) throws IOException {
		BufferedImage imgHead = ImageIO.read(head);
		BufferedImage imgBody = ImageIO.read(body);
		BufferedImage imgLeg = ImageIO.read(leg);

		int height = imgHead.getHeight() + imgBody.getHeight() + imgLeg.getHeight();
		int width = imgHead.getWidth();

		BufferedImage bear = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		bear.createGraphics().drawImage(imgHead, 0, 0, null);
		bear.createGraphics().drawImage(imgBody, 0, imgHead.getHeight(), null);
		bear.createGraphics().drawImage(imgLeg, 0, imgHead.getHeight() + imgBody.getHeight(), null);

		return bear;
	}

	public static void main(String[] args) {
		SpringApplication.run(BearClothing.class, args);
	}

}
