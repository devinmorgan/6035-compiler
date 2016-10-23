package edu.mit.compilers.ir;

import edu.mit.compilers.ScopeStack;

public abstract class Ir {
    // keep track of the line & column number for
    // for error reporting purposes
    private final int lineNumber;
    private final int colNumber;

    public Ir(int lineNumber, int colNumber) {
        this.lineNumber = lineNumber;
        this.colNumber = colNumber;
    }

    public int getLineNumber() {
        return this.lineNumber;
    }

    public int getColNumber() {
        return this.colNumber;
    }

    public abstract String semanticCheck(ScopeStack scopeStack);
}