
/**
 * Cost calculator for LLM usage
 */
@ApplicationScoped
public class CostCalculator {
    
    private final Map<String, ModelPricing> pricing = new ConcurrentHashMap<>();
    
    @PostConstruct
    void init() {
        // Load pricing data
        pricing.put("gpt-4", new ModelPricing(0.03, 0.06));
        pricing.put("gpt-3.5-turbo", new ModelPricing(0.0015, 0.002));
        pricing.put("claude-3-opus", new ModelPricing(0.015, 0.075));
        pricing.put("claude-3-sonnet", new ModelPricing(0.003, 0.015));
        // Add more models...
    }
    
    public double calculate(String modelId, int tokensIn, int tokensOut) {
        var modelPricing = pricing.getOrDefault(modelId, new ModelPricing(0.001, 0.002));
        
        var inputCost = (tokensIn / 1000.0) * modelPricing.inputPer1k;
        var outputCost = (tokensOut / 1000.0) * modelPricing.outputPer1k;
        
        return inputCost + outputCost;
    }
    
    private static class ModelPricing {
        final double inputPer1k;
        final double outputPer1k;
        
        ModelPricing(double inputPer1k, double outputPer1k) {
            this.inputPer1k = inputPer1k;
            this.outputPer1k = outputPer1k;
        }
    }
}
