/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cunoc.proyecto2_promptzal_analisadorsintactico.parser;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.errores.ErrorCompilacion;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.lexer.Token;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.lexer.TipoToken;
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
        errores.add(ErrorCompilacion.sintactico(describir(t), esperado, t.getFila(), t.getColumna()));
        return new ErrorSintactico();
    }

    private void sincronizar() {
        while (!SINCRONIZACION.contains(actual().getTipo())) avanzar();
    }

    private String describir(Token t) {
        return t.getTipo() == TipoToken.EOF ? "fin de archivo" : t.getTipo() + " '" + t.getLexema() + "'";
    }
}
