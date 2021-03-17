package server;

import shared.IGame;
import shared.MyConstants;
import shared.Player;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Implementacion de la conexion de un jugador por RMI.
 */
public final class Game implements IGame {

  private final ArrayList<Player> players = new ArrayList();
  private final Random random = new Random();
  private final Server server;

  private int molePosition = -1;

  public Game(Server server) {
    this.server = server;
  }

  /**
   * Obtiene al jugador indicado.
   * @param username
   * @return
   */
  public Player getPlayer(String username) {
    for (Player p : players) {
      if (p.username.equals(username))
        return p;
    }
    return null;
  }

  /**
   * Obtiene la posicion actual del topo.
   * @return
   */
  public int getMolePosition() {
    return molePosition;
  }

  /**
   * Checa si el username y existe.
   * @param username
   * @return
   */
  public boolean playerExists(String username) {
    return getPlayer(username) != null;
  }

  /**
   * Checa si todos los jugadores estan listos.
   * @return
   */
  public boolean allPlayersReady() {
    int i = 0;
    while (i < players.size() && players.get(i).isReady()) i++;
    return i > 0 && i >= players.size();
  }

  /**
   * Obtiene la nueva posicion del topo.
   * @return
   */
  public int triggerNextMole() {
    // Rehabilita a todos los jugadores para pegar
    for (Player p : players)
      p.setDisabled(false);
    // Obtiene la nueva posicion
    molePosition = random.nextInt(MyConstants.MOLES_AREA_SIZE * MyConstants.MOLES_AREA_SIZE);
    return molePosition;
  }

  /**
   * Checks if a player won.
   * @param username
   * @return
   */
  public boolean isWinner(String username) {
    Player p = getPlayer(username);
    // Winner!
    return p != null && p.getScore() >= MyConstants.POINTS_TO_WIN;
  }

  /**
   * Agrega un nuevo jugador.
   * @param username
   * @return Nulo si el jugador ya existe.
   */
  private Player addNewPlayer(String username) {
    if (!playerExists(username)) {
      Player p = new Player(username);
      System.out.println("Agregado jugador " + username);
      players.add(p);
      return p;
    }
    return null;
  }

  /**
   * Un jugador hace login y recibe la informacion del servidor.
   * @param username
   * @return
   * @throws RemoteException
   */
  @Override
  public boolean login(String username) throws RemoteException {
    // Jugador ya existe
    if (playerExists(username))
      return false;
    // Jugador no existe, entonces lo agregamos a la partida
    addNewPlayer(username);
    return true;
  }

  /**
   * Un usuario intenta pegarle a un topo.
   * @param username
   * @param position
   * @return
   */
  public boolean hitMole(String username, int position) {
    Player p = getPlayer(username);
    if (p != null && !p.isDisabled() && position == molePosition) {
      p.addPoint();
      p.setDisabled(true);
      return true;
    }
    return false;
  }

  /**
   * Comienza una nueva ronda.
   */
  public void startNewMatch() {
    // Limpia el score de cada jugador y los pone en no listo
    for (Player p : players) {
      p.resetScore();
      p.setReady(false);
    }
  }

  /**
   * El jugador indica si esta listo.
   * @param username
   * @param ready
   * @throws RemoteException
   */
  @Override
  public void isReady(String username, boolean ready) throws RemoteException {
    Player p = getPlayer(username);
    if (p != null) p.setReady(ready);
  }

  /**
   * Obtiene el score del jugador.
   * @return
   * @throws RemoteException
   */
  @Override
  public int getScore(String username) throws RemoteException {
    Player p = getPlayer(username);
    return p != null ? p.getScore() : -1;
  }

  /**
   * Regresa la lista de todos los jugadores.
   * @return
   * @throws RemoteException
   */
  @Override
  public ArrayList<Player> getPlayers() throws RemoteException {
    return players;
  }
}