package com.cunoc.proyecto2_promptzal_analisadorsintactico.ast;

// Plantilla base para todos los valores manejados en el ast
public abstract class Termino {
    
    private final int fila;
    
    protected Termino(int fila){ this.fila = fila;}
    // Guarda la linea exacta para ubicar los errores semanticos
    public int getFila() { return fila;}
}
