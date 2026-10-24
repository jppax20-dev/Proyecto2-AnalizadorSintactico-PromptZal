package com.cunoc.proyecto2_promptzal_analisadorsintactico.vista;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;

class BotonNeon extends JButton {

    private final Color acento;

    BotonNeon(String texto, Color acento) {
        super(texto);
        this.acento = acento;
        setFocusable(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setForeground(acento);
        setFont(getFont().deriveFont(Font.BOLD, 12f));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics medidas = getFontMetrics(getFont());
        return new Dimension(medidas.stringWidth(getText()) + 28, medidas.getHeight() + 14);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Color color = isEnabled() ? acento : Tema.TENUE;
        boolean presionado = getModel().isArmed() && getModel().isPressed();
        boolean encima = getModel().isRollover();

        g2.setColor(conAlfa(color, presionado ? 150 : encima ? 90 : 35));
        g2.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
        g2.setColor(conAlfa(color, encima || presionado ? 255 : 170));
        g2.setStroke(new BasicStroke(encima ? 2f : 1.2f));
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);

        g2.setFont(getFont());
        FontMetrics medidas = g2.getFontMetrics();
        int x = (getWidth() - medidas.stringWidth(getText())) / 2;
        int y = (getHeight() - medidas.getHeight()) / 2 + medidas.getAscent();
        g2.setColor(presionado ? Color.WHITE : color);
        g2.drawString(getText(), x, y);
        g2.dispose();
    }

    private static Color conAlfa(Color color, int alfa) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alfa);
    }
}