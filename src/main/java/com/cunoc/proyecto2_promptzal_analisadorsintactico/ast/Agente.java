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
public class Agente {
    
    private final String nombre; 
    private final String contexto;
    private final List<Variable> variables;
    private final List<Comando> comandos;
    private final int fila; 
    private final int columna;
    
    public Agente(String nombre, String contexto, List<Variable> variables, List<Comando> comandos, int fila, int columna){
        this.nombre = nombre;
        this.contexto = contexto;
        this.variables = variables;
        this.comandos = comandos;
        this.fila = fila;
        this.columna = columna;
    }
    
    public String getNombre(){return nombre;}
    public String getContexto(){return contexto;}
    public List<Variable> getVariables(){return variables;}
    public List<Comando> getComandos(){return comandos;}
    public int getFila(){return fila; }
    public int getColumna(){return columna; }
}
