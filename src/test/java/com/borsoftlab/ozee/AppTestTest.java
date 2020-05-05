package com.borsoftlab.ozee;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.After;
import org.junit.Before;
    
public class AppTestTest {
    
    String program = "int i";
    InputStream programStream = new ByteArrayInputStream(program.getBytes());

    OzParser parser;

    @Before
    public void setup(){

        try {
            final OzText text = new OzText(programStream);
            final OzScanner scanner = new OzScanner(text);
            parser = new OzParser(scanner);
        
        } catch (Throwable e) {
            e.printStackTrace();
        }        
    }
        
    @Test
    public void test() {
        try {
            parser.compile();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    @After
    public void close(){
        try {
            programStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
}

}
    