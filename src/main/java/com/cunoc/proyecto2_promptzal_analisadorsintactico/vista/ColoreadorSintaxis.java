package com.cunoc.proyecto2_promptzal_analisadorsintactico.vista;

import com.cunoc.proyecto2_promptzal_analisadorsintactico.lexer.TipoToken;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.lexer.Token;
import java.awt.Color;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

final class ColoreadorSintaxis {

    private static final Color COMENTARIO = new Color(120, 120, 120);
    private static final Map<TipoToken, Color> COLORES = new EnumMap<>(TipoToken.class);
    private static final Pattern CADENAS_Y_COMENTARIOS =
            Pattern.compile("\"[^\"\\r\\n]*\"?|//[^\\r\\n]*|/\\*.*?(?:\\*/|\\z)", Pattern.DOTALL);

    static {
        Color reservada = new Color(0, 51, 204);
        Color comando = new Color(0, 128, 128);
        for (TipoToken tipo : new TipoToken[] {TipoToken.AGENTE, TipoToken.CONTEXTO, TipoToken.VARIABLE,
                TipoToken.EJECUTAR, TipoToken.EXPORTAR}) {
            COLORES.put(tipo, reservada);
        }
        COLORES.put(TipoToken.DIRECTIVA, new Color(128, 0, 128));
        COLORES.put(TipoToken.COMANDO_IA, comando);
        COLORES.put(TipoToken.CARGAR, comando);
        COLORES.put(TipoToken.CONECTOR, new Color(204, 102, 0));
        COLORES.put(TipoToken.CADENA, new Color(0, 153, 0));
        COLORES.put(TipoToken.NUMERO, new Color(200, 0, 0));
    }

    private ColoreadorSintaxis() {
    }

    static void colorear(StyledDocument documento, String texto, List<Token> tokens) {
        pintar(documento, 0, documento.getLength(), Color.BLACK, false);
        Element raiz = documento.getDefaultRootElement();
        for (Token token : tokens) {
            Color color = COLORES.get(token.getTipo());
            int linea = token.getFila() - 1;
            if (color == null || linea < 0 || linea >= raiz.getElementCount()) continue;
            int inicio = raiz.getElement(linea).getStartOffset() + token.getColumna() - 1;
            pintar(documento, inicio, token.getLexema().length(), color, false);
        }
        Matcher coincidencia = CADENAS_Y_COMENTARIOS.matcher(texto);
        while (coincidencia.find()) {
            if (coincidencia.group().startsWith("/")) {
                pintar(documento, coincidencia.start(), coincidencia.end() - coincidencia.start(), COMENTARIO, true);
            }
        }
    }

    private static void pintar(StyledDocument documento, int inicio, int largo, Color color, boolean cursiva) {
        if (inicio < 0 || largo <= 0 || inicio + largo > documento.getLength()) return;
        SimpleAttributeSet atributos = new SimpleAttributeSet();
        StyleConstants.setForeground(atributos, color);
        StyleConstants.setItalic(atributos, cursiva);
        documento.setCharacterAttributes(inicio, largo, atributos, false);
    }
}