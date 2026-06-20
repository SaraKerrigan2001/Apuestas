import javax.swing.*;
import java.awt.*;

/**
 * Banner hero — diseño según imagen: fondo verde oscuro degradado,
 * badge rojo "FEATURED MATCH", hora, equipo1 vs equipo2 en negrita,
 * subtítulo liga, botón "BET NOW" en verde-lima esquina inferior derecha.
 */
public class HeroBanner extends JPanel {

    public HeroBanner() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        add(crearCard(), BorderLayout.CENTER);
    }

    private JPanel crearCard() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Fondo degradado verde oscuro como en la imagen
                GradientPaint gp = new GradientPaint(
                    0, 0,             new Color(0x0A, 0x1A, 0x0A),
                    getWidth(), getHeight(), new Color(0x06, 0x0E, 0x06)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                // Líneas del campo de fútbol muy sutiles
                g2.setColor(new Color(0xFF, 0xFF, 0xFF, 8));
                g2.setStroke(new BasicStroke(1f));
                int cx = getWidth() / 2;
                g2.drawOval(cx - 55, getHeight()/2 - 55, 110, 110);
                g2.drawLine(cx, 0, cx, getHeight());
                g2.drawRect(cx - 80, getHeight() - 60, 160, 60);
                // Borde sutil
                g2.setColor(new Color(0xFF, 0xFF, 0xFF, 15));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(0, 180));

        // Contenido
        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setOpaque(false);
        contenido.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // Badge rojo + hora (igual que imagen)
        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        badgeRow.setOpaque(false);

        JLabel badge = new JLabel("  FEATURED MATCH  ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xFF, 0x3B, 0x30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 3, 3);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Inter", Font.BOLD, 9));
        badge.setForeground(Color.WHITE);
        badge.setOpaque(false);

        JLabel hora = new JLabel("Tonight, 21:00");
        hora.setFont(new Font("Inter", Font.PLAIN, 11));
        hora.setForeground(new Color(0xFF, 0xFF, 0xFF, 140));

        badgeRow.add(badge);
        badgeRow.add(hora);
        badgeRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Título partido — igual que imagen
        JLabel titulo = new JLabel("<html>"
            + "<span style='color:#ffffff;font-size:20px'><b>Real Madrid</b></span>"
            + " <span style='color:#c3f400;font-size:20px'><b>vs</b></span>"
            + " <span style='color:#ffffff;font-size:20px'><b>Barcelona</b></span>"
            + "</html>");
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        titulo.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));

        // Liga
        JLabel liga = new JLabel("La Liga  •  Matchday 32");
        liga.setFont(new Font("Inter", Font.PLAIN, 11));
        liga.setForeground(new Color(0xC4, 0xC9, 0xAC));
        liga.setAlignmentX(Component.LEFT_ALIGNMENT);

        contenido.add(badgeRow);
        contenido.add(Box.createVerticalStrut(55));
        contenido.add(titulo);
        contenido.add(liga);

        card.add(contenido, BorderLayout.CENTER);

        // Botón BET NOW — verde-lima esquina inferior derecha
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 10));
        btnPanel.setOpaque(false);
        JButton betNow = BetTheme.primaryButton("BET NOW");
        betNow.setFont(new Font("Inter", Font.BOLD, 12));
        btnPanel.add(betNow);
        card.add(btnPanel, BorderLayout.SOUTH);

        return card;
    }
}
