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

        final InputStream f;
        final OzParser parser = new OzParser();

        try {
        
            if (args.length == 0){
                System.out.println("\nexecute oZee <file>\n");
                System.exit(1);
            }
            f = new FileInputStream(args[0]);
            try {
                final OzText text = new OzText(f);
                final OzScanner scanner = new OzScanner(text);
                parser.compile(scanner);
                System.out.println();
                System.out.println(scanner.lexemeCount + " lexemes processed");
                System.out.println(scanner.text.loc.line + " lines compiled");
                final OzVm vm = new OzVm();
                byte[] program = parser.getExecMemModule();
                vm.loadProgram(program);
                vm.execute();
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
            System.out.println("Error! Can't open file: '" + args[0] + "'");
        }
    }    
}