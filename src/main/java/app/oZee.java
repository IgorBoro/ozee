package app;

import com.borsoftlab.ozee.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class oZee {

    public static void main(final String[] args) {
        System.out.println("\n...oZee compiler...\n");

        final InputStream f;
        final OzParser parser = new OzParser();

        try {
        /*
         * if (args.length == 0) f = System.in; else { f = new FileInputStream(args[0]);
         * }
         */

            f = new FileInputStream("program01.oZee");
            try {
                final OzText text = new OzText(f);
                final OzScanner scanner = new OzScanner(text);
                parser.compile(scanner);
                System.out.println('\n');
                System.out.println(text.loc.line + " lines compiled");
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
            e.printStackTrace();
        }
    }    
}