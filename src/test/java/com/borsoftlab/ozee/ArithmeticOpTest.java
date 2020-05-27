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
import java.util.List;
import java.util.stream.Stream;

import com.borsoftlab.ozee.OzSymbols.Symbol;

@Nested
@DisplayName("Test class")
public class ArithmeticOpTest {

    final static String program0 
        = "int a = 4;" + '\n'
        + "int b = 5;" + '\n'
        + "int c = a + b;";
    final static String message0
        = "Ok";


    final static String program1 
        = "int i = 1 + 2    3 + 4;";
    final static String message1
        = "int i = 1 + 2    3 + 4;"   + "\n"
        + "                 ^"        + "\n"
        + "Error in line 1: unexpected lexeme" + "\n";

    final static String program2 
        = "int i = 1.2;"  + "\n"
        + "float f = 3;";
    final static String message2
        = "Ok";

    final static String program3 
        = "sbyte w;";
    final static String message3
        = "sbyte w;" + "\n"
        + "^"        + "\n"
        + "Error in line 1: name 'sbyte' not defined" + "\n";

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

                final OzVm vm = new OzVm();
                List<Byte> compiledProgram = parser.getProgramInListArray();
                List<Symbol> symbols = scanner.symbolTable.getTableOrderedByAddr();
                byte[] programImage = OzLinker.linkImage(compiledProgram, symbols);
                scanner.symbolTable.dumpSymbolTableByName();
                vm.loadProgram(programImage);
                vm.execute();
                vm.printMemoryDump();
            } catch (final Exception e) {
                e.printStackTrace();
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
                Arguments.of( program0, message0 )
//                Arguments.of( program1, message1 ),
//                Arguments.of( program2, message2 ),
//                Arguments.of( program3, message3 )
                );
    }
}        
