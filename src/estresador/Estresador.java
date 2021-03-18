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
    // Aleatoriamente pega cada vez que recibe señal de topo
    connection.addMoleListener(new MoleListener() {
      @Override
      public void onPositionReceived(int position) {
        // Va aumentando el tiempo que toma pegar
        average += hitRandom(position);
        hits++;
      }

      @Override
      public void onPlayerWin(String winner) {
        // Acabando el juego, imprime el promedio
        average /= hits;
        System.out.println("Tiempo promedio de golpe para " + username + ": " + average);
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
    try {
      // Hace login
      long time = System.currentTimeMillis();
      connection.getGame().login(username);
      time = System.currentTimeMillis() - time;
      System.out.println("Tiempo de login para " + username + ": " + time);
      // Luego indica que esta listo
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

    PlayerConnection connection = new PlayerConnection();
    connection.connect();
    // Empieza a escuchar el broadcast
    connection.start();

    // Hace varios jugadores y estresa el servidor
    Estresador estresador;
    int numJugadores = 10;
    Estresador[] estresadores = new Estresador[numJugadores];
    String username;
    // Agrega a todos los jugadores
    for(int i = 0; i < numJugadores ; i++) {
      // Calcula el tiempo promedio de login
      username = i + "";
      estresadores[i] = new Estresador(username, connection);
      estresadores[i].init(); // Inicializa listeners (no afecta al estresador)
    }
    // Luego todos hacen login, indican que estan listos y comienzan a pegar
    for (Estresador s : estresadores)
      s.start();
  }
}