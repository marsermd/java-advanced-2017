package ru.ifmo.ctddev.panin.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Class that generates interface implementation to a .java file
 */
public class InterfaceImplementor
{
    private final Class<?> parent;
    private final String implClassName;

    private IdentedWriter writer;

    /**
     * Checkes if parent can be implemented
     * @param parent class to be implemented
     * @throws ImplerException if it can't be implemented
     */
    private static void ValidateToken(Class<?> parent) throws ImplerException
    {
        if (!parent.isInterface())
        {
            throw new ImplerException("Can't implement" + parent);
        }
    }

    /**
     * Create interface implementor
     * @param parent interface to implement
     * @param implClassName implementation class name
     * @throws ImplerException if parent can't be implemented
     */
    public InterfaceImplementor(Class<?> parent, String implClassName) throws ImplerException
    {
        ValidateToken(parent);
        this.parent = parent;
        this.implClassName = implClassName;
    }

    /**
     * Fill file with implementation
     * @param writer writer to implementation file
     * @throws IOException if writing fails
     */
    public void build(BufferedWriter writer) throws IOException
    {
        this.writer = new IdentedWriter(writer);

        printPackage();
        classStart();
        generateMethods();
        classEnd();
    }

    /**
     * Generate implementation for methods
     * @throws IOException if failes printing
     */
    private void generateMethods() throws IOException
    {
        for (Method method: parent.getMethods())
        {
            beginMethod(method);
            returnDefault(method);
            endMethod();
        }
    }

    /**
     * Print method decaration part
     * @param method method info to print
     * @throws IOException if failes printing
     */
    private void beginMethod(Method method) throws IOException
    {
        printModifiers(method.getModifiers(), Modifier.fieldModifiers());
        writer.printToken(method.getReturnType().getCanonicalName())
            .print(method.getName())
            .printInParenthesis(getMethodParameters(method))
            .printLine(getThrows(method))
            .beginBlock();
    }

    /**
     * Print method body(returns some default value for returning type)
     * @param method method to print body for
     * @throws IOException if failes printing
     */
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

    /**
     * Get default value for primitive types
     * @param primitive type
     * @return default return value code(0 or false)
     */
    private String getDefaultForPrimitive(Class<?> primitive)
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

    /**
     * Get default value for normal types
     * @param primitive type
     * @return default return value code (null)
     */
    private String getDefaultForNullable(Class<?> primitive)
    {
        return "null";
    }

    /**
     * End method body
     * @throws IOException if failed printings
     */
    private void endMethod() throws IOException
    {
        writer.endBlock();
    }

    /**
     * Parse method info to string
     * @param method method to get parameters from
     * @return parameters as they would be printed in method declaration
     */
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

    /**
     * Get throws clause of declaration method
     * @param method method to get throws clause from
     * @return throws clause as it would be printed in method declaration
     */
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

    /**
     * Print package line
     * Example: package ru.ifmo.ctddev.panin.implementor;
     * @throws IOException if failes printing
     */
    private void printPackage() throws IOException
    {
        writer.printToken("package")
            .print(parent.getPackage().getName())
            .printLine(";");
    }

    /**
     * Print class declaration
     * @throws IOException if failes printing
     */
    private void classStart() throws IOException
    {
        printModifiers(parent.getModifiers(), Modifier.classModifiers());
        writer.printToken("class")
            .printToken(implClassName)
            .printToken("implements")
            .printLine(parent.getCanonicalName())
            .beginBlock();
    }

    /**
     * Print class end
     * @throws IOException if failes printing
     */
    private void classEnd() throws IOException
    {
        writer.endBlock();
    }

    /**
     * Print modifiers from modifier with applied mask
     * @param modifier {@link java.lang.reflect.Modifier}
     * @param mask bit mask applied to modifier
     * @throws IOException if failes printing
     */
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
