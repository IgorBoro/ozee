package com.borsoftlab.ozee;

import java.util.HashMap;
import java.util.Map;

public class OzAsm {

    private Map<Integer, OzAsmCmd> map = new HashMap<>();
    private static OzAsm  instance = null;


    private OzAsm(){
        installOpcodes();
    }

    public static OzAsm getInstance(){
        if( instance == null ){
            instance = new OzAsm();
        }
        return instance;
    }

    private void installOpcodes(){
        install(OzVm.OPCODE_STOP, "STOP");

        install(OzVm.OPCODE_INC,  "INC");
        install(OzVm.OPCODE_DEC,  "DEC");
        install(OzVm.OPCODE_ADD,  "ADD");
        install(OzVm.OPCODE_ADDF, "ADDF");
        install(OzVm.OPCODE_SUB,  "SUB");
        install(OzVm.OPCODE_SUBF, "SUBF");
        install(OzVm.OPCODE_MUL,  "MUL");
        install(OzVm.OPCODE_MULF, "MULF");
        install(OzVm.OPCODE_DIV,  "DIV");
        install(OzVm.OPCODE_DIVF, "DIVF");
        install(OzVm.OPCODE_MOD,  "MOD");
        install(OzVm.OPCODE_NEG,  "NEG");
        install(OzVm.OPCODE_NEGF, "NEGF");
        install(OzVm.OPCODE_CMP,  "CMP");
        install(OzVm.OPCODE_INT,  "INT");
        install(OzVm.OPCODE_FLT,  "FLT");

        install(OzVm.OPCODE_LSL,  "LSL");
        install(OzVm.OPCODE_LSR,  "LSR");
        install(OzVm.OPCODE_ASL,  "ASL");
        install(OzVm.OPCODE_ASR,  "ASR");

        install(OzVm.OPCODE_PUSH,  "PUSH");
        install(OzVm.OPCODE_EVAL,  "EVAL");
        install(OzVm.OPCODE_EVALB, "EVALB");
        install(OzVm.OPCODE_EVALS, "EVALS");
        install(OzVm.OPCODE_ASGN,  "ASGN");
        install(OzVm.OPCODE_ASGNB, "ASGNB");
        install(OzVm.OPCODE_ASGNS, "ASGNS");

        install(OzVm.OPCODE_PUSHFP, "PUSHFP");
        install(OzVm.OPCODE_POPFP,  "POPFP");
        install(OzVm.OPCODE_PUSHPC, "PUSHPC");
        install(OzVm.OPCODE_POPPC,  "POPPC");

        install(OzVm.OPCODE_DUP,  "DUP");
        install(OzVm.OPCODE_DROP, "DROP");
        install(OzVm.OPCODE_SWAP, "SWAP");
        install(OzVm.OPCODE_OVER, "OVER");

        install(OzVm.OPCODE_JUMP, "JUMP");
        install(OzVm.OPCODE_IFEQ, "IFEQ");
        install(OzVm.OPCODE_IFNE, "IFNE");
        install(OzVm.OPCODE_IFLE, "IFLE");
        install(OzVm.OPCODE_IFGE, "IFGE");
        install(OzVm.OPCODE_IFGT, "IFGT");
        install(OzVm.OPCODE_CALL, "CALL");
        install(OzVm.OPCODE_RET,  "RET");

    }

    void install(final int opcode, final String mnemonic){
        OzAsmCmd asmCmd = new OzAsmCmd(opcode, mnemonic);
        map.put(opcode, asmCmd);
    }

    final String getMnemonic(final int opcode){
        OzAsmCmd cmd = map.get(opcode);
        if( cmd == null )
            return null;
        return cmd.mnemonic;    
    }

    public static class OzAsmCmd {
        int    opcode;
        String mnemonic;
    
        public OzAsmCmd(final int opcode, final String mnemonic) {
            this.opcode = opcode;
            this.mnemonic = mnemonic;
        }
    }

}