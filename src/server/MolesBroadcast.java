package server;

import shared.MyConstants;
import shared.Player;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Broadcast para señalar la posicion de los topos.
 */
public class MolesBroadcast extends Thread {

  private final Server server;

  private MulticastSocket socket;
  private InetAddress address;
  private String winner; // Si ya hay un ganador

  public MolesBroadcast(Server server) {
    this.server = server;
  }

  /**
   * Declara al ganador de la partida.
   * @param username
   */
  public void declareWinner(String username) {
    winner = username;
  }

  /**
   * Comienza la conexion multicast.
   */
  private void startConnection() {
    try {
      // Obtiene el inet address y el socket
      address = InetAddress.getByName(MyConstants.MULTICAST_IP);
      socket = new MulticastSocket(MyConstants.MULTICAST_PORT);
      socket.joinGroup(address);
      socket.setTimeToLive(1);
    } catch (UnknownHostException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Hace un broadcast de un mensaje.
   * @param msg
   */
  private void broadcastMessage(String msg) throws IOException {
    byte[] m = msg.getBytes();
    DatagramPacket msgOut = new DatagramPacket(m, m.length, address, MyConstants.MULTICAST_PORT);
    socket.send(msgOut);
  }

  /**
   * Hace un broadcast de una posicion aleatoria de un topo.
   */
  private void broadcastMole() {
    try {
      // Crea el mensaje de la posicion y lo manda
      int position = server.getCurrentGame().triggerNextMole();
      broadcastMessage(position + "");
  } catch (IOException ex) {
    Logger.getLogger(MolesBroadcast.class.getName()).log(Level.SEVERE, null, ex);
  }
  }

  /**
   * Hace un broadcast del ganador.
   */
  private void broadcastWinner() {
    if (winner == null) return;

    System.out.println("Ha ganado " + winner);
    try {
      // Detiene el juego y luego manda el ganador
      broadcastMessage("WINNER");
      broadcastMessage(winner);
    } catch (IOException ex) {
      Logger.getLogger(MolesBroadcast.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  @Override
  public void run(){
    startConnection();
    // Mientras no haya ganador el juego sigue
    try {
      while (winner == null) {
        // Hace el broadcast y se espera cierto tiempo
        broadcastMole();
        Thread.sleep(MyConstants.GAME_SPEED);
      }
      // Ya hubo un ganador
      broadcastWinner();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      // Cierra el socket
      socket.close();
    }
  }


}
