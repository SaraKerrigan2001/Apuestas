import javax.swing.*;
import java.awt.*;

/**
 * Pantalla de Registro — diseño BetCentral.
 * Layout corregido: checkbox y todos los campos dentro del card.
 */
public class VentanaRegistro extends JFrame {

    private final VentanaAuth ventanaAuth;
    private final UsuarioDAO  dao;

    private JTextField     txtNombre;
    private JTextField     txtEmail;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmar;
    private JCheckBox      chkTerminos;
    private JLabel         lblError;

    public VentanaRegistro(VentanaAuth auth) {
        this.ventanaAuth = auth;
        this.dao = new UsuarioDAOImpl();

        setTitle("BetCentral - Crear Cuenta");
        setSize(560, 720);
        setMinimumSize(new Dimension(480, 660));
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

        add(crearCuerpo(),           BorderLayout.CENTER);
        add(crearFooterConfianza(),  BorderLayout.SOUTH);
    }

    // ---------------------------------------------------------------
    // Cuerpo
    // ---------------------------------------------------------------
    private JPanel crearCuerpo() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BetTheme.BACKGROUND);
        p.setBorder(BorderFactory.createEmptyBorder(22, 50, 12, 50));

        JLabel logo = new JLabel("BETCENTRAL", SwingConstants.CENTER);
        logo.setFont(new Font("Inter", Font.BOLD, 30));
        logo.setForeground(BetTheme.PRIMARY_CONTAINER);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Command Center  •  Sports Intelligence", SwingConstants.CENTER);
        sub.setFont(new Font("Inter", Font.BOLD, 9));
        sub.setForeground(BetTheme.ON_SURFACE_VARIANT);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        sub.setBorder(BorderFactory.createEmptyBorder(3, 0, 18, 0));

        p.add(logo); p.add(sub);
        p.add(crearCard());
        return p;
    }

    // ---------------------------------------------------------------
    // Card
    // ---------------------------------------------------------------
    private JPanel crearCard() {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(26, 30, 38, 200));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BetTheme.WHITE_10);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Título
        JLabel titulo = new JLabel("Crear Cuenta");
        titulo.setFont(new Font("Inter", Font.BOLD, 22));
        titulo.setForeground(BetTheme.PRIMARY);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitulo = new JLabel("Únete a la élite de las apuestas deportivas.");
        subtitulo.setFont(new Font("Inter", Font.PLAIN, 12));
        subtitulo.setForeground(BetTheme.ON_SURFACE_VARIANT);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitulo.setBorder(BorderFactory.createEmptyBorder(3, 0, 16, 0));
        card.add(titulo); card.add(subtitulo);

        // Nombre completo
        card.add(lbl("NOMBRE COMPLETO"));
        card.add(Box.createVerticalStrut(4));
        txtNombre = new JTextField();
        card.add(campoConIcono(txtNombre, "👤", null));
        card.add(Box.createVerticalStrut(12));

        // Email
        card.add(lbl("CORREO ELECTRÓNICO"));
        card.add(Box.createVerticalStrut(4));
        txtEmail = new JTextField();
        card.add(campoConIcono(txtEmail, "✉", null));
        card.add(Box.createVerticalStrut(12));

        // Contraseñas en 2 columnas
        JPanel passGrid = new JPanel(new GridLayout(1, 2, 10, 0));
        passGrid.setOpaque(false);
        passGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 86));
        passGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel colPass = new JPanel();
        colPass.setLayout(new BoxLayout(colPass, BoxLayout.Y_AXIS));
        colPass.setOpaque(false);
        colPass.add(lbl("CONTRASEÑA"));
        colPass.add(Box.createVerticalStrut(4));
        txtPassword = new JPasswordField();
        colPass.add(campoConIconoPass(txtPassword, "🔒"));

        JPanel colConf = new JPanel();
        colConf.setLayout(new BoxLayout(colConf, BoxLayout.Y_AXIS));
        colConf.setOpaque(false);
        colConf.add(lbl("CONFIRMAR"));
        colConf.add(Box.createVerticalStrut(4));
        txtConfirmar = new JPasswordField();
        colConf.add(campoConIconoPass(txtConfirmar, "🔄"));

        passGrid.add(colPass); passGrid.add(colConf);
        card.add(passGrid);
        card.add(Box.createVerticalStrut(14));

        // Checkbox términos — dentro del card
        JPanel termsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        termsRow.setOpaque(false);
        termsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        termsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        chkTerminos = new JCheckBox();
        chkTerminos.setOpaque(false);
        chkTerminos.setFocusPainted(false);
        JLabel termsLbl = new JLabel(
            "<html>Acepto los <span style='color:#00e0ff'><b>Términos y Condiciones</b></span>"
            + " y confirmo ser mayor de 18 años.</html>");
        termsLbl.setFont(new Font("Inter", Font.PLAIN, 11));
        termsLbl.setForeground(BetTheme.ON_SURFACE_VARIANT);
        termsRow.add(chkTerminos); termsRow.add(termsLbl);
        card.add(termsRow);
        card.add(Box.createVerticalStrut(8));

        // Label error
        lblError = new JLabel(" ");
        lblError.setFont(new Font("Inter", Font.PLAIN, 11));
        lblError.setForeground(BetTheme.ERROR);
        lblError.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblError.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));
        card.add(lblError);
        card.add(Box.createVerticalStrut(6));

        // Botón CREAR CUENTA
        card.add(crearBotonPrimario("CREAR CUENTA", this::realizarRegistro));
        card.add(Box.createVerticalStrut(14));

        // Divisor ¿Ya tienes cuenta?
        JPanel div = divisor("¿Ya tienes cuenta?");
        card.add(div);
        card.add(Box.createVerticalStrut(10));

        // Link login
        JLabel iniciaLink = new JLabel("Inicia Sesión", SwingConstants.CENTER);
        iniciaLink.setFont(new Font("Inter", Font.BOLD, 12));
        iniciaLink.setForeground(BetTheme.SECONDARY_CONTAINER);
        iniciaLink.setAlignmentX(Component.CENTER_ALIGNMENT);
        iniciaLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        iniciaLink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                new VentanaLogin(ventanaAuth).setVisible(true);
                dispose();
            }
        });
        card.add(iniciaLink);
        return card;
    }

    // ---------------------------------------------------------------
    // Lógica de registro
    // ---------------------------------------------------------------
    private void realizarRegistro() {
        String nombre = txtNombre.getText().trim();
        String email  = txtEmail.getText().trim();
        String pass1  = new String(txtPassword.getPassword()).trim();
        String pass2  = new String(txtConfirmar.getPassword()).trim();

        java.util.List<String> errores = new java.util.ArrayList<>();
        if (nombre.isEmpty() || nombre.length() < 2)
            errores.add("• Nombre: mínimo 2 caracteres.");
        if (email.isEmpty() || !email.contains("@") || !email.contains("."))
            errores.add("• Correo electrónico inválido.");
        if (pass1.length() < 6)
            errores.add("• Contraseña: mínimo 6 caracteres.");
        if (!pass1.equals(pass2))
            errores.add("• Las contraseñas no coinciden.");
        if (!chkTerminos.isSelected())
            errores.add("• Debes aceptar los Términos y Condiciones.");

        if (!errores.isEmpty()) {
            lblError.setText("<html>" + String.join("<br>", errores) + "</html>");
            lblError.setForeground(BetTheme.ERROR);
            return;
        }

        lblError.setText("Registrando...");
        lblError.setForeground(BetTheme.ON_SURFACE_VARIANT);

        new Thread(() -> {
            if (dao.emailExiste(email)) {
                SwingUtilities.invokeLater(() -> {
                    lblError.setText("⚠  Este correo ya está registrado.");
                    lblError.setForeground(BetTheme.ERROR);
                });
                return;
            }
            boolean ok = dao.registrar(nombre, email, pass1);
            SwingUtilities.invokeLater(() -> {
                if (ok) {
                    JOptionPane.showMessageDialog(this,
                        "✔  ¡Cuenta creada con éxito!\nYa puedes iniciar sesión.",
                        "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
                    new VentanaLogin(ventanaAuth).setVisible(true);
                    dispose();
                } else {
                    lblError.setText("⚠  Error al crear la cuenta. Intenta de nuevo.");
                    lblError.setForeground(BetTheme.ERROR);
                }
            });
        }).start();
    }

    // ---------------------------------------------------------------
    // Footer indicadores de confianza
    // ---------------------------------------------------------------
    private JPanel crearFooterConfianza() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(BetTheme.BACKGROUND);
        outer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BetTheme.WHITE_10));

        JPanel indicadores = new JPanel(new GridLayout(1, 3, 0, 0));
        indicadores.setBackground(BetTheme.BACKGROUND);
        indicadores.setBorder(BorderFactory.createEmptyBorder(8, 20, 6, 20));
        indicadores.add(indicador("🔒", "ENCRYPTED"));
        indicadores.add(indicador("⚡", "FAST PAY"));
        indicadores.add(indicador("🎧", "24/7 LIVE"));
        outer.add(indicadores, BorderLayout.CENTER);

        JLabel copy = new JLabel("© 2026 BETCENTRAL GLOBAL  •  PLAY RESPONSIBLY", SwingConstants.CENTER);
        copy.setFont(new Font("Inter", Font.PLAIN, 8));
        copy.setForeground(new Color(BetTheme.ON_SURFACE_VARIANT.getRed(),
            BetTheme.ON_SURFACE_VARIANT.getGreen(),
            BetTheme.ON_SURFACE_VARIANT.getBlue(), 80));
        copy.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        outer.add(copy, BorderLayout.SOUTH);
        return outer;
    }

    private JPanel indicador(String icono, String texto) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel ico = new JLabel(icono, SwingConstants.CENTER);
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        ico.setForeground(new Color(200, 200, 200, 120));
        ico.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel l = new JLabel(texto, SwingConstants.CENTER);
        l.setFont(new Font("Inter", Font.BOLD, 8));
        l.setForeground(new Color(BetTheme.ON_SURFACE_VARIANT.getRed(),
            BetTheme.ON_SURFACE_VARIANT.getGreen(),
            BetTheme.ON_SURFACE_VARIANT.getBlue(), 100));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(ico); p.add(l);
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

    private JPanel campoConIcono(JTextField tf, String icono, JButton btnDer) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BetTheme.SURFACE_CONTAINER_LOW);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BetTheme.WHITE_10);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel ico = new JLabel("  " + icono + "  ");
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        ico.setForeground(BetTheme.ON_SURFACE_VARIANT);
        tf.setOpaque(false);
        tf.setForeground(BetTheme.ON_SURFACE);
        tf.setCaretColor(BetTheme.PRIMARY_CONTAINER);
        tf.setFont(new Font("Inter", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createEmptyBorder(8, 2, 8, 8));
        p.add(ico, BorderLayout.WEST);
        p.add(tf,  BorderLayout.CENTER);
        if (btnDer != null) p.add(btnDer, BorderLayout.EAST);
        return p;
    }

    private JPanel campoConIconoPass(JPasswordField pf, String icono) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BetTheme.SURFACE_CONTAINER_LOW);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BetTheme.WHITE_10);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel ico = new JLabel("  " + icono + "  ");
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        ico.setForeground(BetTheme.ON_SURFACE_VARIANT);
        pf.setOpaque(false);
        pf.setForeground(BetTheme.ON_SURFACE);
        pf.setCaretColor(BetTheme.PRIMARY_CONTAINER);
        pf.setFont(new Font("Inter", Font.PLAIN, 13));
        pf.setBorder(BorderFactory.createEmptyBorder(8, 2, 8, 8));
        pf.setEchoChar('●');
        p.add(ico, BorderLayout.WEST);
        p.add(pf,  BorderLayout.CENTER);
        return p;
    }

    private JButton crearBotonPrimario(String texto, Runnable accion) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? BetTheme.PRIMARY_FIXED_DIM : BetTheme.PRIMARY_CONTAINER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                // Glow
                g2.setColor(new Color(BetTheme.PRIMARY_CONTAINER.getRed(),
                    BetTheme.PRIMARY_CONTAINER.getGreen(),
                    BetTheme.PRIMARY_CONTAINER.getBlue(), 50));
                g2.setStroke(new BasicStroke(3f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 8, 8);
                g2.dispose(); super.paintComponent(g);
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
        lbl.setFont(new Font("Inter", Font.PLAIN, 11));
        lbl.setForeground(BetTheme.ON_SURFACE_VARIANT);
        p.add(lIzq, BorderLayout.WEST);
        p.add(lbl,  BorderLayout.CENTER);
        p.add(lDer, BorderLayout.EAST);
        return p;
    }
}
