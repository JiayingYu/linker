package linker;

import java.io.FileNotFoundException;

public class LinkerTest {
	public static void main(String[] args) throws FileNotFoundException, SyntaxException {
		String fileName = "input-14";
		Linker linker = new Linker(fileName);
//		linker.printFile();
		
		
		try {linker.passOne();  
		linker.passTwo();
		linker.printSymbolTable();
		System.out.println();
		linker.printMemMap();
		System.out.println();
		linker.printWarnings();
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	
	

}
