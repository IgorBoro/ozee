package com.borsoftlab.oZee;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class oZee {
    public static void main(final String[] args) {
        System.out.println("\n...oZee compiler...");

        final InputStream f;
        try {

            /*
            if (args.length == 0)
                f = System.in;
            else {
                f = new FileInputStream(args[0]);
            }
            */

            f = new FileInputStream("program01.oZee");

            final Text text = new Text(f);
            final Parser parser = new Parser(text);
            parser.compile();
            } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }    
}