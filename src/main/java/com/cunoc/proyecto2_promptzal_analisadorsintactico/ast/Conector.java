package com.cunoc.proyecto2_promptzal_analisadorsintactico.ast;

public class Conector {
    private final String nombre; 
    private final Expresion argumento; 
    private final int fila; 
    
    public Conector(String nombre, Expresion argumento, int fila){
        this.nombre = nombre;
        this.argumento = argumento; 
        this.fila = fila;
    }
    public String getNombre(){
        return nombre;
    }
    public Expresion getArgumento(){
        return argumento;
    }
    public int getFila(){
        return fila;
    }
}
