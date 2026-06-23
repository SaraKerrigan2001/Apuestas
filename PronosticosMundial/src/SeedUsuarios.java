/**
 * Crea usuarios de prueba en la BD (ejecutar una sola vez).
 *
 * Compilar y ejecutar:
 *   javac -cp "lib\mysql-connector-j-8.3.0.jar;bin" -d bin src\SeedUsuarios.java
 *   java  -cp "bin;lib\mysql-connector-j-8.3.0.jar" SeedUsuarios
 */
public class SeedUsuarios {

    public static void main(String[] args) {
        UsuarioDAO dao = new UsuarioDAOImpl();

        crearSiNoExiste(dao, "Admin BetCentral",  "admin@mundial.com",   "admin123",   true);
        crearSiNoExiste(dao, "Apostador Demo",    "jugador@mundial.com", "jugador123", false);

        System.out.println();
        System.out.println("=== Credenciales de prueba ===");
        System.out.println("ADMIN (pestaña Admin):");
        System.out.println("  Email:    admin@mundial.com");
        System.out.println("  Password: admin123");
        System.out.println();
        System.out.println("APOSTADOR (pestaña Login):");
        System.out.println("  Email:    jugador@mundial.com");
        System.out.println("  Password: jugador123");
        System.out.println();
        System.out.println("Admin de respaldo (hardcodeado, no requiere BD):");
        System.out.println("  Email:    admin@betcentral.com");
        System.out.println("  Password: admin");
    }

    private static void crearSiNoExiste(UsuarioDAO dao, String nombre, String email,
                                        String pass, boolean esAdmin) {
        if (dao.emailExiste(email)) {
            System.out.println("[OK] Ya existe: " + email);
            return;
        }
        boolean ok = dao.registrar(nombre, email, pass, esAdmin);
        System.out.println(ok ? "[+] Creado: " + email : "[!] Error al crear: " + email);
    }
}
