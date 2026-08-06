package tech.kayys.wayang.cli;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import tech.kayys.wayang.cli.WayangGollekCli;
import tech.kayys.wayang.cli.WayangTuiCommands;
import tech.kayys.wayang.gollek.sdk.WayangGollekSdk;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class WayangTuiCommandsTest {

    @Test
    void tuiCommandIsRegisteredFromDedicatedModule() {
        CommandLine root = commandLine();

        assertThat((Object) root.getSubcommands().get("tui").getCommand())
                .isInstanceOf(WayangTuiCommands.TuiCommand.class);
    }

    private static CommandLine commandLine() {
        return new CommandLine(new WayangGollekCli(
                WayangGollekSdk.local(),
                stream(new ByteArrayOutputStream()),
                stream(new ByteArrayOutputStream())));
    }

    private static PrintStream stream(ByteArrayOutputStream output) {
        return new PrintStream(output, true, StandardCharsets.UTF_8);
    }
}
