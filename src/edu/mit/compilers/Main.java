package edu.mit.compilers;

import edu.mit.compilers.cfg.CFG;
import edu.mit.compilers.grammar.DecafParser;
import edu.mit.compilers.grammar.DecafScanner;
import edu.mit.compilers.tools.CLI;
import edu.mit.compilers.ir.*;
import edu.mit.compilers.tools.CLI.Action;
import org.antlr.v4.gui.SystemFontMetrics;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.gui.Trees;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;


class Main {

    public static void testParserCode(String[] args) {
        try {
            String[] flags = {"reg", "cp", "cse", "as", "ure", "par"};
            CLI.parse(args, flags);
            System.out.println();
            InputStream inputStream = args.length == 0 ?
                    System.in : new java.io.FileInputStream(CLI.infile);
            PrintStream outputStream = CLI.outfile == null ? System.out : new java.io.PrintStream(new java.io.FileOutputStream(CLI.outfile));
            if (CLI.target == Action.SCAN) {
                DecafScanner scanner =
                        new DecafScanner(new ANTLRInputStream(inputStream));
                //        scanner.setTrace(CLI.debug);
                Token token;
                boolean done = false;
                while (!done) {
                    try {
                        for (token = scanner.nextToken();
                             token.getType() != DecafParser.EOF;
                             token = scanner.nextToken()) {
                            String type = "";
                            String text = token.getText();

                            switch (token.getType()) {
                                case DecafScanner.CHAR:
                                    type = " CHARLITERAL";
                                    break;
                                case DecafScanner.INT:
                                    type = " INTLITERAL";
                                    break;
                                case DecafScanner.STRING:
                                    type = " STRINGLITERAL";
                                    break;
                                case DecafScanner.BOOL:
                                    type = " BOOLEANLITERAL";
                                    break;
                                case DecafScanner.ID:
                                    type = " IDENTIFIER";
                                    break;
                            }
                            outputStream.println(token.getLine()  + type + " " + text);
                        }
                        done = true;
                    } catch(Exception e) {
                        // print the error:
                        System.err.println(CLI.infile + " " + e);
                        //scanner.skip(); // replaces
                        System.exit(1);
                    }
                }
            } else if (CLI.target == Action.PARSE) {
                DecafScanner scanner =
                        new DecafScanner(new ANTLRInputStream(inputStream));
                CommonTokenStream tokenStream = new CommonTokenStream(scanner); // added for Antlr4
                DecafParser parser = new DecafParser(tokenStream);

                // Semantic/token recognition error capture mechanism
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                PrintStream p = new PrintStream(bytes, true, "UTF-8");
                System.setErr(p);

                parser.setTrace(CLI.debug);
                parser.program();
                String printedSoFar = bytes.toString("UTF-8");
                System.out.print(printedSoFar);

                if (parser.getNumberOfSyntaxErrors() > 0 || printedSoFar.length() > 0) {
                    System.exit(1);
                }

            } else if (CLI.target == Action.INTER) {
                DecafScanner lexer = new DecafScanner(new ANTLRInputStream(inputStream));
                TokenStream tokens = new CommonTokenStream(lexer);
                DecafParser parser = new DecafParser(tokens);
                ParseTree tree = parser.program();
                ParseTreeWalker walker = new ParseTreeWalker();
                DecafListener listener = new DecafListener();
                walker.walk(listener, tree);
//                Trees.inspect(tree, parser); // Makes pretty graph

                if (listener.detectedSemanticErrors()) {
                    System.exit(1);
                }
            } else if (CLI.target == Action.ASSEMBLY || CLI.target == Action.DEFAULT) {

                DecafScanner lexer = new DecafScanner(new ANTLRInputStream(inputStream));
                TokenStream tokens = new CommonTokenStream(lexer);
                DecafParser parser = new DecafParser(tokens);
                ParseTree tree = parser.program();
                ParseTreeWalker walker = new ParseTreeWalker();
                DecafListener listener = new DecafListener();

                walker.walk(listener, tree);

                // get the program fro the listener
                IrProgram program = listener.getGeneratedProgram();
                CodeGenerator cg = new CodeGenerator();
                String generatedCode = cg.generateCode(program, CLI.options);


                if (CLI.outfile != null){
                    try {

                        String content = generatedCode.toString();

                        File file = new File(CLI.outfile);

                        if (!file.exists()) {
                            file.createNewFile();
                        }

                        FileWriter fw = new FileWriter(file.getAbsoluteFile());
                        BufferedWriter bw = new BufferedWriter(fw);
                        bw.write(content);
                        bw.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }
                else{
                    System.out.println(generatedCode.toString());

                }

                if (CLI.debug) {
                     System.out.println(listener.prettyPrintProgram());
                }

//            Trees.inspect(tree, parser); // Makes pretty graph
            }
        } catch(Exception e) {
            // print the error:
            System.err.println(CLI.infile + " " + e);
            System.exit(1);
        }
    }

    // TODO: Function for testing purposes only
    // TODO: Remove in final program
    private static void runFilesInDirectory(String dirPath) {
        File dir = new File(dirPath);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {

                // get the fileExtension
                String filePath = child.getPath();
                String fileExtension = "none";
                int i = filePath.lastIndexOf('.');
                if (i > 0) {
                    fileExtension = filePath.substring(i+1);
                }

                // run the test in the file
                if (fileExtension.equals("dcf")) {
                    try {
                        System.out.println("\n\n" + child.getPath());
                        CharStream stream = new ANTLRFileStream(child.getPath());
                        DecafScanner lexer = new DecafScanner(stream);
                        TokenStream tokens = new CommonTokenStream(lexer);
                        DecafParser parser = new DecafParser(tokens);
                        ParseTree tree = parser.program();
                        ParseTreeWalker walker = new ParseTreeWalker();
                        DecafListener listener = new DecafListener();
                        walker.walk(listener, tree);

                    }
                    catch (IOException e) {
                        System.out.println("There was an error:\n" + e);
                    }
                }
            }
        }
    }

    // TODO: Function for testing purposes only
    // TODO: Remove in final program
    private static void runFile(String filePath) {
        // get the fileExtension
        String fileExtension = "none";
        int i = filePath.lastIndexOf('.');
        if (i > 0) {
            fileExtension = filePath.substring(i+1);
        }

        // run the test in the file
        if (fileExtension.equals("dcf")) {
            try {
                System.out.println("\n\n" + filePath);
                CharStream stream = new ANTLRFileStream(filePath);
                DecafScanner lexer = new DecafScanner(stream);
                TokenStream tokens = new CommonTokenStream(lexer);
                DecafParser parser = new DecafParser(tokens);
                ParseTree tree = parser.program();
                ParseTreeWalker walker = new ParseTreeWalker();
                DecafListener listener = new DecafListener();
                walker.walk(listener, tree);
//            Trees.inspect(tree, parser);
            }
            catch (IOException e) {
                System.out.println("There was an error:\n" + e);
            }
        }
    }

//    public static void testDecafListner() {
//        String prefix = "tests/semantics/";
//
//        try {
//            CharStream stream = new ANTLRFileStream(prefix + "legal/custom-02.dcf");
//            DecafScanner lexer = new DecafScanner(stream);
//            TokenStream tokens = new CommonTokenStream(lexer);
//            DecafParser parser = new DecafParser(tokens);
//            ParseTree tree = parser.program();
//            ParseTreeWalker walker = new ParseTreeWalker();
//            DecafListener listener = new DecafListener();
//            walker.walk(listener, tree);
////            Trees.inspect(tree, parser);
//        }
//        catch (IOException e) {
//            System.out.println("There was an error:\n" + e);
//        }
//    }

    public static void main(String[] args) {
        // Run program based on CLI and shell scripts

        Main.testParserCode(args);

//        // Code generation tests
//        Main.testDecafListner();

//        // Semantic analysis tests
//        String legalTests = "./tests/semantics-hidden/legal/";
//        Main.runFilesInDirectory(legalTests);

//        String illegalTests = "./tests/semantics-hidden/illegal/";
//        Main.runFilesInDirectory(illegalTests);

//        String customTest = "./tests/semantics/legal/custom-01.dcf";
//        Main.runFile(customTest);
    }
}