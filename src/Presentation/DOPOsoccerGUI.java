package Presentation;

import Domain.DOPOsoccer;
import Domain.Dificultad;
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
    private static final String IMPROVEMENTS_SCREEN = "improvements";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel screenContainer = new JPanel(cardLayout);
    private final MenuPanel menuPanel;

    private DOPOsoccer game;
    private GamePanel gamePanel;
    private ResultsPanel resultsPanel;
    private ImprovementsPanel improvementsPanel;
    private GameRenderer gameRenderer;
    private Timer gameTimer;
    

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

    /** Parte desde el menu; conserva puntos y mejoras de la partida anterior (null la primera vez). */
    private void startMatch() {
        launchMatch(game);
    }

    private void launchMatch(DOPOsoccer previous) {
        stopGameTimer();
        if (gamePanel != null) screenContainer.remove(gamePanel);
        if (resultsPanel != null) screenContainer.remove(resultsPanel);
        if (improvementsPanel != null) screenContainer.remove(improvementsPanel);
        Dificultad difficulty = menuPanel.isCpuMode() ? menuPanel.getDifficulty() : null;
        game = new DOPOsoccer(menuPanel.getTeamOne(), menuPanel.getTeamTwo(),
            menuPanel.getMatchMode(), menuPanel.getSelectedLimit(), difficulty, previous);
        gameRenderer = new GameRenderer(menuPanel.getStadium(),
            menuPanel.getTeamOne(), menuPanel.getTeamTwo());

        gamePanel = new GamePanel(game, gameRenderer, menuPanel.isCpuMode(), this::returnToMenu);
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
        menuPanel.getTeamOne(), menuPanel.getTeamTwo(),
        menuPanel.isCpuMode(), gameRenderer,
        this::returnToMenu, this::showImprovements);
        screenContainer.add(resultsPanel, RESULTS_SCREEN);
        cardLayout.show(screenContainer, RESULTS_SCREEN);
        screenContainer.revalidate();
        screenContainer.repaint();
    }

    private void showImprovements() {
        if (improvementsPanel != null) screenContainer.remove(improvementsPanel);
        improvementsPanel = new ImprovementsPanel(game, menuPanel.isCpuMode(), this::backToResults);
        screenContainer.add(improvementsPanel, IMPROVEMENTS_SCREEN);
        cardLayout.show(screenContainer, IMPROVEMENTS_SCREEN);
        screenContainer.revalidate();
        screenContainer.repaint();
    }

    private void backToResults() {
        cardLayout.show(screenContainer, RESULTS_SCREEN);
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