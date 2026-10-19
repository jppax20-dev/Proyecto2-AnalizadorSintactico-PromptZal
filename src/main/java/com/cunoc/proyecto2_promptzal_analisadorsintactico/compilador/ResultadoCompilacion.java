package com.cunoc.proyecto2_promptzal_analisadorsintactico.compilador;

import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.Programa;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.errores.ErrorCompilacion;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.lexer.Token;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.semantico.TablaSimbolos;
import java.util.List;

public class ResultadoCompilacion {

    private final List<Token> tokens;
    private final List<ErrorCompilacion> errores;
    private final Programa programa;
    private final TablaSimbolos tabla;
    private final String promptzal;

    public ResultadoCompilacion(List<Token> tokens, List<ErrorCompilacion> errores,
                                Programa programa, TablaSimbolos tabla, String promptzal) {
        this.tokens = tokens;
        this.errores = errores;
        this.programa = programa;
        this.tabla = tabla;
        this.promptzal = promptzal;
    }

    public List<Token> getTokens() { return tokens; }
    public List<ErrorCompilacion> getErrores() { return errores; }
    public Programa getPrograma() { return programa; }
    public TablaSimbolos getTabla() { return tabla; }
    public String getPromptzal() { return promptzal; }
}
