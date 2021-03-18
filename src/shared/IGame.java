package shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Interfaz para hacer el login del jugador.
 */
public interface IGame extends Remote {

  /**
   * Conecta al jugador con el servidor.
   * @param username
   * @return
   * @throws RemoteException
   */
  public boolean login(String username) throws RemoteException;

  /**
   * Desconecta al jugador del servidor.
   * @param username
   * @return
   * @throws RemoteException
   */
  public boolean logout(String username) throws RemoteException;

  /**
   * El jugador indica que ya esta listo para iniciar.
   * @param username
   * @throws RemoteException
   */
  public void isReady(String username, boolean ready) throws RemoteException;

  /**
   * Obtiene el score del jugador.
   * @param username
   * @return
   * @throws RemoteException
   */
  public int getScore(String username) throws RemoteException;

  /**
   * Obtiene la lista de todos los jugadores con su informacion.
   * @return
   * @throws RemoteException
   */
  public ArrayList<Player> getPlayers() throws RemoteException;
}
