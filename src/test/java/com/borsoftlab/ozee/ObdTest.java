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
public class ObdTest {

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
                System.out.println(scanner.text.loc.line + " lines compiled");

                byte[] compiledProgram = parser.getProgramImage();
                System.out.println(compiledProgram.length + " bytes program image");
                scanner.symbolTable.dumpSymbolTableByName();
                byte[] execImage = OzLinker.linkImage(compiledProgram, scanner.symbolTable);
                System.out.println(execImage.length + " bytes execution image");
                final OzVm vm = new OzVm();
                vm.setDebugListener(debugListener);
                vm.loadProgram(execImage);
                System.out.println("\noZee virtual machine started...");
                long startMillis = System.currentTimeMillis();
                vm.execute();
                long execTime = System.currentTimeMillis() - startMillis;
                System.out.println("oZee virtual machine stopped");
                System.out.println("Execution time: " + execTime + " ms");
        
                OzUtils.printMemoryDump(vm.getRam(), 0, execImage.length );
                scanner.symbolTable.dumpSymbolTableByName();
                scanner.symbolTable.dumpRefList();
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
    
    // -----------------------------------------------------------------------                        

    static String program0
            = "ubyte[] buffer[8];" + "\n"
            + "buffer[0] = 231;"   + "\n" 
            + "buffer[1] = 9;"     + "\n" 
            + "buffer[2] = 35;"    + "\n" 
            + "buffer[3] = 17;"    + "\n" 
            + "buffer[4] = 121;"   + "\n" 
            + "buffer[5] = 55;"    + "\n" 
            + "buffer[6] = 247;"   + "\n" 
            + "buffer[7] = 63;"    + "\n" 
            + "float v = buffer[0] * ( buffer[3] + buffer[6]) ;";
    static float expect0 
            = 60984.0f;

    static String program01_04
            = "ubyte[] buffer[8];"     + "\n"
            + "buffer[0] = 178;"       + "\n" 
            + "float v = buffer[0] * 100.0/255.0;";
    static float expect01_04 
            = 69.803925f;

    static String program01_05
            = "ubyte[] buffer[8];"    + "\n"
            + "buffer[0] = 24;"       + "\n" 
            + "float v = buffer[0] - 40;";
    static float expect01_05 
            = -16;


    // -----------------------------------------------------------------------                        

    private static Stream<Arguments> floatArgumentProvider() {
        return Stream.of(
            Arguments.of( program0, expect0 )
//            ,Arguments.of( program01_04, expect01_04 )
//            ,Arguments.of( program01_05, expect01_05 )
        );
    }

} 