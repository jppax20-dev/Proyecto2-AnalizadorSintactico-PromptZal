package com.cunoc.proyecto2_promptzal_analisadorsintactico.vista;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import javax.swing.JTextPane;
import javax.swing.JViewport;

class EditorCodigo extends JTextPane {

    private final Image fondo;

    EditorCodigo(Image fondo) {
        this.fondo = fondo;
        setOpaque(false);
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        Container padre = getParent();
        return padre instanceof JViewport && getUI().getPreferredSize(this).width <= padre.getWidth();
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        if (fondo != null) {
            pintarFondo((Graphics2D) g.create());
        }
        super.paintComponent(g);
    }

    private void pintarFondo(Graphics2D g2) {
        Rectangle visible = getVisibleRect();
        int ancho = fondo.getWidth(null);
        int alto = fondo.getHeight(null);
        if (ancho > 0 && alto > 0 && !visible.isEmpty()) {
            double escala = Math.min(visible.width / (double) ancho, visible.height / (double) alto) * 0.6;
            int anchoFinal = (int) (ancho * escala);
            int altoFinal = (int) (alto * escala);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));
            g2.drawImage(fondo, visible.x + (visible.width - anchoFinal) / 2,
                    visible.y + (visible.height - altoFinal) / 2, anchoFinal, altoFinal, null);
        }
        g2.dispose();
    }
}