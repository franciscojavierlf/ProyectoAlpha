package estresador;

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


  public Estresador(String username, PlayerConnection connection) {
    this.username = username;
    this.connection = connection;
  }

  /**
   * Regresa el promedio de tiempo del estresador.
   * @return
   */
  public float getAverage() {
    return average;
  }

  /**
   * Golpea aleatoriamente a los topos.
   * @return
   */
  private long hitRandom() {
    // Golpea
    if (random.nextInt(5) == 0) {
      long time = System.currentTimeMillis();
      connection.hit(username, random.nextInt(MyConstants.MOLES_AREA_SIZE * MyConstants.MOLES_AREA_SIZE));
      return System.currentTimeMillis() - time;
    }
    return 0l;
  }

  @Override
  public void run() {
    // Se pone como listo
    try {
      connection.getGame().isReady(username, true);
    } catch (RemoteException e) {
      e.printStackTrace();
    }

    // Aleatoriamente pega
    int hits = 0;
    long time = 0l;
    long aux;
    while (true) {
      if (hits >= MyConstants.POINTS_TO_WIN) {
        average = time / hits;
        System.out.println(average);
      }
      else {
        aux = hitRandom();
        // Pego
        if (aux > 0l) {
          hits++;
          time += aux;
        }
      }
      try {
        Thread.sleep(20);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
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
    int numJugadores = 500;
    Estresador[] estresadores = new Estresador[numJugadores];
    try {
      // Agrega a todos los jugadores
      for(int i = 0; i < numJugadores ; i++) {
        connection.getGame().login(i + "");
        estresadores[i] = new Estresador(i + "", connection);
      }
      // Luego todos indican que estan listos y comienzan a pegar
      for (Estresador s : estresadores)
        s.start();
    } catch (RemoteException e) {
      e.printStackTrace();
    }


  }

}