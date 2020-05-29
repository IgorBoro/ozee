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
        = "float a = 23.14;" +"\n"
        + "float b =  6.93;" +"\n"
        + "float c = 11;"    +"\n"
        + "float r = b*b -4*a*c;";
    final static float expect4 = -970.1351f;

    final static String program5 
        = "float r =  7/2;";
    final static float expect5 = 3;

    final static String program6 
        = "float r =  7/2;";
    final static float expect6 = 3;

    final static String program7 
        = "float r =  7.0/2;";
    final static float expect7 = 3.5f;

    final static String program8 
        = "float r =  7/2.0;";
    final static float expect8 = 3.5f;

    final static String program9
        = "int a =  11;" + "\n"
        + "int b = 10; int c = 13; int d = 7;"  + "\n"
        + "float r =  a + a * (b + c/d);";
    final static float expect9 = 132;


    final static String program10
        = "int   a =  11.84;" + "\n"
        + "float r =  a;";
    final static float expect10 = 11;


    final static String program11
        = "float r =  55.8/2.37;";
    final static float expect11 = 23.544304f;


    final static String program12
        = "float r =  55.8/2.37 *25.3 + 16.7*(433/2.0 + 12.6);";
    final static float expect12 = 4421.64088f;


    final static String program13
        = "ubyte a =  240;" + "\n"
        + "float r =  100.0 * a/255.0;";
    final static float expect13 = 94.117645f;

    final static String program14
        = "ubyte a =  84;" + "\n"
        + "float r =  a - 40;";
    final static float expect14 = 44;

    final static String program15
        = "ubyte a =  84;" + "\n"
        + "float r =  a - 40.0;";
    final static float expect15 = 44.0f;

    final static String program16
        = "ubyte A =  70;" + "\n"
        + "float r =  (A - 128.0) * 100.0/128.0;";
    final static float expect16 = -45.3125f;


    final static String program17
        = "ubyte a =   18;" + "\n"
        + "ubyte b =  220;" + "\n"
        + "float r =  a * 256 + b;";
    final static float expect17 = 4828;

    final static String program18
        = "ushort a =  4828;" + "\n"
        + "float  r =  a;";
    final static float expect18 = 4828;

    final static String program19
        = "ushort a =  -10;" + "\n"
        + "float  r =  a;";
    final static float expect19 = 65526.0f;

    final static String program20
        = "ubyte a =  255;" + "\n"
        + "ubyte b =  255;" + "\n"
        + "float r =  (a*256.0 + b)*2.0/65535.0;";
    final static float expect20 = 2.0f;

    final static String program21
        = "ubyte a =  128;" + "\n"
        + "ubyte b =  128;" + "\n"
        + "float r =  (a*256.0 + b)/ 200.0;";
    final static float expect21 = 164.48f;

    final static String program22
        = "short a =  -478;" + "\n"
        + "float r =  2.0 * a;";
    final static float expect22 = -956.0f;

    final static String program23
        = "short a =  -478;" + "\n"
        + "float r =  2 * a;";
    final static float expect23 = -956.0f;

    final static String program24
        = "short a =  478;" + "\n"
        + "float r =  2 * a;";
    final static float expect24 = 956.0f;

    // ------------------------------------------------------

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
                /*
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
