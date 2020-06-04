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

import com.borsoftlab.ozee.OzVm.OnOzVmDebugListener;

@Nested
@DisplayName("Test class")
public class ArraysArithmeticTest {

    OzParser parser   = new OzParser();
    OzScanner scanner = new OzScanner();

    OnOzVmDebugListener debugListener = new OnOzVmDebugListener(){
    
        @Override
        public void onExecutingCommand(int step, int pc, int cmd, int[] stack, int sp) {
            if( step == OzVm.STEP_BEFORE_EXECUTING ){
                System.out.print(OzAsm.getInstance().getMnemonic(cmd));
            } else if( step == OzVm.STEP_OPTIONAL_ARGUMENT ){
                System.out.print( String.format(" 0x%08X", cmd) );
            } else if( step == OzVm.STEP_AFTER_EXECUTING ){
                System.out.println();                

                System.out.print("[ ");
                for( int ptr = 0; ptr < sp; ptr++ ){
                    int value = stack[ptr];
                    System.out.print(String.format("0x%08X ", value));
                }
                System.out.println("] <- top");
            }
        }
    };


    @ParameterizedTest(name="{index}")
    @MethodSource("floatArgumentProvider")
    public void testFloat(String program, float expect) {
        float value = Float.MIN_VALUE;
        System.out.println("::------------------------------------------::");
        try {     
            final InputStream programStream = new ByteArrayInputStream(program.getBytes());
            try {
                final OzText text = new OzText(programStream);
                scanner.resetText(text);
                parser.compile(scanner);

                final OzVm vm = new OzVm();
                byte[] compiledProgram = parser.getProgramImage();
                byte[] programImage = OzLinker.linkImage(compiledProgram, scanner.symbolTable);
                scanner.symbolTable.dumpSymbolTableByName();
                vm.setDebugListener(debugListener);
                vm.loadProgram(programImage);
                System.out.println("\noZee virtual machine started...");
                long startMillis = System.currentTimeMillis();
                vm.execute();
                long execTime = System.currentTimeMillis() - startMillis;
                System.out.println("oZee virtual machine stopped");
                System.out.println("Execution time: " + execTime + " ms");
        
                OzUtils.printMemoryDump(vm.getRam(), 0, programImage.length );
                OzSymbols.Symbol symbol = scanner.symbolTable.lookup("v");
                if( symbol != null ){
                    int valueAddr = symbol.allocAddress;
                    if( symbol.varType == OzScanner.VAR_TYPE_INT ){
                        value = OzUtils.fetchIntFromByteArray(vm.getRam(), valueAddr);
                        System.out.println("v = " + value);
                    } else {
                        value = OzUtils.fetchFloatFromByteArray(vm.getRam(), valueAddr);
                        System.out.println("v = " + value);
                    }
                }

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
        assertEquals(expect, value);
    }

    @ParameterizedTest(name="{index}")
    @MethodSource("stringArgumentProvider")
    public void testString(String program, String expect) {
        float value = Float.MIN_VALUE;
        System.out.println("::------------------------------------------::");
        try {     
            final InputStream programStream = new ByteArrayInputStream(program.getBytes());
            try {
                final OzText text = new OzText(programStream);
                scanner.resetText(text);
                parser.compile(scanner);

                final OzVm vm = new OzVm();
                byte[] compiledProgram = parser.getProgramImage();
                byte[] programImage = OzLinker.linkImage(compiledProgram, scanner.symbolTable);
                scanner.symbolTable.dumpSymbolTableByName();
                vm.setDebugListener(debugListener);
                vm.loadProgram(programImage);
                System.out.println("\noZee virtual machine started...");
                long startMillis = System.currentTimeMillis();
                vm.execute();
                long execTime = System.currentTimeMillis() - startMillis;
                System.out.println("oZee virtual machine stopped");
                System.out.println("Execution time: " + execTime + " ms");
        
                OzUtils.printMemoryDump(vm.getRam(), 0, programImage.length );
                OzSymbols.Symbol symbol = scanner.symbolTable.lookup("v");
                if( symbol != null ){
                    int valueAddr = symbol.allocAddress;
                    if( symbol.varType == OzScanner.VAR_TYPE_INT ){
                        value = OzUtils.fetchIntFromByteArray(vm.getRam(), valueAddr);
                        System.out.println("v = " + value);
                    } else {
                        value = OzUtils.fetchFloatFromByteArray(vm.getRam(), valueAddr);
                        System.out.println("v = " + value);
                    }
                }

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
        assertEquals(expect,  OzCompileError.messageString.toString());
    }

    // -----------------------------------------------------------------------                        

    static String program0
            = "int[] ari[16];"       + "\n"
            + "ari[7] = 1234567890;" + "\n" 
            + "int v = ari[7];";
    static float expect0 
            = 1234567890;

    static String program1
            = "int[] ari[16];"       + "\n"
            + "ari[7] = 1234567890;" + "\n" 
            + "int index = 4;"       + "\n" 
            + "int v = ari[index + 3];";
    static float expect1 
            = 1234567890;

    static String program2
            = "int[] ari[16];"                + "\n"
            + "int index0 = 2;"               + "\n" 
            + "ari[index0 + 5] = 1234567890;" + "\n" 
            + "int index1 = 4;"               + "\n" 
            + "int v = ari[index1 + 3];";
    static float expect2 
            = 1234567890;

    static String program3
            = "int[] ari[16];"               + "\n"
            + "int index0 = 2;"              + "\n" 
            + "ari[index0 + 5] = 12;"        + "\n" 
            + "int index1 = 4;"              + "\n" 
            + "int v = 6 * ari[index1 + 3] - 8;";
    static float expect3 
            = 64;

    static String program4
            = "int[] ari[16];"                   + "\n"
            + "int index0 = 2;"                  + "\n" 
            + "ari[index0] = 207;"               + "\n" 
            + "int index1 = 4;"                  + "\n" 
            + "ari[index1]  = ari[index0] / 3;"  + "\n"
            + "int v = ari[index1];";
    static float expect4 
            = 69;

    static String program5
            = "int v = 3;";
    static float expect5 
            = 3;

    static String program6
            = "int[] ari[4];"     + "\n"
            + "ari[2] = 57;"  + "\n"
            + "int v = ari[2];";
    static float expect6 
            = 57;

    static String program7
            = "float[] arf[4];"     + "\n"
            + "arf[2] = 57.4 + 12.89;"  + "\n"
            + "float v = arf[2];";
    static float expect7 
            = 70.29f;

    static String program8
            = "float[] arf[4];"     + "\n"
            + "arf[2] = 1234;"  + "\n"
            + "float v = arf[2];";
    static float expect8 
            = 1234;

    static String program9
            = "float[] arf[4];"                 + "\n"
            + "int tmp = 783.0 + 1000.4/2.0;"   + "\n"
            + "arf[3] = 1234 + tmp;"            + "\n"
            + "float v = arf[3];";
    static float expect9 
            = 2517;

    static String program110
            = "float[] arf[4];"       + "\n"
            + "arf[3.4] = 1234;"      + "\n"
            + "float v = arf[3.4];";
    static String expect110 
            = "arf[3.4] = 1234;"  + "\n"
            + "    ^"             + "\n"
            + "Error in line 2: expected integer value" + "\n";

    static String program111
            = "float[] arf[4];"       + "\n"
            + "arf[3] = 1234;"        + "\n"
            + "float v = arf[3.4];";
    static String expect111 
            = "float v = arf[3.4];"   + "\n"
            + "              ^"       + "\n"
            + "Error in line 3: expected integer value" + "\n";

    static String program112
            = "float[] arf[4];"     + "\n"
            + "arf[3] = 1234;"      + "\n"
            + "float a = 3;"        + "\n"
            + "float v = arf[a];";
    static String expect112 
            = "float v = arf[a];"   + "\n"
            + "              ^"     + "\n"
            + "Error in line 4: expected integer value" + "\n";

    static String program113
            = "float[] arf[4];"+ "\n"
            + "arf[3] = 1234;" + "\n"
            + "int a = 3;"     + "\n"
            + "float v = arf[a];";
    static String expect113 
            = "Ok";

    // -----------------------------------------------------------------------                        

    private static Stream<Arguments> floatArgumentProvider() {
        return Stream.of(
            Arguments.of( program0, expect0 ),
            Arguments.of( program1, expect1 ),
            Arguments.of( program2, expect2 ),
            Arguments.of( program3, expect3 ),
            Arguments.of( program4, expect4 ),
            Arguments.of( program5, expect5 ),
            Arguments.of( program6, expect6 ),
            Arguments.of( program7, expect7 ),
            Arguments.of( program8, expect8 ),
            Arguments.of( program9, expect9 )
        );
    }

    private static Stream<Arguments> stringArgumentProvider() {
        return Stream.of(
            Arguments.of( program110, expect110 ),
            Arguments.of( program111, expect111 ),
            Arguments.of( program112, expect112 ),
            Arguments.of( program113, expect113 )
            );
    }

} 