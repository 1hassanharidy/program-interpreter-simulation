package interpreter;

import java.io.*;
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
		int count = 0;
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

				
			}

			//Queues.ReadyQueue.add(newProcess);
			//System.out.println(s + " is Process " + timepid);
		}
		myReader.close();

		return count;
	}


	private void parse(String s, int timepid) {
		try {
			File myObj = new File(s + ".txt");
			Scanner myReader = new Scanner(myObj);
			Process newProcess = new Process();
			String processData;
			int count=0;
			int minbnd=ptr;
			
			newProcess.createPCB(timepid, State.READY, ptr+8, minbnd, ptr+8+getInstructionCount(s));
			memory[ptr++]=new MemoryData("pid of process " + timepid+":",timepid);
			//memory[ptr++].setData(timepid);
			memory[ptr++]=new MemoryData("state of process " + timepid+":",newProcess.pcb.state);
			//memory[ptr].setVariable("statePCB " + timepid);
			//memory[ptr++].setData(newProcess.pcb.state);
			memory[ptr++]=new MemoryData("pc of process " + timepid+";",newProcess.pcb.pc);
			//memory[ptr].setVariable("pcPCB " + timepid);
			//memory[ptr++].setData(newProcess.pcb.pc);
			memory[ptr++]=new MemoryData("minbound of process " + timepid+":",newProcess.pcb.minbound);
			//memory[ptr].setVariable("minboundPCB " + timepid);
			//memory[ptr++].setData(newProcess.pcb.minbound);
			memory[ptr++]=new MemoryData("maxbound of prcoess " + timepid+":",newProcess.pcb.maxbound);
			//memory[ptr].setVariable("maxboundPCB " + timepid);
			//memory[ptr++].setData(newProcess.pcb.maxbound);
			memory[ptr++]=new MemoryData("a","Null");
			memory[ptr++]=new MemoryData("b","Null");
			memory[ptr++]=new MemoryData("c","Null");
			while (myReader.hasNextLine()) {
				processData = myReader.nextLine();
				long spaceCounter = processData.chars().filter(ch -> ch == ' ').count();
				if (spaceCounter == 2) {
					String[] instructionParts = processData.split("\\s+", 3);
					if (instructionParts[0].equals("assign")) {
						if (instructionParts[2].toLowerCase().equals("input")) {
							memory[ptr]=new MemoryData();
							memory[ptr].setVariable("Instr"+count++);
							memory[ptr++].setData(instructionParts[2]);
							memory[ptr]=new MemoryData();
							memory[ptr].setVariable("Instr"+count++);
							memory[ptr++].setData(instructionParts[0] + " " + instructionParts[1]);
						} else {
							memory[ptr]=new MemoryData();
							memory[ptr].setVariable("Instr"+count++);
							memory[ptr++].setData(processData);
						}

					} else {
						memory[ptr]=new MemoryData();
						memory[ptr].setVariable("Instr"+count++);
						memory[ptr++].setData(processData);
					}

				} else if (spaceCounter == 3) {
					String[] instructionParts = processData.split("\\s+", 4);
					if (instructionParts[0].toLowerCase().equals("assign")
							&& instructionParts[2].toLowerCase().equals("readfile")) {
						memory[ptr]=new MemoryData();
						memory[ptr].setVariable("Instr"+count++);
						memory[ptr++].setData(instructionParts[2] + " " + instructionParts[3]);
						memory[ptr]=new MemoryData();
						memory[ptr].setVariable("Instr"+count++);
						memory[ptr++].setData(instructionParts[0] + " " + instructionParts[1]);
					}
				} else {
					memory[ptr]=new MemoryData();
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

	private void run(String[][] programs) throws Exception {
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
				if(this.getInstructionCount(program[1])+8 <= remSize) {
					this.parse(program[1], globalpid);

					remSize -= (this.getInstructionCount(program[1])+8);
				}
				else{
					//unload(program[1],timepid);

				}
				timepid++;
			} else {
				hm.put(Integer.parseInt(program[0]), globalpid);

			}
			globalpid++;

		}
		schedule(programs, mintime);

	}

	public void writeToDisk(Process p,int pid) throws Exception {
		File copy = new File(p.pcb.pid + ".txt");
		p.inDisk = true;
		//Scanner myReader = new Scanner(myObj);
		//newProcess.pcb.pid = timepid;
		String processData;
		int count = 0;
		int index = ptr;
		copy.createNewFile();
		FileWriter myWriter = new FileWriter(copy);

		//newProcess.createPCB(timepid, State.READY, minbnd, minbnd, ptr+7+getInstructionCount(s));
		myWriter.write("pidPCB " + p.pcb.pid); index++;
		myWriter.write("statePCB " + State.READY); index++;
		myWriter.write("pcPCB " + p.pcb.pc); index++;
		myWriter.write("minboundPCB " + p.pcb.minbound); index++;
		myWriter.write("maxboundPCB " + p.pcb.maxbound); index++;

		for (int i = p.pcb.minbound; i <= p.pcb.maxbound; i++) {
			String x = String.valueOf(memory[i]);
			long spaceCounter = x.chars().filter(ch -> ch == ' ').count();
			String[] y = x.split("//s");
			if (x.contains("assign")) {
				if(spaceCounter==1){
					if(p.processMemory.contains(y[1]))
					{
						for(MemoryData m: p.processMemory){
							if (m.getVariable() == y[1]) {
								myWriter.write(y[1]+" "+m.getData());
							}
						}
					}
					else{
						myWriter.write(y[1] + " Null");
					}
				}
				else{
					if(p.processMemory.contains(y[1]))
					{
						for(MemoryData m: p.processMemory){
							if (m.getVariable() == y[1]) {
								myWriter.write(y[1]+" "+m.getData());
							}
						}
					}
					else{
						myWriter.write(y[1] + " Null");
					}
					if(p.processMemory.contains(y[2])){
						for(MemoryData m: p.processMemory){
							if (m.getVariable() == y[2]) {
								myWriter.write(y[2]+" "+m.getData());
							}
						}
					}
					else{
						myWriter.write(y[2] + " Null");
					}

				}

			}
			myWriter.write("Instr" + count + " " + x);
			count++;
		}
		for (int i = p.pcb.minbound; i <= p.pcb.maxbound; i++) {
			String x = String.valueOf(memory[i]);
			long spaceCounter = x.chars().filter(ch -> ch == ' ').count();
			String[] y = x.split("//s");
			if (x.contains("assign")) {
				if (spaceCounter == 1) {
					if (p.processMemory.contains(y[1])) {
						for (MemoryData m : p.processMemory) {
							if (m.getVariable() == y[1]) {
								myWriter.write(y[1] + " " + m.getData());
							}
						}
					} else {
						myWriter.write(y[1] + " Null");
					}
				} else {
					if (p.processMemory.contains(y[1])) {
						for (MemoryData m : p.processMemory) {
							if (m.getVariable() == y[1]) {
								myWriter.write(y[1] + " " + m.getData());
							}
						}
					} else {
						myWriter.write(y[1] + " Null");
					}
					if (p.processMemory.contains(y[2])) {
						for (MemoryData m : p.processMemory) {
							if (m.getVariable() == y[2]) {
								myWriter.write(y[2] + " " + m.getData());
							}
						}
					} else {
						myWriter.write(y[2] + " Null");
					}

				}

			}
		}

		//System.out.println(s + " is Process " + timepid);
		System.out.println(copy.getCanonicalPath());
	}

	public Process readFromDisk(String s,Process p) throws FileNotFoundException {
		Scanner myReader = new Scanner(new File(s + ".txt"));
		Process newProcess = null;
//		for(Process p: Queues.ReadyQueue)
//		{
//			if (p.pcb.pid == Integer.parseInt(s)) {
//				newProcess = p;
//			}
//		}
		  newProcess=p;
		
		   newProcess.inDisk = false;

		String processData;
		String[] x = myReader.nextLine().split("\\s");
		int pidd = Integer.parseInt(x[1]);
		x = myReader.nextLine().split("\\s");
		State st = State.READY;
		x = myReader.nextLine().split("\\s");
		int pcc = Integer.parseInt(x[1]);
		 x = myReader.nextLine().split("\\s");
		int min = Integer.parseInt(x[1]);
		x = myReader.nextLine().split("\\s");
		int max = Integer.parseInt(x[1]);

		newProcess.createPCB(pidd, st, pcc, min, max);

		int ptr1 = newProcess.pcb.minbound;

		memory[ptr1].setVariable("pidPCB " + newProcess.pcb.pid);
		memory[ptr1++].setData(timepid);
		memory[ptr1].setVariable("statePCB " + newProcess.pcb.pid);
		memory[ptr1++].setData(newProcess.pcb.state);
		memory[ptr1].setVariable("pcPCB " + newProcess.pcb.pid);
		memory[ptr1++].setData(newProcess.pcb.pc);
		memory[ptr1].setVariable("minboundPCB " + newProcess.pcb.pid);
		memory[ptr1++].setData(newProcess.pcb.minbound);
		memory[ptr1].setVariable("maxboundPCB " + newProcess.pcb.pid);
		memory[ptr1++].setData(newProcess.pcb.maxbound);

		while (myReader.hasNextLine()) {
			processData = myReader.nextLine();
			int count = 0;
			long spaceCounter = processData.chars().filter(ch -> ch == ' ').count();
			if (spaceCounter == 3) {
				String[] instructionParts = processData.split("\\s+", 4);
				if (instructionParts[1].equals("assign")) {
					if (instructionParts[3].toLowerCase().equals("input")) {
						memory[ptr1].setVariable("Instr"+count++);
						memory[ptr1++].setData(instructionParts[3]);
						memory[ptr1].setVariable("Instr"+count++);
						memory[ptr1++].setData(instructionParts[1] + " " + instructionParts[2]);
					} else {
						memory[ptr1].setVariable("Instr"+count++);
						memory[ptr1++].setData(processData);
					}

				} else {
					memory[ptr1].setVariable("Instr"+count++);
					memory[ptr1++].setData(processData);
				}

			} else if (spaceCounter == 4) {
				String[] instructionParts = processData.split("\\s+", 5);
				if (instructionParts[1].toLowerCase().equals("assign")
						&& instructionParts[3].toLowerCase().equals("readfile")) {
					memory[ptr1].setVariable("Instr"+count++);
					memory[ptr1++].setData(instructionParts[3] + " " + instructionParts[4]);
					memory[ptr1].setVariable("Instr"+count++);
					memory[ptr1++].setData(instructionParts[1] + " " + instructionParts[2]);
				}
			}
			else if(spaceCounter == 2){
				String[] instructionParts = processData.split("\\s+", 3);
				memory[ptr1].setVariable(instructionParts[0]);
				memory[ptr1++].setData(instructionParts[1]);
			}
			else {
				memory[ptr1].setVariable("Instr" + count++);
				memory[ptr1++].setData(processData);
			}
		}
		return newProcess;
	}

	public static void copyContent(File a, File b)
			throws Exception
	{
		FileInputStream in = new FileInputStream(a);
		FileOutputStream out = new FileOutputStream(b);

		try {

			int n;

			// read() function to read the
			// byte of data
			while ((n = in.read()) != -1) {
				// write() function to write
				// the byte of data
				out.write(n);
			}
		}
		finally {
			if (in != null) {

				// close() function to close the
				// stream
				in.close();
			}
			// close() function to close
			// the stream
			if (out != null) {
				out.close();
			}
		}
		System.out.println("File Copied");
	}

	public Process getLongest(){
		Process x = Queues.ReadyQueue.get(0);
		for(Process p : Queues.ReadyQueue){
			if(p.timeInMem> x.timeInMem) x = p;
		}
		return x;
	}

	public void checktime(HashMap hm, String[][] programs, int time) throws Exception {

		if (hm.containsKey(time)) {

			if (hm.containsKey(time)) {
				for (String[] program : programs) {
					if (Integer.parseInt(program[0]) == time) {
						System.out.println("time "+time);
						if(this.getInstructionCount(program[1])+8 <= remSize) {
							parse(program[1], timepid);
							hm.remove(time);
							timepid++;
							remSize -= (this.getInstructionCount(program[1])+8);
						}
						else{
							Process remove = getLongest();
							writeToDisk(remove,remove.pcb.pid);
							remSize += remove.pcb.maxbound-remove.pcb.minbound + 1;
							remove = null;
							parse(program[1], timepid);
							hm.remove(time);
							timepid++;
							remSize -= (this.getInstructionCount(program[1])+8);

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
	private void schedule(String[][] programs, int mintime) throws Exception {
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
			
			if(inCPU.inDisk) {
				Process remove = getLongest();
				writeToDisk(remove, remove.pcb.pid);
				remSize += remove.pcb.maxbound - remove.pcb.minbound + 1;
				remove = null;
				readFromDisk(String.valueOf(inCPU.pcb.pid),inCPU);
				//parse(program[1], timepid);
			}
			inCPU.pcb.state=State.RUNNING;
			memory[inCPU.pcb.minbound+1].setData("Running");


			System.out.println("Process in CPU is Process " + inCPU.pcb.pid);



			boolean flag = false;

			int size = Queues.BlockedQueue.size();

			displayRQueue();

			System.out.println("Executing Process " + inCPU.pcb.pid);

			int executedInstructions = 0;
			while (executedInstructions < noOfInstructions) {
				printMemory(inCPU);
				if (inCPU.pcb.pc >= inCPU.pcb.maxbound)
					break;
				else {
					checktime(hm, programs, time);
					System.out.println("Out In");
					if(inCPU.inDisk)
						readFromDisk(String.valueOf(inCPU.pcb.pid),inCPU);
					System.out.println("\ntime " + time);

					System.out.print("\nExecuting instruction  ");
					memory[inCPU.pcb.pc].printMemData();
					executedInstructions++;


					//execute(inCPU);
					inCPU.pcb.pc++;
					inCPU.timeInMem++;
					incrementTime();
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
//					if (inCPU.instructions.get(inCPU.instructionIndex - 1).contains("semSignal")) {
//						System.out.println("semSignal occured ");
//						displayRQueue();
//						displayBQueue();
//						displayInputQueue();
//						displayMFileQueue();
//						displayOutputQueue();
//					}
				}
			}
			if (inCPU.pcb.pc < inCPU.pcb.maxbound)
				if (!Queues.ReadyQueue.contains(inCPU) && !Queues.BlockedQueue.contains(inCPU)) {
					//System.out.println("\ntime " + time);
					checktime(hm,programs,time);
					if(inCPU==null)
						break;
					Queues.ReadyQueue.addLast(inCPU);
					inCPU.pcb.state=State.READY;
					memory[inCPU.pcb.minbound+1].setData("Ready");

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


	public void incrementTime(){
		for(Process p: Queues.ReadyQueue){
			p.timeInMem++;
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

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Interpreter os = new Interpreter();
		String[][] programs = { { "0", "Program_1" }, { "1", "Program_2" }, { "4", "Program_3" } };
		for (MemoryData m : memory) {
			m = new MemoryData();
		}

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
	public static void printMemory(Process p) {
		System.out.println("MEMORY");
		LinkedList <Process>a=(LinkedList) Queues.ReadyQueue.clone();
		if(p!=null)
			a.add(p);
		
		if(a!=null) {
      Collections.sort(a, new Comparator<Process>() {
				    @Override
				    public int compare(Process o1, Process o2) {
				        return o2.pcb.pid >= o1.pcb.pid?1:0;
				    }
				});
		//System.out.println(a.size()); 
      int s=a.size();
	for (int i=1;i<=s;i++) {
		Process x=a.removeFirst();
		//System.out.println(x.inDisk);
		if (!x.inDisk) {
			
			for(int c=x.pcb.minbound;c<x.pcb.maxbound;c++ )
				memory[c].printMemData();
		}
		//System.out.println(a.size());
	}
	}}
}
