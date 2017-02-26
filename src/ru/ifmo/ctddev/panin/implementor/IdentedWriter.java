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
        printLine("}");
        unindent();
        return this;
    }

    public IdentedWriter print(String value) throws IOException
    {
        writer.write(value);
        return this;
    }

    public IdentedWriter printInBrackets(String value) throws IOException
    {
        print("(").print(value).print(")");
        return this;
    }

    public IdentedWriter printToken(String token) throws IOException
    {
        writer.write(token + " ");
        return this;
    }

    public IdentedWriter printLine(String line) throws IOException
    {
        for (int i = 0; i < identCnt; i++)
        {
            writer.write("\t");
        }
        writer.write(line);
        writer.newLine();
        return this;
    }
}
