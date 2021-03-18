package server;

import shared.IGame;
import shared.MyConstants;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Clase para inicializar el servidor. Contiene toda la informacion de la sesion actual.
 */
public final class Server {

  private MolesBroadcast broadcast;
  private HitsListener listener;
  private final Game game;

  public Server() {
    game = new Game(this); // Estado del juego
    broadcast = new MolesBroadcast(this);
    listener = new HitsListener(this);
  }

  /**
   * Regresa el juego que esta corriendo actualmente.
   * @return
   */
  public Game getCurrentGame() {
    return game;
  }

  /**
   * Checa si el jugador ya gano. Hace un broadcast para avisar a los otros jugadores si si.
   * @param username
   */
  public void tryDeclareWinner(String username) throws InterruptedException {
    if (game.isWinner(username)) {
      broadcast.declareWinner(username);
      // Espera a que acabe el broadcast para terminar de escuchar golpes
      broadcast.join();
      listener.stopListening();
      listener.join();
      // Comienza una nueva partida
      startNewMatch();
    }
  }

  /**
   * Inicializa el servicio RMI.
   * @throws RemoteException
   */
  private void startRMI() throws RemoteException {
    // Levanta el RMI
    System.setProperty("java.security.policy", MyConstants.PATH + "server/server.policy");
    if (System.getSecurityManager() == null) {
      System.setSecurityManager(new SecurityManager());
    }
    LocateRegistry.createRegistry(MyConstants.REGISTRY_PORT);
    IGame stub = (IGame) UnicastRemoteObject.exportObject(game, 0);
    Registry registry = LocateRegistry.getRegistry();
    registry.rebind(MyConstants.REGISTRY_NAME, stub);
  }

  /**
   * Comienza a correr el juego.
   */
  private void startNewMatch() throws InterruptedException {
    // Comienza una nueva partida
    game.startNewMatch();

    // Escucha que todos los jugadores esten listos cada medio segundo
    System.out.println("Esperando jugadores...");
    while(!game.hasOnlinePlayers() || !game.allPlayersReady()) {
      Thread.sleep(1000);
    }
    System.out.println("Listo! A comenzar.");
    // Comienza el broadcast y a escuchar golpes
    broadcast.start();
    listener.start();
  }

  /**
   * Inicializa el servidor para recibir a nuevos jugadores y comenzar el juego.
   */
  public void start() {
    try {
      // Arranca el RMI
      startRMI();
      // Arranca una nueva partida
      startNewMatch();
    } catch (RemoteException | InterruptedException e) {
      e.printStackTrace();
    }
  }

    /**
   * Inicia el manager.
   * @param args
   */
  public static void main(String[] args) {
    Server server = new Server();
    server.start();
  }
}
