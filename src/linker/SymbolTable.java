package linker;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class SymbolTable {
	private Map<String, SymbolTuple> treeMap = new TreeMap<String, SymbolTuple>();
	
	public void put(String symbol, int addr) {
		SymbolTuple sp = new SymbolTuple(addr);
		treeMap.put(symbol, sp);	
	}
	
	public void setErrorMsg(String symbol, boolean error) {
		treeMap.get(symbol).setErrorMsg(error);
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
}

class SymbolTuple {
	private final int addr;
	private boolean error;
	static final String errorMsg = "Error: This Variable is multiple times defined; first value used";
	
	SymbolTuple (int addr) {
		this.addr = addr;
	}
	
	void setErrorMsg(boolean error) {
		this.error = error;
	}
	
	int getAddr() {
		return addr;
	}
	
	String getErrorMsg() {
		return (error? errorMsg : "");
	}
}
