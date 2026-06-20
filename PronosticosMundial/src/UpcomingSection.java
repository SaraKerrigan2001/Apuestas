import javax.swing.*;
import java.awt.*;

/**
 * Sección "Upcoming Matches" — diseño según imagen:
 * Título centrado "UPCOMING MATCHES", filas con hora+liga pequeño arriba,
 * nombre partido en negrita, cuotas 3 cajas a la derecha.
 */
public class UpcomingSection extends JPanel {

    private static final Object[][] PARTIDOS = {
        {"20:45", "LIGUE 1",        "PSG vs Marseille",   "1.45", "4.50", "6.70"},
        {"TOM",   "UCL",            "Man City vs Bayern",  "1.95", "3.75", "3.50"},
        {"TOM",   "PREMIER LEAGUE", "Chelsea vs Spurs",    "2.25", "3.20", "3.10"}
    };

    public UpcomingSection() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(8, 0, 80, 0));

        // Título centrado "UPCOMING MATCHES"
        JLabel titulo = new JLabel("UPCOMING MATCHES", SwingConstants.CENTER);
        titulo.setFont(new Font("Inter", Font.BOLD, 13));
        titulo.setForeground(BetTheme.PRIMARY);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        titulo.setBorder(BorderFactory.createEmptyBorder(8, 0, 10, 0));
        titulo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        add(titulo);

        for (Object[] partido : PARTIDOS) {
            add(crearFilaPartido(
                (String) partido[0], (String) partido[1], (String) partido[2],
                (String) partido[3], (String) partido[4], (String) partido[5]
            ));
        }
    }

    private JPanel crearFilaPartido(String hora, String liga, String nombre,
                                     String c1, String c2, String c3) {
        JPanel fila = new JPanel(new BorderLayout(0, 0)) {
            private boolean hover = false;
            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) { hover = true; repaint(); }
                    public void mouseExited(java.awt.event.MouseEvent e)  { hover = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(hover
                    ? BetTheme.SURFACE_CONTAINER_HIGH
                    : BetTheme.SURFACE_CONTAINER_LOW);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Línea separadora inferior
                g2.setColor(BetTheme.WHITE_10);
                g2.fillRect(0, getHeight()-1, getWidth(), 1);
                g2.dispose();
            }
        };
        fila.setOpaque(false);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        fila.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 12));

        // Info izquierda
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        // Hora + liga pequeño
        JPanel metaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        metaRow.setOpaque(false);
        JLabel lblHora = new JLabel(hora);
        lblHora.setFont(new Font("Monospaced", Font.BOLD, 10));
        lblHora.setForeground(BetTheme.ON_SURFACE_VARIANT);
        JLabel lblLiga = new JLabel(liga);
        lblLiga.setFont(new Font("Inter", Font.BOLD, 9));
        lblLiga.setForeground(BetTheme.ON_SURFACE_VARIANT);
        metaRow.add(lblHora);
        metaRow.add(lblLiga);

        // Nombre del partido
        JLabel lblNombre = new JLabel(nombre);
        lblNombre.setFont(new Font("Inter", Font.BOLD, 13));
        lblNombre.setForeground(BetTheme.PRIMARY);

        info.add(metaRow);
        info.add(Box.createVerticalStrut(2));
        info.add(lblNombre);
        fila.add(info, BorderLayout.CENTER);

        // Cuotas — 3 cajas a la derecha
        JPanel cuotas = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        cuotas.setOpaque(false);
        for (String cuota : new String[]{c1, c2, c3}) {
            cuotas.add(crearCuota(cuota));
        }
        fila.add(cuotas, BorderLayout.EAST);

        return fila;
    }

    private JPanel crearCuota(String valor) {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BetTheme.SURFACE_CONTAINER_HIGH);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.setColor(BetTheme.WHITE_5);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 4, 4);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(52, 36));
        p.setLayout(new BorderLayout());
        JLabel lbl = new JLabel(valor, SwingConstants.CENTER);
        lbl.setFont(BetTheme.FONT_MONO);
        lbl.setForeground(BetTheme.PRIMARY);
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }
}
