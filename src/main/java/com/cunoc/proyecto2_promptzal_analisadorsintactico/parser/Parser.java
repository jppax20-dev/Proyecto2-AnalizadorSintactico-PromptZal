/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cunoc.proyecto2_promptzal_analisadorsintactico.parser;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.errores.ErrorCompilacion;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.lexer.Token;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.lexer.TipoToken;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.Expresion;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.Termino;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.TerminoCadena;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.TerminoCargar;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.TerminoId;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.TerminoNumero;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.Comando;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.Conector;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.Variable;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.Agente;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.Directiva;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.Programa;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.Referencia;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
/**
 *
 * @author jppax
 */
public class Parser {
    
    private static final Set<TipoToken> SINCRONIZACION = EnumSet.of(
            TipoToken.AGENTE, TipoToken.LLAVE_C, TipoToken.COMANDO_IA,
            TipoToken.VARIABLE, TipoToken.EJECUTAR, TipoToken.EXPORTAR, TipoToken.EOF);

    private static class ErrorSintactico extends RuntimeException {
        ErrorSintactico() { super(null, null, false, false); }
    }

    private final List<Token> tokens;
    private final List<ErrorCompilacion> errores;
    private int pos = 0;

    public Parser(List<Token> tokens, List<ErrorCompilacion> errores) {
        this.tokens = tokens;
        this.errores = errores;
    }

    private Token actual() {
        return tokens.get(pos);
    }

    private boolean verifica(TipoToken tipo) {
        return actual().getTipo() == tipo;
    }

    private Token avanzar() {
        Token t = actual();
        if (t.getTipo() != TipoToken.EOF) pos++;
        return t;
    }

    private Token match(TipoToken esperado) {
        if (verifica(esperado)) return avanzar();
        throw error(esperado.name());
    }

    private ErrorSintactico error(String esperado) {
        Token t = actual();
        if (pos != posUltimoError) {
            errores.add(ErrorCompilacion.sintactico(describir(t), esperado, t.getFila(), t.getColumna()));
            posUltimoError = pos;
        }
        return new ErrorSintactico();
    }

    private void sincronizar() {
        while (!SINCRONIZACION.contains(actual().getTipo())) avanzar();
        posUltimoError = pos;
    }
    
    private void sincronizarPrograma() {
        while (!SINCRONIZACION_PROGRAMA.contains(actual().getTipo())) avanzar();
        posUltimoError = pos;
    }
    
    private static final Set<TipoToken> SINCRONIZACION_PROGRAMA = EnumSet.of(
            TipoToken.AGENTE, TipoToken.EJECUTAR, TipoToken.EXPORTAR, TipoToken.EOF);

    private int posUltimoError = -1;

    private String describir(Token t) {
        return t.getTipo() == TipoToken.EOF ? "fin de archivo" : t.getTipo() + " '" + t.getLexema() + "'";
    }
    
    //<expresion> -> <termino> <mas_termino>
    private Expresion expresion() {
        List<Termino> terminos = new ArrayList<>();
        terminos.add(termino());
        masTerminos(terminos);
        return new Expresion(terminos);
    }
    // <mas_terminos> -> MAS <termino> <mas_terminos> | ε
    private void masTerminos(List<Termino> terminos) {
        while (verifica(TipoToken.MAS)) {
            avanzar();
            terminos.add(termino());
        }
    }

    // <termino> -> CADENA | ID | NUMERO <unidad> | <cargar>
    private Termino termino() {
        Token t = actual();
        switch (t.getTipo()) {
            case CADENA:
                avanzar();
                return new TerminoCadena(sinComillas(t.getLexema()), t.getFila());
            case ID:
                avanzar();
                return new TerminoId(t.getLexema(), t.getFila());
            case NUMERO:
                avanzar();
                return new TerminoNumero(t.getLexema(), unidad(), t.getFila());
            case CARGAR:
                return cargar();
            default:
                throw error("CADENA, ID, NUMERO o CARGAR");
        }
    }

    // <unidad> -> ID | ε
    private String unidad() {
        return verifica(TipoToken.ID) ? avanzar().getLexema() : null;
    }

    // <cargar> -> CARGAR PAR_A CADENA PAR_C
    private TerminoCargar cargar() {
        Token inicio = match(TipoToken.CARGAR);
        match(TipoToken.PAR_A);
        Token archivo = match(TipoToken.CADENA);
        match(TipoToken.PAR_C);
        return new TerminoCargar(sinComillas(archivo.getLexema()), inicio.getFila());
    }

    private String sinComillas(String lexema) {
        return lexema.substring(1, lexema.length() - 1);
    }
        // <contexto> -> CONTEXTO IGUAL CADENA
    private String contexto() {
        match(TipoToken.CONTEXTO);
        match(TipoToken.IGUAL);
        Token cadena = match(TipoToken.CADENA);
        return sinComillas(cadena.getLexema());
    }

    // <variables> -> VARIABLE ID IGUAL <expresion> <variables> | ε
    private List<Variable> variables() {
        List<Variable> lista = new ArrayList<>();
        while (verifica(TipoToken.VARIABLE)) {
            try {
                lista.add(variable());
            } catch (ErrorSintactico e) {
                sincronizar();
            }
        }
        return lista;
    }

    private Variable variable() {
        match(TipoToken.VARIABLE);
        Token nombre = match(TipoToken.ID);
        match(TipoToken.IGUAL);
        Expresion valor = expresion();
        return new Variable(nombre.getLexema(), valor, nombre.getFila());
    }

    // <comandos> -> <comando> <mas_comandos>
    private List<Comando> comandos() {
        List<Comando> lista = new ArrayList<>();
        comandoProtegido(lista);
        masComandos(lista);
        return lista;
    }

    // <mas_comandos> -> <comando> <mas_comandos> | ε
    private void masComandos(List<Comando> lista) {
        while (verifica(TipoToken.COMANDO_IA)) {
            comandoProtegido(lista);
        }
    }

    private void comandoProtegido(List<Comando> lista) {
        try {
            lista.add(comando());
        } catch (ErrorSintactico e) {
            sincronizar();
        }
    }

    // <comando> -> COMANDO_IA <expresion> <conectores> FLECHA ID
    private Comando comando() {
        Token inicio = match(TipoToken.COMANDO_IA);
        Expresion argumento = expresion();
        List<Conector> conectores = conectores();
        match(TipoToken.FLECHA);
        Token destino = match(TipoToken.ID);
        return new Comando(inicio.getLexema(), argumento, conectores,
                destino.getLexema(), inicio.getFila(), destino.getFila());
    }

    // <conectores> -> CONECTOR <expresion> <conectores> | ε
    private List<Conector> conectores() {
        List<Conector> lista = new ArrayList<>();
        while (verifica(TipoToken.CONECTOR)) {
            Token conector = avanzar();
            lista.add(new Conector(conector.getLexema(), expresion(), conector.getFila()));
        }
        return lista;
    }
    
        // <programa> -> <directivas> <agentes> <ejecuciones> <exportar> EOF
    public Programa parsear() {
        List<Directiva> directivas = directivas();
        List<Agente> agentes = agentes();
        List<Referencia> ejecuciones = ejecuciones();
        List<Referencia> exportaciones = exportar();
        if (!verifica(TipoToken.EOF)) error("fin de archivo");
        return new Programa(directivas, agentes, ejecuciones, exportaciones);
    }

    // <directivas> -> DIRECTIVA CADENA <directivas> | ε
    private List<Directiva> directivas() {
        List<Directiva> lista = new ArrayList<>();
        while (verifica(TipoToken.DIRECTIVA)) {
            try {
                Token nombre = match(TipoToken.DIRECTIVA);
                Token valor = match(TipoToken.CADENA);
                lista.add(new Directiva(nombre.getLexema(), sinComillas(valor.getLexema()), nombre.getFila()));
            } catch (ErrorSintactico e) {
                // DIRECTIVA ya se consumió: hay progreso y el siguiente nivel sincroniza
            }
        }
        return lista;
    }

    // <agentes> -> <agente> <mas_agentes>
    private List<Agente> agentes() {
        List<Agente> lista = new ArrayList<>();
        if (verifica(TipoToken.AGENTE)) {
            lista.add(agente());
        } else {
            error("AGENTE");
            sincronizarPrograma();
        }
        masAgentes(lista);
        return lista;
    }

    // <mas_agentes> -> <agente> <mas_agentes> | ε
    private void masAgentes(List<Agente> lista) {
        while (!verifica(TipoToken.EJECUTAR) && !verifica(TipoToken.EXPORTAR) && !verifica(TipoToken.EOF)) {
            if (verifica(TipoToken.AGENTE)) {
                lista.add(agente());
            } else {
                error("AGENTE o EJECUTAR");
                sincronizarPrograma();
            }
        }
    }

    // <agente> -> AGENTE ID LLAVE_A <contexto> <variables> <comandos> LLAVE_C
    private Agente agente() {
        Token inicio = match(TipoToken.AGENTE);
        String nombre = null;
        String contexto = null;
        try {
            nombre = match(TipoToken.ID).getLexema();
            match(TipoToken.LLAVE_A);
            contexto = contexto();
        } catch (ErrorSintactico e) {
            sincronizar();
        }
        List<Variable> variables = variables();
        List<Comando> comandos = comandos();
        cerrarAgente(comandos);
        return new Agente(nombre, contexto, variables, comandos, inicio.getFila());
    }

    private void cerrarAgente(List<Comando> comandos) {
        while (!verifica(TipoToken.LLAVE_C)) {
            error("COMANDO_IA o LLAVE_C");
            sincronizar();
            if (!verifica(TipoToken.COMANDO_IA)) break;
            masComandos(comandos);
        }
        if (verifica(TipoToken.LLAVE_C)) avanzar();
    }

    // <ejecuciones> -> EJECUTAR ID <mas_ejecuciones>
    private List<Referencia> ejecuciones() {
        List<Referencia> lista = new ArrayList<>();
        if (verifica(TipoToken.EJECUTAR)) {
            ejecucion(lista);
        } else {
            error("EJECUTAR");
            sincronizarPrograma();
        }
        masEjecuciones(lista);
        return lista;
    }

    // <mas_ejecuciones> -> EJECUTAR ID <mas_ejecuciones> | ε
    private void masEjecuciones(List<Referencia> lista) {
        while (!verifica(TipoToken.EXPORTAR) && !verifica(TipoToken.EOF)) {
            if (verifica(TipoToken.EJECUTAR)) {
                ejecucion(lista);
            } else {
                error("EJECUTAR o EXPORTAR");
                avanzar();
                sincronizarPrograma();
            }
        }
    }

    private void ejecucion(List<Referencia> lista) {
        try {
            match(TipoToken.EJECUTAR);
            Token id = match(TipoToken.ID);
            lista.add(new Referencia(id.getLexema(), id.getFila()));
        } catch (ErrorSintactico e) {
            sincronizarPrograma();
        }
    }

    // <exportar> -> EXPORTAR ID <mas_ids>
    private List<Referencia> exportar() {
        List<Referencia> lista = new ArrayList<>();
        try {
            match(TipoToken.EXPORTAR);
            Token id = match(TipoToken.ID);
            lista.add(new Referencia(id.getLexema(), id.getFila()));
            masIds(lista);
        } catch (ErrorSintactico e) {
            sincronizarPrograma();
        }
        return lista;
    }

    // <mas_ids> -> COMA ID <mas_ids> | ε
    private void masIds(List<Referencia> lista) {
        while (verifica(TipoToken.COMA)) {
            avanzar();
            Token id = match(TipoToken.ID);
            lista.add(new Referencia(id.getLexema(), id.getFila()));
        }
    }
}
