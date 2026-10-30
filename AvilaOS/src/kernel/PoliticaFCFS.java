/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kernel;

import estructuras.Cola;
import interfaces.Planificador;
import enums.EstadoProceso;

/**
 * Implementación de la política de planificación FCFS (First-Come, First-Served).
 * Los procesos se atienden en estricto orden de llegada.
 * 
 * @author
 */
public class PoliticaFCFS implements Planificador {

    @Override
    public void planificar(Cola<Pcb> colaListos, Cpu cpu) {
        if (cpu == null || !cpu.estaOcupada()) {
            if (colaListos != null && !colaListos.esVacia()) {
                Pcb siguiente = colaListos.desencolar();
                if (siguiente != null) {
                    siguiente.setEstado(EstadoProceso.RUNNING);
                    cpu.setProcesoEnEjecucion(siguiente);
                }
            }
        }
    }

    @Override
    public void verificarDeadlines(Cola<Pcb> colaListos) {
        if (colaListos == null || colaListos.esVacia()) return;
        
        // Recorrido o filtrado para verificar deadlines en 0
        Cola<Pcb> colaAuxiliar = new Cola<>();
        while (!colaListos.esVacia()) {
            Pcb proceso = colaListos.desencolar();
            if (proceso != null) {
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