package linker;

public class SyntaxException extends Exception {
	private int errorLine;
	private int errorOffset;
	
	public SyntaxException() {}
	public SyntaxException(String msg) {
		super(msg);
	}
	
//	public int getErrorLine() {
//		
//	}
}
