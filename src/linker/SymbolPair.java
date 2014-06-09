package linker;

public class SymbolPair {
	public final String symbol;
	public final int relAddress;
	
	public SymbolPair(String symbol, int relAddress) {	
		this.symbol = symbol;
		this.relAddress = relAddress;
	}

}
