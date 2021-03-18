package estresador;

import client.MoleListener;
import client.PlayerConnection;
import shared.IGame;
import shared.MyConstants;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Estresador para ver cuanto aguanta el programa.
 */
public class Estresador extends Thread {

  private final static Random random = new Random();

  private final String username;
  private final PlayerConnection connection;

  private float average = 0.0f;
  private int hits = 0;


  public Estresador(String username, PlayerConnection connection) {
    this.username = username;
    this.connection = connection;
  }

  /**
   * Agrega los listeners necesarios.
   */
  public void init() {
    final String estresadorUsername = username;
    // Aleatoriamente pega cada vez que recibe señal de topo
    connection.addMoleListener(new MoleListener() {
      @Override
      public void onPositionReceived(int position) {
        // Va aumentando el tiempo que toma pegar
        average += hitRandom(position);
        hits++;
      }

      @Override
      public void onPlayerWin(String username) {
        // Acabando el juego, imprime el promedio
        average /= hits;
        System.out.println("Tiempo promedio de " + estresadorUsername + ": " + average);
      }
    });
  }

  /**
   * Golpea aleatoriamente a los topos.
   * @return
   */
  private long hitRandom(int position) {
    int action = random.nextInt(3);
    long time = System.currentTimeMillis();

    switch (action) {
        // Golpea al topo
      case 0:
        connection.hit(username, position);
        break;
        // Golpea aleatoriamente
      case 1:
        connection.hit(username, random.nextInt(MyConstants.MOLES_AREA_SIZE * MyConstants.MOLES_AREA_SIZE));
        break;
        // No hace nada
      default: break;
    }
    return System.currentTimeMillis() - time;
  }

  @Override
  public void run() {
    // Se pone como listo
    try {
      connection.getGame().isReady(username, true);
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }

  /**
   * Corre muchos hilos.
   * @param args
   */
  public static void main(String[] args) {

    String type = "login"; // Hace las pruebas de login
    //String type = "play"; // Hace las pruebas de jugar;

    PlayerConnection connection = new PlayerConnection();
    connection.connect();
    // Empieza a escuchar el broadcast
    connection.start();

    // Hace varios jugadores y estresa el servidor
    Estresador estresador;
    int numJugadores = 500;
    Estresador[] estresadores = new Estresador[numJugadores];
    try {
      // Agrega a todos los jugadores
      for(int i = 0; i < numJugadores ; i++) {
        connection.getGame().login(i + "");
        estresadores[i] = new Estresador(i + "", connection);
        estresadores[i].init(); // Inicializa listeners
      }
      // Luego todos indican que estan listos y comienzan a pegar
      for (Estresador s : estresadores)
        s.start();
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }
}