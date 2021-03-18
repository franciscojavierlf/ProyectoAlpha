package client;

import shared.IGame;
import shared.MyConstants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase que se comunica con el servidor.
 */
public final class PlayerConnection extends Thread {

  private static int failedHits = 0;

  private IGame game;

  private MulticastSocket multicastSocket;
  private InetAddress address;

  // Listeners para cuando llega la posicion del topo
  private ArrayList<MoleListener> listeners = new ArrayList<>();

  /**
   * Regresa el juego.
   * @return
   */
  public IGame getGame() {
    return this.game;
  }

  /**
   * Agrega un listener de los topos.
   * @param l
   */
  public void addMoleListener(MoleListener l) {
    listeners.add(l);
  }

  /**
   * Se conecta al rmi
   */
  public void connect() {
    try {
      // Agarra el policy
      System.setProperty("java.security.policy", MyConstants.PATH + "client/client.policy");
      if (System.getSecurityManager() == null) {
        System.setSecurityManager(new SecurityManager());
      }
      // Registra el RMI
      Registry registry = LocateRegistry.getRegistry(MyConstants.TCP_IP);
      game = (IGame) registry.lookup(MyConstants.REGISTRY_NAME);
    } catch (AccessException accessException) {
      accessException.printStackTrace();
    } catch (RemoteException e) {
      e.printStackTrace();
    } catch (NotBoundException e) {
      e.printStackTrace();
    }
  }

  /**
   * Golpea un topo y le avisa al servidor.
   */
  public boolean hit(String username, int position) {
    Socket socket = null;
    try {
      socket = new Socket(MyConstants.TCP_IP, MyConstants.TCP_PORT);
      DataInputStream in = new DataInputStream(socket.getInputStream());
      DataOutputStream out = new DataOutputStream(socket.getOutputStream());
      // Manda la señal de golpear
      out.writeUTF(username + "," + position);
      // Espera a recibir respuesta
      return in.readBoolean();
    } catch (IOException ex) {
      failedHits++;
      System.err.println("Error al golpear! Van " + failedHits);
    }
    finally {
      if(socket != null)
        try {
        // Cierra el socket
        socket.close();
      } catch (IOException ex) {
        Logger.getLogger(PlayerConnection.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    return false;
  }

  /**
   * Se conecta al broadcast del servidor.
   * @throws IOException
   */
  private void connectToBroadcast() throws IOException {
    address = InetAddress.getByName(MyConstants.MULTICAST_IP);
    multicastSocket = new MulticastSocket(MyConstants.MULTICAST_PORT);
    multicastSocket.joinGroup(address);
  }

  /**
   * Escucha constantemente al broadcast.
   */
  private void listenToBroadcast() throws IOException {

    byte[] buffer;
    DatagramPacket msgIn;
    String msg;

    // Escucha constantemente
    while(true) {
      buffer = new byte[100];
      msgIn = new DatagramPacket(buffer, buffer.length);
      // Recibe el mensaje
      multicastSocket.receive(msgIn);
      msg = new String(msgIn.getData(), 0, msgIn.getLength());
      // Ganador
      if (msg.equals("WINNER")) {
        // Se espera a recibir el nombre del ganador
        multicastSocket.receive(msgIn);
        String winner = new String(msgIn.getData(), 0, msgIn.getLength());
        // Anuncia a los listeners
        for (MoleListener l : listeners)
          l.onPlayerWin(winner);
      }
      // Manda la posicion a los listeners
      else for (MoleListener l : listeners)
        l.onPositionReceived(Integer.parseInt(msg));
    }
  }

  @Override
  public void run() {
    try {
      // Se conecta al broadcast
      connectToBroadcast();
      // Escucha constantemente para ver donde estan los topos
      listenToBroadcast();
    } catch (IOException ex) {
      Logger.getLogger(PlayerConnection.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
