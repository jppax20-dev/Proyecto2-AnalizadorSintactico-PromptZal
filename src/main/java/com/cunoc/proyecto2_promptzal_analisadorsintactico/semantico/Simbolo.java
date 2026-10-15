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
    private final String ambito;
    private final TipoSimbolo tipo;
    private final int fila;
    
    public Simbolo(String nombre, String ambito, TipoSimbolo tipo, int fila){
        this.nombre = nombre;
        this.ambito = ambito;
        this.tipo = tipo;
        this.fila = fila;
    }
    
    public String getNombre(){return nombre; }
    public String getAmbito(){return ambito; }
    public TipoSimbolo getTipo(){return tipo; }
    public int getFila(){return fila; }
}
