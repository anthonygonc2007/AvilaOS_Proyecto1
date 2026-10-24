/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package enums;

/**
 *
 * @author Gemelos
 */
public enum ProcesoTipo {
    CPU_BOUND,   // Muy consumidor de procesador
    IO_BOUND,    // Muy consumidor de E/S
    PRODUCTOR,   // Produce elementos para un buffer
    CONSUMIDOR   // Consume elementos de un buffer
}
