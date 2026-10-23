package com.cunoc.proyecto2_promptzal_analisadorsintactico.vista;

import com.cunoc.proyecto2_promptzal_analisadorsintactico.lexer.Token;
import java.awt.Color;
import java.awt.Image;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.Timer;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Element;

class PestanaEditor extends JScrollPane {

    private final EditorCodigo editor;
    private final JTextArea lineas = new JTextArea();
    private final Function<String, List<Token>> tokenizador;
    private final Timer temporizadorColor;
    private Path ruta;
    private boolean modificado;
    private int lineasMostradas = -1;
    private Runnable alCambiarEstado = () -> { };
    private Runnable alEditarTexto = () -> { };

    PestanaEditor(String contenido, Path ruta, Image fondo, Function<String, List<Token>> tokenizador) {
        this.editor = new EditorCodigo(fondo);
        this.ruta = ruta;
        this.tokenizador = tokenizador;

        editor.setText(contenido);
        editor.setCaretPosition(0);

        lineas.setFont(editor.getFont());
        lineas.setBackground(new Color(240, 240, 240));
        lineas.setForeground(Color.GRAY);
        lineas.setEditable(false);
        lineas.setFocusable(false);

        temporizadorColor = new Timer(300, e -> colorear());
        temporizadorColor.setRepeats(false);

        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                alEditar();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                alEditar();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        setViewportView(editor);
        setRowHeaderView(lineas);
        setBorder(BorderFactory.createEmptyBorder());
        getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        actualizarLineas();
        colorear();
    }

    EditorCodigo getEditor() {
        return editor;
    }

    String getTexto() {
        return editor.getText();
    }

    Path getRuta() {
        return ruta;
    }

    String getNombre() {
        return ruta == null ? "SinTitulo.pz" : ruta.getFileName().toString();
    }

    boolean isModificado() {
        return modificado;
    }

    int getTotalLineas() {
        return editor.getDocument().getDefaultRootElement().getElementCount();
    }

    void setAlCambiarEstado(Runnable accion) {
        this.alCambiarEstado = accion;
    }
    
    void setAlEditarTexto(Runnable accion) {
        this.alEditarTexto = accion;
    }

    void addCaretListener(CaretListener oyente) {
        editor.addCaretListener(oyente);
    }

    void marcarGuardado(Path rutaGuardada) {
        this.ruta = rutaGuardada;
        this.modificado = false;
        alCambiarEstado.run();
    }

    String posicionCursor() {
        Element raiz = editor.getDocument().getDefaultRootElement();
        int posicion = editor.getCaretPosition();
        int fila = raiz.getElementIndex(posicion);
        int columna = posicion - raiz.getElement(fila).getStartOffset();
        return (fila + 1) + " : " + (columna + 1) + " ";
    }

    void irA(int fila, int columna) {
        Element raiz = editor.getDocument().getDefaultRootElement();
        Element linea = raiz.getElement(Math.max(0, Math.min(fila, raiz.getElementCount()) - 1));
        int posicion = linea.getStartOffset() + Math.max(0, columna - 1);
        editor.setCaretPosition(Math.min(posicion, linea.getEndOffset() - 1));
        editor.requestFocusInWindow();
    }
    
    private void alEditar() {
        actualizarLineas();
        temporizadorColor.restart();
        alEditarTexto.run();
        if (!modificado) {
            modificado = true;
            alCambiarEstado.run();
        }
    }

    private void colorear() {
        String texto = editor.getText();
        ColoreadorSintaxis.colorear(editor.getStyledDocument(), texto, tokenizador.apply(texto));
    }

    private void actualizarLineas() {
        int total = getTotalLineas();
        if (total == lineasMostradas) return;
        lineasMostradas = total;
        int digitos = Math.max(3, String.valueOf(total).length());
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= total; i++) {
            if (i > 1) sb.append('\n');
            sb.append(String.format(" %" + digitos + "d ", i));
        }
        lineas.setText(sb.toString());
    }
}