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

import com.borsoftlab.ozee.OzVm.OnOzVmSupervisingListener;

@Nested
@DisplayName("Test class")
public class ObdTest {

    OzParser parser   = new OzParser();
    OzScanner scanner = new OzScanner();

    OnOzVmSupervisingListener debugListener = new OnOzVmSupervisingListener(){
    
        @Override
        public void onEventInterceptor(int event, int pc, int cmd, int[] stack, int sp) {
            if( event == OzVm.EVENT_BEFORE_EXECUTING ){
                System.out.print(OzAsm.getInstance().getMnemonic(cmd));
            } else if( event == OzVm.EVENT_OPTIONAL_ARGUMENT ){
                System.out.print( String.format(" 0x%08X", cmd) );
            } else if( event == OzVm.EVENT_AFTER_EXECUTING ){
                System.out.println();                

                System.out.print("[ ");
                for( int ptr = 0; ptr < sp; ptr++ ){
                    int value = stack[ptr];
                    System.out.print(String.format("0x%08X ", value));
                }
                System.out.println("] <- top");
            } else if( event == OzVm.EVENT_UNKNOWN_OPCODE){
                System.out.printf("OzVm RTE: unknown opcode - 0x%08X\n", cmd);
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
                System.out.println("code refs:");
                scanner.symbolTable.dumpCodeSegmentRefList();
                System.out.println("data refs:");
                scanner.symbolTable.dumpDataSegmentRefList();
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

    // calculate engine load value        
    static String program01_04
            = "ubyte[] buffer[8];"     + "\n"
            + "buffer[0] = 178;"       + "\n" 
            + "float v = buffer[0] * 100.0/255.0;";
    static float expect01_04 
            = 69.803925f;

    // engine coolant temperature
    static String program01_05
            = "ubyte[] *buffer[8];"   + "\n"
            + "buffer[0] = 24;"       + "\n" 
            + "float *v = buffer[0] - 40;";
    static float expect01_05 
            = -16;

    // short term fuel % trim
    static String program01_06
            = "ubyte[] *buffer[8];"   + "\n"
            + "buffer[0] = 134;"       + "\n" 
            + "float *v = (buffer[0] - 128.0) * 100.0/128.0;";
    static float expect01_06 
            = 4.6875f;

    // fuel pressure
    static String program01_0A
            = "ubyte[] *buffer[8];"   + "\n"
            + "buffer[0] = 255;"       + "\n" 
            + "float *v = buffer[0] * 3 ;";
    static float expect01_0A 
            = 765.0f;

    // intake manifold absolute pressure
    static String program01_0B
            = "ubyte[] *buffer[8];"   + "\n"
            + "buffer[0] = 178;"       + "\n" 
            + "float *v = buffer[0];";
    static float expect01_0B 
            = 178.0f;

    // engine RPM        
    static String program01_0C
            = "ubyte[] *buffer[8];"   + "\n"
            + "buffer[0] = 178;"       + "\n" 
            + "buffer[1] = 49;"       + "\n" 
            + "float *v = ((buffer[0] * 256.0) + buffer[1])/4.0;";
    static float expect01_0C 
            = 11404.25f;

    // engine RPM        
    static String program01_0C_2
            = "ubyte *A = 178;"  + "\n" 
            + "ubyte *B = 49;"   + "\n" 
            + "float *v = ((A*256.0)+B)/4.0;";
    static float expect01_0C_2 
            = 11404.25f;

    // -----------------------------------------------------------------------                        

    private static Stream<Arguments> floatArgumentProvider() {
        return Stream.of(
            Arguments.of( program0, expect0 )
           ,Arguments.of( program01_04, expect01_04 )
           ,Arguments.of( program01_05, expect01_05 )
           ,Arguments.of( program01_06, expect01_06 )
           ,Arguments.of( program01_0A, expect01_0A )
           ,Arguments.of( program01_0B, expect01_0B )
           ,Arguments.of( program01_0C, expect01_0C )
           ,Arguments.of( program01_0C_2, expect01_0C_2 )
        );
    }

} 