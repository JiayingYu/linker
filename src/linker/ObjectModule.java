package linker;

import java.util.ArrayList;
import java.util.List;

public class ObjectModule {
	int defCount = 0;
	int useCount = 0;
	int codeCount = 0;
	List<SymbolPair> defList = new ArrayList<SymbolPair>();
	List<UseTuple> useList = new ArrayList<UseTuple>();
	List<InstructionPair> codeList = new ArrayList<InstructionPair>();
	int baseAddress = 0;
	
	public int getDefCount() {
		return defCount;
	}
	
	public void setDefCount(int defCount) {
		this.defCount = defCount;
	}
	
	public int getUseCount() {
		return useCount;
	}
	
	public void setUseCount(int useCount) {
		this.useCount = useCount;
	}
	
	public int getCodeCount() {
		return codeCount;
	}
	
	public void setCodeCount(int codeCount) {
		this.codeCount = codeCount;
	}
	
	public List<SymbolPair> getDefList() {
		return defList;
	}
	
	public List<UseTuple> getUseList() {
		return useList;
	}
	
	public List<InstructionPair> getCodeList() {
		return codeList;
	}
	
	public int getBaseAddress() {
		return baseAddress;
	}
	
	public void setBaseAddress(int baseAddress) {
		this.baseAddress = baseAddress;
	}	
	
	public void addToUseList(String symbol) {
		UseTuple ut = new UseTuple(symbol);
		useList.add(ut);
	}
	
	// get the symbol in useList referred by external address
	public String getUseSymbol(int index) { 
		return useList.get(index).useSymbol;
	}
	
	public void markSymbolAsUsed(int index) {
		useList.get(index).markAsUsed(true);
	}
}

class UseTuple {
	public final String useSymbol;
	private boolean used = false;
	
	UseTuple(String useSymbol) {
		this.useSymbol = useSymbol;
	}
	
	UseTuple(String useSymbol, boolean used) {
		this.useSymbol = useSymbol;
		markAsUsed(used);
	}
	
	//mark the current symbol in uselist as used
	public void markAsUsed(boolean used) {
		this.used = used;
	}
	
	public boolean used() {
		return used;
	}
}
