package br.com.aps.biometria.ui;

import br.com.aps.biometria.model.User;
import br.com.aps.biometria.service.BiometricAuthService;
import br.com.aps.biometria.service.ServiceResult;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    private final BiometricAuthService service;

    private final JTextField nameField = new JTextField();
    private final JTextField registrationField = new JTextField();
    private final JTextField authRegistrationField = new JTextField();
    private final JTextArea usersArea = new JTextArea();
    private final JTextArea statusArea = new JTextArea();
    private final JLabel registrationFaceStatus = new JLabel("Nenhuma amostra facial capturada.");

    private final List<java.awt.image.BufferedImage> pendingRegistrationFaces = new ArrayList<>();

    public MainFrame(BiometricAuthService service) {
        this.service = service;
        configureFrame();
        initializeComponents();
        refreshUsers();
    }

    private void configureFrame() {
        setTitle("Sistema de Identificação Biométrica");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 620);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(null);
    }

    private void initializeComponents() {
        JPanel content = new JPanel(new BorderLayout(16, 16));
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        content.setBackground(new Color(245, 247, 250));

        JLabel title = new JLabel("Controle de Acesso Biométrico", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(28, 57, 91));
        content.add(title, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 16, 16));
        centerPanel.setOpaque(false);
        centerPanel.add(buildRegistrationPanel());
        centerPanel.add(buildAuthenticationPanel());
        content.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 16, 16));
        bottomPanel.setOpaque(false);
        bottomPanel.add(buildUsersPanel());
        bottomPanel.add(buildStatusPanel());
        content.add(bottomPanel, BorderLayout.SOUTH);

        add(content);
    }

    private JPanel buildRegistrationPanel() {
        JPanel panel = createSectionPanel("Cadastro de Usuário");
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = createDefaultConstraints();

        addField(panel, gbc, 0, "Nome", nameField);
        addField(panel, gbc, 1, "Matrícula", registrationField);

        JButton captureFaceButton = new JButton("Capturar Amostra Facial");
        captureFaceButton.addActionListener(event -> handleRegistrationFaceCapture());

        JButton registerButton = new JButton("Cadastrar usuário");
        registerButton.addActionListener(event -> handleRegister());

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(12, 0, 4, 0);
        panel.add(captureFaceButton, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 10, 0);
        registrationFaceStatus.setFont(new Font("SansSerif", Font.PLAIN, 13));
        panel.add(registrationFaceStatus, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(16, 0, 0, 0);
        panel.add(registerButton, gbc);

        return panel;
    }

    private JPanel buildAuthenticationPanel() {
        JPanel panel = createSectionPanel("Identificação para Acesso");
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = createDefaultConstraints();

        addField(panel, gbc, 0, "Matrícula", authRegistrationField);

        JButton authButton = new JButton("Validar Face");
        authButton.addActionListener(event -> handleAuthenticationWithFace());

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(16, 0, 0, 0);
        panel.add(authButton, gbc);

        return panel;
    }

    private JPanel buildUsersPanel() {
        JPanel panel = createSectionPanel("Usuários Cadastrados");
        panel.setLayout(new BorderLayout());

        usersArea.setEditable(false);
        usersArea.setLineWrap(true);
        usersArea.setWrapStyleWord(true);
        usersArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        panel.add(new JScrollPane(usersArea), BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildStatusPanel() {
        JPanel panel = createSectionPanel("Status do Sistema");
        panel.setLayout(new BorderLayout());

        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
        statusArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        statusArea.setText("Sistema iniciado. Aguarde novas operações.");
        panel.add(new JScrollPane(statusArea), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(191, 201, 214)), title));
        panel.setBackground(Color.WHITE);
        return panel;
    }

    private GridBagConstraints createDefaultConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 0, 12, 12);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 15));
        field.setColumns(20);
        field.setPreferredSize(new Dimension(320, 38));
        field.setBorder(new EmptyBorder(8, 10, 8, 10));

        gbc.gridx = 0;
        gbc.gridy = row * 2;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(10, 0, 4, 0);
        JLabel fieldLabel = new JLabel(label + ":");
        fieldLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(fieldLabel, gbc);

        gbc.gridy = (row * 2) + 1;
        gbc.insets = new Insets(0, 0, 8, 0);
        panel.add(field, gbc);
    }

    private void handleRegister() {
        ServiceResult result = service.registerUser(
                nameField.getText(),
                registrationField.getText(),
                new ArrayList<>(pendingRegistrationFaces)
        );

        appendStatus("Cadastro", result);
        showMessage(result);

        if (result.isSuccess()) {
            nameField.setText("");
            registrationField.setText("");
            pendingRegistrationFaces.clear();
            registrationFaceStatus.setText("Nenhuma amostra facial capturada.");
            refreshUsers();
        }
    }

    private void handleRegistrationFaceCapture() {
        java.awt.image.BufferedImage capturedFace = service.captureFaceForRegistration();
        if (capturedFace != null) {
            if (pendingRegistrationFaces.size() >= BiometricAuthService.REQUIRED_FACE_SAMPLES) {
                appendStatus("Captura facial", ServiceResult.error("As 5 amostras já foram capturadas. Faça o cadastro ou limpe o formulário."));
                return;
            }

            pendingRegistrationFaces.add(capturedFace);
            registrationFaceStatus.setText("Amostras capturadas: " + pendingRegistrationFaces.size() + "/" + BiometricAuthService.REQUIRED_FACE_SAMPLES);
            appendStatus("Captura facial", ServiceResult.success("Amostra facial " + pendingRegistrationFaces.size() + " de " + BiometricAuthService.REQUIRED_FACE_SAMPLES + " pronta."));
        } else {
            appendStatus("Captura facial", ServiceResult.error("A captura facial foi cancelada."));
        }
    }

    private void handleAuthenticationWithFace() {
        java.awt.image.BufferedImage capturedFace = service.captureFaceForAuthentication();
        if (capturedFace == null) {
            appendStatus("Autenticação", ServiceResult.error("A captura facial foi cancelada."));
            return;
        }

        ServiceResult result = service.authenticate(authRegistrationField.getText(), capturedFace);

        appendStatus("Autenticação", result);
        showMessage(result);

        if (result.isSuccess()) {
            authRegistrationField.setText("");
        }
    }

    private void refreshUsers() {
        List<User> users = service.listUsers();
        if (users.isEmpty()) {
            usersArea.setText("Nenhum usuário cadastrado até o momento.");
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (User user : users) {
            builder.append("Nome: ").append(user.getName()).append('\n');
            builder.append("Matrícula: ").append(user.getRegistration()).append('\n');
            int sampleCount = user.getFaceImagePath().contains("|")
                    ? user.getFaceImagePath().split("\\|").length
                    : 1;
            builder.append("Amostras faciais: ").append(sampleCount).append('\n');
            builder.append("----------------------------------------").append('\n');
        }
        usersArea.setText(builder.toString());
    }

    private void appendStatus(String operation, ServiceResult result) {
        String prefix = result.isSuccess() ? "[SUCESSO]" : "[ERRO]";
        String currentText = statusArea.getText();
        statusArea.setText(prefix + " " + operation + ": " + result.getMessage() + "\n\n" + currentText);
    }

    private void showMessage(ServiceResult result) {
        int messageType = result.isSuccess() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE;
        JOptionPane.showMessageDialog(this, result.getMessage(), "Sistema Biométrico", messageType);
    }
}
