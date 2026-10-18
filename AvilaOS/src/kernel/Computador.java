/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kernel;

/**
 *
 * @author Antho
 */
import estructuras.Cola;

public class Computador {
    private final String id;
    private final MemoriaRAM memoriaRAM;
    private final Cola<Pcb> colaListos;
    private final Cola<Pcb> colaBloqueados;

    public Computador(String id, int capacidadRAM) {
        this.id = id;
        this.memoriaRAM = new MemoriaRAM(capacidadRAM);
        this.colaListos = new Cola<>();
        this.colaBloqueados = new Cola<>();
    }

    public String getId() {
        return id;
    }

    public MemoriaRAM getMemoriaRAM() {
        return memoriaRAM;
    }

    public Cola<Pcb> getColaListos() {
        return colaListos;
    }

    public Cola<Pcb> getColaBloqueados() {
        return colaBloqueados;
    }
}