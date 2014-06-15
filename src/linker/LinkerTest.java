package linker;

import java.io.FileNotFoundException;

public class LinkerTest {
	public static void main(String[] args) throws FileNotFoundException, SyntaxException {
		String fileName = "input-4";
		Linker linker = new Linker(fileName);
		
		linker.passOne();  
		linker.printSymbolTable();
		linker.passTwo();
		linker.printMemMap();
		

	}
	
	
	

}
