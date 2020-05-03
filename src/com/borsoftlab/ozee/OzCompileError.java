package com.borsoftlab.ozee;

public class OzCompileError {
    
    static void message( final OzScanner scanner, final String msg ){
        OzText text = scanner.text;
        while( text.lookAheadChar != '\n' && text.lookAheadChar != '\0' ) {
            text.nextChar();
        }
        if( text.lookAheadChar == '\0' )
            System.out.println();
        System.out.println(text.buffer.toString());    
            for( int i = 1; i < scanner.lexemeLoc.pos; i++ )
            System.out.print(' ');
        System.out.println("^");
        System.out.println("Error in line " + scanner.lexemeLoc.line + ": " + msg);
        System.out.println();
        System.exit(0);        
    }

    static void expected( final OzScanner scanner, final String msg) {
        message(scanner, "expected " + msg);
     }
     
    static void warning(final String msg) {
        System.out.println();
        System.out.println("warning: " + msg);
    }
}