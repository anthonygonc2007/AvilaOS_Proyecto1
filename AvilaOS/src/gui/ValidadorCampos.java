/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
/**
 *
 * @author Usuario
 */
public class ValidadorCampos {
    /**
     * Valida si un campo contiene un numero entero dentro del rango especificado
     * @param campo
     * @param nombreCampo
     * @param min
     * @param max
     * @return 
     */
    public static boolean validarEnteroRango(JTextField campo, String nombreCampo, int min, int max) {
        String texto = campo.getText().trim();
        
        if (texto.isEmpty()) {
            mostrarError("El campo '" + nombreCampo + "' no puede estar vacío.");
            campo.requestFocus();
            return false;
        }

        try {
            int valor = Integer.parseInt(texto);
            if (valor < min || valor > max) {
                mostrarError("El campo '" + nombreCampo + "' debe ser un número entero entre " + min + " y " + max + ".");
                campo.requestFocus();
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            mostrarError("El campo '" + nombreCampo + "' debe ser un valor numérico entero válido.");
            campo.requestFocus();
            return false;
        }
    }

    /**
     * Valida que un campo de texto no este vacio o que no tenga solo espacios
     * @param campo
     * @param nombreCampo
     * @return 
     */
    public static boolean validarTextoNoVacio(JTextField campo, String nombreCampo) {
        String texto = campo.getText().trim();
        if (texto.isEmpty()) {
            mostrarError("El campo '" + nombreCampo + "' es obligatorio.");
            campo.requestFocus();
            return false;
        }
        return true;
    }
    
    /**
     * Muestra un mensaje de error
     * @param mensaje 
     */
    private static void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(
            null, 
            mensaje, 
            "Error de Validación", 
            JOptionPane.ERROR_MESSAGE
        );
    }
}

