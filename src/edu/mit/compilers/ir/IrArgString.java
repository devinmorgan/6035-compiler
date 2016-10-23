package edu.mit.compilers.ir;

import edu.mit.compilers.ScopeStack;

/**
 * Created by devinmorgan on 10/16/16.
 */
public class IrArgString extends IrArg {
    public IrArgString(String string, int lineNum, int colNum) {
        super(string, lineNum, colNum);
    }

    @Override
    public String semanticCheck(ScopeStack scopeStack) {
        return "";
    }
}