import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.util.*;

/**
 * Implementación JDBC de UsuarioDAO.
 * Usa SHA-256 para el hash de contraseñas.
 * Lee credenciales de BD desde config.properties.
 */
public class UsuarioDAOImpl implements UsuarioDAO {

    private final String url;
    private final String user;
    private final String pass;

    public UsuarioDAOImpl() {
        Properties props = new Properties();
        String binPath = "";
        try {
            binPath = new File(
                UsuarioDAOImpl.class.getProtectionDomain()
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

    private Connection getConexion() throws SQLException {
        return DriverManager.getConnection(url, user, pass);
    }

    // ---------------------------------------------------------------
    // SHA-256 helper
    // ---------------------------------------------------------------
    private String sha256(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash)
                sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error al hashear contraseña", e);
        }
    }

    // ---------------------------------------------------------------
    // Registrar nuevo usuario
    // ---------------------------------------------------------------
    @Override
    public boolean registrar(String nombreCompleto, String email, String password) {
        if (emailExiste(email)) return false;

        String sql = "INSERT INTO usuarios (nombre_completo, email, password_hash) VALUES (?, ?, ?)";
        try (Connection conn = getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombreCompleto.trim());
            ps.setString(2, email.trim().toLowerCase());
            ps.setString(3, sha256(password));
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] Error al registrar: " + e.getMessage());
            return false;
        }
    }

    // ---------------------------------------------------------------
    // Login — devuelve nombre completo o null
    // ---------------------------------------------------------------
    @Override
    public String login(String email, String password) {
        String sql = "SELECT nombre_completo FROM usuarios " +
                     "WHERE email = ? AND password_hash = ? AND activo = 1";
        try (Connection conn = getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim().toLowerCase());
            ps.setString(2, sha256(password));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("nombre_completo");
            }

        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] Error en login: " + e.getMessage());
        }
        return null;
    }

    // ---------------------------------------------------------------
    // Verificar email duplicado
    // ---------------------------------------------------------------
    @Override
    public boolean emailExiste(String email) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE email = ?";
        try (Connection conn = getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] Error al verificar email: " + e.getMessage());
        }
        return false;
    }
}
