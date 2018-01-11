package main;

public class FailureJsonObject {
	
	public String debugMessage = "debugMessage";
	public int code = 0;

	
	public FailureJsonObject(int code, String message){
		this.code = code;
		this.debugMessage = message;
	}
	
	public String getJsonString(){
		String result = "";
		result += "{";
		result += "\"code\":" +code+ "\",";
		result += "\"debugMessage\":" +debugMessage+ "\"";
		result += "}";
		return result;
	}
	
	public int getLength(){
		return getJsonString().length();
	}
	
	public byte[] getBytes(){
		return getJsonString().getBytes();
	}
	
	public String toString(){
		return debugMessage;
	}
}
