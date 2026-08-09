package tech.kayys.wayang.tool.nono;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import tech.kayys.wayang.spi.sandbox.Sandbox;
import tech.kayys.wayang.spi.sandbox.SandboxExecutionResult;

public class NonoSandbox implements Sandbox {

    private static final Linker linker = Linker.nativeLinker();
    private static final SymbolLookup stdlib = linker.defaultLookup();
    private static SymbolLookup nonoLib;

    private static MethodHandle nono_sandbox_is_supported;
    private static MethodHandle nono_capability_set_new;
    private static MethodHandle nono_capability_set_free;
    private static MethodHandle nono_capability_set_allow_path;
    private static MethodHandle nono_capability_set_allow_file;
    private static MethodHandle nono_sandbox_apply;
    private static MethodHandle nono_last_error;
    private static MethodHandle nono_string_free;

    static {
        // Load the shared library
        try {
            System.loadLibrary("nono_ffi");
            nonoLib = SymbolLookup.loaderLookup();
            initMethodHandles();
        } catch (UnsatisfiedLinkError e) {
            // Fallback: the library might be in the current directory or target/classes
            try {
                String os = System.getProperty("os.name").toLowerCase();
                String ext = os.contains("mac") ? ".dylib" : (os.contains("win") ? ".dll" : ".so");
                Path libPath = Paths.get("target/classes/libnono_ffi" + ext).toAbsolutePath();
                System.load(libPath.toString());
                nonoLib = SymbolLookup.loaderLookup();
                initMethodHandles();
            } catch (UnsatisfiedLinkError e2) {
                System.err.println("Failed to load nono_ffi native library. Ensure it is built and in java.library.path.");
            }
        }
    }

    private static void initMethodHandles() {
        nono_sandbox_is_supported = lookup("nono_sandbox_is_supported", FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN));
        nono_capability_set_new = lookup("nono_capability_set_new", FunctionDescriptor.of(ValueLayout.ADDRESS));
        nono_capability_set_free = lookup("nono_capability_set_free", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
        
        nono_capability_set_allow_path = lookup("nono_capability_set_allow_path", 
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
        
        nono_capability_set_allow_file = lookup("nono_capability_set_allow_file", 
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
            
        nono_sandbox_apply = lookup("nono_sandbox_apply", 
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
            
        nono_last_error = lookup("nono_last_error", FunctionDescriptor.of(ValueLayout.ADDRESS));
        nono_string_free = lookup("nono_string_free", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
    }

    private static MethodHandle lookup(String symbol, FunctionDescriptor descriptor) {
        Optional<MemorySegment> segment = nonoLib.find(symbol).or(() -> stdlib.find(symbol));
        if (segment.isPresent()) {
            return linker.downcallHandle(segment.get(), descriptor);
        }
        throw new NonoException("Symbol not found: " + symbol);
    }

    private MemorySegment capabilitySet = MemorySegment.NULL;
    private final Arena arena;

    public NonoSandbox() {
        if (nono_capability_set_new == null) {
            throw new NonoException("Nono FFI library is not loaded.");
        }
        this.arena = Arena.ofConfined();
        try {
            this.capabilitySet = (MemorySegment) nono_capability_set_new.invoke();
        } catch (Throwable e) {
            throw new NonoException("Failed to initialize capability set", e);
        }
    }

    public static boolean isSupported() {
        if (nono_sandbox_is_supported == null) return false;
        try {
            return (boolean) nono_sandbox_is_supported.invoke();
        } catch (Throwable e) {
            return false;
        }
    }

    public void allowPath(String path, NonoAccessMode mode) {
        try {
            MemorySegment pathSegment = arena.allocateFrom(path);
            int result = (int) nono_capability_set_allow_path.invoke(capabilitySet, pathSegment, mode.getValue());
            if (result != 0) {
                throw new NonoException("Failed to allow path: " + path + " - " + getLastError());
            }
        } catch (Throwable e) {
            throw new NonoException("Failed to invoke allow_path", e);
        }
    }

    public void allowFile(String path, NonoAccessMode mode) {
        try {
            MemorySegment pathSegment = arena.allocateFrom(path);
            int result = (int) nono_capability_set_allow_file.invoke(capabilitySet, pathSegment, mode.getValue());
            if (result != 0) {
                throw new NonoException("Failed to allow file: " + path + " - " + getLastError());
            }
        } catch (Throwable e) {
            throw new NonoException("Failed to invoke allow_file", e);
        }
    }

    public void apply() {
        try {
            int result = (int) nono_sandbox_apply.invokeExact(capabilitySet);
            if (result != 0) {
                throw new NonoException("Failed to apply sandbox - " + getLastError());
            }
        } catch (Throwable e) {
            throw new NonoException("Failed to invoke apply", e);
        }
    }

    private String getLastError() {
        try {
            MemorySegment errorStr = (MemorySegment) nono_last_error.invoke();
            if (errorStr.equals(MemorySegment.NULL)) {
                return "Unknown error (NULL)";
            }
            String errorMsg = errorStr.reinterpret(Integer.MAX_VALUE).getString(0);
            nono_string_free.invoke(errorStr);
            return errorMsg;
        } catch (Throwable e) {
            return "Failed to retrieve last error: " + e.getMessage();
        }
    }

    @Override
    public void close() {
        if (!capabilitySet.equals(MemorySegment.NULL)) {
            try {
                nono_capability_set_free.invokeExact(capabilitySet);
                capabilitySet = MemorySegment.NULL;
            } catch (Throwable e) {
                // Ignore during cleanup
            }
        }
        arena.close();
    }

    @Override
    public void start() throws Exception {
        apply();
    }

    @Override
    public void stop() throws Exception {
        close();
    }

    @Override
    public SandboxExecutionResult executeCommand(String command, long timeoutMillis) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
        // Nono applies to the current process and its children. 
        // Thus, ProcessBuilder inherits the sandbox restrictions.
        Process p = pb.start();
        boolean finished = p.waitFor(timeoutMillis, TimeUnit.MILLISECONDS);
        if (!finished) {
            p.destroyForcibly();
            return new SandboxExecutionResult(-1, "", "Command timed out");
        }
        String stdout = new String(p.getInputStream().readAllBytes());
        String stderr = new String(p.getErrorStream().readAllBytes());
        return new SandboxExecutionResult(p.exitValue(), stdout, stderr);
    }

    @Override
    public void writeFile(String path, String content) throws Exception {
        Files.writeString(Paths.get(path), content);
    }

    @Override
    public String readFile(String path) throws Exception {
        return Files.readString(Paths.get(path));
    }
}
