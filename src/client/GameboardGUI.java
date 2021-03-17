package client;

import shared.IGame;
import shared.MyConstants;
import shared.Player;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;

public final class GameboardGUI extends JFrame {

  private final String username;
  private final PlayerConnection connection;

  private JPanel panel;
  private JButton[] buttons;
  private JButton readyButton;
  private JTextArea output;
  private JTextArea scoreOutput;

  private boolean disabled = false;
  private int score = 0;

  GameboardGUI(String username, PlayerConnection connection) {
    this.username = username;
    this.connection = connection;

    panel = new JPanel(new GridLayout(MyConstants.MOLES_AREA_SIZE + 1, MyConstants.MOLES_AREA_SIZE, 5, 5));
    setContentPane(panel);
    // Agrega los botones
    createGrid();
    // Agrega otras cosas
    addReadyButton();
    addOutput();
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    pack();
    setVisible(true);

    // Agrega un listener para escuchar la posicion de los topos
    connection.addMoleListener(new MoleListener() {
      @Override
      public void onPositionReceived(int position) {
        setMole(position);
      }

      @Override
      public void onPlayerWin(String username) {
        playerWin(username);
      }
    });

    // Empieza a escuchar el broadcast
    connection.start();
  }

  /**
   * Crea los botones.
   */
  private void createGrid() {
    JButton button;

    buttons = new JButton[MyConstants.MOLES_AREA_SIZE * MyConstants.MOLES_AREA_SIZE];
    for (int y = 0; y < MyConstants.MOLES_AREA_SIZE; y++)
      for (int x = 0; x < MyConstants.MOLES_AREA_SIZE; x++) {
        button = new JButton();
        button.setName((x + y * MyConstants.MOLES_AREA_SIZE) + "");

        button.setPreferredSize(new Dimension(50, 50));
        // Accion para mandar el golpe al servidor
        button.addActionListener(l -> hit(Integer.parseInt(((JButton) l.getSource()).getName())));
        panel.add(button);
        buttons[x + y * MyConstants.MOLES_AREA_SIZE] = button;
      }
  }

  /**
   * Golpea al topo en cierta posicion.
   * @param position
   */
  private void hit(int position) {
    if (disabled) return;

    disabled = true;
    boolean missed = !connection.hit(username, position);
    if (missed) {
      output.setText("Fallaste!");
      setGridColor(Color.YELLOW);
    }
    else {
      output.setText("Golpeado!");
      setGridColor(Color.GREEN);
      score++;
      scoreOutput.setText("Score: " + score);
    }
  }

  /**
   * El boton para estar listo.
   */
  private void addReadyButton() {
    readyButton = new JButton("Listo");
    readyButton.addActionListener(l -> {
      try {
        readyButton.setEnabled(false);
        // Le avisa al server que ya esta listo
        connection.getGame().isReady(username, true);
      } catch (RemoteException e) {
        e.printStackTrace();
      }
    });
    panel.add(readyButton);
  }

  /**
   * Agrega el panel para mostrar mensajes.
   */
  private void addOutput() {
    output = new JTextArea();
    output.setPreferredSize(new Dimension(100, 100));
    output.setEnabled(false);
    panel.add(output);
    scoreOutput = new JTextArea();
    scoreOutput.setPreferredSize(new Dimension(100, 100));
    scoreOutput.setEnabled(false);
    panel.add(scoreOutput);
    scoreOutput.setText("Score: " + score);
  }

  /**
   * Cuando un jugador gana.
   * @param username
   */
  private void playerWin(String username) {
    output.setText(username + " ha ganado!");
    scoreOutput.setText("Score: " + score);
    setGridColor(Color.BLACK);
    readyButton.setEnabled(true);
  }

  /**
   * Pone a todos los botones de un color.
   * @param color
   */
  private void setGridColor(Color color) {
    for (JButton b : buttons)
      b.setBackground(color);
  }

  /**
   * Colorea un boton del color de un topo.
   * @param position
   */
  public void setMole(int position) {
    // Colorea el boton y descolorea los otros
    setGridColor(Color.GRAY);
    buttons[position].setBackground(Color.RED);
    disabled = false;
  }
}
