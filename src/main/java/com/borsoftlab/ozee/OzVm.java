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

    byte[] ram;  // little-endian
    int ramSizeInBytes = 1024;

    int[] stack;
    int stackSizeInWords = 32;

    public OzVm(){
    }

    public OzVm(final int ramSize){
        this.ramSizeInBytes = ramSize;
    }

	public void loadProgram(byte[] image) {
        ram = new byte[ramSizeInBytes];
        System.arraycopy(image, 0, ram, 0, image.length);
	}

    public byte[] getRam(){
        return ram;
    }

    public void execute() throws Exception {
        int pc = 0;
        stack = new int[stackSizeInWords];
        int sp = 0; // the stack is growing up
        System.out.println("\noZee virtual machine started...");

        long startMillis = System.currentTimeMillis();

        byte cmd = ram[pc];

        while( cmd != OPCODE_STOP){
            pc++;
            int valueAddr, value, lvalue, rvalue;
            switch(cmd){
                case OPCODE_PUSH:   // push const to stack - expensive operation
                    value = OzUtils.fetchIntFromByteArray(ram, pc);
                    stack[sp++] = value;
                    pc += 4; // skip const in memory
                    System.out.print(OzAsm.getInstance().getMnemonic(cmd));
                    System.out.println(
                        String.format(" 0x%08X", value));
                    break;
                case OPCODE_EVAL: // push value to stack expensive operation
                    stack[sp - 1] = OzUtils.fetchIntFromByteArray(ram, stack[sp - 1]);
                    System.out.println(OzAsm.getInstance().getMnemonic(cmd));
                    break;
                case OPCODE_ASGN: // get value from stack and store it to memory - expensive operation
                    valueAddr = stack[--sp];
                    OzUtils.storeIntToByteArray(ram, valueAddr, stack[--sp]);
                    System.out.println(OzAsm.getInstance().getMnemonic(cmd));
                    break;
                case OPCODE_ADD:
                    rvalue = stack[--sp];
                    lvalue = stack[--sp];
                    stack[sp++] = lvalue + rvalue;
                    System.out.println(OzAsm.getInstance().getMnemonic(cmd));
                    break;
                case OPCODE_SUB:
                    rvalue = stack[--sp];
                    lvalue = stack[--sp];
                    stack[sp++] = lvalue - rvalue;
                    System.out.println(OzAsm.getInstance().getMnemonic(cmd));
                    break;
                case OPCODE_MUL:
                    rvalue = stack[--sp];
                    lvalue = stack[--sp];
                    stack[sp++] = lvalue * rvalue;
                    System.out.println(OzAsm.getInstance().getMnemonic(cmd));
                    break;
                default:
                    throw new Exception(String.format("OzVm RTE: Unknown opcode - 0x%08X", cmd));
            }
            System.out.print("[ ");
            for( int ptr = 0; ptr < sp; ptr++ ){
                value = stack[ptr];
                System.out.print(String.format("0x%08X ", value));
            }
            System.out.println("] <- top");
            cmd = ram[pc];
        }
        long execTime = System.currentTimeMillis() - startMillis;
        System.out.println("oZee virtual machine stopped");
        System.out.println("Execution time: " + execTime + " ms");
    }

    public void printMemoryDump(){
        printMemoryDump(0, ram.length-1);
    }

    public void printMemoryDump(int from, int to){
        if( from % 16 == 0){
            System.out.print(String.format("0x%08X: 0x%02X", from, ram[from]));
        } else {
            int start = 16 * (from / 16);
            System.out.print(String.format("0x%08X:", start));
            for( int ptr = start; ptr < from; ptr ++ ){
                System.out.print("     ");
            }
            System.out.print(String.format(" 0x%02X", ram[from]));

        }
        from++;
        for (int ptr = from; ptr <= to; ptr++){
            if( ptr % 16 == 0){
                System.out.println();                
                System.out.print(String.format("0x%08X: 0x%02X", ptr, ram[ptr]));
            } else {
                System.out.print(String.format(" 0x%02X", ram[ptr]));
            }
        }
        System.out.println();                
    }
}