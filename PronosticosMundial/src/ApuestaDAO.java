import java.util.List;

/**
 * Interfaz DAO para las apuestas del simulador.
 * Define el contrato de persistencia — separa la UI de la BD.
 * Principio: programar hacia la interfaz, no hacia la implementación.
 */
public interface ApuestaDAO {

    /**
     * Registra una nueva apuesta en la base de datos.
     * @param apuesta objeto con todos los datos de la apuesta
     * @return true si se guardó con éxito, false si hubo error
     */
    boolean registrarApuesta(ApuestaModel apuesta);

    /**
     * Actualiza una apuesta existente en la base de datos.
     * @param apuesta objeto con los datos modificados
     * @return true si se actualizó con éxito, false si hubo error
     */
    boolean actualizarApuesta(ApuestaModel apuesta);

    /**
     * Devuelve todas las apuestas registradas, ordenadas por fecha descendente.
     */
    List<ApuestaModel> listarApuestas();

    /**
     * Busca apuestas de un apostador específico.
     * @param nombre nombre del apostador (búsqueda parcial)
     */
    List<ApuestaModel> buscarPorApostador(String nombre);

    /**
     * Elimina una apuesta por su id.
     * @return true si se eliminó, false si no existía o hubo error
     */
    boolean eliminarApuesta(int id);

    /**
     * Devuelve el total de apuestas registradas.
     */
    int contarApuestas();

    /**
     * Devuelve la fecha y hora programada del partido.
     * @param equipoLocal nombre del equipo local
     * @param equipoVisitante nombre del equipo visitante
     * @return LocalDateTime o null si no existe
     */
    java.time.LocalDateTime getFechaHoraPartido(String equipoLocal, String equipoVisitante);
}
