package interpreter;

import static interpreter.Memory.ptr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Process {
	public static ArrayList<MemoryData> processMemory; // changed from x to processMemory.
	protected ArrayList<String> instructions; // added to hold instructions of process.
	protected int instructionIndex = 0;
	private static final Scanner sc = new Scanner(System.in);
	protected PCB pcb;
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
	private String read(String var) {
		Object output=var;
		for (MemoryData d : this.processMemory) {
			if (d.getVariable().equals(var)) {
				output = d.getData();
			}
		}
		return output.toString();
	}
	
	protected void createPCB(int pid, State state, int pc, int minbound, int maxbound) {
		pcb = new PCB(pid, state, pc, minbound, maxbound);
	}
	
	// System calls
	protected void print(String val) {
		String output = read(val);
		
		System.out.print(output);
	}

	private void printVar(String var) {
		print(read(var));
	}

	protected String take() {
		System.out.print("[Process:" + pcb.pid + "]" + "Please enter a value\n");
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
		int val1 = Integer.parseInt(read(var1));
		int val2 = Integer.parseInt(read(var2));
		
			for (int i = val1; i <= val2; i++)
				print(i + " ");
		
	}

	protected void writeFile(String name, String val) {
		String fileName = read(name);
		String fileData = read(val);
		try {
			

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
			String fileName = read(name);

			
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
