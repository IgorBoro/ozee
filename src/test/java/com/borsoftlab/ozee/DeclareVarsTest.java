package com.borsoftlab.ozee;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class DeclareVarsTest {

    static String program0
                        = "int i";
    static String message0 
                        = "int i"
                        + "\n"
                        + "     ^"
                        + "\n" 
                        + "Error in line 1: expected '=' or ';'"
                        + "\n";
        

    final static String program1 
                        = "int i;";
    final static String message1
                        = "Ok";

    final static String program2
                        = "int i=";
    final static String message2
                        = "int i="
                        + "\n"
                        + "      ^"
                        + "\n" 
                        + "Error in line 1: expected ';'"
                        + "\n";
    
    final static String program3 
                        = "int i=5;";
    final static String message3
                        = "Ok";

    final static String program4 
                        = "int id = 180; // comment"
                        + "\n"
                        + "int j = id;"
                        + "\n"
                        + "byte l;"
                        + "\n"
                        + "int t12;"
                        + "\n"
                        + "float g;"
                        + "\n"
                        + "int k= 17 + j + t12;"
                        + "\n"
                        + "/*"
                        + "\n"
                        + " * comment"
                        + "\n"
                        + " */"
                        + "\n"
                        + "byte b = 45;"
                        + "\n"
                        + "float f = 0.523 * 12.3 - 41.6/32 * (32 + 76) + j;";
    final static String message4
                        = "Ok";

    private final String programText;
    private final String messageText;

    OzParser parser   = new OzParser();
    OzScanner scanner = new OzScanner();

    public DeclareVarsTest(final String program, final String message) {
        programText = program;
        messageText = message;
    }

    @Parameterized.Parameters(name = "{index} : ")
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(new Object[][] { 
            { program0, message0 },
            { program1, message1 }, 
            { program2, message2 }, 
            { program3, message3 }, 
            { program4, message4 }, 
        });
    }

    @Test
    public void test() {
        System.out.println("::------------------------------------------::");
        try {     
            final InputStream programStream = new ByteArrayInputStream(programText.getBytes());
            try {
                final OzText text = new OzText(programStream);
                scanner.resetText(text);
                parser.compile(scanner);
            } catch (final Exception e) {
            } finally {
                System.out.println(OzCompileError.messageString);
                try {
                    programStream.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        String actual = OzCompileError.messageString.toString();
//        assertEquals(messageText, actual);
        boolean b = actual.equals(messageText);
        assertTrue( b );
    }
}


    