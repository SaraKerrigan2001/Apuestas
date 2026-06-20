import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.DefaultListCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Vista de registro de apuestas — diseño Sky Bet.
 * Layout: TopBar | Sidebar | Contenido central con banner + formulario + tabla.
 * Funcionalidad original sin validaciones (igual que el enunciado).
 * Polimorfismo: usa ApuestaDAO (interfaz) <- ApuestaDAOImpl
 */
public class VentanaApuestas extends JFrame {

    // --- Formulario ---
    private JTextField        txtApostador;
    private JComboBox<String> cmbEqLocal;
    private JComboBox<String> cmbEqVisitante;
    private JTextField        txtGolesLocal;
    private JTextField        txtGolesVisitante;
    private JButton           btnGuardar;

    // Lista de equipos cargados desde BD
    private String[] equiposArray = {};

    // Los combos se declaran aquí para poder poblarlos desde cargarEquiposValidos()
    // Se inicializan vacíos y se pueblan cuando la BD responde

    // --- Tabla + búsqueda ---
    private JTable            tabla;
    private DefaultTableModel modeloTabla;
    private JTextField        txtBuscar;

    // --- Estado ---
    private JLabel lblEstado;
    private JLabel dotLive;
    private Timer  timerLive;

    // --- DAO (Polimorfismo en acción) ---
    private ApuestaDAO apuestaDAO;

    // Referencia a la topBar para poder ocultarla
    private JPanel topBarPanel;

    // ---------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------
    public VentanaApuestas() {
        apuestaDAO = new ApuestaDAOImpl();
        cargarEquiposSync();

        setTitle("skyBET — Registro de Apuestas");
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(SkyBetTheme.BG_DARK);
        setLayout(new BorderLayout(0, 0));

        topBarPanel = crearTopBar();
        add(topBarPanel,      BorderLayout.NORTH);
        add(crearSidebar(),   BorderLayout.WEST);
        add(crearContenido(), BorderLayout.CENTER);
        add(crearStatusBar(), BorderLayout.SOUTH);

        refrescarTabla();
        iniciarTimerLive();
        cargarEquiposValidos();
    }

    // ---------------------------------------------------------------
    // TOP BAR
    // ---------------------------------------------------------------
    private JPanel crearTopBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(SkyBetTheme.BG_HEADER);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, SkyBetTheme.BORDER),
            BorderFactory.createEmptyBorder(10, 18, 10, 18)
        ));
        p.setPreferredSize(new Dimension(0, 54));

        // Logo
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        logoPanel.setOpaque(false);
        JLabel sky = new JLabel("sky");
        sky.setFont(new Font("Inter", Font.PLAIN, 20));
        sky.setForeground(Color.WHITE);
        JLabel bet = new JLabel("BET");
        bet.setFont(new Font("Inter", Font.BOLD, 20));
        bet.setForeground(SkyBetTheme.ACCENT_BLUE);
        logoPanel.add(sky);
        logoPanel.add(bet);

        // Nav pills — solo Inicio
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        nav.setOpaque(false);
        nav.add(crearNavPill("Inicio", true));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        left.setOpaque(false);
        left.add(logoPanel);
        left.add(nav);
        p.add(left, BorderLayout.WEST);

        // Derecha: botones de autenticación
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        // Botón: Cerrar Sesión
        JButton btnIngresar = crearBotonTopBar("Cerrar Sesión");
        btnIngresar.addActionListener(e -> cerrarSesion());

        right.add(btnIngresar);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private JButton crearNavPill(String texto, boolean activo) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                if (activo) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(SkyBetTheme.ACCENT_BLUE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Inter", Font.BOLD, 11));
        btn.setForeground(activo ? Color.WHITE : SkyBetTheme.TEXT_SECONDARY);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Botón Inicio: oculta/muestra la TopBar
        if ("Inicio".equals(texto)) {
            btn.addActionListener(e -> toggleTopBar());
        }
        return btn;
    }

    /** Alterna la visibilidad de la TopBar (colapsar/expandir) */
    private void toggleTopBar() {
        if (topBarPanel != null) {
            boolean visible = !topBarPanel.isVisible();
            topBarPanel.setVisible(visible);
            revalidate();
            repaint();
        }
    }

    // ---------------------------------------------------------------
    // SIDEBAR — grupos y equipos cargados desde la BD
    // ---------------------------------------------------------------
    private JPanel crearSidebar() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(SkyBetTheme.BG_SIDEBAR);
        p.setPreferredSize(new Dimension(185, 0));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, SkyBetTheme.BORDER),
            BorderFactory.createEmptyBorder(10, 0, 14, 0)
        ));

        // Cargar desde BD en hilo separado
        new Thread(() -> {
            java.util.LinkedHashMap<String, java.util.List<String>> grupos = cargarGruposDesdeDB();
            SwingUtilities.invokeLater(() -> {
                p.removeAll();

                // Título general
                JLabel titGeneral = new JLabel("MUNDIAL 2026");
                titGeneral.setFont(new Font("Inter", Font.BOLD, 10));
                titGeneral.setForeground(SkyBetTheme.ACCENT_BLUE);
                titGeneral.setBorder(BorderFactory.createEmptyBorder(4, 14, 8, 14));
                titGeneral.setAlignmentX(Component.LEFT_ALIGNMENT);
                p.add(titGeneral);

                // Scroll con todos los grupos
                JPanel contenidoGrupos = new JPanel();
                contenidoGrupos.setLayout(new BoxLayout(contenidoGrupos, BoxLayout.Y_AXIS));
                contenidoGrupos.setOpaque(false);

                for (java.util.Map.Entry<String, java.util.List<String>> entry : grupos.entrySet()) {
                    String grupo = entry.getKey();
                    java.util.List<String> equipos = entry.getValue();

                    // Header del grupo
                    JPanel grupoHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
                    grupoHeader.setOpaque(false);
                    grupoHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

                    JLabel lblGrupo = new JLabel("▸  " + grupo.toUpperCase());
                    lblGrupo.setFont(new Font("Inter", Font.BOLD, 9));
                    lblGrupo.setForeground(SkyBetTheme.TEXT_MUTED);
                    grupoHeader.add(lblGrupo);
                    contenidoGrupos.add(grupoHeader);

                    // Equipos del grupo
                    for (String equipo : equipos) {
                        JPanel row = crearFilaSidebar("⚽", equipo);
                        contenidoGrupos.add(row);
                    }
                    contenidoGrupos.add(crearDivider());
                }
                contenidoGrupos.add(Box.createVerticalGlue());

                JScrollPane scroll = new JScrollPane(contenidoGrupos,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                scroll.setBorder(null);
                scroll.getViewport().setBackground(SkyBetTheme.BG_SIDEBAR);
                scroll.getVerticalScrollBar().setUnitIncrement(10);
                scroll.getVerticalScrollBar().setPreferredSize(new Dimension(3, 0));
                scroll.setOpaque(false);
                scroll.getViewport().setOpaque(false);

                p.add(scroll);
                p.revalidate();
                p.repaint();
            });
        }).start();

        return p;
    }

    /** Consulta la BD y devuelve grupos con sus equipos ordenados */
    private java.util.LinkedHashMap<String, java.util.List<String>> cargarGruposDesdeDB() {
        java.util.LinkedHashMap<String, java.util.List<String>> result = new java.util.LinkedHashMap<>();
        String sql = "SELECT grupo, nombre FROM equipos ORDER BY grupo, nombre";

        // Reutiliza las credenciales cargadas por ApuestaDAOImpl
        ApuestaDAOImpl dao = (ApuestaDAOImpl) apuestaDAO;
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection(
                dao.getUrl(), dao.getUser(), dao.getPass());
             java.sql.PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String grupo  = rs.getString("grupo");
                String equipo = rs.getString("nombre");
                result.computeIfAbsent(grupo, k -> new java.util.ArrayList<>()).add(equipo);
            }

        } catch (java.sql.SQLException e) {
            System.err.println("[Sidebar] Error cargando grupos: " + e.getMessage());
            // Fallback: sin datos
        }
        return result;
    }

    private JPanel crearFilaSidebar(String icono, String nombre) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 1));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JLabel icon = new JLabel(icono);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 10));
        JLabel lbl = new JLabel(nombre);
        lbl.setFont(SkyBetTheme.FONT_SMALL);
        lbl.setForeground(SkyBetTheme.TEXT_SECONDARY);
        row.add(icon); row.add(lbl);
        row.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                lbl.setForeground(Color.WHITE); row.repaint();
            }
            public void mouseExited(MouseEvent e) {
                lbl.setForeground(SkyBetTheme.TEXT_SECONDARY); row.repaint();
            }
            public void mouseClicked(MouseEvent e) {
                // Al hacer clic filtra la tabla por ese equipo
                if (txtBuscar != null) txtBuscar.setText(nombre);
            }
        });
        return row;
    }

    private JSeparator crearDivider() {
        JSeparator s = new JSeparator();
        s.setForeground(SkyBetTheme.BORDER);
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return s;
    }

    // ---------------------------------------------------------------
    // CONTENIDO CENTRAL
    // ---------------------------------------------------------------
    private JPanel crearContenido() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(SkyBetTheme.BG_DARK);
        JScrollPane scroll = new JScrollPane(crearMainContent(),
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(SkyBetTheme.BG_DARK);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(4, 0));
        p.add(scroll);
        return p;
    }

    private JPanel crearMainContent() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(SkyBetTheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(14, 16, 40, 16));
        p.add(crearBanner());
        p.add(Box.createVerticalStrut(16));
        p.add(crearSeccionFormulario());
        p.add(Box.createVerticalStrut(16));
        p.add(crearSeccionTabla());
        return p;
    }

    // ---------------------------------------------------------------
    // BANNER hero
    // ---------------------------------------------------------------
    private JPanel crearBanner() {
        JPanel banner = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                    0, 0,             new Color(0x0D, 0x2A, 0x6E),
                    getWidth(), getHeight(), new Color(0x0A, 0x10, 0x2E));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(0x1A, 0x8A, 0xFF, 30));
                g2.fillOval(-40, -40, 200, 200);
                g2.setColor(new Color(0x00, 0xD4, 0xFF, 20));
                g2.fillOval(getWidth() - 120, getHeight() - 80, 200, 200);
                g2.dispose();
            }
        };
        banner.setOpaque(false);
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        banner.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel texto = new JPanel();
        texto.setLayout(new BoxLayout(texto, BoxLayout.Y_AXIS));
        texto.setOpaque(false);

        JLabel t = new JLabel("BET ON MUNDIAL 2026");
        t.setFont(new Font("Inter", Font.BOLD, 24));
        t.setForeground(Color.WHITE);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Registra tus apuestas y sigue el marcador en tiempo real");
        sub.setFont(SkyBetTheme.FONT_BODY);
        sub.setForeground(SkyBetTheme.TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        texto.add(t);
        texto.add(Box.createVerticalStrut(6));
        texto.add(sub);
        banner.add(texto, BorderLayout.WEST);

        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnWrap.setOpaque(false);
        btnWrap.add(SkyBetTheme.primaryButton("Get Bonus"));
        banner.add(btnWrap, BorderLayout.EAST);
        return banner;
    }

    // ---------------------------------------------------------------
    // FORMULARIO
    // ---------------------------------------------------------------
    private JPanel crearSeccionFormulario() {
        JPanel outer = new JPanel(new BorderLayout(0, 10));
        outer.setOpaque(false);
        outer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 135));

        JPanel tituloRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tituloRow.setOpaque(false);
        dotLive = new JLabel("●");
        dotLive.setFont(new Font("Dialog", Font.BOLD, 10));
        dotLive.setForeground(SkyBetTheme.ACCENT_RED);
        JLabel tituloSec = new JLabel("Registrar Apuesta");
        tituloSec.setFont(SkyBetTheme.FONT_SUBTITLE);
        tituloSec.setForeground(Color.WHITE);
        JLabel badge = SkyBetTheme.badge("LIVE", SkyBetTheme.ACCENT_RED, Color.WHITE);
        tituloRow.add(dotLive); tituloRow.add(tituloSec); tituloRow.add(badge);
        outer.add(tituloRow, BorderLayout.NORTH);

        JPanel card = new JPanel(new GridLayout(2, 6, 8, 8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SkyBetTheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(SkyBetTheme.BORDER);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        // Fila 1: labels
        card.add(crearLabel("Nombre del Apostador"));
        card.add(crearLabel("Equipo Local"));
        card.add(crearLabel("Goles Local"));
        card.add(crearLabel("Equipo Visitante"));
        card.add(crearLabel("Goles Visitante"));
        card.add(new JLabel(""));

        // Fila 2: inputs + botón (igual que el original)
        txtApostador      = SkyBetTheme.textField();
        txtGolesLocal     = SkyBetTheme.textField();
        txtGolesVisitante = SkyBetTheme.textField();

        // ComboBox de equipos — se pueblan desde la BD
        cmbEqLocal     = crearComboEquipos();
        cmbEqVisitante = crearComboEquipos();
        if (equiposArray.length > 1) cmbEqVisitante.setSelectedIndex(1);

        card.add(txtApostador);
        card.add(cmbEqLocal);
        card.add(txtGolesLocal);
        card.add(cmbEqVisitante);
        card.add(txtGolesVisitante);

        btnGuardar = SkyBetTheme.primaryButton("Registrar Apuesta");
        btnGuardar.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { guardarApuesta(); }
        });
        card.add(btnGuardar);

        outer.add(card, BorderLayout.CENTER);
        return outer;
    }

    // ---------------------------------------------------------------
    // TABLA — Bet Time Games
    // ---------------------------------------------------------------
    private JPanel crearSeccionTabla() {
        JPanel outer = new JPanel(new BorderLayout(0, 10));
        outer.setOpaque(false);

        // Título + filtros + búsqueda
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        JLabel titulo = new JLabel("Bet Time Games");
        titulo.setFont(SkyBetTheme.FONT_SUBTITLE);
        titulo.setForeground(Color.WHITE);
        left.add(titulo);
        left.add(crearPillFiltro("All Games",  true));
        left.add(crearPillFiltro("Live Games", false));
        left.add(crearPillFiltro("Pre-Match",  false));
        top.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);
        JLabel buscarIco = new JLabel("🔍");
        buscarIco.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        txtBuscar = SkyBetTheme.textField();
        txtBuscar.setPreferredSize(new Dimension(160, 28));
        txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { buscarApostador(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { buscarApostador(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { buscarApostador(); }
        });
        right.add(buscarIco); right.add(txtBuscar);
        top.add(right, BorderLayout.EAST);
        outer.add(top, BorderLayout.NORTH);

        // Tabla
        String[] cols = {"Hora","Apostador","Local","GL","GV","Visitante","Resultado","Fecha"};
        modeloTabla = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tabla = new JTable(modeloTabla) {
            @Override public Component prepareRenderer(
                    javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0
                        ? SkyBetTheme.BG_ROW : SkyBetTheme.BG_ROW_ALT);
                    c.setForeground(SkyBetTheme.TEXT_PRIMARY);
                    if (col == 6) {
                        String res = (String) modeloTabla.getValueAt(
                            convertRowIndexToModel(row), 6);
                        c.setForeground("Local".equals(res)     ? SkyBetTheme.ACCENT_GREEN
                                      : "Visitante".equals(res) ? SkyBetTheme.ACCENT_BLUE
                                      : SkyBetTheme.ACCENT_YELLOW);
                    }
                }
                return c;
            }
        };
        tabla.setBackground(SkyBetTheme.BG_ROW);
        tabla.setForeground(SkyBetTheme.TEXT_PRIMARY);
        tabla.setGridColor(SkyBetTheme.BORDER);
        tabla.setRowHeight(30);
        tabla.setShowHorizontalLines(true);
        tabla.setShowVerticalLines(false);
        tabla.setFont(SkyBetTheme.FONT_BODY);
        tabla.setSelectionBackground(new Color(0x1A, 0x8A, 0xFF, 80));
        tabla.setSelectionForeground(Color.WHITE);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setBackground(SkyBetTheme.BG_PANEL);
        tabla.getTableHeader().setForeground(SkyBetTheme.TEXT_MUTED);
        tabla.getTableHeader().setFont(new Font("Inter", Font.BOLD, 10));
        tabla.getColumnModel().getColumn(0).setMaxWidth(55);
        tabla.getColumnModel().getColumn(3).setMaxWidth(35);
        tabla.getColumnModel().getColumn(4).setMaxWidth(35);
        tabla.getColumnModel().getColumn(6).setMaxWidth(80);
        tabla.getColumnModel().getColumn(7).setPreferredWidth(120);

        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i : new int[]{0, 3, 4, 6})
            tabla.getColumnModel().getColumn(i).setCellRenderer(centrado);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(SkyBetTheme.BORDER));
        scroll.getViewport().setBackground(SkyBetTheme.BG_ROW);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(4, 0));
        outer.add(scroll, BorderLayout.CENTER);
        return outer;
    }

    private JButton crearPillFiltro(String texto, boolean activo) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(activo ? SkyBetTheme.ACCENT_BLUE : SkyBetTheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                if (!activo) {
                    g2.setColor(SkyBetTheme.BORDER);
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Inter", Font.BOLD, 10));
        btn.setForeground(activo ? Color.WHITE : SkyBetTheme.TEXT_SECONDARY);
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ---------------------------------------------------------------
    // STATUS BAR
    // ---------------------------------------------------------------
    private JPanel crearStatusBar() {
        lblEstado = new JLabel("Listo.");
        lblEstado.setFont(new Font("Inter", Font.PLAIN, 11));
        lblEstado.setForeground(SkyBetTheme.TEXT_MUTED);
        lblEstado.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(SkyBetTheme.BG_HEADER);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, SkyBetTheme.BORDER));
        p.add(lblEstado, BorderLayout.CENTER);
        return p;
    }

    // ---------------------------------------------------------------
    // LÓGICA — validaciones con mensaje "debe registrarse primero"
    // ---------------------------------------------------------------
    private void guardarApuesta() {
        // Reset errores visuales
        limpiarError(txtApostador);
        limpiarError(txtGolesLocal);
        limpiarError(txtGolesVisitante);
        marcarComboError(cmbEqLocal,     false);
        marcarComboError(cmbEqVisitante, false);
        lblEstado.setForeground(SkyBetTheme.TEXT_MUTED);

        // Acumular mensajes de error
        java.util.List<String> errores = new java.util.ArrayList<>();

        // --- Validación 1: Nombre del apostador ---
        String nombre = txtApostador.getText().trim();
        if (nombre.isEmpty() || nombre.length() < 2) {
            marcarError(txtApostador, true);
            errores.add("• Nombre del Apostador: campo vacío o muy corto (mín. 2 caracteres)");
        } else if (!nombre.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ ]+")) {
            marcarError(txtApostador, true);
            errores.add("• Nombre del Apostador: solo se permiten letras y espacios");
        } else {
            marcarError(txtApostador, false);
        }

        // --- Validación 2: Equipos ---
        String eqLocal = (String) cmbEqLocal.getSelectedItem();
        String eqVisit = (String) cmbEqVisitante.getSelectedItem();
        if (eqLocal == null || eqLocal.trim().isEmpty()) {
            marcarComboError(cmbEqLocal, true);
            errores.add("• Equipo Local: debe seleccionar un equipo");
        } else {
            marcarComboError(cmbEqLocal, false);
        }
        if (eqVisit == null || eqVisit.trim().isEmpty()) {
            marcarComboError(cmbEqVisitante, true);
            errores.add("• Equipo Visitante: debe seleccionar un equipo");
        } else if (eqLocal != null && eqLocal.equalsIgnoreCase(eqVisit)) {
            marcarComboError(cmbEqVisitante, true);
            errores.add("• Equipo Visitante: no puede ser igual al equipo local");
        } else {
            marcarComboError(cmbEqVisitante, false);
        }

        // --- Validación 3: Goles ---
        int golesLocal = -1, golesVisit = -1;
        try {
            golesLocal = Integer.parseInt(txtGolesLocal.getText().trim());
            if (golesLocal < 0) throw new NumberFormatException();
            marcarError(txtGolesLocal, false);
        } catch (NumberFormatException ex) {
            marcarError(txtGolesLocal, true);
            errores.add("• Goles Local: debe ser un número entero igual o mayor a 0");
        }
        try {
            golesVisit = Integer.parseInt(txtGolesVisitante.getText().trim());
            if (golesVisit < 0) throw new NumberFormatException();
            marcarError(txtGolesVisitante, false);
        } catch (NumberFormatException ex) {
            marcarError(txtGolesVisitante, true);
            errores.add("• Goles Visitante: debe ser un número entero igual o mayor a 0");
        }

        // Si hay errores → mostrar diálogo con el mensaje requerido
        if (!errores.isEmpty()) {
            mostrarDialogoValidacion(errores);
            lblEstado.setText("⚠  Corrija los errores antes de registrar.");
            lblEstado.setForeground(SkyBetTheme.ACCENT_RED);
            return;
        }

        // --- Todo válido: guardar ---
        ApuestaModel nuevaApuesta = new ApuestaModel(
                nombre, eqLocal, eqVisit, golesLocal, golesVisit);

        boolean exito = apuestaDAO.registrarApuesta(nuevaApuesta);
        if (exito) {
            JOptionPane.showMessageDialog(this,
                "¡Apuesta registrada con éxito!",
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
            refrescarTabla();
            lblEstado.setText("Apuesta guardada.");
            lblEstado.setForeground(SkyBetTheme.ACCENT_GREEN);
        } else {
            JOptionPane.showMessageDialog(this,
                "Error al registrar en BD.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Muestra un diálogo de error estilo Sky Bet con el mensaje principal
     * "Invalidación incorrecta — debe registrarse primero antes de apostar"
     * y la lista de campos con error.
     */
    private void mostrarDialogoValidacion(java.util.List<String> errores) {
        // Panel del diálogo con colores SkyBet
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(SkyBetTheme.BG_CARD);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // Ícono + mensaje principal
        JPanel cabecera = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        cabecera.setBackground(SkyBetTheme.BG_CARD);

        JLabel icono = new JLabel("⚠");
        icono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        icono.setForeground(SkyBetTheme.ACCENT_YELLOW);

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setBackground(SkyBetTheme.BG_CARD);

        JLabel titulo = new JLabel("Invalidación incorrecta");
        titulo.setFont(new Font("Inter", Font.BOLD, 15));
        titulo.setForeground(SkyBetTheme.ACCENT_RED);

        JLabel subtitulo = new JLabel("Debe registrarse primero antes de apostar.");
        subtitulo.setFont(new Font("Inter", Font.PLAIN, 12));
        subtitulo.setForeground(SkyBetTheme.TEXT_SECONDARY);

        textos.add(titulo);
        textos.add(Box.createVerticalStrut(3));
        textos.add(subtitulo);

        cabecera.add(icono);
        cabecera.add(textos);
        panel.add(cabecera, BorderLayout.NORTH);

        // Lista de errores específicos
        JPanel listaPanel = new JPanel();
        listaPanel.setLayout(new BoxLayout(listaPanel, BoxLayout.Y_AXIS));
        listaPanel.setBackground(new Color(SkyBetTheme.BG_DARK.getRed(),
            SkyBetTheme.BG_DARK.getGreen(), SkyBetTheme.BG_DARK.getBlue()));
        listaPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SkyBetTheme.BORDER),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        for (String err : errores) {
            JLabel lbl = new JLabel(err);
            lbl.setFont(new Font("Inter", Font.PLAIN, 11));
            lbl.setForeground(SkyBetTheme.ACCENT_RED);
            lbl.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
            listaPanel.add(lbl);
        }
        panel.add(listaPanel, BorderLayout.CENTER);

        // Botón Cerrar
        JButton btnCerrar = new JButton("Entendido") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()
                    ? SkyBetTheme.ACCENT_BLUE.darker() : SkyBetTheme.ACCENT_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnCerrar.setFont(new Font("Inter", Font.BOLD, 12));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setOpaque(false);
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setBorderPainted(false);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 24));
        btnCerrar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setBackground(SkyBetTheme.BG_CARD);
        btnPanel.add(btnCerrar);
        panel.add(btnPanel, BorderLayout.SOUTH);

        // Crear JDialog personalizado
        JDialog dialog = new JDialog(this, "Error de Validación", true);
        dialog.setUndecorated(false);
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(420, 0));
        dialog.setLocationRelativeTo(this);
        btnCerrar.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void limpiarCampos() {
        txtApostador.setText("");
        if (cmbEqLocal.getItemCount() > 0)     cmbEqLocal.setSelectedIndex(0);
        if (cmbEqVisitante.getItemCount() > 1) cmbEqVisitante.setSelectedIndex(1);
        txtGolesLocal.setText("");
        txtGolesVisitante.setText("");
        limpiarError(txtApostador);
        limpiarError(txtGolesLocal);
        limpiarError(txtGolesVisitante);
        marcarComboError(cmbEqLocal,     false);
        marcarComboError(cmbEqVisitante, false);
        lblEstado.setForeground(SkyBetTheme.TEXT_MUTED);
        lblEstado.setText("Listo.");
    }

    private void buscarApostador() {
        String texto = txtBuscar.getText().trim();
        new Thread(() -> {
            List<ApuestaModel> res = texto.isEmpty()
                ? apuestaDAO.listarApuestas()
                : apuestaDAO.buscarPorApostador(texto);
            SwingUtilities.invokeLater(() -> {
                poblarTabla(res);
                lblEstado.setText(res.size() + " resultado(s).");
            });
        }).start();
    }

    private void refrescarTabla() {
        new Thread(() -> {
            List<ApuestaModel> lista = apuestaDAO.listarApuestas();
            SwingUtilities.invokeLater(() -> {
                poblarTabla(lista);
                lblEstado.setText("Actualizado — " + lista.size() + " apuesta(s).");
            });
        }).start();
    }

    private void poblarTabla(List<ApuestaModel> lista) {
        modeloTabla.setRowCount(0);
        for (ApuestaModel a : lista) {
            String hora = a.getFechaFormateada();
            if (hora != null && hora.length() >= 5)
                hora = hora.substring(0, 5);
            modeloTabla.addRow(new Object[]{
                hora,
                a.getNombreApostador(),
                a.getEquipoLocal(),
                a.getGolesLocal(),
                a.getGolesVisitante(),
                a.getEquipoVisitante(),
                a.getResultado(),
                a.getFechaFormateada()
            });
        }
    }

    private void iniciarTimerLive() {
        float[] alfa = {1f};
        boolean[] sub = {false};
        timerLive = new Timer(60, e -> {
            alfa[0] += sub[0] ? 0.05f : -0.05f;
            if (alfa[0] <= 0.2f) { alfa[0] = 0.2f; sub[0] = true;  }
            if (alfa[0] >= 1.0f) { alfa[0] = 1.0f; sub[0] = false; }
            if (dotLive != null)
                dotLive.setForeground(new Color(
                    SkyBetTheme.ACCENT_RED.getRed(),
                    SkyBetTheme.ACCENT_RED.getGreen(),
                    SkyBetTheme.ACCENT_RED.getBlue(),
                    (int)(alfa[0] * 255)));
        });
        timerLive.start();
    }

    // Lista de equipos válidos cargada desde la BD
    private java.util.Set<String> equiposValidos = new java.util.HashSet<>();

    /**
     * Carga equipos de forma SINCRÓNICA desde la BD.
     * Se llama ANTES de construir la UI para que los combos ya tengan datos.
     */
    private void cargarEquiposSync() {
        java.util.Set<String>  set   = new java.util.HashSet<>();
        java.util.List<String> lista = new java.util.ArrayList<>();
        ApuestaDAOImpl dao = (ApuestaDAOImpl) apuestaDAO;
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection(
                dao.getUrl(), dao.getUser(), dao.getPass());
             java.sql.PreparedStatement ps = conn.prepareStatement(
                 "SELECT nombre FROM equipos ORDER BY grupo, nombre");
             java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String nombre = rs.getString("nombre").trim();
                set.add(nombre.toLowerCase());
                lista.add(nombre);
            }
        } catch (java.sql.SQLException e) {
            System.err.println("[cargarEquiposSync] " + e.getMessage());
        }
        equiposValidos = set;
        equiposArray   = lista.toArray(new String[0]);
    }

    /** Recarga asíncrona para refrescar el set de validación */
    private void cargarEquiposValidos() {        new Thread(() -> {
            java.util.Set<String>  set    = new java.util.HashSet<>();
            java.util.List<String> lista  = new java.util.ArrayList<>();
            ApuestaDAOImpl dao = (ApuestaDAOImpl) apuestaDAO;
            try (java.sql.Connection conn = java.sql.DriverManager.getConnection(
                    dao.getUrl(), dao.getUser(), dao.getPass());
                 java.sql.PreparedStatement ps = conn.prepareStatement(
                     "SELECT nombre FROM equipos ORDER BY grupo, nombre");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nombre = rs.getString("nombre").trim();
                    set.add(nombre.toLowerCase());
                    lista.add(nombre);
                }
            } catch (java.sql.SQLException e) {
                System.err.println("[Validacion] Error cargando equipos: " + e.getMessage());
            }
            SwingUtilities.invokeLater(() -> {
                equiposValidos = set;
                equiposArray   = lista.toArray(new String[0]);
                // Poblar ComboBox
                if (cmbEqLocal != null && cmbEqVisitante != null) {
                    poblarCombo(cmbEqLocal,     equiposArray);
                    poblarCombo(cmbEqVisitante, equiposArray);
                    if (cmbEqVisitante.getItemCount() > 1)
                        cmbEqVisitante.setSelectedIndex(1);
                }
            });
        }).start();
    }

    private void poblarCombo(JComboBox<String> cmb, String[] items) {
        cmb.removeAllItems();
        for (String item : items) cmb.addItem(item);
    }

    private JComboBox<String> crearComboEquipos() {
        JComboBox<String> cmb = new JComboBox<>(equiposArray);
        cmb.setBackground(SkyBetTheme.BG_INPUT);
        cmb.setForeground(SkyBetTheme.TEXT_PRIMARY);
        cmb.setFont(SkyBetTheme.FONT_BODY);
        cmb.setBorder(BorderFactory.createLineBorder(SkyBetTheme.BORDER));
        cmb.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? SkyBetTheme.ACCENT_BLUE : SkyBetTheme.BG_INPUT);
                setForeground(isSelected ? Color.WHITE : SkyBetTheme.TEXT_PRIMARY);
                setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
                return this;
            }
        });
        return cmb;
    }

    private void marcarComboError(JComboBox<String> cmb, boolean error) {
        cmb.setBorder(BorderFactory.createLineBorder(
            error ? SkyBetTheme.ACCENT_RED : SkyBetTheme.BORDER));
    }

    /** Marca un campo con borde rojo (error) o normal */
    private void marcarError(JTextField tf, boolean error) {
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(error ? SkyBetTheme.ACCENT_RED : SkyBetTheme.BORDER),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
    }

    /** Muestra tooltip de error junto al campo */
    private void mostrarError(JTextField tf, String msg) {
        marcarError(tf, true);
        tf.setToolTipText(msg);
        tf.requestFocus();
        lblEstado.setText("⚠  " + msg);
        lblEstado.setForeground(SkyBetTheme.ACCENT_RED);
    }

    private void limpiarError(JTextField tf) {
        marcarError(tf, false);
        tf.setToolTipText(null);
    }

    // ---------------------------------------------------------------
    // HELPERS de estilo
    // ---------------------------------------------------------------
    private JLabel crearLabel(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Inter", Font.BOLD, 10));
        l.setForeground(SkyBetTheme.TEXT_MUTED);
        return l;
    }

    // Botón estilizado para TopBar
    private JButton crearBotonTopBar(String texto) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SkyBetTheme.ACCENT_GREEN);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Inter", Font.BOLD, 11));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ---------------------------------------------------------------
    // Cerrar sesión — vuelve a VentanaAuth
    // ---------------------------------------------------------------
    private void cerrarSesion() {
        int conf = JOptionPane.showConfirmDialog(this,
            "¿Deseas cerrar sesión?",
            "Cerrar Sesión", JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        if (conf == JOptionPane.YES_OPTION) {
            new VentanaAuth().setVisible(true);
            dispose();
        }
    }

    // Métodos de autenticación
    private void abrirDialogoIngresar() {
        VentanaAuth auth = new VentanaAuth();
        auth.setVisible(true);
        ToastNotificacion.mostrar(this, "Ingresa a tu cuenta", ToastNotificacion.Tipo.INFO);
    }

    // Ventana de notificaciones
    private void mostrarVentanaNotificaciones() {
        JFrame notifFrame = new JFrame("Notificaciones");
        notifFrame.setSize(400, 300);
        notifFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        notifFrame.setLocationRelativeTo(this);
        notifFrame.getContentPane().setBackground(SkyBetTheme.BG_DARK);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(SkyBetTheme.BG_DARK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel titulo = new JLabel("Notificaciones");
        titulo.setFont(new Font("Inter", Font.BOLD, 16));
        titulo.setForeground(SkyBetTheme.TEXT_PRIMARY);
        mainPanel.add(titulo, BorderLayout.NORTH);

        // Panel de notificaciones
        JPanel notifPanel = new JPanel();
        notifPanel.setLayout(new BoxLayout(notifPanel, BoxLayout.Y_AXIS));
        notifPanel.setBackground(SkyBetTheme.BG_DARK);

        // Agregar notificaciones de ejemplo
        agregarNotificacion(notifPanel, "✔ Nueva apuesta registrada", SkyBetTheme.ACCENT_GREEN);
        agregarNotificacion(notifPanel, "⚠ Tu saldo es bajo", new Color(200, 150, 0));
        agregarNotificacion(notifPanel, "🎉 Ganaste una apuesta", SkyBetTheme.ACCENT_BLUE);
        agregarNotificacion(notifPanel, "ℹ Actualización disponible", SkyBetTheme.ACCENT_BLUE);

        JScrollPane scroll = new JScrollPane(notifPanel);
        scroll.getViewport().setBackground(SkyBetTheme.BG_DARK);
        scroll.setBorder(null);
        mainPanel.add(scroll, BorderLayout.CENTER);

        notifFrame.add(mainPanel);
        notifFrame.setVisible(true);
    }

    private void agregarNotificacion(JPanel panel, String mensaje, Color color) {
        JPanel notifItem = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SkyBetTheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        notifItem.setOpaque(false);
        notifItem.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        notifItem.setPreferredSize(new Dimension(0, 50));
        notifItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel lblMensaje = new JLabel(mensaje);
        lblMensaje.setFont(new Font("Inter", Font.PLAIN, 12));
        lblMensaje.setForeground(SkyBetTheme.TEXT_PRIMARY);
        notifItem.add(lblMensaje, BorderLayout.CENTER);

        panel.add(notifItem);
        panel.add(Box.createVerticalStrut(8));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaApuestas().setVisible(true));
    }
}
