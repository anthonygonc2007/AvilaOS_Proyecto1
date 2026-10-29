/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kernel;

import enums.EstadoProceso;
import enums.PoliticaPlanificacion;
import estructuras.Cola;
import estructuras.ListaEnlazada;
import interfaces.Planificador;
import metricas.GestorMetricas;
import interfaces.IClockObserver;
import core.RelojGlobal;
/**
 * Representa un nodo del clúster (Computador) en ÁvilaOS.
 * Integra la CPU, Memoria RAM y las colas del sistema operativo.
 * 
 * @author Antho
 */
public class Computador implements IClockObserver {
    private final String id;
    private final MemoriaRAM memoriaRAM;
    private final Cpu cpu;
    
    // Colas del ciclo de vida del proceso
    private final Cola<Pcb> colaNuevos;
    private final Cola<Pcb> colaListos;
    private final Cola<Pcb> colaBloqueados;
    private final Cola<Pcb> colaTerminados;
    private ListaEnlazada<BufferAcotado> listaBuffersGlobales;

    // Componentes de planificación
    private PoliticaPlanificacion politicaActual;
    private Planificador planificador;
    private int quantum;
    private final GestorMetricas gestorMetricas;

    public Computador(String id, int capacidadRAM, GestorMetricas gestorMetricas, ListaEnlazada<BufferAcotado> buffers) {
        this.id = id;
        this.memoriaRAM = new MemoriaRAM(capacidadRAM);
        this.gestorMetricas = gestorMetricas;
        this.listaBuffersGlobales = buffers;
        this.cpu = new Cpu();
        
        this.colaNuevos = new Cola<>();
        this.colaListos = new Cola<>();
        this.colaBloqueados = new Cola<>();
        this.colaTerminados = new Cola<>();

        // Política inicial por defecto
        this.quantum = 2;
        setPoliticaPlanificacion(PoliticaPlanificacion.FCFS, this.quantum);
        
        RelojGlobal.registrarObservador(this);
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
        intentarAdmitirNuevos();
        revisarDesbloqueados(); 
        decrementarDeadlines();
        if (cpu.estaOcupada()) {
            gestorMetricas.registrarCicloCPU();
            Pcb enCpu = cpu.getProcesoEnEjecucion();

            if (enCpu.getTipo() == enums.ProcesoTipo.PRODUCTOR || enCpu.getTipo() == enums.ProcesoTipo.CONSUMIDOR) {
                cpu.setModoSO();
                procesarPcbBuffer(enCpu, buffers);
            } else {
                cpu.ejecutarCiclo();
            }

            if (enCpu.getEstado() == EstadoProceso.EXIT) {
                memoriaRAM.liberarProceso(enCpu);
                colaTerminados.encolar(enCpu);
                cpu.setProcesoEnEjecucion(null);
                gestorMetricas.registrarProcesoCompletado(enCpu, 0, enCpu.getTiempoModoSO());
                intentarAdmitirNuevos();
            }
        }

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
            cpu.ejecutarCiclo(); 
            return;
        }

        int cicloActual = p.getTiempoModoSO(); 
        p.incrementarTiempoModoSO(1);

        if (cicloActual % Math.max(1, p.getCiclosProducirConsumir()) == 0) {
            boolean exitoOp = true;
            if (p.getTipo() == enums.ProcesoTipo.PRODUCTOR) {
                exitoOp = buf.producir(p, "Item-" + p.getId());
            } else if (p.getTipo() == enums.ProcesoTipo.CONSUMIDOR) {
                Object item = buf.consumir(p);
                exitoOp = (item != null);
            }

            if (exitoOp) {
                if (p.getElementosProcesados() >= p.getElementosRequeridos()) {
                    p.setEstado(EstadoProceso.EXIT);
                }
            } else {
                cpu.setProcesoEnEjecucion(null);
                colaBloqueados.encolar(p);
            }
        } else {
            cpu.ejecutarCiclo();
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

            // CORRECCIÓN 1: Expulsar el proceso si el tiempo se agotó
            if (p.getDeadline() <= 0 && p.getEstado() != EstadoProceso.EXIT) {
                p.setEstado(EstadoProceso.EXIT);
                memoriaRAM.liberarProceso(p);
                colaTerminados.encolar(p);
                gestorMetricas.registrarProcesoCompletado(p, 0, p.getTiempoModoSO());
            } else {
                aux.encolar(p);
            }
        }
        while (!aux.esVacia()) {
            cola.encolar(aux.desencolar());
        }
    }
    private void revisarDesbloqueados() {
    if (colaBloqueados.esVacia()) return;
    Cola<Pcb> aux = new Cola<>();
    while (!colaBloqueados.esVacia()) {
        Pcb p = colaBloqueados.desencolar();
        // Si el semáforo interno del buffer ya le cambió el estado a READY
        if (p.getEstado() == EstadoProceso.READY) {
            colaListos.encolar(p);
        } else {
            aux.encolar(p);
        }
    }
    while (!aux.esVacia()) {
        colaBloqueados.encolar(aux.desencolar());
    }
}

    @Override
    public void tick(int cicloActual) {
        BufferAcotado[] arregloBuffers = new BufferAcotado[this.listaBuffersGlobales.getTamano()];
        for (int i = 0; i < this.listaBuffersGlobales.getTamano(); i++) {
            arregloBuffers[i] = this.listaBuffersGlobales.obtener(i);
        }
        ejecutarCicloReloj(arregloBuffers);
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