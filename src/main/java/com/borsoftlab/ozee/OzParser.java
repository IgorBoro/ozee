package com.borsoftlab.ozee;

import java.util.Locale;

import com.borsoftlab.ozee.OzSymbols.Symbol;

public class OzParser {

    OzScanner scanner;
    int aheadLexeme = 0;
    ByteArrayBuffer outputBuffer = new ByteArrayBuffer();

    /*
    * Type support stack
    */
    private final OzIntStack tsStack = new OzIntStack(64);

    public OzParser() {
    }

    public void compile(final OzScanner scanner) throws Exception {
        this.scanner = scanner;
        OzCompileError.reset();
        outputBuffer.clean();
        prologCode();
        scanner.nextLexeme();
        stmtList();
        epilogCode();
    }

    private void prologCode() {
        emitCommentListing("unconditional jump");
        emit(OzVm.OPCODE_PUSH, 0x0);
        final int label = outputBuffer.used - 4;
        scanner.symbolTable.addCodeSegmentRef(label);
        // jump over 4 bytes
        emit(OzVm.OPCODE_JUMP);
        // store 4 zero bytes to memory
        emit(OzVm.OPCODE_STOP);
        emit(OzVm.OPCODE_STOP);
        emit(OzVm.OPCODE_STOP);
        emit(OzVm.OPCODE_STOP);
        // store jump address to push command saved in label
        outputBuffer.store(label, outputBuffer.used);
    }

    private void epilogCode() {
        emit(OzVm.OPCODE_STOP);
    }

    void stmtList() throws Exception {
        while (scanner.lookAheadLexeme != OzScanner.lexEOF) {
            if (tsStack.size() != 0) {
                throw new Exception(String.format("Type stack size is wrong: %d", tsStack.size()));
            }
            stmt();
            match(OzScanner.lexSEMICOLON);
        }
    }

    void stmt() throws Exception {
        if (scanner.lookAheadLexeme == OzScanner.lexVARTYPE) {
            int varType = varType();
            boolean isArray = declareArray();
            OzSymbols.Symbol symbol = newIdent(varType, isArray);
            if (scanner.lookAheadLexeme == OzScanner.lexASSIGN) {
                storeIdentReference(symbol);
                match(OzScanner.lexASSIGN);
                boolean isRef = symbol.isArray;
                expression(symbol, isRef);
            }
        } else
        if (scanner.lookAheadLexeme == OzScanner.lexVARNAME) {
            OzSymbols.Symbol symbol = ident();
            storeIdentReference(symbol);
            boolean isSelector = selector( symbol );
            match(OzScanner.lexASSIGN);
            expression(symbol, symbol.isArray && !isSelector);
        }
    }

    private void storeIdentReference(OzSymbols.Symbol symbol) {
        emit(OzVm.OPCODE_PUSH, symbol);
        scanner.symbolTable.addDataSegmentRef(outputBuffer.used - 4);
    }

    private void expression(final OzSymbols.Symbol symbol, final boolean isRef) throws Exception {
        if( isRef ){
            tsStack.push(OzScanner.VAR_TYPE_REF);
            if (scanner.lookAheadLexeme == OzScanner.lexVARTYPE) {
                arrayDefinitionExpression( symbol );
            } else
            if (scanner.lookAheadLexeme == OzScanner.lexVARNAME) {
                arrayReferenceExpression ( symbol );
                assignValue(OzScanner.VAR_TYPE_REF);
            } else {
                OzCompileError.expected(scanner, "array definition", scanner.loc);
            }
        } else {
            arithmeticExpression();
            assignValue(symbol.varType);
        }        
    }

    private boolean selector(Symbol symbol) throws Exception {
        if( symbol.isArray && scanner.lookAheadLexeme == OzScanner.lexLSQUARE ) {
            match(OzScanner.lexLSQUARE);
            evaluateSelector(symbol);
            match(OzScanner.lexRSQUARE);
            return true;
        }
        return false;
    }

    private boolean declareArray() throws Exception {
        if (scanner.lookAheadLexeme == OzScanner.lexLSQUARE) {
            match(OzScanner.lexLSQUARE);
            match(OzScanner.lexRSQUARE);
            return true;
        }
        return false;
    }

    private int varType() throws Exception {
        match(OzScanner.lexVARTYPE, "var type definition");
        final int varType = scanner.varType;
        return varType;
    }

    private OzSymbols.Symbol ident() throws Exception {
        OzSymbols.Symbol symbol = scanner.symbol;
        if (symbol.varType == OzScanner.VAR_TYPE_UNDEF) {
            OzCompileError.message(scanner, "name '" + symbol.name + "' not defined", scanner.loc);
        }
        match(OzScanner.lexVARNAME, "variable name");
        return symbol;
    }

    private void evaluateSelector(final OzSymbols.Symbol symbol) throws Exception {
        emit(OzVm.OPCODE_PUSH, OzSymbols.sizeOfType(symbol.varType));
        emitCommentListing("evaluate the offset the element inside the array");
        final OzLocation loc = new OzLocation(scanner.loc);
        arithmeticExpression(); // put on the stack the index of the element
        final int type = tsStack.pop();
        if (type != OzScanner.VAR_TYPE_INT) {
            OzCompileError.expected(scanner, "integer value", loc);
        }
        emit(OzVm.OPCODE_EVALA);
    }

    private void assignValue(int targetType) throws Exception {
        genCodeConvertTypeAssign(tsStack.pop(), targetType);
        emit(OzVm.OPCODE_SWAP);
        genAssignCode(targetType);
    }

    private OzSymbols.Symbol newIdent(final int varType, boolean isArray) throws Exception {
        final boolean isExport = checkNameExportAttribute();
        OzSymbols.Symbol symbol = scanner.symbol;
        if (symbol.lexeme == OzScanner.lexVARNAME && symbol.varType != OzScanner.VAR_TYPE_UNDEF) {
            OzCompileError.message(scanner, "name '" + symbol.name + "' already defined", scanner.loc);
        }
        match(OzScanner.lexVARNAME, "variable name");
        symbol.isExport = isExport;
        symbol.isArray = isArray;
        symbol.allocateVariable(varType);

        // проверяем объявление имени переменной на дальнейшую квадратную скобку
        // доопределим массив
        if (isArray && scanner.lookAheadLexeme == OzScanner.lexLSQUARE) {
            match(OzScanner.lexLSQUARE);
            int size = evaluateArraySize();
            match(OzScanner.lexRSQUARE);
            symbol.allocateArray( size );
        }
        return symbol;
    }

    private boolean checkNameExportAttribute() throws Exception {
        if (scanner.lookAheadLexeme == OzScanner.lexMUL) {
            match(OzScanner.lexMUL);
            return true;
        }
        return false;
    }

    private void arrayReferenceExpression(final OzSymbols.Symbol lSymbol) throws Exception {
        final OzLocation loc = new OzLocation(scanner.loc);
        final OzSymbols.Symbol rSymbol = ident();
        if ((lSymbol.isArray == rSymbol.isArray) && (lSymbol.varType == rSymbol.varType)) {
            if( rSymbol.arraySize == 0 ) {
                OzCompileError.message(scanner, "array '" + rSymbol.name + "' undefined", loc);
            }
            // evaluate address of array
            storeIdentReference(rSymbol);
            emit(OzVm.OPCODE_EVAL);
        //    lSymbol.refValue = rSymbol.refValue;
        } else {
            OzCompileError.message(scanner, "incompatible types", loc);
        }
    }

    private void arrayDefinitionExpression(final OzSymbols.Symbol lSymbol) throws Exception {
        final OzLocation loc = new OzLocation(scanner.loc);
        final int varType = varType();
        if (lSymbol.isArray && lSymbol.varType == varType) {
            if (scanner.lookAheadLexeme == OzScanner.lexLSQUARE) {
                match(OzScanner.lexLSQUARE);
                int size = evaluateArraySize();
                match(OzScanner.lexRSQUARE);
                lSymbol.allocateArray( size );
            }
        } else {
            OzCompileError.message(scanner, "incompatible array types", loc);
        }
    }

    private int evaluateArraySize() throws Exception {
        if (scanner.lookAheadLexeme != OzScanner.lexNUMBER || scanner.varType != OzScanner.VAR_TYPE_INT) {
            OzCompileError.expected(scanner, "a positive integer number for array size", scanner.loc);
        }
        final OzLocation loc = new OzLocation(scanner.loc);
        match(OzScanner.lexNUMBER);
        if (scanner.intNumber <= 0) {
            OzCompileError.expected(scanner, "an integer above zero for array size", loc);
        }
        return scanner.intNumber;
    }

    private void genAssignCode(final int varType) throws Exception {
        switch (varType) {
            case OzScanner.VAR_TYPE_REF:
            case OzScanner.VAR_TYPE_INT:
            case OzScanner.VAR_TYPE_FLOAT:
                emit(OzVm.OPCODE_ASGN);
                break;
            case OzScanner.VAR_TYPE_BYTE:
            case OzScanner.VAR_TYPE_UBYTE:
                emit(OzVm.OPCODE_ASGNB);
                break;
            case OzScanner.VAR_TYPE_SHORT:
            case OzScanner.VAR_TYPE_USHORT:
                emit(OzVm.OPCODE_ASGNS);
                break;
            default:
                OzCompileError.message(scanner, "Compilation error: assignment type error", scanner.loc);
        }
    }

    public void arithmeticExpression() throws Exception {
        term();
        while (true) {
            switch (scanner.lookAheadLexeme) {
                case OzScanner.lexPLUS:
                    add();
                    break;
                case OzScanner.lexMINUS:
                    sub();
                    break;
                default:
                    return;
            }
        }
    }

    private void term() throws Exception {
        factor();
        while (true) {
            switch (scanner.lookAheadLexeme) {
                case OzScanner.lexMUL:
                    mul();
                    break;
                case OzScanner.lexDIV:
                    div();
                    break;
                default:
                    return;
            }
        }
    }

    private void factor() throws Exception {
        boolean unaryMinus = false;
        if (scanner.lookAheadLexeme == OzScanner.lexMINUS) {
            match(OzScanner.lexMINUS, "unexpected lexeme");
            unaryMinus = true;
        };
        if (scanner.lookAheadLexeme == OzScanner.lexLPAREN) {
            match(OzScanner.lexLPAREN, "(");
            arithmeticExpression();
            match(OzScanner.lexRPAREN, ")");
        } else {
            switch (scanner.lookAheadLexeme) {
                case OzScanner.lexNUMBER:
                    match(OzScanner.lexNUMBER, "number");
                    switch (scanner.varType) {
                        case OzScanner.VAR_TYPE_INT:
                            emit(OzVm.OPCODE_PUSH, scanner.intNumber);
                            tsStack.push(OzScanner.VAR_TYPE_INT);
                            break;
                        case OzScanner.VAR_TYPE_FLOAT:
                            emit(OzVm.OPCODE_PUSH, scanner.floatNumber);
                            tsStack.push(OzScanner.VAR_TYPE_FLOAT);
                            break;
                        default:
                            break;
                    }
                    break;
                case OzScanner.lexVARNAME:
                    final OzSymbols.Symbol symbol = ident();
                    storeIdentReference(symbol);
                    selector( symbol );
                    // на стеке адрес переменной или элемента массива
                    if (symbol.varType == OzScanner.VAR_TYPE_BYTE || symbol.varType == OzScanner.VAR_TYPE_UBYTE) {
                        emit(OzVm.OPCODE_EVALB);
                        if (symbol.varType == OzScanner.VAR_TYPE_BYTE) {
                            emitCommentListing("a signed bit extension for byte");
                            emit(OzVm.OPCODE_PUSH, 24);
                            emit(OzVm.OPCODE_LSL);
                            emit(OzVm.OPCODE_PUSH, 24);
                            emit(OzVm.OPCODE_ASR);
                            emitCommentListing("-");
                        }
                    } else if (symbol.varType == OzScanner.VAR_TYPE_SHORT
                            || symbol.varType == OzScanner.VAR_TYPE_USHORT) {
                        emit(OzVm.OPCODE_EVALS);
                        if (symbol.varType == OzScanner.VAR_TYPE_SHORT) {
                            emitCommentListing("a signed bit extension for short");
                            emit(OzVm.OPCODE_PUSH, 16);
                            emit(OzVm.OPCODE_LSL);
                            emit(OzVm.OPCODE_PUSH, 16);
                            emit(OzVm.OPCODE_ASR);
                            emitCommentListing("-");
                        }
                    } else {
                        emit(OzVm.OPCODE_EVAL);
                    }
                    // теперь все стало VAR_TYPE_INT и далее, вплоть до присваивания будет
                    // или VAR_TYPE_INT или VAR_TYPE_FLOAT
                    if (symbol.varType == OzScanner.VAR_TYPE_FLOAT) {
                        tsStack.push(OzScanner.VAR_TYPE_FLOAT);
                    } else {
                        tsStack.push(OzScanner.VAR_TYPE_INT);
                    }
                    break;
                case OzScanner.lexEOF:
                    break;
                default:
                    OzCompileError.expected(scanner, "scalar type", scanner.loc);
            }
        }
        if (unaryMinus) {
            final int type = tsStack.pop();
            // имеем право проверять только на VAR_TYPE_INT или VAR_TYPE_FLOAT
            if (type == OzScanner.VAR_TYPE_INT) {
                emit(OzVm.OPCODE_NEG);
            } else {
                emit(OzVm.OPCODE_NEGF);
            }
            tsStack.push(type);
        }
    }

    private void add() throws Exception {
        match(OzScanner.lexPLUS, "'+'");
        term();
        final int resultType = genCodeConvertTypeBinOp();
        switch (resultType) {
            case OzScanner.VAR_TYPE_INT:
                emit(OzVm.OPCODE_ADD);
                break;
            case OzScanner.VAR_TYPE_FLOAT:
                emit(OzVm.OPCODE_ADDF);
                break;
            default:
        }
        tsStack.push(resultType);
    }

    private void sub() throws Exception {
        match(OzScanner.lexMINUS, "'-'");
        term();
        final int resultType = genCodeConvertTypeBinOp();
        switch (resultType) {
            case OzScanner.VAR_TYPE_INT:
                emit(OzVm.OPCODE_SUB);
                break;
            case OzScanner.VAR_TYPE_FLOAT:
                emit(OzVm.OPCODE_SUBF);
                break;
            default:
        }
        tsStack.push(resultType);
    }

    private void mul() throws Exception {
        match(OzScanner.lexMUL, "'*'");
        factor();
        final int resultType = genCodeConvertTypeBinOp();
        switch (resultType) {
            case OzScanner.VAR_TYPE_INT:
                emit(OzVm.OPCODE_MUL);
                break;
            case OzScanner.VAR_TYPE_FLOAT:
                emit(OzVm.OPCODE_MULF);
                break;
            default:
        }
        tsStack.push(resultType);
    }

    private void div() throws Exception {
        match(OzScanner.lexDIV, "'/'");
        factor();
        final int resultType = genCodeConvertTypeBinOp();
        switch (resultType) {
            case OzScanner.VAR_TYPE_INT:
                emit(OzVm.OPCODE_DIV);
                break;
            case OzScanner.VAR_TYPE_FLOAT:
                emit(OzVm.OPCODE_DIVF);
                break;
            default:
        }
        tsStack.push(resultType);
    }

    private void emit(final byte opcode) {
        emitListing(opcode);
        emitMem(opcode);
    }

    private void emit(final byte opcode, final int arg) {
        emitListing(opcode, arg);
        emitMem(opcode, arg);
    }

    private void emit(final byte opcode, final float arg) {
        emitListing(opcode, arg);
        emitMem(opcode, arg);
    }

    private void emit(final byte opcode, final Symbol symbol) {
        emitListing(opcode, symbol);
        emitMem(opcode, symbol);
    }

    private void emitCommentListing(final String comment) {
        System.out.println(String.format("; %s", comment));
    }

    private void emitMnemonicListing(final byte opcode) {
        final String mnemonic = OzAsm.getInstance().getMnemonic(opcode);
        System.out.print(String.format("        %s", mnemonic));
    }

    private void emitHexListing(final byte opcode) {
        System.out.print(String.format("0x%04X: 0x%02X", outputBuffer.used, opcode));
    }

    private void emitListing(final byte opcode) {
        emitMnemonicListing(opcode);
        System.out.println();

        emitHexListing(opcode);
        System.out.println();
    }

    private void emitListing(final byte opcode, final int arg) {
        emitMnemonicListing(opcode);
        System.out.print(String.format(" %d", arg));
        System.out.println();

        emitHexListing(opcode);
        System.out.println(String.format(" 0x%08X", arg));
    }

    private void emitListing(final byte opcode, final float arg) {
        emitMnemonicListing(opcode);
        System.out.print(String.format(Locale.US, " %f", arg));
        System.out.println();

        final int i = Float.floatToIntBits(arg);
        emitHexListing(opcode);
        System.out.println(String.format(" 0x%08X", i));
    }

    private void emitListing(final byte opcode, final Symbol sym) {
        emitMnemonicListing(opcode);
        System.out.print(String.format(" %s", sym.name));
        System.out.println();

        emitHexListing(opcode);
        System.out.println(String.format(" 0x%08X", sym.allocAddress));
    }

    private void emitMem(final byte opcode) {
        outputBuffer.add(opcode);
    }

    private void emitMem(final byte opcode, final int arg) {
        emitMem(opcode);
        outputBuffer.add(arg);
    }

    private void emitMem(final byte opcode, final float arg) {
        emitMem(opcode, Float.floatToIntBits(arg));
    }

    private void emitMem(final byte opcode, final Symbol sym) {
        emitMem(opcode, sym.allocAddress);
    }

    public byte[] getProgramImage() {
        return outputBuffer.cut();
    }

    private void match(final int lexeme, final String msg) throws Exception {
        if (scanner.lookAheadLexeme == lexeme) {
            scanner.nextLexeme();
        } else if (scanner.lookAheadLexeme == OzScanner.lexEOF) {
            OzCompileError.message(scanner, "unexpected EOF", scanner.text.loc);
        } else {
            OzCompileError.expected(scanner, msg, scanner.loc);
        }
    }

    private void match(final int lexeme) throws Exception {
        if (scanner.lookAheadLexeme == lexeme) {
            scanner.nextLexeme();
        } else if (scanner.lookAheadLexeme == OzScanner.lexEOF) {
            OzCompileError.message(scanner, "unexpected EOF", scanner.text.loc);
        } else {
            OzCompileError.message(scanner, "unexpected lexeme", scanner.loc);
        }
    }

    private void genCodeConvertTypeAssign(final int stackTopType, final int varType) {
        if (stackTopType != varType) {
            if (stackTopType == OzScanner.VAR_TYPE_INT && varType == OzScanner.VAR_TYPE_FLOAT) {
                emitCommentListing("convert stack operand for assign");
                emit(OzVm.OPCODE_FLT);
                emitCommentListing("-");
            } else if ((varType == OzScanner.VAR_TYPE_INT || varType == OzScanner.VAR_TYPE_SHORT
                    || varType == OzScanner.VAR_TYPE_BYTE) && stackTopType == OzScanner.VAR_TYPE_FLOAT) {
                emitCommentListing("convert stack operand for assign");
                emit(OzVm.OPCODE_INT);
                emitCommentListing("-");
            } else {
            }
        }
    }

    private int genCodeConvertTypeBinOp() {
        final int typeOfTop = tsStack.pop();
        final int typeOfSubTop = tsStack.pop();
        if (typeOfTop != typeOfSubTop) {
            if (typeOfTop == OzScanner.VAR_TYPE_FLOAT) {
                emitCommentListing("convert a sub top stack operand for the binary operation");
                emit(OzVm.OPCODE_SWAP);
                emit(OzVm.OPCODE_FLT);
                emit(OzVm.OPCODE_SWAP);
                emitCommentListing("-");
                return OzScanner.VAR_TYPE_FLOAT;
            } else if (typeOfSubTop == OzScanner.VAR_TYPE_FLOAT) {
                emitCommentListing("convert a top stack operand for the binary operation");
                emit(OzVm.OPCODE_FLT);
                emitCommentListing("-");
                return OzScanner.VAR_TYPE_FLOAT;
            }
        }
        return typeOfTop;
    }

    public class ByteArrayBuffer {

        final static int CHUNK_SIZE = 64;
        byte[] buffer = new byte[CHUNK_SIZE];
        int used = 0;

        ByteArrayBuffer() {
            clean();
        }

        void clean() {
            buffer = new byte[CHUNK_SIZE];
            used = 0;
        }

        void add(final byte b) {
            if (used > buffer.length - 1) {
                expandBuffer();
            }
            buffer[used++] = b;
        }

        void add(final int i) {
            add((byte) (i & 0x000000FF));
            add((byte) ((i & 0x0000FF00) >> 8));
            add((byte) ((i & 0x00FF0000) >> 16));
            add((byte) ((i & 0xFF000000) >> 24));
        }

        void store(int address, int value){
            if( (address + 4) >  buffer.length ){
                expandBuffer();
            }
            OzUtils.storeIntToByteArray(buffer, address, value);
        }

        private void expandBuffer() {
            final byte[] tmp = new byte[buffer.length + CHUNK_SIZE];
            System.arraycopy(buffer, 0, tmp, 0, buffer.length);
            buffer = tmp;
        }


        final byte[] cut() {
            final byte[] tmp = new byte[used];
            System.arraycopy(buffer, 0, tmp, 0, used);
            return tmp;
        }
    }
}