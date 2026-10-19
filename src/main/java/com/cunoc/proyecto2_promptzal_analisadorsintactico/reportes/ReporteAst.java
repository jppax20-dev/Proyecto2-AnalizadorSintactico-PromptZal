package com.cunoc.proyecto2_promptzal_analisadorsintactico.reportes;

import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.Agente;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.Comando;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.Conector;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.Directiva;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.Expresion;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.Programa;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.Referencia;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.Termino;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.TerminoCadena;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.TerminoCargar;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.TerminoId;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.TerminoNumero;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.Variable;
import java.util.List;

public class ReporteAst {

    public String generar(Programa programa) {
        StringBuilder sb = new StringBuilder("Programa\n");
        linea(sb, 1, "Directivas");
        for (Directiva d : programa.getDirectivas()) {
            linea(sb, 2, d.getNombre() + " = \"" + d.getValor() + "\"");
        }
        linea(sb, 1, "Agentes");
        for (Agente a : programa.getAgentes()) {
            agente(sb, a);
        }
        linea(sb, 1, "Ejecuciones");
        for (Referencia r : programa.getEjecuciones()) {
            linea(sb, 2, r.getNombre());
        }
        linea(sb, 1, "Exportaciones");
        for (Referencia r : programa.getExportaciones()) {
            linea(sb, 2, r.getNombre());
        }
        return Html.pagina("Reporte del AST", "<pre>" + Html.escapar(sb.toString()) + "</pre>\n");
    }

    private void agente(StringBuilder sb, Agente a) {
        String nombre = a.getNombre() == null ? "(sin nombre)" : a.getNombre();
        linea(sb, 2, "Agente " + nombre + " [línea " + a.getFila() + "]");
        linea(sb, 3, "Contexto: " + (a.getContexto() == null ? "(no definido)" : "\"" + a.getContexto() + "\""));
        for (Variable v : a.getVariables()) {
            linea(sb, 3, "Variable " + v.getNombre() + " [línea " + v.getFila() + "]");
            expresion(sb, 4, v.getValor());
        }
        for (Comando c : a.getComandos()) {
            comando(sb, c);
        }
    }

    private void comando(StringBuilder sb, Comando c) {
        linea(sb, 3, "Comando " + c.getNombre() + " [línea " + c.getFila() + "]");
        linea(sb, 4, "Argumento");
        expresion(sb, 5, c.getArgumento());
        for (Conector k : c.getConectores()) {
            linea(sb, 4, "Conector " + k.getNombre());
            expresion(sb, 5, k.getArgumento());
        }
        linea(sb, 4, "Destino: " + c.getDestino());
    }

    private void expresion(StringBuilder sb, int nivel, Expresion e) {
        List<Termino> terminos = e.getTerminos();
        if (terminos.size() > 1) {
            linea(sb, nivel, "Concatenación");
            nivel++;
        }
        for (Termino t : terminos) {
            linea(sb, nivel, descripcion(t));
        }
    }

    private String descripcion(Termino t) {
        if (t instanceof TerminoCadena c) return "Cadena \"" + c.getValor() + "\"";
        if (t instanceof TerminoId id) return "Identificador " + id.getNombre();
        if (t instanceof TerminoNumero n) {
            return "Número " + n.getValor() + (n.getUnidad() == null ? "" : " (unidad: " + n.getUnidad() + ")");
        }
        if (t instanceof TerminoCargar c) return "Cargar \"" + c.getArchivo() + "\"";
        return "Término desconocido";
    }

    private void linea(StringBuilder sb, int nivel, String texto) {
        sb.append("    ".repeat(nivel)).append(texto).append('\n');
    }
}