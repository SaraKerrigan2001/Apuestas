import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Ventana secundaria que muestra el historial completo y las estadísticas
 * personales de un jugador específico.
 * Se abre con doble clic sobre una fila en VentanaMundial.
 */
public class VentanaHistorialJugador extends JDialog {

    private static final Color COLOR_ACIERTO   = new Color(198, 239, 206); // verde
    private static final Color COLOR_FALLO     = new Color(255, 199, 206); // rojo claro
    private static final Color COLOR_PENDIENTE = new Color(235, 235, 235); // gris claro

    private final PronosticoDAO dao;
    private final String nombreJugador;

    public VentanaHistorialJugador(JFrame padre, String nombreJugador, PronosticoDAO dao) {
        super(padre, "Historial de " + nombreJugador, false);
        this.dao = dao;
        this.nombreJugador = nombreJugador;

        setSize(750, 550);
        setMinimumSize(new Dimension(600, 450));
        setLocationRelativeTo(padre);
        setLayout(new BorderLayout(10, 10));

        add(crearEncabezado(),    BorderLayout.NORTH);
        add(crearPanelCentral(),  BorderLayout.CENTER);
        add(crearPanelBoton(),    BorderLayout.SOUTH);
    }

    // ---------------------------------------------------------------
    // Encabezado con nombre del jugador
    // ---------------------------------------------------------------
    private JPanel crearEncabezado() {
        JLabel lbl = new JLabel("⚽  " + nombreJugador, SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.BOLD, 20));
        lbl.setBorder(BorderFactory.createEmptyBorder(12, 0, 4, 0));
        JPanel p = new JPanel(new BorderLayout());
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    // ---------------------------------------------------------------
    // Panel central: tarjetas de estadísticas + tabla de historial
    // Carga los datos en un hilo separado para no bloquear el EDT
    // ---------------------------------------------------------------
    private JPanel crearPanelCentral() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

        // Placeholders mientras cargan los datos
        JLabel lblCargando = new JLabel("Cargando...", SwingConstants.CENTER);
        lblCargando.setFont(new Font("Arial", Font.ITALIC, 14));
        panel.add(lblCargando, BorderLayout.CENTER);

        // Cargar en hilo separado
        new Thread(() -> {
            Map<String, String> stats = dao.listarEstadisticasJugador(nombreJugador);
            List<Pronostico> lista    = dao.listarPorJugador(nombreJugador);
            SwingUtilities.invokeLater(() -> {
                panel.remove(lblCargando);
                panel.add(crearPanelStats(stats), BorderLayout.NORTH);
                panel.add(crearTablaHistorial(lista), BorderLayout.CENTER);
                panel.revalidate();
                panel.repaint();
            });
        }).start();

        return panel;
    }

    // ---------------------------------------------------------------
    // Tarjetas de estadísticas personales
    // ---------------------------------------------------------------
    private JPanel crearPanelStats(Map<String, String> stats) {
        JPanel panel = new JPanel(new GridLayout(2, 3, 8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Estadísticas personales"));

        panel.add(tarjeta("Total pronósticos",
                stats.getOrDefault("total", "0"),
                new Color(220, 220, 220), 20));

        panel.add(tarjeta("Aciertos",
                stats.getOrDefault("aciertos", "0"),
                new Color(198, 239, 206), 20));

        panel.add(tarjeta("Fallos",
                stats.getOrDefault("fallos", "0"),
                new Color(255, 199, 206), 20));

        panel.add(tarjeta("Pendientes",
                stats.getOrDefault("pendientes", "0"),
                new Color(235, 235, 235), 20));

        panel.add(tarjeta("% Acierto",
                stats.getOrDefault("porcentaje", "N/A"),
                new Color(255, 235, 156), 20));

        panel.add(tarjeta("Marcador favorito",
                stats.getOrDefault("marcador_favorito", "N/A"),
                new Color(197, 217, 241), 14));

        return panel;
    }

    // ---------------------------------------------------------------
    // Tabla con el historial de pronósticos del jugador
    // ---------------------------------------------------------------
    private JScrollPane crearTablaHistorial(List<Pronostico> lista) {
        String[] cols = {"ID", "Local", "GL", "GV", "Visitante", "Predicho", "Real", "Estado", "Fecha"};
        DefaultTableModel modelo = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Pronostico p : lista) {
            modelo.addRow(new Object[]{
                p.getId(),
                p.getEquipoLocal(),
                p.getGolesLocal(),
                p.getGolesVisitante(),
                p.getEquipoVisitante(),
                p.getMarcadorPredicho(),
                p.getResultadoReal() != null ? p.getResultadoReal() : "-",
                p.getEstadoAcierto(),
                p.getFechaFormateada()
            });
        }

        JTable tabla = new JTable(modelo) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    String estado = (String) modelo.getValueAt(row, 7);
                    if (estado.contains("Acertó"))     c.setBackground(COLOR_ACIERTO);
                    else if (estado.contains("Falló")) c.setBackground(COLOR_FALLO);
                    else                               c.setBackground(COLOR_PENDIENTE);
                }
                return c;
            }
        };

        tabla.setRowHeight(22);
        tabla.getColumnModel().getColumn(0).setMaxWidth(45);  // ID
        tabla.getColumnModel().getColumn(2).setMaxWidth(35);  // GL
        tabla.getColumnModel().getColumn(3).setMaxWidth(35);  // GV
        tabla.getColumnModel().getColumn(5).setMaxWidth(60);  // Predicho
        tabla.getColumnModel().getColumn(6).setMaxWidth(55);  // Real
        tabla.getColumnModel().getColumn(7).setPreferredWidth(90); // Estado
        tabla.getColumnModel().getColumn(8).setPreferredWidth(120); // Fecha

        // Centrar columnas numéricas
        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i : new int[]{0, 2, 3, 5, 6}) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(centrado);
        }

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder("Pronósticos registrados (" + lista.size() + ")"));
        return scroll;
    }

    // ---------------------------------------------------------------
    // Botón cerrar
    // ---------------------------------------------------------------
    private JPanel crearPanelBoton() {
        JButton btn = new JButton("Cerrar");
        btn.addActionListener(e -> dispose());
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.add(btn);
        return p;
    }

    // ---------------------------------------------------------------
    // Helper: tarjeta coloreada
    // ---------------------------------------------------------------
    private JPanel tarjeta(String titulo, String valor, Color color, int tamFuente) {
        JPanel t = new JPanel(new BorderLayout(3, 3));
        t.setBackground(color);
        t.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        JLabel lblTit = new JLabel(titulo, SwingConstants.CENTER);
        lblTit.setFont(new Font("Arial", Font.PLAIN, 10));
        lblTit.setForeground(new Color(60, 60, 60));
        JLabel lblVal = new JLabel(valor, SwingConstants.CENTER);
        lblVal.setFont(new Font("Arial", Font.BOLD, tamFuente));
        t.add(lblTit, BorderLayout.NORTH);
        t.add(lblVal, BorderLayout.CENTER);
        return t;
    }
}
