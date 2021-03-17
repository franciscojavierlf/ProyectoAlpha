package shared;

/**
 * Clase que representa a un jugador.
 */
public final class Player {

  public final String username;

  private int score = 0;
  private int matchesWon = 0;
  private boolean ready = false;
  private boolean disabled = true;

  public Player(String username) {
    this.username = username;
  }

  /**
   * Si el jugador esta listo para comenzar la partida.
   * @return
   */
  public boolean isReady() {
    return ready;
  }

  /**
   * Indica si el jugador esta listo para iniciar la partida.
   * @param ready
   */
  public void setReady(boolean ready) {
    this.ready = ready;
  }

  /**
   * Indica si un jugador esta deshabilitado, refiriendose a que ya pego
   * y no puede volver a pegar hasta el siguiente topo.
   * @param disabled
   */
  public void setDisabled(boolean disabled) {
    this.disabled = disabled;
  }

  /**
   * Checa si el jugador esta deshabilitado para pegar.
   * @return
   */
  public boolean isDisabled() {
    return disabled;
  }

  /**
   * El score de la partida actual.
   * @return
   */
  public int getScore() {
    return score;
  }

  /**
   * La cantidad de partidas que ha ganado.
   * @return
   */
  public int getMatchesWon() {
    return matchesWon;
  }

  /**
   * Agrega un punto por pegarle a un topo.
   * @return
   */
  public int addPoint() {
    return ++score;
  }

  /**
   * Quita un punto por un falso golpe.
   * @return
   */
  public int removePoint() {
    return --score;
  }

  /**
   * Reinicia el score de la partida.
   */
  public void resetScore() {
    score = 0;
  }

  /**
   * Indica que el jugador gano una partida.
   * @return
   */
  public int matchWon() {
    return ++matchesWon;
  }
}
