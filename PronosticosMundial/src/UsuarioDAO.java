/**
 * Interfaz DAO para autenticación de usuarios.
 * Separa la lógica de UI de la BD.
 */
public interface UsuarioDAO {

    /**
     * Registra un nuevo usuario.
     * @return true si se registró, false si el email ya existe o hubo error
     */
    boolean registrar(String nombreCompleto, String email, String password);

    /**
     * Registra un nuevo usuario con rol de administrador o normal.
     */
    boolean registrar(String nombreCompleto, String email, String password, boolean esAdmin);

    /**
     * Verifica credenciales de login normal.
     * @return nombre completo del usuario si es válido, null si falla
     */
    String login(String email, String password);

    /**
     * Verifica credenciales de login de Administrador.
     * @return nombre completo del admin si es válido y tiene permisos, null si falla
     */
    String loginAdmin(String email, String password);

    /**
     * Verifica si un email ya está registrado.
     */
    boolean emailExiste(String email);
}
