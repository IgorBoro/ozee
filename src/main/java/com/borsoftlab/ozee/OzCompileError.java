package com.borsoftlab.ozee;

public class OzCompileError {
    static boolean isError = false;
    static StringBuilder messageString = new StringBuilder();

    static void reset(){
        isError = false;
        messageString.setLength(0);
        messageString.append("Ok");
    }

    static void message( final OzScanner scanner, final String msg, final Location loc )
        throws Exception {
        final OzText text = scanner.text;
        messageString.setLength(0);
        isError = true;
        Location l = new Location(loc);
        while( text.lookAheadChar != '\n' && text.lookAheadChar != '\0' ) {
            text.nextChar();
        }
        messageString.append(text.buffer);
        messageString.append('\n');
        for( int i = 1; i < l.pos; i++ )
            messageString.append(' ');
        messageString.append('^');
        messageString.append('\n');
        messageString.append("Error in line ");
        messageString.append(l.line);
        messageString.append(": ");
        messageString.append(msg);
        messageString.append('\n');
        throw new Exception(messageString.toString());
    }

    static void expected( final OzScanner scanner, final String msg, final Location loc )
    throws Exception {
        message(scanner, "expected " + msg, loc);
     }
     
    static void warning(final String msg) {
        System.out.println();
        System.out.println("warning: " + msg);
    }
}