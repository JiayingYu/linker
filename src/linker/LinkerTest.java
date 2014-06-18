package linker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class LinkerTest {
	public static void main(String[] args) throws FileNotFoundException, SyntaxException {
		for (int i = 1; i <= 33; i++) {
			String inputFileName = "testData/input-" + i;
			Linker linker = new Linker(inputFileName);
			
			String output = "";				
			try {
				linker.passOne();  
				linker.passTwo();
				output += linker.symbolTableToString() + "\n" 
						+ linker.memMapToString() + "\n"
						+ linker.warningsToString();
				
			} catch(Exception e) {
				output = e.getMessage();
			}
			
			try {
				String outputFileName = "outputs/output-" + i;
				File outputFile = new File(outputFileName);
				BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
				writer.write(output);
				writer.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		System.out.printf("Done\n");
	}
}
