package Test;

import static org.junit.jupiter.api.Assertions.*;

import Domain.*;
import Domain.Estadisticas.Stat;
import java.awt.event.KeyEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class PruebasUnidad {

    static final class TestUtil {
        private TestUtil() {}

        static void colocarBalon(Balon b, double x, double y) {
            setDouble(b, "x", x);
            setDouble(b, "y", y);
            setDouble(b, "vx", 0);
            setDouble(b, "vy", 0);
        }

        private static void setDouble(Balon b, String campo, double valor) {
            try {
                Field f = Balon.class.getDeclaredField(campo);
                f.setAccessible(true);
                f.setDouble(b, valor);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }

        /** Hace parecer que el partido empezo hace 'segundos' segundos. */
        static void adelantarReloj(Partido p, long segundos) {
            try {
                Field f = Partido.class.getDeclaredField("inicioNs");
                f.setAccessible(true);
                f.setLong(p, System.nanoTime() - segundos * 1_000_000_000L);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Nested
    class BalonTest {

        @Test
        void estadoInicial() {
            Balon b = new Balon(600, 150, 80, 0.75);
            assertEquals(600, b.getX(), 1e-9);
            assertEquals(150, b.getY(), 1e-9);
            assertEquals(80, b.getSize());
            assertEquals(0, b.getVx(), 1e-9);
            assertEquals(0, b.getVy(), 1e-9);
            assertFalse(b.tienePoderActivo());
            assertNull(b.getLanzador());
            assertEquals(600, b.getHitbox().x);
            assertEquals(80, b.getHitbox().width);
        }

        @Test
        void gravedadAceleraHaciaAbajo() {
            Balon b = new Balon(600, 150, 80, 0.75);
            b.actualizar(1);
            assertEquals(0.4, b.getVy(), 1e-9);
            assertEquals(150.4, b.getY(), 1e-9);
        }

        @Test
        void golpearAsignaVelocidadSegunFuerzaYAngulo() {
            Balon b = new Balon(600, 150, 80, 0.75);
            b.golpear(10, 0);
            assertEquals(10, b.getVx(), 1e-9);
            assertEquals(0, b.getVy(), 1e-9);

            b.golpear(10, -Math.PI / 2);
            assertEquals(0, b.getVx(), 1e-9);
            assertEquals(-10, b.getVy(), 1e-9);
        }

        @Test
        void reboteEnElSueloInvierteYAmortiguaVelocidad() {
            Balon b = new Balon(500, 700, 80, 0.5);
            b.golpear(10, Math.PI / 2); // vy = 10
            b.actualizar(1);            // vy = 10.4, y > 640 -> rebota
            assertEquals(640, b.getY(), 1e-9);
            assertEquals(-5.2, b.getVy(), 1e-9);
        }

        @Test
        void reboteParedIzquierda() {
            Balon b = new Balon(5, 100, 80, 0.5);
            b.golpear(10, Math.PI); // vx = -10
            b.actualizar(1);
            assertEquals(0, b.getX(), 1e-9);
            assertEquals(5, b.getVx(), 1e-9);
        }

        @Test
        void rebotarVerticalFijaYEInvierteVy() {
            Balon b = new Balon(500, 100, 80, 0.75);
            b.golpear(10, Math.PI / 2); // vy = 10
            b.rebotarVertical(235);
            assertEquals(235, b.getY(), 1e-9);
            assertEquals(-7.5, b.getVy(), 1e-9);
            assertEquals(235, b.getHitbox().y);
        }

        @Test
        void activarYLimpiarPoder() {
            Balon b = new Balon(100, 100, 80, 0.75);
            Jugador j = new Jugador(200, 300, 1, "alemania");
            Poder p = new PoderPrueba();

            b.activarPoder(p, j);
            assertTrue(b.tienePoderActivo());
            assertSame(p, b.getPoderActivo());
            assertSame(j, b.getLanzador());

            b.limpiarPoder();
            assertFalse(b.tienePoderActivo());
            assertNull(b.getLanzador());
        }

        @Test
        void reiniciarRestauraPosicionVelocidadYPoder() {
            Balon b = new Balon(600, 150, 80, 0.75);
            b.golpear(10, 0);
            b.activarPoder(new PoderPrueba(), new Jugador(0, 0, 1, "alemania"));
            b.actualizar(1);

            b.reiniciar();
            assertEquals(600, b.getX(), 1e-9);
            assertEquals(150, b.getY(), 1e-9);
            assertEquals(0, b.getVx(), 1e-9);
            assertEquals(0, b.getVy(), 1e-9);
            assertFalse(b.tienePoderActivo());
            assertNull(b.getLanzador());
        }

        // ---------- Balon falso (Ilusion) ----------

        @Test
        void abanicoCreaLaCantidadPedidaConLaMismaPosicionYVelocidad() {
            Balon real = new Balon(300, 200, 80, 0.75);
            real.golpear(10, 0);
            java.util.List<Balonfalso> falsos = Balonfalso.abanico(real, 7);

            assertEquals(7, falsos.size());
            for (Balonfalso f : falsos) {
                assertEquals(real.getX(), f.getX(), 1e-9);
                assertEquals(real.getY(), f.getY(), 1e-9);
                assertEquals(real.getSize(), f.getSize());
                assertEquals(10, Math.hypot(f.getVx(), f.getVy()), 1e-9);
            }
        }

        @Test
        void abanicoDesviaLosAngulosAAmbosLados() {
            Balon real = new Balon(300, 200, 80, 0.75);
            real.golpear(10, 0);
            java.util.List<Balonfalso> falsos = Balonfalso.abanico(real, 4);

            assertEquals(-0.2, Math.atan2(falsos.get(0).getVy(), falsos.get(0).getVx()), 1e-9);
            assertEquals(0.2,  Math.atan2(falsos.get(1).getVy(), falsos.get(1).getVx()), 1e-9);
            assertEquals(-0.4, Math.atan2(falsos.get(2).getVy(), falsos.get(2).getVx()), 1e-9);
            assertEquals(0.4,  Math.atan2(falsos.get(3).getVy(), falsos.get(3).getVx()), 1e-9);
        }

    }

    @Nested
    class CanchaTest {

        @Test
        void techoCubreTodoElAnchoYTiene10DeAlto() {
            Cancha c = new Cancha(200, 400, 1080, 315);
            assertEquals(1080, c.getTecho().x);
            assertEquals(315, c.getTecho().y);
            assertEquals(200, c.getTecho().width);
            assertEquals(10, c.getTecho().height);
        }

        @Test
        void redEstaEnElCentroDelArcoYTieneLaAlturaCompleta() {
            Cancha c = new Cancha(200, 400, 1080, 315);
            assertEquals(1180, c.getRed().x); // x + ancho/2
            assertEquals(315, c.getRed().y);
            assertEquals(5, c.getRed().width);
            assertEquals(400, c.getRed().height);
        }
    }

    @Nested
    class EstadisticasTest {

        private Estadisticas e;

        @BeforeEach
        void setUp() { e = new Estadisticas(); }

        @Test
        void valoresBaseEnNivel1() {
            assertEquals(5.0, e.getVelocidad(), 1e-9);
            assertEquals(12.0, e.getFuerzaSalto(), 1e-9);
            assertEquals(100.0, e.getVidaMaxima(), 1e-9);
            assertEquals(1.0, e.getMultiplicadorKick(), 1e-9);
            assertEquals(10.0, e.getdanoPatada(), 1e-9);
            for (Stat s : Stat.values()) {
                assertEquals(1, e.getNivel(s));
                assertEquals(0, e.getMejoras(s));
                assertFalse(e.estaAlMaximo(s));
            }
        }

        @Test
        void cadaMejoraSuma10PorCientoDelValorBase() {
            e.subir(Stat.SPEED);
            e.subir(Stat.SPEED);
            assertEquals(6.0, e.getVelocidad(), 1e-9);   // 5 * 1.2
            assertEquals(3, e.getNivel(Stat.SPEED));
        }

        @Test
        void cadaStatEsIndependiente() {
            e.subir(Stat.KICK);
            assertEquals(1.1, e.getMultiplicadorKick(), 1e-9);
            assertEquals(11.0, e.getdanoPatada(), 1e-9);
            assertEquals(5.0, e.getVelocidad(), 1e-9);
            assertEquals(12.0, e.getFuerzaSalto(), 1e-9);
            assertEquals(100.0, e.getVidaMaxima(), 1e-9);
        }

        @Test
        void elPrecioSeDuplicaConCadaMejora() {
            assertEquals(500, e.getCostoSiguiente(Stat.SPEED));
            e.subir(Stat.SPEED);
            assertEquals(1000, e.getCostoSiguiente(Stat.SPEED));
            e.subir(Stat.SPEED);
            assertEquals(2000, e.getCostoSiguiente(Stat.SPEED));
            e.subir(Stat.SPEED);
            assertEquals(4000, e.getCostoSiguiente(Stat.SPEED));
            // las otras stats siguen en el precio inicial
            assertEquals(500, e.getCostoSiguiente(Stat.KICK));
        }

        @Test
        void noSePuedeSuperarElNivelMaximo() {
            for (int i = 0; i < Estadisticas.MAX_MEJORAS; i++) {
                assertTrue(e.subir(Stat.KICK), "mejora " + (i + 1));
            }
            assertTrue(e.estaAlMaximo(Stat.KICK));
            assertEquals(Estadisticas.NIVEL_MAX, e.getNivel(Stat.KICK));
            assertEquals(1.9, e.getMultiplicadorKick(), 1e-9);
            assertEquals(19.0, e.getdanoPatada(), 1e-9);
            assertFalse(e.subir(Stat.KICK));
            assertEquals(Estadisticas.MAX_MEJORAS, e.getMejoras(Stat.KICK));
        }
    }

    @Nested
    class JugadorTest {

        /** Jugador apoyado en el suelo (720 - alto 255 = 465). Pais sin poder real: PoderPrueba. */
        private Jugador suelo(int lado) {
            return new Jugador(lado == 1 ? 200 : 880, 465, lado, "alemania");
        }

        private Balon balonLejos() { return new Balon(1000, 100, 80, 0.75); }

        private void avanzar(Jugador j, int frames) {
            for (int i = 0; i < frames; i++) j.actualizar(1);
        }

        // ---------- Estado inicial ----------

        @Test
        void estadoInicial() {
            Jugador j = new Jugador(200, 300, 1, "alemania");
            assertEquals(1, j.getLado());
            assertEquals("alemania", j.getPais());
            assertEquals(0, j.getGoles());
            assertEquals(0, j.getPuntos());
            assertEquals(0, j.getCargaPoder(), 1e-9);
            assertEquals(100, j.getVida(), 1e-9);
            assertEquals(100, j.getVidaMaxima(), 1e-9);
            assertFalse(j.tienePoder());
            assertFalse(j.estaAturdido());
            assertNull(j.getEfectoActivo());
            assertNotNull(j.getPoder());
        }

        // ---------- Movimiento ----------

        @Test
        void moverDerechaEIzquierda() {
            Jugador j = suelo(1);
            j.mover(1);
            j.actualizar(1);
            assertEquals(205, j.getX(), 1e-9);
            j.mover(-1);
            j.actualizar(1);
            assertEquals(200, j.getX(), 1e-9);
            j.mover(0);
            j.actualizar(1);
            assertEquals(200, j.getX(), 1e-9);
        }

        @Test
        void noSalePorLosBordesDeLaPantalla() {
            Jugador izq = new Jugador(2, 465, 1, "alemania");
            izq.mover(-1);
            izq.actualizar(1);
            assertEquals(0, izq.getX(), 1e-9);

            Jugador der = new Jugador(1078, 465, 2, "alemania");
            der.mover(1);
            der.actualizar(1);
            assertEquals(1080, der.getX(), 1e-9); // 1280 - ancho
        }

        @Test
        void saltarDesdeElSuelo() {
            Jugador j = suelo(1);
            j.saltar();
            j.actualizar(1);
            assertTrue(j.getY() < 465);
            assertEquals("jump", j.getEstado());
        }

        @Test
        void noHayDobleSalto() {
            Jugador j = suelo(1);
            j.saltar();
            j.actualizar(1);            // vy = -11.5
            double y1 = j.getY();
            j.saltar();                 // debe ignorarse
            j.actualizar(1);            // vy = -11.0
            assertEquals(y1 - 11.0, j.getY(), 1e-9);
        }

        @Test
        void saltoBloqueadoNoSalta() {
            Jugador j = suelo(1);
            j.setSaltoBloqueado(true);
            j.saltar();
            j.actualizar(1);
            assertEquals(465, j.getY(), 1e-9);
            j.setSaltoBloqueado(false);
            j.saltar();
            j.actualizar(1);
            assertTrue(j.getY() < 465);
        }

        @Test
        void aturdidoNoSaltaNiSeMueve() {
            Jugador j = suelo(1);
            j.aturdir(10);
            j.mover(1);
            j.saltar();
            j.actualizar(1);
            assertEquals(200, j.getX(), 1e-9);
            assertEquals(465, j.getY(), 1e-9);
        }

        // ---------- Estados (sprites) ----------

        @Test
        void estadosDeAnimacion() {
            Jugador j = suelo(1);
            j.actualizar(1);
            assertEquals("idle", j.getEstado());

            j.mover(1);
            assertEquals("forward", j.getEstado());
            j.mover(0);

            Jugador aire = new Jugador(200, 300, 1, "alemania");
            aire.actualizar(1);
            assertEquals("fall", aire.getEstado());

            Jugador aturdido = suelo(1);
            aturdido.aturdir(5);
            assertEquals("stun", aturdido.getEstado());
        }

        // ---------- Dash ----------

        @Test
        void dashLado1VaALaDerechaYLado2ALaIzquierda() {
            Jugador j1 = suelo(1);
            j1.dash();
            j1.actualizar(1);
            assertEquals(212, j1.getX(), 1e-9);
            assertEquals("slide", j1.getEstado());

            Jugador j2 = suelo(2);
            j2.dash();
            j2.actualizar(1);
            assertEquals(868, j2.getX(), 1e-9);
        }

        @Test
        void dashTieneCooldownDe120Frames() {
            Jugador j = suelo(1);
            j.dash();
            j.actualizar(1);
            j.dash();               // en cooldown: no reinicia el slide
            avanzar(j, 19);         // 20 frames en total
            assertNotEquals("slide", j.getEstado());

            avanzar(j, 100);        // 120 frames desde el primer dash
            j.dash();
            j.actualizar(1);
            assertEquals("slide", j.getEstado());
        }

        // ---------- Patada ----------

        @Test
        void patearElBalonLoImpulsaSegunElLado() {
            Jugador j1 = new Jugador(200, 300, 1, "alemania");
            Balon b1 = new Balon(300, 400, 80, 0.75);
            j1.patear(b1, new Jugador(880, 300, 2, "alemania"));
            assertEquals(14 * Math.cos(-Math.PI / 4), b1.getVx(), 1e-9);
            assertTrue(b1.getVx() > 0);
            assertTrue(b1.getVy() < 0);

            Jugador j2 = new Jugador(880, 300, 2, "alemania");
            Balon b2 = new Balon(960, 400, 80, 0.75);
            j2.patear(b2, new Jugador(200, 300, 1, "alemania"));
            assertTrue(b2.getVx() < 0);
            assertTrue(b2.getVy() < 0);
        }

        @Test
        void patearAlRivalCercaLeHaceDano() {
            Jugador j = new Jugador(200, 300, 1, "alemania");
            Jugador rival = new Jugador(300, 300, 2, "alemania");
            j.patear(balonLejos(), rival);
            assertEquals(90, rival.getVida(), 1e-9);
        }

        @Test
        void siElBalonYElRivalEstanAlAlcanceSoloSePateaElBalon() {
            Jugador j = new Jugador(200, 300, 1, "alemania");
            Jugador rival = new Jugador(300, 300, 2, "alemania");
            Balon b = new Balon(300, 400, 80, 0.75);
            j.patear(b, rival);
            assertEquals(100, rival.getVida(), 1e-9);
            assertTrue(b.getVx() > 0);
        }

        @Test
        void patearTieneCooldown() {
            Jugador j = new Jugador(200, 300, 1, "alemania");
            Jugador rival = new Jugador(300, 300, 2, "alemania");
            j.patear(balonLejos(), rival);
            j.patear(balonLejos(), rival);
            assertEquals(90, rival.getVida(), 1e-9);   // solo la primera cuenta
            assertEquals("kick", j.getEstado());
        }

        @Test
        void elDanoDeLaPatadaEscalaConLaMejoraKick() {
            Jugador j = new Jugador(200, 300, 1, "alemania");
            j.sumarPuntos(500);
            assertTrue(j.comprarMejora(Stat.KICK));
            Jugador rival = new Jugador(300, 300, 2, "alemania");
            j.patear(balonLejos(), rival);
            assertEquals(89, rival.getVida(), 1e-9);
        }

        // ---------- Poder ----------

        @Test
        void laBarraSeLlenaEn10Segundos() {
            Jugador j = suelo(1);
            avanzar(j, 60);
            assertEquals(10, j.getCargaPoder(), 0.01);
            avanzar(j, 540);
            assertEquals(100, j.getCargaPoder(), 0.5);
        }

        @Test
        void recibirGolSuma25DeCarga() {
            Jugador j = suelo(1);
            j.recibirGol();
            assertEquals(25, j.getCargaPoder(), 1e-9);
            j.recibirGol();
            assertEquals(50, j.getCargaPoder(), 1e-9);
        }

        @Test
        void noSePuedeUsarElPoderSinBarraLlena() {
            Jugador j = suelo(1);
            j.cargaPoder(99);
            assertFalse(j.usarPoder());
            assertFalse(j.tienePoder());
        }

        @Test
        void usarPoderVaciaLaBarraYActivaElAura() {
            Jugador j = suelo(1);
            j.cargaPoder(100);
            assertTrue(j.usarPoder());
            assertTrue(j.tienePoder());
            assertEquals(0, j.getCargaPoder(), 1e-9);
            j.actualizar(1);
            assertEquals("action", j.getEstado());
        }

        // ---------- Vida ----------

        @Test
        void recibirDanoRestaVida() {
            Jugador j = suelo(1);
            j.recibirDaño(30);
            assertEquals(70, j.getVida(), 1e-9);
            assertFalse(j.estaAturdido());
        }

        @Test
        void vidaEn0AturdeYRestauraLaVida() {
            Jugador j = suelo(1);
            j.dash();
            j.recibirDaño(1000);
            assertTrue(j.estaAturdido());
            assertEquals(100, j.getVida(), 1e-9);
            j.actualizar(1);
            assertEquals("stun", j.getEstado());
        }

        // ---------- Efectos ----------

        @Test
        void efectoElectrocutadoReduceVelocidadYBloqueaSalto() {
            Jugador j = suelo(1);
            j.aplicarEfecto(new efectocamerun());
            j.mover(1);
            j.actualizar(1);
            assertEquals(202.5, j.getX(), 1e-9);
            j.saltar();
            j.actualizar(1);
            assertEquals(465, j.getY(), 1e-9);
        }

        @Test
        void elEfectoExpiraYRestauraLosValores() {
            Jugador j = suelo(1);
            j.aplicarEfecto(new efectocamerun());
            avanzar(j, 179);
            assertNotNull(j.getEfectoActivo());
            avanzar(j, 1);
            assertNull(j.getEfectoActivo());

            double x = j.getX();
            j.mover(1);
            j.actualizar(1);
            assertEquals(x + 5, j.getX(), 1e-9);   // velocidad normal
            j.saltar();
            j.actualizar(1);
            assertTrue(j.getY() < 465);            // salto desbloqueado
        }

        @Test
        void unEfectoNuevoReemplazaAlAnteriorYLoQuita() {
            Jugador j = suelo(1);
            j.aplicarEfecto(new efectocamerun());          // factor 0.5
            Efecto nuevo = new Efectocorea();
            j.aplicarEfecto(nuevo);
            assertSame(nuevo, j.getEfectoActivo());

            j.mover(1);
            j.actualizar(1);
            // factor de velocidad restaurado (1.0) y empuje de Corea (-6) para lado 1: 200 + 5 - 6
            assertEquals(199, j.getX(), 1e-9);
        }

        // ---------- Puntos y mejoras ----------

        @Test
        void comprarMejoraSinPuntosFalla() {
            Jugador j = suelo(1);
            j.sumarPuntos(499);
            assertFalse(j.comprarMejora(Stat.SPEED));
            assertEquals(499, j.getPuntos());
            assertEquals(1, j.getStats().getNivel(Stat.SPEED));
        }

        @Test
        void comprarMejoraCobraYSubeElNivel() {
            Jugador j = suelo(1);
            j.sumarPuntos(1600);
            assertTrue(j.comprarMejora(Stat.SPEED));   // 500
            assertEquals(1100, j.getPuntos());
            assertTrue(j.comprarMejora(Stat.SPEED));   // 1000
            assertEquals(100, j.getPuntos());
            assertFalse(j.comprarMejora(Stat.SPEED));  // 2000
            assertEquals(3, j.getStats().getNivel(Stat.SPEED));
        }

        @Test
        void mejorarSupervivenciaSubeYRellenaLaVida() {
            Jugador j = suelo(1);
            j.recibirDaño(50);
            j.sumarPuntos(500);
            assertTrue(j.comprarMejora(Stat.SURVIVAL));
            assertEquals(110, j.getVidaMaxima(), 1e-9);
            assertEquals(110, j.getVida(), 1e-9);
        }

        @Test
        void noSeCompraMasAllaDelNivelMaximo() {
            Jugador j = suelo(1);
            j.sumarPuntos(10_000_000);
            for (int i = 0; i < Estadisticas.MAX_MEJORAS; i++) {
                assertTrue(j.comprarMejora(Stat.JUMP));
            }
            int puntos = j.getPuntos();
            assertFalse(j.comprarMejora(Stat.JUMP));
            assertEquals(puntos, j.getPuntos()); // no cobra
        }
    }

    @Nested
    class PoderesTest {

        private Jugador jugador(int lado) {
            return new Jugador(lado == 1 ? 200 : 880, 465, lado, "alemania");
        }

        // ---------- FabricaPoderes ----------

        @Test
        void fabricaDevuelveElPoderDeCadaPais() {
            assertInstanceOf(Podereeeuu.class, FabricaPoderes.crear("usa"));
            assertInstanceOf(Podereeeuu.class, FabricaPoderes.crear("eeuu"));
            assertInstanceOf(Podercorea.class, FabricaPoderes.crear("corea"));
            assertInstanceOf(Podercamerun.class, FabricaPoderes.crear("camerun"));
            assertInstanceOf(Podernigeria.class, FabricaPoderes.crear("nigeria"));
        }

        @Test
        void paisSinPoderPropioUsaPoderPrueba() {
            assertInstanceOf(PoderPrueba.class, FabricaPoderes.crear("alemania"));
            assertInstanceOf(PoderPrueba.class, FabricaPoderes.crear("desconocido"));
        }

        // ---------- Datos de cada poder ----------

        @Test
        void danoBaseDeCadaPoder() {
            assertEquals(20, new PoderPrueba().getdaño(), 1e-9);
            assertEquals(25, new Podercorea().getdaño(), 1e-9);
            assertEquals(25, new Podercamerun().getdaño(), 1e-9);
            assertEquals(30, new Podereeeuu().getdaño(), 1e-9);
            assertEquals(40, new Podernigeria().getdaño(), 1e-9);
        }

        @Test
        void nombreDelPoderDeCamerun() {
            assertEquals("Camerun", new Podercamerun().getNombre());
        }

        // ---------- Angulo y lanzamiento ----------

        @Test
        void lanzarHaciaLaDerecha() {
            Balon b = new Balon(300, 300, 80, 0.75);
            Jugador j = jugador(1);
            Poder p = new PoderPrueba();
            p.lanzar(b, j, true);
            assertEquals(22, b.getVx(), 1e-9);
            assertEquals(0, b.getVy(), 1e-9);
            assertTrue(b.tienePoderActivo());
            assertSame(p, b.getPoderActivo());
            assertSame(j, b.getLanzador());
        }

        // ---------- Impacto ----------

        @Test
        void impactoDeCoreaHaceDanoYEmpuja() {
            Jugador lanzador = jugador(1), rival = jugador(2);
            new Podercorea().aplicarImpacto(rival, lanzador, new Balon(0, 0, 80, 0.75));
            assertEquals(75, rival.getVida(), 1e-9);
            assertInstanceOf(Efectocorea.class, rival.getEfectoActivo());
        }

        @Test
        void impactoDeNigeriaHaceDanoYAturde() {
            Jugador lanzador = jugador(1), rival = jugador(2);
            new Podernigeria().aplicarImpacto(rival, lanzador, new Balon(0, 0, 80, 0.75));
            assertEquals(60, rival.getVida(), 1e-9);
            assertTrue(rival.estaAturdido());
            assertInstanceOf(efectonigeria.class, rival.getEfectoActivo());
        }

        @Test
        void impactoDeCamerunElectrocuta() {
            Jugador lanzador = jugador(1), rival = jugador(2);
            new Podercamerun().aplicarImpacto(rival, lanzador, new Balon(0, 0, 80, 0.75));
            assertEquals(75, rival.getVida(), 1e-9);
            assertInstanceOf(efectocamerun.class, rival.getEfectoActivo());
        }

        @Test
        void impactoDeEEUUCreaIlusion() {
            Jugador lanzador = jugador(1), rival = jugador(2);
            new Podereeeuu().aplicarImpacto(rival, lanzador, new Balon(0, 0, 80, 0.75));
            assertEquals(70, rival.getVida(), 1e-9);
            assertInstanceOf(Efectoeeuu.class, rival.getEfectoActivo());
        }

        @Test
        void poderDePruebaSoloHaceDano() {
            Jugador lanzador = jugador(1), rival = jugador(2);
            new PoderPrueba().aplicarImpacto(rival, lanzador, new Balon(0, 0, 80, 0.75));
            assertEquals(80, rival.getVida(), 1e-9);
            assertNull(rival.getEfectoActivo());
        }

        @Test
        void elDanoDelPoderEscalaConElKickDelLanzador() {
            Jugador lanzador = jugador(1), rival = jugador(2);
            lanzador.sumarPuntos(500);
            assertTrue(lanzador.comprarMejora(Stat.KICK)); // multiplicador 1.1
            new Podercorea().aplicarImpacto(rival, lanzador, new Balon(0, 0, 80, 0.75));
            assertEquals(100 - 27.5, rival.getVida(), 1e-9);
        }
    }

    @Nested
    class EfectosTest {

        private Jugador jugador(int lado, double x) {
            return new Jugador(x, 465, lado, "alemania");
        }

        private void avanzar(Jugador j, int frames) {
            for (int i = 0; i < frames; i++) j.actualizar(1);
        }

        // ---------- Efecto base ----------

        @Test
        void elEfectoExpiraCuandoSeAcabaLaDuracion() {
            Efecto e = new efectocamerun();
            assertFalse(e.haExpirado());
            e.actualizar(100);
            assertFalse(e.haExpirado());
            e.actualizar(80);
            assertTrue(e.haExpirado());
        }

        // ---------- Camerun ----------

        @Test
        void camerunAplicaYQuitaLosModificadores() {
            Jugador j = jugador(1, 200);
            Efecto e = new efectocamerun();
            e.aplicar(j);
            j.mover(1);
            j.actualizar(1);
            assertEquals(202.5, j.getX(), 1e-9);

            e.quitar(j);
            j.actualizar(1);
            assertEquals(207.5, j.getX(), 1e-9);
        }

        // ---------- Corea ----------

        @Test
        void coreaEmpujaAlLado1HaciaSuArco() {
            Jugador j = jugador(1, 600);
            j.aplicarEfecto(new Efectocorea());
            j.actualizar(1);
            assertEquals(594, j.getX(), 1e-9);
        }

        @Test
        void coreaDura120Frames() {
            Jugador j = jugador(2, 100);
            j.aplicarEfecto(new Efectocorea());
            avanzar(j, 119);
            assertNotNull(j.getEfectoActivo());
            avanzar(j, 1);
            assertNull(j.getEfectoActivo());
        }

        // ---------- Nigeria ----------

        @Test
        void nigeriaAturdeAlJugador() {
            Jugador j = jugador(1, 600);
            j.aplicarEfecto(new efectonigeria());
            assertTrue(j.estaAturdido());
            j.actualizar(1);
            assertEquals("stun", j.getEstado());
        }

        @Test
        void nigeriaLevantaAlJugadorDelSuelo() {
            Jugador j = jugador(1, 600);
            j.aplicarEfecto(new efectonigeria());
            avanzar(j, 10);
            assertTrue(j.getY() < 455, "y = " + j.getY());
        }

        // ---------- Ilusion (EEUU) ----------

        private Balon balonEnMovimiento() {
            Balon real = new Balon(500, 100, 80, 0.75);
            real.golpear(10, 0);
            return real;
        }

        @Test
        void ilusionCreaSieteFalsosAlSiguienteFrame() {
            Efectoeeuu e = new Efectoeeuu();
            e.alImpactar(balonEnMovimiento());
            assertTrue(e.getFalsos().isEmpty());   // aun no
            e.actualizar(1);
            assertEquals(7, e.getFalsos().size());
        }

        @Test
        void ilusionNoDuplicaLosFalsos() {
            Efectoeeuu e = new Efectoeeuu();
            e.alImpactar(balonEnMovimiento());
            e.actualizar(1);
            e.actualizar(1);
            e.actualizar(1);
            assertEquals(7, e.getFalsos().size());
        }

        @Test
        void ilusionSinImpactoNoCreaFalsos() {
            Efectoeeuu e = new Efectoeeuu();
            e.actualizar(1);
            assertTrue(e.getFalsos().isEmpty());
        }

        @Test
        void ilusionDura150FramesYAlQuitarseDesaparecenLosFalsos() {
            Efectoeeuu e = new Efectoeeuu();
            e.alImpactar(balonEnMovimiento());
            e.actualizar(1);
            assertFalse(e.haExpirado());
            e.actualizar(149);
            assertTrue(e.haExpirado());

            e.quitar(jugador(1, 200));
            assertTrue(e.getFalsos().isEmpty());
        }
    }

    @Nested
    class PartidoTest {

        private Partido p;

        @BeforeEach
        void setUp() { p = new Partido("alemania", "corea"); }

        // ---- helpers ----
        /** Balon dentro del arco izquierdo: anota el jugador 2. */
        private void golIzquierdo(Partido partido) {
            TestUtil.colocarBalon(partido.getBalon(), 90, 400);
            partido.actualizarLogica();
        }

        /** Balon dentro del arco derecho: anota el jugador 1. */
        private void golDerecho(Partido partido) {
            TestUtil.colocarBalon(partido.getBalon(), 1150, 400);
            partido.actualizarLogica();
        }

        // ---------- Construccion ----------

        @Test
        void estadoInicialPorDefecto() {
            assertEquals(Partido.Estado.JUGANDO, p.getEstado());
            assertEquals(Partido.Modo.TIEMPO, p.getModo());
            assertEquals(Partido.GOLES_DEFECTO, p.getGolesObjetivo());
            assertArrayEquals(new int[] { 0, 0 }, p.getGoles());
            assertNull(p.getGanador());
            assertFalse(p.isPausado());
            assertEquals(60, p.getTiempoRestante(), 1.0);
        }

        @Test
        void jugadoresBalonYCanchasSeCreanEnSuLugar() {
            assertEquals(1, p.getJugador().getLado());
            assertEquals(2, p.getJugador2().getLado());
            assertEquals("alemania", p.getJugador().getPais());
            assertEquals("corea", p.getJugador2().getPais());
            assertEquals(600, p.getBalon().getX(), 1e-9);
            assertEquals(0, p.getCanchaizq().getX(), 1e-9);
            assertEquals(1080, p.getCanchader().getX(), 1e-9);
        }

        @Test
        void modoTiempoConLimitePersonalizado() {
            Partido q = new Partido("usa", "corea", Partido.Modo.TIEMPO, 90);
            assertEquals(90, q.getTiempoRestante(), 1.0);
        }

        @Test
        void modoGolesTomaElObjetivoYNoTieneReloj() {
            Partido q = new Partido("usa", "corea", Partido.Modo.GOLES, 3);
            assertEquals(Partido.Modo.GOLES, q.getModo());
            assertEquals(3, q.getGolesObjetivo());
            assertEquals(0, q.getTiempoRestante(), 1e-9);
        }

        // ---------- Teclas ----------

        @Test
        void jugador1SeMueveConAyD() {
            p.teclaPresionada(KeyEvent.VK_D);
            p.actualizarLogica();
            assertEquals(205, p.getJugador().getX(), 1e-9);

            p.teclaLiberada(KeyEvent.VK_D);
            p.teclaPresionada(KeyEvent.VK_A);
            p.actualizarLogica();
            assertEquals(200, p.getJugador().getX(), 1e-9);
        }

        @Test
        void jugador2SeMueveConLasFlechas() {
            p.teclaPresionada(KeyEvent.VK_LEFT);
            p.actualizarLogica();
            assertEquals(875, p.getJugador2().getX(), 1e-9);
            p.teclaLiberada(KeyEvent.VK_LEFT);
            p.teclaPresionada(KeyEvent.VK_RIGHT);
            p.actualizarLogica();
            assertEquals(880, p.getJugador2().getX(), 1e-9);
        }

        @Test
        void saltarConWyConFlechaArriba() {
            p.teclaPresionada(KeyEvent.VK_W);
            p.teclaPresionada(KeyEvent.VK_UP);
            p.actualizarLogica();
            assertTrue(p.getJugador().getY() < 300);
            assertTrue(p.getJugador2().getY() < 300);
        }

        @Test
        void patearConF() {
            TestUtil.colocarBalon(p.getBalon(), 300, 400);
            p.teclaPresionada(KeyEvent.VK_F);
            assertTrue(p.getBalon().getVx() > 0);
        }

        @Test
        void patearConComa() {
            TestUtil.colocarBalon(p.getBalon(), 960, 400);
            p.teclaPresionada(KeyEvent.VK_COMMA);
            assertTrue(p.getBalon().getVx() < 0);
        }

        // ---------- Pausa ----------

        @Test
        void enPausaNoAvanzaLaLogica() {
            p.pausar();
            assertTrue(p.isPausado());
            p.teclaPresionada(KeyEvent.VK_D);
            p.actualizarLogica();
            assertEquals(200, p.getJugador().getX(), 1e-9);
            assertEquals(300, p.getJugador().getY(), 1e-9);
        }

        @Test
        void reanudarRetomaElJuegoYConservaLaTeclaSostenida() {
            p.pausar();
            p.teclaPresionada(KeyEvent.VK_D);
            p.reanudar();
            assertFalse(p.isPausado());
            p.actualizarLogica();
            assertEquals(205, p.getJugador().getX(), 1e-9);
        }

        @Test
        void enPausaSeIgnoranLasAcciones() {
            p.pausar();
            p.teclaPresionada(KeyEvent.VK_W);
            p.reanudar();
            p.actualizarLogica();
            assertTrue(p.getJugador().getY() >= 300);
        }

        @Test
        void elTiempoEnPausaNoCuenta() throws InterruptedException {
            p.pausar();
            double t1 = p.getTiempoRestante();
            Thread.sleep(30);
            double t2 = p.getTiempoRestante();
            assertEquals(t1, t2, 1e-9);
            p.reanudar();
            assertEquals(t1, p.getTiempoRestante(), 0.5);
        }

        // ---------- Colisiones ----------

        @Test
        void elBalonRebotaConUnJugadorSinAura() {
            TestUtil.colocarBalon(p.getBalon(), 300, 400);
            p.actualizarLogica();
            assertEquals(6, p.getBalon().getVx(), 1e-6);
            assertEquals(-6, p.getBalon().getVy(), 1e-6);
        }

        @Test
        void conAuraElContactoLanzaElTiroDePoder() {
            Jugador j = p.getJugador();
            j.cargaPoder(100);
            assertTrue(j.usarPoder());
            TestUtil.colocarBalon(p.getBalon(), 300, 400);
            p.actualizarLogica();

            assertFalse(j.tienePoder());                       // el aura se consume
            assertTrue(p.getBalon().tienePoderActivo());
            assertSame(j, p.getBalon().getLanzador());
            assertEquals(22, p.getBalon().getVx(), 1e-9);
        }

        @Test
        void elTiroDePoderDanaYAplicaEfectoAlRival() {
            Jugador rival = p.getJugador2();
            p.getBalon().activarPoder(new Podercorea(), p.getJugador());
            TestUtil.colocarBalon(p.getBalon(), 960, 400);
            p.actualizarLogica();

            assertEquals(75, rival.getVida(), 1e-9);
            assertInstanceOf(Efectocorea.class, rival.getEfectoActivo());
            assertFalse(p.getBalon().tienePoderActivo()); // impacto una sola vez
        }

        @Test
        void elTiroNoDanaAlPropioLanzador() {
            Jugador lanzador = p.getJugador2();
            p.getBalon().activarPoder(new Podercorea(), lanzador);
            TestUtil.colocarBalon(p.getBalon(), 960, 400);
            p.actualizarLogica();

            assertEquals(100, lanzador.getVida(), 1e-9);
            assertNull(lanzador.getEfectoActivo());
            assertTrue(p.getBalon().tienePoderActivo());
        }

        @Test
        void losJugadoresSeSeparanSiSeSuperponen() {
            p.getJugador2().reiniciarPos(250, 300); // hitboxes encimados
            p.actualizarLogica();
            assertTrue(p.getJugador().getX() < 200);
            assertTrue(p.getJugador2().getX() > 250);
        }

        @Test
        void laIlusionDeEEUUCreaBalonesFalsosEnElPartido() {
            Partido q = new Partido("alemania", "alemania");
            q.getJugador2().aplicarEfecto(new Efectoeeuu());
            // El efecto necesita el balon real en movimiento
            Efecto ef = q.getJugador2().getEfectoActivo();
            Balon real = q.getBalon();
            real.golpear(10, 0);
            ef.alImpactar(real);
            q.actualizarLogica();
            assertEquals(7, q.getBalonesFalsos().size());
        }

        // ---------- Goles ----------

        @Test
        void golEnElArcoIzquierdoLoAnotaElJugador2() {
            golIzquierdo(p);
            assertArrayEquals(new int[] { 0, 1 }, p.getGoles());
        }

        @Test
        void golEnElArcoDerechoLoAnotaElJugador1() {
            golDerecho(p);
            assertArrayEquals(new int[] { 1, 0 }, p.getGoles());
        }

        @Test
        void trasUnGolElBalonVuelveAlCentroYElReceptorGanaCarga() {
            golIzquierdo(p);
            assertEquals(600, p.getBalon().getX(), 1e-9);
            assertEquals(150, p.getBalon().getY(), 1e-9);
            assertFalse(p.getBalon().tienePoderActivo());
            assertTrue(p.getJugador().getCargaPoder() >= 25);   // recibio el gol
            assertTrue(p.getJugador2().getCargaPoder() < 25);   // el anotador no
        }

        @Test
        void golConPoderActivoLimpiaElPoderDelBalon() {
            p.getBalon().activarPoder(new PoderPrueba(), p.getJugador());
            golDerecho(p);
            assertFalse(p.getBalon().tienePoderActivo());
        }

        @Test
        void siElBalonTocaElTechoNoCuentaGol() {
            TestUtil.colocarBalon(p.getBalon(), 90, 270); // toca techo y red a la vez
            p.actualizarLogica();
            assertArrayEquals(new int[] { 0, 0 }, p.getGoles());
            assertEquals(235, p.getBalon().getY(), 1e-9); // 315 - size
        }

        // ---------- Fin del partido ----------

        @Test
        void modoGolesTerminaAlLlegarAlObjetivo() {
            Partido q = new Partido("alemania", "corea", Partido.Modo.GOLES, 2);
            golIzquierdo(q);
            assertEquals(Partido.Estado.JUGANDO, q.getEstado());
            golIzquierdo(q);
            assertEquals(Partido.Estado.TERMINADO, q.getEstado());
            assertSame(q.getJugador2(), q.getGanador());
        }

        @Test
        void alTerminarSeOtorganPuntos() {
            Partido q = new Partido("alemania", "corea", Partido.Modo.GOLES, 1);
            golIzquierdo(q);
            assertEquals(100, q.getJugador2().getPuntos());  // ganador
            assertEquals(25, q.getJugador().getPuntos());    // perdedor
        }

        @Test
        void modoTiempoGanaQuienVayaAdelanteAlAcabar() {
            Partido q = new Partido("alemania", "corea", Partido.Modo.TIEMPO, 30);
            golDerecho(q);                      // 1 - 0
            TestUtil.adelantarReloj(q, 31);
            q.actualizarLogica();
            assertEquals(Partido.Estado.TERMINADO, q.getEstado());
            assertSame(q.getJugador(), q.getGanador());
        }

        @Test
        void empateAlAcabarElTiempoVaAMuerteSubita() {
            Partido q = new Partido("alemania", "corea", Partido.Modo.TIEMPO, 30);
            TestUtil.adelantarReloj(q, 31);
            q.actualizarLogica();
            assertEquals(Partido.Estado.MUERTE_SUBITA, q.getEstado());
            assertEquals(0, q.getTiempoRestante(), 1e-9);
            assertNull(q.getGanador());
        }

        @Test
        void enMuerteSubitaElPrimerGolGana() {
            Partido q = new Partido("alemania", "corea", Partido.Modo.TIEMPO, 30);
            TestUtil.adelantarReloj(q, 31);
            q.actualizarLogica();
            golIzquierdo(q);
            assertEquals(Partido.Estado.TERMINADO, q.getEstado());
            assertSame(q.getJugador2(), q.getGanador());
        }
    }
}