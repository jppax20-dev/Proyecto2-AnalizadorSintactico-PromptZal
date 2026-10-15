/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cunoc.proyecto2_promptzal_analisadorsintactico.semantico;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author jppax
 */
public class TablaSimbolos {
    private final List<Simbolo> simbolos = new ArrayList<>();

    public void agregar(Simbolo simbolo) {
        simbolos.add(simbolo);
    }

    public boolean existeAgente(String nombre) {
        for (Simbolo s : simbolos) {
            if (s.getTipo() == TipoSimbolo.AGENTE && s.getNombre().equals(nombre)) return true;
        }
        return false;
    }

    public boolean existeVariable(String ambito, String nombre) {
        for (Simbolo s : simbolos) {
            if (s.getTipo() != TipoSimbolo.AGENTE
                    && s.getAmbito().equals(ambito) && s.getNombre().equals(nombre)) return true;
        }
        return false;
    }

    public List<Simbolo> getSimbolos() { return simbolos; }
}
