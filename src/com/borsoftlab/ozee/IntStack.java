package com.borsoftlab.ozee;

public class IntStack {

    public static final int NO_ERROR       = 0;
    public static final int STACK_IS_FULL  = 1;
    public static final int STACK_IS_EMPTY = 2;
    public static final int INDEX_OUT_OF_RANGE = 3;

    private final int[] array;
    private final int capacity;
    private int stackPtr;

    private int error;

    public IntStack(final int capacity){
        this.capacity = capacity;
        this.array = new int [this.capacity];
        reset();
    }

    public void reset() {
        this.stackPtr = -1;
        this.error = NO_ERROR;
    }

    private void setError(int error){
        this.error = error;
    }

    public int getError(){
        return error;
    }

    public boolean isEmpty(){
        return stackPtr == -1;
    }

    public boolean isFull(){
        return stackPtr == capacity-1;
    }

    public int capacity(){
        return capacity;
    }

    public int size(){
        return stackPtr+1;
    }

    public int topIndex(){
        return stackPtr;
    }

    public IntStack push(final int data){
        if( isFull() ){
            setError(STACK_IS_FULL);
            return this;
        }
        array[++stackPtr] = data;
        return this;
    }

    public int peek() {
        if( isEmpty() ){
            setError(STACK_IS_EMPTY);
            return 0;
        }
        return array[stackPtr];
    }

    public int pop(){
        if( isEmpty() ){
            setError(STACK_IS_EMPTY);
            return 0;
        }
        return array[stackPtr--];
    }

    public int getAt(final int i){
        if( checkIndex(i) )
            return array[i];
        return 0;
    }

    public void setAt(final int i, final int data){
        if( checkIndex(i) )
            array[i] = data;
    }

    public int getAtTopOffset(final int offset){
        int i = stackPtr-offset;
        if( checkIndex(i) )
            return array[i];
        return 0;
    }

    public void setAtTopOffset(final int offset, final int data){
        int i = stackPtr-offset;
        if( checkIndex(i) )
            array[i] = data;
    }

    boolean checkIndex(final int i){
        if( i< 0 || i >= capacity ){
            setError(INDEX_OUT_OF_RANGE);
            return false;
        }
        return true;
    }    
}