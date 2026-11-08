package Presentation;

import Domain.Balon;
import Domain.Cancha;
import Domain.Jugador;
import Domain.Partido;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.Timer;

public class DOPOsoccerGUI extends JFrame {

    // tiempo de juego

    private Timer timerJuego;
    private static final String[] ESTADOS = {"idle", "forward", "jump", "fall", "kick", "slide", "action"};

    // Nombres de archivo sin extensión. Edita estas listas con los tuyos.
    private static final String[] PAISES = {"alemania", "corea", "camerun", "nigeria", "usa"};
    private static final String[] ESTADIOS = {"alemania", "corea", "camerun", "nigeria", "usa", "bogota","bogota_es_roja"};

    // Prefijo de las miniaturas (dentro de src)
    private static final String CARPETA_BANDERAS = "/banderas/bandera_";
    private static final String CARPETA_MINIATURAS = "/miniaturas/miniatura_";

    private static final Color FONDO_MENU = new Color(20, 22, 38);
    private static final Color SELECCION = new Color(255, 200, 40);

    private CardLayout cartas = new CardLayout();
    private JPanel contenedor = new JPanel(cartas);

    private String pais1 = PAISES[0];
    private String pais2 = PAISES[PAISES.length > 1 ? 1 : 0];
    private String estadio = ESTADIOS[0];

    private Partido partido;
    private PanelJuego panel;

    private Map<String, Image> sprites1;
    private Map<String, Image> sprites2;
    private Image imgBalon;
    private Image imgFondo;
    private Image imgCancha;

    public DOPOsoccerGUI() {
        super("DOPOSoccer");

        contenedor.add(crearMenu(), "menu");
        add(contenedor);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ---------------- MENÚ ----------------

    private JPanel crearMenu() {
        PanelDesplazable contenido = new PanelDesplazable();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBackground(FONDO_MENU);
        contenido.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel titulo = new JLabel("DOPOSOCCER");
        titulo.setFont(new Font("Arial", Font.BOLD, 54));
        titulo.setForeground(Color.WHITE);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenido.add(titulo);
        contenido.add(Box.createVerticalStrut(20));

        contenido.add(crearSeccion("JUGADOR 1", PAISES, CARPETA_BANDERAS, 90, 60, 5, pais1, v -> pais1 = v));
        contenido.add(Box.createVerticalStrut(10));
        contenido.add(crearSeccion("JUGADOR 2", PAISES, CARPETA_BANDERAS, 90, 60, 5, pais2, v -> pais2 = v));
        contenido.add(Box.createVerticalStrut(10));
        contenido.add(crearSeccion("ESTADIO", ESTADIOS, CARPETA_MINIATURAS, 220, 124, 4, estadio, v -> estadio = v));

        JScrollPane scroll = new JScrollPane(contenido);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(20);

        JButton jugar = new JButton("JUGAR");
        jugar.setFont(new Font("Arial", Font.BOLD, 30));
        jugar.setFocusPainted(false);
        jugar.addActionListener(e -> iniciarPartido());

        JPanel barraInferior = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        barraInferior.setBackground(FONDO_MENU);
        barraInferior.add(jugar);

        JPanel menu = new JPanel(new BorderLayout());
        menu.setPreferredSize(new Dimension(1280, 720));
        menu.add(scroll, BorderLayout.CENTER);
        menu.add(barraInferior, BorderLayout.SOUTH);
        return menu;
    }

    private JPanel crearSeccion(String titulo, String[] opciones, String carpeta, int w, int h,
                                int columnas, String inicial, Consumer<String> alElegir) {
        JPanel seccion = new JPanel();
        seccion.setLayout(new BoxLayout(seccion, BoxLayout.Y_AXIS));
        seccion.setOpaque(false);

        JLabel lbl = new JLabel(titulo);
        lbl.setFont(new Font("Arial", Font.BOLD, 20));
        lbl.setForeground(new Color(180, 200, 255));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        seccion.add(lbl);

        JPanel fila = new JPanel(new GridLayout(0, columnas, 12, 12));
        fila.setOpaque(false);

        List<JToggleButton> botones = new ArrayList<>();
        for (String nombre : opciones) {
            JToggleButton btn = new JToggleButton(nombre.replace('_', ' ').toUpperCase());
            btn.setFont(new Font("Arial", Font.BOLD, 11));
            btn.setForeground(Color.WHITE);
            btn.setBackground(new Color(40, 44, 70));
            btn.setFocusPainted(false);
            btn.setVerticalTextPosition(JToggleButton.BOTTOM);
            btn.setHorizontalTextPosition(JToggleButton.CENTER);

            Image img = cargarImagen(carpeta + nombre + ".png");
            if (img != null) btn.setIcon(new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH)));

            btn.setSelected(nombre.equals(inicial));
            btn.addActionListener(e -> {
                alElegir.accept(nombre);
                for (JToggleButton otro : botones) {
                    otro.setSelected(otro == btn);
                    marcar(otro);
                }
            });
            marcar(btn);
            botones.add(btn);
            fila.add(btn);
        }

        // La envoltura centra la cuadrícula
        JPanel envoltura = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        envoltura.setOpaque(false);
        envoltura.add(fila);
        seccion.add(envoltura);
        return seccion;
    }

    private void marcar(JToggleButton btn) {
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(btn.isSelected() ? SELECCION : new Color(70, 75, 110),
                        btn.isSelected() ? 4 : 1),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));
    }

    // ---------------- PARTIDO ----------------

    private void iniciarPartido() {
    if (timerJuego != null) timerJuego.stop();
    if (panel != null) contenedor.remove(panel);

    partido = new Partido(pais1, pais2);

    imgFondo=cargarImagen("/fondos/" + estadio + ".png");
    sprites1=cargarSprites(pais1);
    sprites2=cargarSprites(pais2);
    imgBalon=cargarImagen("/Elementos/balon.png");
    imgCancha=cargarImagen("/Elementos/arco_derecho.png");

    panel=new PanelJuego();
    contenedor.add(panel,"juego");
    cartas.show(contenedor,"juego");
    panel.requestFocusInWindow();

    timerJuego = new Timer(16, e -> {
    try {
        partido.actualizarLogica();
        panel.repaint();
        if (partido.getEstado() == Partido.Estado.TERMINADO) {
            timerJuego.stop();
        }
    } catch (Exception ex) {
        ex.printStackTrace();
        timerJuego.stop();
    }
});
timerJuego.start();
}

    private Image cargarImagen(String ruta) {
        try {
            return ImageIO.read(getClass().getResource(ruta));
        } catch (Exception e) {
            System.out.println("No se pudo cargar: " + ruta);
            return null;
        }
    }

    // Carga /personajes/<pais>_<estado>.png para cada estado
    private Map<String, Image> cargarSprites(String pais) {
        Map<String, Image> mapa = new HashMap<>();
        for (String estado : ESTADOS) {
            mapa.put(estado, cargarImagen("/personajes/" + pais + "_" + estado + ".png"));
        }
        return mapa;
    }

    // Contenido del menú: se ajusta al ancho de la ventana y solo crece hacia abajo
    private static class PanelDesplazable extends JPanel implements Scrollable {
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int orientacion, int direccion) { return 20; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int orientacion, int direccion) { return r.height; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    private class PanelJuego extends JPanel {
        PanelJuego() {
            setPreferredSize(new Dimension(1280, 720));
            setFocusable(true);

            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    partido.teclaPresionada(e.getKeyCode());
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    partido.teclaLiberada(e.getKeyCode());
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            if (imgFondo != null) {
                g2.drawImage(imgFondo, 0, 0, getWidth(), getHeight(), this);
            } else {
                g2.setColor(new Color(135, 206, 235));
                g2.fillRect(0, 0, getWidth(), 400);
                g2.setColor(new Color(34, 139, 34));
                g2.fillRect(0, 400, getWidth(), getHeight() - 400);
            }

            dibujarJugador(g2, partido.getJugador(), sprites1, false, Color.BLUE);
            dibujarJugador(g2, partido.getJugador2(), sprites2, true, Color.RED);

            Balon b = partido.getBalon();
            if (imgBalon != null) {
                g2.drawImage(imgBalon, (int) b.getX(), (int) b.getY(), b.getSize(), b.getSize(), this);
            } else {
                g2.setColor(Color.WHITE);
                g2.fillOval((int) b.getX(), (int) b.getY(), b.getSize(), b.getSize());
                g2.setColor(Color.BLACK);
                g2.drawOval((int) b.getX(), (int) b.getY(), b.getSize(), b.getSize());
            }

            // Cancha derecha
            Cancha c = partido.getCanchader();
            if (imgCancha != null) {
                g2.drawImage(imgCancha, (int) c.getX(), (int) c.getY(), (int) c.getAncho(), (int) c.getAltura(), this);
            } else {
                g2.setColor(Color.BLUE);
                g2.fillRect((int) c.getX(), (int) c.getY(), (int) c.getAncho(), (int) c.getAltura());
            }

            // Cancha izquierda: misma imagen, volteada
            Cancha ci = partido.getCanchaizq();
            if (imgCancha != null) {
                int x1 = (int) ci.getX(), y1 = (int) ci.getY();
                int x2 = x1 + (int) ci.getAncho(), y2 = y1 + (int) ci.getAltura();
                g2.drawImage(imgCancha, x2, y1, x1, y2,
                        0, 0, imgCancha.getWidth(this), imgCancha.getHeight(this), this);
            } else {
                g2.setColor(Color.RED);
                g2.fillRect((int) ci.getX(), (int) ci.getY(), (int) ci.getAncho(), (int) ci.getAltura());
            }

            dibujarMarcador(g2);
        }

        private void dibujarJugador(Graphics2D g2, Jugador j, Map<String, Image> sprites,
                                    boolean voltear, Color colorRespaldo) {
            Image img = sprites.get(j.getEstado());
            if (img == null) img = sprites.get("idle");

            int x1 = (int) j.getX(), y1 = (int) j.getY();
            int x2 = x1 + j.getAncho(), y2 = y1 + j.getAlto();

            if (img == null) {
                g2.setColor(colorRespaldo);
                g2.fillRect(x1, y1, j.getAncho(), j.getAlto());
            } else if (voltear) {
                g2.drawImage(img, x2, y1, x1, y2, 0, 0, img.getWidth(this), img.getHeight(this), this);
            } else {
                g2.drawImage(img, x1, y1, j.getAncho(), j.getAlto(), this);
            }
        }

        private void dibujarMarcador(Graphics2D g2) {
            int[] goles = partido.getGoles();
            int w = 320, h = 76;
            int x = (getWidth() - w) / 2, y = 15;

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Sombra
            g2.setColor(new Color(0, 0, 0, 90));
            g2.fillRoundRect(x + 4, y + 5, w, h, 30, 30);

            // Fondo degradado
            g2.setPaint(new GradientPaint(0, y, new Color(35, 35, 55, 235), 0, y + h, new Color(10, 10, 22, 235)));
            g2.fillRoundRect(x, y, w, h, 30, 30);

            // Franjas de color de cada lado
            Shape clipOriginal = g2.getClip();
            g2.setClip(new RoundRectangle2D.Float(x, y, w, h, 30, 30));
            g2.setColor(new Color(33, 150, 243));
            g2.fillRect(x, y, 10, h);
            g2.setColor(new Color(244, 67, 54));
            g2.fillRect(x + w - 10, y, 10, h);
            g2.setClip(clipOriginal);

            // Borde
            g2.setColor(new Color(255, 255, 255, 120));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(x, y, w, h, 30, 30);
            g2.setStroke(new BasicStroke(1f));

            // Etiquetas
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            g2.setColor(new Color(180, 200, 255));
            centrarTexto(g2, "JUGADOR 1", x + w / 4, y + 22);
            g2.setColor(new Color(255, 190, 185));
            centrarTexto(g2, "JUGADOR 2", x + 3 * w / 4, y + 22);

            // Goles
            g2.setFont(new Font("Arial", Font.BOLD, 42));
            g2.setColor(Color.WHITE);
            centrarTexto(g2, String.valueOf(goles[0]), x + w / 4, y + 64);
            centrarTexto(g2, "-", x + w / 2, y + 60);
            centrarTexto(g2, String.valueOf(goles[1]), x + 3 * w / 4, y + 64);
        }

        private void centrarTexto(Graphics2D g2, String texto, int cx, int baseY) {
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(texto, cx - fm.stringWidth(texto) / 2, baseY);
        }
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(DOPOsoccerGUI::new);
    }
}