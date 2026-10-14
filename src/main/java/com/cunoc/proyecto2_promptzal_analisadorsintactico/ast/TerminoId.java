/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cunoc.proyecto2_promptzal_analisadorsintactico.ast;

/**
 *
 * @author jppax
 */
public class TerminoId extends Termino {
    
    private final String nombre;
    
    public TerminoId(String nombre, int fila) {
        super(fila);
        this.nombre = nombre;
    }
    
    public String getNombre(){
        return nombre;
    }
}
