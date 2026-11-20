package Presentation;

import Domain.Partido;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/** Main menu. It gathers selections and reports them as a MatchConfig. */
public class MenuPanel extends JPanel {
    private static final String[] TEAMS = {"alemania", "corea", "camerun", "nigeria", "usa"};
    private static final String[] STADIUMS = {
        "alemania", "corea", "camerun", "nigeria", "usa", "bogota", "bogota_es_roja"
    };
    private static final String FLAG_FOLDER = "/banderas/bandera_";
    private static final String THUMBNAIL_FOLDER = "/miniaturas/miniatura_";
    private static final Color MENU_BACKGROUND = new Color(20, 22, 38);

    private String teamOne = TEAMS[0];
    private String teamTwo = TEAMS[1];
    private String stadium = STADIUMS[0];
    private final MatchSettingsPanel settingsPanel;
    private final TeamSelectorPanel secondTeamSelector;

    public MenuPanel(Consumer<MatchConfig> onStart) {
        setLayout(new BorderLayout());
        setBackground(MENU_BACKGROUND);
        setPreferredSize(new Dimension(1280, 720));

        JLabel title = new JLabel("DOPOSOCCER", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 46));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
        add(title, BorderLayout.NORTH);

        TeamSelectorPanel firstTeamSelector = new TeamSelectorPanel(
            "JUGADOR 1", TEAMS, FLAG_FOLDER, 60, 40, 1, teamOne, value -> teamOne = value);
        firstTeamSelector.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 20));
        add(firstTeamSelector, BorderLayout.WEST);

        secondTeamSelector = new TeamSelectorPanel(
            "JUGADOR 2", TEAMS, FLAG_FOLDER, 60, 40, 1, teamTwo, value -> teamTwo = value);
        secondTeamSelector.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 40));
        add(secondTeamSelector, BorderLayout.EAST);

        TeamSelectorPanel stadiumSelector = new TeamSelectorPanel(
            "ESTADIO", STADIUMS, THUMBNAIL_FOLDER, 190, 107, 4, stadium, value -> stadium = value);
        JPanel centeredStadiums = new JPanel(new GridBagLayout());
        centeredStadiums.setOpaque(false);
        centeredStadiums.add(stadiumSelector);
        add(centeredStadiums, BorderLayout.CENTER);

        settingsPanel = new MatchSettingsPanel(
            cpuMode -> secondTeamSelector.setTitle(cpuMode ? "CPU" : "JUGADOR 2"));
        add(createBottomPanel(onStart), BorderLayout.SOUTH);
    }

    private JPanel createBottomPanel(Consumer<MatchConfig> onStart) {
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setOpaque(false);
        bottom.add(settingsPanel);
        bottom.add(Box.createVerticalStrut(10));

        JButton startButton = new JButton("JUGAR");
        startButton.setFont(new Font("Arial", Font.BOLD, 30));
        startButton.setFocusPainted(false);
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.addActionListener(event -> onStart.accept(createConfig()));
        bottom.add(startButton);
        bottom.add(Box.createVerticalStrut(15));
        return bottom;
    }

    private MatchConfig createConfig() {
        return new MatchConfig(teamOne, teamTwo, stadium,
            settingsPanel.isCpuMode(), settingsPanel.getMatchMode(),
            settingsPanel.getTimeLimitSeconds(), settingsPanel.getGoalLimit());
    }
    // Selector used for both teams and stadiums in this menu.
    private static class TeamSelectorPanel extends JPanel {
    private static final Color SELECTED_COLOR = new Color(255, 200, 40);
    private static final Color TITLE_COLOR = new Color(180, 200, 255);
    private static final Color BUTTON_COLOR = new Color(40, 44, 70);
    private static final Color BORDER_COLOR = new Color(70, 75, 110);

    private final List<JToggleButton> buttons = new ArrayList<>();
    private final JLabel heading;
    private String selectedValue;

    public TeamSelectorPanel(String title, String[] options, String imageFolder,
                             int imageWidth, int imageHeight, int columns,
                             String initialValue, Consumer<String> onSelection) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        selectedValue = initialValue;

        heading = new JLabel(title);
        heading.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 20));
        heading.setForeground(TITLE_COLOR);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(heading);
        add(Box.createVerticalStrut(8));

        JPanel grid = new JPanel(new GridLayout(0, columns, 12, 8));
        grid.setOpaque(false);
        for (String option : options) {
            grid.add(createOptionButton(option, imageFolder, imageWidth, imageHeight, onSelection));
        }

        JPanel centeredGrid = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centeredGrid.setOpaque(false);
        centeredGrid.add(grid);
        add(centeredGrid);
    }

    public String getSelectedValue() {
        return selectedValue;
    }

    public void setTitle(String title) {
        heading.setText(title);
    }

    private JToggleButton createOptionButton(String option, String folder,
                                              int width, int height,
                                              Consumer<String> onSelection) {
        String label = option.replace('_', ' ').toUpperCase();
        JToggleButton button = new JToggleButton(label);
        button.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 11));
        button.setForeground(Color.WHITE);
        button.setBackground(BUTTON_COLOR);
        button.setFocusPainted(false);
        button.setVerticalTextPosition(JToggleButton.BOTTOM);
        button.setHorizontalTextPosition(JToggleButton.CENTER);
        button.setSelected(option.equals(selectedValue));

        Image image = loadImage(folder + option + ".png");
        if (image != null) {
            button.setIcon(new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_SMOOTH)));
        }
        updateBorder(button);
        button.addActionListener(event -> {
            selectedValue = option;
            for (JToggleButton other : buttons) {
                other.setSelected(other == button);
                updateBorder(other);
            }
            onSelection.accept(option);
        });
        buttons.add(button);
        return button;
    }

    private Image loadImage(String path) {
        try {
            return javax.imageio.ImageIO.read(getClass().getResource(path));
        } catch (Exception exception) {
            System.out.println("Could not load image: " + path);
            return null;
        }
    }

    private void updateBorder(JToggleButton button) {
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(button.isSelected() ? SELECTED_COLOR : BORDER_COLOR,
                                           button.isSelected() ? 4 : 1),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)));
    }

    }



    static class MatchConfig {
        private final String teamOne;
        private final String teamTwo;
        private final String stadium;
        private final boolean cpuMode;
        private final Partido.Modo matchMode;
        private final int timeLimitSeconds;
        private final int goalLimit;
    
        public MatchConfig(String teamOne, String teamTwo, String stadium,
                           boolean cpuMode, Partido.Modo matchMode,
                           int timeLimitSeconds, int goalLimit) {
            this.teamOne = teamOne;
            this.teamTwo = teamTwo;
            this.stadium = stadium;
            this.cpuMode = cpuMode;
            this.matchMode = matchMode;
            this.timeLimitSeconds = timeLimitSeconds;
            this.goalLimit = goalLimit;
        }
    
        public String getTeamOne() { return teamOne; }
        public String getTeamTwo() { return teamTwo; }
        public String getStadium() { return stadium; }
        public boolean isCpuMode() { return cpuMode; }
        public Partido.Modo getMatchMode() { return matchMode; }
        public int getTimeLimitSeconds() { return timeLimitSeconds; }
        public int getGoalLimit() { return goalLimit; }
    
        public int getSelectedLimit() {
            return matchMode == Partido.Modo.TIEMPO ? timeLimitSeconds : goalLimit;
        }
    }

    
}
