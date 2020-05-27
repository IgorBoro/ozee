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
public class IntegerArithmeticTest {

    final static String program0 
        = "int a = 4;" + '\n'
        + "int b = 5;" + '\n'
        + "int r = a + b;";
    final static int expect0 = 9;

    final static String program1 
        = "int a = 6;" + '\n'
        + "int b = 8;" + '\n'
        + "int r = a - b;";
    final static int expect1 = -2;

    final static String program2 
        = "int r = 1 + 2 + 3 + 4 + 5 + 6;";
    final static int expect2 = 21;

    final static String program3 
        = "int r =  3 * 25;";
    final static int expect3 = 75;

    final static String program4 
        = "int r =  3 * ( 5 + 7);";
    final static int expect4 = 36;

    final static String program5 
        = "int r =  8/4;";
    final static int expect5 = 2;

    final static String program6 
        = "int r =  7/2;";
    final static int expect6 = 3;

    final static String program7
        = "int r =  -32;";
    final static int expect7 = -32;

    final static String program8
        = "int r =  -(9 + 15);";
    final static int expect8 = -24;

    OzParser parser   = new OzParser();
    OzScanner scanner = new OzScanner();

    @ParameterizedTest(name="{index}")
    @MethodSource("argumentProvider")
    public void test(String program, int expect) {
        int value = Integer.MIN_VALUE;
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
                // OzUtils.printMemoryDump(vm.getRam());
                int valueAddr = scanner.symbolTable.lookup("r").allocAddress;
                value = OzUtils.fetchIntFromByteArray(vm.getRam(), valueAddr);
                System.out.println("r = " + value);
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
        assertEquals(expect, value);
    }

    private static Stream<Arguments> argumentProvider() {
        return Stream.of(
                Arguments.of( program0, expect0 ),
                Arguments.of( program1, expect1 ),
                Arguments.of( program2, expect2 ),
                Arguments.of( program3, expect3 ),
                Arguments.of( program4, expect4 ),
                Arguments.of( program5, expect5 ),
                Arguments.of( program6, expect6 ),
                Arguments.of( program7, expect7 ),
                Arguments.of( program8, expect8 )
                );
    }
}        
