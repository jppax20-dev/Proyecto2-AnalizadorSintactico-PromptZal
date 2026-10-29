package com.cunoc.proyecto2_promptzal_analisadorsintactico.ast;
import java.util.List;

// Nodo principal que agrupa todas las partes del documento
public class Programa {
    
    // Listas que guardan las cuatro secciones del archivo original
    private final List<Directiva> directivas;
    private final List<Agente> agentes;
    private final List<Referencia> ejecuciones;
    private final List<Referencia> exportaciones;
    
    public Programa(List<Directiva> directivas, List<Agente> agentes, List<Referencia> ejecuciones, 
            List<Referencia> exportaciones){
        this.directivas = directivas;
        this.agentes = agentes;
        this.ejecuciones = ejecuciones;
        this.exportaciones = exportaciones;
    }
    
    public List<Directiva> getDirectivas(){return directivas;}
    public List<Agente> getAgentes(){return agentes;}
    public List<Referencia> getEjecuciones() {return ejecuciones;}
    public List<Referencia> getExportaciones(){return exportaciones;}
}
