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
public class DefineArraysTest {

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
                int valueAddr = scanner.symbolTable.lookup("v").allocAddress;
                int value = OzUtils.fetchIntFromByteArray(vm.getRam(), valueAddr);
                System.out.println("v = " + value);

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

    static String program3
            = "int[] vv[16];" + "\n"
            + "int v;";
    static String message3 
            = "Ok";

    static String program4
            = "int[] vv[16];" + "\n"
            + "int v = 77;";
    static String message4 
            = "Ok";

    static String program5
            = "int v = 3 + 4;";
    static String message5 
            = "Ok";

    static String program6
            = "int[] vv[16];" + "\n"
            + "int v = vv[3];";
    static String message6 
            = "Ok";

    static String program7
            = "int[] vv[16];"       + "\n"
            + "vv[7] = 1234567890;" + "\n" 
            + "int v = vv[7];";
    static String message7 
            = "Ok";

    // -----------------------------------------------------------------------                        

    private static Stream<Arguments> argumentProvider() {
        return Stream.of(
            Arguments.of( program0, message0 ),
            Arguments.of( program1, message1 ),
            Arguments.of( program2, message2 ),
            Arguments.of( program3, message3 ),
            Arguments.of( program4, message4 ),
            Arguments.of( program5, message5 ),
            Arguments.of( program6, message6 ),
            Arguments.of( program7, message7 )
        );
    }
} 