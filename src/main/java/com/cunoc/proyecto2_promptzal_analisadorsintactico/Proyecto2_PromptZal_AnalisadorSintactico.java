
package com.cunoc.proyecto2_promptzal_analisadorsintactico;

import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.Agente;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.Programa;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.compilador.Compilador;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.compilador.ResultadoCompilacion;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.errores.ErrorCompilacion;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.generador.GeneradorPromptzal;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.lexer.AnalizadorLexico;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.lexer.Token;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.parser.Parser;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.reportes.ReporteAst;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.reportes.ReporteErrores;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.reportes.ReporteSimbolos;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.semantico.AnalizadorSemantico;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.semantico.Simbolo;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.semantico.TablaSimbolos;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;

public class Proyecto2_PromptZal_AnalisadorSintactico {

            public static void main(String[] args) throws Exception {
        String texto = Files.readString(Path.of(args.length > 0 ? args[0] : "ejemplo.pz"));
        ResultadoCompilacion r = new Compilador().compilar(texto);

        for (ErrorCompilacion e : r.getErrores()) {
            System.out.println(e.getTipo() + " " + e.getFila() + ":" + e.getColumna() + "  " + e.getDescripcion());
        }
        System.out.println(r.getPromptzal());

        Files.writeString(Path.of("reporte_errores.html"), new ReporteErrores().generar(r.getErrores()));
        Files.writeString(Path.of("reporte_simbolos.html"), new ReporteSimbolos().generar(r.getTabla()));
        Files.writeString(Path.of("reporte_ast.html"), new ReporteAst().generar(r.getPrograma()));
    }
}