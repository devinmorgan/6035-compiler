package edu.mit.compilers.ir;

import edu.mit.compilers.ScopeStack;

import java.util.ArrayList;

public class IrProgram extends Ir{

    private ArrayList<IrFieldDecl> fieldDecls;
    private ArrayList<IrMethodDecl> methodDecls;
    private ArrayList<IrExternDecl> externDecls;

    public void addFieldDecl(IrFieldDecl field) { this.fieldDecls.add(field); }
    public void addMethodDecl(IrMethodDecl method) {
        this.methodDecls.add(method);
    }
    public void addExternDecl(IrExternDecl extern) {
        this.externDecls.add(extern);
    }

    public IrProgram(int lineNumber, int colNumber) {
        super(lineNumber, colNumber);
        this.fieldDecls = new ArrayList<IrFieldDecl>();
        this.methodDecls = new ArrayList<IrMethodDecl>();
        this.externDecls = new ArrayList<IrExternDecl>();
    }

    @Override
    public String semanticCheck(ScopeStack scopeStack) {
        // initialize the global scope
        scopeStack.createNewBlockScope();

        String errorMessage = "";

        // 1) check that no field_decls are declared twice in same scope
        for (IrFieldDecl fieldDecl: this.fieldDecls) {
            if (scopeStack.checkIfSymbolExistsAtCurrentScope(fieldDecl.getName())) {
                errorMessage += "Duplicate field_decl in same scope __filename__"+
                        " line: "+fieldDecl.getLineNumber() + " col: " +fieldDecl.getColNumber() + "\n";
            }
            scopeStack.addObjectToCurrentScope(fieldDecl.getName(), fieldDecl);

            // check that each fieldDecl is correct
            errorMessage += fieldDecl.semanticCheck(scopeStack);
        }

        // 2) check that no externs are declared twice
        for (IrExternDecl externDecl: this.externDecls) {
            if (scopeStack.checkIfSymbolExistsAtCurrentScope(externDecl.getName())) {
                errorMessage += "Duplicate externs declared __filename__"+
                        " line: "+externDecl.getLineNumber() + " col: " + externDecl.getColNumber() + "\n";
            }
            scopeStack.addObjectToCurrentScope(externDecl.getName(), externDecl);

            // check that each externDecl is correct
            errorMessage += externDecl.semanticCheck(scopeStack);
        }

        // 3) check that no methods are declared twice
        for (IrMethodDecl methodDecl: this.methodDecls) {
            if (scopeStack.checkIfSymbolExistsAtCurrentScope(methodDecl.getName())) {
                errorMessage += "Duplicate method declared in __filename__"+
                        " line: "+methodDecl.getLineNumber() + " col: " + methodDecl.getColNumber() + "\n";
            }
            scopeStack.addObjectToCurrentScope(methodDecl.getName(), methodDecl);

            // check that each methodDecl is correct
            errorMessage += methodDecl.semanticCheck(scopeStack);
        }

        // 4) check for main() method
        if (scopeStack.checkIfSymbolExistsAtCurrentScope("main")) {
            Ir object = scopeStack.getSymbol("main");

            if (object instanceof IrMethodDecl) {
                IrMethodDecl mainMethod = (IrMethodDecl) object;

                // 5) make sure main() has no parameters
                if (mainMethod.getParamsList().size() > 0) {
                    errorMessage += "main() method cannot have parameters" +
                            "line: " + mainMethod.getLineNumber() + "col: " + mainMethod.getColNumber();
                }
            }
            else {
                errorMessage += "main must be void method with no parameters " +
                        "line: " + this.getLineNumber() + "col: " + this.getColNumber();
            }
        }
        else {
            errorMessage += "Program must contain a main() method" +
                    "line: " + this.getLineNumber() + "col: " + this.getColNumber();
        }

        return errorMessage;
    }
}
