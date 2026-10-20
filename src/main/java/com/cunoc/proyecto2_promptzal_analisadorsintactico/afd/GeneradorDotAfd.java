package com.cunoc.proyecto2_promptzal_analisadorsintactico.afd;

public class GeneradorDotAfd {

    private static final String LEYENDA =
            "L = letra o _     D = dígito     Punteado rojo = error     Gris = se descarta";

    public String generar(Afd afd) {
        StringBuilder dot = new StringBuilder();
        dot.append("digraph AFD {\n");
        dot.append("  rankdir=LR;\n");
        dot.append("  graph [label=\"").append(LEYENDA).append("\", labelloc=b, fontname=\"Helvetica\"];\n");
        dot.append("  node [shape=circle, fontname=\"Helvetica\", fontsize=11];\n");
        dot.append("  edge [fontname=\"Helvetica\", fontsize=10];\n\n");
        dot.append("  inicio [shape=point];\n");
        dot.append("  inicio -> ").append(afd.getInicial()).append(";\n\n");

        for (Afd.Estado e : afd.getEstados()) {
            dot.append("  ").append(e.id()).append(" [label=\"").append(texto(e.etiqueta()))
                    .append("\"").append(estilo(e.tipo())).append("];\n");
        }
        dot.append('\n');
        for (Afd.Transicion t : afd.getTransiciones()) {
            dot.append("  ").append(t.origen()).append(" -> ").append(t.destino())
                    .append(" [label=\"").append(texto(t.simbolo())).append("\"];\n");
        }
        dot.append("}\n");
        return dot.toString();
    }

    private String estilo(Afd.Tipo tipo) {
        return switch (tipo) {
            case NORMAL -> "";
            case ACEPTACION -> ", shape=doublecircle";
            case DESCARTE -> ", shape=doublecircle, style=filled, fillcolor=\"#e9ecef\"";
            case ERROR -> ", shape=doublecircle, style=dashed, color=red";
        };
    }

    private String texto(String valor) {
        return valor.replace("\"", "\\\"");
    }
}