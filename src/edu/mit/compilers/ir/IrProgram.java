package edu.mit.compilers.ir;

import edu.mit.compilers.AssemblyBuilder;
import edu.mit.compilers.Register;
import edu.mit.compilers.ScopeStack;
import edu.mit.compilers.StackFrame;

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
        for (int i = this.fieldDecls.size()-1; i >=0 ; i--) {
            IrFieldDecl fieldDecl = this.fieldDecls.get(i);
            if (scopeStack.checkIfSymbolExistsInGlobalScope(fieldDecl.getName())) {
                errorMessage += "Duplicate field_decl in same scope __filename__"+
                        " line: "+fieldDecl.getLineNumber() + " col: " +fieldDecl.getColNumber() + "\n";
            }
            // check that each fieldDecl is correct
            errorMessage += fieldDecl.semanticCheck(scopeStack);

            // add the fieldDecl to the stack
            scopeStack.addObjectToCurrentScope(fieldDecl.getName(), fieldDecl);
        }

        // 2) check that no externs are declared twice
        for (int j = this.externDecls.size()-1; j >=0 ; j--) {
            IrExternDecl externDecl = this.externDecls.get(j);
            if (scopeStack.checkIfSymbolExistsInGlobalScope(externDecl.getName())) {
                errorMessage += "Duplicate externs declared __filename__"+
                        " line: "+externDecl.getLineNumber() + " col: " + externDecl.getColNumber() + "\n";
            }
            // check that each externDecl is correct
            errorMessage += externDecl.semanticCheck(scopeStack);

            // add the extern to the stack
            scopeStack.addObjectToCurrentScope(externDecl.getName(), externDecl);
        }

        // 3) check that no methods are declared twice
        for (int k = this.methodDecls.size()-1; k >=0 ; k--) {
            IrMethodDecl methodDecl = this.methodDecls.get(k);
            if (scopeStack.checkIfSymbolExistsInGlobalScope(methodDecl.getName())) {
                errorMessage += "Duplicate method declared in __filename__" +
                        " line: "+methodDecl.getLineNumber() + " col: " + methodDecl.getColNumber() + "\n";
            }
            // add the method to the stack
            scopeStack.addObjectToCurrentScope(methodDecl.getName(), methodDecl);

            // check that each methodDecl is correct
            errorMessage += methodDecl.semanticCheck(scopeStack);
        }

        // 4) check for main() method
        if (scopeStack.checkIfSymbolExistsInGlobalScope("main")) {
            Ir object = scopeStack.getSymbol("main");

            if (object instanceof IrMethodDecl) {
                IrMethodDecl mainMethod = (IrMethodDecl) object;

                // 5) make sure main() has no parameters
                if (mainMethod.getParamsList().size() > 0) {
                    errorMessage += "main() method cannot have parameters" +
                            " line: "+ mainMethod.getLineNumber() + " col: " + mainMethod.getColNumber() + "\n";
                }

                // 6) make sure main() is IrTypeVoid
                if (!(mainMethod.getType() instanceof IrTypeVoid)) {
                    errorMessage += "main() must be of type void" +
                            " line: "+ mainMethod.getLineNumber() + " col: " + mainMethod.getColNumber() + "\n";
                }
            }
            else {
                errorMessage += "There must be a void main() method with no parameters ";
            }
        }
        else {
            errorMessage += "Program must contain a main() method";
        }

        return errorMessage;
    }
    public AssemblyBuilder generateCode(AssemblyBuilder assembly, Register register, StackFrame stackFrame){

        return assembly;
    }
}
