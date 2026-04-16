import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import datechooser.beans.DateChooserCombo;
import modelos.Temperatura;
import servicios.TemperaturaServicio;

public class FrmTemperaturas extends JFrame {

    private DateChooserCombo dccDesde, dccHasta;
    private DateChooserCombo dccFechaEspecifica;
    private JTabbedPane tpTemperaturas;
    private JPanel pnlGrafica;
    private JPanel pnlCiudades;

    private List<Temperatura> datos;

    public FrmTemperaturas() {

        setTitle("Temperaturas por Ciudad");
        setSize(750, 500);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // --- Toolbar ---
        JToolBar tb = new JToolBar();

        JButton btnGraficar = new JButton();
        btnGraficar.setIcon(new ImageIcon(getClass().getResource("/iconos/Grafica.png")));
        btnGraficar.setToolTipText("Graficar promedios por ciudad en el rango de fechas");
        btnGraficar.addActionListener(e -> btnGraficarClick());
        tb.add(btnGraficar);

        JButton btnBuscar = new JButton();
        btnBuscar.setIcon(new ImageIcon(getClass().getResource("/iconos/Datos.png")));
        btnBuscar.setToolTipText("Buscar ciudad más y menos calurosa para la fecha específica");
        btnBuscar.addActionListener(e -> btnBuscarCiudadesClick());
        tb.add(btnBuscar);

        // --- Panel principal ---
        JPanel pnlContenido = new JPanel();
        pnlContenido.setLayout(new BoxLayout(pnlContenido, BoxLayout.Y_AXIS));

        // --- Panel de filtros ---
        JPanel pnlFiltros = new JPanel();
        pnlFiltros.setPreferredSize(new Dimension(pnlFiltros.getWidth(), 80));
        pnlFiltros.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        pnlFiltros.setLayout(null);

        // Rango de fechas
        JLabel lblDesde = new JLabel("Desde:");
        lblDesde.setBounds(10, 10, 60, 25);
        pnlFiltros.add(lblDesde);

        dccDesde = new DateChooserCombo();
        dccDesde.setBounds(70, 10, 120, 25);
        pnlFiltros.add(dccDesde);

        JLabel lblHasta = new JLabel("Hasta:");
        lblHasta.setBounds(200, 10, 60, 25);
        pnlFiltros.add(lblHasta);

        dccHasta = new DateChooserCombo();
        dccHasta.setBounds(260, 10, 120, 25);
        pnlFiltros.add(dccHasta);

        // Fecha específica
        JLabel lblFechaEsp = new JLabel("Fecha específica:");
        lblFechaEsp.setBounds(10, 45, 120, 25);
        pnlFiltros.add(lblFechaEsp);

        dccFechaEspecifica = new DateChooserCombo();
        dccFechaEspecifica.setBounds(135, 45, 120, 25);
        pnlFiltros.add(dccFechaEspecifica);

        // --- Pestañas ---
        pnlGrafica = new JPanel();
        JScrollPane spGrafica = new JScrollPane(pnlGrafica);

        pnlCiudades = new JPanel();

        tpTemperaturas = new JTabbedPane();
        tpTemperaturas.addTab("Gráfica de Promedios", spGrafica);
        tpTemperaturas.addTab("Ciudad Más/Menos Calurosa", pnlCiudades);

        pnlContenido.add(pnlFiltros);
        pnlContenido.add(tpTemperaturas);

        getContentPane().add(tb, BorderLayout.NORTH);
        getContentPane().add(pnlContenido, BorderLayout.CENTER);

        cargarDatos();
    }

    private void cargarDatos() {
        String ruta = System.getProperty("user.dir") + "/src/datos/Temperaturas.csv";
        datos = TemperaturaServicio.getDatos(ruta);
    }

    private void btnGraficarClick() {
        LocalDate desde = dccDesde.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate hasta = dccHasta.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // Obtener promedio por ciudad usando streams (paradigma funcional)
        Map<String, Double> promedios = TemperaturaServicio.getPromedioPorCiudad(datos, desde, hasta);

        // Construir dataset para la gráfica de barras
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        promedios.entrySet().stream()
                .forEach(entry -> dataset.addValue(entry.getValue(), "Promedio °C", entry.getKey()));

        JFreeChart grafica = ChartFactory.createBarChart(
                "Promedio de Temperatura por Ciudad",
                "Ciudad",
                "Temperatura Promedio (°C)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        // Personalizar colores
        CategoryPlot plot = grafica.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(70, 130, 180));

        CategoryAxis ejeX = plot.getDomainAxis();
        ejeX.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        ChartPanel pnlGraficador = new ChartPanel(grafica);
        pnlGraficador.setPreferredSize(new Dimension(650, 380));

        pnlGrafica.removeAll();
        pnlGrafica.setLayout(new BorderLayout());
        pnlGrafica.add(pnlGraficador, BorderLayout.CENTER);
        pnlGrafica.revalidate();

        tpTemperaturas.setSelectedIndex(0);
    }

    private void btnBuscarCiudadesClick() {
        LocalDate fecha = dccFechaEspecifica.getSelectedDate()
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // Obtener ciudad más y menos calurosa usando streams (paradigma funcional)
        Optional<Temperatura> masCalurosa = TemperaturaServicio.getCiudadMasCalurosa(datos, fecha);
        Optional<Temperatura> menosCalurosa = TemperaturaServicio.getCiudadMenosCalurosa(datos, fecha);

        pnlCiudades.removeAll();
        pnlCiudades.setLayout(new GridBagLayout());

        Font fuenteTitulo = new Font("SansSerif", Font.BOLD, 14);
        Font fuenteDato = new Font("SansSerif", Font.PLAIN, 13);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.anchor = GridBagConstraints.WEST;

        // Título
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel lblFecha = new JLabel("Resultados para: " + fecha.toString());
        lblFecha.setFont(fuenteTitulo);
        pnlCiudades.add(lblFecha, gbc);

        gbc.gridwidth = 1;

        // Ciudad más calurosa
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblMasCalurosaLabel = new JLabel("🌡️ Ciudad más calurosa:");
        lblMasCalurosaLabel.setFont(fuenteDato);
        pnlCiudades.add(lblMasCalurosaLabel, gbc);

        gbc.gridx = 1;
        String masCalurosaTxt = masCalurosa
                .map(t -> t.getCiudad() + "  (" + t.getTemperatura() + " °C)")
                .orElse("Sin datos para esta fecha");
        JLabel lblMasCalurosaValor = new JLabel(masCalurosaTxt);
        lblMasCalurosaValor.setFont(fuenteDato);
        lblMasCalurosaValor.setForeground(new Color(180, 50, 50));
        pnlCiudades.add(lblMasCalurosaValor, gbc);

        // Ciudad menos calurosa
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblMenosCalurosaLabel = new JLabel("❄️ Ciudad menos calurosa:");
        lblMenosCalurosaLabel.setFont(fuenteDato);
        pnlCiudades.add(lblMenosCalurosaLabel, gbc);

        gbc.gridx = 1;
        String menosCalurosaTxt = menosCalurosa
                .map(t -> t.getCiudad() + "  (" + t.getTemperatura() + " °C)")
                .orElse("Sin datos para esta fecha");
        JLabel lblMenosCalurosaValor = new JLabel(menosCalurosaTxt);
        lblMenosCalurosaValor.setFont(fuenteDato);
        lblMenosCalurosaValor.setForeground(new Color(50, 100, 180));
        pnlCiudades.add(lblMenosCalurosaValor, gbc);

        pnlCiudades.revalidate();
        pnlCiudades.repaint();

        tpTemperaturas.setSelectedIndex(1);
    }
}
