package Presentation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Pause overlay shown on top of the match. */
public class PausePanel extends JPanel {
    public PausePanel(Runnable onResume, Runnable onReturnToMenu) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        JLabel title = new JLabel("PAUSA");
        title.setFont(new Font("Arial", Font.BOLD, 64));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(CENTER_ALIGNMENT);
        add(title);
        add(Box.createVerticalStrut(8));

        JLabel help = new JLabel("ESC o P para continuar");
        help.setFont(new Font("Arial", Font.PLAIN, 16));
        help.setForeground(new Color(180, 200, 255));
        help.setAlignmentX(CENTER_ALIGNMENT);
        add(help);
        add(Box.createVerticalStrut(30));

        JButton resumeButton = createButton("REANUDAR");
        resumeButton.addActionListener(event -> onResume.run());
        add(resumeButton);
        add(Box.createVerticalStrut(14));

        JButton menuButton = createButton("VOLVER AL MENÚ");
        menuButton.addActionListener(event -> onReturnToMenu.run());
        add(menuButton);
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 26));
        button.setFocusable(false);
        button.setAlignmentX(CENTER_ALIGNMENT);
        Dimension size = new Dimension(320, 52);
        button.setPreferredSize(size);
        button.setMaximumSize(size);
        return button;
    }

    }
