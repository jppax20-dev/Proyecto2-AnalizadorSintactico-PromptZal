package com.cunoc.proyecto2_promptzal_analisadorsintactico.semantico;
import java.util.ArrayList;
import java.util.List;

public class TablaSimbolos {
    // Lista principal donde se guarda todo el inventario de la memoria
    private final List<Simbolo> simbolos = new ArrayList<>();

    public void agregar(Simbolo simbolo) {
        simbolos.add(simbolo);
    }

    // Verifica si un agente especifico ya fue creado previamente
    public boolean existeAgente(String nombre) {
        for (Simbolo s : simbolos) {
            if (s.getTipo() == TipoSimbolo.AGENTE && s.getNombre().equals(nombre)) return true;
        }
        return false;
    }

        // Busca una variable validando que pertenezca al agente correcto
    public boolean existeVariable(String ambito, String nombre) {
        for (Simbolo s : simbolos) {
            if (s.getTipo() != TipoSimbolo.AGENTE
                    && s.getAmbito().equals(ambito) && s.getNombre().equals(nombre)) return true;
        }
        return false;
    }

    public List<Simbolo> getSimbolos() { return simbolos; }
}
