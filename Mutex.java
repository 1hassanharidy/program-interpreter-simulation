package interpreter;

import java.util.LinkedList;

public class Mutex {

	private boolean locked = false;
	LinkedList<Process> queue = new LinkedList<Process>();
	Process owner;
	static Mutex AccessingFile = new Mutex();
	static Mutex OutputtingOnScreen = new Mutex();
	static Mutex TakingInput = new Mutex();

	public Mutex() {
		this.setLocked(false);

	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

}