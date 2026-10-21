package com.cunoc.proyecto2_promptzal_analisadorsintactico.afd;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GeneradorDot {

    private record Arista(String origen, String destino) { }

    private static final String LEYENDA =
            "L: letra o _     D: dígito     C: cualquiera salvo \\\" y salto de línea"
            + "\\nN: cualquiera salvo salto de línea     B: cualquiera salvo *     X: cualquiera salvo * y /"
            + "\\notro: cualquier carácter no listado     rojo punteado: error léxico"
            + "\\nlínea azul punteada: el lexema aceptado en q1 se busca en la lista; si no está, es ID";

    private final TablaTransiciones tabla;

    public GeneradorDot(TablaTransiciones tabla) {
        this.tabla = tabla;
    }

    public String generarDot() {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph AFD_PromptZal {\n");
        sb.append("    rankdir=LR;\n");
        sb.append("    nodesep=0.6;\n");
        sb.append("    ranksep=1.0;\n");
        sb.append("    dpi=150;\n");
        sb.append("    splines=true;\n");
        sb.append("    graph [fontname=\"Helvetica\", fontsize=12, labelloc=b, labeljust=l, label=\"")
          .append(LEYENDA).append("\"];\n");
        sb.append("    node [shape=circle, fontname=\"Helvetica\", fontsize=12, style=filled, fillcolor=white, color=black];\n");
        sb.append("    edge [fontname=\"Helvetica\", fontsize=11, fontcolor=blue, color=gray30];\n\n");

        sb.append("    inicio [shape=point, width=0];\n");
        sb.append("    inicio -> ").append(tabla.getEstadoInicial()).append(";\n\n");
        

        for (Map.Entry<String, String> e : tabla.getAceptacion().entrySet()) {
            sb.append("    ").append(e.getKey())
              .append(" [shape=doublecircle, label=\"").append(e.getKey())
              .append("\\n").append(e.getValue()).append("\"];\n");
        }

        sb.append("    ").append(tabla.getEstadoError())
          .append(" [shape=circle, fillcolor=\"#ffe3e3\", color=red];\n\n");
        sb.append(categorias());
        for (Map.Entry<Arista, List<String>> e : agrupar().entrySet()) {
            Arista arista = e.getKey();
            sb.append("    ").append(arista.origen()).append(" -> ").append(arista.destino())
              .append(" [label=\"  ").append(String.join(", ", e.getValue())).append("  \"");
            if (arista.destino().equals(tabla.getEstadoError())) {
                sb.append(", style=dashed, color=red, fontcolor=red");
            }
            sb.append("];\n");
        }

        sb.append("}\n");
        return sb.toString();
    }

    private Map<Arista, List<String>> agrupar() {
        Map<Arista, List<String>> grupos = new LinkedHashMap<>();
        for (TablaTransiciones.Transicion t : tabla.getTransiciones()) {
            grupos.computeIfAbsent(new Arista(t.origen, t.destino), k -> new ArrayList<>()).add(t.simbolo);
        }
        return grupos;
    }
    
        private String categorias() {
        StringBuilder sb = new StringBuilder();
        int numero = 0;
        for (Map.Entry<String, List<String>> e : tabla.getReservadas().entrySet()) {
            String nodo = "cat" + numero++;
            sb.append("    ").append(nodo)
              .append(" [shape=box, style=\"rounded,filled\", fillcolor=\"#fff9db\", fontsize=11, label=\"")
              .append(e.getKey()).append("\\l");
            for (String lexema : e.getValue()) {
                sb.append("  ").append(lexema).append("\\l");
            }
            sb.append("\"];\n");
            sb.append("    q1 -> ").append(nodo)
              .append(" [style=dashed, color=\"#2b6cb0\", fontcolor=\"#2b6cb0\", label=\"  lexema en la lista  \"];\n");
        }
        return sb.append('\n').toString();
    }
}