/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
*/
package kernel;

import estructuras.Cola;
import interfaces.Planificador;
import enums.EstadoProceso;

/**
 * Implementación de la política EDF (Earliest Deadline First).
 * Da prioridad a aquellos procesos cuyo tiempo límite (deadline) esté más próximo a vencerse.
 * 
 * @author
 */
public class PoliticaEDF implements Planificador {

    @Override
    public void planificar(Cola<Pcb> colaListos, Cpu cpu) {
        if (cpu == null || !cpu.estaOcupada()) {
            if (colaListos != null && !colaListos.esVacia()) {
                reordenarPorDeadline(colaListos);
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

        Cola<Pcb> colaAuxiliar = new Cola<>();
        while (!colaListos.esVacia()) {
            Pcb proceso = colaListos.desencolar();
            if (proceso != null) {
                proceso.decrementarDeadline();
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

    /**
     * Reordena la cola de listos ascendentes por el valor del deadline (Bubble Sort).
     * @param colaListos La cola de listos a ordenar.
     */
    private void reordenarPorDeadline(Cola<Pcb> colaListos) {
    if (colaListos == null || colaListos.esVacia()) return;
        Cola<Pcb> colaTemporal = new Cola<>();
        int tamaño = 0;
        
        while (!colaListos.esVacia()) {
            colaTemporal.encolar(colaListos.desencolar());
            tamaño++;
        }
        Pcb[] arregloProcesos = new Pcb[tamaño];
        for (int i = 0; i < tamaño; i++) {
            arregloProcesos[i] = colaTemporal.desencolar();
        }
        for (int i = 0; i < tamaño - 1; i++) {
            for (int j = 0; j < tamaño - i - 1; j++) {
                if (arregloProcesos[j].getDeadline() > arregloProcesos[j + 1].getDeadline()) {
                    Pcb temp = arregloProcesos[j];
                    arregloProcesos[j] = arregloProcesos[j + 1];
                    arregloProcesos[j + 1] = temp;
                }
            }
        }
        for (int i = 0; i < tamaño; i++) {
            colaListos.encolar(arregloProcesos[i]);
        }
    }
}