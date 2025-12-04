

/**
 * Token counter for LLM context management
 */
@ApplicationScoped
public class TokenCounter {
    
    private final Encoding encoding;
    
    public TokenCounter() {
        // Use cl100k_base encoding (GPT-4, GPT-3.5-turbo)
        this.encoding = Encodings.newDefaultEncodingRegistry().getEncoding(EncodingType.CL100K_BASE);
    }
    
    public int count(Object obj) {
        if (obj == null) return 0;
        
        if (obj instanceof String) {
            return countText((String) obj);
        }
        
        if (obj instanceof Prompt) {
            return countPrompt((Prompt) obj);
        }
        
        // Convert to JSON and count
        var json = JsonUtils.toJson(obj);
        return countText(json);
    }
    
    public int countText(String text) {
        if (text == null || text.isEmpty()) return 0;
        return encoding.countTokens(text);
    }
    
    public int countPrompt(Prompt prompt) {
        int total = 0;
        
        if (prompt.getSystem() != null) {
            total += countText(prompt.getSystem());
        }
        
        if (prompt.getUser() != null) {
            total += countText(prompt.getUser());
        }
        
        if (prompt.getContext() != null) {
            total += count(prompt.getContext());
        }
        
        return total;
    }
    
    public List<Integer> encode(String text) {
        return encoding.encode(text);
    }
    
    public String decode(List<Integer> tokens) {
        return encoding.decode(tokens);
    }
}
