import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;

/**
 * Paleta de colores y utilidades del diseño Sky Bet.
 * Azul oscuro profundo, acentos cyan/verde, estilo premium de apuestas.
 */
public class SkyBetTheme {

    // --- Fondos ---
    public static final Color BG_DARK        = new Color(0x0D, 0x10, 0x1F); // fondo principal
    public static final Color BG_PANEL       = new Color(0x13, 0x18, 0x2E); // paneles
    public static final Color BG_CARD        = new Color(0x1A, 0x21, 0x3A); // cards
    public static final Color BG_ROW         = new Color(0x1F, 0x27, 0x45); // filas tabla
    public static final Color BG_ROW_ALT     = new Color(0x17, 0x1E, 0x38); // filas alternas
    public static final Color BG_SIDEBAR     = new Color(0x10, 0x14, 0x28); // sidebar
    public static final Color BG_HEADER      = new Color(0x0A, 0x0D, 0x1E); // topbar
    public static final Color BG_INPUT       = new Color(0x1E, 0x26, 0x42); // inputs
    public static final Color BG_BTN_LIVE    = new Color(0x1A, 0x8A, 0xFF); // botón azul
    public static final Color BG_BTN_ODDS    = new Color(0x1E, 0x26, 0x42); // botón cuota
    public static final Color BG_BTN_ODDS_SEL= new Color(0x1A, 0x8A, 0xFF); // cuota seleccionada

    // --- Acentos ---
    public static final Color ACCENT_BLUE    = new Color(0x1A, 0x8A, 0xFF);
    public static final Color ACCENT_CYAN    = new Color(0x00, 0xD4, 0xFF);
    public static final Color ACCENT_GREEN   = new Color(0x00, 0xE0, 0x7A);
    public static final Color ACCENT_YELLOW  = new Color(0xFF, 0xC8, 0x00);
    public static final Color ACCENT_RED     = new Color(0xFF, 0x3B, 0x3B);

    // --- Texto ---
    public static final Color TEXT_PRIMARY   = new Color(0xFF, 0xFF, 0xFF);
    public static final Color TEXT_SECONDARY = new Color(0xA0, 0xB0, 0xCC);
    public static final Color TEXT_MUTED     = new Color(0x60, 0x70, 0x90);
    public static final Color TEXT_LIVE      = new Color(0xFF, 0x4D, 0x4D);
    public static final Color TEXT_ODDS      = new Color(0x1A, 0x8A, 0xFF);
    public static final Color TEXT_GAIN      = new Color(0x00, 0xE0, 0x7A);

    // --- Bordes ---
    public static final Color BORDER         = new Color(0x2A, 0x35, 0x55);
    public static final Color BORDER_BRIGHT  = new Color(0x3A, 0x4A, 0x70);

    // --- Tipografía ---
    public static final Font FONT_TITLE      = new Font("Inter", Font.BOLD,   22);
    public static final Font FONT_SUBTITLE   = new Font("Inter", Font.BOLD,   15);
    public static final Font FONT_BODY       = new Font("Inter", Font.PLAIN,  12);
    public static final Font FONT_BODY_BOLD  = new Font("Inter", Font.BOLD,   12);
    public static final Font FONT_SMALL      = new Font("Inter", Font.PLAIN,  10);
    public static final Font FONT_SMALL_BOLD = new Font("Inter", Font.BOLD,   10);
    public static final Font FONT_MONO       = new Font("Monospaced", Font.BOLD, 13);
    public static final Font FONT_ODDS       = new Font("Monospaced", Font.BOLD, 12);

    // ---------------------------------------------------------------
    // Helpers de componentes
    // ---------------------------------------------------------------

    /** Panel con fondo redondeado */
    public static JPanel roundPanel(Color bg, int radio) {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), radio, radio);
                g2.dispose();
                super.paintComponent(g);
            }
        };
    }

    /** Botón de cuota estilo Sky Bet */
    public static JButton oddsButton(String label, String value) {
        JButton btn = new JButton() {
            boolean sel = false;
            {
                setLayout(new BorderLayout(0, 2));
                JLabel lLbl = new JLabel(label, SwingConstants.CENTER);
                lLbl.setFont(FONT_SMALL);
                lLbl.setForeground(TEXT_SECONDARY);
                JLabel lVal = new JLabel(value, SwingConstants.CENTER);
                lVal.setFont(FONT_ODDS);
                lVal.setForeground(ACCENT_BLUE);
                add(lLbl, BorderLayout.NORTH);
                add(lVal, BorderLayout.CENTER);
                addActionListener(e -> {
                    sel = !sel;
                    lLbl.setForeground(sel ? TEXT_PRIMARY  : TEXT_SECONDARY);
                    lVal.setForeground(sel ? TEXT_PRIMARY  : ACCENT_BLUE);
                    repaint();
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(sel ? ACCENT_BLUE : BG_BTN_ODDS);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(sel ? ACCENT_BLUE.brighter() : BORDER);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));
        return btn;
    }

    /** Botón primario azul */
    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? ACCENT_BLUE.darker() : ACCENT_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BODY_BOLD);
        btn.setForeground(TEXT_PRIMARY);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        return btn;
    }

    /** Campo de texto estilo Sky Bet */
    public static JTextField textField() {
        JTextField tf = new JTextField();
        tf.setBackground(BG_INPUT);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT_CYAN);
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    /** Etiqueta de badge (LIVE, HOT, etc.) */
    public static JLabel badge(String texto, Color fondo, Color textColor) {
        JLabel l = new JLabel("  " + texto + "  ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fondo);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setFont(FONT_SMALL_BOLD);
        l.setForeground(textColor);
        l.setOpaque(false);
        return l;
    }
}
