package com.borsoftlab.oZee;

public class OzCompileError {
    
    static void message( final OzText text, final String msg ){
        int nLine = text.loc.line;
        while( text.lookAheadChar != '\n' && text.lookAheadChar != '\0' ) {
            text.nextChar();
        }
        if( text.lookAheadChar == '\0' )
            System.out.println();
        for( int i = 1; i < text.loc.lexemePos; i++ )
            System.out.print(' ');
        System.out.println("^");
        System.out.println("[Line " + nLine + "] Error: " + msg);
        System.out.println();
        System.exit(0);        
    }
}