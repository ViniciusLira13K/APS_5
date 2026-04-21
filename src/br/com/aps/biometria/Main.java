package br.com.aps.biometria;

import br.com.aps.biometria.repository.UserRepository;
import br.com.aps.biometria.service.BiometricAuthService;
import br.com.aps.biometria.ui.MainFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UserRepository repository = new UserRepository();
                BiometricAuthService service = new BiometricAuthService(repository);
                MainFrame frame = new MainFrame(service);
                frame.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        null,
                        "Não foi possível iniciar o sistema de biometria facial.\n\n" + e.getMessage(),
                        "Erro de Inicialização",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
