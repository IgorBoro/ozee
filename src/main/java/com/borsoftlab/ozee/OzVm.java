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
    // shifts
    public static final byte OPCODE_LSL  = (byte) 0x11;
    public static final byte OPCODE_LSR  = (byte) 0x12;
    public static final byte OPCODE_ASR  = (byte) 0x13;
    public static final byte OPCODE_ROR  = (byte) 0x14;

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
    public static final byte OPCODE_JUMP = (byte) 0x40;         //        c ->     |  pc = c
    public static final byte OPCODE_IFEQ = (byte) 0x41;
    public static final byte OPCODE_IFNE = (byte) 0x42;
    public static final byte OPCODE_IFLE = (byte) 0x43;
    public static final byte OPCODE_IFGE = (byte) 0x44;
    public static final byte OPCODE_IFGT = (byte) 0x45;
    public static final byte OPCODE_CALL = (byte) 0x46;
    public static final byte OPCODE_RET  = (byte) 0x47;


    public static final int EVENT_BEFORE_EXECUTING   = 0;
    public static final int EVENT_AFTER_EXECUTING    = 1;
    public static final int EVENT_OPTIONAL_ARGUMENT  = 2;

    byte[] ram;  // little-endian

    int[] stack;
    int stackSizeInWords = 32;
    OnOzVmSupervisingListener supervisor;

    int pc;
    int sp;

    public OzVm(){
        init();
    }

    public void init(){
        stack = new int[stackSizeInWords];
    }

    public void setDebugListener(OnOzVmSupervisingListener debugListener){
        if (debugListener instanceof OnOzVmSupervisingListener) {
            this.supervisor = (OnOzVmSupervisingListener) debugListener;
        } else {
            throw new RuntimeException(debugListener.toString()
                    + " must implement OnOzVmDebugListener interface");
        }
    }
    
	public void loadProgram(byte[] image) {
        ram = new byte[image.length];
        System.arraycopy(image, 0, ram, 0, image.length);
	}

    public byte[] getRam(){
        return ram;
    }

    public void execute() throws Exception {
        pc = 0; // starts from 0
        sp = 0; // the stack is growing up

        byte cmd = ram[pc++];
        while( cmd != OPCODE_STOP){
            int valueAddr, int_value, l_int_value, r_int_value;
            float  l_flt_value, r_flt_value;
            if( supervisor != null ){
                supervisor.onEventInterceptor(EVENT_BEFORE_EXECUTING, pc, cmd, stack, sp);
            }
            switch(cmd){
                case OPCODE_PUSH:   // push const to stack - expensive operation
                    int_value = OzUtils.fetchIntFromByteArray(ram, pc);
                    stack[sp++] = int_value;
                    pc += 4; // skip const in memory
                    if( supervisor != null ){
                        supervisor.onEventInterceptor(EVENT_OPTIONAL_ARGUMENT, pc, int_value, stack, sp);
                    }
                    break;
                case OPCODE_EVAL: // push value to stack expensive operation
                    stack[sp - 1] = OzUtils.fetchIntFromByteArray(ram, stack[sp - 1]);
                    break;
                case OPCODE_EVALB: // push value to stack expensive operation
                    stack[sp - 1] = OzUtils.fetchByteFromByteArray(ram, stack[sp - 1]);
                    break;
                case OPCODE_EVALS: // push value to stack expensive operation
                    stack[sp - 1] = OzUtils.fetchShortFromByteArray(ram, stack[sp - 1]);
                    break;
                case OPCODE_ASGN: // get value from stack and store it to memory - expensive operation
                    valueAddr = stack[--sp];
                    OzUtils.storeIntToByteArray(ram, valueAddr, stack[--sp]);
                    break;
                case OPCODE_ASGNB: // get value from stack and store it to memory - expensive operation
                    valueAddr = stack[--sp];
                    OzUtils.storeByteToByteArray(ram, valueAddr, stack[--sp]);
                    break;
                case OPCODE_ASGNS: // get value from stack and store it to memory - expensive operation
                    valueAddr = stack[--sp];
                    OzUtils.storeShortToByteArray(ram, valueAddr, stack[--sp]);
                    break;
                case OPCODE_SWAP:
                    int_value     = stack[sp - 2];
                    stack[sp - 2] = stack[sp - 1];
                    stack[sp - 1] = int_value;
                    break;    
                case OPCODE_NEG:
                    stack[sp - 1] = -stack[sp - 1];
                    break;
                case OPCODE_ADD:
                    r_int_value = stack[--sp];
                    l_int_value = stack[--sp];
                    stack[sp++] = l_int_value + r_int_value;
                    break;
                case OPCODE_SUB:
                    r_int_value = stack[--sp];
                    l_int_value = stack[--sp];
                    stack[sp++] = l_int_value - r_int_value;
                    break;
                case OPCODE_MUL:
                    r_int_value = stack[--sp];
                    l_int_value = stack[--sp];
                    stack[sp++] = l_int_value * r_int_value;
                    break;
                case OPCODE_DIV:
                    r_int_value = stack[--sp];
                    l_int_value = stack[--sp];
                    stack[sp++] = l_int_value / r_int_value;
                    break;
                case OPCODE_INT:
                    stack[sp - 1] = (int)Float.intBitsToFloat(stack[sp - 1]);
                    break;
                case OPCODE_FLT:
                    stack[sp - 1] = Float.floatToIntBits(stack[sp - 1]);
                    break;     
                case OPCODE_NEGF:
                    stack[sp - 1] = Float.floatToIntBits(- Float.intBitsToFloat(stack[sp - 1]));
                    break;
                case OPCODE_ADDF:
                    r_flt_value = Float.intBitsToFloat(stack[--sp]);
                    l_flt_value = Float.intBitsToFloat(stack[--sp]);
                    stack[sp++] = Float.floatToIntBits(l_flt_value + r_flt_value);
                    break;
                case OPCODE_SUBF:
                    r_flt_value = Float.intBitsToFloat(stack[--sp]);
                    l_flt_value = Float.intBitsToFloat(stack[--sp]);
                    stack[sp++] = Float.floatToIntBits(l_flt_value - r_flt_value);
                    break;
                case OPCODE_MULF:
                    r_flt_value = Float.intBitsToFloat(stack[--sp]);
                    l_flt_value = Float.intBitsToFloat(stack[--sp]);
                    stack[sp++] = Float.floatToIntBits(l_flt_value * r_flt_value);
                    break;
                case OPCODE_DIVF:
                    r_flt_value = Float.intBitsToFloat(stack[--sp]);
                    l_flt_value = Float.intBitsToFloat(stack[--sp]);
                    stack[sp++] = Float.floatToIntBits(l_flt_value / r_flt_value);
                    break;
                case OPCODE_LSL:
                    int shift = stack[--sp];
                    stack[sp-1] = (stack[sp-1] << shift);
                    break;
                case OPCODE_LSR:
                    shift = stack[--sp];
                    stack[sp-1] = (stack[sp-1] >>> shift);
                    break;
                case OPCODE_ASR:
                    shift = stack[--sp];
                    stack[sp-1] = (stack[sp-1] >> shift);
                    break;
                case OPCODE_JUMP:
                    pc = stack[--sp];
                    break;
                default:
                    throw new Exception(String.format("OzVm RTE: unknown opcode - 0x%08X", cmd));
            }
            if( supervisor != null ){
                supervisor.onEventInterceptor(EVENT_AFTER_EXECUTING, pc, cmd, stack, sp);
            }
            cmd = ram[pc++];
        }
        if( supervisor != null ){
            supervisor.onEventInterceptor(EVENT_BEFORE_EXECUTING, pc, cmd, stack, sp);
            pc = 0;
            sp = 0;
            supervisor.onEventInterceptor(EVENT_AFTER_EXECUTING,  pc, cmd, stack, sp);
        }
    }

    public interface OnOzVmSupervisingListener{
        public void onEventInterceptor(int event, int pc, int cmd, int[] stack, int sp);
    }
}