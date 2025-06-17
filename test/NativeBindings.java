import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CCharPointer;

public class NativeBindings {

    @CEntryPoint(name = "readProject")
    public static int readProject(IsolateThread thread, CCharPointer path, CCharPointer outBuffer, int bufferSize) {
        byte[] data = generateBytes(path);
        int len = Math.min(bufferSize, data.length);
        for (int i = 0; i < len; i++) {
            outBuffer.write(i, (byte) data[i]);
        }
        return len;
    }

    private static byte[] generateBytes(CCharPointer path) {
        // Your logic here
        return new byte[] { 42, 17, 88 }; // example
    }

    @CEntryPoint(name = "addNumbers")
    static int add(IsolateThread thread, int a, int b) {
        return a + b;
    }
}
