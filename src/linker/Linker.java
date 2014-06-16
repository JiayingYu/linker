package linker;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class Linker {
	private List<ObjectModule> moduleList = new ArrayList<ObjectModule>();
	private SymbolTable symbolTable = new SymbolTable();
	private MemoryMap memMap = new MemoryMap();
	private int lineNum = 1;
	private int offset = 0;
	private Scanner scanner;
	private File sourceFile;
	private List<String> warningList = new ArrayList<String>();

	public Linker(String fileName) throws FileNotFoundException {
		sourceFile = new File(fileName);
		scanner = new Scanner(fileToString());
	}

	private String fileToString() throws FileNotFoundException {
		Scanner input = new Scanner(sourceFile);
		String fileStr = "";
		String line = "";
		while (input.hasNext()) {
			line = input.next();
			if (!line.matches("\\s+")) //if the line is not empty
				fileStr += line + " EOL\n";
		}

		fileStr = fileStr.substring(0, fileStr.length() - 4);
		return fileStr;
	}

	public void passOne() throws SyntaxException {
		int baseAddress = 0;
		while (scanner.hasNext()) {
			ObjectModule currModule = new ObjectModule();

			readDefList(currModule, baseAddress);
			readUseList(currModule);
			readProgramText(currModule);

			currModule.setBaseAddress(baseAddress);
			baseAddress += currModule.getCodeCount(); // base address for the
														// next module
			moduleList.add(currModule); // add the current module to module list
		}
	}

	private void calLocation() {
		if (scanner.hasNext("EOL")) {
			lineNum++;
			offset = 0;
			String skip = scanner.next();
		}
		offset++;
	}

	private void readDefList(ObjectModule module, int baseAddress) throws SyntaxException {
		int defCount = 0;
		calLocation();

		if (scanner.hasNextInt()) {
			defCount = scanner.nextInt(); // number of symbols defined in
											// deflist
		} else {
			throw new SyntaxException("Number expected " + lineNum + " " + offset);
		}

		module.setDefCount(defCount);

		for (int i = 0; i < defCount; i++) {
			calLocation();

			String symbol = scanner.next();
			if (!symbol.matches("[a-zA-Z]\\w*")) {
				throw new SyntaxException("symbol expected " + lineNum + " " + offset);
			}
			else if (symbol.length() > 16) {
				throw new SyntaxException("symbol too long " + lineNum + " " + offset);
			}

			calLocation();
			int relAddress = 0;
			try {
				relAddress = scanner.nextInt();
			} catch (Exception e) {
				throw new SyntaxException("Address expected " + lineNum + " " + offset);
			}

			SymbolPair sp = new SymbolPair(symbol, relAddress);
			module.defList.add(sp);
			storeSymbolTable(symbol, baseAddress, relAddress);
		}

		 if (scanner.hasNext("[^(EOL)]") && scanner.hasNext("[a-zA-Z]\\w*")) {
			 throw new SyntaxException("too many def in module " + lineNum + " " +
		 offset );
		 }

	}

	private void readUseList(ObjectModule module) throws SyntaxException {
		calLocation();
		int useCount = 0;
		
		try {
			useCount = scanner.nextInt();
		} catch(Exception e) {
			throw new SyntaxException("Number expected " + lineNum + " " + offset);
		}
		
		module.setUseCount(useCount);

		for (int i = 0; i < useCount; i++) {
			calLocation();
			String useSymbol = scanner.next();
			
			if (!useSymbol.matches("[a-zA-Z]\\w*")) {
				throw new SyntaxException("symbol expected " + lineNum + " " + offset);
			}
			else if (useSymbol.length() > 16) {
				throw new SyntaxException("symbol too long " + lineNum + " " + offset);
			}

			module.addToUseList(useSymbol);
		}
		
		if (scanner.hasNext("[^(EOL)]") && scanner.hasNext("[a-zA-Z]\\w*")) {
			 throw new SyntaxException("too many use in module " + lineNum + " " +
		 offset );
		 }
	}

	private void readProgramText(ObjectModule module) throws SyntaxException {
		calLocation();
		int codeCount = 0;
		char instrType = ' ';
		int instr = 0;
		try {
			codeCount = scanner.nextInt();
		} catch(Exception e) {
			throw new SyntaxException("Number expected " + lineNum + " " + offset);
		}
		
		module.setCodeCount(codeCount);

		for (int i = 0; i < codeCount; i++) {
			calLocation();
			
			if (scanner.hasNext("R|I|E|A")) {
				instrType = scanner.next().charAt(0); // check type here
			} else {
				throw new SyntaxException("instruction Type expected " + lineNum + " " + offset);
			}
				

			calLocation();
			
			if (scanner.hasNext("\\d{4}")) {
				instr = scanner.nextInt();
			} else {
				throw new SyntaxException("address expected " + lineNum + " " + offset);
			}
			
			InstructionPair ip = new InstructionPair(instrType, instr);
			module.codeList.add(ip);	
		}
		
		if (scanner.hasNext("[^(EOL)]") && scanner.hasNext("R|I|E|A")) {
			throw new SyntaxException("too many instr in module " + lineNum + " " + offset);
		}
	}

	private void storeSymbolTable(String symbol, int baseAddress, int relAddress) {
		if (!symbolTable.contains(symbol)) {
			int absAddress = baseAddress + relAddress;
			symbolTable.put(symbol, absAddress);
		}
		else {
			symbolTable.setErrorMsg(symbol, true);
		}
	}

	public void printSymbolTable() {
		System.out.println("Symbol Table");
		
		for (Map.Entry<String, SymbolTuple> entry : symbolTable.entrySet()) {
			String symbol = entry.getKey();
			int addr = entry.getValue().getAddr();
			String errorMsg = entry.getValue().getErrorMsg();
			
			System.out.println(symbol + "=" + addr + " " + errorMsg);
		}
	}
	
	public void passTwo() {
		//iterate through moduleList
		Iterator<ObjectModule> it = moduleList.iterator();

		while(it.hasNext()) {
			ObjectModule currModule = it.next();
			int currBaseAddress = currModule.getBaseAddress(); //base address of the current module
			List<InstructionPair> codeList = currModule.getCodeList(); //get current module's  program text

			//iterate through programText
			for (int i = 0; i < codeList.size(); i++) {
				char currInstrType = codeList.get(i).instrType;
				int currInstr = codeList.get(i).instr;
				
				switch(currInstrType) {
				case('I'): 
					resolveImAddress(currInstr); break;
				case('A'): 
					resolveAbsAddress(currInstr); break;
				case('R'): 
					resolveRelAddress(currInstr, currBaseAddress); break;
				case('E'): 
					resolveExtAddress(currInstr, currModule); 
					break;
				default: 
					System.out.print("Invalid instruction type."); break;
				}
			}		
			checkUseListUsage(currModule);
		}	
	}
	
	//check if the symbols in useList are used in the current module
	private void checkUseListUsage(ObjectModule currModule) {
		//iterate through the useList
		Iterator<UseTuple> it = currModule.getUseList().iterator();
		while(it.hasNext()) {
			UseTuple useListTuple = it.next();
			if (!useListTuple.used()) { //if the Tuple in uselist is not used, add to warningList
				int moduleNo = currModule.getBaseAddress() + 1;
				String unUsedSymbol = useListTuple.useSymbol;
				String warnMsg = "Module " + moduleNo + ": " + unUsedSymbol
						+ " appeared in the useList but was not actually used";
				warningList.add(warnMsg);
			}
		}
	}
	
	private void resolveImAddress(int currInstr) {
		memMap.add(currInstr);
	}
	
	private void resolveAbsAddress(int currInstr) {
		memMap.add(currInstr);
	}
	
	private void resolveRelAddress(int currInstr, int currBaseAddress) {
		int relAddress = currInstr % 1000; //rightmost three digit of the current instruction
		int opcode = currInstr / 1000;
		int globalAddress =  currBaseAddress + relAddress; 
		int resolvedInstr = globalAddress+ opcode * 1000;
		memMap.add(resolvedInstr);
	}
	
	//return the symbol used by the E type intr
	private void resolveExtAddress(int currInstr, ObjectModule currModule) {
		int opcode = currInstr / 1000;
		int extAddress = currInstr % 1000; //the index into the current module's useList
		String useListSymbol =  currModule.getUseSymbol(extAddress);
		currModule.markSymbolAsUsed(extAddress); //mark the useListSymbol as used in uselist in current module
		
		if (symbolTable.contains(useListSymbol)) { // No3
			int globalAddress = symbolTable.getAddr(useListSymbol);
			int resolvedInstr = globalAddress + opcode * 1000;
			memMap.add(resolvedInstr);
		}
		else {
			String errorMsg = " Error: " + useListSymbol + " is not defined; zero used";
			memMap.add(opcode * 1000, errorMsg); //No 3
		}		
	}
	
	public void printMemMap() {
		System.out.println("Memory Map");
		//iterate through memMap
		Iterator<MemTuple> it = memMap.iterator();
		int addr = 0;
		int index = 0;
		String errorMsg = "";
		
		while(it.hasNext()) {
			MemTuple mt = it.next();
			addr= mt.addr;
			errorMsg = mt.getErrorMsg();
			
			System.out.printf("%03d: %4d %s\n", index, addr, errorMsg);
			index++;
		}
	}
	
	public void printWarnings() {
		Iterator<String> it = warningList.iterator();
		while(it.hasNext()) {
			System.out.println(it.next());
		}
	}
	
//	public void printModuleList() {
//		System.out.println("\n\nprint module list");
//		
//		Iterator<ObjectModule> it = moduleList.iterator();
//		while (it.hasNext()) {
//			ObjectModule currModule = it.next();
//			System.out.print(currModule.defCount + " ");
//			
//			//print defList
//			List<SymbolPair> defList = currModule.getDefList();
//			for (int i = 0; i < defList.size(); i++) {
//				System.out.print(defList.get(i).symbol + " ");
//				System.out.print(defList.get(i).relAddress + " ");
//			}
//			System.out.println();
//			
//			System.out.print(currModule.useCount + " ");
//			
//			//print useList
//			List<String> useList = currModule.getUseList();
//			for (int i = 0; i < useList.size(); i++) {
//				System.out.print(useList.get(i) + " ");
//			}
//			System.out.println();
//			
//			System.out.print(currModule.codeCount + " ");
//			
//			//print codeList
//			List<InstructionPair> codeList = currModule.getCodeList();
//			for (int i = 0; i < codeList.size(); i++) {
//				System.out.print(codeList.get(i).instrType + " ");
//				System.out.print(codeList.get(i).instr + " ");
//			}
//			System.out.println();
//		}
//	}
}
