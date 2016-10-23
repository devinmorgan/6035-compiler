package edu.mit.compilers.ir;

import edu.mit.compilers.ScopeStack;

/**
 * Created by devinmorgan on 10/5/16.
 */
public class IrStmtReturnExpr extends IrStmtReturn{
    private final IrExpr result;

    public IrStmtReturnExpr(IrExpr result) {
        super(result.getLineNumber(),result.getColNumber());
        this.result = result;
    }

    @Override
    public IrType getExpressionType() {
        return this.result.getExpressionType();
    }

    @Override
    public String semanticCheck(ScopeStack scopeStack) {
        String errorMessage = "";

        // 1) check to make sure that the IrStmtReturn exists within a method
        IrType methodType = scopeStack.getScopeReturnType();
        if (methodType != null) {

            // 2) check if method signature and return type match
            if (!methodType.getClass().equals(this.getExpressionType().getClass())) {
                errorMessage += "Return type does not match method return type"+
                        " line: "+this.getLineNumber() + " col: " +this.getColNumber() + "\n";
            }
        }
        else {
            // we are not in a method so we should not have an IrStmtReturnExpr
            errorMessage += "Return statements can only have a value in non-void methods."+
                    " line: "+this.getLineNumber() + " col: " +this.getColNumber() + "\n";
        }

        return errorMessage;
    }
}
