package Utils;


import java.io.Closeable;
import java.io.IOException;

public class Close{
    public static void close(Closeable...closeables){
        for(Closeable c:closeables){
            try {
                c.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
