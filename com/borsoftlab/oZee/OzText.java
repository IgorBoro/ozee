package com.borsoftlab.oZee;

import java.io.IOException;
import java.io.InputStream;

public class OzText {

    static final int  TABSIZE = 4;    

    InputStream file;
    int lookAhead;
    public Location loc = new Location();

    public OzText(InputStream file){
        this.file = file;
        loc.pos = 0;
        loc.line = 1;
    }

	public void nextChar() {
        try {
            if(( lookAhead = file.read() ) == -1 )
                lookAhead = '\0';
            else if( lookAhead == '\n' ) {
               System.out.println();
               loc.line++;
               loc.pos = 0;
               lookAhead = '\n';
            } else if( lookAhead == '\r' )
               nextChar();
            else if( lookAhead != '\t' ) {
               System.out.write(lookAhead);
               loc.pos++;
            } else {
               do
                  System.out.print(' ');
               while( ++loc.pos % TABSIZE != 0 );
            }
         } catch (IOException e) {};     
    }
    
    class Location {
        int line;    // Номер строки           
        int pos;     // Номер символа в строке 
        int lexemePos;  // Позиция начала лексемы 
     }
     
}