import javax.swing.*;
import java.awt.*;

/**
 * Ventana de selección de autenticación — diseño según imagen:
 * Logo BetCentral centrado, botón "INICIAR SESIÓN" verde-lima,
 * botón "Crear Cuenta" con borde cyan.
 * Es la primera pantalla que ve el usuario al abrir la app.
 */
public class VentanaAuth extends JFrame {

    public VentanaAuth() {
        setTitle("BetCentral - Autenticación");
        setSize(600, 440);
        setMinimumSize(new Dimension(500, 380));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BetTheme.BACKGROUND);
        setLayout(new BorderLayout());

        add(crearContenido(), BorderLayout.CENTER);
        add(crearFooter(),    BorderLayout.SOUTH);
    }

    // ---------------------------------------------------------------
    // Contenido central
    // ---------------------------------------------------------------
    private JPanel crearContenido() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BetTheme.BACKGROUND);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Acento verde-lima muy sutil en fondo
                g2.setColor(new Color(BetTheme.PRIMARY_CONTAINER.getRed(),
                    BetTheme.PRIMARY_CONTAINER.getGreen(),
                    BetTheme.PRIMARY_CONTAINER.getBlue(), 12));
                g2.fillOval(-80, -80, 300, 300);
                g2.setColor(new Color(0x00, 0xE0, 0xFF, 10));
                g2.fillOval(getWidth() - 150, getHeight() - 100, 280, 280);
                g2.dispose();
            }
        };
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(40, 60, 30, 60));

        // --- Logo ---
        JLabel logo = new JLabel("BetCentral", SwingConstants.CENTER);
        logo.setFont(new Font("Inter", Font.BOLD, 36));
        logo.setForeground(BetTheme.PRIMARY_CONTAINER);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("PREMIUM SPORTS INTERFACE", SwingConstants.CENTER);
        sub.setFont(new Font("Inter", Font.BOLD, 10));
        sub.setForeground(BetTheme.ON_SURFACE_VARIANT);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        sub.setBorder(BorderFactory.createEmptyBorder(4, 0, 40, 0));

        // --- Botón INICIAR SESIÓN ---
        JButton btnLogin = new JButton("INICIAR SESIÓN") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed()
                    ? BetTheme.PRIMARY_FIXED_DIM : BetTheme.PRIMARY_CONTAINER;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                // Glow efecto
                if (!getModel().isPressed()) {
                    g2.setColor(new Color(BetTheme.PRIMARY_CONTAINER.getRed(),
                        BetTheme.PRIMARY_CONTAINER.getGreen(),
                        BetTheme.PRIMARY_CONTAINER.getBlue(), 60));
                    g2.setStroke(new BasicStroke(4f));
                    g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnLogin.setFont(new Font("Inter", Font.BOLD, 15));
        btnLogin.setForeground(BetTheme.ON_PRIMARY_CONTAINER);
        btnLogin.setOpaque(false);
        btnLogin.setContentAreaFilled(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.addActionListener(e -> abrirLogin());

        // --- Botón Crear Cuenta ---
        JButton btnRegistro = new JButton("Crear Cuenta") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()
                    ? new Color(0x00, 0xE0, 0xFF, 30)
                    : BetTheme.BACKGROUND);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                // Borde cyan
                g2.setColor(new Color(0x00, 0xE0, 0xFF));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnRegistro.setFont(new Font("Inter", Font.BOLD, 15));
        btnRegistro.setForeground(new Color(0x00, 0xE0, 0xFF));
        btnRegistro.setOpaque(false);
        btnRegistro.setContentAreaFilled(false);
        btnRegistro.setBorderPainted(false);
        btnRegistro.setFocusPainted(false);
        btnRegistro.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRegistro.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        btnRegistro.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRegistro.addActionListener(e -> abrirRegistro());

        p.add(logo);
        p.add(sub);
        p.add(btnLogin);
        p.add(Box.createVerticalStrut(16));
        p.add(btnRegistro);

        return p;
    }

    // ---------------------------------------------------------------
    // Footer
    // ---------------------------------------------------------------
    private JPanel crearFooter() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BetTheme.BACKGROUND);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BetTheme.WHITE_10),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        JLabel copy = new JLabel("© 2026 BETCENTRAL GLOBAL  •  PLAY RESPONSIBLY");
        copy.setFont(new Font("Inter", Font.PLAIN, 9));
        copy.setForeground(new Color(BetTheme.ON_SURFACE_VARIANT.getRed(),
            BetTheme.ON_SURFACE_VARIANT.getGreen(),
            BetTheme.ON_SURFACE_VARIANT.getBlue(), 100));
        p.add(copy, BorderLayout.WEST);

        // Indicador SYSTEM STATUS
        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        statusRow.setOpaque(false);
        JLabel status = new JLabel("SYSTEM STATUS: OPTIMAL");
        status.setFont(new Font("Monospaced", Font.BOLD, 9));
        status.setForeground(BetTheme.PRIMARY_CONTAINER);
        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Dialog", Font.BOLD, 8));
        dot.setForeground(BetTheme.PRIMARY_CONTAINER);
        statusRow.add(status);
        statusRow.add(dot);
        // Pulso del dot
        Timer t = new Timer(800, null);
        float[] a = {1f}; boolean[] up = {false};
        t.addActionListener(ev -> {
            a[0] += up[0] ? 0.1f : -0.1f;
            if (a[0] <= 0.3f) { a[0] = 0.3f; up[0] = true; }
            if (a[0] >= 1.0f) { a[0] = 1.0f; up[0] = false; }
            dot.setForeground(new Color(BetTheme.PRIMARY_CONTAINER.getRed(),
                BetTheme.PRIMARY_CONTAINER.getGreen(),
                BetTheme.PRIMARY_CONTAINER.getBlue(), (int)(a[0]*255)));
        });
        t.start();
        p.add(statusRow, BorderLayout.EAST);
        return p;
    }

    // ---------------------------------------------------------------
    // Navegación
    // ---------------------------------------------------------------
    private void abrirLogin() {
        VentanaLogin login = new VentanaLogin(this);
        login.setVisible(true);
        setVisible(false);
    }

    private void abrirRegistro() {
        VentanaRegistro reg = new VentanaRegistro(this);
        reg.setVisible(true);
        setVisible(false);
    }
}
