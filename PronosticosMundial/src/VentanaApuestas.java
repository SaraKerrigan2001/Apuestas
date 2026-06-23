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
    private FormularioApuestaPanel formPanel;

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

    private JPanel topBarPanel;

    private final String nombreUsuarioLogueado;
    

    // ---------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------
    public VentanaApuestas(String nombreUsuarioLogueado) {
        this.nombreUsuarioLogueado = nombreUsuarioLogueado;
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
        add(new SidebarEquiposPanel(equipo -> {
            if (txtBuscar != null) txtBuscar.setText(equipo);
        }), BorderLayout.WEST);
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

    // Métodos de Sidebar extraídos a SidebarEquiposPanel.java

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
        formPanel = new FormularioApuestaPanel(nombreUsuarioLogueado, apuestaDAO, this::refrescarTabla, msg -> { lblEstado.setText(msg); lblEstado.setForeground(SkyBetTheme.ACCENT_RED); }, msg -> { lblEstado.setText(msg); lblEstado.setForeground(SkyBetTheme.ACCENT_GREEN); }); p.add(formPanel);
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
        String[] cols = {"Hora","Apostador","Local","GL","GV","Visitante","Resultado","Fecha","ID"};
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
        tabla.getColumnModel().getColumn(4).setMaxWidth(35);
        tabla.getColumnModel().getColumn(6).setMaxWidth(80);
        tabla.getColumnModel().getColumn(7).setPreferredWidth(120);
        tabla.getColumnModel().getColumn(8).setMinWidth(0);
        tabla.getColumnModel().getColumn(8).setMaxWidth(0);
        tabla.getColumnModel().getColumn(8).setWidth(0);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() != -1) {
                int row = tabla.getSelectedRow();
                String apostador = (String) tabla.getValueAt(row, 1);
                if (nombreUsuarioLogueado.equals(apostador)) {
                    formPanel.cargarDatosParaEdicion((int) tabla.getValueAt(row, 8), tabla.getValueAt(row, 2).toString(), tabla.getValueAt(row, 3).toString(), tabla.getValueAt(row, 5).toString(), tabla.getValueAt(row, 4).toString());
                } else {
                    formPanel.limpiarCampos();
                    lblEstado.setText("Solo puedes modificar tus propias apuestas.");
                    lblEstado.setForeground(SkyBetTheme.ACCENT_YELLOW);
                }
            }
        });

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

    /**
    * Carga los equipos disponibles de forma síncrona (stub implementation).
    */
    private void cargarEquiposSync() {
        // TODO: cargar equipos reales desde la base de datos.
        // Por ahora, usamos datos de ejemplo.
        this.equiposArray = new String[]{"Equipo A", "Equipo B", "Equipo C"};
    }

    /**
    * Refresca la tabla con las apuestas actuales.
    */
    public void refrescarTabla() {
        new Thread(() -> {
            List<ApuestaModel> lista = apuestaDAO.listarApuestas();
            SwingUtilities.invokeLater(() -> poblarTabla(lista));
        }).start();
    }

    private void poblarTabla(List<ApuestaModel> lista) {
        modeloTabla.setRowCount(0);
        for (ApuestaModel a : lista) {
            String hora = a.getFechaFormateada();
            if (hora != null && hora.length() >= 5) hora = hora.substring(0, 5);
            modeloTabla.addRow(new Object[]{
                hora,
                a.getNombreApostador(),
                a.getEquipoLocal(),
                a.getGolesLocal(),
                a.getGolesVisitante(),
                a.getEquipoVisitante(),
                a.getResultado(),
                a.getFechaFormateada(),
                a.getId()
            });
        }
    }

    /**
    * Inicia el temporizador que indica el estado "Live".
    */
    private void iniciarTimerLive() {
        // Cambio de color cada segundo como ejemplo simple
        timerLive = new Timer(1000, e -> {
            Color c = dotLive.getBackground();
            dotLive.setBackground(c.equals(SkyBetTheme.ACCENT_GREEN) ? SkyBetTheme.ACCENT_RED : SkyBetTheme.ACCENT_GREEN);
        });
        timerLive.start();
    }

    /**
    * Carga los equipos válidos en los combos del formulario (stub).
    */
    private void cargarEquiposValidos() {
        // El FormularioApuestaPanel manejará sus propios combos.
        // Aquí podríamos pasarle la lista de equipos si fuera necesario.
        // Por ahora, no hacemos nada.
    }

    /**
    * Busca apostadores por nombre en la tabla.
    */
    private void buscarApostador() {
        String texto = txtBuscar.getText().trim();
        new Thread(() -> {
            List<ApuestaModel> res = texto.isEmpty()
                ? apuestaDAO.listarApuestas()
                : apuestaDAO.buscarPorApostador(texto);
            SwingUtilities.invokeLater(() -> poblarTabla(res));
        }).start();
    }
}
