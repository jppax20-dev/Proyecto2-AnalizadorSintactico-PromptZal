/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cunoc.proyecto2_promptzal_analisadorsintactico.semantico;

import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.Agente;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.Comando;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.Conector;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.Expresion;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.Programa;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.Referencia;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.Termino;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.TerminoId;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.Variable;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.errores.ErrorCompilacion;
import java.util.List;
/**
 *
 * @author jppax
 */
public class AnalizadorSemantico {
    private static final String AMBITO_GLOBAL = "global";

    private final List<ErrorCompilacion> errores;
    private final TablaSimbolos tabla = new TablaSimbolos();

    public AnalizadorSemantico(List<ErrorCompilacion> errores) {
        this.errores = errores;
    }

    public TablaSimbolos analizar(Programa programa) {
        for (Agente agente : programa.getAgentes()) {
            analizarAgente(agente);
        }
        validarEjecuciones(programa);
        validarExportaciones(programa);
        return tabla;
    }

    private void analizarAgente(Agente agente) {
        String nombre = agente.getNombre();
        if (nombre == null) return;
        if (tabla.existeAgente(nombre)) {
            errores.add(ErrorCompilacion.semantico("Agente duplicado: '" + nombre + "'", agente.getFila()));
            return;
        }
        tabla.agregar(new Simbolo(nombre, TipoSimbolo.AGENTE, AMBITO_GLOBAL, agente.getFila()));

        for (Variable v : agente.getVariables()) {
            validarExpresion(v.getValor(), nombre, false);
            declarar(nombre, v.getNombre(), TipoSimbolo.VARIABLE_DECLARADA, v.getFila());
        }
        for (Comando c : agente.getComandos()) {
            validarExpresion(c.getArgumento(), nombre, c.getNombre().equals("GENERAR"));
            for (Conector conector : c.getConectores()) {
                validarExpresion(conector.getArgumento(), nombre, false);
            }
            declarar(nombre, c.getDestino(), TipoSimbolo.RESULTADO_COMANDO, c.getFilaDestino());
        }
    }

    private void declarar(String ambito, String nombre, TipoSimbolo tipo, int fila) {
        if (!tabla.existeVariable(ambito, nombre)) {
            tabla.agregar(new Simbolo(nombre, tipo, ambito, fila));
        }
    }

    private void validarExpresion(Expresion expresion, String ambito, boolean omitirPrimero) {
        List<Termino> terminos = expresion.getTerminos();
        for (int i = omitirPrimero ? 1 : 0; i < terminos.size(); i++) {
            if (terminos.get(i) instanceof TerminoId id && !tabla.existeVariable(ambito, id.getNombre())) {
                errores.add(ErrorCompilacion.semantico("Identificador '" + id.getNombre()
                        + "' usado sin declarar en el agente '" + ambito + "'", id.getFila()));
            }
        }
    }

    private void validarEjecuciones(Programa programa) {
        for (Referencia r : programa.getEjecuciones()) {
            if (!tabla.existeAgente(r.getNombre())) {
                errores.add(ErrorCompilacion.semantico(
                        "EJECUTAR hace referencia a un agente inexistente: '" + r.getNombre() + "'", r.getFila()));
            }
        }
    }

    private void validarExportaciones(Programa programa) {
        for (Referencia r : programa.getExportaciones()) {
            boolean existe = false;
            for (Referencia ejecucion : programa.getEjecuciones()) {
                if (tabla.existeVariable(ejecucion.getNombre(), r.getNombre())) {
                    existe = true;
                    break;
                }
            }
            if (!existe) {
                errores.add(ErrorCompilacion.semantico("EXPORTAR hace referencia a '" + r.getNombre()
                        + "', que no existe en ningún agente ejecutado", r.getFila()));
            }
        }
    }
}
