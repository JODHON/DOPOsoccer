package Presentation;

import Domain.DOPOsoccer;
import Domain.Partido;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;

/** Match view. It handles keyboard input and delegates drawing and pause UI. */
public class GamePanel extends JPanel {
    private final DOPOsoccer game;
    private final GameRenderer.GameAssets assets;
    private final boolean cpuMode;
    private final GameRenderer renderer;
    private final PausePanel pausePanel;
    private boolean pauseKeyHeld;

    public GamePanel(DOPOsoccer game, GameRenderer.GameAssets assets, boolean cpuMode,
                     Runnable onReturnToMenu) {
        this.game = game;
        this.assets = assets;
        this.cpuMode = cpuMode;
        renderer = new GameRenderer();

        setPreferredSize(new Dimension(1280, 720));
        setFocusable(true);
        setLayout(new GridBagLayout());
        pausePanel = new PausePanel(this::togglePause, onReturnToMenu);
        pausePanel.setVisible(false);
        add(pausePanel);
        addKeyboardControls();
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
        renderer.draw((Graphics2D) graphics, this, game, assets, cpuMode);
    }
}
