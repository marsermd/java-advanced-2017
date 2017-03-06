package ru.ifmo.ctddev.panin.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Generates implementation and can generate a jar file.
 */
public class Implementor implements Impler, JarImpler
{
    /**
     * Generates implementation and can generate a jar file.
     * To generate java file:
     * java -jar Implementor.jar class-name
     * To generate jar:
     * java -jar Implementor.jar -jar class-name jar-name
     * @param args arguments as described earlier
     */
    public static void main(String args[])
    {
        try
        {
            switch (args.length)
            {
                case 1:
                    tryImplementClass(args[0]);
                    break;
                case 3:
                    tryImplementClassToJar(args[1], args[2]);
                    break;
                default:
                    printUsage();
            }
        }
        catch (ClassNotFoundException e)
        {
            System.err.println("Can't implement: Class not found");
        }
        catch (ImplerException e)
        {
            System.err.println("Failed implementing:");
            if (e.getMessage() != null)
            {
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * Print correct usage to stderr
     */
    private static void printUsage()
    {
        System.err.println("Use as");
        System.err.println("java -jar Implementor.jar class-name");
        System.err.println("Or");
        System.err.println("java -jar Implementor.jar -jar class-name jar-name");
    }

    /**
     * Implements className and generates jar at jarName
     * @param className class to be implemented
     * @param jarName target path
     * @throws ClassNotFoundException if given class is not present
     * @throws ImplerException if given className can't be implemented or can't be written to target path.
     */
    private static void tryImplementClassToJar(String className, String jarName) throws ClassNotFoundException, ImplerException
    {
        Class<?> token = Class.forName(className);
        Implementor implementor = new Implementor();
        implementor.implementJar(token, Paths.get(".", jarName));
    }

    /**
     * Implements className and generates java file
     * @param className class to be implemented
     * @throws ClassNotFoundException if given class is not present
     * @throws ImplerException if given className can't be implemented or can't be written.
     */
    private static void tryImplementClass(String className) throws ClassNotFoundException, ImplerException
    {
        Class<?> token = Class.forName(className);
        Implementor implementor = new Implementor();
        implementor.implement(token, Paths.get("."));
    }

    /**
     * Implements className and generates jar at jarName
     * @param token class to be implemented
     * @param jarPath target path
     * @throws ImplerException if given className can't be implemented or can't be written to target path.
     */
    @Override
    public void implementJar(Class<?> token, Path jarPath) throws ImplerException
    {
        implement(token, Paths.get("."));
        Path javaPath = getImplPath(Paths.get("."), token);
        Path classPath = Compiler.compile(javaPath);

        new JarMaker(jarPath)
            .addFile(classPath)
            .build();
    }

    /**
     * Implements className and generates java file at root
     * @param token class to be implemented
     * @param root target path
     * @throws ImplerException if given className can't be implemented or can't be written to target path.
     */
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

    /**
     * Creates file for java implementation of token
     * @param root target path
     * @param token class to be implemented
     * @return created file
     * @throws IOException if file can't be created
     */
    private File createFile(Path root, Class<?> token) throws IOException
    {
        Path path = getImplPath(root, token);
        path.getParent().toFile().mkdirs();
        path.toFile().createNewFile();
        return path.toFile();
    }

    /**
     * Get path where implementation should be placed based on it's root and package
     * Result is path made from root, then token package then java file. For example: "root\some\package\someTokenImpl.java
     * @param root target path
     * @param token class to be implemented
     * @return path that looks like that
     */
    private static Path getImplPath(Path root, Class<?> token)
    {
        Path path = root;

        for (String pckg: token.getPackage().getName().split("\\."))
        {
            path = path.resolve(pckg);
        }
        path = path.resolve(getImplFileName(token));

        return path;
    }

    /**
     * Get file name for token (class with name ABACABA results with ABACABAImpl.java)
     * @param token class to be implemented
     * @return implementation file name
     */
    private static String getImplFileName(Class<?> token)
    {
        return getImplName(token) + ".java";
    }

    /**
     * Get simple name for token (class with name ABACABA results with ABACABAImpl)
     * @param token class to be implemented
     * @return implementation class name
     */
    private static String getImplName(Class<?> token)
    {
        return token.getSimpleName() + "Impl";
    }
}
