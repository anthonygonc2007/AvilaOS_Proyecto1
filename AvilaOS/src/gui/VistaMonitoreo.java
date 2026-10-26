/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package gui;

import core.RelojGlobal;
import enums.PoliticaPlanificacion;
import estructuras.Cola;
import io.ConfiguracionSimulacion;
import kernel.BufferAcotado;
import kernel.Computador;
import kernel.Cpu;
import kernel.Pcb;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 *
 * @author Usuario
 */
public class VistaMonitoreo extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(VistaMonitoreo.class.getName());

    private static final Color COLOR_BG = new Color(18, 24, 38);
    private static final Color COLOR_PANEL_BG = new Color(28, 36, 54);
    private static final Color COLOR_CARD_BG = new Color(38, 48, 70);
    private static final Color COLOR_TEXT_MAIN = new Color(240, 244, 250);
    private static final Color COLOR_TEXT_MUTED = new Color(160, 175, 200);
    private static final Color COLOR_ACCENT_BLUE = new Color(51, 153, 255);
    private static final Color COLOR_ACCENT_GREEN = new Color(46, 204, 113);
    private static final Color COLOR_ACCENT_ORANGE = new Color(230, 126, 34);

    private JLabel lblRelojCiclos;
    private JLabel lblEstadoSimulacion;
    private JTextArea txtLogEventos;
    private JSpinner spinnerDuracionCiclo;
    private JSpinner spinnerLatenciaRed;
    private JTable tblBuffersEstado;
    private DefaultTableModel modelBuffers;
    private ConfiguracionSimulacion configuracionActual;
    private Computador[] listaComputadores;
    private BufferAcotado[] listaBuffers;
    private Timer timerSimulador;
    private boolean enEjecucion = false;
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
        setTitle("ÁvilaOS - Dashboard de Monitoreo y Simulación Distribuida");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BG);
        getContentPane().setLayout(new BorderLayout(10, 10));

        // 1. HEADER (Control Global y Reloj)
        JPanel panelHeader = crearPanelHeader();
        getContentPane().add(panelHeader, BorderLayout.NORTH);

        // 2. PANEL CENTRAL (Split entre Computadores y Log/Buffers)
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setResizeWeight(0.68);
        mainSplit.setBackground(COLOR_BG);
        mainSplit.setBorder(null);

        // Lado Izquierdo: TabbedPane con cada Computador
        tabbedComputadores = new JTabbedPane();
        tabbedComputadores.setBackground(COLOR_PANEL_BG);
        tabbedComputadores.setForeground(COLOR_TEXT_MAIN);
        mainSplit.setLeftComponent(tabbedComputadores);

        // Lado Derecho: Log de Eventos y Estado de Buffers/Semáforos
        JPanel panelDerecho = crearPanelLateralDerecho();
        mainSplit.setRightComponent(panelDerecho);

        getContentPane().add(mainSplit, BorderLayout.CENTER);

        // 3. FOOTER (Status bar)
        JPanel panelFooter = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFooter.setBackground(COLOR_PANEL_BG);
        lblEstadoSimulacion = new JLabel("Estado: Configuración cargada. Listo para iniciar.");
        lblEstadoSimulacion.setForeground(COLOR_TEXT_MUTED);
        panelFooter.add(lblEstadoSimulacion);
        getContentPane().add(panelFooter, BorderLayout.SOUTH);
    }

    /**
     * Crea el Panel Superior con Reloj Global, Controles y Cambios de Parámetros Dinámicos.
     */
    private JPanel crearPanelHeader() {
        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setBackground(COLOR_PANEL_BG);
        header.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

        // Título y Reloj Global
        JPanel pnlTitle = new JPanel(new GridLayout(2, 1));
        pnlTitle.setOpaque(false);
        JLabel title = new JLabel("ÁVILA OS - MONITOR DE SISTEMA DISTRIBUIDO");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(COLOR_ACCENT_BLUE);

        lblRelojCiclos = new JLabel("Reloj Global del Sistema: Ciclo 0");
        lblRelojCiclos.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblRelojCiclos.setForeground(COLOR_TEXT_MAIN);

        pnlTitle.add(title);
        pnlTitle.add(lblRelojCiclos);
        header.add(pnlTitle, BorderLayout.WEST);

        // Controles de Simulación (Iniciar, Pausar, Reiniciar)
        JPanel pnlControles = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        pnlControles.setOpaque(false);

        JButton btnIniciar = crearBotonEstilizado("Iniciar Simulación", COLOR_ACCENT_GREEN);
        JButton btnPausar = crearBotonEstilizado("Pausar", COLOR_ACCENT_ORANGE);
        JButton btnPasoAPaso = crearBotonEstilizado("Paso a Paso (1 Ciclo)", COLOR_ACCENT_BLUE);

        btnIniciar.addActionListener(e -> iniciarSimulacionTimer());
        btnPausar.addActionListener(e -> pausarSimulacionTimer());
        btnPasoAPaso.addActionListener(e -> ejecutarUnCicloSimulacion());

        pnlControles.add(btnIniciar);
        pnlControles.add(btnPausar);
        pnlControles.add(btnPasoAPaso);
        header.add(pnlControles, BorderLayout.CENTER);

        // Parámetros Dinámicos en Tiempo de Ejecución (Duración del Ciclo y Latencia)
        JPanel pnlParametros = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlParametros.setOpaque(false);

        JLabel lblDuracion = new JLabel("Duración Ciclo (ms):");
        lblDuracion.setForeground(COLOR_TEXT_MUTED);
        
        // Duración de Ciclo Segura (Protegida contra valores fuera de rango)
        int duracion = (configuracionActual != null && configuracionActual.duracionCicloMs >= 50) 
                       ? configuracionActual.duracionCicloMs : 1000;
        duracion = Math.max(50, Math.min(duracion, 5000));
        spinnerDuracionCiclo = new JSpinner(new SpinnerNumberModel(duracion, 50, 5000, 100));
        spinnerDuracionCiclo.addChangeListener(e -> {
            int nuevaDuracion = (int) spinnerDuracionCiclo.getValue();
            if (timerSimulador != null) {
                timerSimulador.setDelay(nuevaDuracion);
            }
            registrarLog("Parámetro actualizado: Duración de ciclo cambiada a " + nuevaDuracion + " ms.");
        });

        JLabel lblLatencia = new JLabel("Latencia Red (ciclos):");
        lblLatencia.setForeground(COLOR_TEXT_MUTED);
        
        // Latencia de Red Segura (Protegida contra valores fuera de rango)
        int latencia = (configuracionActual != null && configuracionActual.latenciaRedCiclos >= 0) 
                       ? configuracionActual.latenciaRedCiclos : 2;
        latencia = Math.max(0, Math.min(latencia, 20));
        spinnerLatenciaRed = new JSpinner(new SpinnerNumberModel(latencia, 0, 20, 1));
        spinnerLatenciaRed.addChangeListener(e -> {
            int nuevaLatencia = (int) spinnerLatenciaRed.getValue();
            if (configuracionActual != null) {
                configuracionActual.latenciaRedCiclos = nuevaLatencia;
            }
            registrarLog("Parámetro actualizado: Latencia de red cambiada a " + nuevaLatencia + " ciclos.");
        });

        pnlParametros.add(lblDuracion);
        pnlParametros.add(spinnerDuracionCiclo);
        pnlParametros.add(lblLatencia);
        pnlParametros.add(spinnerLatenciaRed);

        header.add(pnlParametros, BorderLayout.EAST);

        return header;
    }

    /**
     * Panel Derecho: Logs de Eventos Globales y Estado de Buffers / Semáforos.
     */
    private JPanel crearPanelLateralDerecho() {
        JPanel pnlRight = new JPanel(new GridLayout(2, 1, 0, 10));
        pnlRight.setBackground(COLOR_BG);

        // 1. Estado de Buffers Compartidos y Semáforos
        JPanel pnlBuffers = new JPanel(new BorderLayout());
        pnlBuffers.setBackground(COLOR_PANEL_BG);
        pnlBuffers.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_CARD_BG),
                "Buffers Compartidos & Estado de Semáforos",
                0, 0, new Font("Segoe UI", Font.BOLD, 12), COLOR_ACCENT_BLUE));

        modelBuffers = new DefaultTableModel(
                new Object[]{"Buffer ID", "Capacidad", "Elementos", "Anfitrión", "Mutex", "Lleno", "Vacío"}, 0
        );
        tblBuffersEstado = new JTable(modelBuffers);
        estilarTabla(tblBuffersEstado);

        pnlBuffers.add(new JScrollPane(tblBuffersEstado), BorderLayout.CENTER);

        // Botón para crear nuevo Buffer "Al Vuelo" (Exigencia Sección 7)
        JButton btnNuevoBuffer = crearBotonEstilizado("+ Crear Nuevo Buffer en Caliente", COLOR_CARD_BG);
        btnNuevoBuffer.addActionListener(this::accionCrearNuevoBuffer);
        pnlBuffers.add(btnNuevoBuffer, BorderLayout.SOUTH);

        // 2. Registro Global de Eventos (Log del Kernel)
        JPanel pnlLog = new JPanel(new BorderLayout());
        pnlLog.setBackground(COLOR_PANEL_BG);
        pnlLog.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_CARD_BG),
                "Registro Global de Eventos del Kernel (Log)",
                0, 0, new Font("Segoe UI", Font.BOLD, 12), COLOR_ACCENT_BLUE));

        txtLogEventos = new JTextArea();
        txtLogEventos.setEditable(false);
        txtLogEventos.setBackground(new Color(15, 20, 30));
        txtLogEventos.setForeground(new Color(180, 220, 180));
        txtLogEventos.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtLogEventos.setMargin(new Insets(8, 8, 8, 8));

        JScrollPane scrollLog = new JScrollPane(txtLogEventos);
        pnlLog.add(scrollLog, BorderLayout.CENTER);

        pnlRight.add(pnlBuffers);
        pnlRight.add(pnlLog);

        return pnlRight;
    }

    /**
     * Inicializa las estructuras del Backend con el DTO recibido de VistaConfiguracion.
     * @param config 
     */
    public void inicializarSimulacion(ConfiguracionSimulacion config) {
        this.configuracionActual = config;
        RelojGlobal.reiniciar();

        if (config == null) return;

        // 1. Aplicar parámetros en la GUI
        spinnerDuracionCiclo.setValue(config.duracionCicloMs);
        spinnerLatenciaRed.setValue(config.latenciaRedCiclos);

        // 2. Crear Computadores
        int nComp = (config.computadores != null) ? config.computadores.length : 0;
        listaComputadores = new Computador[nComp];
        panelesComputadores = new PanelComputadorUI[nComp];
        tabbedComputadores.removeAll();

        for (int i = 0; i < nComp; i++) {
            ConfiguracionSimulacion.ConfigComputador cc = config.computadores[i];
            int ram = (cc.ramTotal > 0) ? cc.ramTotal : 2048; 

            listaComputadores[i] = new Computador(cc.id, ram);

            // Crear el panel visual para esta máquina
            PanelComputadorUI panelUI = new PanelComputadorUI(listaComputadores[i], cc.politicaInicial, cc.quantum);
            panelesComputadores[i] = panelUI;
            tabbedComputadores.addTab("Computador " + cc.id, panelUI);
        }

        // 3. Crear Buffers
        int nBuf = (config.buffers != null) ? config.buffers.length : 0;
        listaBuffers = new BufferAcotado[nBuf];
        for (int i = 0; i < nBuf; i++) {
            ConfiguracionSimulacion.ConfigBuffer cb = config.buffers[i];
            listaBuffers[i] = new BufferAcotado(cb.id, cb.capacidad, cb.computadorAnfitrionId);
        }

        // 4. Cargar Procesos en los Computadores y en su Memoria RAM
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

                // Asignar al computador correspondiente y cargar en RAM
                for (Computador comp : listaComputadores) {
                    if (comp.getId().equals(cp.computadorId)) {
                        comp.admitirProceso(pcb);
                        comp.getMemoriaRAM().cargarProceso(pcb); // ✔ Carga en la memoria RAM del backend
                        break;
                    }
                }
            }
        }

        // Configurar Timer de simulación
        timerSimulador = new Timer(config.duracionCicloMs, (ActionEvent e) -> ejecutarUnCicloSimulacion());

        // Refrescar paneles con la RAM cargada
        for (PanelComputadorUI pUI : panelesComputadores) {
            if (pUI != null) pUI.refrescar();
        }

        actualizarTablasYGraficos();
        registrarLog("ÁvilaOS inicializado correctamente. Clúster cargado con " + nComp + " computadores.");
    }

    /**
     * Motor principal del avance de la simulación por ciclo de reloj.
     */
    private void ejecutarUnCicloSimulacion() {
        RelojGlobal.avanzarCiclo();
        int cicloActual = RelojGlobal.getCicloActual();
        lblRelojCiclos.setText("Reloj Global del Sistema: Ciclo " + cicloActual);

        // 1. Para cada computador: Ejecutar CPU y planificar
        for (int i = 0; i < listaComputadores.length; i++) {
            Computador comp = listaComputadores[i];
            Cpu cpu = comp.getCpu();

            // Ejecutar ciclo en CPU
            if (cpu.estaOcupada()) {
                Pcb procesoEnCpu = cpu.getProcesoEnEjecucion();
                cpu.ejecutarCiclo();
                registrarLog("[Ciclo " + cicloActual + "] Comp " + comp.getId() + ": Proceso " + procesoEnCpu.getNombre() + " ejecutando en CPU (PC=" + procesoEnCpu.getProgramCounter() + ").");
            }

            // Actualizar vista del computador
            panelesComputadores[i].refrescar();
        }

        actualizarTablasYGraficos();
    }

    private void iniciarSimulacionTimer() {
        if (timerSimulador != null && !enEjecucion) {
            timerSimulador.start();
            enEjecucion = true;
            lblEstadoSimulacion.setText("Estado: Simulación en ejecución...");
            lblEstadoSimulacion.setForeground(COLOR_ACCENT_GREEN);
            registrarLog(">>> Simulación INICIADA.");
        }
    }

    private void pausarSimulacionTimer() {
        if (timerSimulador != null && enEjecucion) {
            timerSimulador.stop();
            enEjecucion = false;
            lblEstadoSimulacion.setText("Estado: Simulación Pausada.");
            lblEstadoSimulacion.setForeground(COLOR_ACCENT_ORANGE);
            registrarLog(">>> Simulación PAUSADA.");
        }
    }

    private void actualizarTablasYGraficos() {
        // Refrescar Tabla de Buffers
        modelBuffers.setRowCount(0);
        if (listaBuffers != null) {
            for (BufferAcotado b : listaBuffers) {
                modelBuffers.addRow(new Object[]{
                        b.getIdBuffer(),
                        b.getCapacidad(),
                        b.getElementosActuales(),
                        b.getComputadorAnfitrionId(),
                        b.getMutex().getValor(),
                        b.getElementosLlenos().getValor(),
                        b.getEspaciosVacios().getValor()
                });
            }
        }
    }

    private void registrarLog(String mensaje) {
        txtLogEventos.append(mensaje + "\n");
        txtLogEventos.setCaretPosition(txtLogEventos.getDocument().getLength());
    }

    /**
     * Acción para crear un nuevo buffer en tiempo de ejecución (Sección 7).
     */
    private void accionCrearNuevoBuffer(ActionEvent e) {
        JTextField txtId = new JTextField();
        JTextField txtCap = new JTextField("5");
        JTextField txtCompId = new JTextField("1");

        Object[] message = {
                "ID del Nuevo Buffer (ej: B3):", txtId,
                "Capacidad Máxima:", txtCap,
                "ID Computador Anfitrión:", txtCompId
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Crear Nuevo Buffer en Caliente", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String id = txtId.getText().trim();
                int cap = Integer.parseInt(txtCap.getText().trim());
                String compId = txtCompId.getText().trim();

                BufferAcotado nuevoBuf = new BufferAcotado(id, cap, compId);

                // Redimensionar arreglo nativo de buffers
                BufferAcotado[] temp = new BufferAcotado[listaBuffers.length + 1];
                System.arraycopy(listaBuffers, 0, temp, 0, listaBuffers.length);
                temp[listaBuffers.length] = nuevoBuf;
                listaBuffers = temp;

                actualizarTablasYGraficos();
                registrarLog("Buffer dinamico creado: " + id + " en Computador " + compId + " con capacidad " + cap);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Datos inválidos para la creación del buffer.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --- MÉTODOS ESTÍLO Y DISEÑO VISUAL ---
    private JButton crearBotonEstilizado(String texto, Color bg) {
        JButton btn = new JButton(texto);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        return btn;
    }

    private void estilarTabla(JTable tabla) {
        tabla.setBackground(new Color(22, 30, 44));
        tabla.setForeground(COLOR_TEXT_MAIN);
        tabla.setGridColor(COLOR_CARD_BG);
        tabla.getTableHeader().setBackground(COLOR_CARD_BG);
        tabla.getTableHeader().setForeground(COLOR_ACCENT_BLUE);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tabla.setRowHeight(22);
    }

    private class PanelComputadorUI extends JPanel {
        private final Computador computador;

        private JLabel lblProcesoRunning;
        private JLabel lblModoEjecucion; 
        private JLabel lblRegistrosCPU; 
        private JProgressBar barRAM;
        private JComboBox<PoliticaPlanificacion> comboPolitica;
        private JSpinner spinnerQuantum;
        private DefaultTableModel modelListos;
        private DefaultTableModel modelBloqueados;

        public PanelComputadorUI(Computador comp, String polInicial, int quantum) {
            this.computador = comp;
            setLayout(new BorderLayout(10, 10));
            setBackground(COLOR_BG);
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // 1. PANEL SUPERIOR (CPU, RAM y Política)
            JPanel pnlTop = new JPanel(new GridLayout(1, 3, 10, 0));
            pnlTop.setOpaque(false);

            // Card 1: CPU
            JPanel cardCPU = crearCardPanel("Estado de la CPU (Procesador)");
            lblProcesoRunning = new JLabel("Proceso: NINGUNO (CPU Libre)");
            lblProcesoRunning.setForeground(COLOR_ACCENT_GREEN);
            lblProcesoRunning.setFont(new Font("Segoe UI", Font.BOLD, 12));
            
            lblModoEjecucion = new JLabel("Modo de Ejecución: Modo Usuario");
            lblModoEjecucion.setForeground(COLOR_TEXT_MUTED);
            
            lblRegistrosCPU = new JLabel("PC: 0 | MAR: 0 | Prioridad: - | Deadline: -");
            lblRegistrosCPU.setForeground(COLOR_TEXT_MAIN);

            cardCPU.add(lblProcesoRunning);
            cardCPU.add(lblModoEjecucion);
            cardCPU.add(lblRegistrosCPU);

            // Card 2: Memoria RAM
            JPanel cardRAM = crearCardPanel("Ocupación de Memoria RAM");
            int capRAM = (comp.getMemoriaRAM() != null && comp.getMemoriaRAM().getCapacidadTotal() > 0) 
                         ? comp.getMemoriaRAM().getCapacidadTotal() : 2048;

            barRAM = new JProgressBar(0, capRAM);
            barRAM.setValue(0);
            barRAM.setStringPainted(true);
            barRAM.setForeground(COLOR_ACCENT_BLUE);

            JLabel lblRAMTotal = new JLabel("RAM Total: " + capRAM + " MB");
            lblRAMTotal.setForeground(COLOR_TEXT_MAIN); // Usa la constante de texto claro/blanco brillante (new Color(240, 244, 250))
            lblRAMTotal.setFont(new Font("Segoe UI", Font.BOLD, 12));

            cardRAM.add(lblRAMTotal);
            cardRAM.add(Box.createVerticalStrut(5));
            cardRAM.add(barRAM);
            // Card 3: Política de Planificación
            JPanel cardPol = crearCardPanel("Política de Planificación");
            comboPolitica = new JComboBox<>(PoliticaPlanificacion.values());
            try {
                comboPolitica.setSelectedItem(PoliticaPlanificacion.valueOf(polInicial.toUpperCase()));
            } catch (Exception ex) {
                comboPolitica.setSelectedItem(PoliticaPlanificacion.FCFS);
            }

            comboPolitica.addActionListener(e -> {
                PoliticaPlanificacion sel = (PoliticaPlanificacion) comboPolitica.getSelectedItem();
                registrarLog("Computador " + comp.getId() + " cambió política de planificación a: " + sel);
            });

            int quantumSeguro = Math.max(1, Math.min(quantum, 10));
            spinnerQuantum = new JSpinner(new SpinnerNumberModel(quantumSeguro, 1, 10, 1));
            JPanel pnlQ = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            pnlQ.setOpaque(false);
            JLabel lblQ = new JLabel("Quantum:");
            lblQ.setForeground(COLOR_TEXT_MUTED);
            pnlQ.add(lblQ);
            pnlQ.add(spinnerQuantum);

            cardPol.add(comboPolitica);
            cardPol.add(pnlQ);

            // Agregar las 3 tarjetas en orden a pnlTop
            pnlTop.add(cardCPU);
            pnlTop.add(cardRAM);
            pnlTop.add(cardPol);

            add(pnlTop, BorderLayout.NORTH);

            // 2. PANEL CENTRAL: COLAS
            JPanel pnlColas = new JPanel(new GridLayout(1, 2, 10, 0));
            pnlColas.setOpaque(false);

            JPanel pnlListos = crearCardPanel("Cola de Listos (Ready Queue)");
            modelListos = new DefaultTableModel(new Object[]{"ID", "Nombre", "Tipo", "Prioridad", "Deadline"}, 0);
            JTable tblListos = new JTable(modelListos);
            estilarTabla(tblListos);
            pnlListos.add(new JScrollPane(tblListos));

            JPanel pnlBloqueados = crearCardPanel("Cola de Bloqueados (Blocked Queue & Semáforos)");
            modelBloqueados = new DefaultTableModel(new Object[]{"ID", "Nombre", "Motivo de Bloqueo / Semáforo"}, 0);
            JTable tblBloqueados = new JTable(modelBloqueados);
            estilarTabla(tblBloqueados);
            pnlBloqueados.add(new JScrollPane(tblBloqueados));

            pnlColas.add(pnlListos);
            pnlColas.add(pnlBloqueados);

            add(pnlColas, BorderLayout.CENTER);
        }

        private JPanel crearCardPanel(String titulo) {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBackground(COLOR_PANEL_BG);
            p.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(COLOR_CARD_BG),
                    titulo, 0, 0, new Font("Segoe UI", Font.BOLD, 12), COLOR_ACCENT_BLUE));
            return p;
        }

        public void refrescar() {
            // 1. Refrescar CPU
            Cpu cpu = computador.getCpu();
            if (cpu != null && cpu.estaOcupada()) {
                Pcb p = cpu.getProcesoEnEjecucion();
                lblProcesoRunning.setText("Proceso: " + p.getId() + " - " + p.getNombre());
                lblRegistrosCPU.setText("PC: " + p.getProgramCounter() + " | MAR: " + p.getMemoryAddressRegister() +
                        " | Prioridad: " + p.getPrioridad() + " | Deadline: " + p.getDeadline());

                if (p.getTiempoModoSO() > 0) {
                    lblModoEjecucion.setText("Modo de Ejecución: KERNEL / SISTEMA OPERATIVO");
                    lblModoEjecucion.setForeground(COLOR_ACCENT_ORANGE);
                } else {
                    lblModoEjecucion.setText("Modo de Ejecución: MODO USUARIO");
                    lblModoEjecucion.setForeground(COLOR_ACCENT_GREEN);
                }
            } else {
                lblProcesoRunning.setText("Proceso: NINGUNO (CPU Libre)");
                lblRegistrosCPU.setText("PC: 0 | MAR: 0 | Prioridad: - | Deadline: -");
                lblModoEjecucion.setText("Modo de Ejecución: Modo Usuario");
                lblModoEjecucion.setForeground(COLOR_TEXT_MUTED);
            }

            // 2. Refrescar Memoria RAM
            int ramUsada = (computador.getMemoriaRAM() != null) ? computador.getMemoriaRAM().getMemoriaUsada() : 0;
            int ramTotal = (computador.getMemoriaRAM() != null && computador.getMemoriaRAM().getCapacidadTotal() > 0) 
                           ? computador.getMemoriaRAM().getCapacidadTotal() : 2048;

            int valorSeguro = Math.max(0, Math.min(ramUsada, ramTotal));
            
            barRAM.setMaximum(ramTotal);
            barRAM.setValue(valorSeguro);
            barRAM.setString(valorSeguro + " MB / " + ramTotal + " MB");

            // 3. Refrescar Cola de Listos
            modelListos.setRowCount(0);
            Cola<Pcb> colaL = computador.getColaListos();
            if (colaL != null && !colaL.esVacia()) {
                Cola<Pcb> temp = new Cola<>();
                while (!colaL.esVacia()) {
                    Pcb p = colaL.desencolar();
                    modelListos.addRow(new Object[]{p.getId(), p.getNombre(), p.getTipo(), p.getPrioridad(), p.getDeadline()});
                    temp.encolar(p);
                }
                while (!temp.esVacia()) {
                    colaL.encolar(temp.desencolar());
                }
            }

            // 4. Refrescar Cola de Bloqueados
            modelBloqueados.setRowCount(0);
            Cola<Pcb> colaB = computador.getColaBloqueados();
            if (colaB != null && !colaB.esVacia()) {
                Cola<Pcb> tempB = new Cola<>();
                while (!colaB.esVacia()) {
                    Pcb p = colaB.desencolar();
                    modelBloqueados.addRow(new Object[]{p.getId(), p.getNombre(), p.getMotivoBloqueo()});
                    tempB.encolar(p);
                }
                while (!tempB.esVacia()) {
                    colaB.encolar(tempB.desencolar());
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
