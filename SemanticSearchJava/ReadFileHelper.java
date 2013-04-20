import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ReadFileHelper
{
    public static List<String> Read(String path){
    	List<String> allLines = new ArrayList<String>();
        try {			        
        	        	        		
			/*	Sets up a file reader to read the file passed on the command
				line one character at a time */
			FileReader input = new FileReader(path);
            
			/* Filter FileReader through a Buffered read to read a line at a
			   time */
			BufferedReader bufRead = new BufferedReader(input);
			
            String line; 	// String that holds current file line
                        
            // Read first line
            line = bufRead.readLine();
                        
			// Read through file one line at time. Print line # and line
            while (line != null){                                
                allLines.add(line.trim());
                line = bufRead.readLine();
            }
            
            bufRead.close();                       
			
        }catch (ArrayIndexOutOfBoundsException e){
            /* If no file was passed on the command line, this expception is
			generated. A message indicating how to the class should be
			called is displayed */
			System.out.println("Usage: java ReadFile filename\n");			

		}catch (IOException e){
			// If another exception is generated, print a stack trace
            e.printStackTrace();            
        }
        
		return allLines;
        
    }// end main
}