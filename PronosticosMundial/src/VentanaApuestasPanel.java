import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * VentanaApuestas embebida como JPanel para usar dentro de BetCentralApp.
 * Mismo diseño Sky Bet, misma lógica, pero sin JFrame propio.
 * El sidebar está conectado al ComboBox — clic en equipo → selecciona en combo.
 */
public class VentanaApuestasPanel extends JPanel {

    // Formulario
    private JTextField        txtApostador;
    private JComboBox<String> cmbEqLocal;
    private JComboBox<String> cmbEqVisitante;
    private JTextField        txtGolesLocal;
    private JTextField        txtGolesVisitante;
    private JButton           btnGuardar;

    // Tabla + búsqueda
    private JTable            tabla;
    private DefaultTableModel modeloTabla;
    private JTextField        txtBuscar;

    // Estado
    private JLabel lblEstado;
    private JLabel lblContador;
    private JLabel dotLive;
    private Timer  timerLive;

    // DAO
    private final ApuestaDAO apuestaDAO;

    // Equipos
    private String[]          equiposArray  = {};
    private java.util.Set<String> equiposValidos = new java.util.HashSet<>();

    // ---------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------
    public VentanaApuestasPanel() {
        apuestaDAO = new ApuestaDAOImpl();
        cargarEquiposSync();

        setLayout(new BorderLayout(0, 0));
        setBackground(SkyBetTheme.BG_DARK);
        setOpaque(true);

        add(crearTopBar(),    BorderLayout.NORTH);
        add(crearSidebar(),   BorderLayout.WEST);
        add(crearContenido(), BorderLayout.CENTER);
        add(crearStatusBar(), BorderLayout.SOUTH);

        refrescarTabla();
        actualizarContador();
        iniciarTimerLive();
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
        p.setPreferredSize(new Dimension(0, 50));

        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        logoPanel.setOpaque(false);
        JLabel sky = new JLabel("sky");
        sky.setFont(new Font("Inter", Font.PLAIN, 18));
        sky.setForeground(Color.WHITE);
        JLabel bet = new JLabel("BET");
        bet.setFont(new Font("Inter", Font.BOLD, 18));
        bet.setForeground(SkyBetTheme.ACCENT_BLUE);
        logoPanel.add(sky); logoPanel.add(bet);

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        nav.setOpaque(false);
        nav.add(crearNavPill("Inicio",   false));
        nav.add(crearNavPill("Apuestas", true));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        left.setOpaque(false);
        left.add(logoPanel); left.add(nav);
        p.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        lblContador = new JLabel("0") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SkyBetTheme.ACCENT_BLUE);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblContador.setFont(new Font("Inter", Font.BOLD, 10));
        lblContador.setForeground(Color.WHITE);
        lblContador.setHorizontalAlignment(SwingConstants.CENTER);
        lblContador.setPreferredSize(new Dimension(22, 22));
        lblContador.setOpaque(false);

        right.add(new JLabel("🔔") {{ setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15)); }});
        right.add(lblContador);
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
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ---------------------------------------------------------------
    // SIDEBAR — grupos y equipos desde BD, conectado al ComboBox
    // ---------------------------------------------------------------
    private JPanel crearSidebar() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(SkyBetTheme.BG_SIDEBAR);
        p.setPreferredSize(new Dimension(185, 0));
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, SkyBetTheme.BORDER));

        new Thread(() -> {
            java.util.LinkedHashMap<String, java.util.List<String>> grupos = cargarGruposDesdeDB();
            SwingUtilities.invokeLater(() -> {
                p.removeAll();

                JLabel titGeneral = new JLabel("  MUNDIAL 2026");
                titGeneral.setFont(new Font("Inter", Font.BOLD, 10));
                titGeneral.setForeground(SkyBetTheme.ACCENT_BLUE);
                titGeneral.setBorder(BorderFactory.createEmptyBorder(10, 0, 8, 0));
                titGeneral.setAlignmentX(Component.LEFT_ALIGNMENT);
                p.add(titGeneral);

                JPanel contenido = new JPanel();
                contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
                contenido.setOpaque(false);

                for (java.util.Map.Entry<String, java.util.List<String>> entry : grupos.entrySet()) {
                    String grupo = entry.getKey();

                    // Header grupo — clicable para filtrar tabla
                    JPanel grupoHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
                    grupoHeader.setOpaque(false);
                    grupoHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

                    JLabel lblGrupo = new JLabel("▸  " + grupo.toUpperCase());
                    lblGrupo.setFont(new Font("Inter", Font.BOLD, 9));
                    lblGrupo.setForeground(SkyBetTheme.ACCENT_BLUE);
                    grupoHeader.add(lblGrupo);
                    grupoHeader.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    grupoHeader.addMouseListener(new MouseAdapter() {
                        public void mouseEntered(MouseEvent e) {
                            lblGrupo.setForeground(Color.WHITE);
                        }
                        public void mouseExited(MouseEvent e) {
                            lblGrupo.setForeground(SkyBetTheme.ACCENT_BLUE);
                        }
                        public void mouseClicked(MouseEvent e) {
                            // Filtrar tabla por grupo
                            if (txtBuscar != null) txtBuscar.setText(grupo);
                        }
                    });
                    contenido.add(grupoHeader);

                    // Equipos del grupo
                    for (String equipo : entry.getValue()) {
                        contenido.add(crearFilaSidebar(equipo));
                    }

                    // Separador entre grupos
                    JSeparator sep = new JSeparator();
                    sep.setForeground(SkyBetTheme.BORDER);
                    sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                    contenido.add(sep);
                }
                contenido.add(Box.createVerticalGlue());

                JScrollPane scroll = new JScrollPane(contenido,
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

    /**
     * Fila del sidebar — clic izquierdo = Equipo Local,
     * clic derecho = Equipo Visitante.
     */
    private JPanel crearFilaSidebar(String nombre) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 1));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel icon = new JLabel("⚽");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 10));

        JLabel lbl = new JLabel(nombre);
        lbl.setFont(SkyBetTheme.FONT_SMALL);
        lbl.setForeground(SkyBetTheme.TEXT_SECONDARY);

        row.add(icon); row.add(lbl);

        // Tooltip de ayuda
        row.setToolTipText("Clic izq: Local  |  Clic der: Visitante");

        row.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                lbl.setForeground(Color.WHITE);
                row.repaint();
            }
            public void mouseExited(MouseEvent e) {
                lbl.setForeground(SkyBetTheme.TEXT_SECONDARY);
                row.repaint();
            }
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    // Clic derecho → Equipo Visitante
                    if (cmbEqVisitante != null) cmbEqVisitante.setSelectedItem(nombre);
                } else {
                    // Clic izquierdo → Equipo Local
                    if (cmbEqLocal != null) cmbEqLocal.setSelectedItem(nombre);
                }
                // También actualiza el campo de búsqueda
                if (txtBuscar != null) txtBuscar.setText(nombre);
            }
        });
        return row;
    }

    private java.util.LinkedHashMap<String, java.util.List<String>> cargarGruposDesdeDB() {
        java.util.LinkedHashMap<String, java.util.List<String>> result = new java.util.LinkedHashMap<>();
        ApuestaDAOImpl dao = (ApuestaDAOImpl) apuestaDAO;
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection(
                dao.getUrl(), dao.getUser(), dao.getPass());
             java.sql.PreparedStatement ps = conn.prepareStatement(
                 "SELECT grupo, nombre FROM equipos ORDER BY grupo, nombre");
             java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                result.computeIfAbsent(rs.getString("grupo"), k -> new java.util.ArrayList<>())
                      .add(rs.getString("nombre").trim());
        } catch (java.sql.SQLException e) {
            System.err.println("[Sidebar] " + e.getMessage());
        }
        return result;
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
    // BANNER
    // ---------------------------------------------------------------
    private JPanel crearBanner() {
        JPanel banner = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x0D, 0x2A, 0x6E),
                    getWidth(), getHeight(), new Color(0x0A, 0x10, 0x2E));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(0x1A, 0x8A, 0xFF, 30));
                g2.fillOval(-40, -40, 200, 200);
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
        JLabel sub = new JLabel("Registra tus apuestas — selecciona el equipo del sidebar");
        sub.setFont(SkyBetTheme.FONT_BODY);
        sub.setForeground(SkyBetTheme.TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        texto.add(t); texto.add(Box.createVerticalStrut(6)); texto.add(sub);
        banner.add(texto, BorderLayout.WEST);

        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnWrap.setOpaque(false);
        JLabel hint = new JLabel("← Clic izq = Local  |  Clic der = Visitante");
        hint.setFont(new Font("Inter", Font.BOLD, 10));
        hint.setForeground(SkyBetTheme.ACCENT_CYAN);
        btnWrap.add(hint);
        banner.add(btnWrap, BorderLayout.EAST);
        return banner;
    }

    // ---------------------------------------------------------------
    // FORMULARIO con ComboBox conectados al sidebar
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
        card.add(crearLabel("Equipo Local  (clic izq en sidebar)"));
        card.add(crearLabel("Goles Local"));
        card.add(crearLabel("Equipo Visitante  (clic der en sidebar)"));
        card.add(crearLabel("Goles Visitante"));
        card.add(new JLabel(""));

        // Fila 2: inputs
        txtApostador      = SkyBetTheme.textField();
        cmbEqLocal        = crearComboEquipos();
        txtGolesLocal     = SkyBetTheme.textField();
        cmbEqVisitante    = crearComboEquipos();
        if (equiposArray.length > 1) cmbEqVisitante.setSelectedIndex(1);
        txtGolesVisitante = SkyBetTheme.textField();

        card.add(txtApostador);
        card.add(cmbEqLocal);
        card.add(txtGolesLocal);
        card.add(cmbEqVisitante);
        card.add(txtGolesVisitante);

        btnGuardar = SkyBetTheme.primaryButton("Registrar Apuesta");
        btnGuardar.addActionListener(e -> guardarApuesta());
        card.add(btnGuardar);

        outer.add(card, BorderLayout.CENTER);
        return outer;
    }

    // ---------------------------------------------------------------
    // TABLA
    // ---------------------------------------------------------------
    private JPanel crearSeccionTabla() {
        JPanel outer = new JPanel(new BorderLayout(0, 10));
        outer.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        JLabel titulo = new JLabel("Bet Time Games");
        titulo.setFont(SkyBetTheme.FONT_SUBTITLE);
        titulo.setForeground(Color.WHITE);
        left.add(titulo);
        left.add(crearPill("All Games",  true));
        left.add(crearPill("Live Games", false));
        left.add(crearPill("Pre-Match",  false));
        top.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);
        right.add(new JLabel("🔍") {{ setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13)); }});
        txtBuscar = SkyBetTheme.textField();
        txtBuscar.setPreferredSize(new Dimension(160, 28));
        txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { buscarApostador(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { buscarApostador(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { buscarApostador(); }
        });
        right.add(txtBuscar);
        top.add(right, BorderLayout.EAST);
        outer.add(top, BorderLayout.NORTH);

        String[] cols = {"Hora","Apostador","Local","GL","GV","Visitante","Resultado","Fecha"};
        modeloTabla = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tabla = new JTable(modeloTabla) {
            @Override public Component prepareRenderer(
                    javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? SkyBetTheme.BG_ROW : SkyBetTheme.BG_ROW_ALT);
                    c.setForeground(SkyBetTheme.TEXT_PRIMARY);
                    if (col == 6) {
                        String res = (String) modeloTabla.getValueAt(convertRowIndexToModel(row), 6);
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

    private JButton crearPill(String texto, boolean activo) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(activo ? SkyBetTheme.ACCENT_BLUE : SkyBetTheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                if (!activo) { g2.setColor(SkyBetTheme.BORDER);
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20); }
                g2.dispose(); super.paintComponent(g);
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
    // LÓGICA con validaciones
    // ---------------------------------------------------------------
    private void guardarApuesta() {
        limpiarError(txtApostador);
        limpiarError(txtGolesLocal);
        limpiarError(txtGolesVisitante);
        marcarComboError(cmbEqLocal,     false);
        marcarComboError(cmbEqVisitante, false);
        lblEstado.setForeground(SkyBetTheme.TEXT_MUTED);

        // Validación 1: Nombre
        String nombre = txtApostador.getText().trim();
        if (nombre.isEmpty()) {
            mostrarError(txtApostador, "El nombre no puede estar vacío."); return;
        }
        if (nombre.length() < 2) {
            mostrarError(txtApostador, "Mínimo 2 caracteres."); return;
        }
        if (!nombre.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ ]+")) {
            mostrarError(txtApostador, "Solo letras y espacios."); return;
        }

        // Validación 2: Equipos (del ComboBox — siempre válidos)
        String eqLocal = (String) cmbEqLocal.getSelectedItem();
        String eqVisit = (String) cmbEqVisitante.getSelectedItem();
        if (eqLocal == null || eqLocal.trim().isEmpty()) {
            marcarComboError(cmbEqLocal, true);
            lblEstado.setText("⚠  Selecciona el equipo local.");
            lblEstado.setForeground(SkyBetTheme.ACCENT_RED); return;
        }
        if (eqVisit == null || eqVisit.trim().isEmpty()) {
            marcarComboError(cmbEqVisitante, true);
            lblEstado.setText("⚠  Selecciona el equipo visitante.");
            lblEstado.setForeground(SkyBetTheme.ACCENT_RED); return;
        }
        if (eqLocal.equalsIgnoreCase(eqVisit)) {
            marcarComboError(cmbEqVisitante, true);
            lblEstado.setText("⚠  Los equipos no pueden ser iguales.");
            lblEstado.setForeground(SkyBetTheme.ACCENT_RED); return;
        }

        // Validación 3: Goles
        int golesLocal, golesVisit;
        try {
            golesLocal = Integer.parseInt(txtGolesLocal.getText().trim());
            if (golesLocal < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            mostrarError(txtGolesLocal, "Goles: número entero ≥ 0."); return;
        }
        try {
            golesVisit = Integer.parseInt(txtGolesVisitante.getText().trim());
            if (golesVisit < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            mostrarError(txtGolesVisitante, "Goles: número entero ≥ 0."); return;
        }

        // Guardar
        ApuestaModel a = new ApuestaModel(nombre, eqLocal, eqVisit, golesLocal, golesVisit);
        boolean exito = apuestaDAO.registrarApuesta(a);
        if (exito) {
            JOptionPane.showMessageDialog(this,
                "¡Apuesta registrada con éxito!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
            refrescarTabla();
            actualizarContador();
            lblEstado.setText("Apuesta guardada — ID " + a.getId());
            lblEstado.setForeground(SkyBetTheme.ACCENT_GREEN);
        } else {
            JOptionPane.showMessageDialog(this,
                "Error al registrar en BD.", "Error", JOptionPane.ERROR_MESSAGE);
        }
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
                lblEstado.setForeground(SkyBetTheme.TEXT_MUTED);
            });
        }).start();
    }

    private void refrescarTabla() {
        new Thread(() -> {
            List<ApuestaModel> lista = apuestaDAO.listarApuestas();
            SwingUtilities.invokeLater(() -> {
                poblarTabla(lista);
                lblEstado.setText("Actualizado — " + lista.size() + " apuesta(s).");
                lblEstado.setForeground(SkyBetTheme.TEXT_MUTED);
            });
        }).start();
    }

    private void poblarTabla(List<ApuestaModel> lista) {
        modeloTabla.setRowCount(0);
        for (ApuestaModel a : lista) {
            String hora = a.getFechaFormateada();
            if (hora != null && hora.length() >= 5) hora = hora.substring(0, 5);
            modeloTabla.addRow(new Object[]{
                hora, a.getNombreApostador(), a.getEquipoLocal(),
                a.getGolesLocal(), a.getGolesVisitante(), a.getEquipoVisitante(),
                a.getResultado(), a.getFechaFormateada()
            });
        }
    }

    private void actualizarContador() {
        new Thread(() -> {
            int total = apuestaDAO.contarApuestas();
            SwingUtilities.invokeLater(() -> lblContador.setText(String.valueOf(total)));
        }).start();
    }

    private void iniciarTimerLive() {
        float[] alfa = {1f}; boolean[] sub = {false};
        timerLive = new Timer(60, e -> {
            alfa[0] += sub[0] ? 0.05f : -0.05f;
            if (alfa[0] <= 0.2f) { alfa[0] = 0.2f; sub[0] = true;  }
            if (alfa[0] >= 1.0f) { alfa[0] = 1.0f; sub[0] = false; }
            if (dotLive != null) dotLive.setForeground(new Color(
                SkyBetTheme.ACCENT_RED.getRed(), SkyBetTheme.ACCENT_RED.getGreen(),
                SkyBetTheme.ACCENT_RED.getBlue(), (int)(alfa[0]*255)));
        });
        timerLive.start();
    }

    // ---------------------------------------------------------------
    // HELPERS BD y estilo
    // ---------------------------------------------------------------
    private void cargarEquiposSync() {
        java.util.Set<String>  set  = new java.util.HashSet<>();
        java.util.List<String> lista = new java.util.ArrayList<>();
        ApuestaDAOImpl dao = (ApuestaDAOImpl) apuestaDAO;
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection(
                dao.getUrl(), dao.getUser(), dao.getPass());
             java.sql.PreparedStatement ps = conn.prepareStatement(
                 "SELECT nombre FROM equipos ORDER BY grupo, nombre");
             java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String n = rs.getString("nombre").trim();
                set.add(n.toLowerCase()); lista.add(n);
            }
        } catch (java.sql.SQLException e) {
            System.err.println("[cargarEquiposSync] " + e.getMessage());
        }
        equiposValidos = set;
        equiposArray   = lista.toArray(new String[0]);
    }

    private JComboBox<String> crearComboEquipos() {
        JComboBox<String> cmb = new JComboBox<>(equiposArray);
        cmb.setBackground(SkyBetTheme.BG_INPUT);
        cmb.setForeground(SkyBetTheme.TEXT_PRIMARY);
        cmb.setFont(SkyBetTheme.FONT_BODY);
        cmb.setBorder(BorderFactory.createLineBorder(SkyBetTheme.BORDER));
        cmb.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, value, index, sel, focus);
                setBackground(sel ? SkyBetTheme.ACCENT_BLUE : SkyBetTheme.BG_INPUT);
                setForeground(sel ? Color.WHITE : SkyBetTheme.TEXT_PRIMARY);
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

    private void marcarError(JTextField tf, boolean error) {
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(error ? SkyBetTheme.ACCENT_RED : SkyBetTheme.BORDER),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
    }

    private JLabel crearLabel(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Inter", Font.BOLD, 10));
        l.setForeground(SkyBetTheme.TEXT_MUTED);
        return l;
    }
}
