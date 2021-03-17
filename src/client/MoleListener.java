package client;

public interface MoleListener {

  /**
   * Cuando una posicion es recibida del servidor.
   * @param position
   */
  void onPositionReceived(int position);

  /**
   * Cuando un jugador gana.
   * @param username
   */
  void onPlayerWin(String username);
}
