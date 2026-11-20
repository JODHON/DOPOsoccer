package Presentation;

import Domain.DOPOsoccer;
import Domain.Partido;
import java.awt.CardLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/** Main application window. Coordinates screens and the match lifecycle. */
public class DOPOsoccerGUI extends JFrame {
    private static final String MENU_SCREEN = "menu";
    private static final String GAME_SCREEN = "game";
    private static final String RESULTS_SCREEN = "results";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel screenContainer = new JPanel(cardLayout);
    private final MenuPanel menuPanel;

    private DOPOsoccer game;
    private GamePanel gamePanel;
    private ResultsPanel resultsPanel;
    private GameRenderer.GameAssets gameAssets;
    private Timer gameTimer;
    private MenuPanel.MatchConfig currentConfig;

    public DOPOsoccerGUI() {
        super("DOPOSoccer");
        menuPanel = new MenuPanel(this::startMatch);
        screenContainer.add(menuPanel, MENU_SCREEN);
        add(screenContainer);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void startMatch(MenuPanel.MatchConfig config) {
        stopGameTimer();
        currentConfig = config;
        if (gamePanel != null) screenContainer.remove(gamePanel);
        if (resultsPanel != null) screenContainer.remove(resultsPanel);
        game = new DOPOsoccer(config.getTeamOne(), config.getTeamTwo(),
            config.getMatchMode(), config.getSelectedLimit());
        gameAssets = new GameRenderer.GameAssets(config.getStadium(), config.getTeamOne(), config.getTeamTwo());

        gamePanel = new GamePanel(game, gameAssets, config.isCpuMode(), this::returnToMenu);
        screenContainer.add(gamePanel, GAME_SCREEN);
        cardLayout.show(screenContainer, GAME_SCREEN);
        gamePanel.requestFocusInWindow();
        startGameTimer();
        screenContainer.revalidate();
        screenContainer.repaint();
    }

    private void startGameTimer() {
        gameTimer = new Timer(16, event -> {
            try {
                game.actualizarJuego();
                gamePanel.repaint();
                if (game.getEstado() == Partido.Estado.TERMINADO) {
                    stopGameTimer();
                    showResults();
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                stopGameTimer();
            }
        });
        gameTimer.start();
    }

    private void showResults() {
        resultsPanel = new ResultsPanel(game,
            currentConfig.getTeamOne(), currentConfig.getTeamTwo(),
            currentConfig.isCpuMode(), gameAssets, this::returnToMenu);
        screenContainer.add(resultsPanel, RESULTS_SCREEN);
        cardLayout.show(screenContainer, RESULTS_SCREEN);
        screenContainer.revalidate();
        screenContainer.repaint();
    }

    private void returnToMenu() {
        stopGameTimer();
        cardLayout.show(screenContainer, MENU_SCREEN);
        menuPanel.requestFocusInWindow();
    }

    private void stopGameTimer() {
        if (gameTimer != null) {
            gameTimer.stop();
            gameTimer = null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DOPOsoccerGUI::new);
    }
}
