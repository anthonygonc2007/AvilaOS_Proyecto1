/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kernel;

/**
 *
 * @author Antho
 */
import estructuras.ListaEnlazada;

public class MemoriaRAM {
    private final int capacidadTotal;
    private int memoriaUsada;
    private final ListaEnlazada<Pcb> procesosEnMemoria;

    public MemoriaRAM(int capacidadTotal) {
        this.capacidadTotal = capacidadTotal;
        this.memoriaUsada = 0;
        this.procesosEnMemoria = new ListaEnlazada<>();
    }

    public synchronized boolean cargarProceso(Pcb proceso) {
        if (proceso == null) return false;
        
        int espacioRequerido = proceso.getMemoriaAsignada();
        if (memoriaUsada + espacioRequerido <= capacidadTotal) {
            procesosEnMemoria.agregarAlFinal(proceso);
            memoriaUsada += espacioRequerido;
            return true;
        }
        return false;
    }

    public synchronized boolean liberarProceso(Pcb proceso) {
        if (proceso == null) return false;

        boolean eliminado = procesosEnMemoria.eliminar(proceso);
        if (eliminado) {
            memoriaUsada -= proceso.getMemoriaAsignada();
            if (memoriaUsada < 0) memoriaUsada = 0;
            return true;
        }
        return false;
    }

    public synchronized int getMemoriaLibre() {
        return capacidadTotal - memoriaUsada;
    }

    public int getCapacidadTotal() {
        return capacidadTotal;
    }

    public int getMemoriaUsada() {
        return memoriaUsada;
    }

    public ListaEnlazada<Pcb> getProcesosEnMemoria() {
        return procesosEnMemoria;
    }
}
