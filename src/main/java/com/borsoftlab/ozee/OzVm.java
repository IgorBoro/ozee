package com.borsoftlab.ozee;

public class OzVm{

    /*
     * oZee Instruction Set Architecture (opcodes)
     */

    public static final byte OPCODE_STOP  = (byte) 0x00;

    /*
     * Arithmetic operations
     */

    public static final byte OPCODE_INC  = (byte) 0x01;
    public static final byte OPCODE_DEC  = (byte) 0x02;
    public static final byte OPCODE_ADD  = (byte) 0x03;
    public static final byte OPCODE_ADDF = (byte) 0x04;
    public static final byte OPCODE_SUB  = (byte) 0x05;
    public static final byte OPCODE_SUBF = (byte) 0x06;
    public static final byte OPCODE_MUL  = (byte) 0x07;
    public static final byte OPCODE_MULF = (byte) 0x08;
    public static final byte OPCODE_DIV  = (byte) 0x09;
    public static final byte OPCODE_DIVF = (byte) 0x0A;
    public static final byte OPCODE_MOD  = (byte) 0x0B;
    public static final byte OPCODE_NEG  = (byte) 0x0C;
    public static final byte OPCODE_NEGF = (byte) 0x0D;
    public static final byte OPCODE_CMP  = (byte) 0x0E;
    public static final byte OPCODE_INT  = (byte) 0x0F;
    public static final byte OPCODE_FLT  = (byte) 0x10;
    public static final byte OPCODE_LSL  = (byte) 0x11;
    public static final byte OPCODE_LSR  = (byte) 0x12;
    public static final byte OPCODE_ASL  =  OPCODE_LSL;
    public static final byte OPCODE_ASR  = (byte) 0x13;

    /*
     * Memory operations
     */
    public static final byte OPCODE_PUSH  = (byte) 0x20;    //         -> c    |  c == M[PC+3], M[PC+2], M[PC+1], M[PC]
    public static final byte OPCODE_EVAL  = (byte) 0x21;    //       A -> M[A] | 
    public static final byte OPCODE_EVALB = (byte) 0x22;
    public static final byte OPCODE_EVALS = (byte) 0x23;
    public static final byte OPCODE_ASGN  = (byte) 0x24;    //    x, A ->      |   M[A+3], M[A+2], M[A+1], M[A] = x
    public static final byte OPCODE_ASGNB = (byte) 0x25;
    public static final byte OPCODE_ASGNS = (byte) 0x26;


    /*
     *
     */
    public static final byte OPCODE_PUSHFP = (byte) 0x27;
    public static final byte OPCODE_POPFP  = (byte) 0x28;
    public static final byte OPCODE_PUSHPC = (byte) 0x29;
    public static final byte OPCODE_POPPC  = (byte) 0x2A;

    /*
     * Stack operations
     */
    public static final byte OPCODE_DUP   = (byte) 0x30;
    public static final byte OPCODE_DROP  = (byte) 0x31;
    public static final byte OPCODE_SWAP  = (byte) 0x32;
    public static final byte OPCODE_OVER  = (byte) 0x33;


    /*
     * Flow control commands
     */
    public static final byte OPCODE_JUMP = (byte) 0x40;
    public static final byte OPCODE_IFEQ = (byte) 0x41;
    public static final byte OPCODE_IFNE = (byte) 0x42;
    public static final byte OPCODE_IFLE = (byte) 0x43;
    public static final byte OPCODE_IFGE = (byte) 0x44;
    public static final byte OPCODE_IFGT = (byte) 0x45;
    public static final byte OPCODE_CALL = (byte) 0x46;
    public static final byte OPCODE_RET  = (byte) 0x47;

    private static final int DEF_MEMORY_SIZE = 1024;

    byte[] memory = new byte[128];  // little-endian

    public OzVm(){
        memory = new byte[DEF_MEMORY_SIZE];
    }

    public OzVm(final int memorySize){
        memory = new byte[memorySize];
    }

	public void loadProgram(byte[] program) {
        System.arraycopy(program, 0, memory, 0, program.length);
	}

    public void execute() {
        int pc = 0;
        int sp = memory.length - 5; // the stack is growing down, origin pos - little byte of free cell
        System.out.println("\noZee virtual machine started...");

        long startMillis = System.currentTimeMillis();

        byte cmd = memory[pc];

        while( cmd != OPCODE_STOP){
            ++pc;
            switch(cmd){
                case OPCODE_PUSH:   // push const to stack
                    System.arraycopy(memory, pc, memory, sp, 4);
                    sp -= 4; // stack is growing
                    pc += 4; // skip const in memory

                    System.out.println(OzUtils.fetchIntFromByteArray(memory, sp + 4));
                break;
                case OPCODE_EVAL: // expensive operation
                sp -= 4;
                int valueAddr = OzUtils.fetchIntFromByteArray(memory, sp - 4);
                System.arraycopy(memory, sp, memory, valueAddr, 4);
                //                    stack[sp] = memory[stack[sp]];
                break;
                case OPCODE_ASGN: // expensive operation
//                    OzUtils.storeIntValueToMemory(memory, stack[sp - 2], stack[sp - 1]);
//                    --sp;
                break;
                case OPCODE_DROP:
//                    --sp;
                break;
                case OPCODE_JUMP:
//                    pc = stack[--sp];
                break;
            }
            cmd = memory[pc];
        }
        long execTime = System.currentTimeMillis() - startMillis;
        System.out.println("oZee virtual machine stopped");
        System.out.println("Execution time: " + execTime + " ms");

    }
}