import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementación concreta de ApuestaDAO usando JDBC + MySQL.
 * Usa la misma estrategia de localización de config.properties
 * que PronosticoDAO para garantizar la conexión desde cualquier
 * directorio de trabajo.
 */
public class ApuestaDAOImpl implements ApuestaDAO {

    // ---------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------
    public ApuestaDAOImpl() {
    }

    public String getUrl()  { return ConexionDB.getInstancia().getUrl();  }
    public String getUser() { return ConexionDB.getInstancia().getUser(); }
    public String getPass() { return ConexionDB.getInstancia().getPass(); }

    private Connection getConexion() throws SQLException {
        return ConexionDB.getInstancia().getConexion();
    }

    // ---------------------------------------------------------------
    // Helper: mapea un ResultSet a ApuestaModel
    // ---------------------------------------------------------------
    private ApuestaModel mapear(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("fecha_registro");
        LocalDateTime fecha = ts != null ? ts.toLocalDateTime() : null;
        return new ApuestaModel(
            rs.getInt("id"),
            rs.getString("nombre_jugador"),
            rs.getString("equipo_local"),
            rs.getString("equipo_visitante"),
            rs.getInt("goles_local"),
            rs.getInt("goles_visitante"),
            fecha
        );
    }

    // ---------------------------------------------------------------
    // INSERT
    // ---------------------------------------------------------------
    @Override
    public boolean registrarApuesta(ApuestaModel a) {
        String sql = "INSERT INTO pronosticos "
                   + "(nombre_jugador, equipo_local, equipo_visitante, goles_local, goles_visitante) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, a.getNombreApostador());
            ps.setString(2, a.getEquipoLocal());
            ps.setString(3, a.getEquipoVisitante());
            ps.setInt(4, a.getGolesLocal());
            ps.setInt(5, a.getGolesVisitante());

            int filas = ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) a.setId(rs.getInt(1));
            }
            return filas > 0;

        } catch (SQLException e) {
            System.err.println("[ApuestaDAO] Error al registrar: " + e.getMessage());
            return false;
        }
    }

    // ---------------------------------------------------------------
    // UPDATE
    // ---------------------------------------------------------------
    @Override
    public boolean actualizarApuesta(ApuestaModel a) {
        String sql = "UPDATE pronosticos "
                   + "SET equipo_local = ?, equipo_visitante = ?, goles_local = ?, goles_visitante = ? "
                   + "WHERE id = ? AND nombre_jugador = ?";
        try (Connection conn = getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, a.getEquipoLocal());
            ps.setString(2, a.getEquipoVisitante());
            ps.setInt(3, a.getGolesLocal());
            ps.setInt(4, a.getGolesVisitante());
            ps.setInt(5, a.getId());
            ps.setString(6, a.getNombreApostador());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[ApuestaDAO] Error al actualizar: " + e.getMessage());
            return false;
        }
    }

    // ---------------------------------------------------------------
    // OBTENER FECHA PARTIDO
    // ---------------------------------------------------------------
    @Override
    public java.time.LocalDateTime getFechaHoraPartido(String equipoLocal, String equipoVisitante) {
        String sql = "SELECT fecha_hora FROM partidos WHERE equipo_local = ? AND equipo_visitante = ?";
        try (Connection conn = getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, equipoLocal);
            ps.setString(2, equipoVisitante);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = rs.getTimestamp("fecha_hora");
                    if (ts != null) {
                        return ts.toLocalDateTime();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[ApuestaDAO] Error al obtener fecha de partido: " + e.getMessage());
        }
        return null;
    }

    // ---------------------------------------------------------------
    // SELECT ALL
    // ---------------------------------------------------------------
    @Override
    public List<ApuestaModel> listarApuestas() {
        List<ApuestaModel> lista = new ArrayList<>();
        String sql = "SELECT id, nombre_jugador, equipo_local, equipo_visitante, "
                   + "goles_local, goles_visitante, fecha_registro "
                   + "FROM pronosticos ORDER BY fecha_registro DESC";
        try (Connection conn = getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            System.err.println("[ApuestaDAO] Error al listar: " + e.getMessage());
        }
        return lista;
    }

    // ---------------------------------------------------------------
    // SELECT por apostador (búsqueda parcial)
    // ---------------------------------------------------------------
    @Override
    public List<ApuestaModel> buscarPorApostador(String nombre) {
        List<ApuestaModel> lista = new ArrayList<>();
        String sql = "SELECT id, nombre_jugador, equipo_local, equipo_visitante, "
                   + "goles_local, goles_visitante, fecha_registro "
                   + "FROM pronosticos WHERE nombre_jugador LIKE ? ORDER BY fecha_registro DESC";
        try (Connection conn = getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + nombre + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            System.err.println("[ApuestaDAO] Error al buscar: " + e.getMessage());
        }
        return lista;
    }

    // ---------------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------------
    @Override
    public boolean eliminarApuesta(int id) {
        String sql = "DELETE FROM pronosticos WHERE id = ?";
        try (Connection conn = getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[ApuestaDAO] Error al eliminar: " + e.getMessage());
            return false;
        }
    }

    // ---------------------------------------------------------------
    // COUNT
    // ---------------------------------------------------------------
    @Override
    public int contarApuestas() {
        String sql = "SELECT COUNT(*) FROM pronosticos";
        try (Connection conn = getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("[ApuestaDAO] Error al contar: " + e.getMessage());
        }
        return 0;
    }
}
