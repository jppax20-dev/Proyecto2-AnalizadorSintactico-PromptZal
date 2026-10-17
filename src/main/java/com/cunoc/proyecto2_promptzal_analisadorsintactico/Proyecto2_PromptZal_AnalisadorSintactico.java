/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.cunoc.proyecto2_promptzal_analisadorsintactico;

import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.Agente;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.Programa;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.errores.ErrorCompilacion;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.generador.GeneradorPromptzal;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.lexer.AnalizadorLexico;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.lexer.Token;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.parser.Parser;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.reportes.ReporteErrores;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.semantico.AnalizadorSemantico;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.semantico.Simbolo;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.semantico.TablaSimbolos;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;

public class Proyecto2_PromptZal_AnalisadorSintactico {

            public static void main(String[] args) throws Exception {
        AnalizadorLexico lexico = new AnalizadorLexico();
        lexico.analizar(Files.readString(Path.of(args.length > 0 ? args[0] : "ejemplo.pz")));

        List<ErrorCompilacion> errores = new ArrayList<>(lexico.getErrores());
        Programa programa = new Parser(lexico.getTokens(), errores).parsear();
        TablaSimbolos tabla = new AnalizadorSemantico(errores).analizar(programa);

        for (ErrorCompilacion e : errores) {
            System.out.println(e.getTipo() + " " + e.getFila() + ":" + e.getColumna() + "  " + e.getDescripcion());
        }
        for (Simbolo s : tabla.getSimbolos()) {
            System.out.println(s.getNombre() + "  " + s.getTipo() + "  " + s.getAmbito() + "  " + s.getFila());
        }
        System.out.println(new GeneradorPromptzal().generar(programa));
        Files.writeString(Path.of("reporte_errores.html"), new ReporteErrores().generar(errores));
    }
}