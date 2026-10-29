/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kernel;

import enums.EstadoProceso;
import enums.PoliticaPlanificacion;
import estructuras.Cola;
import interfaces.Planificador;

/**
 * Representa un nodo del clúster (Computador) en ÁvilaOS.
 * Integra la CPU, Memoria RAM y las colas del sistema operativo.
 * 
 * @author Antho
 */
public class Computador {
    private final String id;
    private final MemoriaRAM memoriaRAM;
    private final Cpu cpu;
    
    // Colas del ciclo de vida del proceso
    private final Cola<Pcb> colaNuevos;
    private final Cola<Pcb> colaListos;
    private final Cola<Pcb> colaBloqueados;
    private final Cola<Pcb> colaTerminados;

    // Componentes de planificación
    private PoliticaPlanificacion politicaActual;
    private Planificador planificador;
    private int quantum;

    public Computador(String id, int capacidadRAM) {
        this.id = id;
        this.memoriaRAM = new MemoriaRAM(capacidadRAM);
        this.cpu = new Cpu();
        
        this.colaNuevos = new Cola<>();
        this.colaListos = new Cola<>();
        this.colaBloqueados = new Cola<>();
        this.colaTerminados = new Cola<>();

        // Política inicial por defecto
        this.quantum = 2;
        setPoliticaPlanificacion(PoliticaPlanificacion.FCFS, this.quantum);
    }

    /**
     * Configura y cambia la política de planificación en tiempo de ejecución.
     */
    public void setPoliticaPlanificacion(PoliticaPlanificacion politica, int quantum) {
        this.politicaActual = politica;
        this.quantum = quantum;
        
        switch (politica) {
            case ROUND_ROBIN:
                this.planificador = new PoliticaRoundRobin(quantum);
                break;
            case PRIORIDAD:
                this.planificador = new PoliticaPrioridades();
                break;
            case EDF:
                this.planificador = new PoliticaEDF();
                break;
            case FCFS:
            default:
                this.planificador = new PoliticaFCFS();
                break;
        }
    }

    /**
     * Ingresa un proceso al sistema validando la regla de no migración.
     * El proceso inicia en la cola de NUEVOS.
     * @param proceso El Pcb a ingresar.
     * @return true si el computador aceptó el proceso, false si violó la no migración.
     */
    public boolean admitirProceso(Pcb proceso) {
        if (!this.id.equals(proceso.getComputadorId())) {
            System.out.println("ALERTA DE SEGURIDAD: Intento de migración bloqueado. " +
                               "El proceso " + proceso.getId() + 
                               " pertenece a " + proceso.getComputadorId() + 
                               " y no puede ingresar a " + this.id);
            return false;
        }
        
        proceso.setEstado(EstadoProceso.NEW);
        this.colaNuevos.encolar(proceso);
        
        // Intentar pasarlo a listo de inmediato si hay memoria
        intentarAdmitirNuevos();
        return true;
    }

    /**
     * Revisa la cola de nuevos y admite a la cola de listos a aquellos procesos
     * que puedan caber en la memoria RAM disponible.
     */
    public void intentarAdmitirNuevos() {
        if (colaNuevos.esVacia()) return;

        Cola<Pcb> aux = new Cola<>();
        while (!colaNuevos.esVacia()) {
            Pcb proceso = colaNuevos.desencolar();
            // Intenta cargar en RAM; si hay espacio, se admite
            if (memoriaRAM.cargarProceso(proceso)) {
                proceso.setEstado(EstadoProceso.READY);
                colaListos.encolar(proceso);
            } else {
                // Si no hay memoria, permanece en espera
                aux.encolar(proceso);
            }
        }
        
        // Regresar los procesos no admitidos a la cola original
        while (!aux.esVacia()) {
            colaNuevos.encolar(aux.desencolar());
        }
    }

    /**
     * Coordina un ciclo de reloj para los componentes de este computador.
     */
    public void ejecutarCicloReloj(BufferAcotado[] buffers) {
        // 1. Intentar admitir procesos retenidos en Nuevos
        intentarAdmitirNuevos();

        // 2. Decrementar el tiempo límite (Deadlines)
        decrementarDeadlines();

        // 3. Ejecutar instrucción actual en la CPU
        if (cpu.estaOcupada()) {
            Pcb enCpu = cpu.getProcesoEnEjecucion();
            
            // Si el proceso interactúa con un buffer
            if (enCpu.getTipo() == enums.ProcesoTipo.PRODUCTOR || enCpu.getTipo() == enums.ProcesoTipo.CONSUMIDOR) {
                procesarPcbBuffer(enCpu, buffers);
            } else {
                cpu.ejecutarCiclo();
            }

            // 4. Liberación de recursos al terminar
            if (enCpu.getEstado() == EstadoProceso.EXIT) {
                memoriaRAM.liberarProceso(enCpu);
                colaTerminados.encolar(enCpu);
                cpu.setProcesoEnEjecucion(null);
                
                // Al liberar memoria, revisamos si puede entrar alguien nuevo
                intentarAdmitirNuevos();
            }
        }

        // 5. Invocar al planificador activo
        if (planificador != null) {
            planificador.planificar(colaListos, cpu);
        }
    }

    /**
     * Procesa la sincronización si el proceso en CPU es un Productor o Consumidor.
     */
    private void procesarPcbBuffer(Pcb p, BufferAcotado[] buffers) {
        BufferAcotado buf = buscarBuffer(p.getBufferAsociadoId(), buffers);
        if (buf == null) {
            cpu.ejecutarCiclo(); // Cae en comportamiento normal (CPU-bound) si el buffer no existe
            return;
        }

        if (p.getTipo() == enums.ProcesoTipo.PRODUCTOR) {
            boolean exito = buf.producir(p, "Item-" + p.getId());
            if (!exito) {
                // Se bloqueó en semWait
                cpu.setProcesoEnEjecucion(null);
                colaBloqueados.encolar(p);
            }
        } else if (p.getTipo() == enums.ProcesoTipo.CONSUMIDOR) {
            Object item = buf.consumir(p);
            if (item == null && p.getEstado() != EstadoProceso.RUNNING) {
                // Se bloqueó en semWait
                cpu.setProcesoEnEjecucion(null);
                colaBloqueados.encolar(p);
            }
        }
    }

    private BufferAcotado buscarBuffer(String idBuffer, BufferAcotado[] buffers) {
        if (buffers == null) return null;
        for (BufferAcotado b : buffers) {
            if (b.getIdBuffer().equalsIgnoreCase(idBuffer)) return b;
        }
        return null;
    }

    private void decrementarDeadlines() {
        decrementarDeadlinesCola(colaListos);
        decrementarDeadlinesCola(colaBloqueados);
    }

    private void decrementarDeadlinesCola(Cola<Pcb> cola) {
        if (cola == null || cola.esVacia()) return;
        Cola<Pcb> aux = new Cola<>();
        while (!cola.esVacia()) {
            Pcb p = cola.desencolar();
            p.setDeadline(p.getDeadline() - 1);
            aux.encolar(p);
        }
        while (!aux.esVacia()) {
            cola.encolar(aux.desencolar());
        }
    }

    // --- Getters ---
    public String getId() { return id; }
    public MemoriaRAM getMemoriaRAM() { return memoriaRAM; }
    public Cpu getCpu() { return cpu; }
    public Cola<Pcb> getColaNuevos() { return colaNuevos; }
    public Cola<Pcb> getColaListos() { return colaListos; }
    public Cola<Pcb> getColaBloqueados() { return colaBloqueados; }
    public Cola<Pcb> getColaTerminados() { return colaTerminados; }
    public PoliticaPlanificacion getPoliticaActual() { return politicaActual; }
}