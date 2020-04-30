package com.borsoftlab.oZee;

public class OzCompileError {
    
    static void message( final OzText text, final String msg ){
        int nLine = text.loc.lexemeLine;
        while( text.lookAheadChar != '\n' && text.lookAheadChar != '\0' ) {
            text.nextChar();
        }
        if( text.lookAheadChar == '\0' )
            System.out.println();
        System.out.println(text.buffer.toString());    
            for( int i = 1; i < text.loc.lexemePos; i++ )
            System.out.print(' ');
        System.out.println("^");
        System.out.println("Error in line " + nLine + ": " + msg);
        System.out.println();
        System.exit(0);        
    }

    static void expected( final OzText text, final String msg) {
        message(text, "expected " + msg);
     }
     
    static void warning(final String msg) {
        System.out.println();
        System.out.println("warning: " + msg);
    }
}