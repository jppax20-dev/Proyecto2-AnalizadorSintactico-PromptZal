/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cunoc.proyecto2_promptzal_analisadorsintactico.ast;

import java.util.List;
/**
 *
 * @author jppax
 */
public class Expresion {
    
    private final List<Termino> terminos;
    
    public Expresion(List<Termino> terminos){
        this.terminos = terminos; 
    }
    
    public List<Termino> getTerminos(){
        return terminos;
    }
}
