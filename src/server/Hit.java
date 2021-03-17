package server;

import shared.Player;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hilo para registrar el golpe de un jugador sin obstaculizar a otros.
 */
public final class Hit extends Thread {

  private final Server server;
  private final Socket playerSocket;

  private DataInputStream in;
  private DataOutputStream out;

  public Hit(Server server, Socket playerSocket) {
    this.server = server;
    this.playerSocket = playerSocket;
    // Obtiene el input stream
    try {
      in = new DataInputStream(playerSocket.getInputStream());
      out = new DataOutputStream(playerSocket.getOutputStream());
    } catch (IOException ex) {
      Logger.getLogger(Hit.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  /**
   * Realiza el golpe de un usuario. Si se llega a la puntuacion maxima, el juego termina.
   * @param username
   */
  public synchronized boolean hit(String username, int position) throws InterruptedException, RemoteException {
    // Golpea al topo
    if (server.getCurrentGame().hitMole(username, position)) {
      int points = server.getCurrentGame().getScore(username);
      System.out.println(username + " pega! Ahora tiene " + points + " puntos.");
      // Checa si el juego termina
      server.tryDeclareWinner(username);
      return true;
    }
    return false;
  }

  @Override
  public void run() {
    boolean res;
    try {
      // El jugador manda su username junto con la posicion para golpear
      String[] message = in.readUTF().split(",");
      // Realiza el golpe
      res = hit(message[0], Integer.parseInt(message[1]));
      // Regresa si pego o no al jugador
      out.writeBoolean(res);
      // Cierra el socket
      playerSocket.close();
    } catch (IOException | InterruptedException ex) {
      Logger.getLogger(Hit.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
