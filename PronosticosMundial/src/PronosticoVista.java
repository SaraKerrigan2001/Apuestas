import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Vista MVC del sistema de pronósticos.
 * Usa BetTheme como paleta de colores (siempre tema oscuro por defecto).
 * Expone getPanelRaiz() para ser embebida en BetCentralApp via CardLayout.
 */
public class PronosticoVista {

    public static final String[] EQUIPOS = {
        "México","Sudáfrica","Corea del Sur","Chequia",
        "Canadá","Bosnia y Herzegovina","Qatar","Suiza",
        "Brasil","Marruecos","Haití","Escocia",
        "Estados Unidos","Paraguay","Australia","Turquía",
        "Alemania","Curazao","Costa de Marfil","Ecuador",
        "Países Bajos","Japón","Suecia","Túnez",
        "Bélgica","Egipto","Irán","Nueva Zelanda",
        "España","Cabo Verde","Arabia Saudita","Uruguay",
        "Francia","Senegal","Irak","Noruega",
        "Argentina","Argelia","Austria","Jordania",
        "Portugal","Rep. Democrática del Congo","Uzbekistán","Colombia",
        "Inglaterra","Croacia","Ghana","Panamá"
    };

    // Colores de estado en filas de tabla
    private static final Color C_LOCAL     = new Color(20,  80,  40);
    private static final Color C_EMPATE    = new Color(90,  75,  10);
    private static final Color C_VISITANTE = new Color(20,  55, 100);
    private static final Color C_ACIERTO   = new Color(20,  80,  40);
    private static final Color C_FALLO     = new Color(100, 25,  25);
    private static final Color C_INPUT     = new Color(55,  58,  66);
    private static final Color C_BORDE     = new Color(80,  85,  95);

    // Siempre en modo oscuro (integrado en BetCentral)
    private boolean modoOscuro = true;

    // Componentes principales
    private final JFrame  frame;
    private       JPanel  panelRaiz;

    // Formulario
    private JComboBox<String> cmbJugador;
    private JComboBox<String> cmbLocal;
    private JComboBox<String> cmbVisitante;
    private JSpinner          spinGolesLocal;
    private JSpinner          spinGolesVisitante;
    private JButton           btnGuardar;
    private JButton           btnActualizar;
    private JButton           btnCancelarEdicion;

    // Barra
    private JTextField txtBuscar;
    private JSpinner   spinDesde;
    private JSpinner   spinHasta;
    private JButton    btnModoOscuro;

    // Tabla
    private JTable            tabla;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> sorter;

    // Etiquetas
    private JLabel lblTitulo;
    private JLabel lblEstado;

    // Botones simples (para tema)
    private final java.util.List<JButton> botonesSimples = new java.util.ArrayList<>();

    // Controlador
    private PronosticoControlador controlador;

    // ---------------------------------------------------------------
    // Constructor — construye el panel raíz (NO un JFrame independiente)
    // ---------------------------------------------------------------
    public PronosticoVista() {
        frame = null; // No usamos JFrame propio, vivimos dentro de BetCentralApp
        construirUI();
    }

    private void construirUI() {
        panelRaiz = new JPanel(new BorderLayout(8, 8));
        panelRaiz.setBackground(BetTheme.BACKGROUND);
        panelRaiz.setOpaque(true);
        panelRaiz.add(crearPanelTitulo(),  BorderLayout.NORTH);
        panelRaiz.add(crearPanelCentral(), BorderLayout.CENTER);
        panelRaiz.add(crearPanelEstado(),  BorderLayout.SOUTH);
    }

    /** Devuelve el panel raíz para ser embebido en CardLayout */
    public JPanel getPanelRaiz() { return panelRaiz; }

    /** Devuelve true si está en modo oscuro */
    public boolean isModoOscuro() { return modoOscuro; }

    /** Registra el controlador (necesario para doble clic en tabla) */
    public void setControlador(PronosticoControlador ctrl) { this.controlador = ctrl; }

    // ---------------------------------------------------------------
    // NORTE: título con contador + botón modo
    // ---------------------------------------------------------------
    private JPanel crearPanelTitulo() {
        lblTitulo = new JLabel("My Bets — Pronósticos del Mundial 2026", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Inter", Font.BOLD, 18));
        lblTitulo.setForeground(BetTheme.PRIMARY);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 0, 4, 0));

        btnModoOscuro = crearBotonSimple("☀ Modo Claro", "CMD_MODO_OSCURO");
        estilizarBotonModo();

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(4, 14, 0, 14));
        panel.add(lblTitulo,     BorderLayout.CENTER);
        panel.add(btnModoOscuro, BorderLayout.EAST);
        return panel;
    }

    // ---------------------------------------------------------------
    // CENTRO: formulario + tabla
    // ---------------------------------------------------------------
    private JPanel crearPanelCentral() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        panel.add(crearPanelFormulario(), BorderLayout.NORTH);
        panel.add(crearPanelTabla(),      BorderLayout.CENTER);
        return panel;
    }

    // ---------------------------------------------------------------
    // Formulario de entrada con estilo BetTheme
    // ---------------------------------------------------------------
    private JPanel crearPanelFormulario() {
        JPanel p = new JPanel(new GridLayout(3, 4, 8, 8));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BetTheme.WHITE_10),
                "Nuevo Pronóstico",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Inter", Font.BOLD, 11),
                BetTheme.ON_SURFACE_VARIANT
            ),
            BorderFactory.createEmptyBorder(6, 8, 8, 8)
        ));
        p.setOpaque(false);

        p.add(lbl("Tu Nombre:"));
        cmbJugador = new JComboBox<>(new String[]{
            "Jugador 1","Jugador 2","Jugador 3","Jugador 4","Jugador 5",
            "Jugador 6","Jugador 7","Jugador 8","Jugador 9","Jugador 10"
        });
        cmbJugador.setEditable(true);
        estilizarInput(cmbJugador);
        p.add(cmbJugador);

        p.add(lbl("Equipo Local:"));
        cmbLocal = new JComboBox<>(EQUIPOS);
        estilizarInput(cmbLocal);
        p.add(cmbLocal);

        p.add(lbl("Goles Local:"));
        spinGolesLocal = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1));
        estilizarSpinner(spinGolesLocal);
        p.add(spinGolesLocal);

        p.add(lbl("Equipo Visitante:"));
        cmbVisitante = new JComboBox<>(EQUIPOS);
        cmbVisitante.setSelectedIndex(1);
        estilizarInput(cmbVisitante);
        p.add(cmbVisitante);

        p.add(lbl("Goles Visitante:"));
        spinGolesVisitante = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1));
        estilizarSpinner(spinGolesVisitante);
        p.add(spinGolesVisitante);

        btnGuardar = BetTheme.primaryButton("💾 Guardar");
        btnGuardar.setActionCommand("CMD_GUARDAR");
        p.add(btnGuardar);

        btnActualizar = crearBotonColor("✔ Actualizar", "CMD_ACTUALIZAR", new Color(30, 100, 180));
        btnActualizar.setVisible(false);
        p.add(btnActualizar);

        btnCancelarEdicion = crearBotonColor("✖ Cancelar", "CMD_CANCELAR_EDICION", new Color(120, 40, 40));
        btnCancelarEdicion.setVisible(false);
        p.add(btnCancelarEdicion);

        return p;
    }

    // ---------------------------------------------------------------
    // Panel tabla: barras + tabla + leyenda
    // ---------------------------------------------------------------
    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BetTheme.WHITE_10),
            "Pronósticos Guardados",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Inter", Font.BOLD, 11),
            BetTheme.ON_SURFACE_VARIANT
        ));
        panel.setOpaque(false);

        // Barra superior
        JPanel barraSup = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 4));
        barraSup.setOpaque(false);
        barraSup.add(lbl("Buscar:"));
        txtBuscar = new JTextField(12);
        estilizarTextField(txtBuscar);
        barraSup.add(txtBuscar);
        barraSup.add(crearBotonSimple("Buscar",      "CMD_BUSCAR"));
        barraSup.add(crearBotonSimple("Ver Todos",   "CMD_VER_TODOS"));
        barraSup.add(crearBotonColor("✏ Editar",     "CMD_EDITAR_FILA",    new Color(30, 100, 180)));
        barraSup.add(crearBotonColor("✖ Eliminar",   "CMD_ELIMINAR",       new Color(150, 30, 30)));
        barraSup.add(crearBotonSimple("⬇ CSV",       "CMD_EXPORTAR_CSV"));
        barraSup.add(crearBotonSimple("📊 Stats",    "CMD_ESTADISTICAS"));
        barraSup.add(crearBotonColor("⚑ Resultado",  "CMD_RESULTADO_REAL", new Color(100, 50, 160)));

        // Barra fechas
        JPanel barraFecha = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        barraFecha.setOpaque(false);
        barraFecha.add(lbl("Desde:"));
        spinDesde = new JSpinner(new SpinnerDateModel());
        spinDesde.setEditor(new JSpinner.DateEditor(spinDesde, "dd/MM/yyyy"));
        spinDesde.setPreferredSize(new Dimension(110, 28));
        estilizarSpinner(spinDesde);
        barraFecha.add(spinDesde);
        barraFecha.add(lbl("Hasta:"));
        spinHasta = new JSpinner(new SpinnerDateModel());
        spinHasta.setEditor(new JSpinner.DateEditor(spinHasta, "dd/MM/yyyy"));
        spinHasta.setPreferredSize(new Dimension(110, 28));
        estilizarSpinner(spinHasta);
        barraFecha.add(spinHasta);
        barraFecha.add(crearBotonSimple("Filtrar", "CMD_FILTRAR_FECHAS"));

        JPanel barras = new JPanel(new GridLayout(2, 1));
        barras.setOpaque(false);
        barras.add(barraSup);
        barras.add(barraFecha);
        panel.add(barras, BorderLayout.NORTH);

        // Tabla
        String[] cols = {"ID","Jugador","Local","GL","GV","Visitante","Predicho","Real","Estado","Fecha"};
        modeloTabla = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tabla = new JTable(modeloTabla) {
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    int mr = convertRowIndexToModel(row);
                    String est  = (String) modeloTabla.getValueAt(mr, 8);
                    String pred = (String) modeloTabla.getValueAt(mr, 6);
                    if      (est.contains("Acertó"))    c.setBackground(C_ACIERTO);
                    else if (est.contains("Falló"))     c.setBackground(C_FALLO);
                    else if ("Local".equals(pred))      c.setBackground(C_LOCAL);
                    else if ("Empate".equals(pred))     c.setBackground(C_EMPATE);
                    else                                c.setBackground(C_VISITANTE);
                }
                return c;
            }
        };
        tabla.setBackground(BetTheme.SURFACE_CONTAINER_LOW);
        tabla.setForeground(BetTheme.ON_SURFACE);
        tabla.setGridColor(new Color(60, 65, 75));
        tabla.setRowHeight(24);
        tabla.getTableHeader().setBackground(BetTheme.SURFACE_CONTAINER_HIGH);
        tabla.getTableHeader().setForeground(BetTheme.ON_SURFACE_VARIANT);
        tabla.getTableHeader().setFont(new Font("Inter", Font.BOLD, 11));
        tabla.setSelectionBackground(new Color(70, 100, 140));
        tabla.setSelectionForeground(BetTheme.PRIMARY);
        tabla.setFont(new Font("Inter", Font.PLAIN, 12));
        sorter = new TableRowSorter<>(modeloTabla);
        tabla.setRowSorter(sorter);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getColumnModel().getColumn(0).setMaxWidth(45);
        tabla.getColumnModel().getColumn(3).setMaxWidth(35);
        tabla.getColumnModel().getColumn(4).setMaxWidth(35);
        tabla.getColumnModel().getColumn(6).setMaxWidth(75);
        tabla.getColumnModel().getColumn(7).setMaxWidth(55);
        tabla.getColumnModel().getColumn(8).setPreferredWidth(90);
        tabla.getColumnModel().getColumn(9).setPreferredWidth(125);
        tabla.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && controlador != null) {
                    int fila = tabla.getSelectedRow();
                    if (fila == -1) return;
                    int mr = tabla.convertRowIndexToModel(fila);
                    controlador.abrirHistorialJugador((String) modeloTabla.getValueAt(mr, 1));
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(BetTheme.WHITE_10));
        scroll.getViewport().setBackground(BetTheme.SURFACE_CONTAINER_LOW);
        panel.add(scroll, BorderLayout.CENTER);

        // Leyenda
        JPanel leyenda = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        leyenda.setOpaque(false);
        leyenda.add(etiqueta(C_ACIERTO,   "✔ Acertó"));
        leyenda.add(etiqueta(C_FALLO,     "✘ Falló"));
        leyenda.add(etiqueta(C_LOCAL,     "Local"));
        leyenda.add(etiqueta(C_EMPATE,    "Empate"));
        leyenda.add(etiqueta(C_VISITANTE, "Visitante"));
        JLabel hint = new JLabel("  Doble clic = historial");
        hint.setFont(new Font("Inter", Font.PLAIN, 10));
        hint.setForeground(BetTheme.ON_SURFACE_VARIANT);
        leyenda.add(hint);
        panel.add(leyenda, BorderLayout.SOUTH);

        return panel;
    }

    // ---------------------------------------------------------------
    // SUR: barra de estado
    // ---------------------------------------------------------------
    private JPanel crearPanelEstado() {
        lblEstado = new JLabel("Listo.", SwingConstants.LEFT);
        lblEstado.setFont(new Font("Inter", Font.PLAIN, 11));
        lblEstado.setForeground(BetTheme.ON_SURFACE_VARIANT);
        lblEstado.setBorder(BorderFactory.createEmptyBorder(3, 14, 5, 14));
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BetTheme.SURFACE_CONTAINER);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BetTheme.WHITE_10));
        p.add(lblEstado, BorderLayout.CENTER);
        return p;
    }

    // ===============================================================
    // API PÚBLICA usada por PronosticoControlador
    // ===============================================================
    public void agregarListener(ActionListener l) {
        btnGuardar.addActionListener(l);
        btnActualizar.addActionListener(l);
        btnCancelarEdicion.addActionListener(l);
        btnModoOscuro.addActionListener(l);
        registrarListener(panelRaiz, l);
    }

    private void registrarListener(Container c, ActionListener l) {
        for (Component comp : c.getComponents()) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                boolean ya = false;
                for (ActionListener al : btn.getActionListeners()) if (al == l) { ya = true; break; }
                if (!ya) btn.addActionListener(l);
            }
            if (comp instanceof Container) registrarListener((Container) comp, l);
        }
    }

    // Getters formulario
    public String getNombreJugador()   { return cmbJugador.getEditor().getItem().toString().trim(); }
    public String getEquipoLocal()     { return (String) cmbLocal.getSelectedItem(); }
    public String getEquipoVisitante() { return (String) cmbVisitante.getSelectedItem(); }
    public int    getGolesLocal()      { return (int) spinGolesLocal.getValue(); }
    public int    getGolesVisitante()  { return (int) spinGolesVisitante.getValue(); }

    // Getters búsqueda/fechas
    public String        getTextoBusqueda() { return txtBuscar.getText(); }
    public java.util.Date getFechaDesde()   { return (java.util.Date) spinDesde.getValue(); }
    public java.util.Date getFechaHasta()   { return (java.util.Date) spinHasta.getValue(); }

    // Getters tabla
    public int[] getFilaSeleccionada() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) return null;
        int mr = tabla.convertRowIndexToModel(fila);
        return new int[]{ (int)modeloTabla.getValueAt(mr,0), (int)modeloTabla.getValueAt(mr,3), (int)modeloTabla.getValueAt(mr,4) };
    }
    public String getNombreJugadorDeTabla() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) return "";
        return (String) modeloTabla.getValueAt(tabla.convertRowIndexToModel(fila), 1);
    }
    public void cargarFilaEnFormulario() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) return;
        int mr = tabla.convertRowIndexToModel(fila);
        cmbJugador.getEditor().setItem(modeloTabla.getValueAt(mr,1).toString());
        cmbLocal.setSelectedItem(modeloTabla.getValueAt(mr,2));
        spinGolesLocal.setValue(modeloTabla.getValueAt(mr,3));
        spinGolesVisitante.setValue(modeloTabla.getValueAt(mr,4));
        cmbVisitante.setSelectedItem(modeloTabla.getValueAt(mr,5));
    }
    public String getContenidoTablaCSV() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            for (int j = 0; j < modeloTabla.getColumnCount(); j++) {
                sb.append(modeloTabla.getValueAt(i,j));
                if (j < modeloTabla.getColumnCount()-1) sb.append(",");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // Setters UI
    public void setTitulo(String t)             { lblTitulo.setText(t); }
    public void setEstado(String m)             { lblEstado.setText(m); }
    public JFrame getFrame()                    { return frame; }

    public void poblarTabla(List<Pronostico> lista) {
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
    public void limpiarFormulario() {
        cmbJugador.getEditor().setItem("");
        cmbLocal.setSelectedIndex(0);
        cmbVisitante.setSelectedIndex(1);
        spinGolesLocal.setValue(0);
        spinGolesVisitante.setValue(0);
    }
    public void limpiarBusqueda()                    { txtBuscar.setText(""); }
    public void deshabilitarBotonGuardar(boolean d)  { btnGuardar.setEnabled(!d); }
    public void mostrarModoEdicion(boolean edicion) {
        btnGuardar.setVisible(!edicion);
        btnActualizar.setVisible(edicion);
        btnCancelarEdicion.setVisible(edicion);
    }

    /** Alterna modo oscuro/claro. Devuelve true si quedó oscuro. */
    public boolean toggleModoOscuro() {
        modoOscuro = !modoOscuro;
        estilizarBotonModo();
        aplicarTema();
        panelRaiz.repaint();
        return modoOscuro;
    }

    // ---------------------------------------------------------------
    // Helpers de estilo
    // ---------------------------------------------------------------
    private JLabel lbl(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Inter", Font.PLAIN, 12));
        l.setForeground(BetTheme.ON_SURFACE_VARIANT);
        return l;
    }

    private JLabel etiqueta(Color color, String texto) {
        JLabel l = new JLabel("  " + texto + "  ");
        l.setOpaque(true);
        l.setBackground(color);
        l.setBorder(BorderFactory.createLineBorder(BetTheme.WHITE_10));
        l.setFont(new Font("Inter", Font.PLAIN, 10));
        l.setForeground(BetTheme.PRIMARY);
        return l;
    }

    private void estilizarInput(JComboBox<?> cb) {
        cb.setBackground(C_INPUT);
        cb.setForeground(BetTheme.PRIMARY);
        cb.setFont(new Font("Inter", Font.PLAIN, 12));
        cb.setBorder(BorderFactory.createLineBorder(C_BORDE));
        Component editor = cb.getEditor().getEditorComponent();
        editor.setBackground(C_INPUT);
        editor.setForeground(BetTheme.PRIMARY);
    }

    private void estilizarSpinner(JSpinner sp) {
        sp.setBackground(C_INPUT);
        sp.setForeground(BetTheme.PRIMARY);
        sp.setFont(new Font("Inter", Font.PLAIN, 12));
        sp.setBorder(BorderFactory.createLineBorder(C_BORDE));
        JComponent ed = sp.getEditor();
        ed.setBackground(C_INPUT);
        if (ed instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) ed).getTextField();
            tf.setBackground(C_INPUT);
            tf.setForeground(BetTheme.PRIMARY);
            tf.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        }
    }

    private void estilizarTextField(JTextField tf) {
        tf.setBackground(C_INPUT);
        tf.setForeground(BetTheme.PRIMARY);
        tf.setCaretColor(BetTheme.PRIMARY_CONTAINER);
        tf.setFont(new Font("Inter", Font.PLAIN, 12));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_BORDE),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
    }

    private JButton crearBotonSimple(String texto, String cmd) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? BetTheme.SURFACE_BRIGHT : BetTheme.SURFACE_CONTAINER_HIGH);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(BetTheme.WHITE_10);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setActionCommand(cmd);
        btn.setFont(new Font("Inter", Font.PLAIN, 12));
        btn.setForeground(BetTheme.ON_SURFACE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        botonesSimples.add(btn);
        return btn;
    }

    private JButton crearBotonColor(String texto, String cmd, Color fondo) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? fondo.darker() : fondo);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setActionCommand(cmd);
        btn.setFont(new Font("Inter", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return btn;
    }

    private void estilizarBotonModo() {
        if (modoOscuro) {
            btnModoOscuro.setText("☀ Modo Claro");
            btnModoOscuro.setForeground(new Color(255, 230, 100));
        } else {
            btnModoOscuro.setText("🌙 Modo Oscuro");
            btnModoOscuro.setForeground(new Color(180, 180, 255));
        }
    }

    private void aplicarTema() {
        Color fondo = modoOscuro ? BetTheme.SURFACE_CONTAINER : new Color(245, 245, 245);
        Color texto = modoOscuro ? BetTheme.ON_SURFACE        : Color.BLACK;
        Color input = modoOscuro ? C_INPUT                    : Color.WHITE;
        panelRaiz.setBackground(modoOscuro ? BetTheme.BACKGROUND : new Color(245,245,245));
        tabla.setBackground(modoOscuro ? BetTheme.SURFACE_CONTAINER_LOW : Color.WHITE);
        tabla.setForeground(texto);
        tabla.getTableHeader().setBackground(modoOscuro ? BetTheme.SURFACE_CONTAINER_HIGH : new Color(230,230,230));
        tabla.getTableHeader().setForeground(texto);
        lblEstado.setForeground(modoOscuro ? BetTheme.ON_SURFACE_VARIANT : Color.DARK_GRAY);
        lblTitulo.setForeground(modoOscuro ? BetTheme.PRIMARY : Color.BLACK);
        for (JButton btn : botonesSimples) btn.setForeground(texto);
    }
}
