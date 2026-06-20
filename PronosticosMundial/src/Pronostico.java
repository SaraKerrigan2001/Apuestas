import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Pronostico {

    private int id;
    private String jugador;
    private String equipoLocal;
    private String equipoVisitante;
    private int golesLocal;
    private int golesVisitante;
    private String resultadoReal;   // ej. "2-1", null = pendiente
    private Boolean acerto;         // true/false/null (null = pendiente)
    private LocalDateTime fechaRegistro;

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Constructor sin id ni fecha (pronóstico nuevo antes de guardar)
    public Pronostico(String jugador, String equipoLocal, String equipoVisitante,
                      int golesLocal, int golesVisitante) {
        this.jugador         = jugador;
        this.equipoLocal     = equipoLocal;
        this.equipoVisitante = equipoVisitante;
        this.golesLocal      = golesLocal;
        this.golesVisitante  = golesVisitante;
    }

    // Constructor completo (pronóstico recuperado de la BD)
    public Pronostico(int id, String jugador, String equipoLocal, String equipoVisitante,
                      int golesLocal, int golesVisitante,
                      String resultadoReal, Boolean acerto, LocalDateTime fechaRegistro) {
        this.id              = id;
        this.jugador         = jugador;
        this.equipoLocal     = equipoLocal;
        this.equipoVisitante = equipoVisitante;
        this.golesLocal      = golesLocal;
        this.golesVisitante  = golesVisitante;
        this.resultadoReal   = resultadoReal;
        this.acerto          = acerto;
        this.fechaRegistro   = fechaRegistro;
    }

    // ---------------------------------------------------------------
    // Getters
    // ---------------------------------------------------------------
    public int getId()                      { return id; }
    public String getJugador()              { return jugador; }
    public String getEquipoLocal()          { return equipoLocal; }
    public String getEquipoVisitante()      { return equipoVisitante; }
    public int getGolesLocal()              { return golesLocal; }
    public int getGolesVisitante()          { return golesVisitante; }
    public String getResultadoReal()        { return resultadoReal; }
    public Boolean getAcerto()              { return acerto; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }

    /** Fecha formateada para mostrar en tabla (dd/MM/yyyy HH:mm) */
    public String getFechaFormateada() {
        return fechaRegistro != null ? fechaRegistro.format(FORMATO) : "-";
    }

    // ---------------------------------------------------------------
    // Setters
    // ---------------------------------------------------------------
    public void setId(int id)                              { this.id = id; }
    public void setJugador(String jugador)                 { this.jugador = jugador; }
    public void setEquipoLocal(String equipoLocal)         { this.equipoLocal = equipoLocal; }
    public void setEquipoVisitante(String equipoVisitante) { this.equipoVisitante = equipoVisitante; }
    public void setGolesLocal(int golesLocal)              { this.golesLocal = golesLocal; }
    public void setGolesVisitante(int golesVisitante)      { this.golesVisitante = golesVisitante; }
    public void setResultadoReal(String resultadoReal)     { this.resultadoReal = resultadoReal; }
    public void setAcerto(Boolean acerto)                  { this.acerto = acerto; }
    public void setFechaRegistro(LocalDateTime fecha)      { this.fechaRegistro = fecha; }

    // ---------------------------------------------------------------
    // Lógica de negocio
    // ---------------------------------------------------------------

    /** Resultado predicho: "Local", "Visitante" o "Empate" */
    public String getResultadoPredicho() {
        if (golesLocal > golesVisitante) return "Local";
        if (golesLocal < golesVisitante) return "Visitante";
        return "Empate";
    }

    /** Marcador predicho como "X-Y" */
    public String getMarcadorPredicho() {
        return golesLocal + "-" + golesVisitante;
    }

    /**
     * Estado legible para la columna "Acierto" en la tabla:
     * "✔ Acertó", "✘ Falló" o "⏳ Pendiente"
     */
    public String getEstadoAcierto() {
        if (acerto == null) return "⏳ Pendiente";
        return acerto ? "✔ Acertó" : "✘ Falló";
    }

    @Override
    public String toString() {
        return String.format("[%d] %s: %s %d-%d %s | Real: %s | %s | %s",
                id, jugador, equipoLocal, golesLocal, golesVisitante,
                equipoVisitante,
                resultadoReal != null ? resultadoReal : "pendiente",
                getEstadoAcierto(),
                getFechaFormateada());
    }
}
