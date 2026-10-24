package com.cunoc.proyecto2_promptzal_analisadorsintactico.vista;

import java.awt.Color;
import javax.swing.UIManager;

final class Tema {

    static final Color FONDO = new Color(0x0A0D14);
    static final Color PANEL = new Color(0x111827);
    static final Color PANEL_CLARO = new Color(0x172033);
    static final Color BORDE = new Color(0x243049);
    static final Color TEXTO = new Color(0xDBE4FF);
    static final Color TENUE = new Color(0x8393B8);
    static final Color CIAN = new Color(0x00E5FF);
    static final Color VERDE = new Color(0x39FF88);
    static final Color MAGENTA = new Color(0xFF3D9A);
    static final Color AMBAR = new Color(0xFFB300);
    static final Color VIOLETA = new Color(0xB07CFF);
    static final Color LIMA = new Color(0xC8FF6E);
    static final Color AMARILLO = new Color(0xFFD166);
    static final Color BASE = new Color(0x1F3A5F);
    static final Color SELECCION = new Color(0x00697D);
    static final Color PESTANA_OK = new Color(0x14532D);
    static final Color PESTANA_ERROR = new Color(0x7F1D1D);

    private static boolean aplicado;

    private Tema() {
    }

    static void aplicar() {
        if (aplicado) return;
        aplicado = true;
        UIManager.put("control", PANEL);
        UIManager.put("info", PANEL_CLARO);
        UIManager.put("text", TEXTO);
        UIManager.put("nimbusBase", BASE);
        UIManager.put("nimbusLightBackground", FONDO);
        UIManager.put("nimbusFocus", CIAN);
        UIManager.put("nimbusSelectionBackground", SELECCION);
        UIManager.put("nimbusSelectedText", Color.WHITE);
        UIManager.put("nimbusDisabledText", TENUE);
        UIManager.put("Table.background", FONDO);
        UIManager.put("Table.alternateRowColor", PANEL);
        UIManager.put("Table.foreground", TEXTO);
        UIManager.put("Table.gridColor", BORDE);
        UIManager.put("TextArea.background", FONDO);
        UIManager.put("TextArea.foreground", TEXTO);
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ex) {
            // si Nimbus no está disponible se conserva el aspecto por defecto
        }
    }
}