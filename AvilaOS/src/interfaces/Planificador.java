/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package interfaces;

import estructuras.Cola;
import kernel.Cpu;
import kernel.Pcb;

/**
 * Interfaz base para los algoritmos de planificación.
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
    
    
    Pcb seleccionarSiguienteProceso(Cola<Pcb> colaListos);
}