package com.borsoftlab.ozee;

public class OzCompileError {
    static boolean isError = false;
    static StringBuilder messageString = new StringBuilder();

    static void reset(){
        isError = false;
        messageString.setLength(0);
    }

    static void message( final OzScanner scanner, final String msg ) throws Exception {
        final OzText text = scanner.text;
        isError = true;
        while( text.lookAheadChar != '\n' && text.lookAheadChar != '\0' ) {
            text.nextChar();
        }
        messageString.append("\n\n");
        messageString.append(text.buffer);
        messageString.append('\n');
        for( int i = 1; i < scanner.lexemeLoc.pos; i++ )
            messageString.append(' ');
        messageString.append('^');
        messageString.append('\n');
        messageString.append("Error in line ");
        messageString.append(scanner.lexemeLoc.line);
        messageString.append(": ");
        messageString.append(msg);
        messageString.append('\n');
        throw new Exception(messageString.toString());
    }

    static void expected( final OzScanner scanner, final String msg) throws Exception {
        message(scanner, "expected " + msg);
     }
     
    static void warning(final String msg) {
        System.out.println();
        System.out.println("warning: " + msg);
    }
}