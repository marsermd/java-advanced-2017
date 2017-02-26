package ru.ifmo.ctddev.panin.walk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Created by marsermd on 20.02.2017.
 */
public class MultipleFileHashCalc implements FileHashCalc
{
    private String pathStr;

    public MultipleFileHashCalc(String path)
    {
        this.pathStr = path;
    }

    @Override
    public void printHashes(BufferedWriter writer) throws IOException
    {
        Path path;
        try
        {
            path = Paths.get(pathStr);
        }
        catch (InvalidPathException e)
        {
            System.err.println("invalid path" + pathStr);
            path = null;
        }
        if (path == null || !Files.exists(path) || !Files.isDirectory(path))
        {
            SingleFileHashCalc fileHashCalc = new SingleFileHashCalc(pathStr);
            fileHashCalc.printHashes(writer);
            return;
        }

        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path singleFile, BasicFileAttributes attrs) throws IOException
            {
                SingleFileHashCalc fileHashCalc = new SingleFileHashCalc(singleFile.toString());
                fileHashCalc.printHashes(writer);

                return FileVisitResult.CONTINUE;
            }
        });
    }
}
