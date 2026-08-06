package tech.kayys.wayang.harness.spi;
import java.util.List;
public interface PromptBundle {
    List<String> getSystemPrompts();
    List<String> getUserPrompts();
}
