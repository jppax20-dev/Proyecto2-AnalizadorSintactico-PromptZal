package com.cunoc.proyecto2_promptzal_analisadorsintactico.compilador;

import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.Programa;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.errores.ErrorCompilacion;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.generador.GeneradorPromptzal;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.lexer.AnalizadorLexico;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.lexer.Token;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.parser.Parser;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.semantico.AnalizadorSemantico;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.semantico.TablaSimbolos;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Compilador {

    public ResultadoCompilacion compilar(String texto) {
        AnalizadorLexico lexico = new AnalizadorLexico();
        lexico.analizar(texto);

        // Junta los errores de escritura iniciales con los que se encuentren despues
        List<ErrorCompilacion> errores = new ArrayList<>(lexico.getErrores());
        Programa programa = new Parser(lexico.getTokens(), errores).parsear();
        TablaSimbolos tabla = new AnalizadorSemantico(errores).analizar(programa);
        String promptzal = new GeneradorPromptzal().generar(programa);
        
        // Ordena los fallos de arriba hacia abajo segun el numero de linea y columna
        errores.sort(Comparator.comparingInt(ErrorCompilacion::getFila)
        .thenComparingInt(ErrorCompilacion::getColumna));

        // Empaqueta todo el trabajo finalizado para mandarlo a la ventana visual
        return new ResultadoCompilacion(lexico.getTokens(), errores, programa, tabla, promptzal);
    }
    
        public List<Token> tokenizar(String texto) {
        AnalizadorLexico lexico = new AnalizadorLexico();
        lexico.analizar(texto);
        return lexico.getTokens();
    }
}
