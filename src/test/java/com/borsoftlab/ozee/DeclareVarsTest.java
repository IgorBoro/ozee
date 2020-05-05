package com.borsoftlab.ozee;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class DeclareVarsTest {

    final static String program1
                        = "int i";
    final static String message1 
                        = "int i"
                        + "\n"
                        + "     ^"
                        + "\n" 
                        + "Error in line 1: expected '=' or ';'"
                        + "\n";
        

    final static String program2 
                        = "int i;";
    final static String message2
                        = "Ok";

    final static String program3
                        = "int i=";
    final static String message3
                        = "int i="
                        + "\n"
                        + "      ^"
                        + "\n" 
                        + "Error in line 1: expected ';'"
                        + "\n";
    

    private final String programText;
    private final String messageText;

    InputStream programStream;

    OzParser parser = new OzParser();
    OzScanner scanner = new OzScanner();

    public DeclareVarsTest(final String program, final String message) {
        programText = program;
        messageText = message;
    }

    @org.junit.runners.Parameterized.Parameters
    public static Collection<Object[]> getParameters() {
        return Arrays.asList(new Object[][] { 
            { program1, message1 },
            { program2, message2 }, 
            { program3, message3 }, 
        });
    }

    @Before
    public void setup() {
        programStream = new ByteArrayInputStream(programText.getBytes());

    }

    @Test
    public void test() {
        System.out.println("<-------------------------->");
        try {
            final OzText text = new OzText(programStream);
            scanner.resetText(text);
            parser.compile(scanner);
        } catch (final Exception e) {
        } finally {
            System.out.println(OzCompileError.messageString);
        }
        assertTrue(OzCompileError.messageString.toString().equals(messageText));
    }

    @After
    public void close() {
        try {
            programStream.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
}

}
    