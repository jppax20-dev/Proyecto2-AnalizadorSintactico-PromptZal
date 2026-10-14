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
public class Comando {
    
    private final String nombre;
    private final Expresion argumento; 
    private final List<Conector> conectores;
    private final String destino; 
    private final int fila;
    private final int filaDestino;
    
    public Comando(String nombre, Expresion argumento, List<Conector> conectores, String destino, int fila, int filaDestino){
        this.nombre = nombre; 
        this.argumento = argumento; 
        this.conectores = conectores; 
        this.destino = destino; 
        this.fila = fila; 
        this.filaDestino = filaDestino;        
    }
    
    public String getNomnbre(){ return nombre;}
    public Expresion getArgumento(){ return argumento;}
    public List<Conector> getConectores(){return conectores;}
    public String getDestino (){return destino; }
    public int getFila(){return fila;}
    public int getFilaDestino(){return filaDestino;}
}
