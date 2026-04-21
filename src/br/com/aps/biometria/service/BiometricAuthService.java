package br.com.aps.biometria.service;

import br.com.aps.biometria.model.User;
import br.com.aps.biometria.repository.UserRepository;
import org.opencv.core.Mat;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BiometricAuthService {
    public static final int REQUIRED_FACE_SAMPLES = 5;

    private final UserRepository repository;
    private final FaceRecognitionService faceRecognitionService;

    public BiometricAuthService(UserRepository repository) {
        this.repository = repository;
        this.faceRecognitionService = new FaceRecognitionService();
    }

    public ServiceResult registerUser(String name, String registration, List<BufferedImage> capturedFaces) {
        String normalizedName = normalize(name);
        String normalizedRegistration = normalize(registration);

        if (normalizedName.isEmpty() || normalizedRegistration.isEmpty()) {
            return ServiceResult.error("Preencha nome e matrícula.");
        }

        if (capturedFaces == null || capturedFaces.size() < REQUIRED_FACE_SAMPLES) {
            return ServiceResult.error("Capture as " + REQUIRED_FACE_SAMPLES + " fotos faciais antes de cadastrar.");
        }

        if (repository.findByRegistration(normalizedRegistration).isPresent()) {
            return ServiceResult.error("Já existe um usuário cadastrado com essa matrícula.");
        }

        List<Mat> normalizedFaces = new ArrayList<>();
        for (BufferedImage capturedFace : capturedFaces) {
            Mat normalizedFace = faceRecognitionService.detectAndNormalizeFace(capturedFace);
            if (normalizedFace == null) {
                return ServiceResult.error("Uma das fotos capturadas não contém um rosto válido.");
            }
            normalizedFaces.add(normalizedFace);
        }

        String faceReference = faceRecognitionService.saveFaceSamples(normalizedFaces, normalizedRegistration);

        User user = new User(normalizedName, normalizedRegistration, faceReference);
        repository.save(user);
        return ServiceResult.success("Usuário cadastrado com " + REQUIRED_FACE_SAMPLES + " amostras faciais.");
    }

    public ServiceResult authenticate(String registration, BufferedImage capturedFace) {
        String normalizedRegistration = normalize(registration);

        if (normalizedRegistration.isEmpty()) {
            return ServiceResult.error("Informe a matrícula.");
        }

        if (capturedFace == null) {
            return ServiceResult.error("Capture a face antes de validar o acesso.");
        }

        Optional<User> user = repository.findByRegistration(normalizedRegistration);
        if (user.isEmpty()) {
            return ServiceResult.error("Usuário não encontrado para a matrícula informada.");
        }

        Mat normalizedFace = faceRecognitionService.detectAndNormalizeFace(capturedFace);
        if (normalizedFace == null) {
            return ServiceResult.error("Nenhum rosto válido foi encontrado na câmera.");
        }

        if (!faceRecognitionService.hasFaceReference(user.get().getFaceImagePath())) {
            return ServiceResult.error("O usuário cadastrado não possui referência facial disponível.");
        }

        boolean matched = faceRecognitionService.matches(user.get().getFaceImagePath(), normalizedFace);
        if (!matched) {
            return ServiceResult.error("Face não reconhecida. Acesso negado.");
        }

        return ServiceResult.success("Acesso liberado para " + user.get().getName() + ".");
    }

    public List<User> listUsers() {
        return repository.findAll();
    }

    public BufferedImage captureFaceForRegistration() {
        return faceRecognitionService.captureFace();
    }

    public BufferedImage captureFaceForAuthentication() {
        return faceRecognitionService.captureFace();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
