package edu.unc.cs.torell.visionAPIpractice;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.FrameGrabber.Exception;

// Imports the Google Cloud client library

import com.google.cloud.vision.v1p3beta1.AnnotateImageRequest;
import com.google.cloud.vision.v1p3beta1.AnnotateImageResponse;
import com.google.cloud.vision.v1p3beta1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1p3beta1.EntityAnnotation;
import com.google.cloud.vision.v1p3beta1.Feature;
import com.google.cloud.vision.v1p3beta1.Feature.Type;
import com.google.cloud.vision.v1p3beta1.Image;
import com.google.cloud.vision.v1p3beta1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;

public class QuickstartSample {
	public static void main(String[] args) throws Exception {
		// Instantiates a client
		try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {

			// The path to the image file to annotate
			String fileName = "test.png";

			// Reads the image file into memory
			Path path = Paths.get(fileName);
			byte[] data = Files.readAllBytes(path);
			ByteString imgBytes = ByteString.copyFrom(data);

			// Builds the image annotation request
			List<AnnotateImageRequest> requests = new ArrayList<>();
			Image img = Image.newBuilder().setContent(imgBytes).build();
			Feature feat = Feature.newBuilder().setType(Type.TEXT_DETECTION).build();
			AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
			requests.add(request);

			// Performs label detection on the image file
			BatchAnnotateImagesResponse response = vision.batchAnnotateImages(requests);
			List<AnnotateImageResponse> responses = response.getResponsesList();

			for (AnnotateImageResponse res : responses) {
				if (res.hasError()) {
					System.out.printf("Error: %s\n", res.getError().getMessage());
					return;
				}

				for (EntityAnnotation annotation : res.getTextAnnotationsList()) {
					annotation.getAllFields().forEach((k, v) -> System.out.printf("%s : %s\n", k, v.toString()));
				}
				System.out.println("------------------------");
				System.out.println(res.getFullTextAnnotation().getText());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Cannot create ImageAnnotator Object, or issue with File");
			e.printStackTrace();
		}
	}

	public static void imageFromVid(String videoFileName, String imageFileName, int secondsOrFrame, boolean useFrame)
			throws Exception, IOException {
		FFmpegFrameGrabber g = new FFmpegFrameGrabber(videoFileName); // set up the
																		// video to pull from
		Java2DFrameConverter converter = new Java2DFrameConverter(); // Object that converts Frames to
																		// BufferedImages
		g.start();

		long start = System.currentTimeMillis();

		if (useFrame) {
			g.setFrameNumber(secondsOrFrame);
		} else {
			g.setFrameNumber((int) (g.getFrameRate() * secondsOrFrame)); // Change the frame pointer
		}
		// for (int i = 0; i < 1; i++) { // Grab X number of frames
		Frame f = g.grabImage(); // Adds the current frame to f and increments the frame number
		ImageIO.write(converter.getBufferedImage(f), "png",
				new File(imageFileName));
		// }

		System.out.println(System.currentTimeMillis() - start);

		g.stop();
	}
}