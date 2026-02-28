package tech.kayys.wayang.vector.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import tech.kayys.gamelan.engine.context.ExecutionContext;
import tech.kayys.gamelan.engine.plugin.ExecutorPlugin;
import tech.kayys.wayang.schema.catalog.BuiltinSchemaCatalog;
import tech.kayys.wayang.schema.vector.VectorSearchConfig;
import tech.kayys.wayang.vector.VectorStore;
import tech.kayys.wayang.vector.VectorQuery;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Map;

@ApplicationScoped
public class VectorSearchExecutor implements ExecutorPlugin {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Inject
    VectorStoreProvider vectorStoreProvider;

    @Override
    public String getSupportedNodeType() {
        return BuiltinSchemaCatalog.VECTOR_SEARCH;
    }

    @Override
    public Object execute(ExecutionContext context) throws Exception {
        Map<String, Object> rawConfig = context.getNode().getConfiguration();
        VectorSearchConfig config = MAPPER.convertValue(rawConfig, VectorSearchConfig.class);

        // For now, VectorStoreProvider handles setting up the specific DB. 
        // In a more advanced implementation, it might select based on config.getStoreType()
        VectorStore store = vectorStoreProvider.getVectorStore();

        // Assume the input to this node is the query text (or embedding array)
        Object rawInput = context.getInput();
        String queryText = rawInput != null ? rawInput.toString() : "";

        VectorQuery query = new VectorQuery(queryText, config.getTopK());
        
        // Return the reactive stream converted to a blocking response for the engine
        // Or return the Uni/Multi if the engine supports reactive types natively. 
        // Assuming blocking wrapper for EIP compatibility:
        return store.search(query, config.getFilters()).await().indefinitely();
    }
}
