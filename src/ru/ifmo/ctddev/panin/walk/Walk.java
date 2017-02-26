package ru.ifmo.ctddev.panin.walk;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by marsermd on 19.02.2017.
 */
public class Walk
{
    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            System.err.println("You should specify at 2 arguments");
            return;
        }

        walk(args[0], args[1]);

    }

    private static void walk(String input, String output)
    {
        try (BufferedReader reader = new BufferedReader(new FileReader(input));
             BufferedWriter writer = new BufferedWriter(new FileWriter(output)))
        {
            walk(reader, writer);
        }
        catch (FileNotFoundException e)
        {
            System.err.println("file not found");
        }
        catch (IOException e)
        {
            System.err.println("can't open file");
        }
    }

    private static void walk(BufferedReader input, BufferedWriter output) throws IOException
    {
        while (true)
        {
            String line;
            try
            {
                line = input.readLine();
            }
            catch (IOException e)
            {
                System.err.println("couldn't read from input file");
                return;
            }
            if (line == null)
            {
                return;
            }

            MultipleFileHashCalc fileHashCalc = new MultipleFileHashCalc(line);
            fileHashCalc.printHashes(output);
        }
    }
}
