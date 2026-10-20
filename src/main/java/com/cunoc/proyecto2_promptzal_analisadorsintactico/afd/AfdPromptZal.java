package com.cunoc.proyecto2_promptzal_analisadorsintactico.afd;

import com.cunoc.proyecto2_promptzal_analisadorsintactico.afd.Afd.Tipo;

public final class AfdPromptZal {

    private AfdPromptZal() { }

    public static Afd construir() {
        Afd afd = new Afd("q0");

        afd.estado("q0", "q0", Tipo.NORMAL)
           .estado("ID", "ID /\\nreservada", Tipo.ACEPTACION)
           .estado("ENT", "NUMERO\\nentero", Tipo.ACEPTACION)
           .estado("PUNTO", ".", Tipo.NORMAL)
           .estado("DEC", "NUMERO\\ndecimal", Tipo.ACEPTACION)
           .estado("ABIERTA", "cadena\\nabierta", Tipo.ERROR)
           .estado("CADENA", "CADENA", Tipo.ACEPTACION)
           .estado("ARROBA", "@", Tipo.ERROR)
           .estado("DIR", "DIRECTIVA", Tipo.ACEPTACION)
           .estado("GUION", "-", Tipo.ERROR)
           .estado("FLECHA", "FLECHA", Tipo.ACEPTACION)
           .estado("BARRA", "/", Tipo.ERROR)
           .estado("COM_L", "comentario\\nde línea", Tipo.DESCARTE)
           .estado("COM_B", "/* ...", Tipo.NORMAL)
           .estado("COM_AST", "/* ... *", Tipo.NORMAL)
           .estado("COM_FIN", "comentario\\nde bloque", Tipo.DESCARTE)
           .estado("IGUAL", "IGUAL", Tipo.ACEPTACION)
           .estado("MAS", "MAS", Tipo.ACEPTACION)
           .estado("LLAVE_A", "LLAVE_A", Tipo.ACEPTACION)
           .estado("LLAVE_C", "LLAVE_C", Tipo.ACEPTACION)
           .estado("PAR_A", "PAR_A", Tipo.ACEPTACION)
           .estado("PAR_C", "PAR_C", Tipo.ACEPTACION)
           .estado("COMA", "COMA", Tipo.ACEPTACION)
           .estado("ERR", "error", Tipo.ERROR);

        afd.transicion("q0", "espacio, tab,\\nsalto de línea", "q0")
           .transicion("q0", "L", "ID")
           .transicion("ID", "L, D", "ID")
           .transicion("q0", "D", "ENT")
           .transicion("ENT", "D", "ENT")
           .transicion("ENT", ".", "PUNTO")
           .transicion("PUNTO", "D", "DEC")
           .transicion("DEC", "D", "DEC")
           .transicion("q0", "\"", "ABIERTA")
           .transicion("ABIERTA", "todo menos\\n\" y salto de línea", "ABIERTA")
           .transicion("ABIERTA", "\"", "CADENA")
           .transicion("q0", "@", "ARROBA")
           .transicion("ARROBA", "L", "DIR")
           .transicion("DIR", "L", "DIR")
           .transicion("q0", "-", "GUION")
           .transicion("GUION", ">", "FLECHA")
           .transicion("q0", "/", "BARRA")
           .transicion("BARRA", "/", "COM_L")
           .transicion("COM_L", "todo menos\\nsalto de línea", "COM_L")
           .transicion("BARRA", "*", "COM_B")
           .transicion("COM_B", "todo menos *", "COM_B")
           .transicion("COM_B", "*", "COM_AST")
           .transicion("COM_AST", "*", "COM_AST")
           .transicion("COM_AST", "todo menos * y /", "COM_B")
           .transicion("COM_AST", "/", "COM_FIN")
           .transicion("q0", "=", "IGUAL")
           .transicion("q0", "+", "MAS")
           .transicion("q0", "{", "LLAVE_A")
           .transicion("q0", "}", "LLAVE_C")
           .transicion("q0", "(", "PAR_A")
           .transicion("q0", ")", "PAR_C")
           .transicion("q0", ",", "COMA")
           .transicion("q0", "cualquier\\notro", "ERR");

        return afd;
    }
}