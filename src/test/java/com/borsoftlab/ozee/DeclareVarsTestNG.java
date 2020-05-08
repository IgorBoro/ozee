package com.borsoftlab.ozee;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.testng.Assert;
import org.testng.annotations.*;

public class DeclareVarsTestNG {

 
    static String program0
                        = "int i ";
    static String message0 
                        = "int i "   + '\n'
                        + "      ^"  + '\n'
                        + "Error in line 1: unexpected EOF" + '\n';

    final static String program1 
                        = "int i;";
    final static String message1
                        = "Ok";

    final static String program2
                        = "int i=";
    final static String message2
                        = "int i="  + '\n'
                        + "      ^" + '\n'
                        + "Error in line 1: unexpected EOF"   + '\n';

    final static String program3
                        = "int i=;";
    final static String message3
                        = "int i=;"  + '\n'
                        + "      ^"  + '\n'
                        + "Error in line 1: unexpected lexeme"   + '\n';
                        
    final static String program4 
                        = "int i=5;";
    final static String message4
                        = "Ok";

    final static String program5 
                        = "int id = 180; // comment"   + '\n'
                        + "int j = id;"                + '\n'
                        + "byte l;"                    + '\n'
                        + "int t12;"                   + '\n'
                        + "float g;"                   + '\n'
                        + "int k= 17 + j + t12;"       + '\n'
                        + "/*"                         + '\n'
                        + " * comment"                 + '\n'
                        + " */"                        + '\n'
                        + "byte b = 45;"               + '\n'
                        + "float f = 0.523 * 12.3 - 41.6/32 * (32 + 76) + j;";
//                        + "float f = 12 + 41. 6/32 * (32 + 76) + j;";
    final static String message5
                        = "Ok";

    static String program6
                        = "float ff=45. 6;";
    static String message6 
                        = "float ff=45. 6;"  + '\n'
                        + "            ^"    + '\n'
                        + "Error in line 1: unexpected symbol"   + '\n';

    static String program7
                        = "float ff=45.6;";
    static String message7 
                        = "Ok";

    final static String program8
                        = "int i= ;";
    final static String message8
                        = "int i= ;"  + '\n'
                        + "       ^"  + '\n'
                        + "Error in line 1: unexpected lexeme"   + '\n';

    static String program9
                        = "int i + ";
    static String message9 
                        = "int i + "   + '\n'
                        + "      ^"    + '\n'
                        + "Error in line 1: expected '=' or ';'" + '\n';


    final static String program10 
                        = "float ;";
    final static String message10
                        = "float ;"  + '\n'
                        + "      ^"  + '\n'
                        + "Error in line 1: expected variable name" + '\n';

    final static String program11 
                        = "float +";
    final static String message11
                        = "float +"  + '\n'
                        + "      ^"  + '\n'
                        + "Error in line 1: expected variable name" + '\n';


    static String program12
                        = "int d=4r56;";
    static String message12 
                        = "int d=4r56;"  + '\n'
                        + "       ^"     + '\n'
                        + "Error in line 1: unexpected lexeme" + '\n';

    static String program13
                        = "int d=4 56;";
    static String message13 
                        = "int d=4 56;"  + '\n'
                        + "        ^"    + '\n'
                        + "Error in line 1: unexpected lexeme" + '\n';

                        
    OzParser parser   = new OzParser();
    OzScanner scanner = new OzScanner();


    @DataProvider(name = "test1")
    public static Object[][] arguments() {
       return new Object[][] {
           {program0, message0},
           {program1, message1},
           {program2, message2},
           {program3, message3},
           {program4, message4},
           {program5, message5},
           {program6, message6},
           {program7, message7},
           {program8, message8},
           {program9, message9},
           {program10, message10},
           {program11, message11},
           {program12, message12},
           {program13, message13},
        };
    }

    @Test(dataProvider = "test1")
    public void test(String program, String message) {
        System.out.println("::------------------------------------------::");
        try {     
            final InputStream programStream = new ByteArrayInputStream(program.getBytes());
            try {
                final OzText text = new OzText(programStream);
                scanner.resetText(text);
                parser.compile(scanner);
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
       Assert.assertEquals(message, OzCompileError.messageString.toString());
    }

}
