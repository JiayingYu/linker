package linker;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class SymbolTable {
	private Map<String, SymbolTuple> treeMap = new TreeMap<String, SymbolTuple>();
	
	public void put(String symbol, int addr, int moduleNo) {
		SymbolTuple sp = new SymbolTuple(addr, moduleNo);
		treeMap.put(symbol, sp);	
	}
	
	public void setErrorMsg(String symbol, boolean duplicated) {
		treeMap.get(symbol).setErrorMsg(duplicated);
	}
	
	
	public boolean contains(String symbol) {
		return treeMap.containsKey(symbol);
	}
	
	public Set<Map.Entry<String, SymbolTuple>> entrySet() {
		return treeMap.entrySet();
	}
	
	public int getAddr(String symbol) {
		return treeMap.get(symbol).getAddr();
	}
	
	public void markSymbolAsUsed(String symbol) {
		treeMap.get(symbol).markUsage(true);
	}
	
	public void setAddr(String symbol, int addr) {
		treeMap.get(symbol).setAddr(addr);
	}
	
	public String toString() {
		String s = "SymbolTable\n";
		for (Map.Entry<String, SymbolTuple> entry : treeMap.entrySet()) {
			String symbol = entry.getKey();
			int addr = entry.getValue().getAddr();
			String errorMsg = entry.getValue().getErrorMsg();	
			int ModuleNo = entry.getValue().moduleNo;
			s += symbol + "=" + addr + " " + errorMsg + " " +  ModuleNo
					+  " " + entry.getValue().isUsed() + "\n";
		}
		return s;
	}
	
}

class SymbolTuple {
	private int addr;
	private boolean duplicated;
	private boolean used = false;
	public final int moduleNo;
	public static final String errorMsg = "Error: This Variable is multiple times defined; first value used";
	
	SymbolTuple (int addr, int moduleNo) {
		this.addr = addr;
		this.moduleNo = moduleNo;
	}
	
	void setErrorMsg(boolean duplicated) {
		this.duplicated = duplicated;
	}
	
	int getAddr() {
		return addr;
	}
	
	public void setAddr(int addr) {
		this.addr = addr;
	}
	
	String getErrorMsg() {
		return (duplicated? errorMsg : "");
	}
	
	void markUsage(boolean used) {
		this.used = used;
	}
	
	boolean isUsed() {
		return used;
	}
	

	
}
