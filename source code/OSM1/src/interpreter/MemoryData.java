package interpreter;

public class MemoryData {
	private String variable;
	private String data;

	public MemoryData(String variable, String data) {
		this.data = data;
		this.variable = variable;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getVariable() {
		return variable;
	}

}
