import javax.swing.*;
import java.awt.*;

/**
 * Pantalla de Inicio de Sesión — diseño BetCentral.
 * Layout corregido: todo centrado dentro del card.
 */
public class VentanaLogin extends JFrame {

    private final VentanaAuth ventanaAuth;
    private final UsuarioDAO  dao;

    private JTextField     txtEmail;
    private JPasswordField txtPassword;
    private JLabel         lblError;
    private boolean        passVisible = false;

    public VentanaLogin(VentanaAuth auth) {
        this.ventanaAuth = auth;
        this.dao = new UsuarioDAOImpl();

        setTitle("BetCentral - Iniciar Sesión");
        setSize(560, 640);
        setMinimumSize(new Dimension(480, 580));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BetTheme.BACKGROUND);
        setLayout(new BorderLayout());

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                ventanaAuth.setVisible(true);
                dispose();
            }
        });

        add(crearCuerpo(), BorderLayout.CENTER);
        add(crearFooter(), BorderLayout.SOUTH);
    }

    // ---------------------------------------------------------------
    // Cuerpo completo
    // ---------------------------------------------------------------
    private JPanel crearCuerpo() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BetTheme.BACKGROUND);
        p.setBorder(BorderFactory.createEmptyBorder(28, 50, 16, 50));

        // Logo
        JLabel logo = new JLabel("BETCENTRAL", SwingConstants.CENTER);
        logo.setFont(new Font("Inter", Font.BOLD, 30));
        logo.setForeground(BetTheme.PRIMARY_CONTAINER);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("PREMIUM SPORTS INTERFACE", SwingConstants.CENTER);
        sub.setFont(new Font("Inter", Font.BOLD, 9));
        sub.setForeground(BetTheme.ON_SURFACE_VARIANT);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        sub.setBorder(BorderFactory.createEmptyBorder(4, 0, 22, 0));

        p.add(logo);
        p.add(sub);
        p.add(crearCard());
        return p;
    }

    // ---------------------------------------------------------------
    // Card con todos los campos
    // ---------------------------------------------------------------
    private JPanel crearCard() {
        JPanel card = new JPanel() {
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
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ---- Email ----
        card.add(lbl("CORREO ELECTRÓNICO"));
        card.add(Box.createVerticalStrut(4));
        txtEmail = new JTextField();
        card.add(campoConIcono(txtEmail, "✉", null));
        card.add(Box.createVerticalStrut(16));

        // ---- Contraseña: label + ¿Olvidé? en misma fila ----
        JPanel passHeader = new JPanel(new BorderLayout());
        passHeader.setOpaque(false);
        passHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        passHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        passHeader.add(lbl("CONTRASEÑA"), BorderLayout.WEST);
        JLabel forgot = new JLabel("¿Olvidé mi contraseña?");
        forgot.setFont(new Font("Inter", Font.BOLD, 10));
        forgot.setForeground(new Color(0x00, 0xDA, 0xF8));
        forgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        passHeader.add(forgot, BorderLayout.EAST);
        card.add(passHeader);
        card.add(Box.createVerticalStrut(4));

        // Campo contraseña con ojo
        txtPassword = new JPasswordField();
        JButton btnOjo = crearBotonOjo();
        card.add(campoConIcono(txtPassword, "🔒", btnOjo));
        card.add(Box.createVerticalStrut(6));

        // Label error
        lblError = new JLabel(" ");
        lblError.setFont(new Font("Inter", Font.PLAIN, 11));
        lblError.setForeground(BetTheme.ERROR);
        lblError.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblError.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        card.add(lblError);
        card.add(Box.createVerticalStrut(6));

        // Botón INICIAR SESIÓN
        card.add(crearBotonPrimario("INICIAR SESIÓN  →", this::realizarLogin));
        card.add(Box.createVerticalStrut(16));

        // Divisor
        card.add(divisor("O ACCEDER CON"));
        card.add(Box.createVerticalStrut(12));

        // Social
        JPanel social = new JPanel(new GridLayout(1, 2, 10, 0));
        social.setOpaque(false);
        social.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        social.setAlignmentX(Component.LEFT_ALIGNMENT);
        social.add(crearBotonSocial("G  GOOGLE"));
        social.add(crearBotonSocial("⌘  APPLE"));
        card.add(social);
        card.add(Box.createVerticalStrut(16));

        // Link registro
        JPanel linkRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        linkRow.setOpaque(false);
        linkRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel noTienes = new JLabel("¿No tienes una cuenta?");
        noTienes.setFont(new Font("Inter", Font.PLAIN, 12));
        noTienes.setForeground(BetTheme.ON_SURFACE_VARIANT);
        JLabel reg = new JLabel("Regístrate ahora");
        reg.setFont(new Font("Inter", Font.BOLD, 12));
        reg.setForeground(BetTheme.PRIMARY_CONTAINER);
        reg.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        reg.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                new VentanaRegistro(ventanaAuth).setVisible(true);
                dispose();
            }
        });
        linkRow.add(noTienes); linkRow.add(reg);
        card.add(linkRow);

        return card;
    }

    // ---------------------------------------------------------------
    // Lógica
    // ---------------------------------------------------------------
    private void realizarLogin() {
        String email = txtEmail.getText().trim();
        String pass  = new String(txtPassword.getPassword()).trim();

        lblError.setText(" ");
        lblError.setForeground(BetTheme.ERROR);

        if (email.isEmpty() || pass.isEmpty()) {
            lblError.setText("⚠  Completa todos los campos.");
            return;
        }
        if (!email.contains("@")) {
            lblError.setText("⚠  Correo electrónico inválido.");
            txtEmail.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BetTheme.ERROR),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
            return;
        }

        lblError.setText("Verificando...");
        lblError.setForeground(BetTheme.ON_SURFACE_VARIANT);

        new Thread(() -> {
            String nombre = dao.login(email, pass);
            SwingUtilities.invokeLater(() -> {
                if (nombre != null) {
                    lblError.setText("✔  Bienvenido, " + nombre + "!");
                    lblError.setForeground(BetTheme.PRIMARY_CONTAINER);
                    Timer t = new Timer(700, ev -> {
                        new VentanaApuestas().setVisible(true);
                        dispose();
                        ventanaAuth.dispose();
                    });
                    t.setRepeats(false); t.start();
                } else {
                    lblError.setText("⚠  Email o contraseña incorrectos.");
                    lblError.setForeground(BetTheme.ERROR);
                    txtPassword.setText("");
                }
            });
        }).start();
    }

    // ---------------------------------------------------------------
    // Footer
    // ---------------------------------------------------------------
    private JPanel crearFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.setBackground(BetTheme.BACKGROUND);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BetTheme.WHITE_10));
        JLabel l = new JLabel("© 2026 BETCENTRAL  •  TÉRMINOS  •  PRIVACIDAD");
        l.setFont(new Font("Inter", Font.PLAIN, 9));
        l.setForeground(new Color(BetTheme.ON_SURFACE_VARIANT.getRed(),
            BetTheme.ON_SURFACE_VARIANT.getGreen(),
            BetTheme.ON_SURFACE_VARIANT.getBlue(), 80));
        p.add(l);
        return p;
    }

    // ---------------------------------------------------------------
    // Helpers UI
    // ---------------------------------------------------------------
    private JLabel lbl(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Inter", Font.BOLD, 10));
        l.setForeground(BetTheme.ON_SURFACE_VARIANT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));
        return l;
    }

    /** Campo con icono izquierdo y botón derecho opcional */
    private JPanel campoConIcono(JComponent campo, String icono, JButton btnDer) {
        JPanel p = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BetTheme.SURFACE_CONTAINER_HIGH);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BetTheme.WHITE_10);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel ico = new JLabel("  " + icono + "  ");
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
        ico.setForeground(BetTheme.ON_SURFACE_VARIANT);

        // Estilizar campo
        campo.setOpaque(false);
        campo.setBackground(new Color(0,0,0,0));
        if (campo instanceof JTextField)
            ((JTextField) campo).setForeground(BetTheme.ON_SURFACE);
        campo.setFont(new Font("Inter", Font.PLAIN, 13));
        campo.setBorder(BorderFactory.createEmptyBorder(8, 2, 8, 8));
        if (campo instanceof JTextField)
            ((JTextField) campo).setCaretColor(BetTheme.PRIMARY_CONTAINER);

        p.add(ico, BorderLayout.WEST);
        p.add(campo, BorderLayout.CENTER);
        if (btnDer != null) p.add(btnDer, BorderLayout.EAST);
        return p;
    }

    private JButton crearBotonOjo() {
        JButton btn = new JButton("👁");
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        btn.setForeground(BetTheme.ON_SURFACE_VARIANT);
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 8));
        btn.addActionListener(e -> {
            passVisible = !passVisible;
            txtPassword.setEchoChar(passVisible ? (char)0 : '●');
            btn.setText(passVisible ? "🙈" : "👁");
        });
        return btn;
    }

    private JButton crearBotonPrimario(String texto, Runnable accion) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? BetTheme.PRIMARY_FIXED_DIM : BetTheme.PRIMARY_CONTAINER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Inter", Font.BOLD, 13));
        btn.setForeground(BetTheme.ON_PRIMARY_CONTAINER);
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        btn.addActionListener(e -> accion.run());
        return btn;
    }

    private JPanel divisor(String texto) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel lIzq = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BetTheme.WHITE_10); g.fillRect(0, getHeight()/2, getWidth(), 1);
            }
        };
        lIzq.setOpaque(false);
        JPanel lDer = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BetTheme.WHITE_10); g.fillRect(0, getHeight()/2, getWidth(), 1);
            }
        };
        lDer.setOpaque(false);

        JLabel lbl = new JLabel(texto, SwingConstants.CENTER);
        lbl.setFont(new Font("Inter", Font.BOLD, 9));
        lbl.setForeground(BetTheme.ON_SURFACE_VARIANT);

        p.add(lIzq, BorderLayout.WEST);
        p.add(lbl,  BorderLayout.CENTER);
        p.add(lDer, BorderLayout.EAST);
        return p;
    }

    private JButton crearBotonSocial(String texto) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()
                    ? BetTheme.SURFACE_BRIGHT : BetTheme.SURFACE_CONTAINER_HIGH);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BetTheme.WHITE_5);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Inter", Font.BOLD, 11));
        btn.setForeground(BetTheme.ON_SURFACE_VARIANT);
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
