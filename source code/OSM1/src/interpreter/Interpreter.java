package interpreter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

import static interpreter.Memory.*;

public class Interpreter {

	private LinkedList timings = new LinkedList<>();
	public static int noOfInstructions = 5;
	public int globalpid;
	public int timepid = 1;
	int[][] processes;
	public static HashMap<Integer, Integer> hm;

	/*
	 * parse method is to read content of programs create a new process for that
	 * program add content of program to an Arraylist(instructions) of the new
	 * process add the new process to the ready queue lastly calls schedule method
	 */

	private int getInstructionCount(String s) throws FileNotFoundException {
		File myObj = new File(s + ".txt");
		Scanner myReader = new Scanner(myObj);

		String processData;
		int count = 4;
		while (myReader.hasNextLine()) {
			processData = myReader.nextLine();
			long spaceCounter = processData.chars().filter(ch -> ch == ' ').count();
			if (spaceCounter == 2) {
				String[] instructionParts = processData.split("\\s+", 3);
				if (instructionParts[0].equals("assign")) {
					if (instructionParts[2].toLowerCase().equals("input")) {
						count += 2;

					} else
						count++;

				} else
					count++;

			} else if (spaceCounter == 3) {
				String[] instructionParts = processData.split("\\s+", 4);
				if (instructionParts[0].toLowerCase().equals("assign")
						&& instructionParts[2].toLowerCase().equals("readfile")) {
					count += 2;
				} else
					count++;

				myReader.close();
			}

			//Queues.ReadyQueue.add(newProcess);
			//System.out.println(s + " is Process " + timepid);
		}
		return count;
	}


	private void parse(String s, int timepid) {
		try {

			File myObj = new File(s + ".txt");
			Scanner myReader = new Scanner(myObj);
			Process newProcess = new Process();
			newProcess.pcb.pid = timepid;
			String processData;
			int count=0;
			int minbnd=ptr;
			
			newProcess.createPCB(timepid, State.READY, minbnd, minbnd, ptr+7+getInstructionCount(s));
			memory[ptr].setVariable("pidPCB " + timepid);
			memory[ptr++].setData(timepid);
			memory[ptr].setVariable("statePCB " + timepid);
			memory[ptr++].setData(newProcess.pcb.state);
			memory[ptr].setVariable("pcPCB " + timepid);
			memory[ptr++].setData(newProcess.pcb.pc);
			memory[ptr].setVariable("minboundPCB " + timepid);
			memory[ptr++].setData(newProcess.pcb.minbound);
			memory[ptr].setVariable("maxboundPCB " + timepid);
			memory[ptr++].setData(newProcess.pcb.maxbound);
			ptr++;

			while (myReader.hasNextLine()) {
				processData = myReader.nextLine();
				long spaceCounter = processData.chars().filter(ch -> ch == ' ').count();
				if (spaceCounter == 2) {
					String[] instructionParts = processData.split("\\s+", 3);
					if (instructionParts[0].equals("assign")) {
						if (instructionParts[2].toLowerCase().equals("input")) {
							memory[ptr].setVariable("Instr"+count++);
							memory[ptr++].setData(instructionParts[2]);
							memory[ptr].setVariable("Instr"+count++);
							memory[ptr++].setData(instructionParts[0] + " " + instructionParts[1]);
						} else {
							memory[ptr].setVariable("Instr"+count++);
							memory[ptr++].setData(processData);
						}

					} else {
						memory[ptr].setVariable("Instr"+count++);
						memory[ptr++].setData(processData);
					}

				} else if (spaceCounter == 3) {
					String[] instructionParts = processData.split("\\s+", 4);
					if (instructionParts[0].toLowerCase().equals("assign")
							&& instructionParts[2].toLowerCase().equals("readfile")) {
						memory[ptr].setVariable("Instr"+count++);
						memory[ptr++].setData(instructionParts[2] + " " + instructionParts[3]);
						memory[ptr].setVariable("Instr"+count++);
						memory[ptr++].setData(instructionParts[0] + " " + instructionParts[1]);
					}
				} else {
					memory[ptr].setVariable("Instr" + count++);
					memory[ptr++].setData(processData);
				}
			}
			myReader.close();
			Queues.ReadyQueue.add(newProcess);
			System.out.println(s + " is Process " + timepid);
			// schedule(newProcess);
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}

	private void run(String[][] programs) throws FileNotFoundException {
		hm = new HashMap<>();
		globalpid = 1;

		Arrays.sort(programs, new Comparator<String[]>() {

			@Override
			public int compare(final String[] entry1, final String[] entry2) {

				// To sort in descending order revert
				// the '>' Operator
				if (Integer.parseInt(entry1[0]) >= Integer.parseInt(entry2[0]))
					return 1;
				else
					return -1;
			}
		});

		for (String[] program : programs) {

			timings.add(Integer.parseInt(program[0]));

		}
		int mintime = (int) Collections.min(timings);
		for (String[] program : programs) {
			if (Integer.parseInt(program[0]) == mintime) {
				System.out.println("time "+mintime);
				if(this.getInstructionCount(program[1]) >= remSize) {
					this.parse(program[1], globalpid);
					timepid++;
					remSize -= this.getInstructionCount(program[1]);
				}
				else{

				}
			} else {
				hm.put(Integer.parseInt(program[0]), globalpid);

			}
			globalpid++;

		}
		schedule(programs, mintime);

	}

	public void checktime(HashMap hm, String[][] programs, int time) throws FileNotFoundException {

		if (hm.containsKey(time)) {

			if (hm.containsKey(time)) {
				for (String[] program : programs) {
					if (Integer.parseInt(program[0]) == time) {
						System.out.println("time "+time);
						if(this.getInstructionCount(program[1]) >= remSize) {
							parse(program[1], timepid);
							hm.remove(time);
							timepid++;
							remSize -= this.getInstructionCount(program[1]);
						}
						else{

						}


					}
				}
			}
		}
	}

	public void displayRQueue() {
		System.out.println("\nReadyQueue \n");
		if (Queues.ReadyQueue.isEmpty())
			System.out.println("Empty");
		else {
			for (Process p : Queues.ReadyQueue) {
				System.out.println("Process " + p.pcb.pid);
			}
		}
	}

	// schedule method functionality implemented by person responsible for scheduler
	// part.
	// execute method is called to execute the instructions of the process.
	private void schedule(String[][] programs, int mintime) throws FileNotFoundException {
		int time = mintime;
		while (!Queues.ReadyQueue.isEmpty() || !hm.isEmpty()) {
			if (Queues.ReadyQueue.isEmpty()) {
				System.out.println("time " + time);
				checktime(hm, programs, time);
				time++;

				continue;
			}

			checktime(hm, programs, time);

			Process inCPU = Queues.ReadyQueue.removeFirst();

			System.out.println("Process in CPU is Process " + inCPU.pcb.pid);



			boolean flag = false;

			int size = Queues.BlockedQueue.size();

			displayRQueue();

			System.out.println("Executing Process " + inCPU.pcb.pid);

			int executedInstructions = 0;
			while (executedInstructions < noOfInstructions) {
				if (inCPU.instructionIndex >= inCPU.instructions.size())
					break;
				else {
					checktime(hm, programs, time);
					System.out.println("\ntime " + time);

					System.out.println("\nExecuting instruction of index " + inCPU.instructionIndex+": ");
					System.out.println(inCPU.instructions.get(inCPU.instructionIndex));
					executedInstructions++;


					execute(inCPU);

					time++;
					if (Queues.BlockedQueue.contains(inCPU)) {
						System.out.println("\nProcess Blocked\n");
						size = Queues.BlockedQueue.size();
						displayBQueue();
						displayInputQueue();
						displayMFileQueue();
						displayOutputQueue();
						// time++;
						// checktime(hm,programs,time);
						System.out.println("\n-----------------------------------");
						break;

					}
					else{
						System.out.println("\nInstruction Done\n");
					}
					if (inCPU.instructions.get(inCPU.instructionIndex - 1).contains("semSignal")) {
						System.out.println("semSignal occured ");
						displayRQueue();
						displayBQueue();
						displayInputQueue();
						displayMFileQueue();
						displayOutputQueue();
					}
				}
			}
			if (inCPU.instructionIndex < inCPU.instructions.size())
				if (!Queues.ReadyQueue.contains(inCPU) && !Queues.BlockedQueue.contains(inCPU)) {
					//System.out.println("\ntime " + time);
					checktime(hm,programs,time);
					Queues.ReadyQueue.addLast(inCPU);

				}
				else {
					continue;
				}
			else {
				// if(Queues.ReadyQueue.isEmpty() && Queues.BlockedQueue.isEmpty())
				// return;
				System.out.println("\ntime " + time);
				System.out.println("\nProcess " + inCPU.pcb.pid + " has finished");

			}
			System.out.println("\n-----------------------------------");

		}

	}



	/*
	 * [Deprecated] execute method executes one instruction at a time. (one
	 * instruction/function call) it starts by comparing the process'
	 * instructionIndex variable with size of instructions Array list the current
	 * instruction is saved in a variable called executedInstruction
	 * instructionIndex of process is incremented spaceCounter variable contains the
	 * number of empty spaces per instruction then we check if spaceCounter ==1 ->
	 * Choice is limited to instructions with 1 empty spaces using switch cases if
	 * spaceCounter ==2 -> Choice is limited to instructions with 2 empty spaces
	 * using switch cases if spaceCounter ==3 -> Only 1 choice which is the one in
	 * Program_3 (assign b readFile a) (assign b readFile a) is split into 2
	 * instructions by using readFileArg & readFlag variables
	 */
	private static void execute(Process p) {
		try {
			int pc = 0;
			int pcMemIndex = -1;
			for (MemoryData d : memory) {
				if ((int)d.getData() == p.pcb.pc) {
					pc = (int)d.getData();
					pcMemIndex++;
					break;
				}
				pcMemIndex++;
			}
			int lastInst = p.pcb.maxbound;
			if (pc <= lastInst) {
				String executedInstruction = (String) memory[pc].getData();

				long spaceCounter = executedInstruction.chars().filter(ch -> ch == ' ').count();
				p.pcb.pc++;
				memory[pcMemIndex].incrementData();
				if (spaceCounter == 0 && executedInstruction.toLowerCase().equals("input")) {
					if (!p.inputFlag) {
						p.inputArg = p.take();
						p.inputFlag = true;
					}
				} else if (spaceCounter == 1) {
					String[] instructionParts = executedInstruction.split("\\s+", 2);
					switch (instructionParts[0].toLowerCase()) {
					case "semwait": {
						switch (instructionParts[1].toLowerCase()) {
						case "userinput": {
							p.semWait(Mutex.TakingInput);
							break;
						}
						case "useroutput": {
							p.semWait(Mutex.OutputtingOnScreen);
							break;
						}
						case "file": {
							p.semWait(Mutex.AccessingFile);
							break;
						}
						default:
							throw new IllegalArgumentException(
									"Unexpected value: " + instructionParts[1].toLowerCase());
						}
						break;
					}
					case "semsignal": {
						switch (instructionParts[1].toLowerCase()) {
						case "userinput": {
							p.semSignal(Mutex.TakingInput);
							break;
						}
						case "useroutput": {
							p.semSignal(Mutex.OutputtingOnScreen);
							break;
						}
						case "file": {
							p.semSignal(Mutex.AccessingFile);
							break;
						}
						default:
							throw new IllegalArgumentException(
									"Unexpected value: " + instructionParts[1].toLowerCase());
						}
						break;
					}
					case "print": {
						p.print(instructionParts[1]);
						break;
					}

					case "assign": {
						if (p.inputFlag) {
							p.assign(instructionParts[1], p.inputArg);
							p.inputFlag = false;
						} else if (p.readFlag) {
							p.assign(instructionParts[1], p.readFileArg);
							p.readFlag = false;
						}
						break;
					}
					case "readfile": {
						if (!p.readFlag) {
							p.readFileArg = p.readFile(instructionParts[1]);
							p.readFlag = true;
						}
						break;
					}
					default:
						throw new IllegalArgumentException("Unexpected value: " + instructionParts[0].toLowerCase());
					}
				} else if (spaceCounter == 2) {
					String[] instructionParts = executedInstruction.split("\\s+", 3);
					switch (instructionParts[0].toLowerCase()) {
					case "assign":
						p.assign(instructionParts[1], instructionParts[2]);
						break;
					case "writefile": {
						p.writeFile(instructionParts[1], instructionParts[2]);
						break;
					}
					case "printfromto": {
						p.printFromTo(instructionParts[1], instructionParts[2]);
						break;
					}
					default:
						throw new IllegalArgumentException("Unexpected value: " + instructionParts[0].toLowerCase());
					}

				}
			} else {
				p.pcb.state = State.FINISHED;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private Interpreter() {
		Queues queues = new Queues();

	}

	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		Interpreter os = new Interpreter();
		String[][] programs = { { "4", "Program_1" }, { "0", "Program_2" }, { "6", "Program_3" } };
		os.run(programs);

	}

	public int getNoOfInstructions() {
		return noOfInstructions;
	}

	public void setNoOfInstructions(int noOfInstructions) {
		Interpreter.noOfInstructions = noOfInstructions;
	}


	public void displayBQueue() {
		System.out.println();
		System.out.println("BlockedQueue \n");
		if (Queues.BlockedQueue.isEmpty())
			System.out.println("Empty");
		else {

			for (Process p : Queues.BlockedQueue) {
				System.out.println("Process " + p.pcb.pid);
			}
		}
		System.out.println();
	}

	public void displayMFileQueue() {
		System.out.println();
		System.out.println("Accessing File Queue \n");
		if (Mutex.AccessingFile.queue.isEmpty())
			System.out.println("Empty");
		else {

			for (Process p : Mutex.AccessingFile.queue) {
				System.out.println("Process " + p.pcb.pid);
			}
		}
		System.out.println();
	}
	public void displayOutputQueue() {
		System.out.println();
		System.out.println("Output Queue \n");
		if (Mutex.OutputtingOnScreen.queue.isEmpty())
			System.out.println("Empty");
		else {

			for (Process p : Mutex.OutputtingOnScreen.queue) {
				System.out.println("Process " + p.pcb.pid);
			}
		}
		System.out.println();
	}

	public void displayInputQueue() {
		System.out.println();
		System.out.println("Input Queue \n");
		if (Mutex.TakingInput.queue.isEmpty())
			System.out.println("Empty");
		else {

			for (Process p : Mutex.TakingInput.queue) {
				System.out.println("Process " + p.pcb.pid);
			}
		}
		System.out.println();
	}
}
