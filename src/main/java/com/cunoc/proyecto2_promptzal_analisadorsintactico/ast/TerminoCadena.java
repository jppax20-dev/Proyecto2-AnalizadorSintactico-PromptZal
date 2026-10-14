/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cunoc.proyecto2_promptzal_analisadorsintactico.ast;

/**
 *
 * @author jppax
 */
public class TerminoCadena extends Termino {
    
    private final String valor; 
    
    public TerminoCadena(String valor, int fila) {
        super(fila);
        this.valor = valor; 
    }
    
    public String getValor (){
        return valor; 
    }
}
