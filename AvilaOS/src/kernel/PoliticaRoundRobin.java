/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kernel;

import estructuras.Cola;
import interfaces.Planificador;
import enums.EstadoProceso;

/**
 * Implementación de la política Round Robin con quantum de tiempo configurable.
 * 
 * @author
 */
public class PoliticaRoundRobin implements Planificador {
    private final int quantum;
    private int ticksActuales;

    /**
     * Constructor para Round Robin.
     * @param quantum Número máximo de ciclos permitidos en CPU por turno.
     */
    public PoliticaRoundRobin(int quantum) {
        this.quantum = quantum;
        this.ticksActuales = 0;
    }

    @Override
    public void planificar(Cola<Pcb> colaListos, Cpu cpu) {
        if (cpu == null) return;

        // Si la CPU está libre, tomamos el siguiente de la cola
        if (!cpu.estaOcupada()) {
            ticksActuales = 0;
            if (colaListos != null && !colaListos.esVacia()) {
                Pcb siguiente = colaListos.desencolar();
                if (siguiente != null) {
                    siguiente.setEstado(EstadoProceso.RUNNING);
                    cpu.setProcesoEnEjecucion(siguiente);
                }
            }
        } else {
            // Si hay un proceso corriendo, gestionamos el quantum
            ticksActuales++;
            if (ticksActuales >= quantum) {
                Pcb actual = cpu.getProcesoEnEjecucion();
                if (actual != null && actual.getEstado() == EstadoProceso.RUNNING) {
                    actual.setEstado(EstadoProceso.READY);
                    cpu.setProcesoEnEjecucion(null);
                    colaListos.encolar(actual); // Vuelve al final de la cola de listos
                }
                ticksActuales = 0;
            }
        }
    }

    @Override
    public void verificarDeadlines(Cola<Pcb> colaListos) {
        if (colaListos == null || colaListos.esVacia()) return;

        Cola<Pcb> colaAuxiliar = new Cola<>();
        while (!colaListos.esVacia()) {
            Pcb proceso = colaListos.desencolar();
            if (proceso != null) {
                //proceso.decrementarDeadline();
                if (proceso.getDeadline() <= 0) {
                    proceso.setEstado(EstadoProceso.EXIT);
                } else {
                    colaAuxiliar.encolar(proceso);
                }
            }
        }
        while (!colaAuxiliar.esVacia()) {
            colaListos.encolar(colaAuxiliar.desencolar());
        }
    }
}
