
public class CryptoData {

	private String string;
	private byte[] iv;
	
	public CryptoData(String text, byte[] iv) {
		this.string = string;
		this.iv = iv;
	}
	
	public String Text(){
		return string;
	}
	
	public byte[] IV(){
		return iv;
	}
}
