package linker;

import java.io.FileNotFoundException;

public class LinkerTest {
	public static void main(String[] args) throws FileNotFoundException {
		String fileName = "input-2";
		Linker linker = new Linker(fileName);
		
		linker.passOne();  
		linker.printSymbolTable();
		System.out.println();
		linker.passTwo();
		linker.printMemMap();
	}
	
	
	

}
