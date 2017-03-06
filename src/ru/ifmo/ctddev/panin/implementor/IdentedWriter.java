package ru.ifmo.ctddev.panin.implementor;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * This wrapper around BufferedReader allows to easily write indented output, as programmers always do.
 */
class IdentedWriter
{
    private final BufferedWriter writer;
    private int identCnt = 0;
    private boolean newLine = true;

    /**
     * Create IdentedWriter
     * @param writer writer to be used
     */
    public IdentedWriter(BufferedWriter writer)
    {
        this.writer = writer;
    }

    /**
     * Add identation
     * @return this
     */
    public IdentedWriter indent()
    {
        identCnt++;
        return this;
    }

    /**
     * Remove identation
     * @return this
     */
    public IdentedWriter unindent()
    {
        identCnt--;
        return this;
    }

    /**
     * Add opening brace and indent
     * @return this
     * @throws IOException if failed printing
     */
    public IdentedWriter beginBlock() throws IOException
    {
        printLine("{");
        indent();
        return this;
    }

    /**
     * Add closing brace and unindent
     * @return this
     * @throws IOException if failed printing
     */
    public IdentedWriter endBlock() throws IOException
    {
        unindent();
        printLine("}");
        return this;
    }

    /**
     * Print value
     * @param value string ro be printed
     * @return this
     * @throws IOException if failed printing
     */
    public IdentedWriter print(String value) throws IOException
    {
        write(value);
        return this;
    }

    /**
     * Print value in brackets "(value)"
     * @param value string ro be printed in parenthesis
     * @return this
     * @throws IOException if failed printing
     */
    public IdentedWriter printInParenthesis(String value) throws IOException
    {
        print("(").print(value).print(")");
        return this;
    }

    /**
     * Print value separated from next values
     * @param token token to be printed
     * @return this
     * @throws IOException if failed printing
     */
    public IdentedWriter printToken(String token) throws IOException
    {
        write(token + " ");
        return this;
    }

    /**
     * Print string and newline character
     * @param line line to be printed
     * @return this
     * @throws IOException if failed printing
     */
    public IdentedWriter printLine(String line) throws IOException
    {
        write(line);
        newLine();
        return this;
    }

    /**
     * Apply identation if needed and writes string
     * @param value string to be printed
     * @throws IOException if failed printing
     */
    private void write(String value) throws IOException
    {
        if (newLine)
        {
            for (int i = 0; i < identCnt; i++)
            {
                writer.write("    ");
            }
        }
        writer.write(value);
        newLine = false;
    }

    /**
     * Print new line
     * @throws IOException if failed printing
     */
    private void newLine() throws IOException
    {
        writer.newLine();
        newLine = true;
    }
}
