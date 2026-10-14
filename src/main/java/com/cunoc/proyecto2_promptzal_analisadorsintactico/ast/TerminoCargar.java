/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cunoc.proyecto2_promptzal_analisadorsintactico.ast;

/**
 *
 * @author jppax
 */
public class TerminoCargar extends Termino {
    
    private final String archivo;
    
    public TerminoCargar(String archivo,int fila) {
        super(fila);
        this.archivo = archivo;
    }
    
    public String getArchivo(){
        return archivo;
    }
    
}
