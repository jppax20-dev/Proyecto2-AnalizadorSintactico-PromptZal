package com.cunoc.proyecto2_promptzal_analisadorsintactico.vista;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

class EditorCodigo extends JTextPane {

    private final Image fondo;

    EditorCodigo(Image fondo) {
        this.fondo = fondo;
        setOpaque(false);
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        setMargin(new Insets(0, 0, 0, 0));
        setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        setCaretColor(Tema.CIAN);
        setSelectionColor(Tema.SELECCION);
        setSelectedTextColor(Color.WHITE);
        Style predeterminado = getStyledDocument().getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setForeground(predeterminado, Tema.TEXTO);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        Container padre = getParent();
        return padre instanceof JViewport && getUI().getPreferredSize(this).width <= padre.getWidth();
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(Tema.FONDO);
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
        if (fondo != null) {
            pintarFondo((Graphics2D) g.create());
        }
    }

    private void pintarFondo(Graphics2D g2) {
        Rectangle visible = getVisibleRect();
        int ancho = fondo.getWidth(null);
        int alto = fondo.getHeight(null);
        if (ancho > 0 && alto > 0 && !visible.isEmpty()) {
            double escala = Math.min(visible.width / (double) ancho, visible.height / (double) alto) * 0.6;
            int anchoFinal = (int) (ancho * escala);
            int altoFinal = (int) (alto * escala);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f));
            g2.drawImage(fondo, visible.x + (visible.width - anchoFinal) / 2,
                    visible.y + (visible.height - altoFinal) / 2, anchoFinal, altoFinal, null);
        }
        g2.dispose();
    }
}