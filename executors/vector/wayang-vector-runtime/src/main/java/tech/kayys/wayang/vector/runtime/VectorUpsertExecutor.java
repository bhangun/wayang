package tech.kayys.wayang.vector.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import tech.kayys.gamelan.engine.context.ExecutionContext;
import tech.kayys.gamelan.engine.plugin.ExecutorPlugin;
import tech.kayys.wayang.schema.catalog.BuiltinSchemaCatalog;
import tech.kayys.wayang.schema.vector.VectorUpsertConfig;
import tech.kayys.wayang.vector.VectorStore;
import tech.kayys.wayang.vector.VectorEntry;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@ApplicationScoped
public class VectorUpsertExecutor implements ExecutorPlugin {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Inject
    VectorStoreProvider vectorStoreProvider;

    @Override
    public String getSupportedNodeType() {
        return BuiltinSchemaCatalog.VECTOR_UPSERT;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object execute(ExecutionContext context) throws Exception {
        Map<String, Object> rawConfig = context.getNode().getConfiguration();
        VectorUpsertConfig config = MAPPER.convertValue(rawConfig, VectorUpsertConfig.class);

        VectorStore store = vectorStoreProvider.getVectorStore();

        // Extract input. We expect a List of VectorEntry records or mapped Maps.
        Object input = context.getInput();
        List<VectorEntry> entries = new ArrayList<>();
        
        if (input instanceof List) {
            for (Object item : (List<?>) input) {
                if (item instanceof VectorEntry) {
                    entries.add((VectorEntry) item);
                } else if (item instanceof Map) {
                    entries.add(MAPPER.convertValue(item, VectorEntry.class));
                }
            }
        }

        if (entries.isEmpty()) {
            throw new IllegalArgumentException("No valid VectorEntries found in input.");
        }

        // Store and block until completion
        store.store(entries).await().indefinitely();
        
        return Map.of(
            "status", "success", 
            "inserted", entries.size(),
            "collection", config.getCollectionName() != null ? config.getCollectionName() : "default"
        );
    }
}
