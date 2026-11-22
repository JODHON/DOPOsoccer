package Presentation;

import Domain.DOPOsoccer;
import Domain.Jugador;
import Domain.Partido;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Displays the final score and winner after a match. */
public class ResultsPanel extends JPanel {
    private static final Color BACKGROUND = new Color(20, 22, 38);
    private static final Color SELECTED_COLOR = new Color(255, 200, 40);
    private static final Color TITLE_COLOR = new Color(180, 200, 255);

    public ResultsPanel(DOPOsoccer game, String teamOne, String teamTwo,
                        boolean cpuMode, GameRenderer renderer, Runnable onReturnToMenu,Runnable goToImprovements) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(BACKGROUND);
        setPreferredSize(new Dimension(1280, 720));
        add(Box.createVerticalGlue());

        String secondPlayerName = cpuMode ? "CPU" : "JUGADOR 2";
        String result = getResultText(game, cpuMode);
        String winnerTeam = getWinnerTeam(game, teamOne, teamTwo);

        addCenteredLabel("FIN DEL JUEGO", 54, Color.WHITE);
        add(Box.createVerticalStrut(30));
        addWinnerFlag(winnerTeam, renderer);
        addCenteredLabel(result, 40, SELECTED_COLOR);
        add(Box.createVerticalStrut(15));

        int[] goals = game.getGoles();
        addCenteredLabel("JUGADOR 1  " + goals[0] + " - " + goals[1]
            + "  " + secondPlayerName, 28, TITLE_COLOR);
        add(Box.createVerticalStrut(10));
        addPointsEarned(game, secondPlayerName);
        add(Box.createVerticalStrut(30));

        JButton returnButton = new JButton("VOLVER AL MENÚ");
        JButton improvementsButton = new JButton("MEJORAS");
      
        returnButton.setFont(new Font("Arial", Font.BOLD, 28));
        returnButton.setFocusPainted(false);
        returnButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        returnButton.addActionListener(event -> onReturnToMenu.run());
        add(returnButton);
        add(Box.createVerticalGlue());
        
        improvementsButton.setFont(new Font("Arial", Font.BOLD, 28));
        improvementsButton.setFocusPainted(false);
        improvementsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        improvementsButton.addActionListener(event -> goToImprovements.run());
        add(improvementsButton);
        add(Box.createVerticalGlue());
    }

    private void addPointsEarned(DOPOsoccer game, String secondPlayerName) {
        Jugador winner = game.getGanador();
        if (winner == null) return;
        boolean firstWins = winner == game.getJugador();
        int firstPoints = firstWins ? Partido.PUNTOS_GANADOR : Partido.PUNTOS_PERDEDOR;
        int secondPoints = firstWins ? Partido.PUNTOS_PERDEDOR : Partido.PUNTOS_GANADOR;
        addCenteredLabel("PUNTOS GANADOS:  JUGADOR 1 +" + firstPoints
            + "   " + secondPlayerName + " +" + secondPoints, 22, SELECTED_COLOR);
    }

    private String getResultText(DOPOsoccer game, boolean cpuMode) {
        Jugador winner = game.getGanador();
        if (winner == game.getJugador()) return "¡GANA JUGADOR 1!";
        if (winner == game.getJugador2()) return "¡GANA "
            + (cpuMode ? "CPU" : "JUGADOR 2") + "!";
        return "¡EMPATE!";
    }

    private String getWinnerTeam(DOPOsoccer game, String teamOne, String teamTwo) {
        Jugador winner = game.getGanador();
        if (winner == game.getJugador()) return teamOne;
        if (winner == game.getJugador2()) return teamTwo;
        return null;
    }

    private void addWinnerFlag(String team, GameRenderer renderer) {
        if (team == null) return;
        try {
            java.awt.Image flag = renderer.loadImage("/banderas/bandera_" + team + ".png");
            if (flag != null) {
                JLabel flagLabel = new JLabel(new ImageIcon(
                    flag.getScaledInstance(150, 100, java.awt.Image.SCALE_SMOOTH)));
                flagLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                add(flagLabel);
                add(Box.createVerticalStrut(20));
            }
        } catch (Exception exception) {
            System.out.println("Could not load winner flag for: " + team);
        }
    }

    private void addCenteredLabel(String text, int fontSize, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, fontSize));
        label.setForeground(color);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(label);
    }
}