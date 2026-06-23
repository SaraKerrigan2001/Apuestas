import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SidebarEquiposPanel extends JPanel {

    private final Consumer<String> onEquipoSeleccionado;

    public SidebarEquiposPanel(Consumer<String> onEquipoSeleccionado) {
        this.onEquipoSeleccionado = onEquipoSeleccionado;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(SkyBetTheme.BG_SIDEBAR);
        setPreferredSize(new Dimension(185, 0));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, SkyBetTheme.BORDER),
            BorderFactory.createEmptyBorder(10, 0, 14, 0)
        ));

        cargarGruposAsincrono();
    }

    private void cargarGruposAsincrono() {
        new Thread(() -> {
            LinkedHashMap<String, List<String>> grupos = cargarGruposDesdeDB();
            SwingUtilities.invokeLater(() -> construirUI(grupos));
        }).start();
    }

    private void construirUI(LinkedHashMap<String, List<String>> grupos) {
        removeAll();

        // Título general
        JLabel titGeneral = new JLabel("MUNDIAL 2026");
        titGeneral.setFont(new Font("Inter", Font.BOLD, 10));
        titGeneral.setForeground(SkyBetTheme.ACCENT_BLUE);
        titGeneral.setBorder(BorderFactory.createEmptyBorder(4, 14, 8, 14));
        titGeneral.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(titGeneral);

        // Scroll con todos los grupos
        JPanel contenidoGrupos = new JPanel();
        contenidoGrupos.setLayout(new BoxLayout(contenidoGrupos, BoxLayout.Y_AXIS));
        contenidoGrupos.setOpaque(false);

        for (Map.Entry<String, List<String>> entry : grupos.entrySet()) {
            String grupo = entry.getKey();
            List<String> equipos = entry.getValue();

            // Header del grupo
            JPanel grupoHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            grupoHeader.setOpaque(false);
            grupoHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

            JLabel lblGrupo = new JLabel("▸  " + grupo.toUpperCase());
            lblGrupo.setFont(new Font("Inter", Font.BOLD, 9));
            lblGrupo.setForeground(SkyBetTheme.TEXT_MUTED);
            grupoHeader.add(lblGrupo);
            contenidoGrupos.add(grupoHeader);

            // Equipos del grupo
            for (String equipo : equipos) {
                JPanel row = crearFilaSidebar("⚽", equipo);
                contenidoGrupos.add(row);
            }
            contenidoGrupos.add(crearDivider());
        }
        contenidoGrupos.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(contenidoGrupos,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(SkyBetTheme.BG_SIDEBAR);
        scroll.getVerticalScrollBar().setUnitIncrement(10);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(3, 0));
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        add(scroll);
        revalidate();
        repaint();
    }

    private LinkedHashMap<String, List<String>> cargarGruposDesdeDB() {
        LinkedHashMap<String, List<String>> result = new LinkedHashMap<>();
        String sql = "SELECT grupo, nombre FROM equipos ORDER BY grupo, nombre";

        try (Connection conn = ConexionDB.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String grupo  = rs.getString("grupo");
                String equipo = rs.getString("nombre");
                result.computeIfAbsent(grupo, k -> new ArrayList<>()).add(equipo);
            }

        } catch (SQLException e) {
            System.err.println("[Sidebar] Error cargando grupos: " + e.getMessage());
        }
        return result;
    }

    private JPanel crearFilaSidebar(String icono, String nombre) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 1));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        JLabel icon = new JLabel(icono);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 10));
        
        JLabel lbl = new JLabel(nombre);
        lbl.setFont(SkyBetTheme.FONT_SMALL);
        lbl.setForeground(SkyBetTheme.TEXT_SECONDARY);
        
        row.add(icon); 
        row.add(lbl);
        
        row.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                lbl.setForeground(Color.WHITE); 
                row.repaint();
            }
            public void mouseExited(MouseEvent e) {
                lbl.setForeground(SkyBetTheme.TEXT_SECONDARY); 
                row.repaint();
            }
            public void mouseClicked(MouseEvent e) {
                if (onEquipoSeleccionado != null) {
                    onEquipoSeleccionado.accept(nombre);
                }
            }
        });
        return row;
    }

    private JSeparator crearDivider() {
        JSeparator s = new JSeparator();
        s.setForeground(SkyBetTheme.BORDER);
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return s;
    }
}
