package br.com.aps.biometria.service;

import br.com.aps.biometria.ui.FaceCaptureDialog;
import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FaceRecognitionService {
    private static final Size FACE_SIZE = new Size(200, 200);
    private static final double HISTOGRAM_MIN_CORRELATION = 0.82;
    private static final double PIXEL_DIFF_MAX = 22.0;

    private final CascadeClassifier faceCascade;
    private final Path facesDirectory;

    public FaceRecognitionService() {
        OpenCV.loadLocally();
        this.faceCascade = new CascadeClassifier(extractCascadeFile().toString());
        this.facesDirectory = Paths.get("data", "faces");

        if (faceCascade.empty()) {
            throw new IllegalStateException("Não foi possível carregar o classificador facial do OpenCV.");
        }

        try {
            Files.createDirectories(facesDirectory);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível preparar a pasta de faces cadastradas.", e);
        }
    }

    public BufferedImage captureFace() {
        return FaceCaptureDialog.captureFace(new JFrame(), this);
    }

    public Mat detectAndNormalizeFace(BufferedImage image) {
        Mat source = bufferedImageToMat(image);
        return detectAndNormalizeFace(source);
    }

    public Mat detectAndNormalizeFace(Mat source) {
        if (source == null || source.empty()) {
            return null;
        }

        Mat gray = new Mat();
        Imgproc.cvtColor(source, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(gray, gray);

        Rect largestFace = detectLargestFace(gray);
        if (largestFace == null) {
            return null;
        }

        Mat cropped = new Mat(gray, largestFace).clone();
        Imgproc.resize(cropped, cropped, FACE_SIZE);
        Imgproc.equalizeHist(cropped, cropped);
        return cropped;
    }

    public Mat annotateFrame(Mat frame) {
        Mat preview = frame.clone();
        Mat gray = new Mat();
        Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
        Rect largestFace = detectLargestFace(gray);
        if (largestFace != null) {
            Imgproc.rectangle(preview, largestFace, new Scalar(0, 255, 0), 2);
        }
        return preview;
    }

    public boolean hasFaceReference(String faceImagePath) {
        return Files.exists(facesDirectory.resolve(faceImagePath));
    }

    public void saveFace(Mat normalizedFace, String fileName) {
        Path output = facesDirectory.resolve(fileName);
        if (!Imgcodecs.imwrite(output.toString(), normalizedFace)) {
            throw new IllegalStateException("Não foi possível salvar a referência facial do usuário.");
        }
    }

    public boolean matches(String storedFileName, Mat candidateFace) {
        Path storedPath = facesDirectory.resolve(storedFileName);
        Mat stored = Imgcodecs.imread(storedPath.toString(), Imgcodecs.IMREAD_GRAYSCALE);
        if (stored.empty()) {
            throw new IllegalStateException("Não foi possível carregar a face cadastrada para comparação.");
        }

        Imgproc.resize(stored, stored, FACE_SIZE);
        Imgproc.equalizeHist(stored, stored);

        Mat difference = new Mat();
        Core.absdiff(stored, candidateFace, difference);
        Scalar meanDifference = Core.mean(difference);
        double pixelDifference = meanDifference.val[0];

        Mat storedHistogram = buildHistogram(stored);
        Mat candidateHistogram = buildHistogram(candidateFace);
        double correlation = Imgproc.compareHist(storedHistogram, candidateHistogram, Imgproc.CV_COMP_CORREL);

        return correlation >= HISTOGRAM_MIN_CORRELATION && pixelDifference <= PIXEL_DIFF_MAX;
    }

    public BufferedImage matToBufferedImage(Mat matrix) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", matrix, buffer);
        try {
            return ImageIO.read(new ByteArrayInputStream(buffer.toArray()));
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível converter o frame da câmera.", e);
        }
    }

    private Mat buildHistogram(Mat image) {
        List<Mat> images = new ArrayList<>();
        images.add(image);

        Mat histogram = new Mat();
        Imgproc.calcHist(images, new MatOfInt(0), new Mat(), histogram, new MatOfInt(256), new MatOfFloat(0, 256));
        Core.normalize(histogram, histogram, 0, 1, Core.NORM_MINMAX);
        return histogram;
    }

    private Rect detectLargestFace(Mat grayImage) {
        org.opencv.core.MatOfRect faces = new org.opencv.core.MatOfRect();
        faceCascade.detectMultiScale(grayImage, faces, 1.1, 4, 0, new Size(120, 120), new Size());

        List<Rect> rectangles = faces.toList();
        return rectangles.stream()
                .max(Comparator.comparingInt(rect -> rect.width * rect.height))
                .orElse(null);
    }

    private Path extractCascadeFile() {
        try (InputStream input = getClass().getResourceAsStream("/cascades/haarcascade_frontalface_default.xml")) {
            if (input == null) {
                throw new IllegalStateException("Arquivo de cascade facial não encontrado nos recursos.");
            }

            Path tempFile = Files.createTempFile("haarcascade_frontalface_default", ".xml");
            Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);
            tempFile.toFile().deleteOnExit();
            return tempFile;
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível preparar o classificador facial.", e);
        }
    }

    private Mat bufferedImageToMat(BufferedImage image) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "png", output);
            return Imgcodecs.imdecode(new MatOfByte(output.toByteArray()), Imgcodecs.IMREAD_COLOR);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível converter a imagem capturada.", e);
        }
    }
}
