import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class PronosticoDAO {

    // ---------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------
    public PronosticoDAO() {
    }

    // ---------------------------------------------------------------
    // Helper: abre una conexión
    // ---------------------------------------------------------------
    private Connection getConexion() throws SQLException {
        return ConexionDB.getInstancia().getConexion();
    }

    // ---------------------------------------------------------------
    // Helper privado: construye un Pronostico desde un ResultSet
    // ---------------------------------------------------------------
    private Pronostico mapearFila(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("fecha_registro");
        LocalDateTime fecha = ts != null ? ts.toLocalDateTime() : null;

        // acerto puede ser NULL en BD → Boolean nullable
        Boolean acerto = null;
        Object acertoDB = rs.getObject("acerto");
        if (acertoDB != null) acerto = ((Number) acertoDB).intValue() == 1;

        return new Pronostico(
            rs.getInt("id"),
            rs.getString("nombre_jugador"),
            rs.getString("equipo_local"),
            rs.getString("equipo_visitante"),
            rs.getInt("goles_local"),
            rs.getInt("goles_visitante"),
            rs.getString("resultado_real"),
            acerto,
            fecha
        );
    }

    // ---------------------------------------------------------------
    // INSERT: guarda un pronóstico nuevo, asigna el id generado
    // ---------------------------------------------------------------
    public boolean guardarPronostico(Pronostico p) {
        String sql = "INSERT INTO pronosticos "
                   + "(nombre_jugador, equipo_local, equipo_visitante, goles_local, goles_visitante) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getJugador());
            ps.setString(2, p.getEquipoLocal());
            ps.setString(3, p.getEquipoVisitante());
            ps.setInt(4, p.getGolesLocal());
            ps.setInt(5, p.getGolesVisitante());

            int filas = ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) p.setId(rs.getInt(1));
            }
            return filas > 0;

        } catch (SQLException e) {
            System.err.println("Error al guardar: " + e.getMessage());
            return false;
        }
    }

    // ---------------------------------------------------------------
    // SELECT ALL: todos los pronósticos con fecha y resultado
    // ---------------------------------------------------------------
    public List<Pronostico> listarTodos() {
        List<Pronostico> lista = new ArrayList<>();
        String sql = "SELECT id, nombre_jugador, equipo_local, equipo_visitante, "
                   + "goles_local, goles_visitante, resultado_real, acerto, fecha_registro "
                   + "FROM pronosticos ORDER BY fecha_registro DESC";

        try (Connection conn = getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapearFila(rs));

        } catch (SQLException e) {
            System.err.println("Error al listar: " + e.getMessage());
        }
        return lista;
    }

    // ---------------------------------------------------------------
    // SELECT por jugador (búsqueda parcial)
    // ---------------------------------------------------------------
    public List<Pronostico> buscarPorJugador(String nombre) {
        List<Pronostico> lista = new ArrayList<>();
        String sql = "SELECT id, nombre_jugador, equipo_local, equipo_visitante, "
                   + "goles_local, goles_visitante, resultado_real, acerto, fecha_registro "
                   + "FROM pronosticos WHERE nombre_jugador LIKE ? ORDER BY fecha_registro DESC";

        try (Connection conn = getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + nombre + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearFila(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar: " + e.getMessage());
        }
        return lista;
    }

    // ---------------------------------------------------------------
    // SELECT todos los pronósticos de un jugador exacto
    // (para VentanaHistorialJugador)
    // ---------------------------------------------------------------
    public List<Pronostico> listarPorJugador(String jugador) {
        List<Pronostico> lista = new ArrayList<>();
        String sql = "SELECT id, nombre_jugador, equipo_local, equipo_visitante, "
                   + "goles_local, goles_visitante, resultado_real, acerto, fecha_registro "
                   + "FROM pronosticos WHERE nombre_jugador = ? ORDER BY fecha_registro DESC";

        try (Connection conn = getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, jugador);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearFila(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar por jugador: " + e.getMessage());
        }
        return lista;
    }

    // ---------------------------------------------------------------
    // SELECT por rango de fechas
    // ---------------------------------------------------------------
    public List<Pronostico> listarPorFechas(LocalDateTime desde, LocalDateTime hasta) {
        List<Pronostico> lista = new ArrayList<>();
        String sql = "SELECT id, nombre_jugador, equipo_local, equipo_visitante, "
                   + "goles_local, goles_visitante, resultado_real, acerto, fecha_registro "
                   + "FROM pronosticos WHERE fecha_registro BETWEEN ? AND ? "
                   + "ORDER BY fecha_registro DESC";

        try (Connection conn = getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(desde));
            ps.setTimestamp(2, Timestamp.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearFila(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar por fechas: " + e.getMessage());
        }
        return lista;
    }

    // ---------------------------------------------------------------
    // UPDATE: actualiza un pronóstico existente por id
    // ---------------------------------------------------------------
    public boolean actualizar(Pronostico p) {
        String sql = "UPDATE pronosticos SET nombre_jugador=?, equipo_local=?, "
                   + "equipo_visitante=?, goles_local=?, goles_visitante=? WHERE id=?";

        try (Connection conn = getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getJugador());
            ps.setString(2, p.getEquipoLocal());
            ps.setString(3, p.getEquipoVisitante());
            ps.setInt(4, p.getGolesLocal());
            ps.setInt(5, p.getGolesVisitante());
            ps.setInt(6, p.getId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar: " + e.getMessage());
            return false;
        }
    }

    // ---------------------------------------------------------------
    // UPDATE: registra el resultado real de un partido y marca aciertos
    // Recibe el marcador real (ej. "2-1") y actualiza TODOS los
    // pronósticos pendientes de ese partido (local vs visitante).
    // ---------------------------------------------------------------
    public int registrarResultadoReal(String equipoLocal, String equipoVisitante,
                                       int golesRealLocal, int golesRealVisitante) {
        String resultadoReal = golesRealLocal + "-" + golesRealVisitante;

        // Calcular qué resultado corresponde
        String resultadoGanador;
        if (golesRealLocal > golesRealVisitante)       resultadoGanador = "Local";
        else if (golesRealLocal < golesRealVisitante)  resultadoGanador = "Visitante";
        else                                           resultadoGanador = "Empate";

        String sql = "UPDATE pronosticos SET resultado_real = ?, "
                   + "acerto = CASE "
                   + "  WHEN goles_local = ? AND goles_visitante = ? THEN 1 "  // marcador exacto
                   + "  WHEN (goles_local > goles_visitante AND ? = 'Local') THEN 1 "
                   + "  WHEN (goles_local < goles_visitante AND ? = 'Visitante') THEN 1 "
                   + "  WHEN (goles_local = goles_visitante AND ? = 'Empate') THEN 1 "
                   + "  ELSE 0 END "
                   + "WHERE equipo_local = ? AND equipo_visitante = ? AND acerto IS NULL";

        try (Connection conn = getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, resultadoReal);
            ps.setInt(2, golesRealLocal);
            ps.setInt(3, golesRealVisitante);
            ps.setString(4, resultadoGanador);
            ps.setString(5, resultadoGanador);
            ps.setString(6, resultadoGanador);
            ps.setString(7, equipoLocal);
            ps.setString(8, equipoVisitante);

            return ps.executeUpdate(); // Retorna cuántos pronósticos fueron actualizados

        } catch (SQLException e) {
            System.err.println("Error al registrar resultado real: " + e.getMessage());
            return -1;
        }
    }

    // ---------------------------------------------------------------
    // DELETE: elimina por id
    // ---------------------------------------------------------------
    public boolean eliminar(int id) {
        String sql = "DELETE FROM pronosticos WHERE id = ?";
        try (Connection conn = getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar: " + e.getMessage());
            return false;
        }
    }

    // ---------------------------------------------------------------
    // COUNT: total de pronósticos
    // ---------------------------------------------------------------
    public int contarPronosticos() {
        String sql = "SELECT COUNT(*) FROM pronosticos";
        try (Connection conn = getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("Error al contar: " + e.getMessage());
        }
        return 0;
    }

    // ---------------------------------------------------------------
    // VALIDAR DUPLICADO: ¿ya existe ese pronóstico para ese jugador?
    // ---------------------------------------------------------------
    public boolean existePronostico(String jugador, String local, String visitante) {
        String sql = "SELECT COUNT(*) FROM pronosticos "
                   + "WHERE nombre_jugador=? AND equipo_local=? AND equipo_visitante=?";
        try (Connection conn = getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, jugador);
            ps.setString(2, local);
            ps.setString(3, visitante);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error al validar duplicado: " + e.getMessage());
        }
        return false;
    }

    // ---------------------------------------------------------------
    // RANKING: lista de jugadores ordenados por nº de pronósticos
    // Devuelve lista de Map con claves: jugador, total, aciertos,
    // fallos, pendientes, porcentaje
    // ---------------------------------------------------------------
    public List<Map<String, String>> listarRanking() {
        List<Map<String, String>> ranking = new ArrayList<>();
        String sql = "SELECT nombre_jugador, "
                   + "COUNT(*) AS total, "
                   + "SUM(CASE WHEN acerto = 1 THEN 1 ELSE 0 END) AS aciertos, "
                   + "SUM(CASE WHEN acerto = 0 THEN 1 ELSE 0 END) AS fallos, "
                   + "SUM(CASE WHEN acerto IS NULL THEN 1 ELSE 0 END) AS pendientes "
                   + "FROM pronosticos GROUP BY nombre_jugador ORDER BY total DESC";

        try (Connection conn = getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, String> fila = new HashMap<>();
                int total     = rs.getInt("total");
                int aciertos  = rs.getInt("aciertos");
                int fallos    = rs.getInt("fallos");
                int pendientes = rs.getInt("pendientes");
                int evaluados = aciertos + fallos;
                String pct = evaluados > 0
                        ? String.format("%.1f%%", (aciertos * 100.0 / evaluados))
                        : "N/A";

                fila.put("jugador",    rs.getString("nombre_jugador"));
                fila.put("total",      String.valueOf(total));
                fila.put("aciertos",   String.valueOf(aciertos));
                fila.put("fallos",     String.valueOf(fallos));
                fila.put("pendientes", String.valueOf(pendientes));
                fila.put("porcentaje", pct);
                ranking.add(fila);
            }

        } catch (SQLException e) {
            System.err.println("Error al listar ranking: " + e.getMessage());
        }
        return ranking;
    }

    // ---------------------------------------------------------------
    // ESTADÍSTICAS PERSONALES de un jugador
    // Claves: total, aciertos, fallos, pendientes, porcentaje,
    //         marcador_favorito, resultado_predicho_frecuente
    // ---------------------------------------------------------------
    public Map<String, String> listarEstadisticasJugador(String jugador) {
        Map<String, String> stats = new HashMap<>();

        String sqlGeneral = "SELECT COUNT(*) AS total, "
            + "SUM(CASE WHEN acerto=1 THEN 1 ELSE 0 END) AS aciertos, "
            + "SUM(CASE WHEN acerto=0 THEN 1 ELSE 0 END) AS fallos, "
            + "SUM(CASE WHEN acerto IS NULL THEN 1 ELSE 0 END) AS pendientes "
            + "FROM pronosticos WHERE nombre_jugador=?";

        String sqlMarcador = "SELECT CONCAT(goles_local,'-',goles_visitante) AS marcador, "
            + "COUNT(*) AS veces FROM pronosticos WHERE nombre_jugador=? "
            + "GROUP BY marcador ORDER BY veces DESC LIMIT 1";

        String sqlResultado = "SELECT "
            + "CASE WHEN goles_local > goles_visitante THEN 'Local' "
            + "     WHEN goles_local < goles_visitante THEN 'Visitante' "
            + "     ELSE 'Empate' END AS resultado, "
            + "COUNT(*) AS veces FROM pronosticos WHERE nombre_jugador=? "
            + "GROUP BY resultado ORDER BY veces DESC LIMIT 1";

        try (Connection conn = getConexion()) {

            try (PreparedStatement ps = conn.prepareStatement(sqlGeneral)) {
                ps.setString(1, jugador);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int total    = rs.getInt("total");
                        int aciertos = rs.getInt("aciertos");
                        int fallos   = rs.getInt("fallos");
                        int pend     = rs.getInt("pendientes");
                        int evaluados = aciertos + fallos;
                        String pct = evaluados > 0
                                ? String.format("%.1f%%", aciertos * 100.0 / evaluados)
                                : "Sin evaluar";
                        stats.put("total",      String.valueOf(total));
                        stats.put("aciertos",   String.valueOf(aciertos));
                        stats.put("fallos",     String.valueOf(fallos));
                        stats.put("pendientes", String.valueOf(pend));
                        stats.put("porcentaje", pct);
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlMarcador)) {
                ps.setString(1, jugador);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next())
                        stats.put("marcador_favorito",
                                rs.getString("marcador") + " (" + rs.getInt("veces") + " veces)");
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlResultado)) {
                ps.setString(1, jugador);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next())
                        stats.put("resultado_frecuente",
                                rs.getString("resultado") + " (" + rs.getInt("veces") + " veces)");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error en estadísticas del jugador: " + e.getMessage());
        }
        return stats;
    }

    // ---------------------------------------------------------------
    // ESTADÍSTICAS GLOBALES para VentanaEstadisticas
    // ---------------------------------------------------------------
    public Map<String, String> listarEstadisticas() {
        Map<String, String> stats = new HashMap<>();

        String sqlRes = "SELECT COUNT(*) AS total, "
            + "SUM(CASE WHEN goles_local > goles_visitante THEN 1 ELSE 0 END) AS victorias_local, "
            + "SUM(CASE WHEN goles_local = goles_visitante THEN 1 ELSE 0 END) AS empates, "
            + "SUM(CASE WHEN goles_local < goles_visitante THEN 1 ELSE 0 END) AS victorias_visitante "
            + "FROM pronosticos";

        String sqlMarcador = "SELECT CONCAT(goles_local,'-',goles_visitante) AS marcador, "
            + "COUNT(*) AS veces FROM pronosticos "
            + "GROUP BY marcador ORDER BY veces DESC LIMIT 1";

        String sqlJugador = "SELECT nombre_jugador, COUNT(*) AS total FROM pronosticos "
            + "GROUP BY nombre_jugador ORDER BY total DESC LIMIT 1";

        try (Connection conn = getConexion()) {

            try (PreparedStatement ps = conn.prepareStatement(sqlRes);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.put("total",               String.valueOf(rs.getInt("total")));
                    stats.put("victorias_local",     String.valueOf(rs.getInt("victorias_local")));
                    stats.put("empates",             String.valueOf(rs.getInt("empates")));
                    stats.put("victorias_visitante", String.valueOf(rs.getInt("victorias_visitante")));
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlMarcador);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    stats.put("marcador_popular",
                            rs.getString("marcador") + " (" + rs.getInt("veces") + " veces)");
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlJugador);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    stats.put("jugador_top",
                            rs.getString("nombre_jugador") + " (" + rs.getInt("total") + " pronósticos)");
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener estadísticas: " + e.getMessage());
        }
        return stats;
    }
}
