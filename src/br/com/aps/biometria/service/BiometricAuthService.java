package br.com.aps.biometria.service;

import br.com.aps.biometria.model.User;
import br.com.aps.biometria.repository.UserRepository;

import java.util.List;
import java.util.Optional;

public class BiometricAuthService {
    private final UserRepository repository;

    public BiometricAuthService(UserRepository repository) {
        this.repository = repository;
    }

    public ServiceResult registerUser(String name, String registration, String biometricCode) {
        String normalizedName = normalize(name);
        String normalizedRegistration = normalize(registration);
        String normalizedBiometricCode = normalize(biometricCode);

        if (normalizedName.isEmpty() || normalizedRegistration.isEmpty() || normalizedBiometricCode.isEmpty()) {
            return ServiceResult.error("Preencha nome, matrícula e código biométrico.");
        }

        if (repository.findByRegistration(normalizedRegistration).isPresent()) {
            return ServiceResult.error("Já existe um usuário cadastrado com essa matrícula.");
        }

        User user = new User(normalizedName, normalizedRegistration, normalizedBiometricCode);
        repository.save(user);
        return ServiceResult.success("Usuário cadastrado com sucesso.");
    }

    public ServiceResult authenticate(String registration, String biometricCode) {
        String normalizedRegistration = normalize(registration);
        String normalizedBiometricCode = normalize(biometricCode);

        if (normalizedRegistration.isEmpty() || normalizedBiometricCode.isEmpty()) {
            return ServiceResult.error("Informe matrícula e código biométrico.");
        }

        Optional<User> user = repository.findByRegistration(normalizedRegistration);
        if (user.isEmpty()) {
            return ServiceResult.error("Usuário não encontrado para a matrícula informada.");
        }

        if (!user.get().getBiometricCode().equals(normalizedBiometricCode)) {
            return ServiceResult.error("Biometria inválida. Acesso negado.");
        }

        return ServiceResult.success("Acesso liberado para " + user.get().getName() + ".");
    }

    public List<User> listUsers() {
        return repository.findAll();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
