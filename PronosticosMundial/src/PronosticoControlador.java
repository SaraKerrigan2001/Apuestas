import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Controlador MVC del sistema de pronósticos.
 * Conecta PronosticoVista (UI) con PronosticosMotor (lógica).
 * Sigue el mismo patrón que CalculadoraControlador.
 */
public class PronosticoControlador implements ActionListener {

    private final PronosticoVista   vista;
    private final PronosticosMotor  motor;

    // Modo de edición: -1 = nuevo pronóstico, >0 = editando ese id
    private int idEditando = -1;

    public PronosticoControlador(PronosticoVista vista, PronosticosMotor motor) {
        this.vista  = vista;
        this.motor  = motor;
        this.vista.agregarListener(this);
        // Carga inicial
        refrescarTabla();
        actualizarTitulo();
    }

    // ---------------------------------------------------------------
    // ActionListener central — igual que CalculadoraControlador
    // ---------------------------------------------------------------
    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd == null) return;

        switch (cmd) {
            case "CMD_GUARDAR":        accionGuardar();        break;
            case "CMD_ACTUALIZAR":     accionActualizar();     break;
            case "CMD_EDITAR_FILA":    cargarEnFormulario();   break;
            case "CMD_ELIMINAR":       eliminarSeleccionado(); break;
            case "CMD_BUSCAR":         buscarPorJugador();     break;
            case "CMD_VER_TODOS":      verTodos();             break;
            case "CMD_FILTRAR_FECHAS": filtrarPorFechas();     break;
            case "CMD_EXPORTAR_CSV":   exportarCSV();          break;
            case "CMD_ESTADISTICAS":   abrirEstadisticas();    break;
            case "CMD_RESULTADO_REAL": registrarResultadoReal(); break;
            case "CMD_MODO_OSCURO":    toggleModoOscuro();     break;
            case "CMD_CANCELAR_EDICION": cancelarEdicion();    break;
            default: break;
        }
    }

    // ---------------------------------------------------------------
    // GUARDAR nuevo pronóstico
    // ---------------------------------------------------------------
    private void accionGuardar() {
        String jugador   = vista.getNombreJugador();
        String local     = vista.getEquipoLocal();
        String visitante = vista.getEquipoVisitante();
        int gl = vista.getGolesLocal();
        int gv = vista.getGolesVisitante();

        vista.deshabilitarBotonGuardar(true);
        vista.setEstado("Verificando...");

        new Thread(() -> {
            String resultado = motor.guardar(jugador, local, visitante, gl, gv);
            SwingUtilities.invokeLater(() -> {
                vista.deshabilitarBotonGuardar(false);
                if (resultado.startsWith("DUPLICADO:")) {
                    String msg = resultado.substring("DUPLICADO:".length());
                    int resp = JOptionPane.showConfirmDialog(vista.getFrame(),
                            msg + "\n¿Guardar de todas formas?",
                            "Pronóstico duplicado", JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (resp == JOptionPane.YES_OPTION) {
                        guardarForzado(jugador, local, visitante, gl, gv);
                    } else {
                        vista.setEstado("Guardado cancelado.");
                        ToastNotificacion.mostrar(vista.getFrame(), "Cancelado.", ToastNotificacion.Tipo.AVISO);
                    }
                } else if (resultado.startsWith("OK:")) {
                    String id = resultado.substring(3);
                    ToastNotificacion.mostrar(vista.getFrame(), "Pronóstico guardado (ID " + id + ")", ToastNotificacion.Tipo.EXITO);
                    vista.setEstado("Guardado con ID " + id);
                    vista.limpiarFormulario();
                    refrescarTabla();
                    actualizarTitulo();
                } else {
                    String msg = resultado.substring("ERROR:".length());
                    ToastNotificacion.mostrar(vista.getFrame(), msg, ToastNotificacion.Tipo.ERROR);
                    vista.setEstado(msg);
                }
            });
        }).start();
    }

    private void guardarForzado(String jugador, String local, String visitante, int gl, int gv) {
        new Thread(() -> {
            String resultado = motor.guardarForzado(jugador, local, visitante, gl, gv);
            SwingUtilities.invokeLater(() -> {
                if (resultado.startsWith("OK:")) {
                    String id = resultado.substring(3);
                    ToastNotificacion.mostrar(vista.getFrame(), "Pronóstico guardado (ID " + id + ")", ToastNotificacion.Tipo.EXITO);
                    vista.setEstado("Guardado con ID " + id);
                    vista.limpiarFormulario();
                    refrescarTabla();
                    actualizarTitulo();
                } else {
                    String msg = resultado.substring("ERROR:".length());
                    ToastNotificacion.mostrar(vista.getFrame(), msg, ToastNotificacion.Tipo.ERROR);
                }
            });
        }).start();
    }

    // ---------------------------------------------------------------
    // EDITAR: cargar fila seleccionada en el formulario
    // ---------------------------------------------------------------
    private void cargarEnFormulario() {
        int[] datos = vista.getFilaSeleccionada();
        // datos = {id, golesLocal, golesVisitante}  o null si no hay selección
        if (datos == null) {
            ToastNotificacion.mostrar(vista.getFrame(), "Selecciona una fila primero.", ToastNotificacion.Tipo.AVISO);
            return;
        }
        idEditando = datos[0];
        vista.cargarFilaEnFormulario();
        vista.mostrarModoEdicion(true);
        vista.setEstado("Editando ID " + idEditando + ". Modifica y pulsa 'Actualizar'.");
    }

    // ---------------------------------------------------------------
    // ACTUALIZAR pronóstico en modo edición
    // ---------------------------------------------------------------
    private void accionActualizar() {
        if (idEditando == -1) return;

        String jugador   = vista.getNombreJugador();
        String local     = vista.getEquipoLocal();
        String visitante = vista.getEquipoVisitante();
        int gl = vista.getGolesLocal();
        int gv = vista.getGolesVisitante();

        new Thread(() -> {
            String resultado = motor.actualizar(idEditando, jugador, local, visitante, gl, gv);
            SwingUtilities.invokeLater(() -> {
                if ("OK".equals(resultado)) {
                    ToastNotificacion.mostrar(vista.getFrame(), "Pronóstico actualizado.", ToastNotificacion.Tipo.EXITO);
                    vista.setEstado("ID " + idEditando + " actualizado.");
                } else {
                    String msg = resultado.substring("ERROR:".length());
                    ToastNotificacion.mostrar(vista.getFrame(), msg, ToastNotificacion.Tipo.ERROR);
                }
                cancelarEdicion();
                refrescarTabla();
            });
        }).start();
    }

    // ---------------------------------------------------------------
    // ELIMINAR fila seleccionada
    // ---------------------------------------------------------------
    private void eliminarSeleccionado() {
        int[] datos = vista.getFilaSeleccionada();
        if (datos == null) {
            ToastNotificacion.mostrar(vista.getFrame(), "Selecciona una fila primero.", ToastNotificacion.Tipo.AVISO);
            return;
        }
        int id = datos[0];
        String jugador = vista.getNombreJugadorDeTabla();

        int conf = JOptionPane.showConfirmDialog(vista.getFrame(),
                "¿Eliminar el pronóstico de \"" + jugador + "\" (ID " + id + ")?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) return;

        new Thread(() -> {
            String resultado = motor.eliminar(id);
            SwingUtilities.invokeLater(() -> {
                if ("OK".equals(resultado)) {
                    ToastNotificacion.mostrar(vista.getFrame(), "Pronóstico eliminado.", ToastNotificacion.Tipo.INFO);
                    vista.setEstado("ID " + id + " eliminado.");
                } else {
                    ToastNotificacion.mostrar(vista.getFrame(), "Error al eliminar.", ToastNotificacion.Tipo.ERROR);
                }
                refrescarTabla();
                actualizarTitulo();
            });
        }).start();
    }

    // ---------------------------------------------------------------
    // BUSCAR por nombre de jugador
    // ---------------------------------------------------------------
    private void buscarPorJugador() {
        String nombre = vista.getTextoBusqueda().trim();
        if (nombre.isEmpty()) { verTodos(); return; }

        new Thread(() -> {
            List<Pronostico> lista = motor.buscarPorNombre(nombre);
            SwingUtilities.invokeLater(() -> {
                vista.poblarTabla(lista);
                vista.setEstado(lista.size() + " resultado(s) para \"" + nombre + "\".");
            });
        }).start();
    }

    private void verTodos() {
        vista.limpiarBusqueda();
        refrescarTabla();
    }

    // ---------------------------------------------------------------
    // FILTRAR por rango de fechas
    // ---------------------------------------------------------------
    private void filtrarPorFechas() {
        java.util.Date dDesde = vista.getFechaDesde();
        java.util.Date dHasta = vista.getFechaHasta();

        LocalDateTime desde = dDesde.toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate().atStartOfDay();
        LocalDateTime hasta = dHasta.toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate().atTime(LocalTime.MAX);

        if (desde.isAfter(hasta)) {
            ToastNotificacion.mostrar(vista.getFrame(),
                    "'Desde' no puede ser mayor que 'Hasta'.", ToastNotificacion.Tipo.AVISO);
            return;
        }

        new Thread(() -> {
            List<Pronostico> lista = motor.listarPorFechas(desde, hasta);
            SwingUtilities.invokeLater(() -> {
                vista.poblarTabla(lista);
                vista.setEstado(lista.size() + " pronóstico(s) en ese rango de fechas.");
            });
        }).start();
    }

    // ---------------------------------------------------------------
    // EXPORTAR CSV
    // ---------------------------------------------------------------
    private void exportarCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("pronosticos.csv"));
        if (chooser.showSaveDialog(vista.getFrame()) != JFileChooser.APPROVE_OPTION) return;

        String contenido = vista.getContenidoTablaCSV();
        try (FileWriter fw = new FileWriter(chooser.getSelectedFile())) {
            fw.write("ID,Jugador,Local,GL,GV,Visitante,Predicho,Real,Estado,Fecha\n");
            fw.write(contenido);
            ToastNotificacion.mostrar(vista.getFrame(), "CSV exportado.", ToastNotificacion.Tipo.EXITO);
            vista.setEstado("CSV guardado: " + chooser.getSelectedFile().getName());
        } catch (IOException ex) {
            ToastNotificacion.mostrar(vista.getFrame(), "Error al exportar.", ToastNotificacion.Tipo.ERROR);
        }
    }

    // ---------------------------------------------------------------
    // ESTADÍSTICAS globales
    // ---------------------------------------------------------------
    private void abrirEstadisticas() {
        new Thread(() -> {
            Map<String, String>       stats   = motor.obtenerEstadisticas();
            List<Map<String, String>> ranking = motor.obtenerRanking();
            SwingUtilities.invokeLater(() ->
                new VentanaEstadisticas(stats, ranking).setVisible(true)
            );
        }).start();
    }

    // ---------------------------------------------------------------
    // REGISTRAR RESULTADO REAL de un partido
    // ---------------------------------------------------------------
    private void registrarResultadoReal() {
        String[] equipos = PronosticoVista.EQUIPOS;
        JComboBox<String> cbLocal     = new JComboBox<>(equipos);
        JComboBox<String> cbVisitante = new JComboBox<>(equipos);
        cbVisitante.setSelectedIndex(1);
        JSpinner spGL = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1));
        JSpinner spGV = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1));

        JPanel form = new JPanel(new java.awt.GridLayout(4, 2, 6, 6));
        form.add(new JLabel("Equipo Local:"));     form.add(cbLocal);
        form.add(new JLabel("Goles Local:"));      form.add(spGL);
        form.add(new JLabel("Equipo Visitante:")); form.add(cbVisitante);
        form.add(new JLabel("Goles Visitante:"));  form.add(spGV);

        int resp = JOptionPane.showConfirmDialog(vista.getFrame(), form,
                "Registrar Resultado Real", JOptionPane.OK_CANCEL_OPTION);
        if (resp != JOptionPane.OK_OPTION) return;

        String local     = (String) cbLocal.getSelectedItem();
        String visitante = (String) cbVisitante.getSelectedItem();
        int gl = (int) spGL.getValue();
        int gv = (int) spGV.getValue();

        new Thread(() -> {
            String resultado = motor.registrarResultadoReal(local, visitante, gl, gv);
            SwingUtilities.invokeLater(() -> {
                if (resultado.startsWith("OK:")) {
                    int n = Integer.parseInt(resultado.substring(3));
                    ToastNotificacion.mostrar(vista.getFrame(),
                            n + " pronóstico(s) actualizado(s).", ToastNotificacion.Tipo.EXITO);
                    vista.setEstado("Resultado " + gl + "-" + gv
                            + " registrado para " + local + " vs " + visitante);
                    refrescarTabla();
                } else {
                    String msg = resultado.substring("ERROR:".length());
                    ToastNotificacion.mostrar(vista.getFrame(), msg, ToastNotificacion.Tipo.ERROR);
                }
            });
        }).start();
    }

    // ---------------------------------------------------------------
    // MODO OSCURO
    // ---------------------------------------------------------------
    private void toggleModoOscuro() {
        boolean ahora = vista.toggleModoOscuro();
        ToastNotificacion.mostrar(vista.getFrame(),
                ahora ? "Modo oscuro activado." : "Modo claro activado.",
                ToastNotificacion.Tipo.INFO);
    }

    // ---------------------------------------------------------------
    // HISTORIAL de jugador (doble clic en tabla)
    // ---------------------------------------------------------------
    public void abrirHistorialJugador(String jugador) {
        SwingUtilities.invokeLater(() ->
            new VentanaHistorialJugador(vista.getFrame(), jugador, new PronosticoDAO())
                .setVisible(true)
        );
    }

    // ---------------------------------------------------------------
    // Helpers internos
    // ---------------------------------------------------------------
    private void refrescarTabla() {
        new Thread(() -> {
            List<Pronostico> lista = motor.listarTodos();
            SwingUtilities.invokeLater(() -> {
                vista.poblarTabla(lista);
                vista.setEstado("Actualizado — " + lista.size() + " pronóstico(s).");
            });
        }).start();
    }

    private void actualizarTitulo() {
        new Thread(() -> {
            int total = motor.contarPronosticos();
            SwingUtilities.invokeLater(() ->
                vista.setTitulo("Pronósticos del Mundial 2026  (" + total + " guardados)")
            );
        }).start();
    }

    private void cancelarEdicion() {
        idEditando = -1;
        vista.mostrarModoEdicion(false);
        vista.limpiarFormulario();
    }
}
