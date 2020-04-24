package com.borsoftlab.oZee;

import java.io.IOException;
import java.io.InputStream;

public class Text {

    static final int  TABSIZE = 4;    

    InputStream file;
    int cchar;
    public Location loc = new Location();

    public Text(InputStream file){
        this.file = file;
        loc.pos = 0;
        loc.line = 1;
    }

	public int nextChar() {
        try {
            if( (cchar = file.read()) == -1 )
                cchar = '\0';
            else if( cchar == '\n' ) {
               System.out.println();
               loc.line++;
               loc.pos = 0;
               cchar = '\n';
            } else if( cchar == '\r' )
               nextChar();
            else if( cchar != '\t' ) {
               System.out.write(cchar);
               loc.pos++;
            } else {
               do
                  System.out.print(' ');
               while( ++loc.pos % TABSIZE != 0 );
            }
         } catch (IOException e) {};     
         return cchar;
    }
    
    class Location {
        int line;    // Номер строки           
        int pos;     // Номер символа в строке 
        int lexemePos;  // Позиция начала лексемы 
     }
     
}