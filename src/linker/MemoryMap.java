package linker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MemoryMap {
	private List<MemTuple> lst = new ArrayList<MemTuple>();
	
	public void add (int addr) {
		lst.add(new MemTuple(addr));
	}
	
	public void add(int addr, String errorMsg) {
		lst.add(new MemTuple(addr, errorMsg));
	}
	
	public Iterator<MemTuple> iterator() {
		return lst.iterator();
	}
	
	public int indexOf(int addr) {
		return lst.indexOf(addr);
	}
 }

class MemTuple {
	public final int addr;
	private String errorMsg = "";
	
	MemTuple(int addr) {
		this.addr = addr;
	}
	
	MemTuple(int addr, String errorMsg) {
		this.addr = addr;
		setErrorMsg(errorMsg);
	}
	
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	
	public String getErrorMsg() {
		return errorMsg;
	}
}