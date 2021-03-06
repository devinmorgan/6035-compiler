package edu.mit.compilers.ll;

import edu.mit.compilers.AssemblyBuilder;
import edu.mit.compilers.LlSymbolTable;
import edu.mit.compilers.StackFrame;

/**
 * Created by devinmorgan on 11/18/16.
 */
public class LlLocationArray extends LlLocation {
    private final LlComponent elementIndex;

    public LlLocationArray(String varName, LlComponent elementIndex) {
        super(varName);
        this.elementIndex = elementIndex;
    }

    public LlComponent getElementIndex() {
        return this.elementIndex;
    }

    @Override
    public String toString() {
        return this.getVarName() + "[" + elementIndex.toString() + "] ";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LlLocationArray)) {
            return false;
        }
        return ((LlLocationArray)obj).elementIndex.equals(this.elementIndex)
                && ((LlLocationArray)obj).getVarName().equals(this.getVarName());
    }

    @Override
    public int hashCode() {
        return this.elementIndex.hashCode() * this.getVarName().hashCode();
    }

    // the return value of this didnt neccesarily have to be an integer :)
    public String getArrayHead(String arrayLocation){

        return Integer.toString((Integer.parseInt(arrayLocation.substring(0, arrayLocation.length() - 6)))/8);
    }
    public void generateBoundsCheck(AssemblyBuilder builder){

    }
    public String generateCode(AssemblyBuilder builder, StackFrame frame, LlSymbolTable symbolTable){
        // check to see if the name of this array exists in the table.

        builder.addComment("array generating code for " +  this.toString());
        LlLocationVar arrayAlias = new LlLocationVar(this.getVarName());
        String arrayLoc = frame.getLlLocation(arrayAlias);
        String returnLocation;
        String accessLocation = this.elementIndex.generateCode(builder, frame, symbolTable);
        if(symbolTable.isInGlobalArraysTable(new LlLocationVar(this.getVarName()))){
            if(builder.getFootnote().equals("")){

                builder.addLinef("movq", accessLocation+ ", %rax");
                builder.addLinef("movq", this.getVarName()+ "( ,%rax, 8), %rax");
                returnLocation = frame.getNextStackLocation();
                builder.addLinef("movq", "%rax, " + returnLocation);
                frame.pushToRegisterStackFrame("%rax");

            }

            else{

                builder.addLinef("movq", accessLocation+ ", %r10");
                returnLocation = frame.getNextStackLocation();
                frame.pushToRegisterStackFrame("%r10");
                builder.putOnFootnote("");
            }

            return returnLocation;
        }
        // if it doesn't create a contagious memory location of length arrayLength
        if(arrayLoc == null){
            builder.addComment("Populating array with default values");
            String newArrayLoc = frame.getNextStackLocation();
            frame.pushToStackFrame(arrayAlias);
            String arrValLoc = "";
            for(int i = 1; i < symbolTable.getFromArrayTable(new LlLocationVar(this.getVarName())); i++){
                arrValLoc = frame.getNextStackLocation();
                frame.pushToRegisterStackFrame("%r10"); // check
            }
            arrayLoc = frame.getLlLocation(arrayAlias);
            builder.addLine();
        }
        // then generate code for the value stored there.

            String arrayHead = this.getArrayHead(arrayLoc);
            returnLocation = frame.getNextStackLocation();
            // do some bounds check here

            builder.addLinef("movq", "$"+ arrayHead + ", %r10");

            builder.addLinef("movq", accessLocation + ", %r11");
            builder.addLinef("imul", "$-1, %r11");
            builder.addLinef("addq",  "%r11, %r10");

            if(builder.getFootnote().equals("")){

                builder.addLinef("movq",  "(%rbp, %r10, 8), %r10");
                builder.addLinef("movq",  "%r10, "+ returnLocation);
                frame.pushToRegisterStackFrame("%r10");

            }

            else{

                builder.addLinef("movq", "%r10, " + returnLocation);
                frame.pushToRegisterStackFrame("%r10");
                builder.addLine();
                builder.putOnFootnote("");
            }

        // initialize everything with zero

        return returnLocation;
    }
}
