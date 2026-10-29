/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kernel;

import enums.EstadoProceso;
import enums.EstadoProceso;
import enums.ModoEjecucion;

/**
 * Representa la CPU de un computador en ÁvilaOS, encargada de ejecutar
 * los ciclos de las instrucciones de los procesos de manera lineal.
 * 
 * @author
 */
public class Cpu {
    private Pcb procesoEnEjecucion;
    private ModoEjecucion modo;

    public Cpu() {
        this.procesoEnEjecucion = null;
        this.modo = ModoEjecucion.USUARIO;
    }

    public void ejecutarCiclo() {
        if (procesoEnEjecucion != null && procesoEnEjecucion.getEstado() == EstadoProceso.RUNNING) {
            // Si el proceso consume llamadas del Kernel, cambia a Modo SO
            if (procesoEnEjecucion.getTiempoModoSO() > 0) {
                this.modo = ModoEjecucion.SISTEMA_OPERATIVO;
            } else {
                this.modo = ModoEjecucion.USUARIO;
            }

            procesoEnEjecucion.incrementarPC();
            procesoEnEjecucion.decrementarTiempoEjecucion();

            if (procesoEnEjecucion.getTiempoRestanteEjecucion() <= 0) {
                procesoEnEjecucion.setEstado(EstadoProceso.EXIT);
                procesoEnEjecucion = null;
                this.modo = ModoEjecucion.USUARIO;
            }
        } else {
            this.modo = ModoEjecucion.USUARIO;
        }
    }

    public Pcb getProcesoEnEjecucion() { return procesoEnEjecucion; }

    public void setProcesoEnEjecucion(Pcb procesoEnEjecucion) {
        this.procesoEnEjecucion = procesoEnEjecucion;
        if (procesoEnEjecucion != null) {
            procesoEnEjecucion.setEstado(EstadoProceso.RUNNING);
        }
    }

    public boolean estaOcupada() { return procesoEnEjecucion != null; }

    public ModoEjecucion getModo() { return modo; }

    public void setModo(ModoEjecucion modo) { this.modo = modo; }
}
