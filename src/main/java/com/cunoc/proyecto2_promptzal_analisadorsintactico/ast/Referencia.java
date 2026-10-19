/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cunoc.proyecto2_promptzal_analisadorsintactico.ast;

/**
 *
 * @author jppax
 */
public class Referencia {
    
    private final String nombre;
    private final int fila;
    private final int columna;
    
    public Referencia(String nombre, int fila, int columna){
        this.nombre = nombre;
        this.fila = fila; 
        this.columna = columna;
    }
    
    public String getNombre(){return nombre; }
    public int getFila(){return fila; }
    public int getColuma(){return columna; }
}

