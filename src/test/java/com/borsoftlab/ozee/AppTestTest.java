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

    final OzParser parser = new OzParser();
    final OzScanner scanner = new OzScanner();

    @Before
    public void setup() {

        try {
            final OzText text = new OzText(programStream);
            scanner.resetText(text);
        
        } catch (Throwable e) {
            e.printStackTrace();
        }        
    }
        
    @Test
    public void test() {
        try {
            parser.compile(scanner);
        } catch (Exception e) {
      //      e.printStackTrace();
            System.out.println(OzCompileError.errorString);
            assert(false);
        } finally {
        }
        assert(true);
    }

    @Test
    public void test2() {
        try {
            parser.compile(scanner);
        } catch (Exception e) {
    //        e.printStackTrace();
            System.out.println(OzCompileError.errorString);
            assert(false);
        } finally {
        }
        assert(true);
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
    