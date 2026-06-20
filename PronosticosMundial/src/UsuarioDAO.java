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
     * Verifica credenciales de login.
     * @return nombre completo del usuario si es válido, null si falla
     */
    String login(String email, String password);

    /**
     * Verifica si un email ya está registrado.
     */
    boolean emailExiste(String email);
}
