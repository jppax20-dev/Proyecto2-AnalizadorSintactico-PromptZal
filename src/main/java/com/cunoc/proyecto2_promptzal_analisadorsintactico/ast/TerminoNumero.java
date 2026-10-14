/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cunoc.proyecto2_promptzal_analisadorsintactico.ast;

/**
 *
 * @author jppax
 */
public class TerminoNumero extends Termino {
    
    private final String valor;
    private final String unidad; 
    
    public TerminoNumero(String valor, String unidad, int fila) {
        super(fila);
        this.valor = valor; 
        this.unidad = unidad;
        
    }
    public String getValor(){
        return valor;
    }
    public String getUnidad(){
        return unidad;
    }
}
