package Presentation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import Domain.Balon;
import Domain.Cancha;
import Domain.Jugador;
import Domain.DOPOsoccer;
import Domain.Partido;

/** Loads match assets and draws the field, players and HUD. */
public class GameRenderer {
public static class GameAssets {
        private static final String[] PLAYER_STATES = {
            "idle", "forward", "jump", "fall", "kick", "slide", "action"
        };
    
        private final Image background;
        private final Image ball;
        private final Image aura;
        private final Image goal;

        
        private final Map<String, Image> firstTeamSprites;
        private final Map<String, Image> secondTeamSprites;
    
        public GameAssets(String stadium, String teamOne, String teamTwo) {
            background = loadImage("/fondos/" + stadium + ".png");
            ball = loadImage("/Elementos/balon.png");
            aura = loadImage("/Elementos/aura.png");
            goal = loadImage("/Elementos/arco_derecho.png");

            
            firstTeamSprites = loadSprites(teamOne);
            secondTeamSprites = loadSprites(teamTwo);
        }
    
        public Image getBackground() { return background; }
        public Image getBall() { return ball; }
        public Image getAura() { return aura; }
        public Image getGoal() { return goal; }
        public Map<String, Image> getFirstTeamSprites() { return firstTeamSprites; }
        public Map<String, Image> getSecondTeamSprites() { return secondTeamSprites; }
    
        public Image loadImage(String path) {
            try {
                return ImageIO.read(getClass().getResource(path));
            } catch (Exception exception) {
                System.out.println("Could not load image: " + path);
                return null;
            }
        }
    
        private Map<String, Image> loadSprites(String team) {
            Map<String, Image> sprites = new HashMap<>();
            for (String state : PLAYER_STATES) {
                sprites.put(state, loadImage("/personajes/" + team + "_" + state + ".png"));
            }
            return sprites;
        }
    }

        private final FieldRenderer fieldRenderer = new FieldRenderer();
        private final PlayerRenderer playerRenderer = new PlayerRenderer();
        private final HudRenderer hudRenderer = new HudRenderer();
        private final GoalRenderer GoalRenderer = new GoalRenderer();
    
        public void draw(Graphics2D graphics, JPanel panel, DOPOsoccer game,
                         GameAssets assets, boolean cpuMode) {
            fieldRenderer.draw(graphics, panel, game, assets);
            playerRenderer.draw(graphics, panel, game.getJugador(),
                assets.getFirstTeamSprites(), assets.getAura(), false, Color.BLUE);
            playerRenderer.draw(graphics, panel, game.getJugador2(),
                assets.getSecondTeamSprites(), assets.getAura(), true, Color.RED);
            GoalRenderer.draw(graphics, panel, game, assets.getGoal());
            hudRenderer.draw(graphics, panel, game, cpuMode, assets);
            drawPauseShade(graphics, panel, game);
        }
    
        private void drawPauseShade(Graphics2D graphics, JPanel panel, DOPOsoccer game) {
            if (game.isPausado()) {
                graphics.setColor(new Color(0, 0, 0, 160));
                graphics.fillRect(0, 0, panel.getWidth(), panel.getHeight());
            } else {
                graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                graphics.setColor(new Color(255, 255, 255, 170));
                graphics.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
                java.awt.FontMetrics metrics = graphics.getFontMetrics();
                String hint = "P / ESC: pausa";
                graphics.drawString(hint,
                    panel.getWidth() / 2 - metrics.stringWidth(hint) / 2,
                    panel.getHeight() - 8);
            }
        }
    
}
