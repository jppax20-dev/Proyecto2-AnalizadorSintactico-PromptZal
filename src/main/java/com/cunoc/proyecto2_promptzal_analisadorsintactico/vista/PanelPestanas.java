package com.cunoc.proyecto2_promptzal_analisadorsintactico.vista;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

class PanelPestanas extends JPanel {

    private final JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
    private final CardLayout cartas = new CardLayout();
    private final JPanel contenido = new JPanel(cartas);
    private final Map<JComponent, Etiqueta> etiquetas = new LinkedHashMap<>();

    PanelPestanas() {
        super(new BorderLayout());
        barra.setBackground(Tema.PANEL_CLARO);
        barra.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.BORDE));
        contenido.setBackground(Tema.FONDO);
        setMinimumSize(new Dimension(0, 120));
        add(barra, BorderLayout.NORTH);
        add(contenido, BorderLayout.CENTER);
    }

    void agregar(String titulo, Color acento, JComponent panel) {
        Etiqueta etiqueta = new Etiqueta(titulo, acento, "carta" + etiquetas.size());
        etiqueta.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                seleccionar(panel);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                etiqueta.setEncima(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                etiqueta.setEncima(false);
            }
        });
        boolean primera = etiquetas.isEmpty();
        etiquetas.put(panel, etiqueta);
        barra.add(etiqueta);
        contenido.add(panel, etiqueta.carta);
        if (primera) seleccionar(panel);
    }

    void seleccionar(JComponent panel) {
        Etiqueta elegida = etiquetas.get(panel);
        if (elegida == null) return;
        cartas.show(contenido, elegida.carta);
        for (Etiqueta etiqueta : etiquetas.values()) {
            etiqueta.setSeleccionada(etiqueta == elegida);
        }
    }

    void rotular(JComponent panel, String texto) {
        Etiqueta etiqueta = etiquetas.get(panel);
        if (etiqueta == null) return;
        etiqueta.setText(texto);
        barra.revalidate();
        barra.repaint();
    }

    private static final class Etiqueta extends JLabel {

        private final Color acento;
        private final String carta;
        private boolean seleccionada;
        private boolean encima;

        Etiqueta(String texto, Color acento, String carta) {
            super(texto);
            this.acento = acento;
            this.carta = carta;
            setOpaque(false);
            setFont(getFont().deriveFont(Font.BOLD, 12f));
            setBorder(BorderFactory.createEmptyBorder(8, 14, 9, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            actualizarTexto();
        }

        void setSeleccionada(boolean valor) {
            seleccionada = valor;
            actualizarTexto();
            repaint();
        }

        void setEncima(boolean valor) {
            encima = valor;
            actualizarTexto();
            repaint();
        }

        private void actualizarTexto() {
            setForeground(seleccionada ? acento : encima ? Tema.TEXTO : Tema.TENUE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics g2 = g.create();
            if (seleccionada || encima) {
                g2.setColor(conAlfa(acento, seleccionada ? 45 : 22));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            g2.setColor(conAlfa(acento, seleccionada ? 255 : 80));
            g2.fillRect(0, getHeight() - 3, getWidth(), 3);
            g2.dispose();
            super.paintComponent(g);
        }

        private static Color conAlfa(Color color, int alfa) {
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), alfa);
        }
    }
}