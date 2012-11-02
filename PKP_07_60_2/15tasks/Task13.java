import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


public class Task13 {
	public void logMessage(String message) throws IOException {
	    PrintWriter log = new PrintWriter(new FileWriter("log.txt", true));
	    //<<<log.println(message)>>>;
	    print log 
	    log.close();
	}
}
