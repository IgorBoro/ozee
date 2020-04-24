package com.borsoftlab.oZee;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class oZee {
    public static void main(final String[] args) {
        System.out.println("\n...oZee compiler...");

        final InputStream f;

        try {
        /*
         * if (args.length == 0) f = System.in; else { f = new FileInputStream(args[0]);
         * }
         */

            f = new FileInputStream("program01.oZee");
            try {
                final Text text = new Text(f);
                final Scanner scanner = new Scanner(text);
                final Parser parser = new Parser(scanner);
                parser.compile();
                System.out.println();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    f.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            try {
                throw e;
            } catch (Throwable e1) {
                e1.printStackTrace();
            }
        }
    }    
}