/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kernel;

import estructuras.Cola;
import interfaces.Planificador;
import enums.EstadoProceso;

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
            // Desalojo apropiativo: Menor entero representa Mayor Prioridad
            Pcb actual = cpu.getProcesoEnEjecucion();
            Pcb primeroListo = colaListos.verFrente();

            if (actual != null && primeroListo != null) {
                if (primeroListo.getPrioridad() < actual.getPrioridad()) {
                    // Desalojar proceso actual
                    actual.setEstado(EstadoProceso.READY);
                    colaListos.encolar(actual);

                    Pcb nuevo = colaListos.desencolar();
                    nuevo.setEstado(EstadoProceso.RUNNING);
                    cpu.setProcesoEnEjecucion(nuevo);
                }
            }
        }
    }

    @Override
    public void verificarDeadlines(Cola<Pcb> colaListos) {
        if (colaListos == null || colaListos.esVacia()) return;

        Cola<Pcb> colaAuxiliar = new Cola<>();
        while (!colaListos.esVacia()) {
            Pcb proceso = colaListos.desencolar();
            if (proceso != null) {
                //proceso.decrementarDeadline();
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

    private void reordenarPorPrioridad(Cola<Pcb> colaListos) {
        if (colaListos == null || colaListos.esVacia()) return;

        Cola<Pcb> colaTemporal = new Cola<>();
        int tamano = 0;

        while (!colaListos.esVacia()) {
            colaTemporal.encolar(colaListos.desencolar());
            tamano++;
        }

        Pcb[] arregloProcesos = new Pcb[tamano];
        for (int i = 0; i < tamano; i++) {
            arregloProcesos[i] = colaTemporal.desencolar();
        }

        for (int i = 0; i < tamano - 1; i++) {
            for (int j = 0; j < tamano - i - 1; j++) {
                if (arregloProcesos[j].getPrioridad() > arregloProcesos[j + 1].getPrioridad()) {
                    Pcb temp = arregloProcesos[j];
                    arregloProcesos[j] = arregloProcesos[j + 1];
                    arregloProcesos[j + 1] = temp;
                }
            }
        }

        for (int i = 0; i < tamano; i++) {
            colaListos.encolar(arregloProcesos[i]);
        }
    }
}