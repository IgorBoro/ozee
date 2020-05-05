package com.borsoftlab.ozee;

public class OzCompileError {
    static StringBuilder errorString = new StringBuilder();

    static void message( final OzScanner scanner, final String msg ) throws Exception {
        final OzText text = scanner.text;
        while( text.lookAheadChar != '\n' && text.lookAheadChar != '\0' ) {
            text.nextChar();
        }
        errorString.append("\n\n");
        errorString.append(text.buffer);
        errorString.append('\n');
        for( int i = 1; i < scanner.text.lexemeLoc.pos; i++ )
            errorString.append(' ');
        errorString.append('^');
        errorString.append('\n');
        errorString.append("Error in line ");
        errorString.append(scanner.lexemeLoc.line);
        errorString.append(": ");
        errorString.append(msg);
        errorString.append('\n');
        throw new Exception(errorString.toString());
    }

    static void expected( final OzScanner scanner, final String msg) throws Exception {
        message(scanner, "expected " + msg);
     }
     
    static void warning(final String msg) {
        System.out.println();
        System.out.println("warning: " + msg);
    }
}