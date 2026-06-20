import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class VentanaMundial extends JFrame {

    // --- Formulario ---
    private JComboBox<String> cmbJugador;
    private JComboBox<String> cmbLocal;
    private JComboBox<String> cmbVisitante;
    private JSpinner spinGolesLocal, spinGolesVisitante;
    private JButton btnGuardar, btnEditar;

    // --- Búsqueda y filtros ---
    private JTextField txtBuscar;
    private JSpinner spinDesde, spinHasta;

    // --- Tabla ---
    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> sorter;

    // --- Estado ---
    private int idEditando = -1;
    private boolean modoOscuro = false;

    // --- Etiquetas ---
    private JLabel lblTitulo, lblEstado;
    private JPanel panelPrincipal;

    // --- DAO ---
    private PronosticoDAO dao;

    // --- Colores tema claro ---
    private static final Color CL_FONDO       = new Color(245, 245, 245);
    private static final Color CL_LOCAL       = new Color(198, 239, 206);
    private static final Color CL_EMPATE      = new Color(255, 235, 156);
    private static final Color CL_VISITANTE   = new Color(197, 217, 241);
    private static final Color CL_ACIERTO     = new Color(198, 239, 206);
    private static final Color CL_FALLO       = new Color(255, 199, 206);
    private static final Color CL_PENDIENTE   = new Color(235, 235, 235);

    // --- Colores tema oscuro ---
    private static final Color CO_FONDO       = new Color(40,  40,  40);
    private static final Color CO_PANEL       = new Color(55,  55,  55);
    private static final Color CO_TEXTO       = new Color(220, 220, 220);
    private static final Color CO_LOCAL       = new Color(30,  90,  50);
    private static final Color CO_EMPATE      = new Color(100, 85,  20);
    private static final Color CO_VISITANTE   = new Color(30,  60,  110);
    private static final Color CO_ACIERTO     = new Color(30,  90,  50);
    private static final Color CO_FALLO       = new Color(110, 30,  30);
    private static final Color CO_PENDIENTE   = new Color(70,  70,  70);

    private static final String[] EQUIPOS = {
        // Grupo A
        "México", "Sudáfrica", "Corea del Sur", "Chequia",
        // Grupo B
        "Canadá", "Bosnia y Herzegovina", "Qatar", "Suiza",
        // Grupo C
        "Brasil", "Marruecos", "Haití", "Escocia",
        // Grupo D
        "Estados Unidos", "Paraguay", "Australia", "Turquía",
        // Grupo E
        "Alemania", "Curazao", "Costa de Marfil", "Ecuador",
        // Grupo F
        "Países Bajos", "Japón", "Suecia", "Túnez",
        // Grupo G
        "Bélgica", "Egipto", "Irán", "Nueva Zelanda",
        // Grupo H
        "España", "Cabo Verde", "Arabia Saudita", "Uruguay",
        // Grupo I
        "Francia", "Senegal", "Irak", "Noruega",
        // Grupo J
        "Argentina", "Argelia", "Austria", "Jordania",
        // Grupo K
        "Portugal", "Rep. Democrática del Congo", "Uzbekistán", "Colombia",
        // Grupo L
        "Inglaterra", "Croacia", "Ghana", "Panamá"
    };

    public VentanaMundial() {
        dao = new PronosticoDAO();
        setTitle("Pronósticos del Mundial 2026");
        setSize(960, 680);
        setMinimumSize(new Dimension(800, 560));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        panelPrincipal = new JPanel(new BorderLayout(8, 8));
        panelPrincipal.setBackground(CL_FONDO);
        setContentPane(panelPrincipal);

        panelPrincipal.add(crearPanelTitulo(),   BorderLayout.NORTH);
        panelPrincipal.add(crearPanelCentral(),  BorderLayout.CENTER);
        panelPrincipal.add(crearPanelSur(),      BorderLayout.SOUTH);

        refrescarTabla();
        actualizarTitulo();
    }

    // ---------------------------------------------------------------
    // NORTE: título + botón modo oscuro
    // ---------------------------------------------------------------
    private JPanel crearPanelTitulo() {
        lblTitulo = new JLabel("Pronósticos del Mundial 2026", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 0, 4, 0));

        JButton btnModoOscuro = new JButton("🌙 Modo Oscuro");
        btnModoOscuro.setFocusPainted(false);
        btnModoOscuro.addActionListener(e -> toggleModoOscuro(btnModoOscuro));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(lblTitulo,     BorderLayout.CENTER);
        panel.add(btnModoOscuro, BorderLayout.EAST);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        return panel;
    }

    // ---------------------------------------------------------------
    // CENTRO: formulario + tabla
    // ---------------------------------------------------------------
    private JPanel crearPanelCentral() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        panel.add(crearPanelFormulario(), BorderLayout.NORTH);
        panel.add(crearPanelTabla(),      BorderLayout.CENTER);
        return panel;
    }

    // ---------------------------------------------------------------
    // Formulario de entrada
    // ---------------------------------------------------------------
    private JPanel crearPanelFormulario() {
        JPanel p = new JPanel(new GridLayout(3, 4, 8, 8));
        p.setBorder(BorderFactory.createTitledBorder("Nuevo Pronóstico"));
        p.setOpaque(false);

        p.add(new JLabel("Tu Nombre:"));
        cmbJugador = new JComboBox<>(new String[]{
            "Jugador 1","Jugador 2","Jugador 3","Jugador 4","Jugador 5",
            "Jugador 6","Jugador 7","Jugador 8","Jugador 9","Jugador 10"
        });
        cmbJugador.setEditable(true);
        p.add(cmbJugador);

        p.add(new JLabel("Equipo Local:"));
        cmbLocal = new JComboBox<>(EQUIPOS);
        p.add(cmbLocal);

        p.add(new JLabel("Goles Local:"));
        spinGolesLocal = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1));
        p.add(spinGolesLocal);

        p.add(new JLabel("Equipo Visitante:"));
        cmbVisitante = new JComboBox<>(EQUIPOS);
        cmbVisitante.setSelectedIndex(1);
        p.add(cmbVisitante);

        p.add(new JLabel("Goles Visitante:"));
        spinGolesVisitante = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1));
        p.add(spinGolesVisitante);

        btnGuardar = new JButton("Guardar");
        btnGuardar.setFont(new Font("Arial", Font.BOLD, 13));
        btnGuardar.setBackground(new Color(34, 139, 34));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.addActionListener(this::accionGuardar);
        p.add(btnGuardar);

        btnEditar = new JButton("Actualizar Edición");
        btnEditar.setFont(new Font("Arial", Font.BOLD, 13));
        btnEditar.setBackground(new Color(30, 100, 180));
        btnEditar.setForeground(Color.WHITE);
        btnEditar.setVisible(false);
        btnEditar.addActionListener(this::accionActualizar);
        p.add(btnEditar);

        return p;
    }

    // ---------------------------------------------------------------
    // Panel tabla: barra de herramientas + filtros de fecha + tabla
    // ---------------------------------------------------------------
    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Pronósticos Guardados"));
        panel.setOpaque(false);

        // --- Barra superior: búsqueda y acciones ---
        JPanel barraSup = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 4));
        barraSup.setOpaque(false);

        barraSup.add(new JLabel("Buscar:"));
        txtBuscar = new JTextField(12);
        barraSup.add(txtBuscar);

        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.addActionListener(e -> buscarPorJugador());
        barraSup.add(btnBuscar);

        JButton btnVerTodos = new JButton("Ver Todos");
        btnVerTodos.addActionListener(e -> { txtBuscar.setText(""); refrescarTabla(); });
        barraSup.add(btnVerTodos);

        JButton btnEditarFila = new JButton("✏ Editar");
        btnEditarFila.setBackground(new Color(30, 100, 180));
        btnEditarFila.setForeground(Color.WHITE);
        btnEditarFila.addActionListener(this::cargarEnFormulario);
        barraSup.add(btnEditarFila);

        JButton btnEliminar = new JButton("✖ Eliminar");
        btnEliminar.setBackground(new Color(180, 30, 30));
        btnEliminar.setForeground(Color.WHITE);
        btnEliminar.addActionListener(this::eliminarSeleccionado);
        barraSup.add(btnEliminar);

        JButton btnExportar = new JButton("⬇ CSV");
        btnExportar.addActionListener(e -> exportarCSV());
        barraSup.add(btnExportar);

        JButton btnStats = new JButton("📊 Estadísticas");
        btnStats.addActionListener(e -> abrirEstadisticas());
        barraSup.add(btnStats);

        JButton btnResultado = new JButton("⚑ Resultado Real");
        btnResultado.setBackground(new Color(130, 60, 180));
        btnResultado.setForeground(Color.WHITE);
        btnResultado.addActionListener(e -> registrarResultadoReal());
        barraSup.add(btnResultado);

        // --- Barra inferior: filtro por fechas ---
        JPanel barraFecha = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        barraFecha.setOpaque(false);
        barraFecha.add(new JLabel("Desde:"));
        spinDesde = new JSpinner(new SpinnerDateModel());
        spinDesde.setEditor(new JSpinner.DateEditor(spinDesde, "dd/MM/yyyy"));
        spinDesde.setPreferredSize(new Dimension(110, 24));
        barraFecha.add(spinDesde);

        barraFecha.add(new JLabel("Hasta:"));
        spinHasta = new JSpinner(new SpinnerDateModel());
        spinHasta.setEditor(new JSpinner.DateEditor(spinHasta, "dd/MM/yyyy"));
        spinHasta.setPreferredSize(new Dimension(110, 24));
        barraFecha.add(spinHasta);

        JButton btnFiltrarFecha = new JButton("Filtrar Fechas");
        btnFiltrarFecha.addActionListener(e -> filtrarPorFechas());
        barraFecha.add(btnFiltrarFecha);

        JPanel barras = new JPanel(new GridLayout(2, 1));
        barras.setOpaque(false);
        barras.add(barraSup);
        barras.add(barraFecha);
        panel.add(barras, BorderLayout.NORTH);

        // --- Tabla ---
        String[] cols = {"ID","Jugador","Local","GL","GV","Visitante","Predicho","Real","Estado","Fecha"};
        modeloTabla = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tabla = new JTable(modeloTabla) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    int mr = convertRowIndexToModel(row);
                    String estado   = (String) modeloTabla.getValueAt(mr, 8);
                    String predicho = (String) modeloTabla.getValueAt(mr, 6);
                    if (estado.contains("Acertó"))       c.setBackground(colorAcierto());
                    else if (estado.contains("Falló"))   c.setBackground(colorFallo());
                    else if (estado.contains("Pendiente")) {
                        if ("Local".equals(predicho))        c.setBackground(colorLocal());
                        else if ("Empate".equals(predicho))  c.setBackground(colorEmpate());
                        else                                 c.setBackground(colorVisitante());
                    } else c.setBackground(colorFondo());
                }
                return c;
            }
        };

        sorter = new TableRowSorter<>(modeloTabla);
        tabla.setRowSorter(sorter);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(22);
        tabla.getColumnModel().getColumn(0).setMaxWidth(45);
        tabla.getColumnModel().getColumn(3).setMaxWidth(35);
        tabla.getColumnModel().getColumn(4).setMaxWidth(35);
        tabla.getColumnModel().getColumn(6).setMaxWidth(70);
        tabla.getColumnModel().getColumn(7).setMaxWidth(55);
        tabla.getColumnModel().getColumn(8).setPreferredWidth(90);
        tabla.getColumnModel().getColumn(9).setPreferredWidth(120);

        // Doble clic → historial del jugador
        tabla.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) abrirHistorialJugador();
            }
        });

        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Leyenda
        JPanel leyenda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        leyenda.setOpaque(false);
        leyenda.add(etiquetaColor(new Color(198,239,206), "Acertó / Local"));
        leyenda.add(etiquetaColor(new Color(255,199,206), "Falló"));
        leyenda.add(etiquetaColor(new Color(255,235,156), "Empate pred."));
        leyenda.add(etiquetaColor(new Color(197,217,241), "Visitante pred."));
        panel.add(leyenda, BorderLayout.SOUTH);

        return panel;
    }

    // ---------------------------------------------------------------
    // SUR: barra de estado
    // ---------------------------------------------------------------
    private JPanel crearPanelSur() {
        lblEstado = new JLabel("Listo.", SwingConstants.LEFT);
        lblEstado.setFont(new Font("Arial", Font.PLAIN, 12));
        lblEstado.setBorder(BorderFactory.createEmptyBorder(3, 10, 5, 10));
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        p.add(lblEstado, BorderLayout.CENTER);
        return p;
    }

    // ---------------------------------------------------------------
    // ACCIÓN: guardar nuevo pronóstico
    // ---------------------------------------------------------------
    private void accionGuardar(ActionEvent e) {
        String jugador   = cmbJugador.getEditor().getItem().toString().trim();
        String local     = (String) cmbLocal.getSelectedItem();
        String visitante = (String) cmbVisitante.getSelectedItem();

        if (jugador.isEmpty()) {
            ToastNotificacion.mostrar(this, "Ingresa tu nombre.", ToastNotificacion.Tipo.AVISO);
            return;
        }
        if (local.equals(visitante)) {
            ToastNotificacion.mostrar(this, "Los equipos no pueden ser iguales.", ToastNotificacion.Tipo.ERROR);
            return;
        }
        int gl = (int) spinGolesLocal.getValue();
        int gv = (int) spinGolesVisitante.getValue();

        btnGuardar.setEnabled(false);
        lblEstado.setText("Verificando...");

        new Thread(() -> {
            boolean dup = dao.existePronostico(jugador, local, visitante);
            SwingUtilities.invokeLater(() -> {
                if (dup) {
                    int r = JOptionPane.showConfirmDialog(this,
                        jugador + " ya pronosticó " + local + " vs " + visitante + ".\n¿Guardar de todas formas?",
                        "Duplicado", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (r != JOptionPane.YES_OPTION) {
                        btnGuardar.setEnabled(true);
                        lblEstado.setText("Cancelado.");
                        return;
                    }
                }
                Pronostico nuevo = new Pronostico(jugador, local, visitante, gl, gv);
                new Thread(() -> {
                    boolean ok = dao.guardarPronostico(nuevo);
                    SwingUtilities.invokeLater(() -> {
                        btnGuardar.setEnabled(true);
                        if (ok) {
                            ToastNotificacion.mostrar(this, "Pronóstico guardado (ID " + nuevo.getId() + ")", ToastNotificacion.Tipo.EXITO);
                            lblEstado.setText("Guardado ID " + nuevo.getId());
                            limpiarFormulario(); refrescarTabla(); actualizarTitulo();
                        } else {
                            ToastNotificacion.mostrar(this, "Error al guardar en BD.", ToastNotificacion.Tipo.ERROR);
                            lblEstado.setText("Error al guardar.");
                        }
                    });
                }).start();
            });
        }).start();
    }

    // ---------------------------------------------------------------
    // ACCIÓN: cargar fila en formulario para editar
    // ---------------------------------------------------------------
    private void cargarEnFormulario(ActionEvent e) {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            ToastNotificacion.mostrar(this, "Selecciona una fila primero.", ToastNotificacion.Tipo.AVISO);
            return;
        }
        int mr = tabla.convertRowIndexToModel(fila);
        idEditando = (int) modeloTabla.getValueAt(mr, 0);
        cmbJugador.getEditor().setItem(modeloTabla.getValueAt(mr, 1).toString());
        cmbLocal.setSelectedItem(modeloTabla.getValueAt(mr, 2));
        spinGolesLocal.setValue(modeloTabla.getValueAt(mr, 3));
        spinGolesVisitante.setValue(modeloTabla.getValueAt(mr, 4));
        cmbVisitante.setSelectedItem(modeloTabla.getValueAt(mr, 5));
        btnGuardar.setVisible(false);
        btnEditar.setVisible(true);
        lblEstado.setText("Editando ID " + idEditando + ". Modifica y pulsa 'Actualizar Edición'.");
    }

    // ---------------------------------------------------------------
    // ACCIÓN: actualizar pronóstico en modo edición
    // ---------------------------------------------------------------
    private void accionActualizar(ActionEvent e) {
        if (idEditando == -1) return;
        String jugador   = cmbJugador.getEditor().getItem().toString().trim();
        String local     = (String) cmbLocal.getSelectedItem();
        String visitante = (String) cmbVisitante.getSelectedItem();
        if (jugador.isEmpty() || local.equals(visitante)) {
            ToastNotificacion.mostrar(this, "Datos inválidos.", ToastNotificacion.Tipo.ERROR);
            return;
        }
        Pronostico p = new Pronostico(jugador, local, visitante,
                (int) spinGolesLocal.getValue(), (int) spinGolesVisitante.getValue());
        p.setId(idEditando);
        btnEditar.setEnabled(false);
        new Thread(() -> {
            boolean ok = dao.actualizar(p);
            SwingUtilities.invokeLater(() -> {
                btnEditar.setEnabled(true);
                ToastNotificacion.mostrar(this, ok ? "Pronóstico actualizado." : "Error al actualizar.",
                        ok ? ToastNotificacion.Tipo.EXITO : ToastNotificacion.Tipo.ERROR);
                cancelarEdicion(); refrescarTabla();
            });
        }).start();
    }

    // ---------------------------------------------------------------
    // ACCIÓN: eliminar fila seleccionada
    // ---------------------------------------------------------------
    private void eliminarSeleccionado(ActionEvent e) {
        int fila = tabla.getSelectedRow();
        if (fila == -1) { ToastNotificacion.mostrar(this, "Selecciona una fila.", ToastNotificacion.Tipo.AVISO); return; }
        int mr = tabla.convertRowIndexToModel(fila);
        int id = (int) modeloTabla.getValueAt(mr, 0);
        String jug = (String) modeloTabla.getValueAt(mr, 1);
        int ok = JOptionPane.showConfirmDialog(this,
                "¿Eliminar pronóstico de \"" + jug + "\" (ID " + id + ")?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
            new Thread(() -> {
                boolean exito = dao.eliminar(id);
                SwingUtilities.invokeLater(() -> {
                    ToastNotificacion.mostrar(this, exito ? "Eliminado." : "Error al eliminar.",
                            exito ? ToastNotificacion.Tipo.INFO : ToastNotificacion.Tipo.ERROR);
                    refrescarTabla(); actualizarTitulo();
                });
            }).start();
        }
    }

    // ---------------------------------------------------------------
    // ACCIÓN: registrar resultado real de un partido
    // ---------------------------------------------------------------
    private void registrarResultadoReal() {
        String[] equipos = EQUIPOS;
        JComboBox<String> cbLocal    = new JComboBox<>(equipos);
        JComboBox<String> cbVisitante = new JComboBox<>(equipos);
        cbVisitante.setSelectedIndex(1);
        JSpinner spGL = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1));
        JSpinner spGV = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1));

        JPanel form = new JPanel(new GridLayout(4, 2, 6, 6));
        form.add(new JLabel("Equipo Local:"));     form.add(cbLocal);
        form.add(new JLabel("Goles Local:"));      form.add(spGL);
        form.add(new JLabel("Equipo Visitante:")); form.add(cbVisitante);
        form.add(new JLabel("Goles Visitante:"));  form.add(spGV);

        int res = JOptionPane.showConfirmDialog(this, form,
                "Registrar Resultado Real", JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return;

        String local     = (String) cbLocal.getSelectedItem();
        String visitante = (String) cbVisitante.getSelectedItem();
        int gl = (int) spGL.getValue();
        int gv = (int) spGV.getValue();

        if (local.equals(visitante)) {
            ToastNotificacion.mostrar(this, "Los equipos no pueden ser iguales.", ToastNotificacion.Tipo.ERROR);
            return;
        }
        new Thread(() -> {
            int actualizados = dao.registrarResultadoReal(local, visitante, gl, gv);
            SwingUtilities.invokeLater(() -> {
                if (actualizados >= 0) {
                    ToastNotificacion.mostrar(this,
                            actualizados + " pronóstico(s) actualizado(s).", ToastNotificacion.Tipo.EXITO);
                    lblEstado.setText("Resultado real " + gl + "-" + gv + " registrado para " + local + " vs " + visitante);
                    refrescarTabla();
                } else {
                    ToastNotificacion.mostrar(this, "Error al registrar resultado.", ToastNotificacion.Tipo.ERROR);
                }
            });
        }).start();
    }

    // ---------------------------------------------------------------
    // ACCIÓN: filtrar por rango de fechas
    // ---------------------------------------------------------------
    private void filtrarPorFechas() {
        java.util.Date dDesde = (java.util.Date) spinDesde.getValue();
        java.util.Date dHasta = (java.util.Date) spinHasta.getValue();
        LocalDateTime desde = dDesde.toInstant().atZone(java.time.ZoneId.systemDefault())
                .toLocalDate().atStartOfDay();
        LocalDateTime hasta = dHasta.toInstant().atZone(java.time.ZoneId.systemDefault())
                .toLocalDate().atTime(LocalTime.MAX);

        if (desde.isAfter(hasta)) {
            ToastNotificacion.mostrar(this, "La fecha 'Desde' no puede ser mayor que 'Hasta'.", ToastNotificacion.Tipo.AVISO);
            return;
        }
        new Thread(() -> {
            List<Pronostico> res = dao.listarPorFechas(desde, hasta);
            SwingUtilities.invokeLater(() -> {
                poblarTabla(res);
                lblEstado.setText(res.size() + " pronóstico(s) en ese rango de fechas.");
            });
        }).start();
    }

    // ---------------------------------------------------------------
    // ACCIÓN: abrir historial del jugador con doble clic
    // ---------------------------------------------------------------
    private void abrirHistorialJugador() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) return;
        int mr = tabla.convertRowIndexToModel(fila);
        String jugador = (String) modeloTabla.getValueAt(mr, 1);
        VentanaHistorialJugador hist = new VentanaHistorialJugador(this, jugador, dao);
        hist.setVisible(true);
    }

    // ---------------------------------------------------------------
    // ACCIÓN: buscar por jugador
    // ---------------------------------------------------------------
    private void buscarPorJugador() {
        String nombre = txtBuscar.getText().trim();
        if (nombre.isEmpty()) { refrescarTabla(); return; }
        new Thread(() -> {
            List<Pronostico> res = dao.buscarPorJugador(nombre);
            SwingUtilities.invokeLater(() -> {
                poblarTabla(res);
                lblEstado.setText(res.size() + " resultado(s) para \"" + nombre + "\".");
            });
        }).start();
    }

    // ---------------------------------------------------------------
    // ACCIÓN: exportar a CSV
    // ---------------------------------------------------------------
    private void exportarCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("pronosticos.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try (FileWriter fw = new FileWriter(chooser.getSelectedFile())) {
            fw.write("ID,Jugador,Local,GL,GV,Visitante,Predicho,Real,Estado,Fecha\n");
            for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                for (int j = 0; j < modeloTabla.getColumnCount(); j++) {
                    fw.write(modeloTabla.getValueAt(i, j).toString());
                    if (j < modeloTabla.getColumnCount() - 1) fw.write(",");
                }
                fw.write("\n");
            }
            ToastNotificacion.mostrar(this, "CSV exportado.", ToastNotificacion.Tipo.EXITO);
        } catch (IOException ex) {
            ToastNotificacion.mostrar(this, "Error al exportar.", ToastNotificacion.Tipo.ERROR);
        }
    }

    // ---------------------------------------------------------------
    // ACCIÓN: abrir ventana de estadísticas
    // ---------------------------------------------------------------
    private void abrirEstadisticas() {
        new Thread(() -> {
            var stats   = dao.listarEstadisticas();
            var ranking = dao.listarRanking();
            SwingUtilities.invokeLater(() -> new VentanaEstadisticas(stats, ranking).setVisible(true));
        }).start();
    }

    // ---------------------------------------------------------------
    // ACCIÓN: alternar modo oscuro
    // ---------------------------------------------------------------
    private void toggleModoOscuro(JButton btn) {
        modoOscuro = !modoOscuro;
        btn.setText(modoOscuro ? "☀ Modo Claro" : "🌙 Modo Oscuro");
        aplicarTema(panelPrincipal);
        SwingUtilities.updateComponentTreeUI(this);
        repaint();
        ToastNotificacion.mostrar(this,
                modoOscuro ? "Modo oscuro activado." : "Modo claro activado.",
                ToastNotificacion.Tipo.INFO);
    }

    private void aplicarTema(Container c) {
        Color fondo = modoOscuro ? CO_PANEL : CL_FONDO;
        Color texto = modoOscuro ? CO_TEXTO : Color.BLACK;
        c.setBackground(fondo);
        if (c instanceof JLabel) ((JLabel)c).setForeground(texto);
        for (Component comp : c.getComponents()) {
            if (comp instanceof Container) aplicarTema((Container) comp);
            comp.setBackground(fondo);
            if (comp instanceof JLabel) ((JLabel)comp).setForeground(texto);
        }
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------
    private void refrescarTabla() {
        new Thread(() -> {
            List<Pronostico> todos = dao.listarTodos();
            SwingUtilities.invokeLater(() -> {
                poblarTabla(todos);
                lblEstado.setText("Actualizado — " + todos.size() + " pronóstico(s).");
            });
        }).start();
    }

    private void poblarTabla(List<Pronostico> lista) {
        modeloTabla.setRowCount(0);
        for (Pronostico p : lista) {
            modeloTabla.addRow(new Object[]{
                p.getId(), p.getJugador(), p.getEquipoLocal(),
                p.getGolesLocal(), p.getGolesVisitante(), p.getEquipoVisitante(),
                p.getResultadoPredicho(),
                p.getResultadoReal() != null ? p.getResultadoReal() : "-",
                p.getEstadoAcierto(), p.getFechaFormateada()
            });
        }
    }

    private void limpiarFormulario() {
        cmbJugador.getEditor().setItem("");
        cmbLocal.setSelectedIndex(0);
        cmbVisitante.setSelectedIndex(1);
        spinGolesLocal.setValue(0);
        spinGolesVisitante.setValue(0);
    }

    private void cancelarEdicion() {
        idEditando = -1;
        btnGuardar.setVisible(true);
        btnEditar.setVisible(false);
        limpiarFormulario();
    }

    private void actualizarTitulo() {
        new Thread(() -> {
            int total = dao.contarPronosticos();
            SwingUtilities.invokeLater(() ->
                lblTitulo.setText("Pronósticos del Mundial 2026  (" + total + " guardados)")
            );
        }).start();
    }

    private JLabel etiquetaColor(Color c, String txt) {
        JLabel l = new JLabel("  " + txt + "  ");
        l.setOpaque(true); l.setBackground(c);
        l.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        l.setFont(new Font("Arial", Font.PLAIN, 11));
        return l;
    }

    // Colores dinámicos según el tema activo
    private Color colorFondo()    { return modoOscuro ? CO_FONDO     : CL_FONDO; }
    private Color colorLocal()    { return modoOscuro ? CO_LOCAL     : CL_LOCAL; }
    private Color colorEmpate()   { return modoOscuro ? CO_EMPATE    : CL_EMPATE; }
    private Color colorVisitante(){ return modoOscuro ? CO_VISITANTE : CL_VISITANTE; }
    private Color colorAcierto()  { return modoOscuro ? CO_ACIERTO   : CL_ACIERTO; }
    private Color colorFallo()    { return modoOscuro ? CO_FALLO     : CL_FALLO; }
}
