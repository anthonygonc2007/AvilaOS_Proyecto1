/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core;

/**
 *
 * @author Antho
 */
public class RelojGlobal {
    private static int cicloActual = 0;

    // Incrementa el reloj del sistema en exactamente 1 ciclo
    public static synchronized void avanzarCiclo() {
        cicloActual++;
    }

    public static synchronized int getCicloActual() {
        return cicloActual;
    }

    // Permite reiniciar el reloj si se reinicia la simulación
    public static synchronized void reiniciar() {
        cicloActual = 0;
    }
}