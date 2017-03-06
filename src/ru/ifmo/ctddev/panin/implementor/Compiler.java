package ru.ifmo.ctddev.panin.implementor;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class to compile java files
 */
class Compiler
{
    /**
     * compiles given source file
     * @param source java source
     * @return path to compiled file
     */
    public static Path compile(Path source)
    {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, null, source.toString());

        String sourcePath = source.toString();
        int pos = sourcePath.lastIndexOf('.');
        String resultPath = sourcePath.substring(0, pos) + ".class";
        return Paths.get(resultPath);
    }
}
