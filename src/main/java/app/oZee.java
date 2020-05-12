package app;

import com.borsoftlab.ozee.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/*
 * mvn exec:java -Dexec.mainClass="app.oZee" -Dexec.args="program01.oZee"
 */

public class oZee {

    public static void main(final String[] args) {
        System.out.println("\n...oZee compiler...\n");

        if (args.length == 0){
            System.out.println("\nexecute: oZee <file>\n");
            System.exit(1);
        }

        InputStream file = null;
        try {
      
            try{
                file = new FileInputStream(args[0]);
            } catch(Throwable e) {
                System.out.println("Error! Can't open file: '" + args[0] + "'");
            }
            final OzText text = new OzText(file);
            final OzScanner scanner = new OzScanner(text);
            final OzParser parser = new OzParser();
            parser.compile(scanner);
            System.out.println();
            System.out.println(scanner.lexemeCount + " lexemes processed");
            System.out.println(scanner.text.loc.line + " lines compiled");
            final OzVm vm = new OzVm();
            byte[] program = parser.getExecMemModule();
            vm.loadProgram(program);
            vm.execute();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if( file != null ) {
                try {
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }    
}