package Presentation;

import Domain.DOPOsoccer;
import Domain.Dificultad;
import Domain.DopoSoccerException;
import Domain.Partido;
import java.awt.CardLayout;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Main application window. Coordinates screens and the match lifecycle. */
public class DOPOsoccerGUI extends JFrame {
    private static final String MENU_SCREEN = "menu";
    private static final String GAME_SCREEN = "game";
    private static final String RESULTS_SCREEN = "results";
    private static final String IMPROVEMENTS_SCREEN = "improvements";
    private static final String SAVE_EXTENSION = "dopo";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel screenContainer = new JPanel(cardLayout);
    private final MenuPanel menuPanel;

    private DOPOsoccer game;
    private GamePanel gamePanel;
    private ResultsPanel resultsPanel;
    private ImprovementsPanel improvementsPanel;
    private GameRenderer gameRenderer;
    private Timer gameTimer;
    private String currentStadium;

    public DOPOsoccerGUI() {
        super("DOPOSoccer");
        menuPanel = new MenuPanel(this::startMatch, this::openMatch);
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
        Dificultad difficulty = menuPanel.isCpuMode() ? menuPanel.getDifficulty() : null;
        DOPOsoccer match = new DOPOsoccer(menuPanel.getTeamOne(), menuPanel.getTeamTwo(),
            menuPanel.getMatchMode(), menuPanel.getSelectedLimit(), difficulty, previous);
        showMatch(match, menuPanel.getStadium());
    }

    /** Muestra una partida nueva o recién abierta (esta llega en pausa). */
    private void showMatch(DOPOsoccer match, String stadium) {
        stopGameTimer();
        if (gamePanel != null) screenContainer.remove(gamePanel);
        if (resultsPanel != null) screenContainer.remove(resultsPanel);
        if (improvementsPanel != null) screenContainer.remove(improvementsPanel);
        game = match;
        currentStadium = (stadium == null) ? menuPanel.getStadium() : stadium;
        gameRenderer = new GameRenderer(currentStadium,
            game.getJugador().getPais(), game.getJugador2().getPais());

        gamePanel = new GamePanel(game, gameRenderer, game.isModoCpu(),
            this::returnToMenu, this::saveMatch);
        screenContainer.add(gamePanel, GAME_SCREEN);
        cardLayout.show(screenContainer, GAME_SCREEN);
        gamePanel.refreshPauseState();
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

    private void saveMatch() {
        JFileChooser chooser = createFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith("." + SAVE_EXTENSION)) {
                file = new File(file.getPath() + "." + SAVE_EXTENSION);
            }
            boolean replace = !file.exists() || JOptionPane.showConfirmDialog(this,
                "El archivo ya existe. ¿Reemplazarlo?", "Guardar partida",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
            if (replace) {
                try {
                    game.setEstadio(currentStadium);
                    game.guardar(file);
                } catch (DopoSoccerException exception) {
                    showError(exception);
                }
            }
        }
        gamePanel.refreshPauseState();
    }

    private void openMatch() {
        JFileChooser chooser = createFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            DOPOsoccer loaded = DOPOsoccer.abrir(chooser.getSelectedFile());
            showMatch(loaded, loaded.getEstadio());
        } catch (DopoSoccerException exception) {
            showError(exception);
        }
    }

    private JFileChooser createFileChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(
            "Partidas DOPOSoccer (*." + SAVE_EXTENSION + ")", SAVE_EXTENSION));
        return chooser;
    }

    private void showError(DopoSoccerException exception) {
        JOptionPane.showMessageDialog(this, exception.getMessage(),
            "DOPOSoccer", JOptionPane.ERROR_MESSAGE);
    }

    private void showResults() {
    resultsPanel = new ResultsPanel(game,
        game.getJugador().getPais(), game.getJugador2().getPais(),
        game.isModoCpu(), gameRenderer,
        this::returnToMenu, this::showImprovements);
        screenContainer.add(resultsPanel, RESULTS_SCREEN);
        cardLayout.show(screenContainer, RESULTS_SCREEN);
        screenContainer.revalidate();
        screenContainer.repaint();
    }

    private void showImprovements() {
        if (improvementsPanel != null) screenContainer.remove(improvementsPanel);
        improvementsPanel = new ImprovementsPanel(game, game.isModoCpu(), this::backToResults);
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