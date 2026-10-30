/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kernel;

import enums.EstadoProceso;

public class Semaforo {
    private int valor;
    private NodoPcb frenteCola;
    private NodoPcb finCola;

    // Costo estándar por ejecutar la primitiva en Modo Sistema Operativo
    private static final int COSTO_INSTRUCCION_SO = 1; 

    public Semaforo(int valorInicial) {
        this.valor = valorInicial;
        this.frenteCola = null;
        this.finCola = null;
    }

    public synchronized boolean semWait(Pcb proceso, String motivo) {
        // Registrar costo de ejecución en Modo SO (Overhead de kernel)
        if (proceso != null) {
            proceso.incrementarTiempoModoSO(COSTO_INSTRUCCION_SO);
        }

        valor--;
        if (valor < 0) {
            // Asignar el estado explícito de bloqueo según el contexto o fallback a BLOCKED
            EstadoProceso estadoBloqueo = MapeadorMotivo.obtenerEstadoPorMotivo(motivo);
            proceso.setEstado(estadoBloqueo);
            proceso.setMotivoBloqueo(motivo);
            
            // Encolar en la cola interna del semáforo
            NodoPcb nuevoNodo = new NodoPcb(proceso);
            if (finCola == null) {
                frenteCola = nuevoNodo;
                finCola = nuevoNodo;
            } else {
                finCola.siguiente = nuevoNodo;
                finCola = nuevoNodo;
            }
            return false; // Proceso bloqueado
        }
        return true; // Acceso concedido
    }

    public synchronized Pcb semSignal(Pcb procesoActual) {
        // Registrar costo de ejecución en Modo SO para el proceso que invoca el Signal
        if (procesoActual != null) {
            procesoActual.incrementarTiempoModoSO(COSTO_INSTRUCCION_SO);
        }

        valor++;
        if (valor <= 0 && frenteCola != null) {
            // Desencolar al primer proceso de la lista
            NodoPcb nodoDespertar = frenteCola;
            frenteCola = frenteCola.siguiente;
            if (frenteCola == null) {
                finCola = null;
            }
            
            Pcb procesoDespertado = nodoDespertar.pcb;
            procesoDespertado.setEstado(EstadoProceso.READY);
            procesoDespertado.setMotivoBloqueo("");
            return procesoDespertado;
        }
        return null;
    }

    public int getValor() {
        return valor;
    }

    // Helper interno para mapear la cadena del motivo al Enum correspondiente
    private static class MapeadorMotivo {
        public static EstadoProceso obtenerEstadoPorMotivo(String motivo) {
            if (motivo == null) return EstadoProceso.BLOCKED;
            
            switch (motivo.toUpperCase()) {
                case "MUTEX":
                case "EXCLUSION_MUTUA":
                    return EstadoProceso.BLOQUEADO_MUTEX;
                case "BUFFER_LLENO":
                case "PRODUCTOR":
                    return EstadoProceso.BLOQUEADO_PRODUCTOR_LLENO;
                case "BUFFER_VACIO":
                case "CONSUMIDOR":
                    return EstadoProceso.BLOQUEADO_CONSUMIDOR_VACIO;
                default:
                    return EstadoProceso.BLOCKED;
            }
        }
    }
}
