/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package avilaos;

import gui.VistaConfiguracion;
import javax.swing.SwingUtilities;
/**
 *
 * @author Antho
 */
public class AvilaOS {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
        VistaConfiguracion vistaInicial = new VistaConfiguracion();
        vistaInicial.setLocationRelativeTo(null); // Centrar la ventana en pantalla
        vistaInicial.setVisible(true);            // Hacer visible la Vista 1
        // TODO code application logic here
        });
    }
}
