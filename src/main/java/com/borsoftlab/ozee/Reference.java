package com.borsoftlab.ozee;

public class Reference{

    static final int REFTYPE_DATA  = 0;
    static final int REFTYPE_LABEL = 1;


    public int refType;
    public int refValue;

    public Reference(int type, int  value){
        refType = type;
        refValue = value;
    }
}

