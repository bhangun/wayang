package tech.kayys.wayang.context;

import tech.kayys.wayang.context.api.model.CompiledContext;
import tech.kayys.wayang.context.api.model.ContextPlan;
import tech.kayys.wayang.context.api.model.TaskIntent;
import tech.kayys.wayang.context.impl.DefaultBudgetedContextCompiler;
import tech.kayys.wayang.context.impl.DefaultContextCompiler;
import tech.kayys.wayang.context.impl.DefaultContextPlanner;
import tech.kayys.wayang.context.impl.DefaultRelevanceScorer;
import tech.kayys.wayang.context.impl.DefaultTokenEstimator;
import tech.kayys.wayang.context.impl.HeuristicSymbolResolver;
import tech.kayys.wayang.context.impl.JavaParserSkeletonizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/**
 * Runnable demo, in two parts:
 *   1. the original hop-based compile (mirrors the Python demo.py)
 *   2. a planner-driven, budget-aware compile sized for a small open-weight
 *      model's context window, showing tiers spill from full skeleton down
 *      to pruned skeleton down to signature digest as the budget tightens --
 *      never down to a summary or paraphrase.
 *
 * Run with:
 *   mvn -pl context-compiler-core exec:java
 */
public final class Demo {

    public static void main(String[] args) throws IOException {
        Path repo = Files.createTempDirectory("context-compiler-demo");
        try {
            buildSyntheticRepo(repo);
            Path target = repo.resolve("app/UserController.java");

            System.out.println("==== 1. hop-based compile (maxHops = 2) ====\n");
            var hopBasedCompiler = new DefaultContextCompiler(
                    new HeuristicSymbolResolver(),
                    new JavaParserSkeletonizer(),
                    new DefaultTokenEstimator()
            );
            CompiledContext hopBased = hopBasedCompiler.compile(repo, target, 2);
            System.out.println(hopBased.summary());

            System.out.println("\n==== 2. planner-driven, budget-aware compile ====\n");
            var planner = new DefaultContextPlanner();
            // A small open-weight model's context window, e.g. an 8K local model.
            ContextPlan plan = planner.plan(TaskIntent.BUG_FIX, 8_000);
            System.out.println("Plan: maxHops=" + plan.maxHops()
                    + ", tokenBudget=" + plan.tokenBudget()
                    + " (" + plan.rationale() + ")");

            var budgetedCompiler = new DefaultBudgetedContextCompiler(
                    new HeuristicSymbolResolver(),
                    new JavaParserSkeletonizer(),
                    new DefaultTokenEstimator(),
                    new DefaultRelevanceScorer()
            );
            // Deliberately tight budget here (not the plan's full figure) so
            // the demo actually shows tier spillage -- pruned skeleton and
            // signature digest both appearing -- rather than everything
            // comfortably fitting as a full skeleton.
            CompiledContext budgeted = budgetedCompiler.compile(repo, target, plan.maxHops(), 260);
            System.out.println(budgeted.summary());
            System.out.println();
            System.out.println("---- compiled prompt ----");
            System.out.println(budgeted.toPromptString());
        } finally {
            deleteRecursively(repo);
        }
    }

    private static void buildSyntheticRepo(Path root) throws IOException {
        Path app = root.resolve("app");
        Files.createDirectories(app);

        write(app.resolve("UserController.java"), """
                package app;

                public class UserController {
                    private final UserService userService = new UserService();

                    public void handleSave() {
                        userService.save();
                    }
                }
                """);

        write(app.resolve("UserService.java"), """
                package app;

                /** Application-level user operations. */
                public class UserService {
                    private final UserRepository userRepository = new UserRepository();

                    /** Persists the current user. */
                    public void save() {
                        userRepository.save();
                    }
                }
                """);

        write(app.resolve("UserRepository.java"), """
                package app;

                public class UserRepository {
                    public void save() {
                        System.out.println("saving user");
                    }
                }
                """);

        // Second, unrelated save() -- trips the name-collision blind spot,
        // the same role models.py / models_order.py play in the Python demo.
        write(app.resolve("OrderRepository.java"), """
                package app;

                public class OrderRepository {
                    public void save() {
                        System.out.println("saving order");
                    }
                }
                """);

        // Reflection-based dispatch -- invisible to static analysis.
        write(app.resolve("PluginDispatcher.java"), """
                package app;

                public class PluginDispatcher {
                    public void dispatch(String className) throws Exception {
                        Class<?> clazz = Class.forName(className);
                        clazz.getDeclaredMethod("run").invoke(clazz.getDeclaredConstructor().newInstance());
                    }
                }
                """);

        // Event-style wiring with no direct call site -- same role as @receiver.
        // ConsumeEvent here is a stand-in annotation; the resolver matches on the
        // annotation's simple name, not on an actual Vert.x/CDI dependency.
        write(app.resolve("OrderEventHandler.java"), """
                package app;

                public class OrderEventHandler {
                    @ConsumeEvent
                    public void onOrderPlaced(String orderId) {
                        System.out.println("order placed: " + orderId);
                    }
                }
                """);

        write(app.resolve("ConsumeEvent.java"), """
                package app;

                import java.lang.annotation.ElementType;
                import java.lang.annotation.Retention;
                import java.lang.annotation.RetentionPolicy;
                import java.lang.annotation.Target;

                @Retention(RetentionPolicy.RUNTIME)
                @Target(ElementType.METHOD)
                public @interface ConsumeEvent {
                }
                """);

        // Entirely unrelated file -- should end up excluded (Tier 3).
        write(app.resolve("UnrelatedUtil.java"), """
                package app;

                public class UnrelatedUtil {
                    public static int square(int n) {
                        return n * n;
                    }
                }
                """);
    }

    private static void write(Path file, String content) throws IOException {
        Files.writeString(file, content);
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) return;
        try (var walk = Files.walk(root)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.delete(p);
                } catch (IOException ignored) {
                    // best-effort cleanup of a temp dir
                }
            });
        }
    }
}
