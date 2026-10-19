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
    private final int columna;
    
    public TerminoId(String nombre, int fila, int columna) {
        super(fila);
        this.nombre = nombre;
        this.columna = columna;
    }
    
    public String getNombre(){return nombre;}
    public int getColumna(){return columna;}
}
