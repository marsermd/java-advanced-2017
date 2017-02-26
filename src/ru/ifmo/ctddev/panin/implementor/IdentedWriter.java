package ru.ifmo.ctddev.panin.implementor;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Created by marsermd on 26.02.2017.
 */
public class IdentedWriter
{
    private final BufferedWriter writer;
    private int identCnt = 0;
    private boolean newLine = true;

    public IdentedWriter(BufferedWriter writer)
    {
        this.writer = writer;
    }

    public IdentedWriter indent()
    {
        identCnt++;
        return this;
    }

    public IdentedWriter unindent()
    {
        identCnt--;
        return this;
    }

    public IdentedWriter beginBlock() throws IOException
    {
        printLine("{");
        indent();
        return this;
    }

    public IdentedWriter endBlock() throws IOException
    {
        unindent();
        printLine("}");
        return this;
    }

    public IdentedWriter print(String value) throws IOException
    {
        write(value);
        return this;
    }

    public IdentedWriter printInBrackets(String value) throws IOException
    {
        print("(").print(value).print(")");
        return this;
    }

    public IdentedWriter printToken(String token) throws IOException
    {
        write(token + " ");
        return this;
    }

    public IdentedWriter printLine(String line) throws IOException
    {
        write(line);
        newLine();
        return this;
    }

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

    private void newLine() throws IOException
    {
        writer.newLine();
        newLine = true;
    }
}
