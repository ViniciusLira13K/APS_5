package br.com.aps.biometria.service;

import br.com.aps.biometria.model.User;
import br.com.aps.biometria.repository.UserRepository;
import org.opencv.core.Mat;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;

public class BiometricAuthService {
    private final UserRepository repository;
    private final FaceRecognitionService faceRecognitionService;

    public BiometricAuthService(UserRepository repository) {
        this.repository = repository;
        this.faceRecognitionService = new FaceRecognitionService();
    }

    public ServiceResult registerUser(String name, String registration, BufferedImage capturedFace) {
        String normalizedName = normalize(name);
        String normalizedRegistration = normalize(registration);

        if (normalizedName.isEmpty() || normalizedRegistration.isEmpty()) {
            return ServiceResult.error("Preencha nome e matrícula.");
        }

        if (capturedFace == null) {
            return ServiceResult.error("Capture a face do usuário antes de cadastrar.");
        }

        if (repository.findByRegistration(normalizedRegistration).isPresent()) {
            return ServiceResult.error("Já existe um usuário cadastrado com essa matrícula.");
        }

        Mat normalizedFace = faceRecognitionService.detectAndNormalizeFace(capturedFace);
        if (normalizedFace == null) {
            return ServiceResult.error("Nenhum rosto válido foi encontrado na imagem capturada.");
        }

        String faceFileName = normalizedRegistration + ".png";
        faceRecognitionService.saveFace(normalizedFace, faceFileName);

        User user = new User(normalizedName, normalizedRegistration, faceFileName);
        repository.save(user);
        return ServiceResult.success("Usuário cadastrado com sucesso.");
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
