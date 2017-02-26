package ru.ifmo.ctddev.panin.walk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * Created by marsermd on 19.02.2017.
 */
public interface FileHashCalc
{
    public void printHashes(BufferedWriter writer) throws IOException;
}
