package linker;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Linker {
	private List<ObjectModule> moduleList = new ArrayList<ObjectModule>();
	private File sourceFile;
	private Map<String, Integer> symbolTable = new TreeMap<String, Integer>();
	private List<Integer> memMap = new ArrayList<Integer>();
	
	public Linker(String fileName) {
		sourceFile = new File(fileName);
	}

	public void passOne() throws FileNotFoundException {
		Scanner scanner = new Scanner(sourceFile); //exception handling to be added
		int baseAddress = 0;
		
		while (scanner.hasNext()) {
			ObjectModule currModule = new ObjectModule();
			readDefList(scanner, currModule, baseAddress);
			readUseList(scanner, currModule);
			readProgramText(scanner, currModule);
			
			currModule.setBaseAddress(baseAddress);
			baseAddress += currModule.getCodeCount(); //base address for the next module	
			moduleList.add(currModule); // add the current module to module list
		}
	}
	
	private void readDefList(Scanner scanner, ObjectModule module, int baseAddress) {
		int defCount = scanner.nextInt(); //number of symbols defined in deflist		
		module.setDefCount(defCount);

		for (int i = 0; i < defCount; i++) {
			String symbol = scanner.next();			
			int relAddress = scanner.nextInt();			
			SymbolPair sp = new SymbolPair(symbol, relAddress);
			module.defList.add(sp);		
			storeSymbolTable(symbol, baseAddress, relAddress);
		}
	}
	
	private void readUseList(Scanner scanner, ObjectModule module) {
		int useCount = scanner.nextInt();
				
		module.setUseCount(useCount);

		for (int i = 0; i < useCount; i++) {
			String useSymbol = scanner.next();			
			module.getUseList().add(useSymbol);
		}
	}
	
	private void readProgramText(Scanner scanner, ObjectModule module) {
		int codeCount = scanner.nextInt();		
		module.setCodeCount(codeCount);

		for (int i = 0; i < codeCount; i++) {
			char instrType = scanner.next().charAt(0); // check type here					
			int instr = scanner.nextInt();			
			InstructionPair ip = new InstructionPair(instrType, instr);			
			module.codeList.add(ip);
		}
	}
	
	private void storeSymbolTable(String symbol, int baseAddress, int relAddress) {
		int absAddress = baseAddress + relAddress;
		symbolTable.put(symbol, absAddress);		
	}
	
	public void printSymbolTable() {
		System.out.println("Symbol Table");
		for (Map.Entry<String, Integer> entry:symbolTable.entrySet()) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
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
					resolveExtAddress(currInstr, currBaseAddress, currModule); break;
				default: 
					System.out.print("Invalid instruction type."); break;
				}
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
	
	private void resolveExtAddress(int currInstr, int currBaseAddress, ObjectModule currModule) {
		int opcode = currInstr / 1000;
		int extAddress = currInstr % 1000; //the index into the current module's useList
		String useListSymbol = currModule.getUseList().get(extAddress); //check if get method returns null
		
		int globalAddress = symbolTable.get(useListSymbol);
		int resolvedInstr = globalAddress + opcode * 1000;
		memMap.add(resolvedInstr);
	}
	
	public void printMemMap() {
		System.out.println("Memory Map");
		//iterate through memMap
		Iterator<Integer> it = memMap.iterator();
		while(it.hasNext()) {
			int address = it.next();
			System.out.printf("%03d: %4d\n", memMap.indexOf(address), address);
		}
	}
	
	public void printModuleList() {
		System.out.println("\n\nprint module list");
		
		Iterator<ObjectModule> it = moduleList.iterator();
		while (it.hasNext()) {
			ObjectModule currModule = it.next();
			System.out.print(currModule.defCount + " ");
			
			//print defList
			List<SymbolPair> defList = currModule.getDefList();
			for (int i = 0; i < defList.size(); i++) {
				System.out.print(defList.get(i).symbol + " ");
				System.out.print(defList.get(i).relAddress + " ");
			}
			System.out.println();
			
			System.out.print(currModule.useCount + " ");
			
			//print useList
			List<String> useList = currModule.getUseList();
			for (int i = 0; i < useList.size(); i++) {
				System.out.print(useList.get(i) + " ");
			}
			System.out.println();
			
			System.out.print(currModule.codeCount + " ");
			
			//print codeList
			List<InstructionPair> codeList = currModule.getCodeList();
			for (int i = 0; i < codeList.size(); i++) {
				System.out.print(codeList.get(i).instrType + " ");
				System.out.print(codeList.get(i).instr + " ");
			}
			System.out.println();
		}
	}
	

}
