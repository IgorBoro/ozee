package com.borsoftlab.oZee;

import java.io.IOException;
import java.io.InputStream;

public class OzText {

   static final int  TABSIZE = 4;    
   InputStream file;
   int lookAheadChar;
   StringBuilder buffer = new StringBuilder();
   boolean bufferCleanFlag = false;

   public Location loc = new Location();

    public OzText(InputStream file){
        this.file = file;
        loc.pos = 0;
        loc.line = 1;
    }

	public void nextChar() {
      if( bufferCleanFlag ){
         buffer.setLength(0);
         bufferCleanFlag = false;
      }
      try {
         if(( lookAheadChar = file.read() ) == -1 )
            lookAheadChar = '\0';
         else if( lookAheadChar == '\n' ) {
            bufferCleanFlag = true;
            loc.line++;
            loc.pos = 0;
         } else if( lookAheadChar == '\r' )
            nextChar();
         else if( lookAheadChar != '\t' ) {
            buffer.append((char)lookAheadChar);
            loc.pos++;
         } else {
            do
               buffer.append(' ');
            while( ++loc.pos % TABSIZE != 0 );
         }
      } catch (IOException e) {};     
    }
}