/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package gui;

import core.RelojGlobal;
import enums.PoliticaPlanificacion;
import estructuras.Cola;
import estructuras.ListaEnlazada;
import interfaces.IClockObserver;
import io.ConfiguracionSimulacion;
import kernel.BufferAcotado;
import kernel.Computador;
import kernel.Cpu;
import kernel.Pcb;
import metricas.GestorMetricas;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 *
 * @author Usuario
 */

public class VistaMonitoreo extends javax.swing.JFrame implements IClockObserver {
    
    private static final Color COLOR_BG = new Color(18, 24, 38);
    private static final Color COLOR_PANEL_BG = new Color(28, 36, 54);
    private static final Color COLOR_CARD_BG = new Color(38, 48, 70);
    private static final Color COLOR_TEXT_MAIN = new Color(240, 244, 250);
    private static final Color COLOR_TEXT_MUTED = new Color(160, 175, 200);
    private static final Color COLOR_ACCENT_BLUE = new Color(51, 153, 255);
    private static final Color COLOR_ACCENT_GREEN = new Color(46, 204, 113);
    private static final Color COLOR_ACCENT_ORANGE = new Color(230, 126, 34);
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(VistaMonitoreo.class.getName());
    private JLabel lblRelojCiclos;
    private JLabel lblEstadoSimulacion;
    private JTextArea txtLogEventos;
    private JSpinner spinnerDuracionCiclo;
    private JSpinner spinnerLatenciaRed;
    private JTable tblBuffersEstado;
    private DefaultTableModel modelBuffers;

    // Métricas en la GUI
    private JLabel lblThroughput;
    private JLabel lblUtilizacionCPU;
    private JLabel lblTiempoRespuesta;
    private JLabel lblCumplimientoDeadlines;
    private JLabel lblIndiceEquidad;
    private JLabel lblProdCons;
    
    // Componentes de la Gráfica JFreeChart
    private XYSeriesCollection datasetCPU;
    private XYSeries[] seriesComputadoresCPU;
    private JFreeChart chartCPU;

    private ConfiguracionSimulacion configuracionActual;
    private Computador[] listaComputadores;
    private ListaEnlazada<BufferAcotado> listaBuffersGlobales;
    private GestorMetricas gestorMetricasGlobal;

    private JTabbedPane tabbedComputadores;
    private PanelComputadorUI[] panelesComputadores;

    public VistaMonitoreo() {
        initComponentsCustom();
    }

    public VistaMonitoreo(ConfiguracionSimulacion config) {
        this();
        inicializarSimulacion(config);
    }

    private void initComponentsCustom() {
        setTitle("ÁvilaOS - Vista 2: Dashboard de Monitoreo y Simulación Distribuida");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1350, 850);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BG);
        getContentPane().setLayout(new BorderLayout(10, 10));

        // 1. Header Superior
        JPanel panelHeader = crearPanelHeader();
        getContentPane().add(panelHeader, BorderLayout.NORTH);

        // 2. Panel Central Dividido
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setResizeWeight(0.65);
        mainSplit.setBackground(COLOR_BG);
        mainSplit.setBorder(null);

        tabbedComputadores = new JTabbedPane();
        tabbedComputadores.setBackground(COLOR_PANEL_BG);
        tabbedComputadores.setForeground(COLOR_TEXT_MAIN);
        mainSplit.setLeftComponent(tabbedComputadores);

        JPanel panelDerecho = crearPanelLateralDerecho();
        mainSplit.setRightComponent(panelDerecho);

        getContentPane().add(mainSplit, BorderLayout.CENTER);

        // 3. Footer / Barra de Estado
        JPanel panelFooter = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFooter.setBackground(COLOR_PANEL_BG);
        lblEstadoSimulacion = new JLabel("Estado: Configuración cargada. Listo para iniciar.");
        lblEstadoSimulacion.setForeground(COLOR_TEXT_MUTED);
        panelFooter.add(lblEstadoSimulacion);
        getContentPane().add(panelFooter, BorderLayout.SOUTH);
    }

    private JPanel crearPanelHeader() {
        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setBackground(COLOR_PANEL_BG);
        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JPanel pnlTitle = new JPanel(new GridLayout(2, 1));
        pnlTitle.setOpaque(false);
        JLabel title = new JLabel("ÁVILA OS - CLÚSTER DE SISTEMAS OPERATIVOS DISTRIBUIDOS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(COLOR_ACCENT_BLUE);

        lblRelojCiclos = new JLabel("Reloj Global del Sistema: Ciclo 0");
        lblRelojCiclos.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblRelojCiclos.setForeground(COLOR_TEXT_MAIN);

        pnlTitle.add(title);
        pnlTitle.add(lblRelojCiclos);
        header.add(pnlTitle, BorderLayout.WEST);

        // Controles de Simulación
        JPanel pnlControles = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        pnlControles.setOpaque(false);

        JButton btnIniciar = crearBotonEstilizado("Iniciar Simulación", COLOR_ACCENT_GREEN);
        JButton btnPausar = crearBotonEstilizado("Pausar", COLOR_ACCENT_ORANGE);
        JButton btnReiniciar = crearBotonEstilizado("Reiniciar", COLOR_CARD_BG);

        btnIniciar.addActionListener(e -> {
            RelojGlobal.iniciar();
            lblEstadoSimulacion.setText("Estado: Simulación ejecutándose con Thread real...");
            lblEstadoSimulacion.setForeground(COLOR_ACCENT_GREEN);
            registrarLog(">>> Hilo de RelojGlobal INICIADO.");
        });

        btnPausar.addActionListener(e -> {
            RelojGlobal.pausar();
            lblEstadoSimulacion.setText("Estado: Simulación Pausada.");
            lblEstadoSimulacion.setForeground(COLOR_ACCENT_ORANGE);
            registrarLog(">>> Simulación PAUSADA.");
        });

        btnReiniciar.addActionListener(e -> {
            RelojGlobal.reiniciar();
            inicializarSimulacion(configuracionActual);
            registrarLog(">>> Simulación REINICIADA a Ciclo 0.");
        });

        pnlControles.add(btnIniciar);
        pnlControles.add(btnPausar);
        pnlControles.add(btnReiniciar);
        header.add(pnlControles, BorderLayout.CENTER);

        // Parámetros Dinámicos
        JPanel pnlParametros = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlParametros.setOpaque(false);

        JLabel lblDuracion = new JLabel("Ciclo (ms):");
        lblDuracion.setForeground(COLOR_TEXT_MUTED);
        spinnerDuracionCiclo = new JSpinner(new SpinnerNumberModel(1000, 50, 5000, 100));
        spinnerDuracionCiclo.addChangeListener(e -> {
            int nuevaDuracion = (int) spinnerDuracionCiclo.getValue();
            RelojGlobal.setDuracionCicloMs(nuevaDuracion);
            registrarLog("Parámetro actualizado: Duración de ciclo = " + nuevaDuracion + " ms.");
        });

        JLabel lblLatencia = new JLabel("Latencia Red:");
        lblLatencia.setForeground(COLOR_TEXT_MUTED);
        spinnerLatenciaRed = new JSpinner(new SpinnerNumberModel(2, 0, 20, 1));

        pnlParametros.add(lblDuracion);
        pnlParametros.add(spinnerDuracionCiclo);
        pnlParametros.add(lblLatencia);
        pnlParametros.add(spinnerLatenciaRed);

        header.add(pnlParametros, BorderLayout.EAST);
        return header;
    }

    private JPanel crearPanelLateralDerecho() {
    JPanel pnlRight = new JPanel(new GridLayout(4, 1, 0, 6)); 
    pnlRight.setBackground(COLOR_BG);

    // 1. Panel de Buffers Compartidos
    JPanel pnlBuffers = new JPanel(new BorderLayout());
    pnlBuffers.setBackground(COLOR_PANEL_BG);
    pnlBuffers.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(COLOR_CARD_BG),
        "Buffers Compartidos & Semáforos", 0, 0, new Font("Segoe UI", Font.BOLD, 12), COLOR_ACCENT_BLUE));

    modelBuffers = new DefaultTableModel(
        new Object[]{"ID Buffer", "Capacidad", "Elementos", "Anfitrión", "Mutex", "Llenos", "Vacíos"}, 0
    );
    tblBuffersEstado = new JTable(modelBuffers);
    estilarTabla(tblBuffersEstado);
    pnlBuffers.add(new JScrollPane(tblBuffersEstado), BorderLayout.CENTER);

    JButton btnNuevoBuffer = crearBotonEstilizado("+ Crear Nuevo Buffer en Caliente", COLOR_CARD_BG);
    btnNuevoBuffer.addActionListener(this::accionCrearNuevoBuffer);
    pnlBuffers.add(btnNuevoBuffer, BorderLayout.SOUTH);

    // 2. Gráfica Comparativa de CPU (JFreeChart)
    JPanel pnlGraficaCPU = crearPanelGraficaCPU();

    // 3. Panel de Métricas Textuales
    JPanel pnlMetricas = new JPanel(new GridLayout(3, 2, 5, 2));
    pnlMetricas.setBackground(COLOR_PANEL_BG);
    pnlMetricas.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(COLOR_CARD_BG),
        "Métricas de Rendimiento del Clúster", 0, 0, new Font("Segoe UI", Font.BOLD, 12), COLOR_ACCENT_BLUE));

    lblThroughput = new JLabel("Throughput: 0.0 proc/ciclo");
    lblUtilizacionCPU = new JLabel("Uso CPU: 0.0%");
    lblTiempoRespuesta = new JLabel("T. Respuesta Prom.: 0.0");
    lblCumplimientoDeadlines = new JLabel("Tasa Deadlines: 100.0%");
    lblIndiceEquidad = new JLabel("Equidad (Jain): 1.00");
    lblProdCons = new JLabel("Prod / Cons: 0 / 0");

    JLabel[] lbls = {lblThroughput, lblUtilizacionCPU, lblTiempoRespuesta, lblCumplimientoDeadlines, lblIndiceEquidad, lblProdCons};
    for (JLabel l : lbls) {
        l.setForeground(COLOR_TEXT_MAIN);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        pnlMetricas.add(l);
    }

    // 4. Log de Eventos del Kernel
    JPanel pnlLog = new JPanel(new BorderLayout());
    pnlLog.setBackground(COLOR_PANEL_BG);
    pnlLog.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(COLOR_CARD_BG),
        "Registro Global de Eventos del Kernel (Log)", 0, 0, new Font("Segoe UI", Font.BOLD, 12), COLOR_ACCENT_BLUE));

    txtLogEventos = new JTextArea();
    txtLogEventos.setEditable(false);
    txtLogEventos.setBackground(new Color(15, 20, 30));
    txtLogEventos.setForeground(new Color(180, 220, 180));
    txtLogEventos.setFont(new Font("Consolas", Font.PLAIN, 11));
    pnlLog.add(new JScrollPane(txtLogEventos), BorderLayout.CENTER);

   
    pnlRight.add(pnlBuffers);    
    pnlRight.add(pnlGraficaCPU); 
    pnlRight.add(pnlMetricas);    
    pnlRight.add(pnlLog);        

    return pnlRight;
}
    
    /**
     * Construye la gráfica en pantalla
     * @return 
     */
    private JPanel crearPanelGraficaCPU() {
        datasetCPU = new XYSeriesCollection();

        chartCPU = ChartFactory.createXYLineChart(
            null,                       // Título
            "Ciclo de Reloj",           // Eje X
            "Uso CPU (%)",              // Eje Y
            datasetCPU,
            PlotOrientation.VERTICAL,
            true,                       // Leyenda
            true,                       // Tooltips
            false
        );

        // 1. Configurar Fondo del Chart
        chartCPU.setBackgroundPaint(COLOR_PANEL_BG);

        // 2. Estilar la Leyenda (Texto Blanco y Fondo Oscuro Transparente)
        if (chartCPU.getLegend() != null) {
            chartCPU.getLegend().setItemPaint(Color.WHITE);
            chartCPU.getLegend().setBackgroundPaint(new Color(15, 20, 30));
        }

        // 3. Estilar el Plot (Área de Dibujo)
        XYPlot plot = chartCPU.getXYPlot();
        plot.setBackgroundPaint(new Color(15, 20, 30));
        plot.setDomainGridlinePaint(COLOR_CARD_BG);
        plot.setRangeGridlinePaint(COLOR_CARD_BG);

        // 4. Configurar Eje X (Ciclos)
        plot.getDomainAxis().setLabelPaint(Color.WHITE);
        plot.getDomainAxis().setTickLabelPaint(Color.WHITE);
        plot.getDomainAxis().setAutoRange(true); // Se expande automáticamente con los ciclos

        // 5. Configurar Eje Y (Porcentaje de CPU: Fijar estricto de 0 a 100%)
        org.jfree.chart.axis.NumberAxis rangeAxis = (org.jfree.chart.axis.NumberAxis) plot.getRangeAxis();
        rangeAxis.setRange(0.0, 105.0); // Rango fijo de 0 a 100%
        rangeAxis.setLabelPaint(Color.WHITE);
        rangeAxis.setTickLabelPaint(Color.WHITE);

        // 6. Hacer las líneas del gráfico más gruesas para que sean visibles
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f)); // Grosor de línea
        plot.setRenderer(renderer);

        // 7. Envolver en el ChartPanel
        ChartPanel chartPanel = new ChartPanel(chartCPU);
        chartPanel.setBackground(COLOR_PANEL_BG);
        chartPanel.setPreferredSize(new Dimension(300, 180)); // Tamaño recomendado
        chartPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(COLOR_CARD_BG),
            "CPU vs. Tiempo", 0, 0, new Font("Segoe UI", Font.BOLD, 12), COLOR_ACCENT_BLUE));

        return chartPanel;
    }
    
    public void inicializarSimulacion(ConfiguracionSimulacion config) {
        this.configuracionActual = config;
        RelojGlobal.reiniciar();
        RelojGlobal.registrarObservador(this);

        if (config == null) return;

        spinnerDuracionCiclo.setValue(config.duracionCicloMs);
        spinnerLatenciaRed.setValue(config.latenciaRedCiclos);
        RelojGlobal.setDuracionCicloMs(config.duracionCicloMs);

        // Instanciar Gestor de Métricas Global
        gestorMetricasGlobal = new GestorMetricas();

        // Crear Buffers Compartidos en ListaEnlazada
        listaBuffersGlobales = new ListaEnlazada<>();
        if (config.buffers != null) {
            for (ConfiguracionSimulacion.ConfigBuffer cb : config.buffers) {
                BufferAcotado b = new BufferAcotado(cb.id, cb.computadorAnfitrionId, cb.capacidad);
                listaBuffersGlobales.agregarAlFinal(b);
            }
        }

        // Crear Computadores
        int nComp = (config.computadores != null) ? config.computadores.length : 0;
        listaComputadores = new Computador[nComp];
        panelesComputadores = new PanelComputadorUI[nComp];
        seriesComputadoresCPU = new XYSeries[nComp];
        datasetCPU.removeAllSeries();
        tabbedComputadores.removeAll();

        for (int i = 0; i < nComp; i++) {
            ConfiguracionSimulacion.ConfigComputador cc = config.computadores[i];
            int ram = (cc.ramTotal > 0) ? cc.ramTotal : 2048;

            listaComputadores[i] = new Computador(cc.id, ram, gestorMetricasGlobal, listaBuffersGlobales);
            if (cc.politicaInicial != null) {
                listaComputadores[i].setPoliticaPlanificacion(
                    PoliticaPlanificacion.valueOf(cc.politicaInicial.toUpperCase()), cc.quantum
                );
            }
            
            seriesComputadoresCPU[i] = new XYSeries("Comp " + cc.id);
            seriesComputadoresCPU[i].add(0, 0.0);
            datasetCPU.addSeries(seriesComputadoresCPU[i]);

            PanelComputadorUI panelUI = new PanelComputadorUI(listaComputadores[i]);
            panelesComputadores[i] = panelUI;
            tabbedComputadores.addTab("Computador " + cc.id, panelUI);
        }

        // Cargar Procesos
        if (config.procesos != null) {
            for (ConfiguracionSimulacion.ConfigProceso cp : config.procesos) {
                Pcb pcb = new Pcb(
                    cp.nombre,
                    cp.computadorId,
                    enums.ProcesoTipo.valueOf(cp.tipo.toUpperCase()),
                    cp.prioridad,
                    cp.memoriaAsignada,
                    cp.deadline,
                    cp.tiempoRestanteEjecucion
                );
                pcb.setBufferAsociadoId(cp.bufferAsociadoId);
                pcb.setCiclosProducirConsumir(cp.ciclosProducirConsumir);
                pcb.setElementosRequeridos(cp.elementosRequeridos);

                for (Computador comp : listaComputadores) {
                    if (comp.getId().equals(cp.computadorId)) {
                        comp.admitirProceso(pcb);
                        break;
                    }
                }
            }
        }

        actualizarVistasGUI();
        registrarLog("ÁvilaOS preparado con " + nComp + " computadores.");
    }

    @Override
    public void tick(int cicloActual) {
        SwingUtilities.invokeLater(() -> {
            lblRelojCiclos.setText("Reloj Global del Sistema: Ciclo " + cicloActual);
            actualizarVistasGUI();
        });
    }

    private void actualizarVistasGUI() {
        int cicloActual = RelojGlobal.getCicloActual();

        // 1. Refrescar Paneles de Computadores y Gráfica
        if (panelesComputadores != null && listaComputadores != null) {
            for (int i = 0; i < listaComputadores.length; i++) {
                Computador comp = listaComputadores[i];
                panelesComputadores[i].refrescar();

                // Actualizar la curva de CPU en la gráfica de JFreeChart
                double usoCpuPercent = comp.getCpu().estaOcupada() ? 100.0 : 0.0;
                if (seriesComputadoresCPU != null && i < seriesComputadoresCPU.length) {
                    seriesComputadoresCPU[i].add(cicloActual, usoCpuPercent);
                }
            }
        }

        // 2. Refrescar Tabla de Buffers
        modelBuffers.setRowCount(0);
        if (listaBuffersGlobales != null) {
            for (int i = 0; i < listaBuffersGlobales.getTamano(); i++) {
                BufferAcotado b = listaBuffersGlobales.obtener(i);
                if (b != null) {
                    modelBuffers.addRow(new Object[]{
                        b.getIdBuffer(),
                        "N/A",
                        "Activo",
                        "Anfitrión",
                        "1", "0", "N"
                    });
                }
            }
        }

        // 3. Refrescar Métricas
        if (gestorMetricasGlobal != null) {
            lblThroughput.setText(String.format("Throughput: %.2f proc/ciclo", gestorMetricasGlobal.calcularThroughput(cicloActual)));
            lblUtilizacionCPU.setText(String.format("Uso CPU Global: %.1f%%", gestorMetricasGlobal.calcularUtilizacionCPU(cicloActual)));
            lblTiempoRespuesta.setText(String.format("T. Respuesta Prom.: %.1f", gestorMetricasGlobal.calcularTiempoRespuestaPromedio()));
            lblCumplimientoDeadlines.setText(String.format("Tasa Deadlines: %.1f%%", gestorMetricasGlobal.calcularTasaCumplimientoDeadlines()));
            lblIndiceEquidad.setText(String.format("Equidad (Jain): %.2f", gestorMetricasGlobal.calcularEquidad()));
            lblProdCons.setText(String.format("Prod / Cons: %d / %d", gestorMetricasGlobal.getTotalElementosProducidos(), gestorMetricasGlobal.getTotalElementosConsumidos()));
        }
    }

    private void registrarLog(String mensaje) {
        txtLogEventos.append(mensaje + "\n");
        txtLogEventos.setCaretPosition(txtLogEventos.getDocument().getLength());
    }

    private void accionCrearNuevoBuffer(ActionEvent e) {
        JTextField txtId = new JTextField("B" + (listaBuffersGlobales.getTamano() + 1));
        JTextField txtCap = new JTextField("5");
        JTextField txtCompId = new JTextField("1");

        Object[] message = {
            "ID del Nuevo Buffer:", txtId,
            "Capacidad:", txtCap,
            "ID Computador Anfitrión:", txtCompId
        };

        if (JOptionPane.showConfirmDialog(this, message, "Crear Nuevo Buffer", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                BufferAcotado nuevo = new BufferAcotado(txtId.getText().trim(), txtCompId.getText().trim(), Integer.parseInt(txtCap.getText().trim()));
                listaBuffersGlobales.agregarAlFinal(nuevo);
                actualizarVistasGUI();
                registrarLog("Buffer dinamico " + txtId.getText() + " creado en Computador " + txtCompId.getText());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Parámetros inválidos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JButton crearBotonEstilizado(String texto, Color bg) {
        JButton btn = new JButton(texto);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return btn;
    }

    private void estilarTabla(JTable tabla) {
        tabla.setBackground(new Color(22, 30, 44));
        tabla.setForeground(COLOR_TEXT_MAIN);
        tabla.setGridColor(COLOR_CARD_BG);
        tabla.getTableHeader().setBackground(COLOR_CARD_BG);
        tabla.getTableHeader().setForeground(COLOR_ACCENT_BLUE);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tabla.setRowHeight(20);
    }

    // Panel UI de cada Computador
    private class PanelComputadorUI extends JPanel {
        private final Computador computador;

        private JLabel lblProcesoRunning;
        private JLabel lblModoEjecucion;
        private JLabel lblRegistrosCPU;
        private JProgressBar barRAM;

        private DefaultTableModel modelNuevos;
        private DefaultTableModel modelListos;
        private DefaultTableModel modelBloqueados;
        private DefaultTableModel modelTerminados;

        public PanelComputadorUI(Computador comp) {
            this.computador = comp;
            setLayout(new BorderLayout(8, 8));
            setBackground(COLOR_BG);
            setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

            // Superior: CPU y RAM
            JPanel pnlTop = new JPanel(new GridLayout(1, 2, 8, 0));
            pnlTop.setOpaque(false);

            JPanel cardCPU = crearCardPanel("Estado de CPU & Modo de Ejecución");
            lblProcesoRunning = new JLabel("Proceso: NINGUNO (CPU Libre)");
            lblProcesoRunning.setForeground(COLOR_ACCENT_GREEN);
            lblProcesoRunning.setFont(new Font("Segoe UI", Font.BOLD, 12));

            lblModoEjecucion = new JLabel("Modo: USUARIO");
            lblModoEjecucion.setForeground(COLOR_TEXT_MUTED);

            lblRegistrosCPU = new JLabel("PC: 0 | MAR: 0 | Prioridad: - | Deadline: -");
            lblRegistrosCPU.setForeground(COLOR_TEXT_MAIN);

            cardCPU.add(lblProcesoRunning);
            cardCPU.add(lblModoEjecucion);
            cardCPU.add(lblRegistrosCPU);

            JPanel cardRAM = crearCardPanel("Ocupación de Memoria RAM");
            barRAM = new JProgressBar(0, comp.getMemoriaRAM().getCapacidadTotal());
            barRAM.setStringPainted(true);
            barRAM.setForeground(COLOR_ACCENT_BLUE);

            JLabel lblRAMInfo = new JLabel("RAM Total: " + comp.getMemoriaRAM().getCapacidadTotal() + " MB");
            lblRAMInfo.setForeground(COLOR_TEXT_MAIN);

            cardRAM.add(lblRAMInfo);
            cardRAM.add(Box.createVerticalStrut(5));
            cardRAM.add(barRAM);

            pnlTop.add(cardCPU);
            pnlTop.add(cardRAM);
            add(pnlTop, BorderLayout.NORTH);

            // Centro: Visualización de las 4 Colas Exigidas
            JPanel pnlColas = new JPanel(new GridLayout(2, 2, 8, 8));
            pnlColas.setOpaque(false);

            JPanel pnlNuevos = crearCardPanel("Cola de Nuevos (New Queue)");
            modelNuevos = new DefaultTableModel(new Object[]{"ID", "Nombre", "Memoria MB", "Tipo"}, 0);
            JTable tblNuevos = new JTable(modelNuevos);
            estilarTabla(tblNuevos);
            pnlNuevos.add(new JScrollPane(tblNuevos));

            JPanel pnlListos = crearCardPanel("Cola de Listos (Ready Queue)");
            modelListos = new DefaultTableModel(new Object[]{"ID", "Nombre", "Prioridad", "Deadline"}, 0);
            JTable tblListos = new JTable(modelListos);
            estilarTabla(tblListos);
            pnlListos.add(new JScrollPane(tblListos));

            JPanel pnlBloqueados = crearCardPanel("Cola de Bloqueados (Blocked Queue & Motivo)");
            modelBloqueados = new DefaultTableModel(new Object[]{"ID", "Nombre", "Motivo Bloqueo"}, 0);
            JTable tblBloqueados = new JTable(modelBloqueados);
            estilarTabla(tblBloqueados);
            pnlBloqueados.add(new JScrollPane(tblBloqueados));

            JPanel pnlTerminados = crearCardPanel("Cola de Terminados (Exit Queue)");
            modelTerminados = new DefaultTableModel(new Object[]{"ID", "Nombre", "Tiempo Modo SO"}, 0);
            JTable tblTerminados = new JTable(modelTerminados);
            estilarTabla(tblTerminados);
            pnlTerminados.add(new JScrollPane(tblTerminados));

            pnlColas.add(pnlNuevos);
            pnlColas.add(pnlListos);
            pnlColas.add(pnlBloqueados);
            pnlColas.add(pnlTerminados);

            add(pnlColas, BorderLayout.CENTER);
        }

        private JPanel crearCardPanel(String titulo) {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBackground(COLOR_PANEL_BG);
            p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_CARD_BG),
                titulo, 0, 0, new Font("Segoe UI", Font.BOLD, 11), COLOR_ACCENT_BLUE));
            return p;
        }

        public void refrescar() {
            Cpu cpu = computador.getCpu();
            if (cpu != null && cpu.estaOcupada()) {
                Pcb p = cpu.getProcesoEnEjecucion();
                lblProcesoRunning.setText("Ejecutando: " + p.getId() + " - " + p.getNombre());
                lblRegistrosCPU.setText("PC: " + p.getProgramCounter() + " | MAR: " + p.getMemoryAddressRegister() + " | Prioridad: " + p.getPrioridad() + " | Deadline: " + p.getDeadline());

                if (cpu.getModo() == enums.ModoEjecucion.SISTEMA_OPERATIVO) {
                    lblModoEjecucion.setText("Modo: KERNEL / SISTEMA OPERATIVO");
                    lblModoEjecucion.setForeground(COLOR_ACCENT_ORANGE);
                } else {
                    lblModoEjecucion.setText("Modo: USUARIO");
                    lblModoEjecucion.setForeground(COLOR_ACCENT_GREEN);
                }
            } else {
                lblProcesoRunning.setText("CPU Libre (Inactiva)");
                lblRegistrosCPU.setText("PC: 0 | MAR: 0 | Prioridad: - | Deadline: -");
                lblModoEjecucion.setText("Modo: USUARIO");
                lblModoEjecucion.setForeground(COLOR_TEXT_MUTED);
            }

            // RAM
            int ramUsada = computador.getMemoriaRAM().getMemoriaUsada();
            int ramTotal = computador.getMemoriaRAM().getCapacidadTotal();
            barRAM.setMaximum(ramTotal);
            barRAM.setValue(ramUsada);
            barRAM.setString(ramUsada + " MB / " + ramTotal + " MB");

            // Refrescar las 4 colas
            refrescarTablaCola(modelNuevos, computador.getColaNuevos(), p -> new Object[]{p.getId(), p.getNombre(), p.getMemoriaAsignada(), p.getTipo()});
            refrescarTablaCola(modelListos, computador.getColaListos(), p -> new Object[]{p.getId(), p.getNombre(), p.getPrioridad(), p.getDeadline()});
            refrescarTablaCola(modelBloqueados, computador.getColaBloqueados(), p -> new Object[]{p.getId(), p.getNombre(), p.getMotivoBloqueo()});
            refrescarTablaCola(modelTerminados, computador.getColaTerminados(), p -> new Object[]{p.getId(), p.getNombre(), p.getTiempoModoSO()});
        }

        private void refrescarTablaCola(DefaultTableModel model, Cola<Pcb> cola, java.util.function.Function<Pcb, Object[]> extractor) {
            model.setRowCount(0);
            if (cola != null && !cola.esVacia()) {
                Cola<Pcb> temp = new Cola<>();
                while (!cola.esVacia()) {
                    Pcb p = cola.desencolar();
                    model.addRow(extractor.apply(p));
                    temp.encolar(p);
                }
                while (!temp.esVacia()) {
                    cola.encolar(temp.desencolar());
                }
            }
        }
    }




    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new VistaMonitoreo().setVisible(true));
    }

   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
