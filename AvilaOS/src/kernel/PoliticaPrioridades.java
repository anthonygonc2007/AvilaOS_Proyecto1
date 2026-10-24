/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kernel;

import estructuras.Cola;
import interfaces.Planificador;
import enums.EstadoProceso;

/**
 * Implementación de la política de Planificación por Prioridades Apropiativas.
 * Si entra un proceso con mayor prioridad (número menor o mayor según convención del equipo),
 * se desalojará al proceso en ejecución actual.
 * 
 * @author 
 */
public class PoliticaPrioridades implements Planificador {

    @Override
    public void planificar(Cola<Pcb> colaListos, Cpu cpu) {
        if (colaListos == null || colaListos.esVacia()) return;

        reordenarPorPrioridad(colaListos);

        if (!cpu.estaOcupada()) {
            Pcb siguiente = colaListos.desencolar();
            if (siguiente != null) {
                siguiente.setEstado(EstadoProceso.RUNNING);
                cpu.setProcesoEnEjecucion(siguiente);
            }
        } else {
            // Lógica apropiativa: evaluar si el primero de la colaListos tiene mayor prioridad que el actual en CPU
            Pcb actual = cpu.getProcesoEnEjecucion();
            // Implementar comparación de prioridades y desalojo si aplica
        }
    }

    @Override
    public void verificarDeadlines(Cola<Pcb> colaListos) {
        // Verificar deadlines en 0
    }

    /**
     * Reordena la cola de listos según el nivel de prioridad del PCB.
     * @param colaListos La cola de listos a ordenar.
     */
    private void reordenarPorPrioridad(Cola<Pcb> colaListos) {
        // Implementación con listas o nodos propios
    }
}
