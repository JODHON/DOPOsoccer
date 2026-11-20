package Presentation;

import Domain.Balon;
import Domain.Cancha;
import Domain.DOPOsoccer;
import Domain.Jugador;
import Domain.Partido;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/** Loads match assets and draws the visual elements of the match. */
public class GameRenderer {
    private static final String[] PLAYER_STATES = {
        "idle", "forward", "jump", "fall", "kick", "slide", "action"
    };

    private final HudRenderer hudRenderer = new HudRenderer();
    private final Image background;
    private final Image ball;
    private final Image aura;
    private final Image goal;
    private final Image blockedIcon;
    private final Map<String, Image> firstTeamSprites;
    private final Map<String, Image> secondTeamSprites;

    public GameRenderer(String stadium, String teamOne, String teamTwo) {
        background = loadImage("/fondos/" + stadium + ".png");
        ball = loadImage("/Elementos/balon.png");
        aura = loadImage("/Elementos/aura.png");
        goal = loadImage("/Elementos/arco_derecho.png");
        blockedIcon = loadImage("/Elementos/icono_bloqueado.png");
        firstTeamSprites = loadSprites(teamOne);
        secondTeamSprites = loadSprites(teamTwo);
    }

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

    public void draw(Graphics2D graphics, JPanel panel, DOPOsoccer game, boolean cpuMode) {
        drawBackground(graphics, panel, background);
        drawBalls(graphics, panel, game, ball);
        drawPlayers(graphics, panel, game);
        drawGoals(graphics, panel, game, goal);
        hudRenderer.draw(graphics, panel, game, cpuMode, blockedIcon);
        drawPauseShade(graphics, panel, game);
    }

    private void drawBackground(Graphics2D graphics, JPanel panel, Image background) {
        if (background != null) {
            graphics.drawImage(background, 0, 0, panel.getWidth(), panel.getHeight(), panel);
        } else {
            graphics.setColor(new Color(135, 206, 235));
            graphics.fillRect(0, 0, panel.getWidth(), 400);
            graphics.setColor(new Color(34, 139, 34));
            graphics.fillRect(0, 400, panel.getWidth(), panel.getHeight() - 400);
        }
    }

    private void drawBalls(Graphics2D graphics, JPanel panel, DOPOsoccer game, Image ballImage) {
        if (ballImage != null) {
            for (Balon fakeBall : game.getBalonesFalsos()) {
                drawBall(graphics, panel, fakeBall, ballImage);
            }
            drawBall(graphics, panel, game.getBalon(), ballImage);
        } else {
            Balon ball = game.getBalon();
            graphics.setColor(Color.WHITE);
            graphics.fillOval((int) ball.getX(), (int) ball.getY(), ball.getSize(), ball.getSize());
            graphics.setColor(Color.BLACK);
            graphics.drawOval((int) ball.getX(), (int) ball.getY(), ball.getSize(), ball.getSize());
        }
    }

    private void drawBall(Graphics2D graphics, JPanel panel, Balon ball, Image image) {
        graphics.drawImage(image, (int) ball.getX(), (int) ball.getY(),
            ball.getSize(), ball.getSize(), panel);
    }

    private void drawPlayers(Graphics2D graphics, JPanel panel, DOPOsoccer game) {
        drawPlayer(graphics, panel, game.getJugador(), firstTeamSprites, aura, false, Color.BLUE);
        drawPlayer(graphics, panel, game.getJugador2(), secondTeamSprites, aura, true, Color.RED);
    }

    private void drawPlayer(Graphics2D graphics, JPanel panel, Jugador player,
                            Map<String, Image> sprites, Image aura,
                            boolean flipped, Color fallbackColor) {
        if (player.tienePoder() && aura != null) drawAura(graphics, panel, player, aura);

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
                0, 0, sprite.getWidth(panel), sprite.getHeight(panel), panel);
        } else {
            graphics.drawImage(sprite, x, y, player.getAncho(), player.getAlto(), panel);
        }
    }

    private void drawAura(Graphics2D graphics, JPanel panel, Jugador player, Image aura) {
        int frameCount = 4;
        int frameWidth = aura.getWidth(panel) / frameCount;
        int frameHeight = aura.getHeight(panel);
        int frame = (int) ((System.currentTimeMillis() / 100) % frameCount);
        int width = (int) (player.getAncho() * 1.4);
        int height = (int) (player.getAlto() * 1.2);
        int x = (int) player.getX() + (player.getAncho() - width) / 2;
        int y = (int) player.getY() + player.getAlto() - height;
        graphics.drawImage(aura, x, y, x + width, y + height,
            frame * frameWidth, 0, (frame + 1) * frameWidth, frameHeight, panel);
    }

    private void drawGoals(Graphics2D graphics, JPanel panel, DOPOsoccer game, Image goalImage) {
        drawGoal(graphics, panel, game.getCanchader(), goalImage, Color.BLUE, false);
        drawGoal(graphics, panel, game.getCanchaizq(), goalImage, Color.RED, true);
    }

    private void drawGoal(Graphics2D graphics, JPanel panel, Cancha goal,
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
                0, 0, image.getWidth(panel), image.getHeight(panel), panel);
        } else {
            graphics.drawImage(image, x, y, width, height, panel);
        }
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
