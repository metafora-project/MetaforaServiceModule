package de.kuei.metafora.service.shared;

public class Base64 {

	private char[] baseCode;
	
	public Base64(){
		baseCode = new char[64];
		
		for(int i=0; i<26; i++){
			char c = (char)(i+'A');
			baseCode[i] = c;
		}
		
		for(int i=0; i<26; i++){
			char c = (char)(i+'a');
			baseCode[i+26] = c;
		}
		
		for(int i=0; i<10; i++){
			char c = (char)(i+'0');
			baseCode[i+26+26] = c;
		}
		
		baseCode[62] = '+';
		baseCode[63] = '/';
	}
	
	
	public String encodeStringForUrl(String text){
		String enc = encodeString(text);
		
		enc = enc.replaceAll("[+]", "%2B");
		enc = enc.replaceAll("[/]", "%2F");
		enc = enc.replaceAll("[=]", "%3D");
		
		return enc;
	}
	
	public String encodeString(String text){
		return encode(text.getBytes());
	}
	
	public String encode(byte[] bytes){
		int fill = 0;
		int len = bytes.length;
		
		String enc = "";
		
		while(len%3 != 0){
			len++;
			fill++;
		}
		
		if(bytes.length != len){
			bytes = arrayCopy(bytes, len);
		}
		
		for(int i=0; i<bytes.length; i+=3){
			enc += encodeBytes(bytes[i], bytes[i+1], bytes[i+2]);
		}
		
		enc = enc.substring(0, enc.length()-fill);
		
		for(int i=0; i<fill; i++){
			enc += "=";
		}
		
		return enc;
	}
	
	public String encodeBytes(byte a, byte b, byte c){
		byte u,v,w,x;
		
		u = (byte)((a&0xFC)>>2);
		v = (byte)( ((a&0x03)<<4) + ((b&0xF0)>>4));
		w = (byte)( ((b&0x0F)<<2) + ((c&0xC0)>>6));
		x = (byte)(c&0x3F);
		
		return ""+baseCode[u]+baseCode[v]+baseCode[w]+baseCode[x];
	}
	

	public String decodeStringForUrl(String text){
		text = text.replaceAll("%2B", "+");
		text = text.replaceAll("%2F", "/");
		text = text.replaceAll("%3D", "=");
		
		String dec = decodeString(text);
		
		return dec;
	}
	
	public String decodeString(String text){
		int fill = 0;
		
		while(text.charAt(text.length()-1-fill) == '='){
			fill++;
		}
		
		text = text.substring(0, text.length()-fill);
		
		for(int i=0; i<fill; i++){
			text += "A";
		}
		
		
		byte[] bytes = new byte[text.length()];
		
		for(int i=0; i<text.length(); i++){
			char c = text.charAt(i);
			for(int j=0; j<baseCode.length; j++){
				if(baseCode[j] == c){
					bytes[i] = (byte)j;
					break;
				}
			}
		}
		
		byte[] dec = decode(bytes);
		
		dec = arrayCopy(dec, dec.length-fill);
		
		return new String(dec);
	}
	
	public byte[] decode(byte[] bytes){
		byte[] dec = new byte[bytes.length];
		int decPointer = 0;
		
		for(int i=0; i<bytes.length; i+=4){
			byte[] decPart = decodeBytes(bytes[i], bytes[i+1], bytes[i+2], bytes[i+3]);
			
			for(int j=0; j<3; j++){
				dec[decPointer] = decPart[j];
				decPointer++;
			}
		}
		
		dec = arrayCopy(dec, decPointer);
		
		return dec;
	}
	
	public byte[] decodeBytes(byte a, byte b, byte c, byte d){
		byte[] dec = new byte[3];
		
		dec[0] = (byte)((a<<2) + ((b&0x30)>>4));
		dec[1] = (byte)(((b&0x0F)<<4) + ((c&0x3C)>>2));
		dec[2] = (byte)(((c&0x03)<<6) + (d&0x3F));
		
		
		return dec;
	}
	
	public byte[] arrayCopy(byte[] bytes, int newLength){
		byte[] ret = new byte[newLength];
		
		int copyLength = 0;
		if(bytes.length > newLength){
			copyLength = newLength;
		}else{
			copyLength = bytes.length; 
		}
		
		for(int i=0; i<copyLength; i++){
			ret[i] = bytes[i];
		}
		
		return ret;
	}
	
	
}
