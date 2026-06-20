import javax.swing.*;
import java.awt.*;

/**
 * Barra horizontal de ligas populares.
 * Diseño según imagen: círculos con símbolo ⊕, label abajo en mayúsculas.
 * Primera liga activa con fondo cyan/secondary.
 */
public class LeaguesBar extends JPanel {

    private static final String[] NOMBRES = {
        "PREMIER LEAGUE", "LA LIGA", "BUNDESLIGA", "SERIE A", "UCL"
    };

    public LeaguesBar() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(4, 16, 4, 16));

        // Cabecera
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        JLabel titulo = new JLabel("POPULAR LEAGUES");
        titulo.setFont(new Font("Inter", Font.BOLD, 10));
        titulo.setForeground(BetTheme.ON_SURFACE_VARIANT);
        header.add(titulo, BorderLayout.WEST);

        JLabel chevron = new JLabel("›");
        chevron.setFont(new Font("Inter", Font.PLAIN, 18));
        chevron.setForeground(BetTheme.ON_SURFACE_VARIANT);
        header.add(chevron, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Fila de ligas
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
        fila.setOpaque(false);

        for (int i = 0; i < NOMBRES.length; i++) {
            fila.add(crearItemLiga(NOMBRES[i], i == 0));
        }

        JScrollPane scroll = new JScrollPane(fila,
            JScrollPane.VERTICAL_SCROLLBAR_NEVER,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel crearItemLiga(String nombre, boolean activa) {
        JPanel item = new JPanel();
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
        item.setOpaque(false);
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Círculo con símbolo ⊕
        JPanel circulo = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Fondo
                g2.setColor(activa ? BetTheme.SECONDARY_CONTAINER : BetTheme.SURFACE_CONTAINER);
                g2.fillOval(0, 0, 50, 50);
                // Borde
                g2.setColor(activa ? BetTheme.ON_SECONDARY_CONTAINER : BetTheme.WHITE_10);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(1, 1, 47, 47);
                // Símbolo ⊕ (círculo con cruz)
                Color iconColor = activa ? BetTheme.ON_SECONDARY_CONTAINER : BetTheme.ON_SURFACE_VARIANT;
                g2.setColor(iconColor);
                g2.setStroke(new BasicStroke(1.8f));
                int cx = 25, cy = 25, r = 10;
                g2.drawOval(cx - r, cy - r, r * 2, r * 2);
                g2.drawLine(cx - r, cy, cx + r, cy);
                g2.drawLine(cx, cy - r, cx, cy + r);
                g2.dispose();
            }
        };
        circulo.setPreferredSize(new Dimension(50, 50));
        circulo.setMaximumSize(new Dimension(50, 50));
        circulo.setOpaque(false);
        circulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lbl = new JLabel(nombre);
        lbl.setFont(new Font("Inter", Font.BOLD, 8));
        lbl.setForeground(activa ? BetTheme.PRIMARY : BetTheme.ON_SURFACE_VARIANT);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));

        item.add(circulo);
        item.add(lbl);
        return item;
    }
}
