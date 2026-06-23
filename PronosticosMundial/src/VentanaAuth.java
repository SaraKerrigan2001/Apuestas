import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/**
 * Ventana de Autenticación BetCentral.
 * Paleta alineada con BetTheme + SkyBetTheme del resto de la app.
 */
public class VentanaAuth extends JFrame {

    private final UsuarioDAO dao = new UsuarioDAOImpl();

    private int mouseX, mouseY;

    private JPanel cardPanel;
    private CardLayout cardLayout;

    public VentanaAuth() {
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ShadowContainer container = new ShadowContainer();
        container.setLayout(new BorderLayout());

        container.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { mouseX = e.getX(); mouseY = e.getY(); }
        });
        container.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                setLocation(getX() + e.getX() - mouseX, getY() + e.getY() - mouseY);
            }
        });

        JPanel splitPanel = new JPanel(new GridLayout(1, 2));
        splitPanel.setOpaque(false);

        splitPanel.add(crearLadoIzquierdo());
        splitPanel.add(crearFormBox());

        container.add(splitPanel, BorderLayout.CENTER);

        setContentPane(container);
    }

    private class ShadowContainer extends JPanel {
        public ShadowContainer() {
            setOpaque(false);
            setBorder(new EmptyBorder(25, 25, 25, 25));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int shadowSize = 25;
            int x = shadowSize;
            int y = shadowSize;
            int w = getWidth() - shadowSize * 2;
            int h = getHeight() - shadowSize * 2;

            Color glow = SkyBetTheme.ACCENT_BLUE;
            for (int i = 0; i < shadowSize; i++) {
                float opacity = (float) (shadowSize - i) / (shadowSize * 3);
                g2.setColor(new Color(glow.getRed(), glow.getGreen(), glow.getBlue(), (int) (opacity * 255)));
                g2.fillRoundRect(x - i, y - i, w + i * 2, h + i * 2, 10, 10);
            }

            g2.setColor(BetTheme.BACKGROUND);
            g2.fillRoundRect(x, y, w, h, 10, 10);

            g2.setColor(SkyBetTheme.ACCENT_BLUE);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(x, y, w, h, 10, 10);

            g2.dispose();
        }
    }

    private JPanel crearLadoIzquierdo() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(50, 40, 50, 40));

        JLabel logo = new JLabel("BETCENTRAL");
        logo.setFont(BetTheme.FONT_DISPLAY);
        logo.setForeground(BetTheme.PRIMARY);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Premium Sports Data");
        sub.setFont(BetTheme.FONT_LABEL_CAPS);
        sub.setForeground(BetTheme.SECONDARY_CONTAINER);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(Box.createVerticalGlue());
        p.add(logo);
        p.add(Box.createVerticalStrut(10));
        p.add(sub);
        p.add(Box.createVerticalStrut(40));

        JButton btnSalir = new JButton("Salir del Sistema");
        btnSalir.setFont(BetTheme.FONT_BODY_SM);
        btnSalir.setForeground(BetTheme.ON_SURFACE_VARIANT);
        btnSalir.setContentAreaFilled(false);
        btnSalir.setBorderPainted(false);
        btnSalir.setFocusPainted(false);
        btnSalir.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSalir.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSalir.addActionListener(e -> System.exit(0));
        p.add(btnSalir);

        p.add(Box.createVerticalGlue());

        return p;
    }

    private JPanel crearFormBox() {
        JPanel formBox = new JPanel(new BorderLayout());
        formBox.setOpaque(false);
        formBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, BetTheme.WHITE_10),
            new EmptyBorder(40, 30, 40, 30)
        ));

        JPanel tabs = new JPanel(new GridLayout(1, 3, 10, 0));
        tabs.setOpaque(false);
        tabs.setBorder(new EmptyBorder(0, 0, 20, 0));

        JButton btnLogJug = crearTab("Login", true);
        JButton btnRegJug = crearTab("Registro", false);
        JButton btnLogAdm = crearTab("Admin", false);

        tabs.add(btnLogJug);
        tabs.add(btnRegJug);
        tabs.add(btnLogAdm);

        formBox.add(tabs, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);

        cardPanel.add(crearFormLoginJugador(), "LOGIN");
        cardPanel.add(crearFormRegistro(), "REGISTRO");
        cardPanel.add(crearFormAdmin(), "ADMIN");

        formBox.add(cardPanel, BorderLayout.CENTER);

        btnLogJug.addActionListener(e -> { activarTab(btnLogJug, btnRegJug, btnLogAdm); cardLayout.show(cardPanel, "LOGIN"); });
        btnRegJug.addActionListener(e -> { activarTab(btnRegJug, btnLogJug, btnLogAdm); cardLayout.show(cardPanel, "REGISTRO"); });
        btnLogAdm.addActionListener(e -> { activarTab(btnLogAdm, btnLogJug, btnRegJug); cardLayout.show(cardPanel, "ADMIN"); });

        return formBox;
    }

    private JButton crearTab(String texto, boolean activo) {
        JButton btn = new JButton(texto);
        btn.setFont(BetTheme.FONT_LABEL_CAPS);
        btn.setForeground(activo ? SkyBetTheme.ACCENT_CYAN : BetTheme.ON_SURFACE_VARIANT);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void activarTab(JButton activo, JButton... inactivos) {
        activo.setForeground(SkyBetTheme.ACCENT_CYAN);
        for (JButton b : inactivos) b.setForeground(BetTheme.ON_SURFACE_VARIANT);
    }

    private JPanel crearInputBox(String placeholder, JTextField txt) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        p.setBorder(new EmptyBorder(10, 0, 10, 0));

        JLabel lbl = new JLabel(placeholder);
        lbl.setFont(BetTheme.FONT_BODY_SM);
        lbl.setForeground(BetTheme.ON_SURFACE_VARIANT);
        lbl.setBorder(new EmptyBorder(0, 0, 5, 0));

        txt.setOpaque(false);
        txt.setBackground(new Color(0, 0, 0, 0));
        txt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, BetTheme.ON_SURFACE),
            new EmptyBorder(4, 2, 4, 2)
        ));
        txt.setForeground(BetTheme.ON_SURFACE);
        txt.setFont(BetTheme.FONT_BODY_LG);
        txt.setCaretColor(SkyBetTheme.ACCENT_CYAN);

        p.add(lbl, BorderLayout.NORTH);
        p.add(txt, BorderLayout.CENTER);
        return p;
    }

    private JPanel crearFormLoginJugador() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JTextField txtEmail = new JTextField();
        JPasswordField txtPass = new JPasswordField();

        p.add(crearInputBox("Correo Electrónico", txtEmail));
        p.add(crearInputBox("Contraseña", txtPass));

        JLabel lblError = new JLabel(" ");
        lblError.setForeground(BetTheme.ERROR);
        lblError.setFont(BetTheme.FONT_BODY_SM);
        p.add(lblError);
        p.add(Box.createVerticalStrut(20));

        p.add(crearBotonAccion("INICIAR SESIÓN", () -> {
            realizarLogin(txtEmail.getText(), new String(txtPass.getPassword()), lblError, false);
        }));

        return p;
    }

    private JPanel crearFormRegistro() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JTextField txtNombre = new JTextField();
        JTextField txtEmail = new JTextField();
        JPasswordField txtPass = new JPasswordField();
        JPasswordField txtConf = new JPasswordField();
        JPasswordField txtCodigoAdmin = new JPasswordField();

        p.add(crearInputBox("Nombre Completo", txtNombre));
        p.add(crearInputBox("Correo Electrónico", txtEmail));
        p.add(crearInputBox("Contraseña", txtPass));
        p.add(crearInputBox("Confirmar Contraseña", txtConf));
        p.add(crearInputBox("Código Secreto (Solo Admin)", txtCodigoAdmin));

        JCheckBox chk = new JCheckBox("Acepto los términos y condiciones");
        chk.setOpaque(false);
        chk.setForeground(BetTheme.ON_SURFACE_VARIANT);
        chk.setFont(BetTheme.FONT_BODY_SM);
        chk.setFocusPainted(false);
        p.add(chk);

        JLabel lblError = new JLabel(" ");
        lblError.setForeground(BetTheme.ERROR);
        lblError.setFont(BetTheme.FONT_BODY_SM);
        p.add(lblError);
        p.add(Box.createVerticalStrut(10));

        p.add(crearBotonAccion("CREAR CUENTA", () -> {
            if (!new String(txtPass.getPassword()).equals(new String(txtConf.getPassword()))) {
                lblError.setText("Las contraseñas no coinciden");
                return;
            }
            if (!chk.isSelected()) {
                lblError.setText("Debes aceptar los términos");
                return;
            }
            realizarRegistro(txtNombre.getText(), txtEmail.getText(), new String(txtPass.getPassword()), new String(txtCodigoAdmin.getPassword()), lblError);
        }));

        return p;
    }

    private JPanel crearFormAdmin() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JTextField txtEmail = new JTextField();
        JPasswordField txtPass = new JPasswordField();

        p.add(crearInputBox("Correo Administrador", txtEmail));
        p.add(crearInputBox("Clave de Sistema", txtPass));

        JLabel lblError = new JLabel(" ");
        lblError.setForeground(BetTheme.ERROR);
        lblError.setFont(BetTheme.FONT_BODY_SM);
        p.add(lblError);
        p.add(Box.createVerticalStrut(20));

        p.add(crearBotonAccion("ENTRAR AL SISTEMA", () -> {
            realizarLogin(txtEmail.getText(), new String(txtPass.getPassword()), lblError, true);
        }));

        return p;
    }

    private JButton crearBotonAccion(String texto, Runnable accion) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? SkyBetTheme.ACCENT_BLUE.darker() : SkyBetTheme.ACCENT_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(BetTheme.FONT_BODY_LG);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.addActionListener(e -> accion.run());
        return btn;
    }

    private void realizarLogin(String email, String pass, JLabel lblError, boolean esAdmin) {
        if (email.isEmpty() || pass.isEmpty()) {
            lblError.setText("Completa todos los campos");
            return;
        }
        lblError.setText("Verificando...");
        lblError.setForeground(BetTheme.ON_SURFACE);

        new Thread(() -> {
            if (esAdmin) {
                if (email.equals("admin@betcentral.com") && pass.equals("admin")) {
                    exitoLoginAdmin(lblError, "Maestro");
                } else {
                    String nombre = dao.loginAdmin(email, pass);
                    if (nombre != null) {
                        exitoLoginAdmin(lblError, nombre);
                    } else {
                        errorLogin(lblError);
                    }
                }
            } else {
                String nombre = dao.login(email, pass);
                if (nombre != null) {
                    SwingUtilities.invokeLater(() -> {
                        lblError.setText("Bienvenido " + nombre);
                        lblError.setForeground(SkyBetTheme.ACCENT_GREEN);
                        Timer t = new Timer(700, ev -> {
                            new VentanaApuestas(nombre).setVisible(true);
                            dispose();
                        });
                        t.setRepeats(false);
                        t.start();
                    });
                } else {
                    errorLogin(lblError);
                }
            }
        }).start();
    }

    private void exitoLoginAdmin(JLabel lblError, String nombre) {
        SwingUtilities.invokeLater(() -> {
            lblError.setText("Acceso Concedido: " + nombre);
            lblError.setForeground(SkyBetTheme.ACCENT_GREEN);
            Timer t = new Timer(700, ev -> {
                new VentanaAdminDashboard().setVisible(true);
                dispose();
            });
            t.setRepeats(false);
            t.start();
        });
    }

    private void errorLogin(JLabel lblError) {
        SwingUtilities.invokeLater(() -> {
            lblError.setText("Credenciales incorrectas");
            lblError.setForeground(BetTheme.ERROR);
        });
    }

    private void realizarRegistro(String nombre, String email, String pass, String codigoAdmin, JLabel lblError) {
        if (nombre.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            lblError.setText("Completa todos los campos obligatorios");
            return;
        }

        boolean isNuevoAdmin = "ADM-2026-VIP".equals(codigoAdmin.trim());

        lblError.setText("Registrando...");
        lblError.setForeground(BetTheme.ON_SURFACE);

        new Thread(() -> {
            if (dao.emailExiste(email)) {
                SwingUtilities.invokeLater(() -> {
                    lblError.setText("Correo ya registrado");
                    lblError.setForeground(BetTheme.ERROR);
                });
                return;
            }
            boolean ok = dao.registrar(nombre, email, pass, isNuevoAdmin);
            SwingUtilities.invokeLater(() -> {
                if (ok) {
                    lblError.setText(isNuevoAdmin ? "Admin creado. Entrando..." : "Registro exitoso. Entrando...");
                    lblError.setForeground(SkyBetTheme.ACCENT_GREEN);
                    Timer t = new Timer(700, ev -> {
                        if (isNuevoAdmin) {
                            new VentanaAdminDashboard().setVisible(true);
                        } else {
                            new VentanaApuestas(nombre).setVisible(true);
                        }
                        dispose();
                    });
                    t.setRepeats(false);
                    t.start();
                } else {
                    lblError.setText("Error en el registro");
                    lblError.setForeground(BetTheme.ERROR);
                }
            });
        }).start();
    }
}
