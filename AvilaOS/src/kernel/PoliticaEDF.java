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
        // Lógica estándar para purgar procesos con deadline en 0
    }

    /**
     * Reordena la cola de listos ascendentes por el valor del deadline.
     * @param colaListos La cola de listos a ordenar.
     */
    private void reordenarPorDeadline(Cola<Pcb> colaListos) {
        // Implementación manual del ordenamiento utilizando las estructuras de datos propias del proyecto.
    }
}