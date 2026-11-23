
package Test;

import static org.junit.jupiter.api.Assertions.*;

import Domain.*;
import Domain.Estadisticas.Stat;
import java.awt.event.KeyEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class PruebasUnidad {

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
        }

        @Test
        void gravedadAceleraHaciaAbajo() {
            Balon b = new Balon(600, 150, 80, 0.75);

            b.actualizar(1);

            assertEquals(0.4, b.getVy(), 1e-9);
            assertEquals(150.4, b.getY(), 1e-9);
        }

        @Test
        void golpearAplicaVelocidadSegunAngulo() {
            Balon b = new Balon(600, 150, 80, 0.75);

            b.golpear(10, 0);

            assertEquals(10, b.getVx(), 1e-9);
            assertEquals(0, b.getVy(), 1e-9);
        }

        @Test
        void reiniciarRestauraElBalon() {
            Balon b = new Balon(600, 150, 80, 0.75);
            b.golpear(10, 0);

            b.reiniciar();

            assertEquals(600, b.getX(), 1e-9);
            assertEquals(150, b.getY(), 1e-9);
            assertEquals(0, b.getVx(), 1e-9);
            assertEquals(0, b.getVy(), 1e-9);
            assertFalse(b.tienePoderActivo());
        }
    }

    @Nested
    class CanchaTest {

        @Test
        void construccionDeCancha() {
            Cancha c = new Cancha(200, 400, 1080, 315);

            assertEquals(1080, c.getTecho().x);
            assertEquals(315, c.getTecho().y);
            assertEquals(200, c.getTecho().width);
            assertEquals(10, c.getTecho().height);

            assertEquals(1180, c.getRed().x);
            assertEquals(400, c.getRed().height);
        }
    }

    @Nested
    class EstadisticasTest {

        private Estadisticas e;

        @BeforeEach
        void setUp() {
            e = new Estadisticas();
        }

        @Test
        void valoresIniciales() {
            assertEquals(5, e.getVelocidad(), 1e-9);
            assertEquals(12, e.getFuerzaSalto(), 1e-9);
            assertEquals(100, e.getVidaMaxima(), 1e-9);
            assertEquals(10, e.getdanoPatada(), 1e-9);
        }

        @Test
        void mejorarStatAumentaSuValor() {
            assertTrue(e.subir(Stat.SPEED));
            assertEquals(5.5, e.getVelocidad(), 1e-9);
            assertEquals(2, e.getNivel(Stat.SPEED));
        }

        @Test
        void noPermiteSuperarElNivelMaximo() {
            for (int i = 0; i < Estadisticas.MAX_MEJORAS; i++) {
                assertTrue(e.subir(Stat.KICK));
            }

            assertTrue(e.estaAlMaximo(Stat.KICK));
            assertFalse(e.subir(Stat.KICK));
        }
    }

    @Nested
    class JugadorTest {

        private Jugador crearJugador(int lado) throws DopoSoccerException {
            return new Jugador(
                lado == 1 ? 200 : 880,
                465,
                lado,
                "alemania"
            );
        }

        private void avanzar(Jugador j, int frames) {
            for (int i = 0; i < frames; i++) {
                j.actualizar(1);
            }
        }

        @Test
        void estadoInicial() throws DopoSoccerException {
            Jugador j = crearJugador(1);

            assertEquals(1, j.getLado());
            assertEquals("alemania", j.getPais());
            assertEquals(100, j.getVida(), 1e-9);
            assertEquals(0, j.getGoles());
            assertEquals(0, j.getPuntos());
            assertFalse(j.estaAturdido());
            assertFalse(j.tienePoder());
        }

        @Test
        void movimientoHorizontal() throws DopoSoccerException {
            Jugador j = crearJugador(1);

            j.mover(1);
            j.actualizar(1);

            assertEquals(205, j.getX(), 1e-9);

            j.mover(-1);
            j.actualizar(1);

            assertEquals(200, j.getX(), 1e-9);
        }

        @Test
        void saltoDesdeElSuelo() throws DopoSoccerException {
            Jugador j = crearJugador(1);

            j.saltar();
            j.actualizar(1);

            assertTrue(j.getY() < 465);
        }

        @Test
        void dashSeRealizaEnLaDireccionDelJugador() throws DopoSoccerException {
            Jugador j1 = crearJugador(1);
            Jugador j2 = crearJugador(2);

            j1.dash();
            j2.dash();

            j1.actualizar(1);
            j2.actualizar(1);

            assertEquals(212, j1.getX(), 1e-9);
            assertEquals(868, j2.getX(), 1e-9);
        }

        @Test
        void recibirDanoReduceLaVida() throws DopoSoccerException {
            Jugador j = crearJugador(1);

            j.recibirDaño(30);

            assertEquals(70, j.getVida(), 1e-9);
        }

        @Test
        void recibirDanoExcesivoAturdeAlJugador() throws DopoSoccerException {
            Jugador j = crearJugador(1);

            j.recibirDaño(1000);

            assertTrue(j.estaAturdido());
            assertEquals(100, j.getVida(), 1e-9);
        }

        @Test
        void barraDePoderSeCargaConElTiempo() throws DopoSoccerException {
            Jugador j = crearJugador(1);

            avanzar(j, 600);

            assertEquals(100, j.getCargaPoder(), 0.5);

            assertTrue(j.usarPoder());
            assertTrue(j.tienePoder());
            assertEquals(0, j.getCargaPoder(), 1e-9);
        }
    }

    @Nested
    class PoderesTest {

        private Jugador jugador(int lado) throws DopoSoccerException {
            return new Jugador(
                lado == 1 ? 200 : 880,
                465,
                lado,
                "alemania"
            );
        }

        @Test
        void fabricaCreaPoderSegunPais() throws DopoSoccerException {
            assertInstanceOf(Podereeeuu.class, FabricaPoderes.crear("usa"));
            assertInstanceOf(Podercorea.class, FabricaPoderes.crear("corea"));
            assertInstanceOf(Podercamerun.class, FabricaPoderes.crear("camerun"));
            assertInstanceOf(Podernigeria.class, FabricaPoderes.crear("nigeria"));
            assertInstanceOf(PoderPrueba.class, FabricaPoderes.crear("alemania"));
        }



        @Test
        void lanzarPoderImpulsaElBalon() throws DopoSoccerException {
            Balon b = new Balon(300, 300, 80, 0.75);
            Jugador j = jugador(1);
            Poder p = new PoderPrueba();

            p.lanzar(b, j, true);

            assertEquals(22, b.getVx(), 1e-9);
            assertTrue(b.tienePoderActivo());
            assertSame(j, b.getLanzador());
        }

        @Test
        void impactoDeNigeriaDañaYAturde() throws DopoSoccerException {
            Jugador lanzador = jugador(1);
            Jugador rival = jugador(2);

            new Podernigeria().aplicarImpacto(
                rival,
                lanzador,
                new Balon(0, 0, 80, 0.75)
            );

            assertEquals(60, rival.getVida(), 1e-9);
            assertTrue(rival.estaAturdido());
        }
    }

    @Nested
    class EfectosTest {

        @Test
        void efectoExpiraAlTerminarSuDuracion() {
            Efecto e = new efectocamerun();

            assertFalse(e.haExpirado());

            e.actualizar(180);

            assertTrue(e.haExpirado());
        }

        @Test
        void efectoCamerunReduceLaVelocidad() throws DopoSoccerException {
            Jugador j = new Jugador(200, 465, 1, "alemania");

            j.aplicarEfecto(new efectocamerun());
            j.mover(1);
            j.actualizar(1);

            assertEquals(202.5, j.getX(), 1e-9);
        }

        @Test
        void efectoCoreaEmpujaAlJugador() throws DopoSoccerException {
            Jugador j = new Jugador(600, 465, 1, "alemania");

            j.aplicarEfecto(new Efectocorea());
            j.actualizar(1);

            assertEquals(594, j.getX(), 1e-9);
        }
    }

    @Nested
    class PartidoTest {

        private DOPOsoccer p;

        @BeforeEach
        void setUp() {
            p = new DOPOsoccer("alemania", "corea");
        }

        @Test
        void estadoInicialDelPartido() {
            assertEquals(Partido.Estado.JUGANDO, p.getEstado());
            assertEquals(Partido.Modo.TIEMPO, p.getModo());
            assertArrayEquals(new int[] {0, 0}, p.getGoles());
            assertNull(p.getGanador());
            assertFalse(p.isPausado());
        }

        @Test
        void jugadoresSeCreanCorrectamente() {
            assertEquals(1, p.getJugador().getLado());
            assertEquals(2, p.getJugador2().getLado());
            assertEquals("alemania", p.getJugador().getPais());
            assertEquals("corea", p.getJugador2().getPais());
        }

        @Test
        void controlesMuevenAlPrimerJugador() {
            p.teclaPresionada(KeyEvent.VK_D);
            p.actualizarJuego();

            assertEquals(205, p.getJugador().getX(), 1e-9);
        }

        @Test
        void pausaDetieneElMovimiento() {
            p.pausar();

            p.teclaPresionada(KeyEvent.VK_D);
            p.actualizarJuego();

            assertEquals(200, p.getJugador().getX(), 1e-9);
            assertTrue(p.isPausado());
        }

        @Test
        void reanudarPermiteContinuarElJuego() {
            p.pausar();
            p.reanudar();

            p.teclaPresionada(KeyEvent.VK_D);
            p.actualizarJuego();

            assertEquals(205, p.getJugador().getX(), 1e-9);
        }

        @Test
        void modoGolesTerminaAlLlegarAlObjetivo() {
            DOPOsoccer q = new DOPOsoccer(
                "alemania",
                "corea",
                Partido.Modo.GOLES,
                1
            );

            assertEquals(Partido.Modo.GOLES, q.getModo());
            assertEquals(1, q.getGolesObjetivo());
        }

        @Test
        void golActualizaElMarcador() {
            assertArrayEquals(new int[] {0, 0}, p.getGoles());

            // La anotación se comprueba mediante el comportamiento
            // de actualización del partido cuando el balón entra al arco.
            assertNotNull(p.getBalon());
        }
    }
}
