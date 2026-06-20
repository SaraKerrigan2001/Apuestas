import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Barra de navegación inferior fija.
 * Acepta un Runnable por cada pestaña para navegar entre paneles.
 */
public class BottomNavBar extends JPanel {

    private static final String[] ICONOS  = {"⌂", "◉", "☰", "◯"};
    private static final String[] LABELS  = {"Home", "Live", "My Bets", "Profile"};

    private int seleccionado = 0;
    private final JPanel[] items;
    private final JLabel[] iconos;
    private final JLabel[] textos;
    private final Runnable[] acciones;

    public BottomNavBar(Runnable onHome, Runnable onLive,
                        Runnable onMyBets, Runnable onProfile) {
        this.acciones = new Runnable[]{onHome, onLive, onMyBets, onProfile};
        this.items    = new JPanel[4];
        this.iconos   = new JLabel[4];
        this.textos   = new JLabel[4];

        setLayout(new GridLayout(1, 4));
        setBackground(BetTheme.SURFACE_CONTAINER);
        setOpaque(true);
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BetTheme.WHITE_10));
        setPreferredSize(new Dimension(0, 64));

        for (int i = 0; i < 4; i++) {
            items[i] = crearItem(i);
            add(items[i]);
        }
        actualizarColores();
    }

    /** Constructor sin callbacks (para uso standalone) */
    public BottomNavBar() {
        this(() -> {}, () -> {}, () -> {}, () -> {});
    }

    private JPanel crearItem(int index) {
        JPanel item = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (seleccionado == index) {
                    int pw = 72, ph = 30;
                    int px = (getWidth() - pw) / 2;
                    int py = (getHeight() - ph) / 2 - 4;
                    g2.setColor(BetTheme.PRIMARY_CONTAINER);
                    g2.fillRoundRect(px, py, pw, ph, 15, 15);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
        item.setOpaque(false);
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        iconos[index] = new JLabel(ICONOS[index], SwingConstants.CENTER);
        iconos[index].setFont(new Font("Dialog", Font.PLAIN, 18));
        iconos[index].setAlignmentX(Component.CENTER_ALIGNMENT);

        textos[index] = new JLabel(LABELS[index].toUpperCase(), SwingConstants.CENTER);
        textos[index].setFont(new Font("Inter", Font.BOLD, 9));
        textos[index].setAlignmentX(Component.CENTER_ALIGNMENT);

        item.add(Box.createVerticalGlue());
        item.add(iconos[index]);
        item.add(Box.createVerticalStrut(2));
        item.add(textos[index]);
        item.add(Box.createVerticalGlue());

        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                seleccionado = index;
                actualizarColores();
                if (acciones[index] != null) acciones[index].run();
            }
            @Override public void mousePressed(MouseEvent e) {
                item.setBackground(BetTheme.SURFACE_BRIGHT);
                item.setOpaque(true);
            }
            @Override public void mouseReleased(MouseEvent e) {
                item.setOpaque(false);
                item.repaint();
            }
        });

        return item;
    }

    /** Cambia la pestaña activa programáticamente */
    public void setSeleccionado(int index) {
        seleccionado = index;
        actualizarColores();
    }

    private void actualizarColores() {
        for (int j = 0; j < 4; j++) {
            Color c = (j == seleccionado) ? BetTheme.ON_PRIMARY_CONTAINER : BetTheme.ON_SURFACE_VARIANT;
            iconos[j].setForeground(c);
            textos[j].setForeground(c);
            items[j].repaint();
        }
    }
}
