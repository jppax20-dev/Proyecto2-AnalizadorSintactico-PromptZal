package com.cunoc.proyecto2_promptzal_analisadorsintactico.lexer;

import java.util.List; 
import com.cunoc.proyecto2_promptzal_analisadorsintactico.errores.ErrorCompilacion; 

//aqui estan los %%
%%

%public
%class Lexer
%unicode
%line
%column
%type Token
%xstate COMENTARIO

%{
    private List<ErrorCompilacion> errores;

    public Lexer(java.io.Reader in, List<ErrorCompilacion> errores) {
        this(in);
        this.errores = errores;
    }

    private Token token(TipoToken tipo) {
        return new Token(tipo, yytext(), yyline + 1, yycolumn + 1);
    }

    private void error(String descripcion) {
        errores.add(ErrorCompilacion.lexico(descripcion, yyline + 1, yycolumn + 1));
    }
    
    private int comFila;
    private int comColumna;
%}

LETRA      = [\p{L}_]
DIGITO     = [0-9]
ID         = {LETRA}({LETRA}|{DIGITO})*
NUMERO     = {DIGITO}+(\.{DIGITO}+)?
CADENA     = \"[^\"\r\n]*\"
ESPACIO    = [ \t\r\n]+
COM_LINEA  = "//"[^\r\n]*
//%%
%%

"@modelo" | "@rol" | "@formato"   { return token(TipoToken.DIRECTIVA); }

"AGENTE"     { return token(TipoToken.AGENTE); }
"contexto"   { return token(TipoToken.CONTEXTO); }
"variable"   { return token(TipoToken.VARIABLE); }
"EJECUTAR"   { return token(TipoToken.EJECUTAR); }
"EXPORTAR"   { return token(TipoToken.EXPORTAR); }
"CARGAR"     { return token(TipoToken.CARGAR); }

"PREGUNTAR" | "GENERAR" | "RESUMIR" | "ANALIZAR" |
"TRADUCIR" | "CLASIFICAR" | "EXTRAER"   { return token(TipoToken.COMANDO_IA); }

"SOBRE" | "DESDE" | "EN" | "COMO"   { return token(TipoToken.CONECTOR); }

"->"   { return token(TipoToken.FLECHA); }
"="    { return token(TipoToken.IGUAL); }
"+"    { return token(TipoToken.MAS); }
"{"    { return token(TipoToken.LLAVE_A); }
"}"    { return token(TipoToken.LLAVE_C); }
"("    { return token(TipoToken.PAR_A); }
")"    { return token(TipoToken.PAR_C); }
","    { return token(TipoToken.COMA); }

{ID}       { return token(TipoToken.ID); }
{NUMERO}   { return token(TipoToken.NUMERO); }
{CADENA}   { return token(TipoToken.CADENA); }

{ESPACIO} | {COM_LINEA}   { }

"/*"   { comFila = yyline + 1; comColumna = yycolumn + 1; yybegin(COMENTARIO); }

<COMENTARIO> {
    "*/"    { yybegin(YYINITIAL); }
    [^]     { }
    <<EOF>> {
        errores.add(ErrorCompilacion.lexico("Comentario de bloque sin cerrar", comFila, comColumna));
        yybegin(YYINITIAL);
        return new Token(TipoToken.EOF, "", yyline + 1, yycolumn + 1);
    }
}

\"[^\"\r\n]*   { error("Cadena sin cerrar"); }
[^]            { error("Carácter no reconocido: '" + yytext() + "'"); }

<<EOF>>   { return new Token(TipoToken.EOF, "", yyline + 1, yycolumn + 1); }

