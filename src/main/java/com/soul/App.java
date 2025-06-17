package com.soul;

import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;

/**
 * Simple test without MPXJ to isolate the shared library build issue
 */
public class App {
    
    @CEntryPoint(name = "graal_create_isolate", builtin = CEntryPoint.Builtin.CREATE_ISOLATE)
    public static native IsolateThread createIsolate();

    @CEntryPoint(name = "graal_detach_thread", builtin = CEntryPoint.Builtin.DETACH_THREAD)
    public static native void detachThread(IsolateThread thread);

    @CEntryPoint(name = "hello_test")
    public static int helloTest(IsolateThread thread) {
        System.out.println("Hello from native shared library!");
        return 42;
    }

    @CEntryPoint(name = "process_file_path")
    public static int processFilePath(IsolateThread thread, CCharPointer filePath) {
        try {
            String filePathString = CTypeConversion.toJavaString(filePath);
            System.out.println("Received file path: " + filePathString);
            
            // Simple file existence check without MPXJ
            java.io.File file = new java.io.File(filePathString);
            if (file.exists()) {
                System.out.println("File exists: " + file.getAbsolutePath());
                return 0;
            } else {
                System.out.println("File does not exist: " + file.getAbsolutePath());
                return 1;
            }
        } catch (Exception ex) {
            System.err.println("Error: " + ex.getMessage());
            return -1;
        }
    }
}