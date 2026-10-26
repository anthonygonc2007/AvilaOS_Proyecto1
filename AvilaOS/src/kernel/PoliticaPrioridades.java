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
     * Reordena la cola de listos según el nivel de prioridad del PCB.
     * @param colaListos La cola de listos a ordenar.
     */
    private void reordenarPorPrioridad(Cola<Pcb> colaListos) {
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

        // Ordenamiento por burbuja basado en la prioridad (ej: menor valor = mayor prioridad)
        for (int i = 0; i < tamaño - 1; i++) {
            for (int j = 0; j < tamaño - i - 1; j++) {
                if (arregloProcesos[j].getPrioridad() > arregloProcesos[j + 1].getPrioridad()) {
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
