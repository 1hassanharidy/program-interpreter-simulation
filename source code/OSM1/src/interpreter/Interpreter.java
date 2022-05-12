package interpreter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

public class Interpreter {

	private LinkedList timings = new LinkedList<>();
	public static int noOfInstructions = 2;
	public int globalpid;
	public int timepid = 1;
	int[][] processes;
	public static HashMap<Integer, Integer> hm;

	/*
	 * parse method is to read content of programs create a new process for that
	 * program add content of program to an Arraylist(instructions) of the new
	 * process add the new process to the ready queue lastly calls schedule method
	 */
	private void parse(String s, int timepid) {
		try {

			File myObj = new File(s + ".txt");
			Scanner myReader = new Scanner(myObj);
			Process newProcess = new Process();
			newProcess.pid = timepid;
			String processData;
			while (myReader.hasNextLine()) {
				processData = myReader.nextLine();
				long spaceCounter = processData.chars().filter(ch -> ch == ' ').count();
				if (spaceCounter == 2) {
					String[] instructionParts = processData.split("\\s+", 3);
					if (instructionParts[0].equals("assign")) {
						if (instructionParts[2].toLowerCase().equals("input")) {
							newProcess.instructions.add(instructionParts[2]);
							newProcess.instructions.add(instructionParts[0] + " " + instructionParts[1]);
						} else
							newProcess.instructions.add(processData);

					} else
						newProcess.instructions.add(processData);

				} else if (spaceCounter == 3) {
					String[] instructionParts = processData.split("\\s+", 4);
					if (instructionParts[0].toLowerCase().equals("assign")
							&& instructionParts[2].toLowerCase().equals("readfile")) {
						newProcess.instructions.add(instructionParts[2] + " " + instructionParts[3]);
						newProcess.instructions.add(instructionParts[0] + " " + instructionParts[1]);
					}
				} else
					newProcess.instructions.add(processData);
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

	private void run(String[][] programs) {
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
				this.parse(program[1], globalpid);
				timepid++;
			} else {
				hm.put(Integer.parseInt(program[0]), globalpid);

			}
			globalpid++;

		}
		schedule(programs, mintime);

	}

	public void checktime(HashMap hm, String[][] programs, int time) {

		if (hm.containsKey(time)) {

			if (hm.containsKey(time)) {
				for (String[] program : programs) {
					if (Integer.parseInt(program[0]) == time) {
						System.out.println("time "+time);
						parse(program[1], timepid);
						hm.remove(time);
						timepid++;

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
				System.out.println("Process " + p.pid);
			}
		}
	}

	// schedule method functionality implemented by person responsible for scheduler
	// part.
	// execute method is called to execute the instructions of the process.
	private void schedule(String[][] programs, int mintime) {
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

			System.out.println("Process in CPU is Process " + inCPU.pid);



			boolean flag = false;

			int size = Queues.BlockedQueue.size();

			displayRQueue();

			System.out.println("Executing Process " + inCPU.pid);

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
				System.out.println("\nProcess " + inCPU.pid + " has finished");

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
			if (p.instructionIndex < p.instructions.size()) {
				String executedInstruction = p.instructions.get(p.instructionIndex);
				if (p.instructionIndex > 0) {
					String previousInstruction = p.instructions.get(p.instructionIndex - 1);
				}

				long spaceCounter = executedInstruction.chars().filter(ch -> ch == ' ').count();
				p.instructionIndex++;
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
				p.instructions.clear();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private Interpreter() {
		Queues queues = new Queues();

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Interpreter os = new Interpreter();
		String[][] programs = { { "0", "Program_1" }, { "1", "Program_2" }, { "4", "Program_3" } };
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
				System.out.println("Process " + p.pid);
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
				System.out.println("Process " + p.pid);
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
				System.out.println("Process " + p.pid);
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
				System.out.println("Process " + p.pid);
			}
		}
		System.out.println();
	}
}
