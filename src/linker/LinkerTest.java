package linker;

import java.io.FileNotFoundException;

public class LinkerTest {
	public static void main(String[] args) throws FileNotFoundException, SyntaxException {
		String fileName = "input-7";
		Linker linker = new Linker(fileName);
		
		linker.passOne();  
		linker.passTwo();
		linker.printSymbolTable();
		System.out.println();
		linker.printMemMap();
		System.out.println();
		linker.printWarnings();
	}
	
	
	

}
