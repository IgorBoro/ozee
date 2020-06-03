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
public class DefineArraysTest {

    OzParser parser   = new OzParser();
    OzScanner scanner = new OzScanner();

    @ParameterizedTest(name="{index}")
    @MethodSource("argumentProvider")
    public void test(String program, String message) {

//        float value = Float.MIN_VALUE;


        System.out.println("::------------------------------------------::");
        try {     
            final InputStream programStream = new ByteArrayInputStream(program.getBytes());
            try {
                final OzText text = new OzText(programStream);
                scanner.resetText(text);
                parser.compile(scanner);

                final OzVm vm = new OzVm();
//                vm.setDebugListener(debugListener);
                byte[] compiledProgram = parser.getProgramImage();
                byte[] programImage = OzLinker.linkImage(compiledProgram, scanner.symbolTable);
                scanner.symbolTable.dumpSymbolTableByName();
                vm.loadProgram(programImage);
                System.out.println("\noZee virtual machine started...");
                long startMillis = System.currentTimeMillis();
                vm.execute();
                long execTime = System.currentTimeMillis() - startMillis;
                System.out.println("oZee virtual machine stopped");
                System.out.println("Execution time: " + execTime + " ms");
        
                OzUtils.printMemoryDump(vm.getRam(), 0, programImage.length);
//                int valueAddr = scanner.symbolTable.lookup("r").allocAddress;
//                value = OzUtils.fetchFloatFromByteArray(vm.getRam(), valueAddr);
//                System.out.println("r = " + value);

            } catch (final Exception e) {
                // e.printStackTrace();
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

    // -----------------------------------------------------------------------                        
    static String program0
            = "int[] i = int[16];";
    static String message0 
            = "Ok";

    static String program1
            = "int[] vv[16];";
    static String message1 
            = "Ok";

    static String program2
            = "int[] vv[16] = int[16];";
    static String message2 
            = "int[] vv[16] = int[16];" + "\n"
            + "             ^"          + "\n"
            + "Error in line 1: expected ';'" + "\n";
    // -----------------------------------------------------------------------                        

    private static Stream<Arguments> argumentProvider() {
        return Stream.of(
//            Arguments.of( program0, message0 ),
//            Arguments.of( program1, message1 ),
            Arguments.of( program2, message2 )
        );
    }
} 