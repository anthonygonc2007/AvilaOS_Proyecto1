/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kernel;

import enums.EstadoProceso;
import enums.ProcesoTipo;
import java.util.UUID;

/**
 *
 * @author Gemelos
 */
public class Pcb {
    private final String id;                  // ID único
    private String nombre;                // Nombre del proceso
    private String computadorId;        
    private EstadoProceso estado;
    private ProcesoTipo tipo;
    private int prioridad;
    private int memoriaAsignada;
    private int tiempoEsperaIO;
    private long deadline;
    private int tiempoRestanteEjecucion;
    
    private int programCounter;           // PC: Dirección de la instrucción actual
    private int memoryAddressRegister;    // MAR: Registro de dirección de memoria
    private String bufferAsociadoId;
    private int ciclosProducirConsumir;
    private int elementosRequeridos;
    private int elementosProcesados;
    private String motivoBloqueo;
    
    // Contador de tiempo invertido en Modo Sistema Operativo
    private int tiempoModoSO;

    public Pcb(String nombre, String computadorId, ProcesoTipo tipo, int prioridad, 
               int memoriaAsignada, long deadline, int tiempoRestanteEjecucion) {
        this.id = "PROC-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        this.nombre = nombre;
        this.computadorId = computadorId;
        this.estado = EstadoProceso.READY;
        this.tipo = tipo;
        this.prioridad = prioridad;
        this.memoriaAsignada = memoriaAsignada;
        this.deadline = deadline;
        this.tiempoRestanteEjecucion = tiempoRestanteEjecucion;
        this.programCounter = 0;
        this.memoryAddressRegister = 0;
        this.elementosProcesados = 0;
        this.motivoBloqueo = "";
        this.tiempoModoSO = 0;
        this.tiempoEsperaIO = 0;
    }

    public void incrementarPC() {
        this.programCounter++;
        this.memoryAddressRegister++;
    }
    
    public void decrementarTiempoEjecucion() {
        if (tiempoRestanteEjecucion > 0) {
            tiempoRestanteEjecucion--;
        }
    }

    public void decrementarDeadline() {
        if (deadline > 0) {
            deadline--;
        }
    }
    
    // Método agregado para registrar la contabilidad de tiempo en kernel
    public void incrementarTiempoModoSO(int costo) {
        this.tiempoModoSO += costo;
    }
    
    public int getTiempoModoSO() { return tiempoModoSO; }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getComputadorId() { return computadorId; }
    
    public EstadoProceso getEstado() { return estado; }
    public void setEstado(EstadoProceso estado) { this.estado = estado; }
    
    public ProcesoTipo getTipo() { return tipo; }
    public int getPrioridad() { return prioridad; }
    public int getMemoriaAsignada() { return memoriaAsignada; }
    
    public long getDeadline() { return deadline; }
    public void setDeadline(long deadline) { this.deadline = deadline; }
    
    public int getTiempoRestanteEjecucion() { return tiempoRestanteEjecucion; }
    public void setTiempoRestanteEjecucion(int tiempoRestanteEjecucion) { this.tiempoRestanteEjecucion = tiempoRestanteEjecucion; }
    
    public int getProgramCounter() { return programCounter; }
    public void setProgramCounter(int programCounter) { this.programCounter = programCounter; }
    
    public int getMemoryAddressRegister() { return memoryAddressRegister; }
    public void setMemoryAddressRegister(int memoryAddressRegister) { this.memoryAddressRegister = memoryAddressRegister; }
    
    public String getBufferAsociadoId() { return bufferAsociadoId; }
    public void setBufferAsociadoId(String bufferAsociadoId) { this.bufferAsociadoId = bufferAsociadoId; }
    
    public int getCiclosProducirConsumir() { return ciclosProducirConsumir; }
    public void setCiclosProducirConsumir(int ciclosProducirConsumir) { this.ciclosProducirConsumir = ciclosProducirConsumir; }
    
    public int getElementosRequeridos() { return elementosRequeridos; }
    public void setElementosRequeridos(int elementosRequeridos) { this.elementosRequeridos = elementosRequeridos; }
    
    public int getElementosProcesados() { return elementosProcesados; }
    public void setElementosProcesados(int elementosProcesados) { this.elementosProcesados = elementosProcesados; }
    
    public String getMotivoBloqueo() { return motivoBloqueo; }
    public void setMotivoBloqueo(String motivoBloqueo) { this.motivoBloqueo = motivoBloqueo; }
    
    public int getTiempoEsperaIO() { return tiempoEsperaIO; }
    public void setTiempoEsperaIO(int tiempoEsperaIO) { this.tiempoEsperaIO = tiempoEsperaIO; }
}