package tech.kayys.wayang.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

/**
 * CLI command module for opening the Wayang agentic terminal UI.
 *
 * <p>Launches the full agentic REPL/Panel experience backed by the local
 * Gollek/Wayang inference engine. Switches between REPL and Panel layouts
 * on demand via the {@code /panel} and {@code /repl} slash commands.</p>
 */
final class WayangTuiCommands {

    private WayangTuiCommands() {
    }

    @Command(name = "tui", aliases = {"agent", "code"}, description = "Open the Wayang agentic terminal UI.")
    static final class TuiCommand implements Callable<Integer> {
        @ParentCommand
        WayangGollekCli parent;

        @Override
        public Integer call() {
            WayangCliContext context = parent.context();
            try {
                tech.kayys.wayang.tui.Main.main(new String[0]);
                return 0;
            } catch (Exception e) {
                context.err().println("Unable to open Wayang TUI: " + e.getMessage());
                if (Boolean.getBoolean("wayang.cli.debug")) e.printStackTrace(context.err());
                return 1;
            }
        }
    }
}
