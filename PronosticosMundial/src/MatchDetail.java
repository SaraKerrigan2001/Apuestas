import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla de detalle de partido en vivo.
 * Mapea el HTML: BETCENTRAL - Match Detail
 * Scoreboard + Stats + Market Tabs + Mercados de apuestas
 */
public class MatchDetail extends JPanel {

    // Callback para cuando se agregan apuestas al carrito
    private final Runnable onAbrirBetSlip;
    private final BetSlip  betSlip;

    private JLabel lblMinuto;
    private int    minutoActual = 65;
    private Timer  timerMinuto;
    private JLabel lblContadorBets;
    private JButton btnBetSlipFlotante;

    public MatchDetail(Runnable onAbrirBetSlip, BetSlip betSlip) {
        this.onAbrirBetSlip = onAbrirBetSlip;
        this.betSlip        = betSlip;

        setLayout(new BorderLayout());
        setBackground(BetTheme.BACKGROUND);
        setOpaque(true);

        // Scroll principal
        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBackground(BetTheme.BACKGROUND);
        contenido.setOpaque(true);
        contenido.setBorder(BorderFactory.createEmptyBorder(12, 14, 80, 14));

        contenido.add(crearScoreboard());
        contenido.add(Box.createVerticalStrut(14));
        contenido.add(crearMarketTabs());
        contenido.add(Box.createVerticalStrut(14));
        contenido.add(crearMercados());

        JScrollPane scroll = new JScrollPane(contenido,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BetTheme.BACKGROUND);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(4, 0));

        add(scroll, BorderLayout.CENTER);

        // Botón flotante BetSlip
        add(crearBotonFlotante(), BorderLayout.SOUTH);

        iniciarTimer();
    }

    // ---------------------------------------------------------------
    // Scoreboard: equipos, marcador, stats
    // ---------------------------------------------------------------
    private JPanel crearScoreboard() {
        JPanel card = new JPanel(new BorderLayout(0, 14)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BetTheme.SURFACE_CONTAINER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                // Barra lateral izquierda verde
                g2.setColor(BetTheme.PRIMARY_CONTAINER);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                g2.setColor(BetTheme.WHITE_10);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        // Cabecera: LIVE + liga
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setOpaque(false);
        JPanel liveRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        liveRow.setOpaque(false);
        JLabel dot = new JLabel("●");
        dot.setForeground(BetTheme.PRIMARY_CONTAINER);
        dot.setFont(new Font("Dialog", Font.BOLD, 9));
        iniciarPulso(dot);
        lblMinuto = new JLabel("LIVE 65'");
        lblMinuto.setFont(new Font("Monospaced", Font.BOLD, 11));
        lblMinuto.setForeground(BetTheme.PRIMARY_CONTAINER);
        liveRow.add(dot);
        liveRow.add(lblMinuto);
        cabecera.add(liveRow, BorderLayout.WEST);
        JLabel liga = new JLabel("PREMIER LEAGUE");
        liga.setFont(new Font("Inter", Font.BOLD, 10));
        liga.setForeground(BetTheme.ON_SURFACE_VARIANT);
        cabecera.add(liga, BorderLayout.EAST);
        card.add(cabecera, BorderLayout.NORTH);

        // Equipos y marcador
        JPanel marcador = new JPanel(new BorderLayout());
        marcador.setOpaque(false);

        card.add(marcador, BorderLayout.CENTER);
        marcador.add(crearPanelEquipo("Manchester City", "MC", new Color(100, 160, 220)), BorderLayout.WEST);
        marcador.add(crearPanelMarcador("2", "1"), BorderLayout.CENTER);
        marcador.add(crearPanelEquipo("Arsenal", "AR", new Color(220, 60, 60)), BorderLayout.EAST);

        // Stats bar
        card.add(crearStatsBar(), BorderLayout.SOUTH);
        return card;
    }

    private JPanel crearPanelEquipo(String nombre, String iniciales, Color color) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(130, 80));

        // Escudo circular
        JPanel escudo = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BetTheme.SURFACE_CONTAINER_HIGH);
                g2.fillRoundRect(0, 0, 48, 48, 10, 10);
                g2.setColor(color.darker());
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(0, 0, 47, 47, 10, 10);
                g2.setColor(color);
                g2.setFont(new Font("Inter", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(iniciales, (48 - fm.stringWidth(iniciales)) / 2,
                    (48 - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        escudo.setPreferredSize(new Dimension(48, 48));
        escudo.setMaximumSize(new Dimension(48, 48));
        escudo.setOpaque(false);
        escudo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lbl = new JLabel(nombre, SwingConstants.CENTER);
        lbl.setFont(new Font("Inter", Font.BOLD, 13));
        lbl.setForeground(BetTheme.PRIMARY);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(Box.createVerticalGlue());
        p.add(escudo);
        p.add(Box.createVerticalStrut(6));
        p.add(lbl);
        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel crearPanelMarcador(String g1, String g2) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        row.setOpaque(false);
        JLabel lG1 = new JLabel(g1);
        lG1.setFont(new Font("Inter", Font.BOLD, 42));
        lG1.setForeground(BetTheme.PRIMARY);
        JLabel sep = new JLabel(":");
        sep.setFont(new Font("Inter", Font.BOLD, 36));
        sep.setForeground(new Color(BetTheme.ON_SURFACE_VARIANT.getRed(),
            BetTheme.ON_SURFACE_VARIANT.getGreen(),
            BetTheme.ON_SURFACE_VARIANT.getBlue(), 80));
        JLabel lG2 = new JLabel(g2);
        lG2.setFont(new Font("Inter", Font.BOLD, 42));
        lG2.setForeground(BetTheme.PRIMARY);
        row.add(lG1); row.add(sep); row.add(lG2);

        p.add(Box.createVerticalGlue());
        p.add(row);
        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel crearStatsBar() {
        JPanel p = new JPanel(new GridLayout(1, 3, 12, 0));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        p.add(crearStatBarra("Posesión", "58%", "42%", 0.58f));
        p.add(crearStatCentro("Tiros (Al arco)", "12(5) - 8(3)"));
        p.add(crearStatBarra("Córners", "7", "4", 0.63f));
        return p;
    }

    private JPanel crearStatBarra(String label, String v1, String v2, float pct) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JPanel valores = new JPanel(new BorderLayout());
        valores.setOpaque(false);
        JLabel lv1 = new JLabel(v1); lv1.setFont(BetTheme.FONT_LABEL_CAPS); lv1.setForeground(BetTheme.ON_SURFACE_VARIANT);
        JLabel lLbl = new JLabel(label, SwingConstants.CENTER); lLbl.setFont(BetTheme.FONT_LABEL_CAPS); lLbl.setForeground(BetTheme.ON_SURFACE_VARIANT);
        JLabel lv2 = new JLabel(v2); lv2.setFont(BetTheme.FONT_LABEL_CAPS); lv2.setForeground(BetTheme.ON_SURFACE_VARIANT);
        valores.add(lv1, BorderLayout.WEST); valores.add(lLbl, BorderLayout.CENTER); valores.add(lv2, BorderLayout.EAST);
        JPanel barra = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BetTheme.SURFACE_BRIGHT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.setColor(BetTheme.PRIMARY_CONTAINER);
                g2.fillRoundRect(0, 0, (int)(getWidth() * pct), getHeight(), 4, 4);
                g2.dispose();
            }
        };
        barra.setPreferredSize(new Dimension(0, 4));
        barra.setMaximumSize(new Dimension(Integer.MAX_VALUE, 4));
        barra.setOpaque(false);
        p.add(valores); p.add(Box.createVerticalStrut(3)); p.add(barra);
        return p;
    }

    private JPanel crearStatCentro(String label, String valor) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel lLbl = new JLabel(label, SwingConstants.CENTER);
        lLbl.setFont(BetTheme.FONT_LABEL_CAPS); lLbl.setForeground(BetTheme.ON_SURFACE_VARIANT);
        lLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel lVal = new JLabel(valor, SwingConstants.CENTER);
        lVal.setFont(BetTheme.FONT_MONO_SM); lVal.setForeground(BetTheme.PRIMARY);
        lVal.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lLbl); p.add(Box.createVerticalStrut(3)); p.add(lVal);
        return p;
    }

    // ---------------------------------------------------------------
    // Market Tabs (chips de categoría)
    // ---------------------------------------------------------------
    private JPanel crearMarketTabs() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        String[] tabs = {"POPULAR", "GOALS", "ASIAN LINES", "HALFTIME", "CORNERS"};
        for (int i = 0; i < tabs.length; i++) {
            JButton btn = crearChip(tabs[i], i == 0);
            p.add(btn);
        }
        return p;
    }

    private JButton crearChip(String texto, boolean activo) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(activo ? BetTheme.PRIMARY_CONTAINER : BetTheme.SURFACE_CONTAINER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                if (!activo) {
                    g2.setColor(BetTheme.WHITE_10);
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Inter", Font.BOLD, 10));
        btn.setForeground(activo ? BetTheme.ON_PRIMARY_CONTAINER : BetTheme.ON_SURFACE_VARIANT);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ---------------------------------------------------------------
    // Mercados de apuestas
    // ---------------------------------------------------------------
    private JPanel crearMercados() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        p.add(crearMercado1x2());
        p.add(Box.createVerticalStrut(12));
        p.add(crearMercadoOverUnder());
        p.add(Box.createVerticalStrut(12));
        p.add(crearMercadoBTTS());
        p.add(Box.createVerticalStrut(12));
        p.add(crearMercadoNextGoal());
        return p;
    }

    private JPanel crearMercado1x2() {
        JPanel card = crearCardMercado();
        card.add(crearCabeceraMercado("Full Time Result", null), BorderLayout.NORTH);
        JPanel btns = new JPanel(new GridLayout(1, 3, 8, 0));
        btns.setOpaque(false);
        btns.add(crearBotonCuota("1",  "1.28", false));
        btns.add(crearBotonCuota("X",  "4.50", false));
        btns.add(crearBotonCuota("2",  "9.00", false));
        card.add(btns, BorderLayout.CENTER);
        return card;
    }

    private JPanel crearMercadoOverUnder() {
        JPanel card = crearCardMercado();
        card.add(crearCabeceraMercado("Match Goals", "Línea: 4.5"), BorderLayout.NORTH);
        JPanel btns = new JPanel(new GridLayout(1, 2, 8, 0));
        btns.setOpaque(false);
        btns.add(crearBotonCuotaConFlecha("Over 4.5",  "2.10", true));
        btns.add(crearBotonCuotaConFlecha("Under 4.5", "1.75", false));
        card.add(btns, BorderLayout.CENTER);
        return card;
    }

    private JPanel crearMercadoBTTS() {
        JPanel card = crearCardMercado();
        card.add(crearCabeceraMercado("Both Teams to Score", null), BorderLayout.NORTH);
        JPanel btns = new JPanel(new GridLayout(1, 2, 8, 0));
        btns.setOpaque(false);
        btns.add(crearBotonCuota("YES", "1.85", false));
        btns.add(crearBotonCuota("NO",  "1.95", false));
        card.add(btns, BorderLayout.CENTER);
        return card;
    }

    private JPanel crearMercadoNextGoal() {
        // Card especial con borde izquierdo verde y badge HOT
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BetTheme.SURFACE_CONTAINER_LOW);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(BetTheme.PRIMARY_CONTAINER);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                g2.setColor(BetTheme.WHITE_10);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        card.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JPanel cab = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        cab.setOpaque(false);
        JLabel titulo = new JLabel("Next Goal (4th)");
        titulo.setFont(new Font("Inter", Font.BOLD, 15));
        titulo.setForeground(BetTheme.PRIMARY);
        JLabel hot = new JLabel("  HOT  ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BetTheme.PRIMARY_CONTAINER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        hot.setFont(new Font("Inter", Font.BOLD, 9));
        hot.setForeground(BetTheme.ON_PRIMARY_CONTAINER);
        hot.setOpaque(false);
        cab.add(titulo); cab.add(hot);
        card.add(cab, BorderLayout.NORTH);

        JPanel btns = new JPanel(new GridLayout(1, 3, 8, 0));
        btns.setOpaque(false);
        btns.add(crearBotonCuota("MAN CITY", "1.60", false));
        btns.add(crearBotonCuota("NO GOAL",  "4.00", false));
        btns.add(crearBotonCuota("ARSENAL",  "3.20", false));
        card.add(btns, BorderLayout.CENTER);
        return card;
    }

    // ---------------------------------------------------------------
    // Helpers de mercado
    // ---------------------------------------------------------------
    private JPanel crearCardMercado() {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BetTheme.SURFACE_CONTAINER_LOW);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(BetTheme.WHITE_10);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        card.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        return card;
    }

    private JPanel crearCabeceraMercado(String titulo, String sub) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel t = new JLabel(titulo);
        t.setFont(new Font("Inter", Font.BOLD, 15));
        t.setForeground(BetTheme.PRIMARY);
        p.add(t, BorderLayout.WEST);
        if (sub != null) {
            JLabel s = new JLabel(sub);
            s.setFont(BetTheme.FONT_LABEL_CAPS);
            s.setForeground(BetTheme.ON_SURFACE_VARIANT);
            p.add(s, BorderLayout.EAST);
        }
        return p;
    }

    private JButton crearBotonCuota(String label, String odds, boolean seleccionado) {
        boolean[] sel = {seleccionado};
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(sel[0] ? BetTheme.PRIMARY_CONTAINER : BetTheme.SURFACE_CONTAINER_HIGH);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BetTheme.WHITE_10);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setLayout(new BoxLayout(btn, BoxLayout.Y_AXIS));
        JLabel lLbl = new JLabel(label, SwingConstants.CENTER);
        lLbl.setFont(new Font("Inter", Font.BOLD, 10));
        lLbl.setForeground(sel[0] ? BetTheme.ON_PRIMARY_CONTAINER : BetTheme.ON_SURFACE_VARIANT);
        lLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel lOdds = new JLabel(odds, SwingConstants.CENTER);
        lOdds.setFont(new Font("Monospaced", Font.BOLD, 14));
        lOdds.setForeground(sel[0] ? BetTheme.ON_PRIMARY_CONTAINER : BetTheme.PRIMARY);
        lOdds.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.add(Box.createVerticalGlue());
        btn.add(lLbl); btn.add(Box.createVerticalStrut(3)); btn.add(lOdds);
        btn.add(Box.createVerticalGlue());
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 6, 10, 6));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            sel[0] = !sel[0];
            lLbl.setForeground(sel[0] ? BetTheme.ON_PRIMARY_CONTAINER : BetTheme.ON_SURFACE_VARIANT);
            lOdds.setForeground(sel[0] ? BetTheme.ON_PRIMARY_CONTAINER : BetTheme.PRIMARY);
            btn.repaint();
            if (sel[0]) betSlip.agregarApuesta(label, odds);
            else        betSlip.quitarApuesta(label);
            actualizarContadorBets();
        });
        return btn;
    }

    private JButton crearBotonCuotaConFlecha(String label, String odds, boolean subiendo) {
        JButton btn = crearBotonCuota(label, odds, false);
        // La flecha se pinta dentro del botón via label adicional
        return btn;
    }

    // ---------------------------------------------------------------
    // Botón flotante BetSlip + contador
    // ---------------------------------------------------------------
    private JPanel crearBotonFlotante() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 6));
        p.setOpaque(false);
        btnBetSlipFlotante = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BetTheme.PRIMARY_CONTAINER);
                g2.fillOval(0, 0, 52, 52);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblContadorBets = new JLabel("0");
        lblContadorBets.setFont(new Font("Inter", Font.BOLD, 10));
        lblContadorBets.setForeground(BetTheme.ON_PRIMARY_CONTAINER);
        btnBetSlipFlotante.setLayout(new BorderLayout());
        JLabel icon = new JLabel("☰", SwingConstants.CENTER);
        icon.setFont(new Font("Dialog", Font.BOLD, 18));
        icon.setForeground(BetTheme.ON_PRIMARY_CONTAINER);
        btnBetSlipFlotante.add(icon);
        btnBetSlipFlotante.setPreferredSize(new Dimension(52, 52));
        btnBetSlipFlotante.setOpaque(false); btnBetSlipFlotante.setContentAreaFilled(false);
        btnBetSlipFlotante.setBorderPainted(false); btnBetSlipFlotante.setFocusPainted(false);
        btnBetSlipFlotante.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBetSlipFlotante.setVisible(false);
        btnBetSlipFlotante.addActionListener(e -> onAbrirBetSlip.run());
        p.add(btnBetSlipFlotante);
        return p;
    }

    public void actualizarContadorBets() {
        int n = betSlip.contarApuestas();
        lblContadorBets.setText(String.valueOf(n));
        btnBetSlipFlotante.setVisible(n > 0);
        btnBetSlipFlotante.setToolTipText(n + " apuesta(s) en el carrito");
    }

    private void iniciarTimer() {
        timerMinuto = new Timer(60000, e -> {
            minutoActual = Math.min(minutoActual + 1, 95);
            lblMinuto.setText("LIVE " + minutoActual + "'");
        });
        timerMinuto.start();
    }

    private void iniciarPulso(JLabel dot) {
        float[] alfa = {1f};
        boolean[] subiendo = {false};
        new Timer(50, e -> {
            alfa[0] += subiendo[0] ? 0.05f : -0.05f;
            if (alfa[0] <= 0.3f) { alfa[0] = 0.3f; subiendo[0] = true; }
            if (alfa[0] >= 1.0f) { alfa[0] = 1.0f; subiendo[0] = false; }
            dot.setForeground(new Color(
                BetTheme.PRIMARY_CONTAINER.getRed(),
                BetTheme.PRIMARY_CONTAINER.getGreen(),
                BetTheme.PRIMARY_CONTAINER.getBlue(),
                (int)(alfa[0] * 255)));
        }).start();
    }
}
