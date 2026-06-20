import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Modelo de datos de una Apuesta.
 * Nombre "ApuestaModel" para no colisionar con la clase interna
 * "Apuesta" que existe dentro de Apuestasmundial.java.
 */
public class ApuestaModel {

    private int           id;
    private String        nombreApostador;
    private String        equipoLocal;
    private String        equipoVisitante;
    private int           golesLocal;
    private int           golesVisitante;
    private LocalDateTime fechaRegistro;

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ---------------------------------------------------------------
    // Constructor sin id (nueva apuesta antes de guardar)
    // ---------------------------------------------------------------
    public ApuestaModel(String nombreApostador, String equipoLocal,
                        String equipoVisitante, int golesLocal, int golesVisitante) {
        this.nombreApostador = nombreApostador;
        this.equipoLocal     = equipoLocal;
        this.equipoVisitante = equipoVisitante;
        this.golesLocal      = golesLocal;
        this.golesVisitante  = golesVisitante;
    }

    // ---------------------------------------------------------------
    // Constructor completo (apuesta recuperada de BD)
    // ---------------------------------------------------------------
    public ApuestaModel(int id, String nombreApostador, String equipoLocal,
                        String equipoVisitante, int golesLocal, int golesVisitante,
                        LocalDateTime fechaRegistro) {
        this.id              = id;
        this.nombreApostador = nombreApostador;
        this.equipoLocal     = equipoLocal;
        this.equipoVisitante = equipoVisitante;
        this.golesLocal      = golesLocal;
        this.golesVisitante  = golesVisitante;
        this.fechaRegistro   = fechaRegistro;
    }

    // ---------------------------------------------------------------
    // Getters
    // ---------------------------------------------------------------
    public int           getId()               { return id; }
    public String        getNombreApostador()   { return nombreApostador; }
    public String        getEquipoLocal()       { return equipoLocal; }
    public String        getEquipoVisitante()   { return equipoVisitante; }
    public int           getGolesLocal()        { return golesLocal; }
    public int           getGolesVisitante()    { return golesVisitante; }
    public LocalDateTime getFechaRegistro()     { return fechaRegistro; }

    public String getFechaFormateada() {
        return fechaRegistro != null ? fechaRegistro.format(FMT) : "-";
    }

    // ---------------------------------------------------------------
    // Setters
    // ---------------------------------------------------------------
    public void setId(int id)                              { this.id = id; }
    public void setNombreApostador(String v)               { this.nombreApostador = v; }
    public void setEquipoLocal(String v)                   { this.equipoLocal = v; }
    public void setEquipoVisitante(String v)               { this.equipoVisitante = v; }
    public void setGolesLocal(int v)                       { this.golesLocal = v; }
    public void setGolesVisitante(int v)                   { this.golesVisitante = v; }
    public void setFechaRegistro(LocalDateTime v)          { this.fechaRegistro = v; }

    // ---------------------------------------------------------------
    // Utilidades
    // ---------------------------------------------------------------
    /** Resultado predicho: "Local", "Visitante" o "Empate" */
    public String getResultado() {
        if (golesLocal > golesVisitante) return "Local";
        if (golesLocal < golesVisitante) return "Visitante";
        return "Empate";
    }

    /** Marcador como "X-Y" */
    public String getMarcador() {
        return golesLocal + "-" + golesVisitante;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s | %s %d-%d %s (%s) | %s",
            id, nombreApostador, equipoLocal, golesLocal,
            golesVisitante, equipoVisitante, getResultado(), getFechaFormateada());
    }
}
