package com.borsoftlab.oZee;

public class OzVm{

    // opcode

    public static final byte OPCODE_STOP  = (byte) 0x00;

    /*
     * Arithmetic operations
     */
     public static final byte OPCODE_ADD  = (byte) 0x10;
     public static final byte OPCODE_ADDF = (byte) 0x11;
     public static final byte OPCODE_SUB  = (byte) 0x12;
     public static final byte OPCODE_SUBF = (byte) 0x13;
     public static final byte OPCODE_DIV  = (byte) 0x14;
     public static final byte OPCODE_DIVF = (byte) 0x15;
     public static final byte OPCODE_MOD  = (byte) 0x16;
     public static final byte OPCODE_NEG  = (byte) 0x17;
     public static final byte OPCODE_NEGF = (byte) 0x18;
     public static final byte OPCODE_CMP  = (byte) 0x19;

    /*
     * Memory operations
     */
    public static final byte OPCODE_PUSH  = (byte) 0x20;    //         -> C    |  C == M[PC+3], M[PC+2], M[PC+1], M[PC]
    public static final byte OPCODE_EVAL  = (byte) 0x21;    //       A -> M[A] | 
    public static final byte OPCODE_EVALB = (byte) 0x22;
    public static final byte OPCODE_EVALS = (byte) 0x23;
    public static final byte OPCODE_SAVE  = (byte) 0x24;
    public static final byte OPCODE_SAVEB = (byte) 0x25;
    public static final byte OPCODE_SAVES = (byte) 0x26;


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




    public void execute(byte[] mem){
        int[] stack = new int[64];
        int pc = 0;
        int sp = 0;
        System.out.println("\noZee virtual machine started...\n");


        byte cmd = mem[pc];

        while( cmd != OPCODE_STOP){
            ++pc;
            switch(cmd){
                case OPCODE_PUSH:
                    stack[sp++] = OzUtils.getIntValue(mem, pc);
                    pc += 4;
                    System.out.println(stack[sp-1]);
                break;
                case OPCODE_EVAL:
                    stack[sp] = mem[stack[sp]];
                break;

                case OPCODE_SAVE:
                    OzUtils.storeIntValue(mem, stack[sp - 2], stack[sp - 1]);
                    --sp;
                break;
                case OPCODE_DROP:
                    --sp;
                break;
                case OPCODE_JUMP:
                    pc = stack[--sp];
                break;
            }
            cmd = mem[pc];
        }
        System.out.println("\noZee virtual machine stoped.\n");


    }
    
}