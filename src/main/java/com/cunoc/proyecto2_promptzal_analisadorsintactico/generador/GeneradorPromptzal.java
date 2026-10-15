package com.cunoc.proyecto2_promptzal_analisadorsintactico.generador;

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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GeneradorPromptzal {

    public String generar(Programa programa) {
        StringBuilder sb = new StringBuilder();
        encabezado(programa, sb);
        Set<String> escritos = new HashSet<>();
        for (Referencia ejecucion : programa.getEjecuciones()) {
            Agente agente = buscarAgente(programa, ejecucion.getNombre());
            if (agente != null && escritos.add(ejecucion.getNombre())) {
                bloqueAgente(agente, sb);
            }
        }
        resultados(programa, sb);
        return sb.toString();
    }

    private void encabezado(Programa programa, StringBuilder sb) {
        sb.append("# Prompt generado con PromptZal\n");
        lineaDirectiva(programa, "@modelo", "Modelo destino", sb);
        lineaDirectiva(programa, "@rol", "Rol general", sb);
        lineaDirectiva(programa, "@formato", "Formato de respuesta", sb);
    }

    private void lineaDirectiva(Programa programa, String nombre, String etiqueta, StringBuilder sb) {
        for (Directiva d : programa.getDirectivas()) {
            if (d.getNombre().equals(nombre)) {
                sb.append(etiqueta).append(": ").append(d.getValor()).append("\n");
                return;
            }
        }
    }

    private Agente buscarAgente(Programa programa, String nombre) {
        for (Agente a : programa.getAgentes()) {
            if (nombre.equals(a.getNombre())) return a;
        }
        return null;
    }

    private void bloqueAgente(Agente agente, StringBuilder sb) {
        sb.append("\n## Agente: ").append(agente.getNombre()).append("\n");
        if (agente.getContexto() != null) {
            sb.append("Contexto: ").append(agente.getContexto()).append("\n");
        }
        if (!agente.getVariables().isEmpty()) {
            sb.append("Variables disponibles:\n");
            for (Variable v : agente.getVariables()) {
                sb.append("- ").append(v.getNombre()).append(": ").append(origen(v.getValor())).append("\n");
            }
        }
        sb.append("Instrucciones:\n");
        int numero = 1;
        for (Comando c : agente.getComandos()) {
            sb.append(numero++).append(". ").append(instruccion(c)).append("\n");
        }
    }

    private void resultados(Programa programa, StringBuilder sb) {
        sb.append("\n## Resultado solicitado\n");
        for (Referencia r : programa.getExportaciones()) {
            sb.append("- ").append(r.getNombre()).append("\n");
        }
    }

    private String instruccion(Comando c) {
        String x = texto(c.getArgumento());
        Expresion sobre = conector(c, "SOBRE");
        Expresion desde = conector(c, "DESDE");
        Expresion en = conector(c, "EN");
        Expresion como = conector(c, "COMO");
        String guarda = "el resultado";
        String base;
        switch (c.getNombre()) {
            case "PREGUNTAR":
                base = sobre == null ? "Responde: " + x
                        : "Basandote en " + texto(sobre) + ", responde: " + x;
                guarda = "la respuesta";
                break;
            case "GENERAR":
                base = desde == null ? "Genera " + x
                        : "Genera " + x + " a partir de: " + texto(desde);
                break;
            case "RESUMIR":
                base = en == null ? "Resume " + x
                        : "Resume " + x + " en un maximo de " + texto(en);
                break;
            case "ANALIZAR":
                base = sobre == null ? "Analiza " + x
                        : "Analiza " + x + " considerando: " + texto(sobre);
                break;
            case "TRADUCIR":
                base = en == null ? "Traduce " + x
                        : "Traduce " + x + " al idioma " + texto(en);
                break;
            case "CLASIFICAR":
                base = en == null ? "Clasifica " + x
                        : "Clasifica " + x + " en las categorias: " + texto(en);
                break;
            case "EXTRAER":
                base = desde == null ? "Extrae " + x
                        : "Extrae " + x + " a partir de " + texto(desde);
                break;
            default:
                base = instruccionGenerica(c, x);
        }
        String formato = como == null ? "" : " Entrega el resultado en formato " + texto(como) + ".";
        return cerrar(base) + " Guarda " + guarda + " como " + c.getDestino() + "." + formato;
    }

    private String instruccionGenerica(Comando c, String x) {
        StringBuilder sb = new StringBuilder("Realiza la operacion " + c.getNombre() + " sobre " + x);
        for (Conector k : c.getConectores()) {
            if (!k.getNombre().equals("COMO")) {
                sb.append(", ").append(k.getNombre().toLowerCase()).append(": ").append(texto(k.getArgumento()));
            }
        }
        return sb.toString();
    }

    private Expresion conector(Comando c, String nombre) {
        for (Conector k : c.getConectores()) {
            if (k.getNombre().equals(nombre)) return k.getArgumento();
        }
        return null;
    }

    private String cerrar(String frase) {
        return frase.matches(".*[.?!]$") ? frase : frase + ".";
    }

    private String texto(Expresion expresion) {
        List<String> partes = new ArrayList<>();
        for (Termino t : expresion.getTerminos()) {
            partes.add(texto(t));
        }
        return String.join(" + ", partes);
    }

    private String texto(Termino termino) {
        if (termino instanceof TerminoCadena cadena) return cadena.getValor();
        if (termino instanceof TerminoId id) return id.getNombre();
        if (termino instanceof TerminoNumero numero) {
            return numero.getUnidad() == null ? numero.getValor() : numero.getValor() + " " + numero.getUnidad();
        }
        if (termino instanceof TerminoCargar carga) return "el contenido del archivo \"" + carga.getArchivo() + "\"";
        return "";
    }

    private String origen(Expresion expresion) {
        List<String> partes = new ArrayList<>();
        for (Termino t : expresion.getTerminos()) {
            partes.add(origen(t));
        }
        return String.join(" + ", partes);
    }

    private String origen(Termino termino) {
        if (termino instanceof TerminoCadena cadena) return "\"" + cadena.getValor() + "\"";
        if (termino instanceof TerminoCargar carga) {
            return "contenido del archivo \"" + carga.getArchivo() + "\" (adjuntar el archivo)";
        }
        return texto(termino);
    }
}