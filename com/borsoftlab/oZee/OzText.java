package com.borsoftlab.oZee;

import java.io.IOException;
import java.io.InputStream;

public class OzText {

    static final int  TABSIZE = 4;    

    InputStream file;
    int lookAheadChar;
    public Location loc = new Location();

    public OzText(InputStream file){
        this.file = file;
        loc.pos = 0;
        loc.line = 1;
        loc.lexemeCount = 0;
    }

	public void nextChar() {
        try {
            if(( lookAheadChar = file.read() ) == -1 )
                lookAheadChar = '\0';
            else if( lookAheadChar == '\n' ) {
               System.out.println();
               loc.line++;
               loc.pos = 0;
               lookAheadChar = '\n';
            } else if( lookAheadChar == '\r' )
               nextChar();
            else if( lookAheadChar != '\t' ) {
               System.out.write(lookAheadChar);
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
        int lexemeCount;
     }
     
}