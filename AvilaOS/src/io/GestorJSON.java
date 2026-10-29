/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 
 * @author Usuario
 */
public class GestorJSON {

    private final Gson gson;

    public GestorJSON() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Lee un archivo JSON y retorna la configuración cargada.
     * @param rutaArchivo
     * @return
     * @throws IOException 
     */
    public ConfiguracionSimulacion cargarConfiguracion(String rutaArchivo) throws IOException {
        try (FileReader lector = new FileReader(rutaArchivo)) {
            return gson.fromJson(lector, ConfiguracionSimulacion.class);
        }
    }
    
    /**
     * Guarda el objeto de configuración en un archivo JSON
     * @param config
     * @param rutaArchivo
     * @throws IOException 
     */
    public void guardarConfiguracion(ConfiguracionSimulacion config, String rutaArchivo) throws IOException {
        try (FileWriter escritor = new FileWriter(rutaArchivo)) {
            gson.toJson(config, escritor);
        }
    }
}
