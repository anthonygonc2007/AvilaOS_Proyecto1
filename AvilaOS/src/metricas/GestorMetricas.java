/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package metricas;

/**
 *
 * @author Gemelos
 */
import kernel.Pcb;
import estructuras.ListaEnlazada;

public class GestorMetricas {
    private int totalProcesosCreados = 0;
    private int procesosCompletados = 0;
    private int procesosDeadlinesFallados = 0;
    private int tiempoTotalEjecucionCPU = 0;
    private int sumaTiemposRespuesta = 0;
    
    // Métricas Productor-Consumidor
    private int totalElementosProducidos = 0;
    private int totalElementosConsumidos = 0;
    private int tiempoTotalBloqueadoSemaforos = 0;
    
    // Para medir equidad (varianza del tiempo de CPU asignado)
    private final ListaEnlazada<Integer> tiemposAsignadosPorProceso = new ListaEnlazada<>();

public synchronized void registrarProcesoCompletado(Pcb proceso, int tiempoRespuesta, int tiempoCpuUtilizado) {
        this.procesosCompletados++;
        this.sumaTiemposRespuesta += tiempoRespuesta;
        this.tiemposAsignadosPorProceso.agregarAlFinal(tiempoCpuUtilizado); 
    }

    public synchronized void registrarCicloCPU() {
        this.tiempoTotalEjecucionCPU++;
    }

    public synchronized void registrarBloqueoSemaforo() {
        this.tiempoTotalBloqueadoSemaforos++;
    }
    
    public synchronized void registrarProduccion() { this.totalElementosProducidos++; }
    public synchronized void registrarConsumo() { this.totalElementosConsumidos++; }
    public synchronized void registrarDeadlineFallado() { this.procesosDeadlinesFallados++; }

    // --- CÁLCULO DE INDICADORES ---

    public double calcularThroughput(int cicloGlobalActual) {
        if (cicloGlobalActual <= 0) return 0.0;
        return (double) procesosCompletados / cicloGlobalActual;
    }

    public double calcularUtilizacionCPU(int cicloGlobalActual) {
        if (cicloGlobalActual <= 0) return 0.0;
        return Math.min(100.0, ((double) tiempoTotalEjecucionCPU / cicloGlobalActual) * 100.0);
    }

    public double calcularTiempoRespuestaPromedio() {
        if (procesosCompletados == 0) return 0.0;
        return (double) sumaTiemposRespuesta / procesosCompletados;
    }

    public double calcularTasaCumplimientoDeadlines() {
        int totalFinalizados = procesosCompletados + procesosDeadlinesFallados;
        if (totalFinalizados == 0) return 100.0;
        return ((double) procesosCompletados / totalFinalizados) * 100.0;
    }

    /**
     * Calcula la equidad usando el Índice de Jain (Jain's Fairness Index).
     * 1.0 representa equidad perfecta. Cerca de 0 indica inanición severa.
     */
public double calcularEquidad() {
        if (tiemposAsignadosPorProceso.esVacia()) return 1.0;
        double suma = 0;
        double sumaCuadrados = 0;
        
        int n = tiemposAsignadosPorProceso.getTamano();
        for (int i = 0; i < n; i++) {
            // Adaptado para obtener elementos de la ListaEnlazada
            int t = tiemposAsignadosPorProceso.obtener(i); 
            suma += t;
            sumaCuadrados += (t * t);
        }
        
        if (sumaCuadrados == 0) return 1.0;
        return (suma * suma) / (n * sumaCuadrados);
    }

    // Getters para UI
    public int getProcesosCompletados() { return procesosCompletados; }
    public int getTotalElementosProducidos() { return totalElementosProducidos; }
    public int getTotalElementosConsumidos() { return totalElementosConsumidos; }
    public int getTiempoTotalBloqueadoSemaforos() { return tiempoTotalBloqueadoSemaforos; }
}
