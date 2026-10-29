package com.cunoc.proyecto2_promptzal_analisadorsintactico.graphviz;

import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.*;
import java.util.List;

public class GeneradorDotAst {

    private int contador;

    private String id() {
        return "n" + (contador++);
    }

    // El método debe llamarse generarCodigo y retornar String
    public String generarCodigo(Programa programa) {
        contador = 0;
        StringBuilder dot = new StringBuilder();
        
        dot.append("digraph AST {\n");
        dot.append("  bgcolor=\"#0a0d14\";\n");
        dot.append("  rankdir=TB;\n");
        dot.append("  node [shape=box, style=\"rounded,filled\", fontname=\"Helvetica\", fillcolor=\"#f8f9fa\", color=\"#343a40\"];\n");
        dot.append("  edge [color=\"#adb5bd\", dir=none];\n\n");

        String idRaiz = id();
        dot.append("  ").append(idRaiz).append(" [label=\"Programa\", fillcolor=\"#2c3e50\", fontcolor=white];\n");

        for (Directiva d : programa.getDirectivas()) {
            String idDir = id();
            dot.append("  ").append(idDir).append(" [label=\"").append(d.getNombre()).append(" = '").append(d.getValor()).append("'\"];\n");
            dot.append("  ").append(idRaiz).append(" -> ").append(idDir).append(";\n");
        }

        for (Agente a : programa.getAgentes()) {
            agente(dot, idRaiz, a);
        }

        for (Referencia r : programa.getEjecuciones()) {
            String idEjec = id();
            dot.append("  ").append(idEjec).append(" [label=\"Ejecutar: ").append(r.getNombre()).append("\"];\n");
            dot.append("  ").append(idRaiz).append(" -> ").append(idEjec).append(";\n");
        }

        for (Referencia r : programa.getExportaciones()) {
            String idExp = id();
            dot.append("  ").append(idExp).append(" [label=\"Exportar: ").append(r.getNombre()).append("\"];\n");
            dot.append("  ").append(idRaiz).append(" -> ").append(idExp).append(";\n");
        }

        dot.append("}\n");
        return dot.toString();
    }

    private void agente(StringBuilder dot, String idPadre, Agente a) {
        String idAgente = id();
        String nombre = a.getNombre() == null ? "Sin nombre" : a.getNombre();
        dot.append("  ").append(idAgente).append(" [label=\"Agente: ").append(nombre).append("\\nLínea: ").append(a.getFila()).append("\", fillcolor=\"#d35400\", fontcolor=white];\n");
        dot.append("  ").append(idPadre).append(" -> ").append(idAgente).append(";\n");

        if (a.getContexto() != null) {
            String idCtx = id();
            dot.append("  ").append(idCtx).append(" [label=\"Contexto: '").append(a.getContexto()).append("'\"];\n");
            dot.append("  ").append(idAgente).append(" -> ").append(idCtx).append(";\n");
        }

        for (Variable v : a.getVariables()) {
            String idVar = id();
            dot.append("  ").append(idVar).append(" [label=\"Var: ").append(v.getNombre()).append("\\nLínea: ").append(v.getFila()).append("\", fillcolor=\"#27ae60\", fontcolor=white];\n");
            dot.append("  ").append(idAgente).append(" -> ").append(idVar).append(";\n");
            expresion(dot, idVar, v.getValor());
        }

        for (Comando c : a.getComandos()) {
            comando(dot, idAgente, c);
        }
    }

    private void comando(StringBuilder dot, String idPadre, Comando c) {
        String idCmd = id();
        dot.append("  ").append(idCmd).append(" [label=\"").append(c.getNombre()).append("\\nLínea: ").append(c.getFila()).append("\", fillcolor=\"#2980b9\", fontcolor=white];\n");
        dot.append("  ").append(idPadre).append(" -> ").append(idCmd).append(";\n");

        String idArg = id();
        dot.append("  ").append(idArg).append(" [label=\"Argumento\", fillcolor=\"#8e44ad\", fontcolor=white];\n");
        dot.append("  ").append(idCmd).append(" -> ").append(idArg).append(";\n");
        expresion(dot, idArg, c.getArgumento());

        for (Conector k : c.getConectores()) {
            String idConector = id();
            dot.append("  ").append(idConector).append(" [label=\"").append(k.getNombre()).append("\", fillcolor=\"#8e44ad\", fontcolor=white];\n");
            dot.append("  ").append(idCmd).append(" -> ").append(idConector).append(";\n");
            expresion(dot, idConector, k.getArgumento());
        }

        String idDestino = id();
        dot.append("  ").append(idDestino).append(" [label=\"-> ").append(c.getDestino()).append("\"];\n");
        dot.append("  ").append(idCmd).append(" -> ").append(idDestino).append(";\n");
    }

    private void expresion(StringBuilder dot, String idPadre, Expresion e) {
        List<Termino> terminos = e.getTerminos();
        if (terminos.size() > 1) {
            String idConcat = id();
            dot.append("  ").append(idConcat).append(" [label=\"+\"];\n");
            dot.append("  ").append(idPadre).append(" -> ").append(idConcat).append(";\n");
            for (Termino t : terminos) {
                termino(dot, idConcat, t);
            }
        } else if (!terminos.isEmpty()) {
            termino(dot, idPadre, terminos.get(0));
        }
    }

    private void termino(StringBuilder dot, String idPadre, Termino t) {
        String idTerm = id();
        String desc = descripcion(t).replace("\"", "\\\"");
        dot.append("  ").append(idTerm).append(" [label=\"").append(desc).append("\"];\n");
        dot.append("  ").append(idPadre).append(" -> ").append(idTerm).append(";\n");
    }

    private String descripcion(Termino t) {
        if (t instanceof TerminoCadena c) return "\"" + c.getValor() + "\"";
        if (t instanceof TerminoId id) return id.getNombre();
        if (t instanceof TerminoNumero n) {
            return n.getValor() + (n.getUnidad() == null ? "" : " " + n.getUnidad());
        }
        if (t instanceof TerminoCargar c) return "CARGAR(\"" + c.getArchivo() + "\")";
        return "Desconocido";
    }
    private String esc(String texto) {
        return texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}