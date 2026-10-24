/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kernel;

/**
 *
 * @author Gemelos
 */
public class Semaforo {
private int valor;
    private NodoPcb frenteCola;
    private NodoPcb finCola;

    public Semaforo(int valorInicial) {
        this.valor = valorInicial;
        this.frenteCola = null;
        this.finCola = null;
    }

    public synchronized boolean semWait(Pcb proceso, String motivo) {
        valor--;
        if (valor < 0) {
            // El recurso no está disponible; bloqueamos el proceso
            proceso.setEstado(enums.EstadoProceso.BLOCKED);
            proceso.setMotivoBloqueo("Esperando en Semáforo");
            
            // Encolar usando nuestra propia lista enlazada
            NodoPcb nuevoNodo = new NodoPcb(proceso);
            if (finCola == null) {
                frenteCola = nuevoNodo;
                finCola = nuevoNodo;
            } else {
                finCola.siguiente = nuevoNodo;
                finCola = nuevoNodo;
            }
            return false; // Bloqueado
        }
        return true; // Puede continuar
    }

    public synchronized Pcb semSignal() {
        valor++;
        if (valor <= 0 && frenteCola != null) {
            // Desencolar al primer proceso bloqueado
            NodoPcb nodoDespertar = frenteCola;
            frenteCola = frenteCola.siguiente;
            if (frenteCola == null) {
                finCola = null;
            }
            
            Pcb procesoDespertado = nodoDespertar.pcb;
            procesoDespertado.setEstado(enums.EstadoProceso.READY);
            procesoDespertado.setMotivoBloqueo("");
            return procesoDespertado;
        }
        return null;
    }
}
