import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.util.*;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

/**
 * Implementación JDBC de UsuarioDAO.
 * Usa PBKDF2 con Salt para contraseñas seguras y ConexionDB.
 */
public class UsuarioDAOImpl implements UsuarioDAO {

    public UsuarioDAOImpl() {
    }

    private Connection getConexion() throws SQLException {
        return ConexionDB.getInstancia().getConexion();
    }

    // ---------------------------------------------------------------
    // PBKDF2 helper
    // ---------------------------------------------------------------
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;

    private String hashPassword(String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            
            return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error al hashear contraseña", e);
        }
    }

    private boolean verifyPassword(String password, String storedHash) {
        // Fallback for old SHA-256 (64 hex chars, no colon)
        if (!storedHash.contains(":")) {
            return oldSha256(password).equals(storedHash);
        }
        
        try {
            String[] parts = storedHash.split(":");
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] hash = Base64.getDecoder().decode(parts[1]);
            
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] testHash = skf.generateSecret(spec).getEncoded();
            
            return MessageDigest.isEqual(hash, testHash);
        } catch (Exception e) {
            return false;
        }
    }

    private String oldSha256(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
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
        return registrar(nombreCompleto, email, password, false);
    }

    @Override
    public boolean registrar(String nombreCompleto, String email, String password, boolean esAdmin) {
        if (emailExiste(email)) return false;

        String sql = "INSERT INTO usuarios (nombre_completo, email, password_hash, es_admin) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombreCompleto.trim());
            ps.setString(2, email.trim().toLowerCase());
            ps.setString(3, hashPassword(password));
            ps.setInt(4, esAdmin ? 1 : 0);
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
        String sql = "SELECT nombre_completo, password_hash FROM usuarios " +
                     "WHERE email = ? AND activo = 1";
        try (Connection conn = getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (verifyPassword(password, rs.getString("password_hash"))) {
                        return rs.getString("nombre_completo");
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] Error en login: " + e.getMessage());
        }
        return null;
    }

    @Override
    public String loginAdmin(String email, String password) {
        String sql = "SELECT nombre_completo, password_hash FROM usuarios " +
                     "WHERE email = ? AND activo = 1 AND es_admin = 1";
        try (Connection conn = getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (verifyPassword(password, rs.getString("password_hash"))) {
                        return rs.getString("nombre_completo");
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("[UsuarioDAO] Error en loginAdmin: " + e.getMessage());
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
