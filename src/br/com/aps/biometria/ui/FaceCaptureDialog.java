package br.com.aps.biometria.ui;

import br.com.aps.biometria.service.FaceRecognitionService;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class FaceCaptureDialog extends JDialog {
    private final FaceRecognitionService faceRecognitionService;
    private final VideoCapture videoCapture = new VideoCapture(0);
    private final JLabel previewLabel = new JLabel("Inicializando câmera...", JLabel.CENTER);
    private final Timer timer;

    private Mat currentFrame;
    private Mat currentDetectedFace;
    private BufferedImage capturedFace;

    private FaceCaptureDialog(JFrame owner, FaceRecognitionService faceRecognitionService) {
        super(owner, "Captura Facial", true);
        this.faceRecognitionService = faceRecognitionService;
        this.timer = new Timer(80, event -> updatePreview());

        configureDialog();
        initializeComponents();
        openCamera();
    }

    public static BufferedImage captureFace(JFrame owner, FaceRecognitionService faceRecognitionService) {
        FaceCaptureDialog dialog = new FaceCaptureDialog(owner, faceRecognitionService);
        dialog.setVisible(true);
        return dialog.capturedFace;
    }

    private void configureDialog() {
        setSize(760, 620);
        setMinimumSize(new Dimension(720, 580));
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(12, 12));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                releaseCamera();
            }
        });
    }

    private void initializeComponents() {
        previewLabel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(previewLabel, BorderLayout.CENTER);

        JLabel instructions = new JLabel("Centralize o rosto na câmera e clique em Capturar Face.", JLabel.CENTER);
        add(instructions, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));

        JButton captureButton = new JButton("Capturar Face");
        captureButton.addActionListener(event -> handleCapture());
        actions.add(captureButton);

        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(event -> {
            capturedFace = null;
            releaseCamera();
            dispose();
        });
        actions.add(cancelButton);

        add(actions, BorderLayout.SOUTH);
    }

    private void openCamera() {
        if (!videoCapture.isOpened()) {
            videoCapture.open(0);
        }

        if (!videoCapture.isOpened()) {
            throw new IllegalStateException("Não foi possível acessar a webcam. Verifique as permissões de câmera do macOS.");
        }

        timer.start();
    }

    private void updatePreview() {
        Mat frame = new Mat();
        if (!videoCapture.read(frame) || frame.empty()) {
            return;
        }

        currentFrame = frame.clone();
        currentDetectedFace = faceRecognitionService.detectAndNormalizeFace(currentFrame);
        Mat annotated = faceRecognitionService.annotateFrame(frame);
        previewLabel.setIcon(new javax.swing.ImageIcon(faceRecognitionService.matToBufferedImage(annotated)));
        previewLabel.setText("");
    }

    private void handleCapture() {
        if (currentFrame == null || currentFrame.empty()) {
            JOptionPane.showMessageDialog(this, "Nenhum frame disponível da câmera.", "Câmera", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Mat detectedFace = currentDetectedFace;
        if (detectedFace == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "A foto capturada não possui um rosto válido.\nEssa foto não foi salva. Tire uma nova foto.",
                    "Biometria Facial",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        capturedFace = faceRecognitionService.matToBufferedImage(detectedFace);
        releaseCamera();
        dispose();
    }

    private void releaseCamera() {
        timer.stop();
        if (videoCapture.isOpened()) {
            videoCapture.release();
        }
    }
}
