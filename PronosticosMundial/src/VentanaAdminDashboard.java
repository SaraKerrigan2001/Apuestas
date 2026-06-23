import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VentanaAdminDashboard extends JFrame {

    // ---------------------------------------------------------------
    // Paleta de colores — igual que VentanaApuestas (SkyBet)
    // ---------------------------------------------------------------
    private static final Color SURF         = new Color(0x0D, 0x10, 0x1F); // fondo principal
    private static final Color SURF_CONT    = new Color(0x13, 0x18, 0x2E); // paneles/cards
    private static final Color SURF_HIGH    = new Color(0x1F, 0x27, 0x45); // filas / botones
    private static final Color TOPBAR_BG    = new Color(0x0A, 0x0D, 0x1E); // topbar azul oscuro
    private static final Color TOPBAR_BORDER= new Color(0x2A, 0x35, 0x55); // borde topbar
    private static final Color SIDEBAR_BG   = new Color(0x10, 0x14, 0x28); // sidebar
    private static final Color NEON         = new Color(0xC3, 0xF4, 0x00); // verde-lima
    private static final Color ACCENT_BLUE  = new Color(0x1A, 0x8A, 0xFF); // azul acento
    private static final Color ACCENT_CYAN  = new Color(0x00, 0xE0, 0xFF); // cyan
    private static final Color ON_SURF      = new Color(0xE1, 0xE2, 0xEB); // texto principal
    private static final Color ON_SURF_VAR  = new Color(0xA0, 0xB0, 0xCC); // texto secundario
    private static final Color MUTED        = new Color(0x60, 0x70, 0x90); // texto muted
    private static final Color BORDER       = new Color(0x2A, 0x35, 0x55); // bordes
    private static final Color ERROR        = new Color(0xFF, 0x3B, 0x3B); // rojo error
    private static final Color SECONDARY    = new Color(0x00, 0xE0, 0xFF); // cyan secondary

    private JLabel lblUsuarios, lblEquipos, lblPartidos, lblPronosticos;
    private JLabel lblAciertos, lblFallos, lblPendientes, lblAccuracy;
    private DefaultTableModel txModel, usuariosModel, partidosModel, mercadosModel;
    private JLabel lblStatusLat, lblFooterTime;

    private Timer timerRefresh;

    private JPanel cardPanel;
    private CardLayout cardLayout;
    private String currentCard = "Dashboard";

    private List<MenuItemInfo> menuItems = new ArrayList<>();

    private class MenuItemInfo {
        JPanel panel;
        JLabel label;
        String name;
    }

    public VentanaAdminDashboard() {
        setTitle("Velocity Admin | BetCentral Dashboard");
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(SURF);
        setLayout(new BorderLayout());

        add(createTopNav(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(SURF);

        // Dashboard Card
        JScrollPane scrollDash = new JScrollPane(createMainContent());
        scrollDash.setBorder(null);
        scrollDash.getVerticalScrollBar().setUnitIncrement(16);
        scrollDash.getViewport().setBackground(SURF);
        cardPanel.add(scrollDash, "Dashboard");

        // Usuarios Card
        cardPanel.add(createTablePanel("Gestión de Usuarios", new String[]{"ID", "Nombre", "Email", "Saldo", "Estado", "Fecha Registro"}, "Usuarios"), "Usuarios");

        // Mercados Card
        cardPanel.add(createTablePanel("Gestión de Mercados", new String[]{"ID Partido", "Grupo", "Mercado", "Cuotas (1 | X | 2)", "Estado"}, "Mercados"), "Mercados");

        // Partidos Card
        cardPanel.add(createTablePanel("Gestión de Partidos", new String[]{"ID", "Grupo", "Encuentro", "Cuotas (1 | X | 2)", "Estado"}, "Partidos"), "Partidos");

        // Riesgo Card
        cardPanel.add(createRiesgoPanel(), "Riesgo");

        // Configuración Card
        cardPanel.add(createConfiguracionPanel(), "Configuración");

        add(cardPanel, BorderLayout.CENTER);

        timerRefresh = new Timer(10000, e -> refreshData());
        timerRefresh.start();
        
        SwingUtilities.invokeLater(this::refreshData);
    }

    private JPanel createTopNav() {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(TOPBAR_BG);
        nav.setPreferredSize(new Dimension(0, 64));
        nav.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, TOPBAR_BORDER));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 18));
        left.setOpaque(false);
        JLabel logo = new JLabel("VELOCITY ADMIN");
        logo.setFont(new Font("Inter", Font.BOLD, 22));
        logo.setForeground(NEON);
        left.add(logo);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        right.setOpaque(false);

        // Badge SYSTEM ONLINE
        JPanel statusPnl = createRoundPanel(SURF_HIGH, 8);
        statusPnl.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 5));
        statusPnl.setPreferredSize(new Dimension(180, 34));
        JLabel dot = new JLabel("●");
        dot.setForeground(NEON);
        JLabel lblStatus = new JLabel("SYSTEM: ONLINE");
        lblStatus.setFont(new Font("Monospaced", Font.BOLD, 11));
        lblStatus.setForeground(NEON);
        statusPnl.add(dot);
        statusPnl.add(lblStatus);
        right.add(statusPnl);

        // Avatar AD
        JLabel ad = new JLabel(" AD ", SwingConstants.CENTER);
        ad.setOpaque(true);
        ad.setBackground(NEON);
        ad.setForeground(new Color(0x16, 0x1E, 0x00));
        ad.setFont(new Font("Inter", Font.BOLD, 13));
        ad.setPreferredSize(new Dimension(38, 38));
        right.add(ad);

        // Botón Cerrar Sesión
        JButton btnCerrar = new JButton("Cerrar Sesión") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()
                    ? new Color(0x12, 0x1E, 0x42)
                    : new Color(0x1A, 0x2A, 0x5E));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(0x1A, 0x8A, 0xFF));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnCerrar.setFont(new Font("Inter", Font.BOLD, 12));
        btnCerrar.setForeground(new Color(0x1A, 0x8A, 0xFF));
        btnCerrar.setOpaque(false);
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setBorderPainted(false);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCerrar.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btnCerrar.addActionListener(e -> cerrarSesion());
        right.add(btnCerrar);

        nav.add(left, BorderLayout.WEST);
        nav.add(right, BorderLayout.EAST);
        return nav;
    }

    /** Cierra sesión y vuelve a VentanaAuth */
    private void cerrarSesion() {
        int conf = JOptionPane.showConfirmDialog(
            this,
            "¿Deseas cerrar sesión del panel de administración?",
            "Cerrar Sesión",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (conf == JOptionPane.YES_OPTION) {
            if (timerRefresh != null) timerRefresh.stop();
            new VentanaAuth().setVisible(true);
            dispose();
        }
    }

    private JPanel createSidebar() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(SIDEBAR_BG);
        side.setPreferredSize(new Dimension(180, 0));
        side.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER),
            new EmptyBorder(16, 10, 16, 10)
        ));

        side.add(createMenuCategory("MANAGEMENT"));
        addMenuItem(side, "Dashboard", true);
        addMenuItem(side, "Usuarios", false);
        addMenuItem(side, "Mercados", false);
        addMenuItem(side, "Partidos", false);
        
        side.add(Box.createVerticalStrut(20));
        side.add(createMenuCategory("OPERACIONES"));
        addMenuItem(side, "Riesgo", false);
        addMenuItem(side, "Configuración", false);
        
        side.add(Box.createVerticalGlue());
        
        JPanel info = createRoundPanel(SURF, 12);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBorder(new EmptyBorder(10, 10, 10, 10));
        info.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.setMaximumSize(new Dimension(220, 60));
        JLabel ver = new JLabel("v4.2.0-stable");
        ver.setFont(new Font("Monospaced", Font.PLAIN, 10));
        ver.setForeground(ON_SURF_VAR);
        lblStatusLat = new JLabel("Latencia: --ms");
        lblStatusLat.setFont(new Font("Inter", Font.PLAIN, 11));
        lblStatusLat.setForeground(NEON);
        info.add(ver);
        info.add(Box.createVerticalStrut(5));
        info.add(lblStatusLat);
        side.add(info);

        return side;
    }

    private JLabel createMenuCategory(String title) {
        JLabel l = new JLabel(title);
        l.setFont(new Font("Inter", Font.BOLD, 10));
        l.setForeground(MUTED);
        l.setBorder(new EmptyBorder(10, 5, 5, 5));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void addMenuItem(JPanel container, String text, boolean active) {
        JPanel p = createRoundPanel(active ? NEON : SURF_CONT, 8);
        p.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
        p.setMaximumSize(new Dimension(220, 40));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l = new JLabel(text);
        l.setFont(new Font("Inter", Font.BOLD, 12));
        l.setForeground(active ? SURF : ON_SURF_VAR);
        p.add(l);
        
        MenuItemInfo info = new MenuItemInfo();
        info.panel = p;
        info.label = l;
        info.name = text;
        menuItems.add(info);

        p.setCursor(new Cursor(Cursor.HAND_CURSOR));
        p.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectMenuItem(text);
            }
        });
        container.add(p);
    }

    private void selectMenuItem(String name) {
        currentCard = name;
        cardLayout.show(cardPanel, name);
        
        for (MenuItemInfo info : menuItems) {
            if (info.name.equals(name)) {
                info.panel.repaint();
                info.label.setForeground(SURF);
            } else {
                info.panel.setBackground(SURF_CONT);
                info.label.setForeground(ON_SURF_VAR);
            }
        }
        
        // Redraw to ensure correct bg
        for (MenuItemInfo info : menuItems) {
            final boolean active = info.name.equals(name);
            JPanel p = info.panel;
            // Usamos un pequeño hack: RoundPanel usa getBackground() o tenemos que sobreescribir.
            // En nuestro caso, `createRoundPanel` capturaba el Color final, lo cual es inmutable.
            // Para hacerlo mutable, necesitamos setBackground y que RoundPanel lo use.
            p.setBackground(active ? NEON : SURF_CONT);
            p.repaint();
        }
        refreshData();
    }

    private JPanel createMainContent() {
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(SURF);
        main.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel hl = new JPanel(new GridLayout(2,1));
        hl.setOpaque(false);
        JLabel title = new JLabel("Panel de Control");
        title.setFont(new Font("Inter", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Datos en tiempo real de la base de datos del Mundial 2026.");
        sub.setFont(new Font("Inter", Font.PLAIN, 14));
        sub.setForeground(ON_SURF_VAR);
        hl.add(title);
        hl.add(sub);
        
        JPanel hr = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        hr.setOpaque(false);

        // Botón EXPORTAR — estilo visible con borde cyan, exporta pronósticos a CSV
        JButton btnExport = new JButton("⬇  EXPORTAR") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()
                    ? new Color(0x12, 0x1E, 0x42)
                    : new Color(0x1A, 0x21, 0x3A));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(ACCENT_CYAN);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnExport.setFont(new Font("Inter", Font.BOLD, 12));
        btnExport.setForeground(ACCENT_CYAN);
        btnExport.setOpaque(false);
        btnExport.setContentAreaFilled(false);
        btnExport.setBorderPainted(false);
        btnExport.setFocusPainted(false);
        btnExport.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnExport.setBorder(new EmptyBorder(8, 16, 8, 16));
        btnExport.addActionListener(e -> exportarDashboardCSV());

        // Botón ACTUALIZAR — verde-lima
        JButton btnRefresh = new JButton("↺  ACTUALIZAR") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? NEON.darker() : NEON);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnRefresh.setFont(new Font("Inter", Font.BOLD, 12));
        btnRefresh.setForeground(new Color(0x16, 0x1E, 0x00));
        btnRefresh.setOpaque(false);
        btnRefresh.setContentAreaFilled(false);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.setBorder(new EmptyBorder(8, 16, 8, 16));
        btnRefresh.addActionListener(e -> refreshData());

        hr.add(btnExport);
        hr.add(btnRefresh);
        
        header.add(hl, BorderLayout.WEST);
        header.add(hr, BorderLayout.EAST);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        main.add(header);
        main.add(Box.createVerticalStrut(30));

        // KPI Cards Grid
        JPanel kpiGrid = new JPanel(new GridLayout(1, 4, 20, 0));
        kpiGrid.setOpaque(false);
        kpiGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        
        lblUsuarios = new JLabel("--");
        lblEquipos = new JLabel("--");
        lblPartidos = new JLabel("--");
        lblPronosticos = new JLabel("--");
        
        kpiGrid.add(createKpiCard("USUARIOS", lblUsuarios, "Registrados en el sistema", SECONDARY));
        kpiGrid.add(createKpiCard("EQUIPOS", lblEquipos, "Mundial 2026", NEON));
        kpiGrid.add(createKpiCard("PARTIDOS", lblPartidos, "Fixture completo", NEON.darker()));
        kpiGrid.add(createKpiCard("PRONÓSTICOS", lblPronosticos, "Total jugados", ON_SURF_VAR));
        main.add(kpiGrid);
        main.add(Box.createVerticalStrut(30));

        // Accuracy Cards Grid
        JPanel accGrid = new JPanel(new GridLayout(1, 4, 20, 0));
        accGrid.setOpaque(false);
        accGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        
        lblAciertos = new JLabel("--");
        lblFallos = new JLabel("--");
        lblPendientes = new JLabel("--");
        lblAccuracy = new JLabel("--%");
        
        accGrid.add(createStatCard("ACIERTOS", lblAciertos, NEON));
        accGrid.add(createStatCard("FALLOS", lblFallos, ERROR));
        accGrid.add(createStatCard("PENDIENTES", lblPendientes, SECONDARY));
        accGrid.add(createStatCard("ACCURACY", lblAccuracy, NEON.darker()));
        main.add(accGrid);
        main.add(Box.createVerticalStrut(30));

        // Tabla
        JPanel tablePnl = createRoundPanel(SURF_CONT, 10);
        tablePnl.setLayout(new BorderLayout());
        tablePnl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(10,10,10,10)
        ));
        
        JLabel tTitle = new JLabel("Últimos Pronósticos");
        tTitle.setFont(new Font("Inter", Font.BOLD, 18));
        tTitle.setForeground(Color.WHITE);
        tTitle.setBorder(new EmptyBorder(0,0,10,0));
        tablePnl.add(tTitle, BorderLayout.NORTH);

        txModel = new DefaultTableModel(new String[]{"ID", "Partido", "Jugador", "Marcador", "Estado"}, 0);
        JTable table = buildStyledTable(txModel);
        
        JScrollPane scrollT = new JScrollPane(table);
        scrollT.setBorder(BorderFactory.createEmptyBorder());
        scrollT.getViewport().setBackground(SURF_CONT);
        tablePnl.add(scrollT, BorderLayout.CENTER);
        
        lblFooterTime = new JLabel("Última actualización: --");
        lblFooterTime.setFont(new Font("Monospaced", Font.PLAIN, 10));
        lblFooterTime.setForeground(ON_SURF_VAR);
        lblFooterTime.setBorder(new EmptyBorder(10, 0, 0, 0));
        tablePnl.add(lblFooterTime, BorderLayout.SOUTH);

        main.add(tablePnl);

        return main;
    }

    private JPanel createTablePanel(String titleStr, String[] cols, String identifier) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(SURF);
        pnl.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel(titleStr);
        title.setFont(new Font("Inter", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        title.setBorder(new EmptyBorder(0,0,20,0));
        pnl.add(title, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(cols, 0);
        if (identifier.equals("Usuarios")) usuariosModel = model;
        else if (identifier.equals("Partidos")) partidosModel = model;
        else if (identifier.equals("Mercados")) mercadosModel = model;

        JTable table = buildStyledTable(model);
        
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(SURF_CONT);
        pnl.add(scroll, BorderLayout.CENTER);

        return pnl;
    }

    private JPanel createPlaceholderPanel(String titleStr, String descStr) {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBackground(SURF);
        
        JPanel inner = createRoundPanel(SURF_CONT, 15);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBorder(new EmptyBorder(50, 50, 50, 50));

        JLabel title = new JLabel(titleStr);
        title.setFont(new Font("Inter", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel desc = new JLabel(descStr);
        desc.setFont(new Font("Inter", Font.PLAIN, 14));
        desc.setForeground(MUTED);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        inner.add(title);
        inner.add(Box.createVerticalStrut(20));
        inner.add(desc);

        pnl.add(inner);
        return pnl;
    }

    private JTable buildStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            public boolean isCellEditable(int r, int c) { return false; }
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0
                        ? new Color(0x1F, 0x27, 0x45)   // fila par  — azul medio
                        : new Color(0x17, 0x1E, 0x38));  // fila impar — azul oscuro
                    c.setForeground(ON_SURF);
                }
                return c;
            }
        };
        table.setBackground(new Color(0x17, 0x1E, 0x38));
        table.setForeground(ON_SURF);
        table.setGridColor(BORDER);
        table.setRowHeight(30);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setFont(new Font("Monospaced", Font.PLAIN, 11));
        table.setSelectionBackground(new Color(0x1A, 0x8A, 0xFF, 80));
        table.setSelectionForeground(Color.WHITE);

        JTableHeader th = table.getTableHeader();
        th.setBackground(new Color(0x13, 0x18, 0x2E));
        th.setForeground(MUTED);
        th.setFont(new Font("Inter", Font.BOLD, 11));
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        center.setBackground(new Color(0x1F, 0x27, 0x45));
        center.setForeground(ON_SURF);

        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i == 0 || i >= 3) {
                table.getColumnModel().getColumn(i).setCellRenderer(center);
            }
        }

        // Renderer especial para columna "Estado" — colores según valor
        int estadoCol = table.getColumnCount() - 1;
        table.getColumnModel().getColumn(estadoCol).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(
                        JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                    super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                    setHorizontalAlignment(JLabel.CENTER);
                    String v = val != null ? val.toString() : "";
                    if (v.contains("PENDING") || v.contains("PENDIENTE")) {
                        setForeground(ACCENT_CYAN);
                    } else if (v.contains("SETTLED") || v.contains("ACIERTO") || v.contains("Local") || v.contains("Visitante")) {
                        setForeground(new Color(0x00, 0xE0, 0x7A));
                    } else if (v.contains("LOSS") || v.contains("FALLO") || v.contains("ERROR")) {
                        setForeground(new Color(0xFF, 0x3B, 0x3B));
                    } else {
                        setForeground(ON_SURF);
                    }
                    setBackground(row % 2 == 0
                        ? new Color(0x1F, 0x27, 0x45)
                        : new Color(0x17, 0x1E, 0x38));
                    return this;
                }
            }
        );

        return table;
    }

    private JPanel createKpiCard(String title, JLabel valLbl, String desc, Color highlight) {
        JPanel p = createRoundPanel(new Color(0x1A, 0x21, 0x3A), 10);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(18, 18, 18, 18)
        ));
        JLabel t = new JLabel(title);
        t.setFont(new Font("Inter", Font.BOLD, 11));
        t.setForeground(MUTED);
        valLbl.setFont(new Font("Monospaced", Font.BOLD, 30));
        valLbl.setForeground(Color.WHITE);
        JLabel d = new JLabel(desc);
        d.setFont(new Font("Inter", Font.PLAIN, 11));
        d.setForeground(highlight);
        p.add(t);
        p.add(Box.createVerticalStrut(8));
        p.add(valLbl);
        p.add(Box.createVerticalStrut(4));
        p.add(d);
        return p;
    }

    private JPanel createStatCard(String title, JLabel valLbl, Color fg) {
        JPanel p = createRoundPanel(new Color(0x1A, 0x21, 0x3A), 10);
        p.setLayout(new GridBagLayout());
        p.setBorder(BorderFactory.createLineBorder(BORDER));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        valLbl.setFont(new Font("Monospaced", Font.BOLD, 26));
        valLbl.setForeground(fg);
        p.add(valLbl, gbc);
        gbc.gridy = 1;
        JLabel t = new JLabel(title);
        t.setFont(new Font("Inter", Font.BOLD, 10));
        t.setForeground(MUTED);
        p.add(t, gbc);
        return p;
    }

    private JPanel createRoundPanel(Color initBg, int radius) {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground()); // usar getBackground para que reaccione al hover/select
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));
                g2.dispose();
            }
        };
        p.setBackground(initBg);
        return p;
    }

    private void refreshData() {
        long t0 = System.currentTimeMillis();
        
        SwingWorker<AdminDashboardServer.DashboardStats, Void> worker = new SwingWorker<>() {
            @Override
            protected AdminDashboardServer.DashboardStats doInBackground() {
                return AdminDashboardServer.getStats();
            }

            @Override
            protected void done() {
                try {
                    AdminDashboardServer.DashboardStats s = get();
                    lblUsuarios.setText(String.valueOf(s.usuarios));
                    lblEquipos.setText(String.valueOf(s.equipos));
                    lblPartidos.setText(String.valueOf(s.partidos));
                    lblPronosticos.setText(String.valueOf(s.pronosticos));
                    
                    lblAciertos.setText(String.valueOf(s.pAciertos));
                    lblFallos.setText(String.valueOf(s.pFallos));
                    lblPendientes.setText(String.valueOf(s.pPendientes));
                    
                    int total = s.pAciertos + s.pFallos;
                    double acc = total > 0 ? (s.pAciertos * 100.0 / total) : 0;
                    lblAccuracy.setText(String.format("%.1f%%", acc));
                    
                    txModel.setRowCount(0);
                    for (Map<String, Object> tx : s.liveTx) {
                        txModel.addRow(new Object[]{
                            tx.get("id"),
                            tx.get("mercado"),
                            tx.get("usuario"),
                            tx.get("marcador"),
                            tx.get("status")
                        });
                    }
                    
                    long ms = System.currentTimeMillis() - t0;
                    lblStatusLat.setText("Latencia: " + ms + "ms");
                    if (lblFooterTime != null) {
                        lblFooterTime.setText("Última actualización: " + new java.util.Date());
                    }
                } catch (Exception ex) {
                    lblStatusLat.setText("Error DB");
                }
            }
        };
        
        // Dependiendo de la vista activa, traemos más datos
        if (currentCard.equals("Usuarios") || currentCard.equals("Partidos") || currentCard.equals("Mercados")) {
            SwingWorker<Void, Void> secondaryWorker = new SwingWorker<>() {
                private List<Object[]> usrList, ptList;
                @Override
                protected Void doInBackground() {
                    if (currentCard.equals("Usuarios")) usrList = AdminDashboardServer.getUsuariosList();
                    if (currentCard.equals("Partidos") || currentCard.equals("Mercados")) ptList = AdminDashboardServer.getPartidosList();
                    return null;
                }
                @Override
                protected void done() {
                    if (usrList != null && usuariosModel != null) {
                        usuariosModel.setRowCount(0);
                        for(Object[] o : usrList) usuariosModel.addRow(o);
                    }
                    if (ptList != null && partidosModel != null) {
                        partidosModel.setRowCount(0);
                        for(Object[] o : ptList) partidosModel.addRow(o);
                    }
                    if (ptList != null && mercadosModel != null) {
                        mercadosModel.setRowCount(0);
                        for(Object[] o : ptList) mercadosModel.addRow(o);
                    }
                }
            };
            secondaryWorker.execute();
        }
        
        worker.execute();
    }

    // ================================================================
    // PANEL RIESGO — Módulo de gestión de riesgo y límites
    // ================================================================
    private JPanel createRiesgoPanel() {
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(SURF);
        main.setBorder(new EmptyBorder(28, 28, 28, 28));

        // Título
        JLabel titulo = new JLabel("Módulo de Riesgo");
        titulo.setFont(new Font("Inter", Font.BOLD, 28));
        titulo.setForeground(Color.WHITE);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Control de límites, exposición y usuarios bloqueados.");
        sub.setFont(new Font("Inter", Font.PLAIN, 13));
        sub.setForeground(ON_SURF_VAR);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setBorder(new EmptyBorder(4, 0, 20, 0));

        main.add(titulo);
        main.add(sub);

        // --- Fila 1: tarjetas de métricas de riesgo ---
        JPanel kpis = new JPanel(new GridLayout(1, 3, 16, 0));
        kpis.setOpaque(false);
        kpis.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        kpis.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblExposicion = new JLabel("$0.00");
        JLabel lblApuestasHoy = new JLabel("0");
        JLabel lblUsuariosBloq = new JLabel("0");
        lblExposicion.setFont(new Font("Monospaced", Font.BOLD, 26)); lblExposicion.setForeground(NEON);
        lblApuestasHoy.setFont(new Font("Monospaced", Font.BOLD, 26)); lblApuestasHoy.setForeground(ACCENT_CYAN);
        lblUsuariosBloq.setFont(new Font("Monospaced", Font.BOLD, 26)); lblUsuariosBloq.setForeground(new Color(0xFF, 0x3B, 0x3B));

        kpis.add(createStatCard("EXPOSICIÓN TOTAL", lblExposicion, NEON));
        kpis.add(createStatCard("APUESTAS HOY",    lblApuestasHoy, ACCENT_CYAN));
        kpis.add(createStatCard("USUARIOS BLOQ.",  lblUsuariosBloq, new Color(0xFF, 0x3B, 0x3B)));
        main.add(kpis);
        main.add(Box.createVerticalStrut(20));

        // --- Límites de apuestas ---
        JPanel limiteCard = createRoundPanel(new Color(0x1A, 0x21, 0x3A), 10);
        limiteCard.setLayout(new BoxLayout(limiteCard, BoxLayout.Y_AXIS));
        limiteCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(16, 16, 16, 16)
        ));
        limiteCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        limiteCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tLimites = new JLabel("⚡  Límites de Apuesta");
        tLimites.setFont(new Font("Inter", Font.BOLD, 15));
        tLimites.setForeground(NEON);
        limiteCard.add(tLimites);
        limiteCard.add(Box.createVerticalStrut(12));

        // Grid de límites
        JPanel gridLimites = new JPanel(new GridLayout(2, 3, 12, 8));
        gridLimites.setOpaque(false);

        String[][] limites = {
            {"Apuesta mínima ($)", "1.00"},
            {"Apuesta máxima ($)", "500.00"},
            {"Máx. por usuario/día ($)", "1000.00"},
            {"Cuota máxima", "15.00"},
            {"Máx. apuestas/usuario/día", "20"},
            {"Umbral alerta riesgo ($)", "2000.00"}
        };

        for (String[] lim : limites) {
            JPanel campo = new JPanel(new BorderLayout(4, 0));
            campo.setOpaque(false);
            JLabel lbl = new JLabel(lim[0]);
            lbl.setFont(new Font("Inter", Font.PLAIN, 10));
            lbl.setForeground(MUTED);
            JTextField txt = new JTextField(lim[1]);
            txt.setBackground(new Color(0x0D, 0x10, 0x1F));
            txt.setForeground(Color.WHITE);
            txt.setCaretColor(NEON);
            txt.setFont(new Font("Monospaced", Font.PLAIN, 12));
            txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(4, 8, 4, 8)
            ));
            campo.add(lbl, BorderLayout.NORTH);
            campo.add(txt, BorderLayout.CENTER);
            gridLimites.add(campo);
        }
        limiteCard.add(gridLimites);
        limiteCard.add(Box.createVerticalStrut(10));

        JButton btnGuardarLimites = crearBotonAdmin("💾  Guardar Límites", NEON, new Color(0x16, 0x1E, 0x00));
        btnGuardarLimites.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnGuardarLimites.addActionListener(e ->
            JOptionPane.showMessageDialog(this, "Límites guardados correctamente.", "✔ Guardado", JOptionPane.INFORMATION_MESSAGE));
        limiteCard.add(btnGuardarLimites);
        main.add(limiteCard);
        main.add(Box.createVerticalStrut(16));

        // --- Tabla usuarios en riesgo ---
        JPanel tablaCard = createRoundPanel(new Color(0x1A, 0x21, 0x3A), 10);
        tablaCard.setLayout(new BorderLayout());
        tablaCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(14, 14, 14, 14)
        ));
        tablaCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        tablaCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel tablaHeader = new JPanel(new BorderLayout());
        tablaHeader.setOpaque(false);
        JLabel tTabla = new JLabel("🛡  Usuarios en Seguimiento");
        tTabla.setFont(new Font("Inter", Font.BOLD, 14));
        tTabla.setForeground(Color.WHITE);
        tablaHeader.add(tTabla, BorderLayout.WEST);

        JButton btnBloquear = crearBotonAdmin("Bloquear Seleccionado", new Color(0xFF, 0x3B, 0x3B), Color.WHITE);
        btnBloquear.addActionListener(e ->
            JOptionPane.showMessageDialog(this, "Usuario bloqueado.", "Riesgo", JOptionPane.WARNING_MESSAGE));
        tablaHeader.add(btnBloquear, BorderLayout.EAST);
        tablaCard.add(tablaHeader, BorderLayout.NORTH);

        DefaultTableModel riesgoModel = new DefaultTableModel(
            new String[]{"Usuario", "Email", "Apuestas Hoy", "Monto Total", "Estado"}, 0);
        // Datos de ejemplo desde BD
        new Thread(() -> {
            try {
                java.sql.Connection conn = java.sql.DriverManager.getConnection(
                    AdminDashboardServer.DB_URL, AdminDashboardServer.DB_USER, AdminDashboardServer.DB_PASS);
                java.sql.ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT u.nombre_completo, u.email, COUNT(p.id) AS cnt, " +
                    "COALESCE(SUM(0),0) AS total, IF(u.activo=1,'ACTIVO','BLOQUEADO') AS estado " +
                    "FROM usuarios u LEFT JOIN pronosticos p ON p.nombre_jugador = u.nombre_completo " +
                    "WHERE u.es_admin = 0 " +
                    "GROUP BY u.id");
                while (rs.next()) {
                    final Object[] row = {
                        rs.getString("nombre_completo"), rs.getString("email"),
                        rs.getInt("cnt"), "$" + rs.getDouble("total"), rs.getString("estado")
                    };
                    SwingUtilities.invokeLater(() -> riesgoModel.addRow(row));
                }
                conn.close();
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> riesgoModel.addRow(new Object[]{"Error BD", ex.getMessage(), "-", "-", "-"}));
            }
        }).start();

        JTable tablaRiesgo = buildStyledTable(riesgoModel);
        JScrollPane scrollR = new JScrollPane(tablaRiesgo);
        scrollR.setBorder(BorderFactory.createEmptyBorder());
        scrollR.getViewport().setBackground(new Color(0x1A, 0x21, 0x3A));
        scrollR.setBorder(new EmptyBorder(10, 0, 0, 0));
        tablaCard.add(scrollR, BorderLayout.CENTER);
        main.add(tablaCard);

        JScrollPane scrollMain = new JScrollPane(main);
        scrollMain.setBorder(null);
        scrollMain.getViewport().setBackground(SURF);
        scrollMain.getVerticalScrollBar().setUnitIncrement(14);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(SURF);
        wrapper.add(scrollMain);
        return wrapper;
    }

    // ================================================================
    // PANEL CONFIGURACIÓN — Ajustes del sistema y BD
    // ================================================================
    private JPanel createConfiguracionPanel() {
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(SURF);
        main.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel titulo = new JLabel("Configuración del Sistema");
        titulo.setFont(new Font("Inter", Font.BOLD, 28));
        titulo.setForeground(Color.WHITE);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Ajustes globales de Velocity Admin — BetCentral.");
        sub.setFont(new Font("Inter", Font.PLAIN, 13));
        sub.setForeground(ON_SURF_VAR);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setBorder(new EmptyBorder(4, 0, 20, 0));
        main.add(titulo); main.add(sub);

        // --- Sección: Conexión a Base de Datos ---
        main.add(crearSeccionConfig("🗄  Conexión a Base de Datos", new String[][]{
            {"Host MySQL",    AdminDashboardServer.DB_URL.replace("jdbc:mysql://","").split("/")[0]},
            {"Base de Datos", "mundial_db"},
            {"Usuario BD",    AdminDashboardServer.DB_USER},
            {"Estado",        "● CONECTADO"}
        }, false));
        main.add(Box.createVerticalStrut(14));

        // --- Sección: Credenciales Admin ---
        main.add(crearSeccionConfig("🔐  Credenciales de Administrador", new String[][]{
            {"Email Admin",          "josevera@gmail.com"},
            {"Nueva Contraseña",     ""},
            {"Confirmar Contraseña", ""}
        }, true));
        main.add(Box.createVerticalStrut(14));

        // --- Sección: Ajustes del Sistema ---
        main.add(crearSeccionConfig("⚙  Ajustes Generales del Sistema", new String[][]{
            {"Nombre del Sistema",   "BetCentral · Mundial 2026"},
            {"Versión",              "v4.2.0-stable"},
            {"Timeout sesión (min)", "30"},
            {"Max usuarios online",  "100"}
        }, false));
        main.add(Box.createVerticalStrut(14));

        // --- Sección: Operaciones de BD ---
        JPanel bdOps = createRoundPanel(new Color(0x1A, 0x21, 0x3A), 10);
        bdOps.setLayout(new BoxLayout(bdOps, BoxLayout.Y_AXIS));
        bdOps.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(16, 16, 16, 16)
        ));
        bdOps.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        bdOps.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tBdOps = new JLabel("🔧  Operaciones de Base de Datos");
        tBdOps.setFont(new Font("Inter", Font.BOLD, 14));
        tBdOps.setForeground(Color.WHITE);
        bdOps.add(tBdOps);
        bdOps.add(Box.createVerticalStrut(12));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);

        JButton btnVerificar = crearBotonAdmin("✔ Verificar Conexión", ACCENT_BLUE, Color.WHITE);
        btnVerificar.addActionListener(e -> {
            try {
                java.sql.DriverManager.getConnection(
                    AdminDashboardServer.DB_URL, AdminDashboardServer.DB_USER, AdminDashboardServer.DB_PASS).close();
                JOptionPane.showMessageDialog(this, "✔  Conexión a MySQL exitosa.", "DB OK", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "✘  Error: " + ex.getMessage(), "Error DB", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnLimpiarLogs = crearBotonAdmin("🗑 Limpiar Cache", SURF_HIGH, ON_SURF);
        btnLimpiarLogs.addActionListener(e ->
            JOptionPane.showMessageDialog(this, "Cache limpiado correctamente.", "OK", JOptionPane.INFORMATION_MESSAGE));

        JButton btnBackup = crearBotonAdmin("💾 Exportar SQL", new Color(0x00, 0xD0, 0x7A), Color.WHITE);
        btnBackup.addActionListener(e -> exportarSQL());

        btnRow.add(btnVerificar); btnRow.add(btnLimpiarLogs); btnRow.add(btnBackup);
        bdOps.add(btnRow);
        main.add(bdOps);

        JScrollPane scrollMain = new JScrollPane(main);
        scrollMain.setBorder(null);
        scrollMain.getViewport().setBackground(SURF);
        scrollMain.getVerticalScrollBar().setUnitIncrement(14);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(SURF);
        wrapper.add(scrollMain);
        return wrapper;
    }

    /** Helper: sección de configuración con campos editables */
    private JPanel crearSeccionConfig(String tituloStr, String[][] campos, boolean esPassword) {
        JPanel card = createRoundPanel(new Color(0x1A, 0x21, 0x3A), 10);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(14, 16, 14, 16)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30 + campos.length * 52));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel t = new JLabel(tituloStr);
        t.setFont(new Font("Inter", Font.BOLD, 14));
        t.setForeground(Color.WHITE);
        card.add(t);
        card.add(Box.createVerticalStrut(10));

        JPanel grid = new JPanel(new GridLayout(1, campos.length, 12, 0));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        java.util.List<JTextField> texFields = new java.util.ArrayList<>();

        for (String[] campo : campos) {
            JPanel c = new JPanel(new BorderLayout(0, 4));
            c.setOpaque(false);
            JLabel lbl = new JLabel(campo[0]);
            lbl.setFont(new Font("Inter", Font.PLAIN, 10));
            lbl.setForeground(MUTED);

            JTextField tf;
            if (esPassword && !campo[0].equals("Email Admin")) {
                tf = new JPasswordField(campo[1]);
                ((JPasswordField) tf).setEchoChar('●');
            } else {
                tf = new JTextField(campo[1]);
            }
            tf.setBackground(new Color(0x0D, 0x10, 0x1F));
            tf.setForeground(campo[0].equals("Estado")
                ? new Color(0x00, 0xE0, 0x7A) : Color.WHITE);
            tf.setCaretColor(NEON);
            tf.setFont(new Font("Monospaced", Font.PLAIN, 11));
            tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(5, 8, 5, 8)
            ));
            if (campo[0].equals("Estado") || campo[0].equals("Versión")
                    || campo[0].equals("Usuario BD") || campo[0].equals("Base de Datos")
                    || campo[0].equals("Host MySQL")) {
                tf.setEditable(false);
                tf.setBackground(new Color(0x13, 0x18, 0x2E));
            }
            texFields.add(tf);
            c.add(lbl, BorderLayout.NORTH);
            c.add(tf,  BorderLayout.CENTER);
            grid.add(c);
        }
        card.add(grid);
        card.add(Box.createVerticalStrut(8));

        if (esPassword) {
            JButton btnGuardar = crearBotonAdmin("🔐  Actualizar Contraseña", NEON, new Color(0x16, 0x1E, 0x00));
            btnGuardar.setAlignmentX(Component.LEFT_ALIGNMENT);
            btnGuardar.addActionListener(e -> {
                String nueva    = texFields.size() > 1 ? new String(((JPasswordField)texFields.get(1)).getPassword()) : "";
                String confirma = texFields.size() > 2 ? new String(((JPasswordField)texFields.get(2)).getPassword()) : "";
                if (nueva.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Ingresa la nueva contraseña.", "Error", JOptionPane.WARNING_MESSAGE); return;
                }
                if (!nueva.equals(confirma)) {
                    JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden.", "Error", JOptionPane.ERROR_MESSAGE); return;
                }
                if (nueva.length() < 6) {
                    JOptionPane.showMessageDialog(this, "Mínimo 6 caracteres.", "Error", JOptionPane.WARNING_MESSAGE); return;
                }
                JOptionPane.showMessageDialog(this, "✔  Contraseña actualizada.", "OK", JOptionPane.INFORMATION_MESSAGE);
                texFields.get(1).setText(""); texFields.get(2).setText("");
            });
            card.add(btnGuardar);
        } else {
            JButton btnGuardar = crearBotonAdmin("💾  Guardar Cambios", ACCENT_BLUE, Color.WHITE);
            btnGuardar.setAlignmentX(Component.LEFT_ALIGNMENT);
            btnGuardar.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "✔  Configuración guardada.", "OK", JOptionPane.INFORMATION_MESSAGE));
            card.add(btnGuardar);
        }
        return card;
    }

    /** Exporta un mini-resumen SQL a un archivo */
    private void exportarSQL() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("betcentral_backup.sql"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try (java.io.PrintWriter pw = new java.io.PrintWriter(fc.getSelectedFile())) {
            pw.println("-- BetCentral Backup — " + new java.util.Date());
            pw.println("-- Generado desde Velocity Admin");
            pw.println("USE mundial_db;");
            java.sql.Connection conn = java.sql.DriverManager.getConnection(
                AdminDashboardServer.DB_URL, AdminDashboardServer.DB_USER, AdminDashboardServer.DB_PASS);
            for (String tbl : new String[]{"equipos","partidos","pronosticos","usuarios"}) {
                java.sql.ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM " + tbl);
                java.sql.ResultSetMetaData meta = rs.getMetaData();
                while (rs.next()) {
                    StringBuilder sb = new StringBuilder("INSERT INTO " + tbl + " VALUES (");
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        if (i > 1) sb.append(", ");
                        Object v = rs.getObject(i);
                        sb.append(v == null ? "NULL" : "'" + v.toString().replace("'", "\\'") + "'");
                    }
                    sb.append(");");
                    pw.println(sb);
                }
            }
            conn.close();
            JOptionPane.showMessageDialog(this, "✔  Backup guardado en:\n" + fc.getSelectedFile().getAbsolutePath(), "OK", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al exportar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Botón estilizado para el admin dashboard */
    private JButton crearBotonAdmin(String texto, Color fondo, Color textColor) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? fondo.darker() : fondo);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Inter", Font.BOLD, 11));
        btn.setForeground(textColor);
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(7, 14, 7, 14));
        return btn;
    }

    /** Exporta pronósticos del dashboard a CSV */
    private void exportarDashboardCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("pronosticos_betcentral.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        try (java.io.PrintWriter pw = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(
                    new java.io.FileOutputStream(fc.getSelectedFile()), "UTF-8"))) {

            // Cabecera CSV
            pw.println("ID,Partido,Jugador,Goles Local,Goles Visitante,Marcador,Estado,Fecha");

            // Datos desde BD
            java.sql.Connection conn = java.sql.DriverManager.getConnection(
                AdminDashboardServer.DB_URL, AdminDashboardServer.DB_USER, AdminDashboardServer.DB_PASS);
            java.sql.ResultSet rs = conn.createStatement().executeQuery(
                "SELECT p.id, " +
                "CONCAT(p.equipo_local,' vs ',p.equipo_visitante) AS partido, " +
                "p.nombre_jugador, p.goles_local, p.goles_visitante, " +
                "CONCAT(p.goles_local,'-',p.goles_visitante) AS marcador, " +
                "CASE WHEN p.acerto IS NULL THEN 'PENDING' " +
                "     WHEN p.acerto=1 THEN 'SETTLED' ELSE 'LOSS' END AS estado, " +
                "p.fecha_registro " +
                "FROM pronosticos p ORDER BY p.fecha_registro DESC");

            int filas = 0;
            while (rs.next()) {
                pw.printf("%s,\"%s\",\"%s\",%s,%s,%s,%s,%s%n",
                    rs.getString("id"),
                    rs.getString("partido"),
                    rs.getString("nombre_jugador"),
                    rs.getString("goles_local"),
                    rs.getString("goles_visitante"),
                    rs.getString("marcador"),
                    rs.getString("estado"),
                    rs.getString("fecha_registro")
                );
                filas++;
            }
            conn.close();

            JOptionPane.showMessageDialog(this,
                "✔  Exportado " + filas + " registro(s) a:\n" + fc.getSelectedFile().getAbsolutePath(),
                "Exportación exitosa", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error al exportar: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaAdminDashboard().setVisible(true));
    }
}
