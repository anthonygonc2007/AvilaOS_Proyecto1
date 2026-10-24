/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kernel;

import estructuras.Cola;

/**
 *
 * @author Antho
 */
public class Computador {
    private final String id;
    private final MemoriaRAM memoriaRAM;
    private final Cpu cpu;
    private final Cola<Pcb> colaListos;
    private final Cola<Pcb> colaBloqueados;

    public Computador(String id, int capacidadRAM) {
        this.id = id;
        this.memoriaRAM = new MemoriaRAM(capacidadRAM);
        this.cpu = new Cpu();
        this.colaListos = new Cola<>();
        this.colaBloqueados = new Cola<>();
    }

    public String getId() {
        return id;
    }

    public MemoriaRAM getMemoriaRAM() {
        return memoriaRAM;
    }

    public Cpu getCpu() {
        return cpu;
    }

    public Cola<Pcb> getColaListos() {
        return colaListos;
    }

    public Cola<Pcb> getColaBloqueados() {
        return colaBloqueados;
    }

    /**
     * Intenta ingresar un proceso a la cola de listos validando la regla de no migración.
     * @param proceso El Pcb a ingresar.
     * @return true si fue admitido, false si fue rechazado por violar la regla.
     */
    public boolean admitirProceso(Pcb proceso) {
        // Validar la regla de no migración: un proceso asignado a un computador no cambia de máquina.
        if (!this.id.equals(proceso.getComputadorId())) {
            System.out.println("ALERTA DE SEGURIDAD: Intento de migración bloqueado. " +
                               "El proceso " + proceso.getId() + 
                               " pertenece a " + proceso.getComputadorId() + 
                               " y no puede ingresar a " + this.id);
            return false;
        }
        
        // Si el ID coincide, se respeta la regla y entra a la cola
        this.colaListos.encolar(proceso);
        proceso.setEstado(enums.EstadoProceso.READY);
        return true;
    }
}