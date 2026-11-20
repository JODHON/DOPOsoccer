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
    
        public void draw(Graphics2D graphics, JPanel panel, DOPOsoccer game,
                         GameAssets assets, boolean cpuMode) {
            fieldRenderer.draw(graphics, panel, game, assets);
            playerRenderer.draw(graphics, panel, game.getJugador(),
                assets.getFirstTeamSprites(), assets.getAura(), false, Color.BLUE);
            playerRenderer.draw(graphics, panel, game.getJugador2(),
                assets.getSecondTeamSprites(), assets.getAura(), true, Color.RED);
            hudRenderer.draw(graphics, panel, game, cpuMode);
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
        // Drawing helpers are kept together because they form one rendering responsibility.
        private static class FieldRenderer {
        public void draw(Graphics2D graphics, JPanel observer, DOPOsoccer game, GameAssets assets) {
            drawBackground(graphics, observer, assets.getBackground());
            drawGoals(graphics, observer, game, assets.getGoal());
            drawBalls(graphics, observer, game, assets.getBall());
        }
    
        private void drawBackground(Graphics2D graphics, JPanel observer, Image background) {
            if (background != null) {
                graphics.drawImage(background, 0, 0, observer.getWidth(), observer.getHeight(), observer);
            } else {
                graphics.setColor(new Color(135, 206, 235));
                graphics.fillRect(0, 0, observer.getWidth(), 400);
                graphics.setColor(new Color(34, 139, 34));
                graphics.fillRect(0, 400, observer.getWidth(), observer.getHeight() - 400);
            }
        }
    
        private void drawBalls(Graphics2D graphics, JPanel observer, DOPOsoccer game, Image ballImage) {
            if (ballImage != null) {
                for (Balon fakeBall : game.getBalonesFalsos()) {
                    drawBall(graphics, observer, fakeBall, ballImage);
                }
            }
    
            Balon ball = game.getBalon();
            if (ballImage != null) {
                drawBall(graphics, observer, ball, ballImage);
            } else {
                graphics.setColor(Color.WHITE);
                graphics.fillOval((int) ball.getX(), (int) ball.getY(), ball.getSize(), ball.getSize());
                graphics.setColor(Color.BLACK);
                graphics.drawOval((int) ball.getX(), (int) ball.getY(), ball.getSize(), ball.getSize());
            }
        }
    
        private void drawBall(Graphics2D graphics, JPanel observer, Balon ball, Image image) {
            graphics.drawImage(image, (int) ball.getX(), (int) ball.getY(),
                ball.getSize(), ball.getSize(), observer);
        }
    
        private void drawGoals(Graphics2D graphics, JPanel observer, DOPOsoccer game, Image goalImage) {
            drawGoal(graphics, observer, game.getCanchader(), goalImage, Color.BLUE, false);
            drawGoal(graphics, observer, game.getCanchaizq(), goalImage, Color.RED, true);
        }
    
        private void drawGoal(Graphics2D graphics, JPanel observer, Cancha goal,
                              Image image, Color fallback, boolean flipped) {
            int x = (int) goal.getX();
            int y = (int) goal.getY();
            int width = (int) goal.getAncho();
            int height = (int) goal.getAltura();
            if (image == null) {
                graphics.setColor(fallback);
                graphics.fillRect(x, y, width, height);
            } else if (flipped) {
                graphics.drawImage(image, x + width, y, x, y + height,
                    0, 0, image.getWidth(observer), image.getHeight(observer), observer);
            } else {
                graphics.drawImage(image, x, y, width, height, observer);
            }
        }
    
        }
        private static class PlayerRenderer {
        public void draw(Graphics2D graphics, JPanel observer, Jugador player,
                         Map<String, Image> sprites, Image aura,
                         boolean flipped, Color fallbackColor) {
            if (player.tienePoder() && aura != null) {
                drawAura(graphics, observer, player, aura);
            }
    
            Image sprite = sprites.get(player.getEstado());
            if (sprite == null) sprite = sprites.get("idle");
    
            int x = (int) player.getX();
            int y = (int) player.getY();
            int right = x + player.getAncho();
            int bottom = y + player.getAlto();
    
            if (sprite == null) {
                graphics.setColor(fallbackColor);
                graphics.fillRect(x, y, player.getAncho(), player.getAlto());
            } else if (flipped) {
                graphics.drawImage(sprite, right, y, x, bottom,
                    0, 0, sprite.getWidth(observer), sprite.getHeight(observer), observer);
            } else {
                graphics.drawImage(sprite, x, y, player.getAncho(), player.getAlto(), observer);
            }
        }
    
        private void drawAura(Graphics2D graphics, JPanel observer, Jugador player, Image aura) {
            int frameCount = 4;
            int frameWidth = aura.getWidth(observer) / frameCount;
            int frameHeight = aura.getHeight(observer);
            int frame = (int) ((System.currentTimeMillis() / 100) % frameCount);
            int width = (int) (player.getAncho() * 1.4);
            int height = (int) (player.getAlto() * 1.2);
            int x = (int) player.getX() + (player.getAncho() - width) / 2;
            int y = (int) player.getY() + player.getAlto() - height;
    
            graphics.drawImage(aura, x, y, x + width, y + height,
                frame * frameWidth, 0, (frame + 1) * frameWidth, frameHeight, observer);
        }
    
        }
        private static class HudRenderer {
        private static final Color SELECTED_COLOR = new Color(255, 200, 40);
    
        public void draw(Graphics2D graphics, JPanel panel, DOPOsoccer game, boolean cpuMode) {
            drawScoreboard(graphics, panel, game, cpuMode);
            drawMatchClock(graphics, panel, game);
            drawPowerBar(graphics, panel, game.getJugador(), 30, 30, new Color(33, 150, 243));
            drawPowerBar(graphics, panel, game.getJugador2(), panel.getWidth() - 270, 30,
                new Color(244, 67, 54));
        }
    
        private void drawScoreboard(Graphics2D graphics, JPanel panel, DOPOsoccer game, boolean cpuMode) {
            int[] goals = game.getGoles();
            int width = 320;
            int height = 76;
            int x = (panel.getWidth() - width) / 2;
            int y = 15;
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    
            graphics.setColor(new Color(0, 0, 0, 90));
            graphics.fillRoundRect(x + 4, y + 5, width, height, 30, 30);
            graphics.setPaint(new GradientPaint(0, y, new Color(35, 35, 55, 235),
                0, y + height, new Color(10, 10, 22, 235)));
            graphics.fillRoundRect(x, y, width, height, 30, 30);
    
            Shape originalClip = graphics.getClip();
            graphics.setClip(new RoundRectangle2D.Float(x, y, width, height, 30, 30));
            graphics.setColor(new Color(33, 150, 243));
            graphics.fillRect(x, y, 10, height);
            graphics.setColor(new Color(244, 67, 54));
            graphics.fillRect(x + width - 10, y, 10, height);
            graphics.setClip(originalClip);
    
            graphics.setColor(new Color(255, 255, 255, 120));
            graphics.setStroke(new BasicStroke(2f));
            graphics.drawRoundRect(x, y, width, height, 30, 30);
            graphics.setStroke(new BasicStroke(1f));
    
            graphics.setFont(new Font("Arial", Font.BOLD, 14));
            graphics.setColor(new Color(180, 200, 255));
            drawCenteredText(graphics, "JUGADOR 1", x + width / 4, y + 22);
            graphics.setColor(new Color(255, 190, 185));
            drawCenteredText(graphics, cpuMode ? "CPU" : "JUGADOR 2", x + 3 * width / 4, y + 22);
    
            graphics.setFont(new Font("Arial", Font.BOLD, 42));
            graphics.setColor(Color.WHITE);
            drawCenteredText(graphics, String.valueOf(goals[0]), x + width / 4, y + 64);
            drawCenteredText(graphics, "-", x + width / 2, y + 60);
            drawCenteredText(graphics, String.valueOf(goals[1]), x + 3 * width / 4, y + 64);
        }
    
        private void drawMatchClock(Graphics2D graphics, JPanel panel, DOPOsoccer game) {
            boolean suddenDeath = game.getEstado() == Partido.Estado.MUERTE_SUBITA;
            int seconds = (int) Math.ceil(game.getTiempoRestante());
            boolean goalMode = game.getModo() == Partido.Modo.GOLES;
            String text;
            if (goalMode) text = "PRIMERO A " + game.getGolesObjetivo() + " GOLES";
            else if (suddenDeath) text = "MUERTE SÚBITA";
            else text = String.format("%d:%02d", seconds / 60, seconds % 60);
    
            int width = goalMode ? 260 : (suddenDeath ? 220 : 120);
            int height = 36;
            int x = (panel.getWidth() - width) / 2;
            int y = 98;
            graphics.setColor(new Color(10, 10, 22, 220));
            graphics.fillRoundRect(x, y, width, height, 20, 20);
            graphics.setColor(new Color(255, 255, 255, 120));
            graphics.setStroke(new BasicStroke(2f));
            graphics.drawRoundRect(x, y, width, height, 20, 20);
            graphics.setStroke(new BasicStroke(1f));
    
            boolean urgent = !goalMode && (suddenDeath
                || (game.getEstado() == Partido.Estado.JUGANDO && seconds <= 10));
            graphics.setFont(new Font("Arial", Font.BOLD, (goalMode || suddenDeath) ? 20 : 24));
            graphics.setColor(urgent ? new Color(255, 90, 80) : Color.WHITE);
            drawCenteredText(graphics, text, x + width / 2, y + 26);
        }
    
        private void drawPowerBar(Graphics2D graphics, JPanel panel, Jugador player,
                                  int x, int y, Color fillColor) {
            int width = 240;
            int height = 24;
            double charge = Math.max(0, Math.min(100, player.getCargaPoder()));
            boolean ready = charge >= 100;
    
            graphics.setColor(new Color(10, 10, 22, 220));
            graphics.fillRoundRect(x, y, width, height, 14, 14);
            graphics.setColor(ready ? SELECTED_COLOR : fillColor);
            graphics.fillRoundRect(x, y, (int) (width * charge / 100.0), height, 14, 14);
            graphics.setColor(new Color(255, 255, 255, 150));
            graphics.setStroke(new BasicStroke(2f));
            graphics.drawRoundRect(x, y, width, height, 14, 14);
            graphics.setStroke(new BasicStroke(1f));
    
            String text;
            if (player.tienePoder()) text = "PODER ACTIVO";
            else if (ready) text = "¡PODER LISTO!";
            else text = "PODER " + (int) charge + "%";
            graphics.setFont(new Font("Arial", Font.BOLD, 14));
            graphics.setColor(ready && !player.tienePoder() ? Color.BLACK : Color.WHITE);
            drawCenteredText(graphics, text, x + width / 2, y + 17);
        }
    
        private void drawCenteredText(Graphics2D graphics, String text, int centerX, int baselineY) {
            FontMetrics metrics = graphics.getFontMetrics();
            graphics.drawString(text, centerX - metrics.stringWidth(text) / 2, baselineY);
        }
    
        }
    
    
}
