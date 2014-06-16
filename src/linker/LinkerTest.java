package linker;

import java.io.FileNotFoundException;

public class LinkerTest {
	public static void main(String[] args) throws FileNotFoundException, SyntaxException {
		String fileName = "input-3";
		Linker linker = new Linker(fileName);
		
		linker.passOne();  
		linker.printSymbolTable();
		System.out.println();
		linker.passTwo();
		linker.printMemMap();
		linker.printSymbolTable();
		linker.printWarnings();
	}
	
	
	

}
