import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;

/**
 * Paleta de colores y utilidades de estilo de BetCentral.
 * Mapea exactamente los tokens del diseño HTML/Tailwind.
 */
public class BetTheme {

    // --- Colores principales ---
    public static final Color BACKGROUND              = new Color(0x10, 0x13, 0x19);
    public static final Color SURFACE                 = new Color(0x10, 0x13, 0x19);
    public static final Color SURFACE_CONTAINER       = new Color(0x1D, 0x20, 0x26);
    public static final Color SURFACE_CONTAINER_LOW   = new Color(0x19, 0x1C, 0x22);
    public static final Color SURFACE_CONTAINER_HIGH  = new Color(0x27, 0x2A, 0x31);
    public static final Color SURFACE_BRIGHT          = new Color(0x36, 0x39, 0x40);

    public static final Color PRIMARY                 = new Color(0xFF, 0xFF, 0xFF);
    public static final Color PRIMARY_CONTAINER       = new Color(0xC3, 0xF4, 0x00);
    public static final Color ON_PRIMARY_CONTAINER    = new Color(0x55, 0x6D, 0x00);
    public static final Color PRIMARY_FIXED_DIM       = new Color(0xAB, 0xD6, 0x00);

    public static final Color SECONDARY               = new Color(0xB9, 0xF1, 0xFF);
    public static final Color SECONDARY_CONTAINER     = new Color(0x00, 0xE0, 0xFF);
    public static final Color ON_SECONDARY_CONTAINER  = new Color(0x00, 0x5F, 0x6D);

    public static final Color ON_SURFACE              = new Color(0xE1, 0xE2, 0xEB);
    public static final Color ON_SURFACE_VARIANT      = new Color(0xC4, 0xC9, 0xAC);
    public static final Color OUTLINE_VARIANT         = new Color(0x44, 0x49, 0x33);

    public static final Color ERROR                   = new Color(0xFF, 0xB4, 0xAB);
    public static final Color ON_ERROR                = new Color(0x69, 0x00, 0x05);
    public static final Color ERROR_CONTAINER         = new Color(0x93, 0x00, 0x0A);

    public static final Color WHITE_10                = new Color(0xFF, 0xFF, 0xFF, 25);
    public static final Color WHITE_5                 = new Color(0xFF, 0xFF, 0xFF, 13);

    // --- Tipografía ---
    public static final Font FONT_DISPLAY    = new Font("Inter", Font.BOLD,   28);
    public static final Font FONT_HEADLINE   = new Font("Inter", Font.BOLD,   20);
    public static final Font FONT_TITLE      = new Font("Inter", Font.BOLD,   16);
    public static final Font FONT_BODY_LG    = new Font("Inter", Font.PLAIN,  14);
    public static final Font FONT_BODY_SM    = new Font("Inter", Font.PLAIN,  12);
    public static final Font FONT_LABEL_CAPS = new Font("Inter", Font.BOLD,   10);
    public static final Font FONT_MONO       = new Font("Monospaced", Font.BOLD, 13);
    public static final Font FONT_MONO_SM    = new Font("Monospaced", Font.BOLD, 11);

    // --- Bordes ---
    public static Border etchedBorder() {
        return BorderFactory.createLineBorder(new Color(0xFF, 0xFF, 0xFF, 25), 1);
    }

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xFF, 0xFF, 0xFF, 25), 1),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        );
    }

    public static Border panelPadding(int v, int h) {
        return BorderFactory.createEmptyBorder(v, h, v, h);
    }

    // --- Helpers de paneles ---
    public static JPanel darkPanel(Color bg) {
        JPanel p = new JPanel();
        p.setBackground(bg);
        p.setOpaque(true);
        return p;
    }

    /** Botón estilo primario (fondo amarillo-verde) */
    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text.toUpperCase()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? PRIMARY_FIXED_DIM : PRIMARY_CONTAINER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_LABEL_CAPS);
        btn.setForeground(ON_PRIMARY_CONTAINER);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }

    /** Botón estilo cuota (fondo surface-container-high) */
    public static JButton oddsButton(String label, String odds) {
        JPanel inner = new JPanel(new BorderLayout(0, 2));
        inner.setOpaque(false);
        JLabel lbl = new JLabel(label.toUpperCase(), SwingConstants.CENTER);
        lbl.setFont(FONT_LABEL_CAPS);
        lbl.setForeground(ON_SURFACE_VARIANT);
        JLabel val = new JLabel(odds, SwingConstants.CENTER);
        val.setFont(FONT_MONO);
        val.setForeground(PRIMARY);
        inner.add(lbl, BorderLayout.NORTH);
        inner.add(val, BorderLayout.CENTER);

        JButton btn = new JButton() {
            boolean selected = false;
            { setLayout(new BorderLayout()); add(inner); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(selected ? PRIMARY_CONTAINER : SURFACE_CONTAINER_HIGH);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(WHITE_5);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override public void fireActionPerformed(java.awt.event.ActionEvent e) {
                selected = !selected;
                lbl.setForeground(selected ? ON_PRIMARY_CONTAINER : ON_SURFACE_VARIANT);
                val.setForeground(selected ? ON_PRIMARY_CONTAINER : PRIMARY);
                repaint();
                super.fireActionPerformed(e);
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 6, 8, 6));
        return btn;
    }
}
