/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cunoc.proyecto2_promptzal_analisadorsintactico.semantico;

/**
 *
 * @author jppax
 */
public class Simbolo {
    
    private final String nombre;
    private final TipoSimbolo tipo;
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
