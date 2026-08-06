package tech.kayys.wayang.tools.impl;



import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeGrepImpl {
    public record Match(Path file, int line, String text) {}

    private final Path baseDir;
    public CodeGrepImpl(Path baseDir) { this.baseDir = baseDir; }

        public List<Match> grep(String regex, List<Path> files) throws IOException {
        Pattern p = Pattern.compile(regex);
        List<Match> out = new ArrayList<>();
        for (Path f : files) {
            if (!Files.isRegularFile(f)) continue;
            var lines = Files.readAllLines(f, StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                Matcher m = p.matcher(lines.get(i));
                if (m.find()) out.add(new Match(f, i + 1, lines.get(i)));
            }
        }
        return out;
    }
}
