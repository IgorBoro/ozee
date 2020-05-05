package com.borsoftlab.ozee;

import org.junit.Test;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.assertTrue;

public class DeclareVarsTest {
    
    String program = "int i";
    InputStream programStream = new ByteArrayInputStream(program.getBytes());

    OzParser parser = new OzParser();
    OzScanner scanner = new OzScanner();

    @Before
    public void setup() {
        programStream = new ByteArrayInputStream(program.getBytes());

        parser = new OzParser();
        scanner = new OzScanner();
    }
        
    @Test(expected = Exception.class)
    public void test() throws Exception{
        try {
            OzText text = new OzText(programStream);
            scanner.resetText(text);
            parser.compile(scanner);
        } catch (Exception e) {
            System.out.println(OzCompileError.errorString);
            throw e;
        } finally {
        }
    }

    final String error = "\n\n"
                       + "int i"
                       + "\n"
                       + "     ^"
                       + "\n" 
                       + "Error in line 1: expected '=' or ';'"
                       + "\n";

    @Test
    public void test2(){
        try {
            OzText text = new OzText(programStream);
            scanner.resetText(text);
            parser.compile(scanner);
        } catch (Exception e) {
            // System.out.println(OzCompileError.errorString);
        } finally {
        }
        assertTrue( OzCompileError.errorString.toString().equals(error) );
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
    