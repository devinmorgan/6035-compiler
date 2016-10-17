package edu.mit.compilers.ir;

/**
 * Created by devinmorgan on 10/5/16.
 */
public abstract class IrCtrlFlow extends IrStatement {
    final IrExpr condExpr;
    final IrCodeBlock stmtBody;

    public IrCtrlFlow(IrExpr condExpr, IrCodeBlock stmtBody) {
        super(condExpr.getLineNumber(), condExpr.getColNumber());
        this.condExpr = condExpr;
        this.stmtBody = stmtBody;
    }

}
