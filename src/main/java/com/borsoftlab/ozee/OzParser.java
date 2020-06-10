package com.borsoftlab.ozee;

import java.util.Locale;

import com.borsoftlab.ozee.OzSymbols.Symbol;

public class OzParser {

    OzScanner scanner;
    int aheadLexeme = 0;
    ByteArray mem = new ByteArray();
    int pc = 0;

    /*
    * Type support stack
    */
    private OzIntStack tsStack = new OzIntStack( 64 );


    public OzParser(){
    }
   
	public void compile(final OzScanner scanner) throws Exception {
        this.scanner = scanner;
        OzCompileError.reset();
        pc = 0;

        emitCommentListing("unconditional jump");
        emit(OzVm.OPCODE_PUSH, 0x0);
        int label = pc - 4;
        scanner.symbolTable.addCodeSegmentRef(label);

        // jump over 4 bytes
        emit(OzVm.OPCODE_JUMP);
        // address for metadata
        emit(OzVm.OPCODE_STOP);
        emit(OzVm.OPCODE_STOP);
        emit(OzVm.OPCODE_STOP);
        emit(OzVm.OPCODE_STOP);
        // store jump address to push command saved in label
        OzUtils.storeIntToByteArray(mem.mem, label, pc);

        scanner.nextLexeme();
        stmtList();
        emit(OzVm.OPCODE_STOP);
    }

    void stmtList() throws Exception {
        while( scanner.lookAheadLexeme != OzScanner.lexEOF ){
            if( tsStack.size() != 0 )
            {
                throw new Exception(String.format("Type stack size is wrong: %d", tsStack.size()));
            }
            stmt();
            match(OzScanner.lexSEMICOLON);
        }
    }

    void stmt() throws Exception {
        // если обнаружено объявление типа
        if( scanner.lookAheadLexeme == OzScanner.lexVARTYPE) {
            int varType = getVarType();
            boolean isArray = checkArrayDeclaration(varType);
            OzSymbols.Symbol symbol = declareNewVariable(varType, isArray);
            assignExpression(symbol);
        } else // если обнаружено имя переменной
        if( scanner.lookAheadLexeme == OzScanner.lexVARNAME) {
            OzSymbols.Symbol symbol = getVariable();

            // проверяем переменную слева на элемент массива
            if( scanner.lookAheadLexeme == OzScanner.lexLSQUARE ) {
                assignExpressionToElementOfArray(symbol);
            } else {
                // просто переменная - не элемент массива
                assignExpression(symbol);
            }
        }
    }

    private void evaluateAddressOfArrayElement(int sizeOfElement) throws Exception {
        emitCommentListing("evaluate the address of the first element of the array");
        emit(OzVm.OPCODE_EVAL);
        emitCommentListing("skip four bytes of size of the array");

        emit(OzVm.OPCODE_PUSH, 4);
        emit(OzVm.OPCODE_ADD);

        match(OzScanner.lexLSQUARE);
        emitCommentListing("evaluate the offset the element inside the array");
        OzLocation loc = new OzLocation(scanner.loc);
        expression();
        int type = tsStack.pop();
        if( type != OzScanner.VAR_TYPE_INT ){
            OzCompileError.expected(scanner, "integer value", loc);
        }
        match(OzScanner.lexRSQUARE);

        emit(OzVm.OPCODE_PUSH, sizeOfElement);
        emit(OzVm.OPCODE_MUL);
        emit(OzVm.OPCODE_ADD);
        emitCommentListing("there is an element address on the stack");
    }

    private void assignExpressionToElementOfArray(OzSymbols.Symbol symbol) throws Exception {
        emit(OzVm.OPCODE_PUSH, symbol);
        scanner.symbolTable.addDataSegmentRef( pc - 4 );

        evaluateAddressOfArrayElement(OzSymbols.sizeOfType(symbol.varType));

        // едим знак равенства
        match(OzScanner.lexASSIGN);
        // теперь на верхушке стека находится адрес элемента массива

        // вычисляем выражение
        expression();

        // теперь на верхушке стека находится значение которое надо положить
        // в элемент массива, а под ним адрес элемента

        // дальше по схеме
        genCodeConvertTypeAssign(tsStack.pop(), symbol.varType);
        // меняем местами адрес и значение
        emit(OzVm.OPCODE_SWAP);
        // теперь адрес сверху адрес как и положено при сохранении в память                    
        genCodeAssign(symbol.varType);
    }

    private void assignExpression(OzSymbols.Symbol symbol) throws Exception {
        if( scanner.lookAheadLexeme == OzScanner.lexASSIGN){
            if( symbol.arraySize != 0 ) {
                OzCompileError.message(scanner, " array '" + symbol.name + "' already defined", scanner.loc);
            }
            match(OzScanner.lexASSIGN, "'='");
            if( symbol.isArray && 
                ( scanner.lookAheadLexeme == OzScanner.lexVARTYPE || scanner.lookAheadLexeme == OzScanner.lexVARNAME )  ){
                assignArrayDefinition(symbol);
            } else {
                if( !symbol.isArray ) {
                    assignArithmeticExpression(symbol);
                } else {
                    match(OzScanner.lexVARTYPE, "array definition");
                }
            }
        } else if( scanner.lookAheadLexeme == OzScanner.lexSEMICOLON ) {
        // empty
        } else if( scanner.lookAheadLexeme == OzScanner.lexEOF ) {
            OzCompileError.message(scanner, "unexpected EOF", scanner.text.loc);
        } else {
            OzCompileError.expected(scanner, "'=' or ';'", scanner.loc);
        }
    }

    private int getVarType() throws Exception {
        match(OzScanner.lexVARTYPE, "var type definition");
        int varType = scanner.varType;
        return varType;
    }

    private boolean checkArrayDeclaration(int varType) throws Exception {
        if( scanner.lookAheadLexeme == OzScanner.lexLSQUARE ){
            match(OzScanner.lexLSQUARE);
            match(OzScanner.lexRSQUARE);
            return true;
        }
        return false;
    }

    private OzSymbols.Symbol declareNewVariable(int varType, boolean isArray) throws Exception {
        boolean isExport = checkNameExportAttribute();
        if( scanner.symbol.lexeme  == OzScanner.lexVARNAME &&
            scanner.symbol.varType != OzScanner.VAR_TYPE_UNDEF ){
            OzCompileError.message(scanner, "name '" + scanner.symbol.name + "' already defined",
            scanner.loc);
        }
        match(OzScanner.lexVARNAME, "variable name");
        scanner.symbol.isExport = isExport;
        scanner.symbol.isArray  = isArray;
        scanner.symbol.allocateVariable(varType);

        // проверяем объявление имени переменной на дальнейшую квадратную скобку
        // доопределим массив
        if( isArray && scanner.lookAheadLexeme == OzScanner.lexLSQUARE ){
            defineArray();
        }
        return scanner.symbol;
    }

    private boolean checkNameExportAttribute() throws Exception {
        if( scanner.lookAheadLexeme == OzScanner.lexMUL){
            match(OzScanner.lexMUL);
            return true;
        }
        return false;
    }

    private OzSymbols.Symbol getVariable() throws Exception {
        if( scanner.symbol.varType == OzScanner.VAR_TYPE_UNDEF ){
            OzCompileError.message(scanner, "name '" + scanner.symbol.name + "' not defined",
            scanner.loc);
        }
        match(OzScanner.lexVARNAME, "variable name");
        return scanner.symbol;
    }

    private void assignArithmeticExpression(OzSymbols.Symbol symbol) throws Exception {
        expression();
        genCodeConvertTypeAssign(tsStack.pop(), symbol.varType);
        emit(OzVm.OPCODE_PUSH, symbol);
        scanner.symbolTable.addDataSegmentRef( pc - 4 );
        genCodeAssign(symbol.varType);
    }

    private void assignArrayDefinition(OzSymbols.Symbol lSymbol) throws Exception {
        OzLocation loc = new OzLocation(scanner.loc);
        if( scanner.lookAheadLexeme == OzScanner.lexVARTYPE ){
            int varType = getVarType();
            if( lSymbol.isArray && lSymbol.varType == varType ){
                if( scanner.lookAheadLexeme == OzScanner.lexLSQUARE ){
                    defineArray();
                }    
            } else {
                OzCompileError.message(scanner, "incompatible array types", loc);
            }
        } else
        if( scanner.lookAheadLexeme == OzScanner.lexVARNAME ) {
            OzSymbols.Symbol rSymbol = getVariable();
            if( ( lSymbol.isArray == rSymbol.isArray ) && ( lSymbol.varType == rSymbol.varType ) ){
                genCodeArrayAssign(lSymbol, rSymbol);
            } else {
                OzCompileError.message(scanner, "incompatible types", loc);
            }
        }
    }

    private void genCodeArrayAssign(OzSymbols.Symbol lSymbol, OzSymbols.Symbol rSymbol) {
        emit( OzVm.OPCODE_PUSH, rSymbol );
        scanner.symbolTable.addDataSegmentRef( pc - 4 );
        emit( OzVm.OPCODE_EVAL );
        emit( OzVm.OPCODE_PUSH, lSymbol  );
        scanner.symbolTable.addDataSegmentRef( pc - 4 );
        emit( OzVm.OPCODE_ASGN );
        lSymbol.refValue = rSymbol.refValue;
    }

    private void defineArray() throws Exception {
        match(OzScanner.lexLSQUARE);
        if( scanner.lookAheadLexeme != OzScanner.lexNUMBER ||
            scanner.varType != OzScanner.VAR_TYPE_INT ) {
                OzCompileError.expected(scanner, "a positive integer number for array size", scanner.loc);
        }
        OzLocation loc = new OzLocation(scanner.loc);
        match(OzScanner.lexNUMBER);
        if( scanner.intNumber <= 0 ) {
            OzCompileError.expected(scanner, "an integer above zero for array size", loc);
        }
        match(OzScanner.lexRSQUARE);
        scanner.symbol.allocateArray(scanner.intNumber);
        
        if( scanner.lookAheadLexeme != OzScanner.lexSEMICOLON) {
            OzCompileError.expected(scanner, "';'", scanner.loc);
        }
    }

    private void genCodeAssign(int varType) throws Exception {
        switch(varType){
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

    public void expression() throws Exception {
        term();
        while(true) {
            switch( scanner.lookAheadLexeme ){
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
        while(true) {
            switch( scanner.lookAheadLexeme ){
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
        if( scanner.lookAheadLexeme == OzScanner.lexMINUS){
            match(OzScanner.lexMINUS, "unexpected lexeme");
            unaryMinus = true;
        };
        if( scanner.lookAheadLexeme == OzScanner.lexLPAREN){
            match(OzScanner.lexLPAREN, "(");
            expression();
            match(OzScanner.lexRPAREN, ")");
        } else {
            switch(scanner.lookAheadLexeme){
                case OzScanner.lexNUMBER:
                    match(OzScanner.lexNUMBER, "number");
                    switch(scanner.varType ){
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
                    OzSymbols.Symbol symbol = getVariable();
                    emit(OzVm.OPCODE_PUSH, symbol);
                    scanner.symbolTable.addDataSegmentRef( pc - 4 );
                    if( symbol.isArray ) {
                        // определяем адрес массива
                        evaluateAddressOfArrayElement(OzSymbols.sizeOfType(symbol.varType));
                        // на стеке адрес элемента массива
                    }        
                    if( symbol.varType == OzScanner.VAR_TYPE_BYTE ||
                        symbol.varType == OzScanner.VAR_TYPE_UBYTE) {
                        emit(OzVm.OPCODE_EVALB);
                        if( symbol.varType == OzScanner.VAR_TYPE_BYTE ){
                            emitCommentListing("a signed bit extension for byte");
                            emit( OzVm.OPCODE_PUSH, 24 );
                            emit( OzVm.OPCODE_LSL );
                            emit( OzVm.OPCODE_PUSH, 24 );
                            emit( OzVm.OPCODE_ASR );
                            emitCommentListing("-");
                        }
                    } else
                    if(
                        symbol.varType == OzScanner.VAR_TYPE_SHORT ||
                        symbol.varType == OzScanner.VAR_TYPE_USHORT) {
                        emit(OzVm.OPCODE_EVALS);
                        if( symbol.varType == OzScanner.VAR_TYPE_SHORT ) {
                            emitCommentListing("a signed bit extension for short");
                            emit( OzVm.OPCODE_PUSH, 16 );
                            emit( OzVm.OPCODE_LSL );
                            emit( OzVm.OPCODE_PUSH, 16 );
                            emit( OzVm.OPCODE_ASR );
                            emitCommentListing("-");
                        }
                    } else {
                        emit(OzVm.OPCODE_EVAL);
                    }
                    // теперь все стало VAR_TYPE_INT и далее, вплоть до присваивания будет
                    // или VAR_TYPE_INT или VAR_TYPE_FLOAT
                    if( symbol.varType == OzScanner.VAR_TYPE_FLOAT) {
                        tsStack.push(OzScanner.VAR_TYPE_FLOAT);
                    } else {
                        tsStack.push(OzScanner.VAR_TYPE_INT);
                    }
                    break;
                case OzScanner.lexEOF:
                break ;   
                default:
                    OzCompileError.expected(scanner, "scalar type", scanner.loc);    
            }
        }
        if( unaryMinus ) {
            int type = tsStack.pop();
            // имеем право проверять только на VAR_TYPE_INT или VAR_TYPE_FLOAT
            if( type == OzScanner.VAR_TYPE_INT) {
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
        int resultType = genCodeConvertTypeBinOp();
        switch ( resultType ){
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
        int resultType = genCodeConvertTypeBinOp();
        switch (resultType){
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
        int resultType = genCodeConvertTypeBinOp();
        switch ( resultType ){
            case OzScanner.VAR_TYPE_INT:
                emit(OzVm.OPCODE_MUL);
                break;
            case OzScanner.VAR_TYPE_FLOAT:    
                emit(OzVm.OPCODE_MULF);
            break;
            default:
        }
        tsStack.push( resultType );
    }

    private void div() throws Exception {
        match(OzScanner.lexDIV, "'/'");
        factor();
        int resultType = genCodeConvertTypeBinOp();
        switch ( resultType ){
            case OzScanner.VAR_TYPE_INT:
                emit(OzVm.OPCODE_DIV);
                break;
            case OzScanner.VAR_TYPE_FLOAT:    
                emit(OzVm.OPCODE_DIVF);
            break;
            default:
        }
        tsStack.push( resultType );
    }

    private void emit(byte opcode){
        emitListing(opcode);
        emitMem(opcode);
    }

    private void emit(byte opcode, final int arg){
        emitListing(opcode, arg);
        emitMem(opcode, arg);
    }

    private void emit(byte opcode, final float arg){
        emitListing(opcode, arg);
        emitMem(opcode, arg);
    }

    private void emit(byte opcode, final Symbol symbol){
        emitListing(opcode, symbol);
        emitMem(opcode, symbol);
    }

    private void emitCommentListing(final String comment){
        System.out.println(String.format("; %s", comment));
    }

    private void emitMnemonicListing(byte opcode){
        String mnemonic = OzAsm.getInstance().getMnemonic(opcode);
        System.out.print(String.format("        %s", mnemonic));
    }

    private void emitHexListing(byte opcode){
        System.out.print(String.format("0x%04X: 0x%02X", pc, opcode));
    }

    private void emitListing(byte opcode) {
        emitMnemonicListing(opcode);
        System.out.println();

        emitHexListing(opcode);
        System.out.println();
    }

    private void emitListing(byte opcode, final int arg) {
        emitMnemonicListing(opcode);
        System.out.print(String.format(" %d", arg));
        System.out.println();

        emitHexListing(opcode);
        System.out.println(String.format(" 0x%08X", arg));
    }

    private void emitListing(byte opcode, final float arg) {
        emitMnemonicListing(opcode);
        System.out.print(String.format(Locale.US, " %f", arg));
        System.out.println();

        int i = Float.floatToIntBits(arg);
        emitHexListing(opcode);
        System.out.println(String.format(" 0x%08X",  i));
    }

    private void emitListing(byte opcode, final Symbol sym) {
        emitMnemonicListing(opcode);
        System.out.print(String.format(" %s", sym.name));
        System.out.println();

        emitHexListing(opcode);
        System.out.println(String.format(" 0x%08X", sym.allocAddress));
    }

    private void emitMem(byte opcode){
        mem.add(opcode);
        pc++;
    }

    private void emitMem(byte opcode, int arg){
        emitMem(opcode);
        OzUtils.addIntToByteArray(mem, arg);
        pc += 4;
    }

    private void emitMem(byte opcode, float arg){
        emitMem(opcode, Float.floatToIntBits(arg));
    }

    private void emitMem(byte opcode, final Symbol sym){
        emitMem(opcode, sym.allocAddress);
    }

    public byte[] getProgramImage(){
        return mem.cut();
    }

    private void match(final int lexeme, final String msg) throws Exception {
        if( scanner.lookAheadLexeme == lexeme ){
            scanner.nextLexeme();
        } else  if( scanner.lookAheadLexeme == OzScanner.lexEOF ) {
            OzCompileError.message(scanner, "unexpected EOF", scanner.text.loc);
        } else {
            OzCompileError.expected(scanner, msg, scanner.loc);
        }
    }     

    private void match(final int lexeme) throws Exception {
        if( scanner.lookAheadLexeme == lexeme ){
            scanner.nextLexeme();
        } else  if( scanner.lookAheadLexeme == OzScanner.lexEOF ) {
            OzCompileError.message(scanner, "unexpected EOF", scanner.text.loc);
        } else {
            OzCompileError.message(scanner, "unexpected lexeme", scanner.loc);
        }
    }     
    
    private void genCodeConvertTypeAssign(int stackTopType, int varType){
        if( stackTopType != varType ){
            if( stackTopType == OzScanner.VAR_TYPE_INT && varType == OzScanner.VAR_TYPE_FLOAT ) {
                emitCommentListing("convert stack operand for assign");
                emit(OzVm.OPCODE_FLT);
                emitCommentListing("-");
            } else
            if (( varType == OzScanner.VAR_TYPE_INT   ||
                  varType == OzScanner.VAR_TYPE_SHORT ||
                  varType == OzScanner.VAR_TYPE_BYTE ) &&
                stackTopType == OzScanner.VAR_TYPE_FLOAT ) {
                emitCommentListing("convert stack operand for assign");
                emit(OzVm.OPCODE_INT);
                emitCommentListing("-");
            } else {
            }
        }
    }

    private int genCodeConvertTypeBinOp(){
        int typeOfTop    = tsStack.pop();
        int typeOfSubTop = tsStack.pop();
        if( typeOfTop != typeOfSubTop ){
            if( typeOfTop == OzScanner.VAR_TYPE_FLOAT ){
                emitCommentListing("convert a sub top stack operand for the binary operation");
                emit(OzVm.OPCODE_SWAP);
                emit(OzVm.OPCODE_FLT);
                emit(OzVm.OPCODE_SWAP);
                emitCommentListing("-");
                return OzScanner.VAR_TYPE_FLOAT;
            } else if ( typeOfSubTop == OzScanner.VAR_TYPE_FLOAT ){
                emitCommentListing("convert a top stack operand for the binary operation");
                emit(OzVm.OPCODE_FLT);
                emitCommentListing("-");
                return OzScanner.VAR_TYPE_FLOAT;
            }
        }
        return typeOfTop;
    }

    public class ByteArray{
        
        final static int CHUNK_SIZE = 64;
        byte[] mem = new byte[CHUNK_SIZE];
        int used = 0;

        void add(byte b){
            if( used == mem.length ){
                byte[] tmp = new byte[mem.length + CHUNK_SIZE];
                System.arraycopy(mem, 0, tmp, 0, mem.length);
                mem = tmp;
            }
            mem[used++] = b;
        }

        byte[] cut(){
            byte[] tmp = new byte[used];
            System.arraycopy(mem, 0, tmp, 0, used);
            return tmp;
        }
    }
}