import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDashboardServer {

    public static String DB_URL  = "jdbc:mysql://localhost:3306/mundial_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    public static String DB_USER = "root";
    public static String DB_PASS = "";

    public static class DashboardStats {
        public int usuarios = 0;
        public int equipos = 0;
        public int partidos = 0;
        public int pronosticos = 0;
        
        public int totalApuestas = 0;
        public double totalMonto = 0;
        public int ganadas = 0;
        public int perdidas = 0;
        public int pendientes = 0;
        
        public int pAciertos = 0;
        public int pFallos = 0;
        public int pPendientes = 0;
        
        public List<Map<String, Object>> betsByGroup = new ArrayList<>();
        public List<Map<String, Object>> topMarkets = new ArrayList<>();
        public List<Map<String, Object>> liveTx = new ArrayList<>();
    }

    // Ya no iniciamos el servidor web
    public static void startServer(int port) {
        System.out.println("[AdminDashboard] Servidor web desactivado. Usando modo nativo.");
    }

    public static void stopServer() {
        // Nada que detener
    }

    public static DashboardStats getStats() {
        DashboardStats stats = new DashboardStats();
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {}

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement st = conn.createStatement()) {

            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM usuarios");
            if (rs.next()) stats.usuarios = rs.getInt(1);

            rs = st.executeQuery("SELECT COUNT(*) FROM equipos");
            if (rs.next()) stats.equipos = rs.getInt(1);

            rs = st.executeQuery("SELECT COUNT(*) FROM partidos");
            if (rs.next()) stats.partidos = rs.getInt(1);

            rs = st.executeQuery("SELECT COUNT(*) FROM pronosticos");
            if (rs.next()) stats.pronosticos = rs.getInt(1);

            try {
                rs = st.executeQuery("SELECT COUNT(*), COALESCE(SUM(monto),0), " +
                    "COALESCE(SUM(acerto=1),0), COALESCE(SUM(acerto=0),0), " +
                    "SUM(CASE WHEN acerto IS NULL THEN 1 ELSE 0 END) FROM apuestas");
                if (rs.next()) {
                    stats.totalApuestas = rs.getInt(1);
                    stats.totalMonto = rs.getDouble(2);
                    stats.ganadas = rs.getInt(3);
                    stats.perdidas = rs.getInt(4);
                    stats.pendientes = rs.getInt(5);
                }
            } catch (SQLException ignored) { }

            rs = st.executeQuery("SELECT grupo, COUNT(*) AS cnt FROM partidos GROUP BY grupo ORDER BY grupo");
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("label", rs.getString("grupo"));
                map.put("value", rs.getInt("cnt"));
                stats.betsByGroup.add(map);
            }

            rs = st.executeQuery(
                "SELECT equipo_local, equipo_visitante, " +
                "(cuota_local + cuota_empate + cuota_visitante) AS total_cuotas " +
                "FROM partidos ORDER BY total_cuotas DESC LIMIT 5");
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("label", rs.getString("equipo_local") + " vs " + rs.getString("equipo_visitante"));
                map.put("value", rs.getDouble("total_cuotas"));
                stats.topMarkets.add(map);
            }

            rs = st.executeQuery(
                "SELECT id, nombre_jugador, equipo_local, equipo_visitante, " +
                "goles_local, goles_visitante, resultado_real, acerto, fecha_registro " +
                "FROM pronosticos ORDER BY fecha_registro DESC LIMIT 20");
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", "#PRO-" + rs.getInt("id"));
                map.put("mercado", rs.getString("equipo_local") + " vs " + rs.getString("equipo_visitante"));
                map.put("usuario", rs.getString("nombre_jugador"));
                map.put("marcador", rs.getInt("goles_local") + "-" + rs.getInt("goles_visitante"));
                
                Object acerto = rs.getObject("acerto");
                if (acerto == null) {
                    map.put("status", "PENDING");
                } else if (rs.getInt("acerto") == 1) {
                    map.put("status", "SETTLED");
                } else {
                    map.put("status", "LOSS");
                }
                stats.liveTx.add(map);
            }

            try {
                rs = st.executeQuery(
                    "SELECT COALESCE(SUM(acerto=1),0) AS a, COALESCE(SUM(acerto=0),0) AS f, " +
                    "SUM(CASE WHEN acerto IS NULL THEN 1 ELSE 0 END) AS p FROM pronosticos");
                if (rs.next()) {
                    stats.pAciertos = rs.getInt("a");
                    stats.pFallos = rs.getInt("f");
                    stats.pPendientes = rs.getInt("p");
                }
            } catch (SQLException ignored) {}

        } catch (SQLException e) {
            System.err.println("Error obteniendo stats: " + e.getMessage());
        }

        return stats;
    }

    public static List<Object[]> getUsuariosList() {
        List<Object[]> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, nombre_completo, email, saldo, activo, fecha_registro FROM usuarios WHERE email != 'josevera@gmail.com' ORDER BY id DESC")) {
            while(rs.next()) {
                list.add(new Object[]{
                    rs.getInt("id"), 
                    rs.getString("nombre_completo"), 
                    rs.getString("email"),
                    String.format("$%.2f", rs.getDouble("saldo")), 
                    rs.getBoolean("activo") ? "ACTIVO" : "INACTIVO", 
                    rs.getTimestamp("fecha_registro")
                });
            }
        } catch(Exception e){}
        return list;
    }

    public static List<Object[]> getPartidosList() {
        List<Object[]> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, grupo, equipo_local, equipo_visitante, cuota_local, cuota_empate, cuota_visitante, jugado FROM partidos ORDER BY id ASC")) {
            while(rs.next()) {
                list.add(new Object[]{
                    rs.getInt("id"), 
                    rs.getString("grupo"), 
                    rs.getString("equipo_local") + " vs " + rs.getString("equipo_visitante"),
                    rs.getDouble("cuota_local") + " | " + rs.getDouble("cuota_empate") + " | " + rs.getDouble("cuota_visitante"),
                    rs.getBoolean("jugado") ? "TERMINADO" : "PENDIENTE"
                });
            }
        } catch(Exception e){}
        return list;
    }
}
