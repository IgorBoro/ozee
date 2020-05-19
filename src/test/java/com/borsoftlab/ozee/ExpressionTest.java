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
public class ExpressionTest {

    final static String program0 
                        = "float g = 187 * 10.0;"    + '\n'
                        + "float p = 7.7;"           + '\n'
                        + "float f = 0.523 * 12.3 + g - 41.6/32 * (p + 76);";
    final static String message0
                        = "Ok";


    final static String program1 
                        = "int i = 1 + 2    3 + 4;";
    final static String message1
                        = "int i = 1 + 2    3 + 4;"   + "\n"
                        + "                 ^"        + "\n"
                        + "Error in line 1: unexpected lexeme" + "\n";


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
            Arguments.of( program1, message1 )
        );
    }
}


    