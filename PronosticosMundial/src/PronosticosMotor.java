import java.util.List;
import java.util.Map;

/**
 * Capa de lógica de negocio del sistema de pronósticos.
 * No conoce la interfaz gráfica ni la base de datos directamente.
 * Recibe datos del Controlador y devuelve resultados procesados.
 */
public class PronosticosMotor {

    private final PronosticoDAO dao;

    public PronosticosMotor(PronosticoDAO dao) {
        this.dao = dao;
    }

    // ---------------------------------------------------------------
    // CRUD de pronósticos
    // ---------------------------------------------------------------

    /** Valida y guarda un nuevo pronóstico. Devuelve mensaje de resultado. */
    public String guardar(String jugador, String local, String visitante,
                          int golesLocal, int golesVisitante) {
        if (jugador == null || jugador.trim().isEmpty())
            return "ERROR:Ingresa tu nombre.";
        if (local == null || visitante == null || local.equals(visitante))
            return "ERROR:Los equipos no pueden ser iguales.";

        boolean duplicado = dao.existePronostico(jugador.trim(), local, visitante);
        if (duplicado)
            return "DUPLICADO:" + jugador + " ya pronosticó " + local + " vs " + visitante;

        Pronostico p = new Pronostico(jugador.trim(), local, visitante, golesLocal, golesVisitante);
        boolean ok = dao.guardarPronostico(p);
        return ok ? "OK:" + p.getId() : "ERROR:No se pudo guardar en la base de datos.";
    }

    /** Fuerza el guardado aunque sea duplicado (el usuario confirmó). */
    public String guardarForzado(String jugador, String local, String visitante,
                                  int golesLocal, int golesVisitante) {
        if (jugador == null || jugador.trim().isEmpty())
            return "ERROR:Ingresa tu nombre.";
        if (local == null || visitante == null || local.equals(visitante))
            return "ERROR:Los equipos no pueden ser iguales.";

        Pronostico p = new Pronostico(jugador.trim(), local, visitante, golesLocal, golesVisitante);
        boolean ok = dao.guardarPronostico(p);
        return ok ? "OK:" + p.getId() : "ERROR:No se pudo guardar en la base de datos.";
    }

    /** Actualiza un pronóstico existente. Devuelve "OK" o "ERROR:...". */
    public String actualizar(int id, String jugador, String local, String visitante,
                              int golesLocal, int golesVisitante) {
        if (jugador == null || jugador.trim().isEmpty())
            return "ERROR:El nombre no puede estar vacío.";
        if (local == null || visitante == null || local.equals(visitante))
            return "ERROR:Los equipos no pueden ser iguales.";

        Pronostico p = new Pronostico(jugador.trim(), local, visitante, golesLocal, golesVisitante);
        p.setId(id);
        return dao.actualizar(p) ? "OK" : "ERROR:No se pudo actualizar.";
    }

    /** Elimina un pronóstico por id. Devuelve "OK" o "ERROR:...". */
    public String eliminar(int id) {
        return dao.eliminar(id) ? "OK" : "ERROR:No se pudo eliminar.";
    }

    // ---------------------------------------------------------------
    // Consultas
    // ---------------------------------------------------------------

    public List<Pronostico> listarTodos() {
        return dao.listarTodos();
    }

    public List<Pronostico> buscarPorNombre(String nombre) {
        return dao.buscarPorJugador(nombre);
    }

    public List<Pronostico> listarPorJugador(String jugador) {
        return dao.listarPorJugador(jugador);
    }

    public List<Pronostico> listarPorFechas(
            java.time.LocalDateTime desde, java.time.LocalDateTime hasta) {
        return dao.listarPorFechas(desde, hasta);
    }

    public int contarPronosticos() {
        return dao.contarPronosticos();
    }

    public Map<String, String> obtenerEstadisticas() {
        return dao.listarEstadisticas();
    }

    public Map<String, String> obtenerEstadisticasJugador(String jugador) {
        return dao.listarEstadisticasJugador(jugador);
    }

    public List<Map<String, String>> obtenerRanking() {
        return dao.listarRanking();
    }

    // ---------------------------------------------------------------
    // Registro de resultado real
    // ---------------------------------------------------------------

    /**
     * Registra el marcador real de un partido y actualiza los pronósticos
     * correspondientes marcando quién acertó y quién falló.
     *
     * @return "OK:N" donde N es la cantidad de pronósticos actualizados,
     *         o "ERROR:..." si hubo un fallo.
     */
    public String registrarResultadoReal(String local, String visitante,
                                          int golesLocal, int golesVisitante) {
        if (local == null || visitante == null || local.equals(visitante))
            return "ERROR:Los equipos no pueden ser iguales.";
        if (golesLocal < 0 || golesVisitante < 0)
            return "ERROR:Los goles no pueden ser negativos.";

        int actualizados = dao.registrarResultadoReal(local, visitante, golesLocal, golesVisitante);
        if (actualizados < 0) return "ERROR:No se pudo registrar el resultado.";
        return "OK:" + actualizados;
    }

    // ---------------------------------------------------------------
    // Lógica de apuestas (cálculos sin BD)
    // ---------------------------------------------------------------

    /**
     * Calcula el resultado predicho a partir de un marcador "X-Y".
     * @return "Local", "Visitante" o "Empate", o null si el formato es inválido.
     */
    public String calcularResultadoPredicho(String marcador) {
        if (marcador == null || !marcador.matches("\\d+-\\d+")) return null;
        String[] p = marcador.split("-");
        int gl = Integer.parseInt(p[0]);
        int gv = Integer.parseInt(p[1]);
        if (gl > gv) return "Local";
        if (gl < gv) return "Visitante";
        return "Empate";
    }

    /**
     * Determina si una predicción es de "alto voltaje" (5 o más goles totales).
     */
    public boolean esAltoVoltaje(String marcador) {
        if (marcador == null || !marcador.matches("\\d+-\\d+")) return false;
        String[] p = marcador.split("-");
        return (Integer.parseInt(p[0]) + Integer.parseInt(p[1])) >= 5;
    }

    /**
     * Calcula el premio potencial de una apuesta antes de simular el partido.
     * @param monto  dinero apostado
     * @param cuota  cuota correspondiente a la predicción
     * @return monto * cuota redondeado a 2 decimales
     */
    public double calcularPremioPotencial(double monto, double cuota) {
        return Math.round(monto * cuota * 100.0) / 100.0;
    }

    /**
     * Valida que el monto a apostar sea válido.
     * @return "OK" o "ERROR:..."
     */
    public String validarMonto(double monto, double saldoActual) {
        if (monto <= 0)            return "ERROR:El monto debe ser mayor que cero.";
        if (monto > saldoActual)   return "ERROR:Saldo insuficiente (disponible: $"
                                           + String.format("%.2f", saldoActual) + ").";
        return "OK";
    }
}
