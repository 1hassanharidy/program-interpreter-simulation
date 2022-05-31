package interpreter;

public class MemoryData {
	private String variable;

	public void setVariable(String variable) {
		this.variable = variable;
	}

	private Object data;

	public MemoryData(String variable, Object data) {
		this.data = data;
		this.variable = variable;
	}
	public MemoryData() {
		this.data = null;
		this.variable = null;
	}
	public Object getData() {
		return data;
	}
	
	public void incrementData() {
		try {
			 this.data=(Object)((int)this.data + 1);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String getVariable() {
		return variable;
	}
	public void printMemData() {
		System.out.println(this.variable+" "+String.valueOf(this.data));		
	}

}
