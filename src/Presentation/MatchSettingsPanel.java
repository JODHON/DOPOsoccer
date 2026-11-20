package Presentation;

import Domain.Partido;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;

/** Match mode, CPU mode and match-limit settings. */
public class MatchSettingsPanel extends JPanel {
        private static final int MIN_TIME_SECONDS = 30;
        private static final int MAX_TIME_SECONDS = 300;
        private static final int TIME_STEP_SECONDS = 10;
        private static final int MIN_GOALS = 1;
        private static final int MAX_GOALS = 15;
    
        private boolean cpuMode;
        private Partido.Modo matchMode = Partido.Modo.TIEMPO;
        private int timeLimitSeconds = Partido.DURACION_DEFECTO_S;
        private int goalLimit = Partido.GOLES_DEFECTO;
        private boolean updatingSlider;
    
        private final JSlider limitSlider;
        private final JLabel limitLabel;
        private final JToggleButton oneVsOneButton;
        private final JToggleButton cpuButton;
        private final JToggleButton timeButton;
        private final JToggleButton goalsButton;
    
        private final Consumer<Boolean> onCpuModeChanged;
    
        public MatchSettingsPanel(Consumer<Boolean> onCpuModeChanged) {
            this.onCpuModeChanged = onCpuModeChanged;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(5, 0, 15, 0));
    
            JPanel optionGroups = new JPanel();
            optionGroups.setLayout(new BoxLayout(optionGroups, BoxLayout.X_AXIS));
            optionGroups.setOpaque(false);
    
            oneVsOneButton = createModeButton("1 VS 1");
            cpuButton = createModeButton("1 VS CPU");
            oneVsOneButton.setSelected(true);
            updateBorder(oneVsOneButton);
            oneVsOneButton.addActionListener(event -> selectCpuMode(false));
            cpuButton.addActionListener(event -> selectCpuMode(true));
            JPanel gameModeGroup = createOptionGroup("MODO DE JUEGO", oneVsOneButton, cpuButton);
    
            timeButton = createModeButton("POR TIEMPO");
            goalsButton = createModeButton("POR GOLES");
            timeButton.setToolTipText("Gana quien tenga más goles al acabar el tiempo; si empatan, muerte súbita");
            goalsButton.setToolTipText("Gana el primero en llegar al número de goles elegido");
            timeButton.setSelected(true);
            updateBorder(timeButton);
            timeButton.addActionListener(event -> selectMatchMode(Partido.Modo.TIEMPO));
            goalsButton.addActionListener(event -> selectMatchMode(Partido.Modo.GOLES));
            JPanel matchModeGroup = createOptionGroup("TIPO DE PARTIDO", timeButton, goalsButton);
    
            limitSlider = new JSlider();
            limitSlider.setOpaque(false);
            limitSlider.setFocusable(false);
            limitSlider.setPreferredSize(new Dimension(250, 28));
            limitLabel = new JLabel();
            limitLabel.setFont(new Font("Arial", Font.BOLD, 16));
            limitLabel.setForeground(Color.WHITE);
            limitLabel.setPreferredSize(new Dimension(90, 24));
            limitSlider.addChangeListener(event -> updateSelectedLimit());
    
            JPanel sliderRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
            sliderRow.setOpaque(false);
            sliderRow.add(limitSlider);
            sliderRow.add(limitLabel);
            matchModeGroup.add(Box.createVerticalStrut(6));
            matchModeGroup.add(sliderRow);
    
            optionGroups.add(Box.createHorizontalGlue());
            optionGroups.add(gameModeGroup);
            optionGroups.add(Box.createHorizontalStrut(80));
            optionGroups.add(matchModeGroup);
            optionGroups.add(Box.createHorizontalGlue());
            add(optionGroups);
            configureSlider();
        }
    
    
        public boolean isCpuMode() { return cpuMode; }
        public Partido.Modo getMatchMode() { return matchMode; }
        public int getTimeLimitSeconds() { return timeLimitSeconds; }
        public int getGoalLimit() { return goalLimit; }
    
        private JPanel createOptionGroup(String title, JToggleButton first, JToggleButton second) {
            JPanel group = new JPanel();
            group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
            group.setOpaque(false);
            JLabel heading = new JLabel(title);
            heading.setFont(new Font("Arial", Font.BOLD, 20));
            heading.setForeground(new Color(180, 200, 255));
            heading.setAlignmentX(CENTER_ALIGNMENT);
            group.add(heading);
            group.add(Box.createVerticalStrut(8));
            JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
            row.setOpaque(false);
            row.add(first);
            row.add(second);
            group.add(row);
            group.setAlignmentY(TOP_ALIGNMENT);
            return group;
        }
    
        private JToggleButton createModeButton(String text) {
            JToggleButton button = new JToggleButton(text);
            button.setFont(new Font("Arial", Font.BOLD, 18));
            button.setForeground(Color.WHITE);
            button.setBackground(new Color(40, 44, 70));
            button.setFocusPainted(false);
            button.setPreferredSize(new Dimension(170, 44));
            updateBorder(button);
            return button;
        }
    
        private void selectCpuMode(boolean selected) {
            cpuMode = selected;
            oneVsOneButton.setSelected(!selected);
            cpuButton.setSelected(selected);
            updateBorder(oneVsOneButton);
            updateBorder(cpuButton);
            onCpuModeChanged.accept(selected);
        }
    
        private void selectMatchMode(Partido.Modo selected) {
            matchMode = selected;
            timeButton.setSelected(selected == Partido.Modo.TIEMPO);
            goalsButton.setSelected(selected == Partido.Modo.GOLES);
            updateBorder(timeButton);
            updateBorder(goalsButton);
            configureSlider();
        }
    
        private void configureSlider() {
            updatingSlider = true;
            if (matchMode == Partido.Modo.TIEMPO) {
                limitSlider.setMinimum(MIN_TIME_SECONDS / TIME_STEP_SECONDS);
                limitSlider.setMaximum(MAX_TIME_SECONDS / TIME_STEP_SECONDS);
                limitSlider.setValue(timeLimitSeconds / TIME_STEP_SECONDS);
            } else {
                limitSlider.setMinimum(MIN_GOALS);
                limitSlider.setMaximum(MAX_GOALS);
                limitSlider.setValue(goalLimit);
            }
            updatingSlider = false;
            updateLimitLabel();
        }
    
        private void updateSelectedLimit() {
            if (updatingSlider) return;
            if (matchMode == Partido.Modo.TIEMPO) {
                timeLimitSeconds = limitSlider.getValue() * TIME_STEP_SECONDS;
            } else {
                goalLimit = limitSlider.getValue();
            }
            updateLimitLabel();
        }
    
        private void updateLimitLabel() {
            if (matchMode == Partido.Modo.TIEMPO) {
                limitLabel.setText(String.format("%d:%02d min", timeLimitSeconds / 60, timeLimitSeconds % 60));
            } else {
                limitLabel.setText(goalLimit + (goalLimit == 1 ? " GOL" : " GOLES"));
            }
        }
    
        private void updateBorder(JToggleButton button) {
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(button.isSelected()
                    ? new Color(255, 200, 40) : new Color(70, 75, 110), button.isSelected() ? 4 : 1),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));
        }
    }
