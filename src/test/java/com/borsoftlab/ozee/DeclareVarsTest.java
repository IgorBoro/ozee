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

    final static String program = "int i";
    final static String error = "\n\n"
                       + "int i"
                       + "\n"
                       + "     ^"
                       + "\n" 
                       + "Error in line 1: expected '=' or ';'"
                       + "\n";
        

    private final String programText;
    private final String messageText;

    InputStream programStream = new ByteArrayInputStream(program.getBytes());

    OzParser parser = new OzParser();
    OzScanner scanner = new OzScanner();

    public DeclareVarsTest(final String program, final String message) {
        programText = program;
        messageText = message;
    }

    @org.junit.runners.Parameterized.Parameters
    public static Collection getParameters() {
        return Arrays.asList(new Object[][] { { program, error } });
    }

    @Before
    public void setup() {
        programStream = new ByteArrayInputStream(programText.getBytes());

        parser = new OzParser();
        scanner = new OzScanner();
    }

    @Test(expected = Exception.class)
    public void test() throws Exception {
        try {
            final OzText text = new OzText(programStream);
            scanner.resetText(text);
            parser.compile(scanner);
        } catch (final Exception e) {
            System.out.println(OzCompileError.errorString);
            throw e;
        } finally {
        }
    }

    @Test
    public void test2() {
        try {
            final OzText text = new OzText(programStream);
            scanner.resetText(text);
            parser.compile(scanner);
        } catch (final Exception e) {
            // System.out.println(OzCompileError.errorString);
        } finally {
        }
        assertTrue(OzCompileError.errorString.toString().equals(messageText));
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
    