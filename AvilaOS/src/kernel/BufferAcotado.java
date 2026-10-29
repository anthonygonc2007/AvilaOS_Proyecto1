/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kernel;

import kernel.Pcb;
import kernel.Semaforo;
import enums.ModoEjecucion;

public class BufferAcotado {
    private final String idBuffer;
    private final String computadorPropietarioId; // ID del computador donde reside en RAM
    private final Object[] buffer;
    private int in, out;
     private int cantidadElementos;

    // Semáforos para sincronización
    private final Semaforo mutex;
    private final Semaforo espacios;
    private final Semaforo elementos;

    // Costo de latencia de red (ej: 2 ciclos extra si el acceso es remoto)
    private static final int COSTO_LATENCIA_RED = 2;

    public BufferAcotado(String idBuffer, String computadorPropietarioId, int capacidad) {
        this.idBuffer = idBuffer;
        this.computadorPropietarioId = computadorPropietarioId;
        this.buffer = new Object[capacidad];
        this.in = 0;
        this.out = 0;
        this.cantidadElementos = 0;

        this.mutex = new Semaforo(1);
        this.espacios = new Semaforo(capacidad);
        this.elementos = new Semaforo(0);
    }

    public synchronized boolean producir(Pcb proceso, Object item) {
        aplicarCostoRedSiAplica(proceso);
        proceso.incrementarTiempoModoSO(1); // Costo de la llamada al SO

        if (!espacios.semWait(proceso, "BUFFER_LLENO")) return false;
        if (!mutex.semWait(proceso, "MUTEX")) {
            espacios.semSignal(proceso); // Rollback si el mutex está ocupado
            return false;
        }

        // Sección crítica
        buffer[in] = item;
        in = (in + 1) % buffer.length;
        cantidadElementos++;
        proceso.setElementosProcesados(proceso.getElementosProcesados() + 1);

        mutex.semSignal(proceso);
        Pcb despertado = elementos.semSignal(proceso);
        
        // El proceso que despierta vuelve a la cola de listos (manejado en Computador)
        return true; 
    }

    public synchronized Object consumir(Pcb proceso) {
        aplicarCostoRedSiAplica(proceso);
        proceso.incrementarTiempoModoSO(1); // Costo de la llamada al SO

        if (!elementos.semWait(proceso, "BUFFER_VACIO")) return null;
        if (!mutex.semWait(proceso, "MUTEX")) {
            elementos.semSignal(proceso); // Rollback
            return null;
        }

        // Sección crítica
        Object item = buffer[out];
        buffer[out] = null;
        out = (out + 1) % buffer.length;
        cantidadElementos--;
        proceso.setElementosProcesados(proceso.getElementosProcesados() + 1);

        mutex.semSignal(proceso);
        espacios.semSignal(proceso);

        return item;
    }

    private void aplicarCostoRedSiAplica(Pcb proceso) {
        if (!this.computadorPropietarioId.equals(proceso.getComputadorId())) {
            proceso.incrementarTiempoModoSO(COSTO_LATENCIA_RED);
        }
    }

    public String getIdBuffer() { return idBuffer; }
    public String getComputadorPropietarioId() { return computadorPropietarioId; }
    public int getCapacidad() { return buffer.length; }
    public int getCantidadElementos() { return cantidadElementos; }
    public int getValorMutex() { return mutex.getValor(); }
    public int getValorEspacios() { return espacios.getValor(); }
    public int getValorElementos() { return elementos.getValor(); }
}