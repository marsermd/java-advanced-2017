package ru.ifmo.ctddev.panin.implementor;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipOutputStream;

/**
 * Utility class to assemble files into a jar
 */
class JarMaker
{
    private List<Path> sourcesToAdd = new ArrayList<>();
    private final File jarFile;

    /**
     * Creates a jar builder
     * @param jarFile target .jar file(will be created or overwritten)
     */
    public JarMaker(Path jarFile)
    {
        this.jarFile = jarFile.toFile();
    }

    /**
     * Add file from source
     * @param source source .java file
     * @return this
     */
    public JarMaker addFile(Path source)
    {
        sourcesToAdd.add(source);
        return this;
    }

    /**
     * Apply changes
     */
    public void build()
    {
        try (JarOutputStream out = new JarOutputStream((new BufferedOutputStream(new FileOutputStream(jarFile)))))
        {
            for (Path source: sourcesToAdd)
            {
                writeFile(source, out);
            }
        }
        catch (FileNotFoundException e)
        {
            System.err.println("File not found " + jarFile);
        }
        catch (IOException e)
        {
            System.err.println("Couldn't write to " + jarFile);
        }

        sourcesToAdd.clear();
    }

    /**
     * Write file to Jar
     * @param originalPath from and to paths.
     * @param out stream to jar
     */
    private void writeFile(Path originalPath, JarOutputStream out)
    {
        String zipCorrectedPath = originalPath.toString().replace("\\", "/");
        if (zipCorrectedPath.startsWith("./"))
        {
            zipCorrectedPath = zipCorrectedPath.substring(2);
        }
        wrieFile(zipCorrectedPath, originalPath.toFile(), out);
    }

    /**
     * Write file to Jar
     * @param path path, fixed to work correctly with JarOutputStream
     * @param source source file to read from
     * @param out stream to jar
     */
    private void wrieFile(String path, File source, JarOutputStream out)
    {
        JarEntry entry = new JarEntry(path);
        entry.setTime(source.lastModified());
        try
        {
            out.putNextEntry(entry);
        }
        catch (IOException e)
        {
            System.err.println("Failed adding entry " + path);
            return;
        }

        try(BufferedInputStream in = new BufferedInputStream(new FileInputStream(source)))
        {
            byte[] buffer = new byte[1024];
            while (true)
            {
                int count = in.read(buffer);
                if (count == -1)
                    break;
                out.write(buffer, 0, count);
            }
            out.closeEntry();
        }
        catch (IOException e)
        {
            System.err.println("Couldn't write " + path.toString() + " to " + jarFile);
        }
    }
}
