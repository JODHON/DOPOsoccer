package Presentation;

import Domain.DOPOsoccer;
import Domain.Partido;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Match view. Handles keyboard input, pause state and match rendering. */
public class GamePanel extends JPanel {
    private final DOPOsoccer game;
    private final GameRenderer renderer;
    private final boolean cpuMode;
    private final JPanel pausePanel;
    private boolean pauseKeyHeld;

    public GamePanel(DOPOsoccer game, GameRenderer renderer, boolean cpuMode,
                     Runnable onReturnToMenu) {
        this.game = game;
        this.renderer = renderer;
        this.cpuMode = cpuMode;

        setPreferredSize(new Dimension(1280, 720));
        setFocusable(true);
        setLayout(new GridBagLayout());
        pausePanel = createPausePanel(onReturnToMenu);
        pausePanel.setVisible(false);
        add(pausePanel);
        addKeyboardControls();
    }

    private JPanel createPausePanel(Runnable onReturnToMenu) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel title = new JLabel("PAUSA");
        title.setFont(new Font("Arial", Font.BOLD, 64));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(8));

        JLabel help = new JLabel("ESC o P para continuar");
        help.setFont(new Font("Arial", Font.PLAIN, 16));
        help.setForeground(new Color(180, 200, 255));
        help.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(help);
        panel.add(Box.createVerticalStrut(30));

        JButton resumeButton = createPauseButton("REANUDAR");
        resumeButton.addActionListener(event -> togglePause());
        panel.add(resumeButton);
        panel.add(Box.createVerticalStrut(14));

        JButton menuButton = createPauseButton("VOLVER AL MENÚ");
        menuButton.addActionListener(event -> onReturnToMenu.run());
        panel.add(menuButton);
        return panel;
    }

    private JButton createPauseButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 26));
        button.setFocusable(false);
        button.setAlignmentX(CENTER_ALIGNMENT);
        Dimension size = new Dimension(320, 52);
        button.setPreferredSize(size);
        button.setMaximumSize(size);
        return button;
    }

    public void refreshPauseState() {
        pausePanel.setVisible(game.isPausado());
        requestFocusInWindow();
        revalidate();
        repaint();
    }

    private void togglePause() {
        if (game.getEstado() == Partido.Estado.TERMINADO) return;
        if (game.isPausado()) game.reanudar();
        else game.pausar();
        refreshPauseState();
    }

    private void addKeyboardControls() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                int keyCode = event.getKeyCode();
                if (keyCode == KeyEvent.VK_ESCAPE || keyCode == KeyEvent.VK_P) {
                    if (!pauseKeyHeld) togglePause();
                    pauseKeyHeld = true;
                } else {
                    game.teclaPresionada(keyCode);
                }
            }

            @Override
            public void keyReleased(KeyEvent event) {
                int keyCode = event.getKeyCode();
                if (keyCode == KeyEvent.VK_ESCAPE || keyCode == KeyEvent.VK_P) {
                    pauseKeyHeld = false;
                } else {
                    game.teclaLiberada(keyCode);
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        renderer.draw((Graphics2D) graphics, this, game, cpuMode);
    }
}
