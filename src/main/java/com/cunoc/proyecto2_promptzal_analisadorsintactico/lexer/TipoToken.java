/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cunoc.proyecto2_promptzal_analisadorsintactico.lexer;

/**
 *
 * @author jppax
 */
public enum TipoToken {
    DIRECTIVA,
    AGENTE, CONTEXTO, VARIABLE, EJECUTAR, EXPORTAR,
    COMANDO_IA, CARGAR,
    CONECTOR, FLECHA,
    IGUAL, MAS,
    LLAVE_A, LLAVE_C, PAR_A, PAR_C, COMA,
    ID, CADENA, NUMERO,
    EOF
}
