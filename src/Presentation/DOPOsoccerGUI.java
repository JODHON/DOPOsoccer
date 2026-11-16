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
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
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
import javax.swing.JSlider;
import javax.swing.JToggleButton;
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
    private static final Color COLOR_TITULO_SECCION = new Color(180, 200, 255);

    private CardLayout cartas = new CardLayout();
    private JPanel contenedor = new JPanel(cartas);

    private String pais1 = PAISES[0];
    private String pais2 = PAISES[PAISES.length > 1 ? 1 : 0];
    private String estadio = ESTADIOS[0];

    // Modo de juego elegido en el menú: false = 1 vs 1, true = 1 vs CPU
    private boolean modoCPU = false;

    // Tipo de partido elegido en el menú: por tiempo o por goles
    private Partido.Modo modoPartido = Partido.Modo.TIEMPO;

    // Límite del partido: cada tipo recuerda su último valor elegido en el slider
    private static final int MIN_TIEMPO_S = 30, MAX_TIEMPO_S = 300, PASO_TIEMPO_S = 10;
    private static final int MIN_GOLES = 1, MAX_GOLES = 15;
    private int duracionSegundos = Partido.DURACION_DEFECTO_S;
    private int golesObjetivo = Partido.GOLES_DEFECTO;
    private JSlider sliderLimite;
    private JLabel lblLimite;
    private boolean ajustandoSlider = false;
    private JLabel lblJugador2;

    private Partido partido;
    private PanelJuego panel;
    private JPanel panelFin;

    private Map<String, Image> sprites1;
    private Map<String, Image> sprites2;
    private Image imgBalon;
    private Image imgAura;
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
        JPanel menu = new JPanel(new BorderLayout());
        menu.setBackground(FONDO_MENU);
        menu.setPreferredSize(new Dimension(1280, 720));

        // Título
        JLabel titulo = new JLabel("DOPOSOCCER", JLabel.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 46));
        titulo.setForeground(Color.WHITE);
        titulo.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
        menu.add(titulo, BorderLayout.NORTH);

        // Columna izquierda: jugador 1
        JPanel izquierda = crearSeccion(crearTituloSeccion("JUGADOR 1"), PAISES, CARPETA_BANDERAS,
                60, 40, 1, pais1, v -> pais1 = v);
        izquierda.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 20));
        menu.add(izquierda, BorderLayout.WEST);

        // Columna derecha: jugador 2 (o CPU)
        lblJugador2 = crearTituloSeccion("JUGADOR 2");
        JPanel derecha = crearSeccion(lblJugador2, PAISES, CARPETA_BANDERAS,
                60, 40, 1, pais2, v -> pais2 = v);
        derecha.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 40));
        menu.add(derecha, BorderLayout.EAST);

        // Centro: estadios (4 columnas; con 7 estadios quedan 2 filas)
        JPanel estadios = crearSeccion(crearTituloSeccion("ESTADIO"), ESTADIOS, CARPETA_MINIATURAS,
                190, 107, 4, estadio, v -> estadio = v);
        JPanel centro = new JPanel(new GridBagLayout()); // centra la sección en el espacio disponible
        centro.setOpaque(false);
        centro.add(estadios);
        menu.add(centro, BorderLayout.CENTER);

        // Abajo: modo de juego + JUGAR
        menu.add(crearBarraInferior(), BorderLayout.SOUTH);
        return menu;
    }

    private JLabel crearTituloSeccion(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Arial", Font.BOLD, 20));
        lbl.setForeground(COLOR_TITULO_SECCION);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }

    private JPanel crearBarraInferior() {
        JPanel barra = new JPanel();
        barra.setLayout(new BoxLayout(barra, BoxLayout.Y_AXIS));
        barra.setOpaque(false);
        barra.setBorder(BorderFactory.createEmptyBorder(5, 0, 15, 0));

        // Las dos opciones van lado a lado, alineadas arriba
        JPanel opciones = new JPanel();
        opciones.setLayout(new BoxLayout(opciones, BoxLayout.X_AXIS));
        opciones.setOpaque(false);

        // Modo de juego
        JToggleButton btn1v1 = crearBotonModo("1 VS 1");
        JToggleButton btnCpu = crearBotonModo("1 VS CPU");
        btn1v1.setSelected(!modoCPU);
        btnCpu.setSelected(modoCPU);
        marcar(btn1v1);
        marcar(btnCpu);
        btn1v1.addActionListener(e -> elegirModo(false, btn1v1, btnCpu));
        btnCpu.addActionListener(e -> elegirModo(true, btn1v1, btnCpu));
        JPanel grupoModo = crearGrupoOpciones("MODO DE JUEGO", btn1v1, btnCpu);

        // Tipo de partido + slider del límite
        JToggleButton btnTiempo = crearBotonModo("POR TIEMPO");
        JToggleButton btnGoles = crearBotonModo("POR GOLES");
        btnTiempo.setToolTipText("Gana quien tenga más goles al acabar el tiempo; si empatan, muerte súbita");
        btnGoles.setToolTipText("Gana el primero en llegar al número de goles elegido");
        btnTiempo.setSelected(modoPartido == Partido.Modo.TIEMPO);
        btnGoles.setSelected(modoPartido == Partido.Modo.GOLES);
        marcar(btnTiempo);
        marcar(btnGoles);
        btnTiempo.addActionListener(e -> elegirTipoPartido(Partido.Modo.TIEMPO, btnTiempo, btnGoles));
        btnGoles.addActionListener(e -> elegirTipoPartido(Partido.Modo.GOLES, btnTiempo, btnGoles));
        JPanel grupoTipo = crearGrupoOpciones("TIPO DE PARTIDO", btnTiempo, btnGoles);
        grupoTipo.add(Box.createVerticalStrut(6));
        grupoTipo.add(crearFilaSlider());

        grupoModo.setAlignmentY(Component.TOP_ALIGNMENT);
        grupoTipo.setAlignmentY(Component.TOP_ALIGNMENT);
        opciones.add(Box.createHorizontalGlue());
        opciones.add(grupoModo);
        opciones.add(Box.createHorizontalStrut(80));
        opciones.add(grupoTipo);
        opciones.add(Box.createHorizontalGlue());

        barra.add(opciones);
        barra.add(Box.createVerticalStrut(10));

        JButton jugar = new JButton("JUGAR");
        jugar.setFont(new Font("Arial", Font.BOLD, 30));
        jugar.setFocusPainted(false);
        jugar.setAlignmentX(Component.CENTER_ALIGNMENT);
        jugar.addActionListener(e -> iniciarPartido());
        barra.add(jugar);

        return barra;
    }

    // Título + dos botones de opción en una fila
    private JPanel crearGrupoOpciones(String titulo, JToggleButton a, JToggleButton b) {
        JPanel grupo = new JPanel();
        grupo.setLayout(new BoxLayout(grupo, BoxLayout.Y_AXIS));
        grupo.setOpaque(false);

        grupo.add(crearTituloSeccion(titulo));
        grupo.add(Box.createVerticalStrut(8));

        JPanel fila = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        fila.setOpaque(false);
        fila.add(a);
        fila.add(b);
        grupo.add(fila);
        return grupo;
    }

    // Slider que fija los segundos (por tiempo) o los goles (por goles)
    private JPanel crearFilaSlider() {
        sliderLimite = new JSlider();
        sliderLimite.setOpaque(false);
        sliderLimite.setFocusable(false);
        sliderLimite.setPreferredSize(new Dimension(250, 28));

        lblLimite = new JLabel();
        lblLimite.setFont(new Font("Arial", Font.BOLD, 16));
        lblLimite.setForeground(Color.WHITE);
        lblLimite.setPreferredSize(new Dimension(90, 24)); // ancho fijo para que no salte el diseño

        sliderLimite.addChangeListener(e -> {
            if (ajustandoSlider) return;
            if (modoPartido == Partido.Modo.TIEMPO) {
                duracionSegundos = sliderLimite.getValue() * PASO_TIEMPO_S;
            } else {
                golesObjetivo = sliderLimite.getValue();
            }
            actualizarEtiquetaLimite();
        });
        configurarSlider();

        JPanel fila = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        fila.setOpaque(false);
        fila.add(sliderLimite);
        fila.add(lblLimite);
        return fila;
    }

    // Ajusta rango y valor del slider según el tipo de partido
    private void configurarSlider() {
        ajustandoSlider = true;
        if (modoPartido == Partido.Modo.TIEMPO) {
            sliderLimite.setMinimum(MIN_TIEMPO_S / PASO_TIEMPO_S);
            sliderLimite.setMaximum(MAX_TIEMPO_S / PASO_TIEMPO_S);
            sliderLimite.setValue(duracionSegundos / PASO_TIEMPO_S);
        } else {
            sliderLimite.setMinimum(MIN_GOLES);
            sliderLimite.setMaximum(MAX_GOLES);
            sliderLimite.setValue(golesObjetivo);
        }
        ajustandoSlider = false;
        actualizarEtiquetaLimite();
    }

    private void actualizarEtiquetaLimite() {
        if (modoPartido == Partido.Modo.TIEMPO) {
            lblLimite.setText(String.format("%d:%02d min", duracionSegundos / 60, duracionSegundos % 60));
        } else {
            lblLimite.setText(golesObjetivo + (golesObjetivo == 1 ? " GOL" : " GOLES"));
        }
    }

    private JToggleButton crearBotonModo(String texto) {
        JToggleButton btn = new JToggleButton(texto);
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(40, 44, 70));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(170, 44));
        return btn;
    }

    private void elegirModo(boolean cpu, JToggleButton btn1v1, JToggleButton btnCpu) {
        modoCPU = cpu;
        btn1v1.setSelected(!cpu);
        btnCpu.setSelected(cpu);
        marcar(btn1v1);
        marcar(btnCpu);
        lblJugador2.setText(cpu ? "CPU" : "JUGADOR 2");
    }

    private void elegirTipoPartido(Partido.Modo modo, JToggleButton btnTiempo, JToggleButton btnGoles) {
        modoPartido = modo;
        btnTiempo.setSelected(modo == Partido.Modo.TIEMPO);
        btnGoles.setSelected(modo == Partido.Modo.GOLES);
        marcar(btnTiempo);
        marcar(btnGoles);
        configurarSlider();
    }

    private JPanel crearSeccion(JLabel lbl, String[] opciones, String carpeta, int w, int h,
                                int columnas, String inicial, Consumer<String> alElegir) {
        JPanel seccion = new JPanel();
        seccion.setLayout(new BoxLayout(seccion, BoxLayout.Y_AXIS));
        seccion.setOpaque(false);

        seccion.add(lbl);
        seccion.add(Box.createVerticalStrut(8));

        JPanel cuadricula = new JPanel(new GridLayout(0, columnas, 12, 8));
        cuadricula.setOpaque(false);

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
            cuadricula.add(btn);
        }

        // La envoltura centra la cuadrícula dentro de la sección
        JPanel envoltura = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        envoltura.setOpaque(false);
        envoltura.add(cuadricula);
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

        // TODO: cuando exista la lógica de CPU, pasar 'modoCPU' al Partido
        int limite = (modoPartido == Partido.Modo.TIEMPO) ? duracionSegundos : golesObjetivo;
        partido = new Partido(pais1, pais2, modoPartido, limite);

        imgFondo = cargarImagen("/fondos/" + estadio + ".png");
        sprites1 = cargarSprites(pais1);
        sprites2 = cargarSprites(pais2);
        imgBalon = cargarImagen("/Elementos/balon.png");
        imgAura = cargarImagen("/Elementos/aura.png");
        imgCancha = cargarImagen("/Elementos/arco_derecho.png");

        panel = new PanelJuego();
        contenedor.add(panel, "juego");
        cartas.show(contenedor, "juego");
        panel.requestFocusInWindow();

        timerJuego = new Timer(16, e -> {
            try {
                partido.actualizarLogica();
                panel.repaint();
                if (partido.getEstado() == Partido.Estado.TERMINADO) {
                    timerJuego.stop();
                    mostrarPantallaFin();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                timerJuego.stop();
            }
        });
        timerJuego.start();
    }

    // ---------------- PANTALLA FINAL ----------------

    private void mostrarPantallaFin() {
        int[] goles = partido.getGoles();
        String nombre1 = "JUGADOR 1";
        String nombre2 = modoCPU ? "CPU" : "JUGADOR 2";

        String resultado;
        String paisGanador = null;
        Jugador ganador = partido.getGanador();
        if (ganador == partido.getJugador()) {
            resultado = "¡GANA " + nombre1 + "!";
            paisGanador = pais1;
        } else if (ganador == partido.getJugador2()) {
            resultado = "¡GANA " + nombre2 + "!";
            paisGanador = pais2;
        } else {
            resultado = "¡EMPATE!"; // no debería ocurrir: el empate se resuelve en muerte súbita
        }

        JPanel fin = new JPanel();
        fin.setLayout(new BoxLayout(fin, BoxLayout.Y_AXIS));
        fin.setBackground(FONDO_MENU);
        fin.setPreferredSize(new Dimension(1280, 720));

        fin.add(Box.createVerticalGlue());

        JLabel titulo = new JLabel("FIN DEL JUEGO");
        titulo.setFont(new Font("Arial", Font.BOLD, 54));
        titulo.setForeground(Color.WHITE);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        fin.add(titulo);
        fin.add(Box.createVerticalStrut(30));

        // Bandera del ganador (si hay ganador)
        if (paisGanador != null) {
            Image bandera = cargarImagen(CARPETA_BANDERAS + paisGanador + ".png");
            if (bandera != null) {
                JLabel lblBandera = new JLabel(new ImageIcon(bandera.getScaledInstance(150, 100, Image.SCALE_SMOOTH)));
                lblBandera.setAlignmentX(Component.CENTER_ALIGNMENT);
                fin.add(lblBandera);
                fin.add(Box.createVerticalStrut(20));
            }
        }

        JLabel lblResultado = new JLabel(resultado);
        lblResultado.setFont(new Font("Arial", Font.BOLD, 40));
        lblResultado.setForeground(SELECCION);
        lblResultado.setAlignmentX(Component.CENTER_ALIGNMENT);
        fin.add(lblResultado);
        fin.add(Box.createVerticalStrut(15));

        JLabel lblMarcador = new JLabel(nombre1 + "  " + goles[0] + " - " + goles[1] + "  " + nombre2);
        lblMarcador.setFont(new Font("Arial", Font.BOLD, 28));
        lblMarcador.setForeground(COLOR_TITULO_SECCION);
        lblMarcador.setAlignmentX(Component.CENTER_ALIGNMENT);
        fin.add(lblMarcador);
        fin.add(Box.createVerticalStrut(40));

        JButton volver = new JButton("VOLVER AL MENÚ");
        volver.setFont(new Font("Arial", Font.BOLD, 28));
        volver.setFocusPainted(false);
        volver.setAlignmentX(Component.CENTER_ALIGNMENT);
        volver.addActionListener(e -> cartas.show(contenedor, "menu"));
        fin.add(volver);

        fin.add(Box.createVerticalGlue());

        if (panelFin != null) contenedor.remove(panelFin);
        panelFin = fin;
        contenedor.add(panelFin, "fin");
        cartas.show(contenedor, "fin");
        contenedor.revalidate();
        contenedor.repaint();
    }

    // Pausa / reanudar (teclas P o ESC, o botones del menú de pausa)
    private void alternarPausa() {
        if (partido == null || panel == null || partido.getEstado() == Partido.Estado.TERMINADO) return;
        if (partido.isPausado()) partido.reanudar(); else partido.pausar();
        panel.mostrarPausa(partido.isPausado());
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

    private class PanelJuego extends JPanel {
        private JPanel panelPausa;
        private boolean teclaPausaSostenida;

        PanelJuego() {
            setPreferredSize(new Dimension(1280, 720));
            setFocusable(true);

            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    int c = e.getKeyCode();
                    if (c == KeyEvent.VK_ESCAPE || c == KeyEvent.VK_P) {
                        if (!teclaPausaSostenida) alternarPausa(); // evita repetir mientras se mantiene
                        teclaPausaSostenida = true;
                    } else {
                        partido.teclaPresionada(c);
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    int c = e.getKeyCode();
                    if (c == KeyEvent.VK_ESCAPE || c == KeyEvent.VK_P) {
                        teclaPausaSostenida = false;
                    } else {
                        partido.teclaLiberada(c);
                    }
                }
            });

            // Menú de pausa centrado, oculto hasta que se pausa
            setLayout(new GridBagLayout());
            panelPausa = crearPanelPausa();
            panelPausa.setVisible(false);
            add(panelPausa);
        }

        void mostrarPausa(boolean pausado) {
            panelPausa.setVisible(pausado);
            requestFocusInWindow();
            revalidate();
            repaint();
        }

        private JPanel crearPanelPausa() {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setOpaque(false);

            JLabel titulo = new JLabel("PAUSA");
            titulo.setFont(new Font("Arial", Font.BOLD, 64));
            titulo.setForeground(Color.WHITE);
            titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
            p.add(titulo);
            p.add(Box.createVerticalStrut(8));

            JLabel ayuda = new JLabel("ESC o P para continuar");
            ayuda.setFont(new Font("Arial", Font.PLAIN, 16));
            ayuda.setForeground(COLOR_TITULO_SECCION);
            ayuda.setAlignmentX(Component.CENTER_ALIGNMENT);
            p.add(ayuda);
            p.add(Box.createVerticalStrut(30));

            JButton reanudar = crearBotonPausa("REANUDAR");
            reanudar.addActionListener(e -> alternarPausa());
            p.add(reanudar);
            p.add(Box.createVerticalStrut(14));

            JButton menuPrincipal = crearBotonPausa("VOLVER AL MENÚ");
            menuPrincipal.addActionListener(e -> {
                if (timerJuego != null) timerJuego.stop();
                cartas.show(contenedor, "menu");
            });
            p.add(menuPrincipal);
            return p;
        }

        private JButton crearBotonPausa(String texto) {
            JButton b = new JButton(texto);
            b.setFont(new Font("Arial", Font.BOLD, 26));
            b.setFocusable(false); // así el panel conserva el foco y ESC/P siguen funcionando
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            Dimension d = new Dimension(320, 52);
            b.setPreferredSize(d);
            b.setMaximumSize(d);
            return b;
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

            // Ilusion (EEUU): los balones falsos se dibujan igual que el real
            if (imgBalon != null) {
                for (Balon f : partido.getBalonesFalsos()) {
                    g2.drawImage(imgBalon, (int) f.getX(), (int) f.getY(), f.getSize(), f.getSize(), this);
                }
            }

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
            dibujarTiempo(g2);
            dibujarBarraPoder(g2, partido.getJugador(), 30, 30, new Color(33, 150, 243));
            dibujarBarraPoder(g2, partido.getJugador2(), getWidth() - 30 - 240, 30, new Color(244, 67, 54));

            if (partido.isPausado()) {
                g2.setColor(new Color(0, 0, 0, 160));
                g2.fillRect(0, 0, getWidth(), getHeight());
            } else {
                g2.setFont(new Font("Arial", Font.PLAIN, 12));
                g2.setColor(new Color(255, 255, 255, 170));
                centrarTexto(g2, "P / ESC: pausa", getWidth() / 2, getHeight() - 8);
            }
        }

        private void dibujarJugador(Graphics2D g2, Jugador j, Map<String, Image> sprites,
                                    boolean voltear, Color colorRespaldo) {
            // Aura animada (4 frames en horizontal) detras del jugador mientras tiene el poder activo
            if (j.tienePoder() && imgAura != null) {
                int frames = 4;
                int fw = imgAura.getWidth(this) / frames;
                int fh = imgAura.getHeight(this);
                int f = (int) ((System.currentTimeMillis() / 100) % frames);
                int w = (int) (j.getAncho() * 1.4);
                int h = (int) (j.getAlto() * 1.2);
                int ax = (int) j.getX() + (j.getAncho() - w) / 2;
                int ay = (int) j.getY() + j.getAlto() - h;
                g2.drawImage(imgAura, ax, ay, ax + w, ay + h, f * fw, 0, (f + 1) * fw, fh, this);
            }

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
            centrarTexto(g2, modoCPU ? "CPU" : "JUGADOR 2", x + 3 * w / 4, y + 22);

            // Goles
            g2.setFont(new Font("Arial", Font.BOLD, 42));
            g2.setColor(Color.WHITE);
            centrarTexto(g2, String.valueOf(goles[0]), x + w / 4, y + 64);
            centrarTexto(g2, "-", x + w / 2, y + 60);
            centrarTexto(g2, String.valueOf(goles[1]), x + 3 * w / 4, y + 64);
        }

        // Contador de tiempo debajo del marcador (o aviso de muerte súbita)
        private void dibujarTiempo(Graphics2D g2) {
            boolean muerteSubita = partido.getEstado() == Partido.Estado.MUERTE_SUBITA;
            int segundos = (int) Math.ceil(partido.getTiempoRestante());

            boolean porGoles = partido.getModo() == Partido.Modo.GOLES;

            String texto;
            if (porGoles) texto = "PRIMERO A " + partido.getGolesObjetivo() + " GOLES";
            else if (muerteSubita) texto = "MUERTE SÚBITA";
            else texto = String.format("%d:%02d", segundos / 60, segundos % 60);

            int w = porGoles ? 260 : (muerteSubita ? 220 : 120), h = 36;
            int x = (getWidth() - w) / 2, y = 98;

            g2.setColor(new Color(10, 10, 22, 220));
            g2.fillRoundRect(x, y, w, h, 20, 20);
            g2.setColor(new Color(255, 255, 255, 120));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(x, y, w, h, 20, 20);
            g2.setStroke(new BasicStroke(1f));

            boolean urgente = !porGoles && (muerteSubita || (partido.getEstado() == Partido.Estado.JUGANDO && segundos <= 10));
            g2.setFont(new Font("Arial", Font.BOLD, (porGoles || muerteSubita) ? 20 : 24));
            g2.setColor(urgente ? new Color(255, 90, 80) : Color.WHITE);
            centrarTexto(g2, texto, x + w / 2, y + 26);
        }

        // Barra de carga del poder de un jugador, con porcentaje
        private void dibujarBarraPoder(Graphics2D g2, Jugador j, int x, int y, Color colorCarga) {
            int w = 240, h = 24;
            double carga = Math.max(0, Math.min(100, j.getCargaPoder()));
            boolean listo = carga >= 100;

            // Fondo
            g2.setColor(new Color(10, 10, 22, 220));
            g2.fillRoundRect(x, y, w, h, 14, 14);

            // Relleno
            g2.setColor(listo ? SELECCION : colorCarga);
            g2.fillRoundRect(x, y, (int) (w * carga / 100.0), h, 14, 14);

            // Borde
            g2.setColor(new Color(255, 255, 255, 150));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(x, y, w, h, 14, 14);
            g2.setStroke(new BasicStroke(1f));

            // Texto
            String texto;
            if (j.tienePoder()) texto = "PODER ACTIVO";
            else if (listo) texto = "¡PODER LISTO!";
            else texto = "PODER " + (int) carga + "%";

            g2.setFont(new Font("Arial", Font.BOLD, 14));
            g2.setColor(listo && !j.tienePoder() ? Color.BLACK : Color.WHITE);
            centrarTexto(g2, texto, x + w / 2, y + 17);
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