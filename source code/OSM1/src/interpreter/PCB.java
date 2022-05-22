package interpreter;

public class PCB {
	int pid;
	State state;
	int pc;
	int minbound;
	int maxbound;

	public PCB(int id, State state, int pc, int boundary1, int boundary2) {

		this.pid = id;
		this.state = state;
		this.pc = pc;
		this.minbound = boundary1;
		this.maxbound = boundary2;
	}
}
