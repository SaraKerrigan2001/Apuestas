import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Sección "Live Now" — diseño según imagen:
 * Cards planos con fondo oscuro, equipos + marcador a la izquierda,
 * minuto + liga a la derecha, y 3 botones de cuota anchos y planos abajo.
 */
public class LiveSection extends JPanel {

    private Timer  pulseTimer;
    private float  pulseAlpha = 1.0f;
    private boolean pulseUp   = false;
    private JLabel dotLive;

    public LiveSection() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 16, 8, 16));

        add(crearCabecera());
        add(Box.createVerticalStrut(10));
        add(crearLiveCard("Arsenal", "2", "Liverpool", "1",
                "74'", "PREMIER LEAGUE", "1.85", "3.40", "4.20", 2));
        add(Box.createVerticalStrut(2));
        add(crearLiveCard("Napoli", "0", "Juventus", "0",
                "12'", "SERIE A", "2.10", "3.15", "3.80", -1));

        iniciarPulso();
    }

    // ---------------------------------------------------------------
    // Cabecera LIVE NOW
    // ---------------------------------------------------------------
    private JPanel crearCabecera() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        dotLive = new JLabel("●");
        dotLive.setFont(new Font("Dialog", Font.BOLD, 10));
        dotLive.setForeground(BetTheme.PRIMARY_CONTAINER);
        left.add(dotLive);

        JLabel titulo = new JLabel("LIVE NOW");
        titulo.setFont(new Font("Inter", Font.BOLD, 14));
        titulo.setForeground(BetTheme.PRIMARY);
        left.add(titulo);
        row.add(left, BorderLayout.WEST);

        // Badge mercados
        JLabel badge = new JLabel("128 MARKETS") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BetTheme.SURFACE_CONTAINER_HIGH);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(BetTheme.FONT_MONO_SM);
        badge.setForeground(BetTheme.PRIMARY_CONTAINER);
        badge.setOpaque(false);
        badge.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        row.add(badge, BorderLayout.EAST);

        return row;
    }

    // ---------------------------------------------------------------
    // Card de partido en vivo — diseño plano según imagen
    // ---------------------------------------------------------------
    private JPanel crearLiveCard(String eq1, String g1, String eq2, String g2,
                                  String minuto, String liga,
                                  String cuota1, String cuotaX, String cuota2,
                                  int seleccionado) {
        JPanel card = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BetTheme.SURFACE_CONTAINER);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Línea separadora inferior muy sutil
                g2.setColor(BetTheme.WHITE_10);
                g2.fillRect(0, getHeight() - 1, getWidth(), 1);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        // --- Fila superior: equipos + marcador | minuto + liga ---
        JPanel top = new JPanel(new BorderLayout(0, 6));
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(12, 14, 8, 14));

        // Equipos
        JPanel equipos = new JPanel();
        equipos.setLayout(new BoxLayout(equipos, BoxLayout.Y_AXIS));
        equipos.setOpaque(false);
        equipos.add(crearFilaEquipo(eq1, g1));
        equipos.add(Box.createVerticalStrut(6));
        equipos.add(crearFilaEquipo(eq2, g2));
        top.add(equipos, BorderLayout.WEST);

        // Minuto + liga
        JPanel derecha = new JPanel();
        derecha.setLayout(new BoxLayout(derecha, BoxLayout.Y_AXIS));
        derecha.setOpaque(false);
        JLabel lblMin = new JLabel(minuto);
        lblMin.setFont(new Font("Monospaced", Font.BOLD, 12));
        lblMin.setForeground(BetTheme.PRIMARY_CONTAINER);
        lblMin.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JLabel lblLiga = new JLabel(liga);
        lblLiga.setFont(new Font("Inter", Font.BOLD, 9));
        lblLiga.setForeground(BetTheme.ON_SURFACE_VARIANT);
        lblLiga.setAlignmentX(Component.RIGHT_ALIGNMENT);
        derecha.add(lblMin);
        derecha.add(Box.createVerticalStrut(3));
        derecha.add(lblLiga);
        top.add(derecha, BorderLayout.EAST);

        card.add(top, BorderLayout.CENTER);

        // --- Fila de cuotas: 3 botones anchos planos ---
        JPanel cuotasRow = new JPanel(new GridLayout(1, 3, 1, 0));
        cuotasRow.setOpaque(false);
        cuotasRow.setPreferredSize(new Dimension(0, 38));

        cuotasRow.add(crearBotonCuota("HOME",  cuota1, seleccionado == 0));
        cuotasRow.add(crearBotonCuota("DRAW",  cuotaX, seleccionado == 1));
        cuotasRow.add(crearBotonCuota("AWAY",  cuota2, seleccionado == 2));

        card.add(cuotasRow, BorderLayout.SOUTH);
        return card;
    }

    // Fila equipo + gol
    private JPanel crearFilaEquipo(String nombre, String goles) {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        fila.setOpaque(false);
        JLabel lblNombre = new JLabel(nombre);
        lblNombre.setFont(new Font("Inter", Font.BOLD, 13));
        lblNombre.setForeground(BetTheme.PRIMARY);
        JLabel lblGoles = new JLabel(goles);
        lblGoles.setFont(new Font("Monospaced", Font.BOLD, 15));
        lblGoles.setForeground(BetTheme.PRIMARY_CONTAINER);
        fila.add(lblNombre);
        fila.add(lblGoles);
        return fila;
    }

    // Botón de cuota plano ancho — seleccionado en verde-lima
    private JButton crearBotonCuota(String label, String odds, boolean seleccionado) {
        boolean[] sel = {seleccionado};
        JLabel lLbl = new JLabel(label, SwingConstants.CENTER);
        lLbl.setFont(new Font("Inter", Font.BOLD, 9));
        JLabel lOdds = new JLabel(odds, SwingConstants.CENTER);
        lOdds.setFont(new Font("Monospaced", Font.BOLD, 13));

        JButton btn = new JButton() {
            {
                setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                lLbl.setAlignmentX(CENTER_ALIGNMENT);
                lOdds.setAlignmentX(CENTER_ALIGNMENT);
                add(Box.createVerticalGlue());
                add(lLbl);
                add(lOdds);
                add(Box.createVerticalGlue());
                actualizarColores();
            }
            void actualizarColores() {
                lLbl.setForeground(sel[0]
                    ? BetTheme.ON_PRIMARY_CONTAINER
                    : BetTheme.ON_SURFACE_VARIANT);
                lOdds.setForeground(sel[0]
                    ? BetTheme.ON_PRIMARY_CONTAINER
                    : BetTheme.PRIMARY);
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(sel[0]
                    ? BetTheme.PRIMARY_CONTAINER
                    : BetTheme.SURFACE_CONTAINER_HIGH);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Borde derecho separador
                g2.setColor(BetTheme.WHITE_10);
                g2.fillRect(getWidth()-1, 0, 1, getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.addActionListener(e -> {
            sel[0] = !sel[0];
            btn.repaint();
        });
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void iniciarPulso() {
        pulseTimer = new Timer(50, (ActionEvent e) -> {
            if (pulseUp) {
                pulseAlpha += 0.04f;
                if (pulseAlpha >= 1.0f) { pulseAlpha = 1.0f; pulseUp = false; }
            } else {
                pulseAlpha -= 0.04f;
                if (pulseAlpha <= 0.3f) { pulseAlpha = 0.3f; pulseUp = true; }
            }
            dotLive.setForeground(new Color(
                BetTheme.PRIMARY_CONTAINER.getRed(),
                BetTheme.PRIMARY_CONTAINER.getGreen(),
                BetTheme.PRIMARY_CONTAINER.getBlue(),
                (int)(pulseAlpha * 255)));
        });
        pulseTimer.start();
    }

    public void detenerPulso() { if (pulseTimer != null) pulseTimer.stop(); }
}
