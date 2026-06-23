import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Singleton para gestionar la conexión a la base de datos.
 * Centraliza la lectura de config.properties.
 */
public class ConexionDB {

    private static ConexionDB instancia;
    private final String url;
    private final String user;
    private final String pass;

    private ConexionDB() {
        Properties props = new Properties();

        String binPath = "";
        try {
            binPath = new File(
                ConexionDB.class.getProtectionDomain()
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

        boolean cargado = false;
        for (File f : candidatos) {
            if (f.exists()) {
                try (FileInputStream fis = new FileInputStream(f)) {
                    props.load(fis);
                    cargado = true;
                    System.out.println("[ConexionDB] Config cargado desde: " + f.getAbsolutePath());
                    break;
                } catch (IOException ignored) {}
            }
        }
        
        if (!cargado) {
            System.err.println("[ConexionDB] config.properties no encontrado. Usando valores por defecto.");
        }

        this.url  = props.getProperty("db.url",
            "jdbc:mysql://localhost:3306/mundial_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        this.user = props.getProperty("db.user", "root");
        this.pass = props.getProperty("db.pass", "");
    }

    public static synchronized ConexionDB getInstancia() {
        if (instancia == null) {
            instancia = new ConexionDB();
        }
        return instancia;
    }

    public Connection getConexion() throws SQLException {
        return DriverManager.getConnection(url, user, pass);
    }
    
    public String getUrl() { return url; }
    public String getUser() { return user; }
    public String getPass() { return pass; }
}
