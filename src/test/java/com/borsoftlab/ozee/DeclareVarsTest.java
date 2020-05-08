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
public class DeclareVarsTest {

    static String program0
                        = "int i ";
    static String message0 
                        = "int i "   + '\n'
                        + "      ^"  + '\n'
                        + "Error in line 1: unexpected EOF" + '\n';

    final static String program1 
                        = "int i;";
    final static String message1
                        = "Ok";

    final static String program2
                        = "int i=";
    final static String message2
                        = "int i="  + '\n'
                        + "      ^" + '\n'
                        + "Error in line 1: unexpected EOF"   + '\n';

    final static String program3
                        = "int i=;";
    final static String message3
                        = "int i=;"  + '\n'
                        + "      ^"  + '\n'
                        + "Error in line 1: unexpected lexeme"   + '\n';
                        
    final static String program4 
                        = "int i=5;";
    final static String message4
                        = "Ok";

    final static String program5 
                        = "int id = 180; // comment"   + '\n'
                        + "int j = id;"                + '\n'
                        + "byte l;"                    + '\n'
                        + "int t12;"                   + '\n'
                        + "float g;"                   + '\n'
                        + "int k= 17 + j + t12;"       + '\n'
                        + "/*"                         + '\n'
                        + " * comment"                 + '\n'
                        + " */"                        + '\n'
                        + "byte b = 45;"               + '\n'
                        + "float f = 0.523 * 12.3 - 41.6/32 * (32 + 76) + j;";
//                        + "float f = 12 + 41. 6/32 * (32 + 76) + j;";
    final static String message5
                        = "Ok";

    static String program6
                        = "float ff=45. 6;";
    static String message6 
                        = "float ff=45. 6;"  + '\n'
                        + "            ^"    + '\n'
                        + "Error in line 1: unexpected symbol"   + '\n';

    static String program7
                        = "float ff=45.6;";
    static String message7 
                        = "Ok";

    final static String program8
                        = "int i= ;";
    final static String message8
                        = "int i= ;"  + '\n'
                        + "       ^"  + '\n'
                        + "Error in line 1: unexpected lexeme"   + '\n';

    static String program9
                        = "int i + ";
    static String message9 
                        = "int i + "   + '\n'
                        + "      ^"    + '\n'
                        + "Error in line 1: expected '=' or ';'" + '\n';


    final static String program10 
                        = "float ;";
    final static String message10
                        = "float ;"  + '\n'
                        + "      ^"  + '\n'
                        + "Error in line 1: expected variable name" + '\n';

    final static String program11 
                        = "float +";
    final static String message11
                        = "float +"  + '\n'
                        + "      ^"  + '\n'
                        + "Error in line 1: expected variable name" + '\n';


    static String program12
                        = "int d=4r56;";
    static String message12 
                        = "int d=4r56;"  + '\n'
                        + "       ^"     + '\n'
                        + "Error in line 1: unexpected lexeme" + '\n';

    static String program13
                        = "int d=4 56;";
    static String message13 
                        = "int d=4 56;"  + '\n'
                        + "        ^"    + '\n'
                        + "Error in line 1: unexpected lexeme" + '\n';

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
                scanner.symbolTable.dumpSymbolTable();
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
            Arguments.of( program13, message13 )
        );
    }
}


    