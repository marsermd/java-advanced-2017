package ru.ifmo.ctddev.panin.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by marsermd on 26.02.2017.
 */
public class Implementor implements Impler
{
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException
    {
        InterfaceImplementor implementor = new InterfaceImplementor(token, getImplName(token));

        File implPath = null;
        try
        {
            implPath = createFile(root, token);
        }
        catch (IOException e)
        {
            throw new ImplerException("Failed creating file " + implPath.toString(), e);
        }

        try (BufferedWriter out = new BufferedWriter(new FileWriter(implPath)))
        {
            implementor.build(out);
        }
        catch (IOException e)
        {
            throw new ImplerException("Failed writing to file " + implPath.toString(), e);
        }
        System.out.println("built");
    }

    private File createFile(Path root, Class<?> token) throws IOException
    {
        Path path = getImplPath(root, token);
        path.getParent().toFile().mkdirs();
        path.toFile().createNewFile();
        return path.toFile();
    }

    private Path getImplPath(Path root, Class<?> token)
    {
        Path path = root;

        for (String pckg: token.getPackage().getName().split("\\."))
        {
            path = path.resolve(pckg);
        }
        path = path.resolve(getImplFileName(token));

        return path;
    }

    private String getImplFileName(Class<?> token)
    {
        return getImplName(token) + ".java";
    }

    private String getImplName(Class<?> token)
    {
        return token.getSimpleName() + "Impl";
    }
}
