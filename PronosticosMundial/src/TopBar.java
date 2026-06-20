import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Barra superior fija — equivale al <header> del HTML.
 * Contiene: avatar circular | logo | botón Ingresar | botón campana | botón soporte
 */
public class TopBar extends JPanel {
    
    private JButton btnCampana;
    private int notificacionesSinLeer = 0;
    private java.util.List<String> notificaciones = new ArrayList<>();

    public TopBar() {
        setLayout(new BorderLayout());
        setBackground(new Color(0x10, 0x13, 0x19, 204));
        setOpaque(true);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BetTheme.WHITE_10),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        setPreferredSize(new Dimension(0, 56));

        // Izquierda: avatar circular con inicial "U"
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(crearAvatar());
        add(left, BorderLayout.WEST);

        // Centro: logo BetCentral en verde-lima
        JLabel logo = new JLabel("BetCentral");
        logo.setFont(new Font("Inter", Font.BOLD, 20));
        logo.setForeground(BetTheme.PRIMARY_CONTAINER);
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        add(logo, BorderLayout.CENTER);

        // Derecha: botón INGRESAR + campana + botón ···
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(crearBotonIngresar());
        right.add(crearBotonCampana());
        right.add(crearBotonMenu());
        add(right, BorderLayout.EAST);
    }

    private JPanel crearAvatar() {
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Círculo de fondo
                g2.setColor(BetTheme.SURFACE_CONTAINER_HIGH);
                g2.fillOval(0, 0, 32, 32);
                // Inicial
                g2.setColor(BetTheme.PRIMARY_CONTAINER);
                g2.setFont(new Font("Inter", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                String inicial = "U";
                int x = (32 - fm.stringWidth(inicial)) / 2;
                int y = (32 - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(inicial, x, y);
                // Borde etched
                g2.setColor(BetTheme.WHITE_10);
                g2.drawOval(0, 0, 31, 31);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(32, 32));
        avatar.setOpaque(false);
        return avatar;
    }

    private JButton crearBotonIngresar() {
        JButton btn = BetTheme.primaryButton("Cerrar Sesión");
        btn.setPreferredSize(new Dimension(130, 34));
        btn.addActionListener(e -> {
            java.awt.Window win = SwingUtilities.getWindowAncestor(this);
            int conf = JOptionPane.showConfirmDialog(win,
                "¿Deseas cerrar sesión?",
                "Cerrar Sesión", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            if (conf == JOptionPane.YES_OPTION) {
                new VentanaAuth().setVisible(true);
                if (win != null) win.dispose();
            }
        });
        return btn;
    }

    private JButton crearBotonCampana() {
        btnCampana = new JButton("🔔") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? BetTheme.SURFACE_BRIGHT : BetTheme.SURFACE_CONTAINER_HIGH);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BetTheme.WHITE_10);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
                
                // Indicador de notificaciones sin leer
                if (notificacionesSinLeer > 0) {
                    g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(220, 50, 50));
                    g2.fillOval(getWidth() - 12, -2, 14, 14);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Arial", Font.BOLD, 9));
                    String count = notificacionesSinLeer > 9 ? "9+" : String.valueOf(notificacionesSinLeer);
                    FontMetrics fm = g2.getFontMetrics();
                    int x = getWidth() - 12 + (14 - fm.stringWidth(count)) / 2;
                    int y = -2 + (14 - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString(count, x, y);
                    g2.dispose();
                }
            }
        };
        btnCampana.setFont(new Font("Dialog", Font.PLAIN, 16));
        btnCampana.setForeground(BetTheme.PRIMARY_CONTAINER);
        btnCampana.setPreferredSize(new Dimension(38, 34));
        btnCampana.setOpaque(false);
        btnCampana.setContentAreaFilled(false);
        btnCampana.setBorderPainted(false);
        btnCampana.setFocusPainted(false);
        btnCampana.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCampana.setToolTipText("Notificaciones (" + notificacionesSinLeer + ")");
        
        btnCampana.addActionListener(e -> mostrarNotificaciones());
        
        return btnCampana;
    }

    private void mostrarNotificaciones() {
        JFrame ventanaPrincipal = (JFrame) SwingUtilities.getWindowAncestor(this);
        
        if (notificaciones.isEmpty()) {
            ToastNotificacion.mostrar(ventanaPrincipal, "No hay notificaciones", ToastNotificacion.Tipo.INFO);
            return;
        }
        
        // Crear diálogo de notificaciones
        JDialog dlgNotificaciones = new JDialog(ventanaPrincipal, "Notificaciones", true);
        dlgNotificaciones.setSize(400, 300);
        dlgNotificaciones.setLocationRelativeTo(ventanaPrincipal);
        dlgNotificaciones.getContentPane().setBackground(SkyBetTheme.BG_DARK);
        dlgNotificaciones.setLayout(new BorderLayout(10, 10));
        
        // Lista de notificaciones
        JList<String> listNotificaciones = new JList<>(notificaciones.toArray(new String[0]));
        listNotificaciones.setBackground(SkyBetTheme.BG_INPUT);
        listNotificaciones.setForeground(SkyBetTheme.TEXT_PRIMARY);
        
        JScrollPane scrollPane = new JScrollPane(listNotificaciones);
        scrollPane.setBackground(SkyBetTheme.BG_DARK);
        scrollPane.setOpaque(true);
        dlgNotificaciones.add(scrollPane, BorderLayout.CENTER);
        
        // Botones inferiores
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panelBotones.setBackground(SkyBetTheme.BG_DARK);
        
        JButton btnLimpiar = BetTheme.primaryButton("Limpiar todas");
        btnLimpiar.addActionListener(e -> {
            notificaciones.clear();
            notificacionesSinLeer = 0;
            btnCampana.repaint();
            dlgNotificaciones.dispose();
            ToastNotificacion.mostrar(ventanaPrincipal, "Notificaciones limpiadas", ToastNotificacion.Tipo.EXITO);
        });
        
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setBackground(SkyBetTheme.BG_INPUT);
        btnCerrar.setForeground(SkyBetTheme.TEXT_PRIMARY);
        btnCerrar.addActionListener(e -> dlgNotificaciones.dispose());
        
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnCerrar);
        dlgNotificaciones.add(panelBotones, BorderLayout.SOUTH);
        
        notificacionesSinLeer = 0;
        btnCampana.repaint();
        dlgNotificaciones.setVisible(true);
    }

    /**
     * Método público para agregar notificaciones desde otras partes de la aplicación
     */
    public void agregarNotificacion(String mensaje) {
        notificaciones.add(0, "[" + java.time.LocalTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + mensaje);
        notificacionesSinLeer++;
        btnCampana.repaint();
        btnCampana.setToolTipText("Notificaciones (" + notificacionesSinLeer + ")");
    }

    /** Botón ··· (tres puntos) estilo imagen */
    private JButton crearBotonMenu() {
        JButton btn = new JButton("···") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()
                    ? BetTheme.SURFACE_BRIGHT : BetTheme.SURFACE_CONTAINER_HIGH);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BetTheme.WHITE_10);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Inter", Font.BOLD, 16));
        btn.setForeground(BetTheme.ON_SURFACE_VARIANT);
        btn.setPreferredSize(new Dimension(38, 34));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton crearBotonSoporte() {
        JButton btn = new JButton("⌨") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? BetTheme.SURFACE_BRIGHT : BetTheme.SURFACE_CONTAINER_HIGH);
                g2.fillOval(0, 0, 38, 38);
                g2.setColor(BetTheme.WHITE_10);
                g2.drawOval(0, 0, 37, 37);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Dialog", Font.PLAIN, 16));
        btn.setForeground(BetTheme.PRIMARY_CONTAINER);
        btn.setPreferredSize(new Dimension(38, 38));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("Soporte");
        return btn;
    }
}
