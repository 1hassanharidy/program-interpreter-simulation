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

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String getVariable() {
		return variable;
	}

}
