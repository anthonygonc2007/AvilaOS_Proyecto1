/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package kernel;

import estructuras.Cola;

/**
 * Interfaz base para los algoritmos de planificación.
 * Persona B: Implementar esta interfaz en clases como PlanificadorFCFS, PlanificadorRR, etc.
 */
public interface Planificador {
    
    /**
     * Define la lógica para elegir el siguiente proceso de la cola y asignarlo a la CPU.
     * @param colaListos La cola de procesos listos del computador.
     * @param cpu La CPU del computador.
     */
    void planificar(Cola<Pcb> colaListos, Cpu cpu);
    
    /**
     * Revisa la cola de listos para terminar inmediatamente los procesos con deadline == 0.
     * @param colaListos La cola de procesos listos a verificar.
     */
    void verificarDeadlines(Cola<Pcb> colaListos);
}