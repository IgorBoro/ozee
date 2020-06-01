package com.borsoftlab.ozee;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

@Nested
@DisplayName("Test class")
public class DeclareArraysTest {

    static String program0
                        = "int[] i;";
    static String message0 
                        = "Ok";

    static String program1
                        = "int[] i;" + "\n"
                        + "short i;";
    static String message1 
                        = "short i;"  + '\n'
                        + "      ^"   + '\n'
                        + "Error in line 2: name 'i' already defined" + '\n';

    static String program2
                        = "int[] i;" + "\n"
                        + "short[] s;" + "\n"
                        + "ushort[] us;" + "\n"
                        + "byte[] b;" + "\n"
                        + "ubyte[] ub;" + "\n"
                        + "float[] f;";
    static String message2 
                        = "Ok";

    final static String program3
                        = "int[] i = int[15];";
    final static String message3
                        = "Ok";
                        
// -----------------------------------------------------------------------                        


    OzParser parser   = new OzParser();
    OzScanner scanner = new OzScanner();

    @ParameterizedTest(name="{index}")
    @MethodSource("argumentProvider")
    public void test(String program, String message) {
        System.out.println("::------------------------------------------::");
        try {     
            final InputStream programStream = new ByteArrayInputStream(program.getBytes());
            try {
                final OzText text = new OzText(programStream);
                scanner.resetText(text);
                parser.compile(scanner);
                scanner.symbolTable.dumpSymbolTableByName();
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
        assertEquals(message, OzCompileError.messageString.toString());
    }

    private static Stream<Arguments> argumentProvider() {
        return Stream.of(
            Arguments.of( program0, message0 ),
            Arguments.of( program1, message1 ),
            Arguments.of( program2, message2 ),
            Arguments.of( program3, message3 )
//            Arguments.of( program4, message4 ),
//            Arguments.of( program5, message5 ),
//            Arguments.of( program6, message6 ),
//            Arguments.of( program7, message7 ),
//            Arguments.of( program8, message8 ),
//            Arguments.of( program9, message9 ),
//            Arguments.of( program10, message10 ),
//            Arguments.of( program11, message11 ),
//            Arguments.of( program12, message12 ),
//            Arguments.of( program13, message13 ),
//            Arguments.of( program14, message14 )
        );
    }
}   