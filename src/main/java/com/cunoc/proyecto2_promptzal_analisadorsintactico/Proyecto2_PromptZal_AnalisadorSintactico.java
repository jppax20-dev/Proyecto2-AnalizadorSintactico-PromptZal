package com.cunoc.proyecto2_promptzal_analisadorsintactico;

//Proyecto 2 Lenguajes SS 2026
import com.cunoc.proyecto2_promptzal_analisadorsintactico.vista.VentanaPrincipal;
import java.awt.EventQueue;

public class Proyecto2_PromptZal_AnalisadorSintactico {

        public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setLocationRelativeTo(null); 
            ventana.setVisible(true);
        });
    }
}