package tech.kayys.wayang.cli;

import java.io.PrintStream;
import java.util.function.Supplier;

final public class WayangCliRender {

    private WayangCliRender() {
    }

    public static void jsonOrText(
            PrintStream out,
            boolean json,
            Supplier<String> jsonOutput,
            Supplier<String> textOutput) {
        if (json) {
            out.println(jsonOutput.get());
        } else {
            out.print(textOutput.get());
        }
    }
}
