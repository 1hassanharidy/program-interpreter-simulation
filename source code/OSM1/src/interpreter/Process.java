package interpreter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Process {
	private ArrayList<MemoryData> processMemory; // changed from x to processMemory.
	protected ArrayList<String> instructions; // added to hold instructions of process.
	protected int instructionIndex = 0;
	private static final Scanner sc = new Scanner(System.in);
	protected int pid;
	// readFileArg is the variable that holds the return value of readFile in assign
	// x readFile y
	protected String readFileArg = "";
	// readFlag is to specify whether the readFile a has been executed.
	protected boolean readFlag = false;

	// inputArg is the variable that holds the return value of input in assign x
	// input
	protected String inputArg = "";
	// inputFlag is to specify whether the input has been scanned.
	protected boolean inputFlag = false;

	public Process() {
		this.processMemory = new ArrayList<MemoryData>();
		this.instructions = new ArrayList<String>();
	}

	// System calls
	protected void print(String val) {
		String output = null;
		for (MemoryData d : this.processMemory) {
			if (d.getVariable().equals(val)) {
				output = d.getData();
			}
		}
		System.out.print(output);
	}

	private void printVar(String var) {
		for (MemoryData d : this.processMemory) {
			if (d.getVariable().equals(var))
				this.print(d.getData());
		}
	}

	protected String take() {
		System.out.print("[Process:" + this.pid + "]" + "Please enter a value\n");
		String val = sc.nextLine();
		return val;
	}

	protected void assign(String var, String inp) {
		boolean found = false;
		for (MemoryData d : this.processMemory) {

			if (d.getVariable().equals(var)) {
				d.setData(inp);
				found = true;
				break;
			}

		}
		if (!found) {
			this.processMemory.add(new MemoryData(var, inp));
		}
	}

	protected void printFromTo(String var1, String var2) {
		int val1 = 0;
		int val2 = 0;
		boolean found1 = false;
		boolean found2 = false;
		for (MemoryData d : this.processMemory) {
			if (d.getVariable().equals(var1)) {
				val1 = Integer.parseInt(d.getData());
				found1 = true;
			}
			if (d.getVariable().equals(var2)) {
				val2 = Integer.parseInt(d.getData());
				found2 = true;
			}
		}
		if (found1 && found2 && val1 <= val2) {
			for (int i = val1; i <= val2; i++)
				System.out.print(i + " ");
		}
	}

	protected void writeFile(String name, String val) {
		String fileName = name;
		String fileData = val;
		try {
			for (MemoryData d : this.processMemory) {
				if (d.getVariable().equals(name)) {
					fileName = d.getData();
				}
				if (d.getVariable().equals(val)) {
					fileData = d.getData();
				}
			}

			File myObj = new File(fileName + ".txt");
			if (myObj.createNewFile()) {
				System.out.println("File created: " + myObj.getName());
				System.out.println(myObj.getCanonicalPath());
			} else {
				System.out.println("File already exists.");
				System.out.println(myObj.getCanonicalPath());
			}

			FileWriter myWriter = new FileWriter(fileName + ".txt");
			myWriter.write(fileData);
			myWriter.close();
			System.out.println("Successfully wrote to the file.");

		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}

	}

	protected String readFile(String name) {
		try {
			String fileName = name;

			for (MemoryData d : this.processMemory) {
				if (d.getVariable().equals(name)) {
					fileName = d.getData();
				}
			}
			File myObj = new File(fileName + ".txt");
			Scanner myReader = new Scanner(myObj);
			String data = myReader.nextLine();
			while (myReader.hasNextLine()) {
				data = data + "\n" + myReader.nextLine();
				// System.out.println(data);
			}
			myReader.close();
			return data;
		} catch (FileNotFoundException e) {
			System.out.println("The system cannot find the file specified");
		}
		return name;

	}

	protected void semWait(Mutex mutex) {

		if (mutex.isLocked() && (!mutex.queue.contains(this))) {
			mutex.queue.addLast(this);
			Queues.BlockedQueue.addLast(this);
			return;
		}

		else if (mutex.isLocked() && (mutex.queue.contains(this))) {
			return;
		}

		else {

			mutex.owner = this;
			mutex.setLocked(true);
		}
	}

	protected void semSignal(Mutex mutex) {

		if (mutex.isLocked() && mutex.owner == this) {

			mutex.setLocked(false);
			if (!mutex.queue.isEmpty()) {
				mutex.owner = mutex.queue.getFirst();
				mutex.setLocked(true);
				// mutex.queue.removeFirst();

				Queues.BlockedQueue.remove(mutex.owner);
				Queues.ReadyQueue.addLast(mutex.queue.removeFirst());
			} else {
				mutex.owner = null;
			}
		}
	}
}
