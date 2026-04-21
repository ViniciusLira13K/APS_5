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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FaceRecognitionService {
    private static final String FACE_REFERENCE_SEPARATOR = "|";
    private static final Size FACE_SIZE = new Size(200, 200);
    private static final double HISTOGRAM_MIN_CORRELATION = 0.58;
    private static final double TEMPLATE_MATCH_MIN = 0.62;
    private static final double FINAL_MATCH_SCORE_MIN = 0.64;
    private static final double PIXEL_DIFF_MAX = 36.0;

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

        Rect largestFace = locateLargestFace(source);
        if (largestFace == null) {
            return null;
        }

        Mat gray = toGray(source);
        Mat cropped = new Mat(gray, largestFace).clone();
        Imgproc.resize(cropped, cropped, FACE_SIZE);
        Imgproc.equalizeHist(cropped, cropped);
        return cropped;
    }

    public Mat annotateFrame(Mat frame) {
        Mat preview = frame.clone();
        Rect largestFace = locateLargestFace(frame);
        if (largestFace != null) {
            Imgproc.rectangle(preview, largestFace, new Scalar(0, 255, 0), 2);
        }
        return preview;
    }

    public boolean hasFaceReference(String faceImagePath) {
        return !loadStoredFaces(faceImagePath).isEmpty();
    }

    public String saveFaceSamples(List<Mat> normalizedFaces, String registration) {
        List<String> savedFiles = new ArrayList<>();

        for (int index = 0; index < normalizedFaces.size(); index++) {
            String fileName = registration + "_" + (index + 1) + ".png";
            Path output = facesDirectory.resolve(fileName);
            if (!Imgcodecs.imwrite(output.toString(), normalizedFaces.get(index))) {
                throw new IllegalStateException("Não foi possível salvar a referência facial do usuário.");
            }
            savedFiles.add(fileName);
        }

        return String.join(FACE_REFERENCE_SEPARATOR, savedFiles);
    }

    public boolean matches(String storedFileName, Mat candidateFace) {
        List<Mat> storedFaces = loadStoredFaces(storedFileName);
        if (storedFaces.isEmpty()) {
            throw new IllegalStateException("Não foi possível carregar a face cadastrada para comparação.");
        }

        MatchScore bestScore = null;
        for (Mat storedFace : storedFaces) {
            MatchScore directScore = scoreSample(storedFace, candidateFace);
            if (directScore.isMatch()) {
                return true;
            }

            Mat flippedCandidate = new Mat();
            Core.flip(candidateFace, flippedCandidate, 1);
            MatchScore flippedScore = scoreSample(storedFace, flippedCandidate);
            if (flippedScore.isMatch()) {
                return true;
            }

            bestScore = chooseBest(bestScore, directScore, flippedScore);
        }

        return false;
    }

    private MatchScore scoreSample(Mat stored, Mat candidateFace) {
        Imgproc.resize(stored, stored, FACE_SIZE);
        Imgproc.equalizeHist(stored, stored);
        Imgproc.GaussianBlur(stored, stored, new Size(3, 3), 0);

        Mat normalizedCandidate = candidateFace.clone();
        Imgproc.resize(normalizedCandidate, normalizedCandidate, FACE_SIZE);
        Imgproc.equalizeHist(normalizedCandidate, normalizedCandidate);
        Imgproc.GaussianBlur(normalizedCandidate, normalizedCandidate, new Size(3, 3), 0);

        Mat difference = new Mat();
        Core.absdiff(stored, normalizedCandidate, difference);
        Scalar meanDifference = Core.mean(difference);
        double pixelDifference = meanDifference.val[0];
        double pixelScore = Math.max(0.0, 1.0 - (pixelDifference / 60.0));

        Mat storedHistogram = buildHistogram(stored);
        Mat candidateHistogram = buildHistogram(normalizedCandidate);
        double correlation = Imgproc.compareHist(storedHistogram, candidateHistogram, Imgproc.CV_COMP_CORREL);

        Mat storedFloat = new Mat();
        Mat candidateFloat = new Mat();
        stored.convertTo(storedFloat, org.opencv.core.CvType.CV_32F);
        normalizedCandidate.convertTo(candidateFloat, org.opencv.core.CvType.CV_32F);

        Mat templateResult = new Mat();
        Imgproc.matchTemplate(storedFloat, candidateFloat, templateResult, Imgproc.TM_CCOEFF_NORMED);
        double templateScore = templateResult.get(0, 0)[0];

        double finalScore = (templateScore * 0.50) + (correlation * 0.30) + (pixelScore * 0.20);
        boolean matched = finalScore >= FINAL_MATCH_SCORE_MIN
                || (templateScore >= 0.72 && correlation >= 0.52)
                || (correlation >= 0.74 && pixelDifference <= PIXEL_DIFF_MAX)
                || (templateScore >= TEMPLATE_MATCH_MIN && pixelDifference <= 24.0);

        return new MatchScore(matched, finalScore, templateScore, correlation, pixelDifference);
    }

    private MatchScore chooseBest(MatchScore currentBest, MatchScore directScore, MatchScore flippedScore) {
        MatchScore bestLocal = directScore.finalScore >= flippedScore.finalScore ? directScore : flippedScore;
        if (currentBest == null || bestLocal.finalScore > currentBest.finalScore) {
            return bestLocal;
        }
        return currentBest;
    }

    private List<Mat> loadStoredFaces(String faceReference) {
        List<String> fileNames = parseFaceReferences(faceReference);
        List<Mat> storedFaces = new ArrayList<>();

        for (String fileName : fileNames) {
            Path storedPath = facesDirectory.resolve(fileName);
            if (!Files.exists(storedPath)) {
                continue;
            }

            Mat stored = Imgcodecs.imread(storedPath.toString(), Imgcodecs.IMREAD_GRAYSCALE);
            if (!stored.empty()) {
                storedFaces.add(stored);
            }
        }

        return storedFaces;
    }

    private List<String> parseFaceReferences(String faceReference) {
        if (faceReference == null || faceReference.isBlank()) {
            return List.of();
        }

        if (faceReference.contains(FACE_REFERENCE_SEPARATOR)) {
            return List.of(faceReference.split("\\|"));
        }

        if (faceReference.endsWith(".png")) {
            return List.of(faceReference);
        }

        try (Stream<Path> files = Files.list(facesDirectory)) {
            return files
                    .filter(path -> path.getFileName().toString().startsWith(faceReference + "_"))
                    .map(path -> path.getFileName().toString())
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível listar as amostras faciais cadastradas.", e);
        }
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

    private Rect locateLargestFace(Mat source) {
        Mat gray = toGray(source);

        Rect largestFace = detectLargestFace(gray, new Size(90, 90), 4);
        if (largestFace != null) {
            return largestFace;
        }

        Mat equalized = gray.clone();
        Imgproc.equalizeHist(equalized, equalized);
        largestFace = detectLargestFace(equalized, new Size(80, 80), 3);
        if (largestFace != null) {
            return largestFace;
        }

        Mat blurred = new Mat();
        Imgproc.GaussianBlur(equalized, blurred, new Size(3, 3), 0);
        return detectLargestFace(blurred, new Size(60, 60), 2);
    }

    private Rect detectLargestFace(Mat grayImage, Size minFaceSize, int minNeighbors) {
        org.opencv.core.MatOfRect faces = new org.opencv.core.MatOfRect();
        faceCascade.detectMultiScale(grayImage, faces, 1.1, minNeighbors, 0, minFaceSize, new Size());

        List<Rect> rectangles = faces.toList();
        return rectangles.stream()
                .max(Comparator.comparingInt(rect -> rect.width * rect.height))
                .orElse(null);
    }

    private Mat toGray(Mat source) {
        Mat gray = new Mat();
        if (source.channels() == 1) {
            source.copyTo(gray);
        } else {
            Imgproc.cvtColor(source, gray, Imgproc.COLOR_BGR2GRAY);
        }
        return gray;
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

    private static class MatchScore {
        private final boolean match;
        private final double finalScore;
        private final double templateScore;
        private final double histogramScore;
        private final double pixelDifference;

        private MatchScore(boolean match, double finalScore, double templateScore, double histogramScore, double pixelDifference) {
            this.match = match;
            this.finalScore = finalScore;
            this.templateScore = templateScore;
            this.histogramScore = histogramScore;
            this.pixelDifference = pixelDifference;
        }

        private boolean isMatch() {
            return match;
        }
    }
}
