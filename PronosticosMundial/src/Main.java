import javax.swing.*;
import java.awt.*;

/**
 * Punto de entrada de PronosticosMundial.
 * Flujo: VentanaAuth → VentanaLogin / VentanaRegistro → VentanaApuestas
 *
 * Compilar:  javac -cp "lib\mysql-connector-j-8.3.0.jar" -d bin src\*.java
 * Ejecutar:  java  -cp "bin;lib\mysql-connector-j-8.3.0.jar" Main
 */
public class Main {
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}

        // Defaults globales SkyBet / BetCentral
        UIManager.put("Panel.background",             SkyBetTheme.BG_DARK);
        UIManager.put("ScrollPane.background",        SkyBetTheme.BG_DARK);
        UIManager.put("Viewport.background",          SkyBetTheme.BG_DARK);
        UIManager.put("Label.foreground",             SkyBetTheme.TEXT_PRIMARY);
        UIManager.put("ToolTip.background",           SkyBetTheme.BG_CARD);
        UIManager.put("ToolTip.foreground",           SkyBetTheme.TEXT_PRIMARY);
        UIManager.put("OptionPane.background",        BetTheme.SURFACE_CONTAINER);
        UIManager.put("OptionPane.messageForeground", BetTheme.ON_SURFACE);
        UIManager.put("ComboBox.background",          SkyBetTheme.BG_INPUT);
        UIManager.put("ComboBox.foreground",          SkyBetTheme.TEXT_PRIMARY);
        UIManager.put("TextField.background",         SkyBetTheme.BG_INPUT);
        UIManager.put("TextField.foreground",         SkyBetTheme.TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground",    SkyBetTheme.ACCENT_CYAN);
        UIManager.put("Table.background",             SkyBetTheme.BG_ROW);
        UIManager.put("Table.foreground",             SkyBetTheme.TEXT_PRIMARY);
        UIManager.put("TableHeader.background",       SkyBetTheme.BG_PANEL);
        UIManager.put("TableHeader.foreground",       SkyBetTheme.TEXT_MUTED);
        UIManager.put("ScrollBar.background",         SkyBetTheme.BG_DARK);
        UIManager.put("ScrollBar.thumb",              SkyBetTheme.BG_CARD);

        // Abrir pantalla de autenticación
        SwingUtilities.invokeLater(() -> new VentanaAuth().setVisible(true));
    }
}
