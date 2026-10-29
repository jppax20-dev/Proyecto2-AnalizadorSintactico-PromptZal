package com.cunoc.proyecto2_promptzal_analisadorsintactico.semantico;

public class Simbolo {
    
    private final String nombre;
    private final TipoSimbolo tipo;
    // Define a que agente pertenece para evitar choques de nombres
    private final String ambito;
    private final int fila;
    
    public Simbolo(String nombre, TipoSimbolo tipo, String ambito, int fila){
        this.nombre = nombre;
        this.tipo = tipo;
        this.ambito = ambito;
        this.fila = fila;
    }
    
    public String getNombre(){return nombre; }
    public TipoSimbolo getTipo(){return tipo; }
    public String getAmbito(){return ambito;}
    public int getFila(){return fila; }
}
