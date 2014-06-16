package linker;

public class SymbolPair {
	public final String symbol;
	private int relAddress;
	
	public SymbolPair(String symbol, int relAddress) {	
		this.symbol = symbol;
		this.relAddress = relAddress;
	}
	
	public void setRelAddress(int relAddress) {
		this.relAddress = relAddress;
	}
	
	public int getRelAddress() {
		return relAddress;
	}

}
