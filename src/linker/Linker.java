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
	private int instrCount = 0;

	public Linker(String fileName) throws FileNotFoundException {
		sourceFile = new File(fileName);
		scanner = new Scanner(fileToString());
	}

	private String fileToString() throws FileNotFoundException {
		Scanner input = new Scanner(sourceFile);
		String fileStr = "";
		String line = "";
		while (input.hasNext()) {
			line = input.nextLine();
			if (!line.matches("\\s*")) // if the line is not empty
				fileStr += line + " EOL\n";
		}

		if (!fileStr.matches("\\s*"))
			fileStr = fileStr.substring(0, fileStr.length() - 4);
		return fileStr;
	}
	
	public void printFile() throws FileNotFoundException {
		System.out.print(fileToString());
	}

	public void passOne() throws SyntaxException {
		int baseAddress = 0;
		int moduleNo = 1;
		while (scanner.hasNext()) {
			ObjectModule currModule = new ObjectModule(baseAddress, moduleNo);

			readDefList(currModule);
			readUseList(currModule);
			readProgramText(currModule);

			baseAddress += currModule.getCodeCount(); // base address for the
														// next module
			moduleNo++;
			moduleList.add(currModule); // add the current module to module list
		}
		checkDefRelAddress();
	}

	// check if an address in definition exceeds the size of the module. for No5
	private void checkDefRelAddress() {
		// iterate through the moduleList
		Iterator<ObjectModule> modListIt = moduleList.iterator();
		while (modListIt.hasNext()) {
			ObjectModule module = modListIt.next();
			
			int currModuleSize = module.codeCount - 1;

			// iterate through the current defList,
			Iterator<SymbolPair> it = module.getDefList().iterator();
			while (it.hasNext()) {
				// check if any relAddress exceeds module size(codeCount - 1)
				SymbolPair sp = it.next();
				if (sp.getRelAddress() > currModuleSize) {
					int oldRelAddr = sp.getRelAddress();
					// set relAddr in module
					sp.setRelAddress(0);
					// set globalAddr in symbolTable
					int globalAddr = module.baseAddress;
					symbolTable.setAddr(sp.symbol, globalAddr);

					int moduleNo = module.moduleNo;
					String warnMsg = "Module " + moduleNo + ": " + sp.symbol
							+ " too big " + oldRelAddr + " (max=" + currModuleSize
							+ ") assume zero relative";
					warningList.add(warnMsg);
				}
			}
		}
	}

	private int moduleSize() {
		int tolCodeCount = 0;
		// iterate through the moduleList
		Iterator<ObjectModule> modListIt = moduleList.iterator();
		while (modListIt.hasNext()) {
			tolCodeCount += modListIt.next().codeCount;
		}
		return tolCodeCount - 1;
	}

	private void calLocation() {
		if (scanner.hasNext("EOL")) {
			lineNum++;
			offset = 0;
			String skip = scanner.next();
		}
		offset++;
	}

	private void readDefList(ObjectModule module) throws SyntaxException {
		int defCount = 0;
		int defCountLineNum = 0;
		int defCountOffset = 0;
		calLocation();

		if (scanner.hasNextInt()) {
			// number of symbols defined in deflist
			defCount = scanner.nextInt(); 
			defCountLineNum = lineNum;
			defCountOffset = offset;
		} else {
			throw new SyntaxException("Parse Error line " + lineNum 
					+ " offset " + offset + ": " + SyntaxError.NUM_EXPECTED);
		}

		module.setDefCount(defCount);

		for (int i = 0; i < defCount; i++) {
			calLocation();
			String symbol = "";
			
			if (!scanner.hasNext()) {
				throw new SyntaxException("Parse Error line " + defCountLineNum 
						+ " offset " + defCountOffset + ": " + SyntaxError.TO_MANY_DEF_IN_MODULE);
			}
			
			if (scanner.hasNext("[a-zA-Z]\\w*")) {
				symbol = scanner.next();
				if (symbol.length() > 16) 
					throw new SyntaxException("Parse Error line " + lineNum 
							+ " offset " + offset + ": " + SyntaxError.SYS_TOLONG);
			} else {
				throw new SyntaxException("Parse Error line " + lineNum 
						+ " offset " + offset + ": " + SyntaxError.SYM_EXPECTED);
			} 

			calLocation();
			int relAddress = 0;
			try {
				relAddress = scanner.nextInt();
			} catch (Exception e) {
				throw new SyntaxException("Parse Error line " + lineNum 
						+ " offset " + offset + ": " + SyntaxError.NUM_EXPECTED);
			}

			SymbolPair sp = new SymbolPair(symbol, relAddress);
			module.defList.add(sp);
			storeSymbolTable(symbol, module.baseAddress, module.moduleNo,
					relAddress);
		}
	}

	private void storeSymbolTable(String symbol, int baseAddress, int moduleNo,
			int relAddress) {
		if (!symbolTable.contains(symbol)) {
			int absAddress = baseAddress + relAddress;
			symbolTable.put(symbol, absAddress, moduleNo);
		} else {
			symbolTable.setErrorMsg(symbol, true);
		}
	}

	private void readUseList(ObjectModule module) throws SyntaxException {
		calLocation();
		int useCount = 0;
		int useCountLineNum = 0;
		int useCountOffset = 0;

		try {
			useCount = scanner.nextInt();
			useCountLineNum = lineNum;
			useCountOffset = offset;
		} catch (Exception e) {
			throw new SyntaxException("Parse Error line " + lineNum 
					+ " offset " + offset + ": " + SyntaxError.NUM_EXPECTED);
		}

		module.setUseCount(useCount);

		for (int i = 0; i < useCount; i++) {
			calLocation();
			String useSymbol = "";
					
			if (!scanner.hasNext()) {
				throw new SyntaxException("Parse Error line " + useCountLineNum 
						+ " offset " + useCountOffset + ": " + SyntaxError.TO_MANY_USE_IN_MODULE);
			}

			if (scanner.hasNext("[a-zA-Z]\\w*")) {
				useSymbol = scanner.next();
				if (useSymbol.length() > 16) {
					throw new SyntaxException("Parse Error line " + lineNum 
							+ " offset " + offset + ": " + SyntaxError.SYS_TOLONG);
				}
				//detect duplicate use symbol 
				calLocation();
				if (scanner.hasNext(useSymbol)) {
					throw new SyntaxException("Parse Error line " + useCountLineNum 
							+ " offset " + useCountOffset + ": " + SyntaxError.TO_MANY_USE_IN_MODULE);
				}
			} else {
				throw new SyntaxException("Parse Error line " + lineNum 
						+ " offset " + offset + ": " + SyntaxError.SYM_EXPECTED);
			} 
			module.addToUseList(useSymbol);
		}
	}

	private void readProgramText(ObjectModule module) throws SyntaxException {
		int codeCount = 0;
		char instrType = ' ';
		int instr = 0;
		calLocation();

		
		if (scanner.hasNextInt()) {
			codeCount = scanner.nextInt();
			instrCount += codeCount;
			if (instrCount > 512) {
				throw new SyntaxException("Parse Error line " + lineNum 
						+ " offset " + offset + ": " + SyntaxError.TO_MANY_INSTR);
			}
		} else {
			throw new SyntaxException("Parse Error line " + lineNum 
					+ " offset " + offset + ": " + SyntaxError.NUM_EXPECTED);
		}

		module.setCodeCount(codeCount);

		for (int i = 0; i < codeCount; i++) {
			calLocation();

			if (scanner.hasNext("R|I|E|A")) {
				instrType = scanner.next().charAt(0); // check type here
			} else {
				throw new SyntaxException("Parse Error line " + lineNum 
						+ " offset " + offset + ": " + SyntaxError.INSTR_TYPE_EXPECTED);
			}

			calLocation();

			if (scanner.hasNextInt()) {
				instr = scanner.nextInt();
			} else {
				throw new SyntaxException("Parse Error line " + lineNum 
						+ " offset " + offset + ": " + SyntaxError.ADDR_EXPECTED);
			}

			InstructionPair ip = new InstructionPair(instrType, instr);
			module.codeList.add(ip);
		}
	}

	public void printSymbolTable() {
		System.out.print(symbolTable);
	}

	public void passTwo() {
		// iterate through moduleList
		Iterator<ObjectModule> it = moduleList.iterator();

		while (it.hasNext()) {
			ObjectModule currModule = it.next();
			// base address of the current module
			int currBaseAddress = currModule.getBaseAddress(); 
			// get current module's program text
			List<InstructionPair> codeList = currModule.getCodeList(); 

			// iterate through programText
			for (int i = 0; i < codeList.size(); i++) {
				char currInstrType = codeList.get(i).instrType;
				int currInstr = codeList.get(i).instr;

				switch (currInstrType) {
				case ('I'):
					resolveImAddress(currInstr);
					break;
				case ('A'):
					resolveAbsAddress(currInstr);
					break;
				case ('R'):
					resolveRelAddress(currInstr, currBaseAddress);
					break;
				case ('E'):
					resolveExtAddress(currInstr, currModule);
					break;
				default:
					System.out.print("Invalid instruction type.");
					break;
				}
			}
			checkUseListUsage(currModule);
		}
		checkSymbolTableUsage();
	}

	// check if the symbols in useList are used in the current module
	private void checkUseListUsage(ObjectModule currModule) {
		// iterate through the useList
		Iterator<UseTuple> it = currModule.getUseList().iterator();
		while (it.hasNext()) {
			UseTuple useListTuple = it.next();
			if (!useListTuple.used()) { // if the Tuple in uselist is not used,
										// add to warningList
				int moduleNo = currModule.moduleNo;
				String unUsedSymbol = useListTuple.useSymbol;
				String warnMsg = "Module " + moduleNo + ": " + unUsedSymbol
						+ " appeared in the useList but was not actually used";
				warningList.add(warnMsg);
			}
		}
	}

	private void checkSymbolTableUsage() {
		// iterate through the symbolTable, check if a symbol is used
		for (Map.Entry<String, SymbolTuple> entry : symbolTable.entrySet()) {
			boolean symbolUsed = entry.getValue().isUsed();
			if (symbolUsed == false) {
				int moduleNo = entry.getValue().moduleNo;
				String unUsedSymbol = entry.getKey();
				String warnMsg = "Module " + moduleNo + ": " + unUsedSymbol
						+ " was defined but never used";
				warningList.add(warnMsg);
			}
		}
	}

	private void resolveImAddress(int currInstr) {
		// check if immediate instruction is illegal
		String errorMsg = "";
		if (currInstr > 9999) {
			currInstr = 9999;
			errorMsg = " Error: Illegal immediate value; treated as 9999";
		}
		memMap.add(currInstr, errorMsg);
	}

	private void resolveAbsAddress(int currInstr) {
		// for No. 9
		if (currInstr % 1000 > 512) {
			String errorMsg = " Error: Absolute address exceeds machine size; zero used";
			memMap.add(currInstr / 1000 * 1000, errorMsg);
			return;
		}	
		memMap.add(currInstr);
	}

	private void resolveRelAddress(int currInstr, int currBaseAddress) {
		// rightmost three digit of the current instruction
		String errorMsg = "";
		int opcode = currInstr / 1000;
		int relAddress = currInstr % 1000; 
		//check if opcode is illegal
		if (opcode > 9) {
			int resolvedInstr = 9999;
			errorMsg = " Error: Illegal opcode; treated as 9999";
			memMap.add(resolvedInstr, errorMsg);
			return;
		}
		
		//check if relative address exceeds module size
		if (relAddress > moduleSize()) {
			relAddress = 0;
			//global address following opcode
			errorMsg = " Error: Relative address exceeds module size; zero used";
		}
		int resolvedInstr = opcode * 1000 + currBaseAddress + relAddress;

		memMap.add(resolvedInstr, errorMsg);
	}

	// return the symbol used by the E type intr
	private void resolveExtAddress(int currInstr, ObjectModule currModule) {
		int opcode = currInstr / 1000;
		// the index into the current module's useList
		int extAddress = currInstr % 1000; 
		String useListSymbol = "";
		// if extAddr exceeds the length of useList, use currInstr as immediate No. 6
		if (extAddress > currModule.useList.size() - 1) {
			String errorMsg = " Error: External Address exceeds length of useList; "
					+ "treated as immediate";
			memMap.add(currInstr, errorMsg);
		} else {
			useListSymbol = currModule.getUseSymbol(extAddress);
			 // mark the useListSymbol as used in uselist in current module
			currModule.markSymbolAsUsed(extAddress);
			if (symbolTable.contains(useListSymbol)) { // No3
				int globalAddress = symbolTable.getAddr(useListSymbol);
				int resolvedInstr = globalAddress + opcode * 1000;
				memMap.add(resolvedInstr);
				// mark the useListSymbol as used in symbolTable
				symbolTable.markSymbolAsUsed(useListSymbol); 
			} else {
				String errorMsg = " Error: " + useListSymbol
						+ " is not defined; zero used";
				memMap.add(opcode * 1000, errorMsg); // No 3
			}
		}
	}

	public void printMemMap() {
		System.out.println("Memory Map");
		// iterate through memMap
		Iterator<MemTuple> it = memMap.iterator();
		int addr = 0;
		int index = 0;
		String errorMsg = "";

		while (it.hasNext()) {
			MemTuple mt = it.next();
			addr = mt.addr;
			errorMsg = mt.getErrorMsg();

			System.out.printf("%03d: %04d %s\n", index, addr, errorMsg);
			index++;
		}
	}

	public void printWarnings() {
		Iterator<String> it = warningList.iterator();
		while (it.hasNext()) {
			System.out.println("Warning: " + it.next());
		}
	}
}
