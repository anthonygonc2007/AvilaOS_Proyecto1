/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package estructuras;

/**
 *
 * @author Antho
 */
public class Cola<T> {
    private Nodo<T> cabeza;
    private Nodo<T> cola;
    private int tamano;

    public Cola() {
        this.cabeza = null;
        this.cola = null;
        this.tamano = 0;
    }

    public boolean esVacia() {
        return cabeza == null;
    }

    public void encolar(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (esVacia()) {
            cabeza = nuevo;
            cola = nuevo;
        } else {
            cola.setSiguiente(nuevo);
            cola = nuevo;
        }
        tamano++;
    }

    public T desencolar() {
        if (esVacia()) {
            return null;
        }
        T dato = cabeza.getDato();
        cabeza = cabeza.getSiguiente();
        if (cabeza == null) {
            cola = null;
        }
        tamano--;
        return dato;
    }

    public T verFrente() {
        if (esVacia()) {
            return null;
        }
        return cabeza.getDato();
    }

    public int getTamano() {
        return tamano;
    }
}
