package server;

import shared.MyConstants;

import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Conexion TCP para escuchar los golpes de los jugadores.
 */
public class HitsListener extends Thread {

  private final Server server;
  private ServerSocket serverSocket;
  private boolean isListening;

  public HitsListener(Server server) {
    this.server = server;
  }

  public void stopListening() {
    isListening = false;
  }

  @Override
  public void run() {
    Socket playerSocket;
    Hit hit;

    try {
      // Escucha sin parar a golpes
      serverSocket = new ServerSocket(MyConstants.TCP_PORT);
      isListening = true;
      while(isListening) {
        // Recibe datos del jugador
        playerSocket = serverSocket.accept();
        // Abre un thread para registrar el golpe. Asi no obstaculiza a otros jugadores.
        hit = new Hit(server, playerSocket);
        hit.start();
        // Continua al siguiente jugador
      }
    }
    catch(EOFException e) {
      e.printStackTrace();
    }
    catch(IOException e) {
      e.printStackTrace();
    } finally {
      try {
        // Cierra el socket
        serverSocket.close();
      } catch (IOException e){
        e.printStackTrace();
      }
    }
  }
}
