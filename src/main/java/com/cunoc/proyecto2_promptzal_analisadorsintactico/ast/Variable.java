/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cunoc.proyecto2_promptzal_analisadorsintactico.ast;

/**
 *
 * @author jppax
 */
public class Variable {
    
    private final String nombre;
    private final Expresion valor;
    private final int fila; 
    
    public Variable(String nombre, Expresion valor, int fila){
        this.nombre = nombre; 
        this.valor = valor; 
        this.fila = fila;
    }
    public String getNombre(){return nombre;}
    public Expresion getValor(){return valor;}
    public int getFila(){return fila;}
}
