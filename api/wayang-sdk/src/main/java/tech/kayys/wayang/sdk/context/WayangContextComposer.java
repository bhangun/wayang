package tech.kayys.wayang.sdk.context;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public final class WayangContextComposer {

    public String compose(String basePrompt, Path cwd) {
        StringBuilder sb = new StringBuilder();
        if (basePrompt != null) {
            sb.append(basePrompt).append("\n\n");
        }
        appendProjectContext(sb, cwd);
        appendSkills(sb, cwd);
        return sb.toString().trim();
    }

    private void appendProjectContext(StringBuilder sb, Path cwd) {
        Path wayangProject = cwd.resolve(".wayang/project.md");
        if (Files.exists(wayangProject)) {
            sb.append("## Project Context\n\n");
            try {
                sb.append(Files.readString(wayangProject)).append("\n\n");
                return; // Prefer .wayang/project.md
            } catch (IOException ignored) {}
        }
        
        Path readme = cwd.resolve("README.md");
        if (Files.exists(readme)) {
            sb.append("## Project Context (README.md)\n\n");
            try {
                sb.append(Files.readString(readme)).append("\n\n");
            } catch (IOException ignored) {}
        }
    }

    private void appendSkills(StringBuilder sb, Path cwd) {
        Path globalSkills = Path.of(System.getProperty("user.home"), ".wayang", "skills");
        Path localSkills = cwd.resolve(".wayang/skills");
        
        boolean hasSkills = false;
        
        if (Files.isDirectory(globalSkills)) {
            hasSkills = appendSkillsDir(sb, globalSkills, hasSkills);
        }
        if (Files.isDirectory(localSkills)) {
            appendSkillsDir(sb, localSkills, hasSkills);
        }
    }

    private boolean appendSkillsDir(StringBuilder sb, Path dir, boolean hasSkillsStarted) {
        boolean started = hasSkillsStarted;
        try (Stream<Path> stream = Files.list(dir)) {
            for (Path entry : stream.toList()) {
                Path skillFile = null;
                String skillName = null;

                if (Files.isDirectory(entry)) {
                    // Standard skill folder structure: <skill-name>/SKILL.md
                    Path skillMd = entry.resolve("SKILL.md");
                    Path lowerMd = entry.resolve("skill.md");
                    Path readmeMd = entry.resolve("README.md");

                    if (Files.isRegularFile(skillMd)) {
                        skillFile = skillMd;
                    } else if (Files.isRegularFile(lowerMd)) {
                        skillFile = lowerMd;
                    } else if (Files.isRegularFile(readmeMd)) {
                        skillFile = readmeMd;
                    }
                    skillName = entry.getFileName().toString();
                } else if (entry.toString().endsWith(".md") && !entry.getFileName().toString().equalsIgnoreCase("README.md")) {
                    skillFile = entry;
                    skillName = entry.getFileName().toString().replace(".md", "");
                }

                if (skillFile != null && Files.isRegularFile(skillFile)) {
                    try {
                        String rawContent = Files.readString(skillFile);
                        if (rawContent.isBlank()) continue;

                        if (!started) {
                            sb.append("## Available Skills\n\n");
                            started = true;
                        }

                        // Extract frontmatter if present
                        String parsedName = skillName;
                        String parsedDescription = "";
                        String body = rawContent;

                        if (rawContent.startsWith("---")) {
                            int end = rawContent.indexOf("\n---", 3);
                            if (end > 0) {
                                String frontmatter = rawContent.substring(3, end).strip();
                                body = rawContent.substring(end + 4).stripLeading();
                                for (String line : frontmatter.split("\\r?\\n")) {
                                    if (line.startsWith("name:")) {
                                        parsedName = line.substring(5).strip();
                                    } else if (line.startsWith("description:")) {
                                        parsedDescription = line.substring(12).strip();
                                    }
                                }
                            }
                        }

                        sb.append("### Skill: ").append(parsedName).append("\n");
                        if (!parsedDescription.isBlank()) {
                            sb.append("**Description**: ").append(parsedDescription).append("\n\n");
                        }
                        sb.append(body).append("\n\n");
                    } catch (IOException ignored) {}
                }
            }
        } catch (IOException ignored) {}
        return started;
    }
}
