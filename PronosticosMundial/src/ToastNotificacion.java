import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Notificación visual tipo "toast" que aparece en la esquina inferior derecha
 * de la ventana padre, permanece unos segundos y desaparece sola con fade-out.
 *
 * Uso:
 *   ToastNotificacion.mostrar(ventanaPadre, "Mensaje", ToastNotificacion.Tipo.EXITO);
 */
public class ToastNotificacion extends JWindow {

    public enum Tipo {
        EXITO   (new Color(34, 139, 34),  "✔"),
        ERROR   (new Color(180, 30, 30),  "✖"),
        INFO    (new Color(30, 100, 180), "ℹ"),
        AVISO   (new Color(200, 150, 0),  "⚠")
        ;
        final Color color;
        final String icono;
        Tipo(Color color, String icono) { this.color = color; this.icono = icono; }
    }

    private static final int DURACION_MS   = 2500; // tiempo visible
    private static final int FADE_PASOS    = 30;   // pasos del fade-out
    private static final int FADE_DELAY_MS = 20;   // ms entre cada paso

    private float opacidad = 1.0f;

    private ToastNotificacion(JFrame padre, String mensaje, Tipo tipo) {
        super(padre);

        // Panel principal con bordes redondeados pintados a mano
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(tipo.color);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
        panel.setOpaque(false);

        JLabel lblIcono = new JLabel(tipo.icono);
        lblIcono.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
        lblIcono.setForeground(Color.WHITE);

        JLabel lblTexto = new JLabel(mensaje);
        lblTexto.setFont(new Font("Arial", Font.BOLD, 13));
        lblTexto.setForeground(Color.WHITE);

        panel.add(lblIcono);
        panel.add(lblTexto);

        setContentPane(panel);
        pack();

        // Hacer la ventana transparente (soportado en Java 7+)
        try {
            setBackground(new Color(0, 0, 0, 0));
        } catch (Exception ignored) {}

        // Posicionar en la esquina inferior derecha del padre
        if (padre != null) {
            Point loc    = padre.getLocation();
            Dimension dp = padre.getSize();
            Dimension dt = getSize();
            setLocation(
                loc.x + dp.width  - dt.width  - 20,
                loc.y + dp.height - dt.height - 50
            );
        }
    }

    /** Muestra el toast y lo cierra automáticamente tras DURACION_MS milisegundos */
    public static void mostrar(JFrame padre, String mensaje, Tipo tipo) {
        SwingUtilities.invokeLater(() -> {
            ToastNotificacion toast = new ToastNotificacion(padre, mensaje, tipo);
            Toolkit.getDefaultToolkit().beep();
            toast.setVisible(true);

            // Timer para iniciar el fade-out después de DURACION_MS
            Timer timerEspera = new Timer(DURACION_MS, null);
            timerEspera.setRepeats(false);
            timerEspera.addActionListener(e -> {
                // Fade-out progresivo
                Timer timerFade = new Timer(FADE_DELAY_MS, null);
                timerFade.addActionListener(ev -> {
                    toast.opacidad -= (1.0f / FADE_PASOS);
                    if (toast.opacidad <= 0f) {
                        timerFade.stop();
                        toast.dispose();
                    } else {
                        try {
                            toast.setOpacity(Math.max(0f, toast.opacidad));
                        } catch (Exception ignored) {}
                        toast.repaint();
                    }
                });
                timerFade.start();
            });
            timerEspera.start();
        });
    }
}
