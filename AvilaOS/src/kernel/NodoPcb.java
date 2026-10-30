/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kernel;

/**
 * Nodo propio para la cola del semáforo
 * @author 
 */

class NodoPcb {
    Pcb pcb;
    NodoPcb siguiente;

    public NodoPcb(Pcb pcb) {
        this.pcb = pcb;
        this.siguiente = null;
    }
}