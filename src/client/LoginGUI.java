package client;

import shared.MyConstants;
import shared.IGame;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GUI para realizar el login al servidor.
 */
public class LoginGUI extends JFrame {

  private PlayerConnection connection;

  private JPanel panel1;
  private JTextField usernameText;
  private JButton loginButton;
  private JLabel messageText;

  public LoginGUI() {
    // Pone los parametros default del jframe
    setContentPane(panel1);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    pack();
    setVisible(true);

    // Accion del boton para entrar
    loginButton.addActionListener(e -> {
      // Obtiene el username
      String username = usernameText.getText();
      // ERROR: sin nombre adecuado
      if (username.trim().isEmpty())
        System.err.println("Nombre invalido");
        // Comienza conexion
      else {
        connection = new PlayerConnection();
        connection.connect();
        try {
          if (connection.getGame().login(username)) {
            System.out.println("Entrando al juego...");
            GameboardGUI board = new GameboardGUI(username, connection);
            setVisible(false);
          } else System.err.println("Jugador ya existente");
        } catch (RemoteException remoteException) {
          remoteException.printStackTrace();
        }
      }
    });
  }

  /**
   * Abre la ventana.
   *
   * @param args
   */
  public static void main(String args[]) {

    try {
      for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
        if ("Nimbus".equals(info.getName())) {
          UIManager.setLookAndFeel(info.getClassName());
          break;
        }
      }
    } catch (ClassNotFoundException ex) {
      Logger.getLogger(LoginGUI.class.getName()).log(Level.SEVERE, null, ex);
    } catch (InstantiationException ex) {
      Logger.getLogger(LoginGUI.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      Logger.getLogger(LoginGUI.class.getName()).log(Level.SEVERE, null, ex);
    } catch (UnsupportedLookAndFeelException ex) {
      Logger.getLogger(LoginGUI.class.getName()).log(Level.SEVERE, null, ex);
    }

    LoginGUI login = new LoginGUI();
  }

}
