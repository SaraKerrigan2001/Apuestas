import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Ventana de estadísticas globales del torneo.
 * Muestra: tarjetas de resumen, gráfico de pastel, gráfico de barras y ranking de jugadores.
 * Se abre desde VentanaMundial → botón "📊 Estadísticas".
 */
public class VentanaEstadisticas extends JDialog {

    private static final Color COLOR_LOCAL     = new Color(198, 239, 206);
    private static final Color COLOR_EMPATE    = new Color(255, 235, 156);
    private static final Color COLOR_VISITANTE = new Color(197, 217, 241);
    private static final Color COLOR_TOTAL     = new Color(220, 220, 220);
    private static final Color COLOR_ACIERTO   = new Color(198, 239, 206);
    private static final Color COLOR_FALLO     = new Color(255, 199, 206);

    // Colores del pie chart
    private static final Color PIE_LOCAL     = new Color(76,  153,  0);
    private static final Color PIE_EMPATE    = new Color(220, 180,  0);
    private static final Color PIE_VISITANTE = new Color(30,  100, 200);

    private final Map<String, String>       stats;
    private final List<Map<String, String>> ranking;

    public VentanaEstadisticas(Map<String, String> stats, List<Map<String, String>> ranking) {
        this.stats   = stats;
        this.ranking = ranking;

        setTitle("Estadísticas Globales del Mundial");
        setSize(820, 640);
        setMinimumSize(new Dimension(700, 560));
        setLocationRelativeTo(null);
        setModal(false);
        setLayout(new BorderLayout(10, 10));

        add(crearEncabezado(),  BorderLayout.NORTH);
        add(crearCuerpo(),      BorderLayout.CENTER);
        add(crearPanelCerrar(), BorderLayout.SOUTH);
    }

    // ---------------------------------------------------------------
    // Encabezado
    // ---------------------------------------------------------------
    private JPanel crearEncabezado() {
        JLabel lbl = new JLabel("Estadísticas Globales del Mundial 2026", SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.BOLD, 18));
        lbl.setBorder(BorderFactory.createEmptyBorder(12, 0, 4, 0));
        JPanel p = new JPanel(new BorderLayout());
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    // ---------------------------------------------------------------
    // Cuerpo: pestañas (Resumen | Ranking)
    // ---------------------------------------------------------------
    private JTabbedPane crearCuerpo() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("📊  Resumen",  crearPanelResumen());
        tabs.addTab("🏆  Ranking",  crearPanelRanking());
        return tabs;
    }

    // ---------------------------------------------------------------
    // Pestaña 1: Resumen con tarjetas + pie chart + barras
    // ---------------------------------------------------------------
    private JPanel crearPanelResumen() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        String total  = stats.getOrDefault("total",               "0");
        String vLocal = stats.getOrDefault("victorias_local",     "0");
        String emps   = stats.getOrDefault("empates",             "0");
        String vVisit = stats.getOrDefault("victorias_visitante", "0");
        String marc   = stats.getOrDefault("marcador_popular",    "N/A");
        String jugTop = stats.getOrDefault("jugador_top",         "N/A");

        // --- Tarjetas superiores ---
        JPanel tarjetas = new JPanel(new GridLayout(2, 3, 8, 8));
        tarjetas.add(tarjeta("Total pronósticos",   total,   COLOR_TOTAL,     24));
        tarjetas.add(tarjeta("Victoria Local",      vLocal + " pred.", COLOR_LOCAL,  20));
        tarjetas.add(tarjeta("Empate",              emps   + " pred.", COLOR_EMPATE, 20));
        tarjetas.add(tarjeta("Victoria Visitante",  vVisit + " pred.", COLOR_VISITANTE, 20));
        tarjetas.add(tarjeta("Marcador popular",    marc,   new Color(220, 198, 239), 14));
        tarjetas.add(tarjeta("Jugador más activo",  jugTop, new Color(198, 224, 239), 13));
        panel.add(tarjetas, BorderLayout.NORTH);

        // --- Gráficos: pie + barras lado a lado ---
        JPanel graficos = new JPanel(new GridLayout(1, 2, 10, 0));
        graficos.add(crearPieChart(vLocal, emps, vVisit, total));
        graficos.add(crearGraficoBarra(vLocal, emps, vVisit, total));
        panel.add(graficos, BorderLayout.CENTER);

        return panel;
    }

    // ---------------------------------------------------------------
    // Gráfico de PASTEL (pie chart) con Graphics2D
    // ---------------------------------------------------------------
    private JPanel crearPieChart(String vLocal, String emps, String vVisit, String total) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int tot = parseOrZero(total);
                int vL  = parseOrZero(vLocal);
                int emp = parseOrZero(emps);
                int vV  = parseOrZero(vVisit);

                if (tot == 0) {
                    g2.setColor(Color.GRAY);
                    g2.setFont(new Font("Arial", Font.ITALIC, 12));
                    g2.drawString("Sin datos", getWidth() / 2 - 30, getHeight() / 2);
                    return;
                }

                int margen = 20;
                int diam   = Math.min(getWidth(), getHeight()) - margen * 2 - 60;
                int x      = (getWidth() - diam) / 2;
                int y      = margen;

                double angLocal    = (vL  / (double) tot) * 360;
                double angEmpate   = (emp / (double) tot) * 360;
                double angVisitante = 360 - angLocal - angEmpate;

                // Sombra suave
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillOval(x + 4, y + 4, diam, diam);

                int startAngle = 0;
                int[][] sectores = {
                    {(int) angLocal,     PIE_LOCAL.getRGB()},
                    {(int) angEmpate,    PIE_EMPATE.getRGB()},
                    {(int) angVisitante, PIE_VISITANTE.getRGB()}
                };

                for (int[] s : sectores) {
                    g2.setColor(new Color(s[1]));
                    g2.fillArc(x, y, diam, diam, startAngle, s[0]);
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2));
                    g2.drawArc(x, y, diam, diam, startAngle, s[0]);
                    startAngle += s[0];
                }

                // Leyenda debajo del pastel
                int leyY = y + diam + 12;
                String[] etiquetas = {
                    "Local: "    + vLocal + " (" + pct(vL, tot) + ")",
                    "Empate: "   + emps   + " (" + pct(emp, tot) + ")",
                    "Visitante: "+ vVisit + " (" + pct(vV, tot) + ")"
                };
                Color[] colLey = {PIE_LOCAL, PIE_EMPATE, PIE_VISITANTE};

                g2.setFont(new Font("Arial", Font.PLAIN, 11));
                int lx = 12;
                for (int i = 0; i < etiquetas.length; i++) {
                    g2.setColor(colLey[i]);
                    g2.fillRect(lx, leyY, 12, 12);
                    g2.setColor(Color.DARK_GRAY);
                    g2.drawString(etiquetas[i], lx + 16, leyY + 11);
                    lx += g2.getFontMetrics().stringWidth(etiquetas[i]) + 28;
                }
            }
        };
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Distribución (Pie Chart)",
            TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.PLAIN, 11), Color.GRAY));
        return panel;
    }

    // ---------------------------------------------------------------
    // Gráfico de BARRAS horizontal con Graphics2D
    // ---------------------------------------------------------------
    private JPanel crearGraficoBarra(String vLocal, String emps, String vVisit, String total) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int tot = parseOrZero(total);
                if (tot == 0) {
                    g2.setColor(Color.GRAY);
                    g2.setFont(new Font("Arial", Font.ITALIC, 12));
                    g2.drawString("Sin datos", getWidth() / 2 - 30, getHeight() / 2);
                    return;
                }

                int margenIzq = 130, margenDer = 55;
                int anchoMax  = getWidth() - margenIzq - margenDer;
                int altBarra  = 26, sep = 18, iniY = 28;

                String[][] datos = {
                    {"Victoria Local",    vLocal, String.valueOf(parseOrZero(vLocal))},
                    {"Empate",            emps,   String.valueOf(parseOrZero(emps))},
                    {"Vic. Visitante",    vVisit, String.valueOf(parseOrZero(vVisit))}
                };
                Color[] colores = {PIE_LOCAL, PIE_EMPATE, PIE_VISITANTE};

                for (int i = 0; i < datos.length; i++) {
                    int y     = iniY + i * (altBarra + sep);
                    int valor = parseOrZero(datos[i][2]);
                    int ancho = (int) ((valor / (double) tot) * anchoMax);

                    g2.setFont(new Font("Arial", Font.PLAIN, 12));
                    g2.setColor(Color.DARK_GRAY);
                    g2.drawString(datos[i][0], 5, y + altBarra - 7);

                    // Barra con gradiente
                    GradientPaint gp = new GradientPaint(
                        margenIzq, y, colores[i].brighter(),
                        margenIzq + ancho, y + altBarra, colores[i].darker());
                    g2.setPaint(gp);
                    g2.fillRoundRect(margenIzq, y, Math.max(ancho, 4), altBarra, 10, 10);
                    g2.setColor(colores[i].darker());
                    g2.setStroke(new BasicStroke(1));
                    g2.drawRoundRect(margenIzq, y, Math.max(ancho, 4), altBarra, 10, 10);

                    // Valor + porcentaje
                    g2.setFont(new Font("Arial", Font.BOLD, 12));
                    g2.setColor(Color.DARK_GRAY);
                    g2.drawString(datos[i][2] + " (" + pct(valor, tot) + ")",
                            margenIzq + ancho + 6, y + altBarra - 7);
                }
            }
        };
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Distribución (Barras)",
            TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.PLAIN, 11), Color.GRAY));
        return panel;
    }

    // ---------------------------------------------------------------
    // Pestaña 2: Ranking de jugadores
    // ---------------------------------------------------------------
    private JPanel crearPanelRanking() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JLabel titulo = new JLabel("Clasificación de jugadores", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 15));
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        panel.add(titulo, BorderLayout.NORTH);

        // Tabla de ranking
        String[] cols = {"#", "Jugador", "Total", "Aciertos", "Fallos", "Pendientes", "% Acierto"};
        DefaultTableModel modelo = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        int posicion = 1;
        for (Map<String, String> fila : ranking) {
            modelo.addRow(new Object[]{
                posicion++,
                fila.getOrDefault("jugador",    "-"),
                fila.getOrDefault("total",      "0"),
                fila.getOrDefault("aciertos",   "0"),
                fila.getOrDefault("fallos",     "0"),
                fila.getOrDefault("pendientes", "0"),
                fila.getOrDefault("porcentaje", "N/A")
            });
        }

        JTable tablaRanking = new JTable(modelo) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    // Medallar las primeras posiciones
                    switch (row) {
                        case 0: c.setBackground(new Color(255, 215, 0,  80)); break; // Oro
                        case 1: c.setBackground(new Color(192, 192, 192, 80)); break; // Plata
                        case 2: c.setBackground(new Color(205, 127, 50,  80)); break; // Bronce
                        default:
                            // Colorear según porcentaje de acierto
                            String pctStr = (String) modelo.getValueAt(row, 6);
                            if (!"N/A".equals(pctStr)) {
                                double pct = Double.parseDouble(pctStr.replace("%", ""));
                                if (pct >= 70)      c.setBackground(COLOR_ACIERTO);
                                else if (pct >= 40) c.setBackground(COLOR_EMPATE);
                                else                c.setBackground(COLOR_FALLO);
                            } else {
                                c.setBackground(Color.WHITE);
                            }
                    }
                }
                return c;
            }
        };

        tablaRanking.setRowHeight(24);
        tablaRanking.setFont(new Font("Arial", Font.PLAIN, 13));
        tablaRanking.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        tablaRanking.getColumnModel().getColumn(0).setMaxWidth(40);  // #
        tablaRanking.getColumnModel().getColumn(2).setMaxWidth(60);  // Total
        tablaRanking.getColumnModel().getColumn(3).setMaxWidth(70);  // Aciertos
        tablaRanking.getColumnModel().getColumn(4).setMaxWidth(60);  // Fallos
        tablaRanking.getColumnModel().getColumn(5).setMaxWidth(80);  // Pendientes
        tablaRanking.getColumnModel().getColumn(6).setMaxWidth(80);  // %

        panel.add(new JScrollPane(tablaRanking), BorderLayout.CENTER);

        // Leyenda de colores del ranking
        JPanel leyenda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        leyenda.add(etiqueta(new Color(255, 215, 0,  80), "🥇 1er lugar"));
        leyenda.add(etiqueta(new Color(192, 192, 192, 80), "🥈 2do lugar"));
        leyenda.add(etiqueta(new Color(205, 127, 50,  80), "🥉 3er lugar"));
        leyenda.add(etiqueta(COLOR_ACIERTO,  "≥70% acierto"));
        leyenda.add(etiqueta(COLOR_EMPATE,   "40-69%"));
        leyenda.add(etiqueta(COLOR_FALLO,    "<40%"));
        panel.add(leyenda, BorderLayout.SOUTH);

        return panel;
    }

    // ---------------------------------------------------------------
    // Botón cerrar
    // ---------------------------------------------------------------
    private JPanel crearPanelCerrar() {
        JButton btn = new JButton("Cerrar");
        btn.addActionListener(e -> dispose());
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.add(btn);
        return p;
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------
    private JPanel tarjeta(String titulo, String valor, Color color, int tamFuente) {
        JPanel t = new JPanel(new BorderLayout(3, 3));
        t.setBackground(color);
        t.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        JLabel lblT = new JLabel(titulo, SwingConstants.CENTER);
        lblT.setFont(new Font("Arial", Font.PLAIN, 10));
        lblT.setForeground(new Color(60, 60, 60));
        JLabel lblV = new JLabel(valor, SwingConstants.CENTER);
        lblV.setFont(new Font("Arial", Font.BOLD, tamFuente));
        t.add(lblT, BorderLayout.NORTH);
        t.add(lblV, BorderLayout.CENTER);
        return t;
    }

    private JLabel etiqueta(Color c, String txt) {
        JLabel l = new JLabel("  " + txt + "  ");
        l.setOpaque(true);
        l.setBackground(c);
        l.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        l.setFont(new Font("Arial", Font.PLAIN, 11));
        return l;
    }

    private int parseOrZero(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }

    private String pct(int parte, int total) {
        if (total == 0) return "0%";
        return String.format("%.1f%%", parte * 100.0 / total);
    }
}
