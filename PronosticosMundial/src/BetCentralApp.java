import javax.swing.*;
import java.awt.*;

/**
 * Ventana principal unificada de PronosticosMundial + BetCentral.
 *
 * Cards:
 *   LOBBY        → Lobby BetCentral (Hero, Ligas, Live, Upcoming)
 *   MATCH_DETAIL → Detalle del partido en vivo + mercados
 *   BET_SLIP     → Carrito de apuestas
 *   APUESTAS     → VentanaApuestas (Sky Bet) con registro en BD
 *   PRONOSTICOS  → Formulario MVC de pronósticos
 *
 * BottomNavBar:
 *   Home    → LOBBY
 *   Live    → MATCH_DETAIL
 *   My Bets → APUESTAS  (skyBET)
 *   Profile → PRONOSTICOS
 */
public class BetCentralApp extends JFrame {

    public static final String CARD_LOBBY        = "LOBBY";
    public static final String CARD_MATCH_DETAIL = "MATCH_DETAIL";
    public static final String CARD_BET_SLIP     = "BET_SLIP";
    public static final String CARD_APUESTAS     = "APUESTAS";
    public static final String CARD_PRONOSTICOS  = "PRONOSTICOS";

    private final CardLayout   cardLayout;
    private final JPanel       cardPanel;
    private       BottomNavBar bottomNav;

    // Pantallas
    private final BetSlip               betSlip;
    private final MatchDetail           matchDetail;
    private final VentanaApuestasPanel  apuestasPanel;
    private final PronosticoVista       vista;
    private final PronosticoControlador controlador;

    public BetCentralApp() {
        setTitle("BetCentral · Mundial 2026");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setResizable(true);
        getContentPane().setBackground(BetTheme.BACKGROUND);
        setLayout(new BorderLayout());

        // TopBar fijo
        add(new TopBar(), BorderLayout.NORTH);

        // CardLayout central
        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);
        cardPanel.setBackground(BetTheme.BACKGROUND);
        cardPanel.setOpaque(true);

        // Card 1: Lobby
        cardPanel.add(crearLobby(), CARD_LOBBY);

        // Card 2: BetSlip
        betSlip = new BetSlip(() -> mostrarCard(CARD_LOBBY, 0));
        cardPanel.add(betSlip, CARD_BET_SLIP);

        // Card 3: MatchDetail
        matchDetail = new MatchDetail(
            () -> mostrarCard(CARD_BET_SLIP, 2),
            betSlip
        );
        cardPanel.add(matchDetail, CARD_MATCH_DETAIL);

        // Card 4: VentanaApuestas embebida (Sky Bet)
        apuestasPanel = new VentanaApuestasPanel();
        cardPanel.add(apuestasPanel, CARD_APUESTAS);

        // Card 5: Pronósticos MVC
        PronosticoDAO    dao   = new PronosticoDAO();
        PronosticosMotor motor = new PronosticosMotor(dao);
        vista       = new PronosticoVista();
        controlador = new PronosticoControlador(vista, motor);
        vista.setControlador(controlador);
        cardPanel.add(vista.getPanelRaiz(), CARD_PRONOSTICOS);

        add(cardPanel, BorderLayout.CENTER);

        // BottomNavBar
        bottomNav = new BottomNavBar(
            () -> mostrarCard(CARD_LOBBY,        0),  // Home
            () -> mostrarCard(CARD_MATCH_DETAIL, 1),  // Live
            () -> mostrarCard(CARD_APUESTAS,     2),  // My Bets → skyBET
            () -> mostrarCard(CARD_PRONOSTICOS,  3)   // Profile → pronósticos
        );
        add(bottomNav, BorderLayout.SOUTH);

        cardLayout.show(cardPanel, CARD_LOBBY);
        setVisible(true);
    }

    public void mostrarCard(String card, int tabIndex) {
        cardLayout.show(cardPanel, card);
        bottomNav.setSeleccionado(tabIndex);
    }

    private JScrollPane crearLobby() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BetTheme.BACKGROUND);
        panel.setOpaque(true);
        panel.add(new HeroBanner());
        panel.add(new LeaguesBar());
        panel.add(new LiveSection());
        panel.add(new UpcomingSection());

        JScrollPane scroll = new JScrollPane(panel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setBackground(BetTheme.BACKGROUND);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        return scroll;
    }
}
