/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cunoc.proyecto2_promptzal_analisadorsintactico.ast;

/**
 *
 * @author jppax
 */
public abstract class Termino {
    
    private final int fila;
    
    protected Termino(int fila){
        this.fila = fila;
    }
    public int getFila() {
        return fila;
    }
}
