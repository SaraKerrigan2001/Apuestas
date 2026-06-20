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

    private final String url;
    private final String user;
    private final String pass;

    // ---------------------------------------------------------------
    // Constructor: lee credenciales desde config.properties
    // ---------------------------------------------------------------
    public ApuestaDAOImpl() {
        Properties props = new Properties();

        // Obtener ruta del .class para buscar config.properties relativo a él
        String binPath = "";
        try {
            binPath = new File(
                ApuestaDAOImpl.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()
            ).getAbsolutePath();
        } catch (Exception ignored) {}

        List<File> candidatos = new ArrayList<>();
        if (!binPath.isEmpty()) {
            File binDir = new File(binPath);
            candidatos.add(new File(binDir, "config.properties"));
            candidatos.add(new File(binDir.getParent(), "config.properties"));
        }
        candidatos.add(new File("config.properties"));
        candidatos.add(new File("C:/java/PronosticosMundial/config.properties"));

        for (File f : candidatos) {
            if (f.exists()) {
                try (FileInputStream fis = new FileInputStream(f)) {
                    props.load(fis);
                    break;
                } catch (IOException ignored) {}
            }
        }

        this.url  = props.getProperty("db.url",
            "jdbc:mysql://localhost:3306/mundial_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        this.user = props.getProperty("db.user", "root");
        this.pass = props.getProperty("db.pass", "");
    }

    public String getUrl()  { return url;  }
    public String getUser() { return user; }
    public String getPass() { return pass; }

    private Connection getConexion() throws SQLException {
        return DriverManager.getConnection(url, user, pass);
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
