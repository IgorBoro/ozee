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
import com.borsoftlab.ozee.OzVm.OnOzVmDebugListener;

@Nested
@DisplayName("Test class")
public class FloatArithmeticTest {

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
    public void test(String program, float expect) {
        float value = Float.MIN_VALUE;
        System.out.println("::------------------------------------------::");
        try {     
            final InputStream programStream = new ByteArrayInputStream(program.getBytes());
            try {
                final OzText text = new OzText(programStream);
                scanner.resetText(text);
                parser.compile(scanner);

                final OzVm vm = new OzVm();
                vm.setDebugListener(debugListener);
                byte[] compiledProgram = parser.getProgramImage();
                List<Symbol> symbols = scanner.symbolTable.getTableOrderedByAddr();
                byte[] programImage = OzLinker.linkImage(compiledProgram, symbols);
                scanner.symbolTable.dumpSymbolTableByName();
                vm.loadProgram(programImage);
                System.out.println("\noZee virtual machine started...");
                long startMillis = System.currentTimeMillis();
                vm.execute();
                long execTime = System.currentTimeMillis() - startMillis;
                System.out.println("oZee virtual machine stopped");
                System.out.println("Execution time: " + execTime + " ms");
        
                // OzUtils.printMemoryDump(vm.getRam());
                int valueAddr = scanner.symbolTable.lookup("r").allocAddress;
                value = OzUtils.fetchFloatFromByteArray(vm.getRam(), valueAddr);
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

    final static String program0 
        = "float a = 4.78;" + '\n'
        + "float b = 5.22;" + '\n'
        + "float r = a + b;";
    final static float expect0 = 10;

    final static String program1 
        = "float r = 56 - 23;";
    final static float expect1 = 33;

    final static String program2 
        = "float r = - 82.79;";
    final static float expect2 = -82.79f;

    final static String program3 
        = "float a = -4.78;" + '\n'
        + "float b = -a;" + '\n'
        + "float r = a + b;";
    final static float expect3 = 0;

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

    final static String program9
        = "int a =  11;" + "\n"
        + "int b = 10; int c = 13; int d = 7;"  + "\n"
        + "int r =  a + a * (b + c/d);";
    final static int expect9 = 132;

    // ------------------------------------------------------

    final static String program10
        = "byte a =  11;" + "\n"
        + "int r =  a;";
    final static int expect10 = 11;

    final static String program11
        = "byte a =  -17;" + "\n"
        + "int r =  a;";
    final static int expect11 = -17;

    final static String program12
        = "byte a =  128;" + "\n"
        + "int r =  a;";
    final static int expect12 = -128;

    final static String program13
        = "byte a =  129;" + "\n"
        + "int r =  a;";
    final static int expect13 = -127;

    final static String program14
        = "byte a =  130;" + "\n"
        + "int r =  a;";
    final static int expect14 = -126;

    final static String program15
        = "byte a =  156;" + "\n"
        + "int r =  a;";
    final static int expect15 = -100;

    // -----------------------------------

    final static String program16
        = "ubyte a =  11;" + "\n"
        + "int r =  a;";
    final static int expect16 = 11;

    final static String program17
        = "ubyte a =  -17;" + "\n"
        + "int r =  a;";
    final static int expect17 = 239;

    final static String program18
        = "ubyte a =  128;" + "\n"
        + "int r =  a;";
    final static int expect18 = 128;

    final static String program19
        = "ubyte a =  129;" + "\n"
        + "int r =  a;";
    final static int expect19 = 129;

    final static String program20
        = "ubyte a =  130;" + "\n"
        + "int r =  a;";
    final static int expect20 = 130;

    final static String program21
        = "ubyte a =  156;" + "\n"
        + "int r =  a;";
    final static int expect21 = 156;


    // ------------------------------------------------------

    final static String program22
        = "short a =  11;" + "\n"
        + "int r =  a;";
    final static int expect22 = 11;

    final static String program23
        = "short a =  -2317;" + "\n"
        + "int r =  a;";
    final static int expect23 = -2317;

    final static String program24
        = "short a =  128;" + "\n"
        + "int r =  a;";
    final static int expect24 = 128;

    final static String program25
        = "short a =  129;" + "\n"
        + "int r =  a;";
    final static int expect25 = 129;

    final static String program26
        = "short a =  130;" + "\n"
        + "int r =  a;";
    final static int expect26 = 130;

    final static String program27
        = "short a =  34500;" + "\n"
        + "int r =  a;";
    final static int expect27 = -31036;

    // -----------------------------------

    final static String program28
        = "ushort a =  11;" + "\n"
        + "int r =  a;";
    final static int expect28 = 11;

    final static String program29
        = "ushort a =  -17;" + "\n"
        + "int r =  a;";
    final static int expect29 = 65519;

    final static String program30
        = "ushort a =  128;" + "\n"
        + "int r =  a;";
    final static int expect30 = 128;

    final static String program31
        = "ushort a =  129;" + "\n"
        + "int r =  a;";
    final static int expect31 = 129;

    final static String program32
        = "ushort a =  130;" + "\n"
        + "int r =  a;";
    final static int expect32 = 130;

    final static String program33
        = "ushort a =  156;" + "\n"
        + "int r =  a;";
    final static int expect33 = 156;

    final static String program34
        = "ushort a =  156;" + "\n"
        + "int r =  a;";
    final static int expect34 = 156;

    final static String program35
        = "ushort a =  46111;" + "\n"
        + "int r =  a;";
    final static int expect35 = 46111;

    final static String program36
        = "short a =  46111;" + "\n"
        + "int r =  a;";
    final static int expect36 = -19425;

    // -----------------------------------------

    private static Stream<Arguments> argumentProvider() {
        return Stream.of(
                 Arguments.of( program0, expect0 )
                ,Arguments.of( program1, expect1 )
                ,Arguments.of( program2, expect2 )
                ,Arguments.of( program3, expect3 )
                /*
                ,Arguments.of( program4, expect4 )
                ,Arguments.of( program5, expect5 )
                ,Arguments.of( program6, expect6 )
                ,Arguments.of( program7, expect7 )
                ,Arguments.of( program8, expect8 )
                ,Arguments.of( program9, expect9 )
                ,Arguments.of( program10, expect10 )
                ,Arguments.of( program11, expect11 )
                ,Arguments.of( program12, expect12 )
                ,Arguments.of( program13, expect13 )
                ,Arguments.of( program14, expect14 )
                ,Arguments.of( program15, expect15 )
                ,Arguments.of( program16, expect16 )
                ,Arguments.of( program17, expect17 )
                ,Arguments.of( program18, expect18 )
                ,Arguments.of( program19, expect19 )
                ,Arguments.of( program20, expect20 )
                ,Arguments.of( program21, expect21 )
                ,Arguments.of( program22, expect22 )
                ,Arguments.of( program23, expect23 )
                ,Arguments.of( program24, expect24 )
                ,Arguments.of( program25, expect25 )
                ,Arguments.of( program26, expect26 )
                ,Arguments.of( program27, expect27 )
                ,Arguments.of( program28, expect28 )
                ,Arguments.of( program29, expect29 )
                ,Arguments.of( program30, expect30 )
                ,Arguments.of( program31, expect31 )
                ,Arguments.of( program32, expect32 )
                ,Arguments.of( program33, expect33 )
                ,Arguments.of( program34, expect34 )
                ,Arguments.of( program35, expect35 )
                ,Arguments.of( program36, expect36 )
                */
                );
    }
}        
