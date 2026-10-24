/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io;


/**
 * Representa la configuración inicial de la simulación
 * @author Usuario
 */
public class ConfiguracionSimulacion {

    // 1. Parámetros Generales del Sistema
    public int duracionCicloMs;
    public int latenciaRedCiclos;
    public int numComputadores;

    // 2. Arreglos de Configuración
    public ConfigComputador[] computadores;
    public ConfigBuffer[] buffers;
    public ConfigProceso[] procesos;
 

    public static class ConfigComputador {
        public int id;
        public int ramTotal;
        public String politicaInicial;
        public int quantum;
    }

    public static class ConfigBuffer {
        public String id;
        public int capacidad;
        public int computadorAnfitrionId;
    }

    public static class ConfigProceso {
        public String id;
        public String nombre;
        public String tipo;
        public int prioridad;
        public int deadline;
        public int memoriaRequerida;
        public int computadorAsignadoId;
        
        // Atributos específicos según tipo
        public int ciclosInstrucciones;    // Para CPU_BOUND / IO_BOUND
        public String bufferId;            // Para PRODUCTOR / CONSUMIDOR
        public int frecuenciaCiclos;
        public int totalElementos;
    }
}
