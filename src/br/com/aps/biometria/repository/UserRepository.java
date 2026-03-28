package br.com.aps.biometria.repository;

import br.com.aps.biometria.model.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class UserRepository {
    private final Path dataFile;

    public UserRepository() {
        this(Paths.get("data", "usuarios.csv"));
    }

    public UserRepository(Path dataFile) {
        this.dataFile = dataFile;
        initializeStorage();
    }

    public List<User> findAll() {
        try {
            List<String> lines = Files.readAllLines(dataFile, StandardCharsets.UTF_8);
            List<User> users = new ArrayList<>();

            for (String line : lines) {
                if (!line.isBlank()) {
                    users.add(User.fromCsvLine(line));
                }
            }

            users.sort(Comparator.comparing(User::getName, String.CASE_INSENSITIVE_ORDER));
            return users;
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível ler os usuários cadastrados.", e);
        }
    }

    public Optional<User> findByRegistration(String registration) {
        return findAll().stream()
                .filter(user -> user.getRegistration().equalsIgnoreCase(registration))
                .findFirst();
    }

    public void save(User user) {
        List<User> users = findAll();
        users.removeIf(existing -> existing.getRegistration().equalsIgnoreCase(user.getRegistration()));
        users.add(user);
        users.sort(Comparator.comparing(User::getName, String.CASE_INSENSITIVE_ORDER));

        List<String> lines = users.stream()
                .map(User::toCsvLine)
                .toList();

        try {
            Files.write(dataFile, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível salvar o usuário.", e);
        }
    }

    private void initializeStorage() {
        try {
            Path parent = dataFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            if (!Files.exists(dataFile)) {
                Files.createFile(dataFile);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível preparar o armazenamento local.", e);
        }
    }
}
