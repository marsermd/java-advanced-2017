package ru.ifmo.ctddev.panin.walk;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Formatter;

/**
 * Created by marsermd on 19.02.2017.
 */
public class SingleFileHashCalc implements FileHashCalc
{
    private String pathURI;
    private Path path;

    public SingleFileHashCalc(Path path)
    {
        this.path = path;
        this.pathURI = path.toString();
    }
    public SingleFileHashCalc(String pathURI)
    {
        this.pathURI = pathURI;
        try
        {
            path = Paths.get(pathURI);
        }
        catch (InvalidPathException e)
        {
            System.err.println("invalid path" + pathURI);
            path = null;
        }
    }

    private long computeAndGetHash()
    {
        if (path == null)
        {
            return 0;
        }
        try(BufferedInputStream reader = new BufferedInputStream(new FileInputStream(path.toFile())))
        {
            int prime = 0x01000193;
            int hash = 0x811c9dc5;

            int lengthRead;
            byte[] buffer = new byte[1024];

            while ((lengthRead = reader.read(buffer)) >= 0) // 0 = eof
            {
                for (int i = 0; i < lengthRead; i++)
                {
                    hash *= prime;
                    hash ^= buffer[i] & 0xFF;
                }
            }

            return hash;
        }
        catch (FileNotFoundException e)
        {
            System.err.println("not existing path:" + path.toString());
            return 0;
        }
        catch (IOException e)
        {
            System.err.println("path could not be read" + path);
            return 0;
        }
    }

    @Override
    public void printHashes(BufferedWriter writer) throws IOException
    {
        Formatter formatter = new Formatter();
        String hexHash = formatter.format("%08x", 0x00000000FFFFFFFFL & computeAndGetHash()).toString();
        writer.write(hexHash + " " + pathURI);
        writer.newLine();
    }
}
