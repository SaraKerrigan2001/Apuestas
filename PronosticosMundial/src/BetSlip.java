import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla "Bet Slip" — carrito de apuestas.
 * Mapea el HTML: BetCentral - Your Bet Slip
 * Lista de apuestas + stake input + quick add + summary + PLACE BET
 */
public class BetSlip extends JPanel {

    // Modelo de una apuesta
    private static class Apuesta {
        String liga, partido, tipo, seleccion, odds;
        Apuesta(String liga, String partido, String tipo, String seleccion, String odds) {
            this.liga = liga; this.partido = partido;
            this.tipo = tipo; this.seleccion = seleccion; this.odds = odds;
        }
    }

    private final List<Apuesta>  apuestas   = new ArrayList<>();
    private final Runnable       onVolver;

    // Componentes dinámicos
    private JPanel    panelLista;
    private JLabel    lblContador;
    private JLabel    lblTotalOdds;
    private JLabel    lblPayout;
    private JTextField txtStake;
    private double    saldo = 1250.50;

    // Apuestas demo precargadas
    private static final Apuesta[] DEMO = {
        new Apuesta("PREMIER LEAGUE", "Arsenal vs Man City",        "1X2",        "Arsenal to Win",          "2.45"),
        new Apuesta("LA LIGA",         "Real Madrid vs Barcelona",   "OVER/UNDER", "Over 2.5 Goals",          "1.85"),
        new Apuesta("CHAMPIONS LEAGUE","Bayern Munich vs PSG",       "BTTS",       "Both Teams to Score - Yes","1.72")
    };

    public BetSlip(Runnable onVolver) {
        this.onVolver = onVolver;
        setLayout(new BorderLayout());
        setBackground(BetTheme.BACKGROUND);
        setOpaque(true);

        // Cargar apuestas demo
        for (Apuesta a : DEMO) apuestas.add(a);

        add(crearEncabezado(), BorderLayout.NORTH);

        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBackground(BetTheme.BACKGROUND);
        contenido.setOpaque(true);
        contenido.setBorder(BorderFactory.createEmptyBorder(10, 14, 80, 14));

        panelLista = new JPanel();
        panelLista.setLayout(new BoxLayout(panelLista, BoxLayout.Y_AXIS));
        panelLista.setOpaque(false);

        contenido.add(panelLista);
        contenido.add(Box.createVerticalStrut(16));
        contenido.add(crearStakeSection());
        contenido.add(Box.createVerticalStrut(16));
        contenido.add(crearResumen());

        JScrollPane scroll = new JScrollPane(contenido,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BetTheme.BACKGROUND);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(4, 0));
        add(scroll, BorderLayout.CENTER);

        refrescarLista();
    }

    // ---------------------------------------------------------------
    // Encabezado: título + contador + Clear All
    // ---------------------------------------------------------------
    private JPanel crearEncabezado() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BetTheme.SURFACE);
        p.setOpaque(true);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BetTheme.WHITE_10),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        JLabel icon = new JLabel("☰");
        icon.setFont(new Font("Dialog", Font.PLAIN, 18));
        icon.setForeground(BetTheme.PRIMARY_CONTAINER);

        JLabel titulo = new JLabel("BET SLIP");
        titulo.setFont(new Font("Inter", Font.BOLD, 16));
        titulo.setForeground(BetTheme.PRIMARY);

        lblContador = new JLabel(String.valueOf(apuestas.size())) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BetTheme.PRIMARY_CONTAINER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblContador.setFont(new Font("Inter", Font.BOLD, 10));
        lblContador.setForeground(BetTheme.ON_PRIMARY_CONTAINER);
        lblContador.setOpaque(false);
        lblContador.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        left.add(icon); left.add(titulo); left.add(lblContador);
        p.add(left, BorderLayout.WEST);

        JButton btnClear = new JButton("CLEAR ALL");
        btnClear.setFont(new Font("Inter", Font.BOLD, 10));
        btnClear.setForeground(BetTheme.ON_SURFACE_VARIANT);
        btnClear.setOpaque(false); btnClear.setContentAreaFilled(false);
        btnClear.setBorderPainted(false); btnClear.setFocusPainted(false);
        btnClear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClear.addActionListener(e -> limpiarTodo());
        p.add(btnClear, BorderLayout.EAST);

        return p;
    }

    // ---------------------------------------------------------------
    // Lista de apuestas
    // ---------------------------------------------------------------
    private void refrescarLista() {
        panelLista.removeAll();
        for (int i = 0; i < apuestas.size(); i++) {
            final int idx = i;
            panelLista.add(crearCardApuesta(apuestas.get(i), idx));
            panelLista.add(Box.createVerticalStrut(6));
        }
        lblContador.setText(String.valueOf(apuestas.size()));
        panelLista.revalidate();
        panelLista.repaint();
        calcularTotales();
    }

    private JPanel crearCardApuesta(Apuesta a, int idx) {
        JPanel card = new JPanel(new BorderLayout(10, 6)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BetTheme.SURFACE_CONTAINER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(BetTheme.WHITE_10);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));
        card.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        // Info izquierda
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel lLiga = new JLabel(a.liga);
        lLiga.setFont(new Font("Inter", Font.BOLD, 9));
        lLiga.setForeground(BetTheme.ON_SURFACE_VARIANT);

        JLabel lPartido = new JLabel(a.partido);
        lPartido.setFont(new Font("Inter", Font.BOLD, 13));
        lPartido.setForeground(BetTheme.PRIMARY);

        JPanel tipoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        tipoRow.setOpaque(false);
        JLabel lTipo = new JLabel("  " + a.tipo + "  ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(BetTheme.SECONDARY_CONTAINER.getRed(),
                    BetTheme.SECONDARY_CONTAINER.getGreen(),
                    BetTheme.SECONDARY_CONTAINER.getBlue(), 25));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.setColor(new Color(BetTheme.SECONDARY_CONTAINER.getRed(),
                    BetTheme.SECONDARY_CONTAINER.getGreen(),
                    BetTheme.SECONDARY_CONTAINER.getBlue(), 50));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lTipo.setFont(new Font("Inter", Font.BOLD, 9));
        lTipo.setForeground(BetTheme.ON_SECONDARY_CONTAINER);
        lTipo.setOpaque(false);

        JLabel lSel = new JLabel(a.seleccion);
        lSel.setFont(new Font("Inter", Font.BOLD, 12));
        lSel.setForeground(BetTheme.SECONDARY_CONTAINER);

        tipoRow.add(lTipo); tipoRow.add(lSel);

        info.add(lLiga);
        info.add(Box.createVerticalStrut(2));
        info.add(lPartido);
        info.add(Box.createVerticalStrut(3));
        info.add(tipoRow);
        card.add(info, BorderLayout.CENTER);

        // Derecha: X + cuota
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);

        JButton btnX = new JButton("✕");
        btnX.setFont(new Font("Inter", Font.PLAIN, 14));
        btnX.setForeground(BetTheme.ON_SURFACE_VARIANT);
        btnX.setOpaque(false); btnX.setContentAreaFilled(false);
        btnX.setBorderPainted(false); btnX.setFocusPainted(false);
        btnX.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnX.setAlignmentX(Component.RIGHT_ALIGNMENT);
        btnX.addActionListener(e -> {
            apuestas.remove(idx);
            refrescarLista();
        });

        JLabel lOdds = new JLabel(a.odds);
        lOdds.setFont(new Font("Monospaced", Font.BOLD, 18));
        lOdds.setForeground(BetTheme.PRIMARY_CONTAINER);
        lOdds.setAlignmentX(Component.RIGHT_ALIGNMENT);

        right.add(btnX);
        right.add(Box.createVerticalStrut(4));
        right.add(lOdds);
        card.add(right, BorderLayout.EAST);

        return card;
    }

    // ---------------------------------------------------------------
    // Sección de stake
    // ---------------------------------------------------------------
    private JPanel crearStakeSection() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        JLabel lbl = new JLabel("WAGER AMOUNT ($)");
        lbl.setFont(new Font("Inter", Font.BOLD, 10));
        lbl.setForeground(BetTheme.ON_SURFACE_VARIANT);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl);
        p.add(Box.createVerticalStrut(6));

        // Input
        JPanel inputPanel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(11, 14, 20));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BetTheme.WHITE_10);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
            }
        };
        inputPanel.setOpaque(false);
        inputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        inputPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel dollar = new JLabel("  $  ");
        dollar.setFont(new Font("Inter", Font.BOLD, 20));
        dollar.setForeground(BetTheme.PRIMARY);
        inputPanel.add(dollar, BorderLayout.WEST);

        txtStake = new JTextField("100");
        txtStake.setFont(new Font("Monospaced", Font.BOLD, 22));
        txtStake.setForeground(BetTheme.PRIMARY);
        txtStake.setBackground(new Color(11, 14, 20));
        txtStake.setCaretColor(BetTheme.PRIMARY_CONTAINER);
        txtStake.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 8));
        txtStake.addActionListener(e -> calcularTotales());
        txtStake.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { calcularTotales(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { calcularTotales(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { calcularTotales(); }
        });
        inputPanel.add(txtStake, BorderLayout.CENTER);

        p.add(inputPanel);
        p.add(Box.createVerticalStrut(8));

        // Quick add
        JPanel quickPanel = new JPanel(new GridLayout(1, 4, 6, 0));
        quickPanel.setOpaque(false);
        quickPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        quickPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        quickPanel.add(crearBtnQuick("+$10",  10,   false));
        quickPanel.add(crearBtnQuick("+$50",  50,   false));
        quickPanel.add(crearBtnQuick("+$100", 100,  false));
        quickPanel.add(crearBtnQuick("MAX",   -1,   true));
        p.add(quickPanel);

        return p;
    }

    private JButton crearBtnQuick(String label, double monto, boolean isMax) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isMax
                    ? new Color(BetTheme.PRIMARY_CONTAINER.getRed(), BetTheme.PRIMARY_CONTAINER.getGreen(), BetTheme.PRIMARY_CONTAINER.getBlue(), 25)
                    : BetTheme.SURFACE_CONTAINER_HIGH);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(isMax
                    ? new Color(BetTheme.PRIMARY_CONTAINER.getRed(), BetTheme.PRIMARY_CONTAINER.getGreen(), BetTheme.PRIMARY_CONTAINER.getBlue(), 50)
                    : BetTheme.WHITE_5);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Monospaced", Font.BOLD, 12));
        btn.setForeground(isMax ? BetTheme.PRIMARY_CONTAINER : BetTheme.PRIMARY);
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            try {
                double actual = Double.parseDouble(txtStake.getText().trim());
                if (monto == -1) txtStake.setText(String.format("%.2f", saldo));
                else             txtStake.setText(String.format("%.2f", actual + monto));
            } catch (NumberFormatException ex) {
                txtStake.setText(monto == -1 ? String.format("%.2f", saldo) : String.valueOf((int)monto));
            }
        });
        return btn;
    }

    // ---------------------------------------------------------------
    // Resumen y botón PLACE BET
    // ---------------------------------------------------------------
    private JPanel crearResumen() {
        JPanel card = new JPanel(new BorderLayout(0, 12)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BetTheme.SURFACE_CONTAINER_LOW);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BetTheme.WHITE_10);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Total odds
        JPanel rowOdds = new JPanel(new BorderLayout());
        rowOdds.setOpaque(false);
        JLabel lOddsLbl = new JLabel("TOTAL ODDS");
        lOddsLbl.setFont(new Font("Inter", Font.BOLD, 10));
        lOddsLbl.setForeground(BetTheme.ON_SURFACE_VARIANT);
        lblTotalOdds = new JLabel("0.00");
        lblTotalOdds.setFont(new Font("Monospaced", Font.BOLD, 20));
        lblTotalOdds.setForeground(BetTheme.PRIMARY);
        rowOdds.add(lOddsLbl,   BorderLayout.WEST);
        rowOdds.add(lblTotalOdds, BorderLayout.EAST);

        // Separador
        JPanel sep = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BetTheme.WHITE_5);
                g.fillRect(0, 0, getWidth(), 1);
            }
        };
        sep.setPreferredSize(new Dimension(0, 1));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setOpaque(false);

        // Payout
        JPanel rowPayout = new JPanel(new BorderLayout());
        rowPayout.setOpaque(false);
        JLabel lPayLbl = new JLabel("POTENTIAL PAYOUT");
        lPayLbl.setFont(new Font("Inter", Font.BOLD, 10));
        lPayLbl.setForeground(BetTheme.ON_SURFACE_VARIANT);
        lblPayout = new JLabel("$0.00");
        lblPayout.setFont(new Font("Monospaced", Font.BOLD, 24));
        lblPayout.setForeground(BetTheme.PRIMARY_CONTAINER);
        rowPayout.add(lPayLbl, BorderLayout.WEST);
        rowPayout.add(lblPayout, BorderLayout.EAST);

        // Botón PLACE BET
        JButton btnPlace = new JButton("PLACE BET  →") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Glow effect
                g2.setColor(new Color(BetTheme.PRIMARY_CONTAINER.getRed(),
                    BetTheme.PRIMARY_CONTAINER.getGreen(),
                    BetTheme.PRIMARY_CONTAINER.getBlue(), 40));
                g2.fillRoundRect(-4, -4, getWidth()+8, getHeight()+8, 14, 14);
                g2.setColor(getModel().isPressed() ? BetTheme.PRIMARY_FIXED_DIM : BetTheme.PRIMARY_CONTAINER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnPlace.setFont(new Font("Inter", Font.BOLD, 16));
        btnPlace.setForeground(BetTheme.ON_PRIMARY_CONTAINER);
        btnPlace.setOpaque(false); btnPlace.setContentAreaFilled(false);
        btnPlace.setBorderPainted(false); btnPlace.setFocusPainted(false);
        btnPlace.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnPlace.setBorder(BorderFactory.createEmptyBorder(14, 0, 14, 0));
        btnPlace.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        btnPlace.addActionListener(e -> mostrarConfirmacion());

        // Leyenda seguridad
        JLabel seguridad = new JLabel("🔒  Encrypted Transaction", SwingConstants.CENTER);
        seguridad.setFont(new Font("Inter", Font.PLAIN, 9));
        seguridad.setForeground(new Color(BetTheme.ON_SURFACE_VARIANT.getRed(),
            BetTheme.ON_SURFACE_VARIANT.getGreen(),
            BetTheme.ON_SURFACE_VARIANT.getBlue(), 128));

        // Ensamblar
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);
        inner.add(rowOdds);
        inner.add(Box.createVerticalStrut(10));
        inner.add(sep);
        inner.add(Box.createVerticalStrut(10));
        inner.add(rowPayout);
        inner.add(Box.createVerticalStrut(12));
        inner.add(btnPlace);
        inner.add(Box.createVerticalStrut(6));
        inner.add(seguridad);

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    // ---------------------------------------------------------------
    // Lógica de cálculo
    // ---------------------------------------------------------------
    private void calcularTotales() {
        double totalOdds = 1.0;
        for (Apuesta a : apuestas) {
            try { totalOdds *= Double.parseDouble(a.odds); }
            catch (NumberFormatException ignored) {}
        }
        double stake = 0;
        try { stake = Double.parseDouble(txtStake.getText().trim()); }
        catch (NumberFormatException ignored) {}

        double payout = stake * totalOdds;
        if (lblTotalOdds != null) lblTotalOdds.setText(String.format("%.2f", totalOdds));
        if (lblPayout    != null) lblPayout.setText("$" + String.format("%.2f", payout));
    }

    private void limpiarTodo() {
        apuestas.clear();
        refrescarLista();
    }

    private void mostrarConfirmacion() {
        if (apuestas.isEmpty()) {
            ToastNotificacion.mostrar(null,
                "No hay apuestas en el carrito.", ToastNotificacion.Tipo.AVISO);
            return;
        }
        double stake = 0;
        try { stake = Double.parseDouble(txtStake.getText().trim()); }
        catch (NumberFormatException ignored) {}
        if (stake <= 0 || stake > saldo) {
            ToastNotificacion.mostrar(null,
                "Monto inválido o saldo insuficiente.", ToastNotificacion.Tipo.ERROR);
            return;
        }
        saldo -= stake;
        ToastNotificacion.mostrar(null,
            "¡Apuesta de $" + String.format("%.2f", stake) + " colocada con éxito!",
            ToastNotificacion.Tipo.EXITO);
        limpiarTodo();
        if (onVolver != null) onVolver.run();
    }

    // ---------------------------------------------------------------
    // API pública — usada por MatchDetail
    // ---------------------------------------------------------------
    public void agregarApuesta(String seleccion, String odds) {
        // Evitar duplicados por selección
        for (Apuesta a : apuestas)
            if (a.seleccion.equals(seleccion)) return;
        apuestas.add(new Apuesta("LIVE MARKET", "Manchester City vs Arsenal",
            "LIVE", seleccion, odds));
        refrescarLista();
    }

    public void quitarApuesta(String seleccion) {
        apuestas.removeIf(a -> a.seleccion.equals(seleccion));
        refrescarLista();
    }

    public int contarApuestas() { return apuestas.size(); }
}
