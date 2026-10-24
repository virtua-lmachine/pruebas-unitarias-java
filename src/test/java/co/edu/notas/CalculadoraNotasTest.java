package co.edu.notas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class CalculadoraNotasTest {
    private CalculadoraNotas calculadora;

    @BeforeEach
    void setup() {
        calculadora = new CalculadoraNotas();
    }

    // ==========================================
    // estaAprobado(double notaFinal)
    // ==========================================

    @Test
    @DisplayName("Caso límite: exactamente 3.0 aprueba")
    void estaAprobado_notaExactamenteTres_retornaTrue() {
        assertTrue(calculadora.estaAprobado(3.0));
    }

    @Test
    @DisplayName("Caso límite: 2.9 no aprueba (un lado de la frontera)")
    void estaAprobado_notaDosPuntoNueve_retornaFalse() {
        assertFalse(calculadora.estaAprobado(2.9));
    }

    @Test
    @DisplayName("Caso límite: 5.0 aprueba (máximo válido)")
    void estaAprobado_notaMaxima_retornaTrue() {
        assertTrue(calculadora.estaAprobado(5.0));
    }

    @Test
    @DisplayName("Caso límite: 0.0 no aprueba (mínimo válido)")
    void estaAprobado_notaMinima_retornaFalse() {
        assertFalse(calculadora.estaAprobado(0.0));
    }

    @Test
    @DisplayName("Nota mayor a 5.0 lanza IllegalArgumentException")
    void estaAprobado_notaMayorACinco_lanzaExcepcion() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> calculadora.estaAprobado(5.1));
        assertTrue(ex.getMessage().contains("entre 0.0 y 5.0"));
    }

    @Test
    @DisplayName("Nota menor a 0.0 lanza IllegalArgumentException")
    void estaAprobado_notaMenorACero_lanzaExcepcion() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> calculadora.estaAprobado(-0.1));
        assertTrue(ex.getMessage().contains("entre 0.0 y 5.0"));
    }

    // ==========================================
    // calcularNotaFinal(seguimiento, parcial, proyecto)
    // ==========================================

    @Test
    @DisplayName("Cálculo correcto con valores típicos (30% + 30% + 40%)")
    void calcularNotaFinal_valoresTipicos_retornaPromedioPonderado() {
        double resultado = calculadora.calcularNotaFinal(4.0, 3.0, 5.0);
        assertEquals(4.1, resultado, 0.01);
    }

    @Test
    @DisplayName("Caso límite: todas las notas en 0.0")
    void calcularNotaFinal_todasCero_retornaCero() {
        assertEquals(0.0, calculadora.calcularNotaFinal(0.0, 0.0, 0.0), 0.01);
    }

    @Test
    @DisplayName("Caso límite: todas las notas en 5.0")
    void calcularNotaFinal_todasCinco_retornaCinco() {
        assertEquals(5.0, calculadora.calcularNotaFinal(5.0, 5.0, 5.0), 0.01);
    }

    @Test
    @DisplayName("Redondeo a una décima: 3.05 -> 3.1")
    void calcularNotaFinal_redondeoUnaDecima_retornaRedondeado() {
        double resultado = calculadora.calcularNotaFinal(3.0, 3.0, 3.1666666666666665);
        assertEquals(3.1, resultado, 0.01);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.1, -1.0, 5.1, 6.0})
    @DisplayName("Seguimiento fuera de rango lanza excepción")
    void calcularNotaFinal_seguimientoInvalido_lanzaExcepcion(double valorInvalido) {
        assertThrows(IllegalArgumentException.class,
                () -> calculadora.calcularNotaFinal(valorInvalido, 3.0, 3.0));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.1, -1.0, 5.1, 6.0})
    @DisplayName("Parcial fuera de rango lanza excepción")
    void calcularNotaFinal_parcialInvalido_lanzaExcepcion(double valorInvalido) {
        assertThrows(IllegalArgumentException.class,
                () -> calculadora.calcularNotaFinal(3.0, valorInvalido, 3.0));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.1, -1.0, 5.1, 6.0})
    @DisplayName("Proyecto fuera de rango lanza excepción")
    void calcularNotaFinal_proyectoInvalido_lanzaExcepcion(double valorInvalido) {
        assertThrows(IllegalArgumentException.class,
                () -> calculadora.calcularNotaFinal(3.0, 3.0, valorInvalido));
    }

    // ==========================================
    // desempeno(double notaFinal)
    // ==========================================

    @Test
    @DisplayName("Desempeño Superior: 4.6 (frontera inferior)")
    void desempeno_notaCuatroPuntoSeis_retornaSuperior() {
        assertEquals("Superior", calculadora.desempeno(4.6));
    }

    @Test
    @DisplayName("Desempeño Superior: 5.0 (máximo)")
    void desempeno_notaCinco_retornaSuperior() {
        assertEquals("Superior", calculadora.desempeno(5.0));
    }

    @Test
    @DisplayName("Desempeño Alto: 4.0 (frontera inferior)")
    void desempeno_notaCuatro_retornoAlto() {
        assertEquals("Alto", calculadora.desempeno(4.0));
    }

    @Test
    @DisplayName("Desempeño Alto: 4.5 (dentro del rango)")
    void desempeno_notaCuatroPuntoCinco_retornaAlto() {
        assertEquals("Alto", calculadora.desempeno(4.5));
    }

    @Test
    @DisplayName("Caso límite: 3.9 es Básico (un lado de frontera Alto)")
    void desempeno_notaTresPuntoNueve_retornaBasico() {
        assertEquals("Básico", calculadora.desempeno(3.9));
    }

    @Test
    @DisplayName("Desempeño Básico: 3.0 (frontera inferior)")
    void desempeno_notaTres_retornaBasico() {
        assertEquals("Básico", calculadora.desempeno(3.0));
    }

    @Test
    @DisplayName("Desempeño Básico: 3.5 (dentro del rango)")
    void desempeno_notaTresPuntoCinco_retornaBasico() {
        assertEquals("Básico", calculadora.desempeno(3.5));
    }

    @Test
    @DisplayName("Caso límite: 2.9 es Bajo (un lado de frontera Básico)")
    void desempeno_notaDosPuntoNueve_retornaBajo() {
        assertEquals("Bajo", calculadora.desempeno(2.9));
    }

    @Test
    @DisplayName("Desempeño Bajo: 0.0 (mínimo)")
    void desempeno_notaCero_retornaBajo() {
        assertEquals("Bajo", calculadora.desempeno(0.0));
    }

    @Test
    @DisplayName("Nota mayor a 5.0 en desempeño lanza IllegalArgumentException")
    void desempeno_notaMayorACinco_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> calculadora.desempeno(5.1));
    }

    @Test
    @DisplayName("Nota menor a 0.0 en desempeño lanza IllegalArgumentException")
    void desempeno_notaMenorACero_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> calculadora.desempeno(-0.1));
    }
}
