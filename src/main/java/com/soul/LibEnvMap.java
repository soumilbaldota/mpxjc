package com.soul;

import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;

import org.mpxj.ProjectFile;
import org.mpxj.json.JsonWriter;
import org.mpxj.reader.UniversalProjectReader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class LibEnvMap {

    private static byte[] resultBuffer = null;

    @CEntryPoint(name = "readProjectAndStore")
    public static int readProjectAndStore(IsolateThread thread, CCharPointer cFilePath) {
        String filePath = CTypeConversion.toJavaString(cFilePath);

        if (filePath == null || filePath.isEmpty()) {
            System.err.println("Invalid file path");
            return -1;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("File does not exist: " + filePath);
            return -1;
        }

        try (InputStream inputStream = new FileInputStream(file)) {
            UniversalProjectReader reader = new UniversalProjectReader();
            ProjectFile pf = reader.read(inputStream);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JsonWriter writer = new JsonWriter();
            writer.write(pf, outputStream);

            resultBuffer = outputStream.toByteArray();
            return resultBuffer.length;
        } catch (Exception e) {
            System.err.println("Error reading project file: " + e.getMessage());
            return -1;
        }
    }

    @CEntryPoint(name = "copyStoredProjectBytes")
    public static int copyStoredProjectBytes(IsolateThread thread, CCharPointer outBuffer, int bufferSize) {
        if (resultBuffer == null) {
            System.err.println("No buffer stored from previous call");
            return -1;
        }

        int len = Math.min(resultBuffer.length, bufferSize);
        for (int i = 0; i < len; i++) {
            outBuffer.write(i, resultBuffer[i]);
        }

        resultBuffer = null; // Free up after copy
        return len;
    }
}
