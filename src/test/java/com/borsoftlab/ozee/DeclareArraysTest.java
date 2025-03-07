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
                        = "int [ ] i;";
    static String message0 
                        = "Ok";

    static String program1
                        = "int[] i;" + "\n"
                        + "int[] a;" + "\n"
                        + "short i;";
    static String message1 
                        = "short i;"  + '\n'
                        + "      ^"   + '\n'
                        + "Error in line 3: name 'i' already defined" + '\n';

    static String program2
                        = "int[] i;" + "\n"
                        + "short[  ] s;" + "\n"
                        + "ushort[] us;" + "\n"
                        + "byte [] b;" + "\n"
                        + "ubyte [ ]  ub;" + "\n"
                        + "float [] f;";
    static String message2 
                        = "Ok";

    final static String program3
                        = "int[] i = int[15];";
    final static String message3
                        = "Ok";

    final static String program4
                        = "int[] i = 15;";
    final static String message4
                        = "int[] i = 15;" + "\n"
                        + "          ^"   + "\n"
                        + "Error in line 1: expected array definition" + "\n";

    final static String program5
                        = "int[] i;" + "\n"
                        + "i = int[15];";
    final static String message5
                        = "Ok";

    final static String program6
                        = "int[] i = int[15];";
    final static String message6
                        = "Ok";

    final static String program7
                        = "int i;" + "\n"
                        + "i = int[15];";
    final static String message7
                        = "i = int[15];" + "\n"
                        + "    ^"        + "\n"
                        + "Error in line 2: expected scalar type" + "\n";

    final static String program8 
                        = "int i = int[15];";
    final static String message8
                        = "int i = int[15];" + "\n"
                        + "        ^"        + "\n"
                        + "Error in line 1: expected scalar type" + "\n";

    final static String program9
                        = "int[] i;" + "\n"
                        + "i = short[15];";
    final static String message9
                        = "i = short[15];" + "\n"
                        + "    ^"          + "\n"
                        + "Error in line 2: incompatible array types" + "\n";

    final static String program10 
                        = "int[] i = int[a];";
    final static String message10
                        = "int[] i = int[a];" + "\n"
                        + "              ^"   + "\n"
                        + "Error in line 1: expected a positive integer number for array size" + "\n";

    final static String program11
                        = "int [ ] intArray = int[ 256 ];";
    final static String message11
                        = "Ok";

    final static String program12
                        = "int [ ] intArray = int[ -256 ];";
    final static String message12
                        = "int [ ] intArray = int[ -256 ];" + "\n"
                        + "                        ^"       + "\n"
                        + "Error in line 1: expected a positive integer number for array size" + "\n";

    final static String program13
                        = "int [ ] intArray = int[ 0 ];";
    final static String message13
                        = "int [ ] intArray = int[ 0 ];" + "\n"
                        + "                        ^"    + "\n"
                        + "Error in line 1: expected an integer above zero for array size" + "\n";

    final static String program14
                        = "int [ ] intArray = int[ 2.56 ];";
    final static String message14
                        = "int [ ] intArray = int[ 2.56 ];" + "\n"
                        + "                        ^"       + "\n"
                        + "Error in line 1: expected a positive integer number for array size" + "\n";

    final static String program15
                        = "int[] i = int[ 2 * 8 ];";
    final static String message15
                        = "int[] i = int[ 2 * 8 ];" + "\n"
                        + "                 ^"      + "\n"
                        + "Error in line 1: unexpected lexeme" + "\n";

    final static String program16
                        = "int[] i = int[ 16 ];";
    final static String message16
                        = "Ok";

    final static String program17
                        = "int[] i[16];" + "\n"
                        + "int[] j[16];" + "\n"
                        + "i = j;";
    final static String message17
                        = "i = j;" + "\n"
                        + "  ^"    + "\n"
                        + "Error in line 3: array 'i' already defined" + "\n";

    final static String program18
                        = "int[] i[16];" + "\n"
                        + "int[] j = i;";
    final static String message18
                        = "Ok";


    final static String program19
                        = "int[] i[16];" + "\n"
                        + "int[] j;"     + "\n"
                        + "i = j;";
    final static String message19
                        = "i = j;" + "\n"
                        + "  ^"    + "\n"
                        + "Error in line 3: array 'i' already defined" + "\n";

    final static String program20
                        = "int[] i;" + "\n"
                        + "int[] j;"     + "\n"
                        + "i = j;";
    final static String message20
                        = "i = j;" + "\n"
                        + "    ^"    + "\n"
                        + "Error in line 3: array 'j' undefined" + "\n";

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
            Arguments.of( program3, message3 ),
            Arguments.of( program4, message4 ),
            Arguments.of( program5, message5 ),
            Arguments.of( program6, message6 ),
            Arguments.of( program7, message7 ),
            Arguments.of( program8, message8 ),
            Arguments.of( program9, message9 ),
            Arguments.of( program10, message10 ),
            Arguments.of( program11, message11 ),
            Arguments.of( program12, message12 ),
            Arguments.of( program13, message13 ),
            Arguments.of( program14, message14 ),
            Arguments.of( program15, message15 ),
            Arguments.of( program16, message16 ),
            Arguments.of( program17, message17 ),
            Arguments.of( program18, message18 ),
            Arguments.of( program19, message19 ),
            Arguments.of( program20, message20 )
        );
    }
}   