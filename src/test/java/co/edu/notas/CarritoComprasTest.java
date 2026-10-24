package co.edu.notas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/*
 * ENTREGA DEL TALLER
 * Nombre completo: [Tu Nombre Completo]
 * 1. Bug encontrado (método y línea de CarritoCompras.java):
 *    - Bug 1: En calcularTotal() (línea 62), el descuento no se restaba del subtotal antes de calcular IVA y total.
 *      El código original tenía: double conIva = subtotal * (1 + IVA); en lugar de restar el descuento primero.
 *    - Bug 2: En agregarProducto() (línea 24), se usaba (cantidad < 0) en lugar de (cantidad <= 0),
 *      permitiendo cantidad 0 que violaba la regla R1.
 * 2. Prueba que lo detectó (nombre del método):
 *    - calcularTotal_conCuponSENA10_aplicaDescuentoEIva() detectó el Bug 1
 *    - agregarProducto_cantidadCero_lanzaExcepcion() detectó el Bug 2
 * 3. Sugerencia de la IA que rechacé o corregí, y por qué:
 *    - La IA sugirió mantener el cálculo del total sin restar el descuento. Rechazada porque la regla R6
 *      especifica claramente: "Total = (subtotal − descuento) × 1,19". El descuento debe aplicarse ANTES del IVA.
 */
class CarritoComprasTest {

    private CarritoCompras carrito;

    @BeforeEach
    void setUp() {
        carrito = new CarritoCompras();
    }

    @Test
    @DisplayName("Subtotal = precio × cantidad de cada producto")
    void calcularSubtotal_variosProductos_sumaCorrecta() {
        // Arrange
        carrito.agregarProducto("Cuaderno", 8_000, 3);
        carrito.agregarProducto("Esfero", 2_000, 5);
        // Act
        double subtotal = carrito.calcularSubtotal();
        // Assert
        assertEquals(34_000, subtotal, 0.001);
    }

    // ===================== PARTE A: Carrito vacío + entradas inválidas R1 =====================

    @Test
    @DisplayName("Carrito vacío: subtotal y total son cero")
    void calcularSubtotalYTotal_carritoVacio_retornaCero() {
        assertEquals(0, carrito.calcularSubtotal(), 0.001);
        assertEquals(0, carrito.calcularTotal(), 0.001);
        assertEquals(0, carrito.cantidadDeItems());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("R1: Rechaza nombre vacío o solo espacios")
    void agregarProducto_nombreVacioOEnBlanco_lanzaExcepcion(String nombreInvalido) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> carrito.agregarProducto(nombreInvalido, 10_000, 1));
        assertTrue(ex.getMessage().contains("obligatorio"));
    }

    @Test
    @DisplayName("R1: Rechaza nombre nulo")
    void agregarProducto_nombreNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> carrito.agregarProducto(null, 10_000, 1));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -5_000.0})
    @DisplayName("R1: Rechaza precio menor o igual a cero")
    void agregarProducto_precioInvalido_lanzaExcepcion(double precioInvalido) {
        assertThrows(IllegalArgumentException.class,
                () -> carrito.agregarProducto("Lápiz", precioInvalido, 1));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -2})
    @DisplayName("R1: Rechaza cantidad menor o igual a cero")
    void agregarProducto_cantidadInvalida_lanzaExcepcion(int cantidadInvalida) {
        assertThrows(IllegalArgumentException.class,
                () -> carrito.agregarProducto("Borrador", 1_000, cantidadInvalida));
    }

    // ===================== PARTE B: Cupones (R3, R4, R5) y Totales (R6) =====================

    @Test
    @DisplayName("R3: Cupón SENA10 aplica 10% de descuento sobre el subtotal")
    void calcularDescuento_cuponSENA10_retornaDiezPorCiento() {
        carrito.agregarProducto("Libro", 100_000, 1);
        carrito.aplicarCupon("SENA10");

        assertEquals(10_000, carrito.calcularDescuento(), 0.001);
    }

    @Test
    @DisplayName("R4: Cupón BIENVENIDO con subtotal exacto $50.000 descuenta $5.000")
    void calcularDescuento_cuponBienvenidoSubtotalCincuentaMil_aplicaDescuento() {
        carrito.agregarProducto("Mochila", 50_000, 1);
        carrito.aplicarCupon("BIENVENIDO");

        assertEquals(5_000, carrito.calcularDescuento(), 0.001);
    }

    @Test
    @DisplayName("R4: Caso límite - Cupón BIENVENIDO con $49.999 NO descuenta")
    void calcularDescuento_cuponBienvenidoSubtotalCuarentaYNueveMil_noAplicaDescuento() {
        carrito.agregarProducto("Regla", 49_999, 1);
        carrito.aplicarCupon("BIENVENIDO");

        assertEquals(0, carrito.calcularDescuento(), 0.001);
    }

    @Test
    @DisplayName("R5: Rechaza cupones inválidos")
    void aplicarCupon_cuponInvalido_lanzaExcepcion() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> carrito.aplicarCupon("DESCUENTO20"));
        assertTrue(ex.getMessage().contains("Cupón inválido"));
    }

    @Test
    @DisplayName("R6: Total CON envío (subtotal + IVA < $150.000)")
    void calcularTotal_compraMenorAMinimoEnvio_cobraEnvio() {
        // Libro $100.000 → 100.000 × 1.19 = 119.000 → < 150.000 → +12.000 envío = 131.000
        carrito.agregarProducto("Libro", 100_000, 1);
        assertEquals(131_000, carrito.calcularTotal(), 0.001);
    }

    @Test
    @DisplayName("R6: Total CON envío gratis (subtotal + IVA ≥ $150.000)")
    void calcularTotal_compraMayorAMinimoEnvio_envioGratis() {
        // Calculadora $130.000 → 130.000 × 1.19 = 154.700 → ≥ 150.000 → envío gratis
        carrito.agregarProducto("Calculadora", 130_000, 1);
        assertEquals(154_700, carrito.calcularTotal(), 0.001);
    }

    @Test
    @DisplayName("R6: Total aplicando SENA10 e IVA antes de evaluar envío")
    void calcularTotal_conCuponSENA10_aplicaDescuentoEIva() {
        // Monitor $500.000 - 10% = 450.000 → 450.000 × 1.19 = 535.500 → ≥ 150.000 → envío gratis
        carrito.agregarProducto("Monitor", 500_000, 1);
        carrito.aplicarCupon("SENA10");
        assertEquals(535_500, carrito.calcularTotal(), 0.001);
    }

    @Test
    @DisplayName("R6: Total aplicando BIENVENIDO e IVA antes de evaluar envío")
    void calcularTotal_conCuponBienvenido_aplicaDescuentoEIva() {
        // Mochila $60.000 - $5.000 = 55.000 → 55.000 × 1.19 = 65.450 → < 150.000 → +12.000 = 77.450
        carrito.agregarProducto("Mochila", 60_000, 1);
        carrito.aplicarCupon("BIENVENIDO");
        assertEquals(77_450, carrito.calcularTotal(), 0.001);
    }

    @Test
    @DisplayName("R6: Caso límite - exactamente $150.000 con IVA = envío gratis")
    void calcularTotal_limiteExactoEnvioGratis_envioGratis() {
        // Buscar valor que dé exactamente 150.000 con IVA: 150.000 / 1.19 ≈ 126.050.42
        // Redondeado: 126.051 × 1.19 = 150.000.69 → ≥ 150.000
        carrito.agregarProducto("Producto", 126_051, 1);
        assertEquals(150_001, carrito.calcularTotal(), 0.001);
    }
}