package ru.ifmo.ctddev.panin.implementor;

import org.junit.runners.Parameterized;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.NoSuchElementException;

/**
 * Created by marsermd on 26.02.2017.
 */
public class InterfaceImplementor
{
    private final Class<?> parent;
    private final String implClassName;

    private IdentedWriter writer;

    public InterfaceImplementor(Class<?> parent, String implClassName)
    {
        this.parent = parent;
        this.implClassName = implClassName;
    }

    public void build(BufferedWriter writer) throws IOException
    {
        this.writer = new IdentedWriter(writer);

        printPackage();
        classStart();
        generateMethods();
        classEnd();
    }

    private void generateMethods() throws IOException
    {
        for (Method method: parent.getMethods())
        {
            beginMethod(method);
            returnDefault(method);
            endMethod();
        }
    }

    private void beginMethod(Method method) throws IOException
    {
        printModifiers(method.getModifiers(), Modifier.fieldModifiers());
        writer.printToken(method.getReturnType().getCanonicalName())
            .print(method.getName())
            .printInBrackets(getMethodParameters(method))
            .printLine(getThrows(method))
            .beginBlock();
    }

    private void returnDefault(Method method) throws IOException
    {
        Class<?> returnType = method.getReturnType();
        writer.printToken("return");
        if (returnType.equals(Void.TYPE))
        {
            // don't want to return anything
        }
        else if (returnType.isPrimitive())
        {
            writer.print(getDefaultForPrimitive(returnType));
        }
        else
        {
            writer.print(getDefaultForNullable(returnType));
        }
        writer.printLine(";");
    }

    private String getDefaultForPrimitive(Class<?> primitive) throws IOException
    {
        if (primitive == boolean.class)
        {
            return "false";
        }
        else
        {
            return "0";
        }
    }

    private String getDefaultForNullable(Class<?> primitive)
    {
        return "null";
    }

    private void endMethod() throws IOException
    {
        writer.endBlock();
    }

    private String getMethodParameters(Method method)
    {
        StringBuilder builder = new StringBuilder();
        int cnt = 0;
        for (Class<?> parameterType: method.getParameterTypes())
        {
            if (cnt != 0)
            {
                builder.append(", ");
            }
            builder.append(parameterType.getCanonicalName());
            builder.append(" ");
            builder.append("arg");
            builder.append(cnt);
            cnt++;
        }
        return builder.toString();
    }

    private String getThrows(Method method)
    {
        StringBuilder builder = new StringBuilder();
        int cnt = 0;
        for (Class<?> exceptionType: method.getExceptionTypes())
        {
            if (cnt == 0)
            {
                builder.append("throws ");
            }
            else
            {
                builder.append(", ");
            }
            builder.append(exceptionType.getCanonicalName());
            cnt++;
        }
        return builder.toString();
    }

    private void printPackage() throws IOException
    {
        writer.printToken("package")
            .print(parent.getPackage().getName())
            .printLine(";");
    }

    private void classStart() throws IOException
    {
        printModifiers(parent.getModifiers(), Modifier.classModifiers());
        writer.printToken("class")
            .printToken(implClassName)
            .printToken("implements")
            .printLine(parent.getCanonicalName())
            .beginBlock();
    }

    private void classEnd() throws IOException
    {
        writer.endBlock();
    }

    private void printModifiers(int modifier, int mask) throws IOException
    {
        modifier &= mask;
        if (Modifier.isAbstract(modifier))
        {
            // we actually implement everything that is abstract
        }

        if (Modifier.isFinal(modifier))
        {
            writer.printToken("final");
        }

        if (Modifier.isPrivate(modifier))
        {
            writer.printToken("private");
        }
        if (Modifier.isProtected(modifier))
        {
            writer.printToken("protected");
        }
        if (Modifier.isPublic(modifier))
        {
            writer.printToken("public");
        }

        if (Modifier.isStatic(modifier))
        {
            writer.printToken("static");
        }
        if (Modifier.isSynchronized(modifier))
        {
            writer.printToken("synchronized");
        }
        if (Modifier.isVolatile(modifier))
        {
            writer.printToken("volatile");
        }
    }
}
