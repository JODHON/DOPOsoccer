package Presentation;

import Domain.DOPOsoccer;
import Domain.DopoSoccerException;
import Domain.Estadisticas;
import Domain.Jugador;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/** Pantalla donde cada jugador gasta sus puntos en mejorar sus estadísticas. */
public class ImprovementsPanel extends JPanel {
    private static final Color BACKGROUND = new Color(20, 22, 38);
    private static final Color SELECTED_COLOR = new Color(255, 200, 40);
    private static final Color TITLE_COLOR = new Color(180, 200, 255);
    private static final String[] STAT_NAMES = {"VELOCIDAD", "PATADA", "SALTO", "SUPERVIVENCIA"};

    private final DOPOsoccer game;
    private final boolean cpuMode;
    private final JPanel playersPanel = new JPanel(new GridLayout(1, 2, 40, 0));

    public ImprovementsPanel(DOPOsoccer game, boolean cpuMode, Runnable onBack) {
        this.game = game;
        this.cpuMode = cpuMode;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(BACKGROUND);
        setPreferredSize(new Dimension(1280, 720));
        setBorder(BorderFactory.createEmptyBorder(30, 60, 30, 60));

        JLabel title = new JLabel("MEJORAS");
        title.setFont(new Font("Arial", Font.BOLD, 46));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(title);
        add(Box.createVerticalStrut(30));

        playersPanel.setOpaque(false);
        add(playersPanel);
        add(Box.createVerticalGlue());

        JButton backButton = new JButton("VOLVER");
        backButton.setFont(new Font("Arial", Font.BOLD, 28));
        backButton.setFocusPainted(false);
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.addActionListener(event -> onBack.run());
        add(backButton);

        refresh();
    }

    private void refresh() {
        playersPanel.removeAll();
        playersPanel.add(createPlayerColumn("JUGADOR 1", game.getJugador()));
        playersPanel.add(createPlayerColumn(cpuMode ? "CPU" : "JUGADOR 2", game.getJugador2()));
        playersPanel.revalidate();
        playersPanel.repaint();
    }

    private JPanel createPlayerColumn(String name, Jugador player) {
        JPanel column = new JPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setOpaque(false);

        column.add(createLabel(name, 30, TITLE_COLOR));
        column.add(createLabel("Puntos: " + player.getPuntos(), 26, SELECTED_COLOR));
        column.add(Box.createVerticalStrut(20));

        Estadisticas.Stat[] stats = Estadisticas.Stat.values();
        for (int i = 0; i < stats.length; i++) {
            column.add(createStatRow(STAT_NAMES[i], stats[i], player));
            column.add(Box.createVerticalStrut(12));
        }
        return column;
    }

    private JPanel createStatRow(String label, Estadisticas.Stat stat, Jugador player) {
        Estadisticas stats = player.getStats();
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        row.setOpaque(false);

        JLabel info = new JLabel(label + "  Nv " + stats.getNivel(stat) + "/" + Estadisticas.NIVEL_MAX);
        info.setFont(new Font("Arial", Font.BOLD, 20));
        info.setForeground(Color.WHITE);
        row.add(info);

        JButton buy = new JButton(stats.estaAlMaximo(stat)
            ? "MÁX" : "Subir (" + stats.getCostoSiguiente(stat) + ")");
        buy.setFont(new Font("Arial", Font.BOLD, 16));
        buy.setFocusPainted(false);
        buy.setEnabled(!stats.estaAlMaximo(stat));
        buy.addActionListener(event -> buy(player, stat));
        row.add(buy);
        return row;
    }

    private void buy(Jugador player, Estadisticas.Stat stat) {
        try {
            player.comprarMejora(stat);
            refresh();
        } catch (DopoSoccerException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                "No se pudo comprar", JOptionPane.WARNING_MESSAGE);
        }
    }

    private JLabel createLabel(String text, int size, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, size));
        label.setForeground(color);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }
}