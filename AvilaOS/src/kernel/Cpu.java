/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kernel;

import enums.EstadoProceso;

/**
 * Representa la CPU de un computador en ÁvilaOS, encargada de ejecutar
 * los ciclos de las instrucciones de los procesos de manera lineal.
 * 
 * @author
 */
public class Cpu {
    private Pcb procesoEnEjecucion;

    public Cpu() {
        this.procesoEnEjecucion = null;
    }
    
    /**
     * Ejecuta un ciclo de reloj sobre el proceso actual en la CPU.
     * Incrementa el PC y el MAR linealmente y descuenta el tiempo restante[cite: 1].
     */
    public void ejecutarCiclo() {
        if (procesoEnEjecucion != null && procesoEnEjecucion.getEstado() == EstadoProceso.RUNNING) {
            procesoEnEjecucion.incrementarPC();
            procesoEnEjecucion.decrementarTiempoEjecucion();
            if (procesoEnEjecucion.getTiempoRestanteEjecucion() <= 0) {
                procesoEnEjecucion.setEstado(EstadoProceso.EXIT);
                procesoEnEjecucion = null;
            }
        }
    }

    public Pcb getProcesoEnEjecucion() {
        return procesoEnEjecucion;
    }

    public void setProcesoEnEjecucion(Pcb procesoEnEjecucion) {
        this.procesoEnEjecucion = procesoEnEjecucion;
        if (procesoEnEjecucion != null) {
            procesoEnEjecucion.setEstado(EstadoProceso.RUNNING);
        }
    }

    public boolean estaOcupada() {
        return procesoEnEjecucion != null;
    }
}
