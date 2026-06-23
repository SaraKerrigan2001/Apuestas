import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class FormularioApuestaPanel extends JPanel {

    private JTextField txtApostador;
    private JComboBox<String> cmbEqLocal;
    private JComboBox<String> cmbEqVisitante;
    private JTextField txtGolesLocal;
    private JTextField txtGolesVisitante;
    private JButton btnGuardar;
    private JButton btnCancelarEdicion;

    private String[] equiposArray = {};
    private Map<String, String> equiposGruposMap;
    private int apuestaSeleccionadaId = -1;
    
    private final String nombreUsuarioLogueado;
    private final ApuestaDAO apuestaDAO;
    private final Runnable onApuestaGuardada;
    private final Consumer<String> onError;
    private final Consumer<String> onExito;

    public FormularioApuestaPanel(String nombreUsuarioLogueado, ApuestaDAO apuestaDAO,
                                  Runnable onApuestaGuardada, Consumer<String> onError, Consumer<String> onExito) {
        this.nombreUsuarioLogueado = nombreUsuarioLogueado;
        this.apuestaDAO = apuestaDAO;
        this.onApuestaGuardada = onApuestaGuardada;
        this.onError = onError;
        this.onExito = onExito;

        setLayout(new BorderLayout(0, 10));
        setOpaque(false);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 135));
        
        construirUI();
    }

    private void construirUI() {
        JPanel tituloRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tituloRow.setOpaque(false);
        JLabel dotLive = new JLabel("●");
        dotLive.setFont(new Font("Dialog", Font.BOLD, 10));
        dotLive.setForeground(SkyBetTheme.ACCENT_RED);
        JLabel tituloSec = new JLabel("Registrar Apuesta");
        tituloSec.setFont(SkyBetTheme.FONT_SUBTITLE);
        tituloSec.setForeground(Color.WHITE);
        JLabel badge = SkyBetTheme.badge("LIVE", SkyBetTheme.ACCENT_RED, Color.WHITE);
        tituloRow.add(dotLive); tituloRow.add(tituloSec); tituloRow.add(badge);
        add(tituloRow, BorderLayout.NORTH);

        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SkyBetTheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(SkyBetTheme.BORDER);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        // Fila 1: labels
        gbc.gridy = 0;
        gbc.gridx = 0; gbc.weightx = 1.0; card.add(crearLabel("Nombre del Apostador"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.5; card.add(crearLabel("Equipo Local"), gbc);
        gbc.gridx = 2; gbc.weightx = 0.3; card.add(crearLabel("Goles Local"), gbc);
        gbc.gridx = 3; gbc.weightx = 1.5; card.add(crearLabel("Equipo Visitante"), gbc);
        gbc.gridx = 4; gbc.weightx = 0.3; card.add(crearLabel("Goles Visitante"), gbc);
        gbc.gridx = 5; gbc.weightx = 1.0; card.add(new JLabel(""), gbc);

        // Fila 2: inputs + botón
        txtApostador = SkyBetTheme.textField();
        txtApostador.setText(nombreUsuarioLogueado);
        txtApostador.setEditable(false);
        txtGolesLocal = SkyBetTheme.textField();
        txtGolesVisitante = SkyBetTheme.textField();

        cmbEqLocal = crearComboEquipos();
        cmbEqVisitante = crearComboEquipos();

        gbc.gridy = 1;
        gbc.gridx = 0; card.add(txtApostador, gbc);
        gbc.gridx = 1; card.add(cmbEqLocal, gbc);
        gbc.gridx = 2; card.add(txtGolesLocal, gbc);
        gbc.gridx = 3; card.add(cmbEqVisitante, gbc);
        gbc.gridx = 4; card.add(txtGolesVisitante, gbc);

        JPanel btnPanelForm = new JPanel(new GridLayout(1, 2, 4, 0));
        btnPanelForm.setOpaque(false);
        btnGuardar = SkyBetTheme.primaryButton("Registrar Apuesta");
        btnGuardar.addActionListener(e -> guardarApuesta());
        
        btnCancelarEdicion = new JButton("Cancelar");
        btnCancelarEdicion.setFont(new Font("Inter", Font.BOLD, 12));
        btnCancelarEdicion.setForeground(Color.WHITE);
        btnCancelarEdicion.setBackground(SkyBetTheme.BG_ROW_ALT);
        btnCancelarEdicion.setFocusPainted(false);
        btnCancelarEdicion.setVisible(false);
        btnCancelarEdicion.addActionListener(e -> limpiarCampos());
        
        btnPanelForm.add(btnGuardar);
        btnPanelForm.add(btnCancelarEdicion);
        
        gbc.gridx = 5; card.add(btnPanelForm, gbc);

        add(card, BorderLayout.CENTER);
    }

    public void setEquipos(String[] equipos, Map<String, String> mapGrupos) {
        this.equiposArray = equipos;
        this.equiposGruposMap = mapGrupos;
        cmbEqLocal.removeAllItems();
        cmbEqVisitante.removeAllItems();
        for (String eq : equipos) {
            cmbEqLocal.addItem(eq);
            cmbEqVisitante.addItem(eq);
        }
        if (cmbEqVisitante.getItemCount() > 1) {
            cmbEqVisitante.setSelectedIndex(1);
        }
    }

    public void cargarDatosParaEdicion(int id, String local, String golesL, String visitante, String golesV) {
        cmbEqLocal.setSelectedItem(local);
        txtGolesLocal.setText(golesL);
        cmbEqVisitante.setSelectedItem(visitante);
        txtGolesVisitante.setText(golesV);
        apuestaSeleccionadaId = id;
        
        btnGuardar.setText("Modificar Apuesta");
        btnCancelarEdicion.setVisible(true);
    }

    public void limpiarCampos() {
        apuestaSeleccionadaId = -1;
        txtGolesLocal.setText("");
        txtGolesVisitante.setText("");
        if (cmbEqLocal.getItemCount() > 0) cmbEqLocal.setSelectedIndex(0);
        if (cmbEqVisitante.getItemCount() > 1) cmbEqVisitante.setSelectedIndex(1);
        btnGuardar.setText("Registrar Apuesta");
        btnCancelarEdicion.setVisible(false);
        
        limpiarError(txtApostador);
        limpiarError(txtGolesLocal);
        limpiarError(txtGolesVisitante);
        marcarComboError(cmbEqLocal, false);
        marcarComboError(cmbEqVisitante, false);
    }

    private void guardarApuesta() {
        limpiarError(txtApostador);
        limpiarError(txtGolesLocal);
        limpiarError(txtGolesVisitante);
        marcarComboError(cmbEqLocal, false);
        marcarComboError(cmbEqVisitante, false);

        List<String> errores = new ArrayList<>();

        String nombre = txtApostador.getText().trim();
        if (nombre.isEmpty() || nombre.length() < 2) {
            marcarError(txtApostador, true);
            errores.add("• Nombre del Apostador: campo vacío o muy corto (mín. 2 caracteres)");
        } else if (!nombre.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ ]+")) {
            marcarError(txtApostador, true);
            errores.add("• Nombre del Apostador: solo se permiten letras y espacios");
        }

        String eqLocal = (String) cmbEqLocal.getSelectedItem();
        String eqVisit = (String) cmbEqVisitante.getSelectedItem();
        if (eqLocal == null || eqLocal.trim().isEmpty()) {
            marcarComboError(cmbEqLocal, true);
            errores.add("• Equipo Local: debe seleccionar un equipo");
        }
        if (eqVisit == null || eqVisit.trim().isEmpty()) {
            marcarComboError(cmbEqVisitante, true);
            errores.add("• Equipo Visitante: debe seleccionar un equipo");
        } else if (eqLocal != null && eqLocal.equalsIgnoreCase(eqVisit)) {
            marcarComboError(cmbEqVisitante, true);
            errores.add("• Equipo Visitante: no puede ser igual al equipo local");
        }

        int golesLocal = -1, golesVisit = -1;
        try {
            golesLocal = Integer.parseInt(txtGolesLocal.getText().trim());
            if (golesLocal < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            marcarError(txtGolesLocal, true);
            errores.add("• Goles Local: debe ser un número entero igual o mayor a 0");
        }
        try {
            golesVisit = Integer.parseInt(txtGolesVisitante.getText().trim());
            if (golesVisit < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            marcarError(txtGolesVisitante, true);
            errores.add("• Goles Visitante: debe ser un número entero igual o mayor a 0");
        }

        LocalDateTime fechaPartido = apuestaDAO.getFechaHoraPartido(eqLocal, eqVisit);
        if (fechaPartido != null) {
            LocalDateTime ahoraMas10 = LocalDateTime.now().plusMinutes(10);
            if (fechaPartido.isBefore(ahoraMas10)) {
                errores.add("• Regla de tiempo: El partido está a menos de 10 minutos de iniciar o ya comenzó.");
            }
        }

        if (!errores.isEmpty()) {
            mostrarDialogoValidacion(errores);
            if (onError != null) onError.accept("⚠  Corrija los errores antes de registrar.");
            return;
        }

        ApuestaModel modelo = new ApuestaModel(nombre, eqLocal, eqVisit, golesLocal, golesVisit);
        boolean exito;
        if (apuestaSeleccionadaId != -1) {
            modelo.setId(apuestaSeleccionadaId);
            exito = apuestaDAO.actualizarApuesta(modelo);
        } else {
            exito = apuestaDAO.registrarApuesta(modelo);
        }

        if (exito) {
            JOptionPane.showMessageDialog(this, "¡Apuesta registrada con éxito!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
            if (onApuestaGuardada != null) onApuestaGuardada.run();
            if (onExito != null) onExito.accept("Apuesta guardada.");
        } else {
            JOptionPane.showMessageDialog(this, "Error al registrar en BD.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JComboBox<String> crearComboEquipos() {
        JComboBox<String> cmb = new JComboBox<>(equiposArray);
        cmb.setUI(new javax.swing.plaf.basic.BasicComboBoxUI());
        cmb.setBackground(SkyBetTheme.BG_INPUT);
        cmb.setForeground(SkyBetTheme.TEXT_PRIMARY);
        cmb.setFont(SkyBetTheme.FONT_BODY);
        cmb.setBorder(BorderFactory.createLineBorder(SkyBetTheme.BORDER));
        cmb.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? SkyBetTheme.ACCENT_BLUE : SkyBetTheme.BG_INPUT);
                setForeground(isSelected ? Color.WHITE : SkyBetTheme.TEXT_PRIMARY);
                setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
                if (value != null && equiposGruposMap != null) {
                    String grupo = equiposGruposMap.get(value.toString());
                    if (grupo != null) setText(grupo + " - " + value.toString());
                }
                return this;
            }
        });
        return cmb;
    }

    private void marcarComboError(JComboBox<String> cmb, boolean error) {
        cmb.setBorder(BorderFactory.createLineBorder(error ? SkyBetTheme.ACCENT_RED : SkyBetTheme.BORDER));
    }

    private void marcarError(JTextField tf, boolean error) {
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(error ? SkyBetTheme.ACCENT_RED : SkyBetTheme.BORDER),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
    }

    private void limpiarError(JTextField tf) {
        marcarError(tf, false);
        tf.setToolTipText(null);
    }

    private JLabel crearLabel(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Inter", Font.BOLD, 10));
        l.setForeground(SkyBetTheme.TEXT_MUTED);
        return l;
    }

    private void mostrarDialogoValidacion(List<String> errores) {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(SkyBetTheme.BG_CARD);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JPanel cabecera = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        cabecera.setBackground(SkyBetTheme.BG_CARD);

        JLabel icono = new JLabel("⚠");
        icono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        icono.setForeground(SkyBetTheme.ACCENT_YELLOW);

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setBackground(SkyBetTheme.BG_CARD);

        JLabel titulo = new JLabel("Invalidación incorrecta");
        titulo.setFont(new Font("Inter", Font.BOLD, 15));
        titulo.setForeground(SkyBetTheme.ACCENT_RED);

        JLabel subtitulo = new JLabel("Debe registrarse primero antes de apostar.");
        subtitulo.setFont(new Font("Inter", Font.PLAIN, 12));
        subtitulo.setForeground(SkyBetTheme.TEXT_SECONDARY);

        textos.add(titulo); textos.add(Box.createVerticalStrut(3)); textos.add(subtitulo);
        cabecera.add(icono); cabecera.add(textos);
        panel.add(cabecera, BorderLayout.NORTH);

        JPanel listaPanel = new JPanel();
        listaPanel.setLayout(new BoxLayout(listaPanel, BoxLayout.Y_AXIS));
        listaPanel.setBackground(new Color(SkyBetTheme.BG_DARK.getRed(), SkyBetTheme.BG_DARK.getGreen(), SkyBetTheme.BG_DARK.getBlue()));
        listaPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(SkyBetTheme.BORDER), BorderFactory.createEmptyBorder(10, 14, 10, 14)));

        for (String err : errores) {
            JLabel lbl = new JLabel(err);
            lbl.setFont(new Font("Inter", Font.PLAIN, 11));
            lbl.setForeground(SkyBetTheme.ACCENT_RED);
            lbl.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
            listaPanel.add(lbl);
        }
        panel.add(listaPanel, BorderLayout.CENTER);

        JButton btnCerrar = new JButton("Entendido") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? SkyBetTheme.ACCENT_BLUE.darker() : SkyBetTheme.ACCENT_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnCerrar.setFont(new Font("Inter", Font.BOLD, 12));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setBorderPainted(false);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel pnlSur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlSur.setBackground(SkyBetTheme.BG_CARD);
        pnlSur.add(btnCerrar);
        panel.add(pnlSur, BorderLayout.SOUTH);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Errores de Validación", true);
        dialog.setUndecorated(true);
        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        btnCerrar.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }
}
