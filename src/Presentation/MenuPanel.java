package Presentation;

import Domain.Partido;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/** Main menu. Stores the selected teams, stadium and match settings. */
public class MenuPanel extends JPanel {
    private static final String[] TEAMS = {"alemania", "corea", "camerun", "nigeria", "usa"};
    private static final String[] STADIUMS = {
        "alemania", "corea", "camerun", "nigeria", "usa", "bogota", "bogota_es_roja"
    };
    private static final String FLAG_FOLDER = "/banderas/bandera_";
    private static final String THUMBNAIL_FOLDER = "/miniaturas/miniatura_";
    private static final Color MENU_BACKGROUND = new Color(20, 22, 38);
    private static final Color SELECTED_COLOR = new Color(255, 200, 40);
    private static final Color TITLE_COLOR = new Color(180, 200, 255);
    private static final Color BUTTON_COLOR = new Color(40, 44, 70);
    private static final Color BORDER_COLOR = new Color(70, 75, 110);

    private String teamOne = TEAMS[0];
    private String teamTwo = TEAMS[1];
    private String stadium = STADIUMS[0];
    private final JLabel secondTeamTitle = new JLabel("JUGADOR 2");
    private final MatchSettingsPanel settingsPanel;

    public MenuPanel(Runnable onStart) {
        setLayout(new BorderLayout());
        setBackground(MENU_BACKGROUND);
        setPreferredSize(new Dimension(1280, 720));

        JLabel title = new JLabel("DOPOSOCCER", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 46));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
        add(title, BorderLayout.NORTH);

        JPanel firstTeam = createSelector("JUGADOR 1", TEAMS, FLAG_FOLDER,
            60, 40, 1, teamOne, value -> teamOne = value);
        firstTeam.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 20));
        add(firstTeam, BorderLayout.WEST);

        JPanel secondTeam = createSelector(secondTeamTitle, TEAMS, FLAG_FOLDER,
            60, 40, 1, teamTwo, value -> teamTwo = value);
        secondTeam.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 40));
        add(secondTeam, BorderLayout.EAST);

        JPanel stadiumSelector = createSelector("ESTADIO", STADIUMS, THUMBNAIL_FOLDER,
            190, 107, 4, stadium, value -> stadium = value);
        JPanel centeredStadiums = new JPanel(new GridBagLayout());
        centeredStadiums.setOpaque(false);
        centeredStadiums.add(stadiumSelector);
        add(centeredStadiums, BorderLayout.CENTER);

        settingsPanel = new MatchSettingsPanel(
            cpuMode -> secondTeamTitle.setText(cpuMode ? "CPU" : "JUGADOR 2"));
        add(createBottomPanel(onStart), BorderLayout.SOUTH);
    }

    private JPanel createSelector(String title, String[] options, String imageFolder,
                                  int imageWidth, int imageHeight, int columns,
                                  String initialValue, java.util.function.Consumer<String> onSelection) {
        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Arial", Font.BOLD, 20));
        heading.setForeground(TITLE_COLOR);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        return createSelector(heading, options, imageFolder, imageWidth, imageHeight,
            columns, initialValue, onSelection);
    }

    private JPanel createSelector(JLabel heading, String[] options, String imageFolder,
                                  int imageWidth, int imageHeight, int columns,
                                  String initialValue, java.util.function.Consumer<String> onSelection) {
        JPanel selector = new JPanel();
        selector.setLayout(new BoxLayout(selector, BoxLayout.Y_AXIS));
        selector.setOpaque(false);
        heading.setFont(new Font("Arial", Font.BOLD, 20));
        heading.setForeground(TITLE_COLOR);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        selector.add(heading);
        selector.add(Box.createVerticalStrut(8));

        JPanel grid = new JPanel(new GridLayout(0, columns, 12, 8));
        grid.setOpaque(false);
        List<JToggleButton> buttons = new ArrayList<>();
        for (String option : options) {
            JToggleButton button = createOptionButton(option, imageFolder,
                imageWidth, imageHeight, initialValue, buttons, onSelection);
            buttons.add(button);
            grid.add(button);
        }

        JPanel centeredGrid = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centeredGrid.setOpaque(false);
        centeredGrid.add(grid);
        selector.add(centeredGrid);
        return selector;
    }

    private JToggleButton createOptionButton(String option, String folder, int width, int height,
                                             String initialValue, List<JToggleButton> buttons,
                                             java.util.function.Consumer<String> onSelection) {
        JToggleButton button = new JToggleButton(option.replace('_', ' ').toUpperCase());
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setForeground(Color.WHITE);
        button.setBackground(BUTTON_COLOR);
        button.setFocusPainted(false);
        button.setVerticalTextPosition(JToggleButton.BOTTOM);
        button.setHorizontalTextPosition(JToggleButton.CENTER);
        button.setSelected(option.equals(initialValue));

        try {
            Image image = ImageIO.read(getClass().getResource(folder + option + ".png"));
            if (image != null) {
                button.setIcon(new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_SMOOTH)));
            }
        } catch (Exception exception) {
            System.out.println("Could not load image: " + folder + option + ".png");
        }
        updateBorder(button);
        button.addActionListener(event -> {
            for (JToggleButton other : buttons) {
                other.setSelected(other == button);
                updateBorder(other);
            }
            button.setSelected(true);
            updateBorder(button);
            onSelection.accept(option);
        });
        return button;
    }

    private void updateBorder(JToggleButton button) {
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(button.isSelected() ? SELECTED_COLOR : BORDER_COLOR,
                button.isSelected() ? 4 : 1),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)));
    }

    private JPanel createBottomPanel(Runnable onStart) {
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setOpaque(false);
        bottom.add(settingsPanel);
        bottom.add(Box.createVerticalStrut(10));

        JButton startButton = new JButton("JUGAR");
        startButton.setFont(new Font("Arial", Font.BOLD, 30));
        startButton.setFocusPainted(false);
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.addActionListener(event -> onStart.run());
        bottom.add(startButton);
        bottom.add(Box.createVerticalStrut(15));
        return bottom;
    }

    public String getTeamOne() { return teamOne; }
    public String getTeamTwo() { return teamTwo; }
    public String getStadium() { return stadium; }
    public boolean isCpuMode() { return settingsPanel.isCpuMode(); }
    public Partido.Modo getMatchMode() { return settingsPanel.getMatchMode(); }
    public int getTimeLimitSeconds() { return settingsPanel.getTimeLimitSeconds(); }
    public int getGoalLimit() { return settingsPanel.getGoalLimit(); }
    public int getSelectedLimit() {
        return getMatchMode() == Partido.Modo.TIEMPO ? getTimeLimitSeconds() : getGoalLimit();
    }
}
