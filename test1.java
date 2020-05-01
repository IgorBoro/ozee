

import org.testing.annotations.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.borsoftlab.oZee.OzParser;
import com.borsoftlab.oZee.OzScanner;
import com.borsoftlab.oZee.OzText;


public class test1 {

    @Test
    public void test_id_declaration() {
            System.out.println("\n...oZee compiler...\n");
    
            final InputStream f;
    
            try {
            /*
             * if (args.length == 0) f = System.in; else { f = new FileInputStream(args[0]);
             * }
             */
    
                System.out.println("");
                f = new FileInputStream("program01.oZee");
                try {
                    final OzText text = new OzText(f);
                    final OzScanner scanner = new OzScanner(text);
                    final OzParser parser = new OzParser(scanner);
                    parser.compile();
                    System.out.println('\n');
                  //  System.out.println(text.loc.line + " lines compiled");
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
    
