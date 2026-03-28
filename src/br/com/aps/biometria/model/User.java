package br.com.aps.biometria.model;

public class User {
    private final String name;
    private final String registration;
    private final String biometricCode;

    public User(String name, String registration, String biometricCode) {
        this.name = name;
        this.registration = registration;
        this.biometricCode = biometricCode;
    }

    public String getName() {
        return name;
    }

    public String getRegistration() {
        return registration;
    }

    public String getBiometricCode() {
        return biometricCode;
    }

    public String toCsvLine() {
        return escape(name) + ";" + escape(registration) + ";" + escape(biometricCode);
    }

    public static User fromCsvLine(String line) {
        String[] parts = splitEscaped(line);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Linha de usuário inválida.");
        }

        return new User(unescape(parts[0]), unescape(parts[1]), unescape(parts[2]));
    }

    private static String[] splitEscaped(String line) {
        StringBuilder current = new StringBuilder();
        String[] values = new String[3];
        int index = 0;
        boolean escaping = false;

        for (char character : line.toCharArray()) {
            if (escaping) {
                current.append(character);
                escaping = false;
                continue;
            }

            if (character == '\\') {
                current.append(character);
                escaping = true;
                continue;
            }

            if (character == ';' && index < values.length - 1) {
                values[index++] = current.toString();
                current.setLength(0);
                continue;
            }

            current.append(character);
        }

        values[index] = current.toString();
        return values;
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace(";", "\\;");
    }

    private static String unescape(String value) {
        StringBuilder builder = new StringBuilder();
        boolean escaping = false;

        for (char current : value.toCharArray()) {
            if (escaping) {
                builder.append(current);
                escaping = false;
            } else if (current == '\\') {
                escaping = true;
            } else {
                builder.append(current);
            }
        }

        if (escaping) {
            builder.append('\\');
        }

        return builder.toString();
    }
}
