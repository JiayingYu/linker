package linker;

import java.util.ArrayList;
import java.util.List;

public class ObjectModule {
	int defCount = 0;
	int useCount = 0;
	int codeCount = 0;
	List<SymbolPair> defList = new ArrayList<SymbolPair>();
	List<String> useList = new ArrayList<String>();
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
	
	public List<String> getUseList() {
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
	
	
}
