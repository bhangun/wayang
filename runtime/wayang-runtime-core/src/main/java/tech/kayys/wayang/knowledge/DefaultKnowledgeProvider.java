package tech.kayys.wayang.knowledge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import tech.kayys.wayang.context.Document;
import tech.kayys.wayang.core.Id;
import tech.kayys.wayang.core.Metadata;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.ResourceType;

public class DefaultKnowledgeProvider implements KnowledgeProvider {
    
    private final String id;
    private final String name;
    private final String version;
    private final Metadata metadata;
    private final List<Document> knowledge = new CopyOnWriteArrayList<>();
    
    public DefaultKnowledgeProvider() {
        this.id = Id.random().asString();
        this.name = "default-knowledge-provider";
        this.version = "1.0.0";
        this.metadata = Metadata.builder()
            .name(name)
            .description("Default Knowledge Provider")
            .version(version)
            .label("type", "knowledge")
            .now()
            .build();
        
        // Add some default knowledge
        knowledge.add(Document.of("doc1", 
            "Paris is the capital of France. It is located on the Seine River.",
            "Paris"));
        knowledge.add(Document.of("doc2", 
            "London is the capital of the United Kingdom. It is located on the Thames River.",
            "London"));
        knowledge.add(Document.of("doc3", 
            "Berlin is the capital of Germany. It is located on the Spree River.",
            "Berlin"));
    }
    
    @Override
    public String id() { return id; }
    
    @Override
    public String name() { return name; }
    
    @Override
    public String version() { return version; }
    
    @Override
    public Metadata metadata() { return metadata; }
    
    @Override
    public ResourceType type() { return new ResourceType.Custom("knowledge"); }
    
    @Override
    public ResourceId resourceId() { return new ResourceId.CustomId(Id.fromString(id), type()); }
    
    @Override
    public KnowledgeResult retrieve(KnowledgeRequest request) throws Exception {
        List<Document> results = new ArrayList<>();
        List<KnowledgeFact> facts = new ArrayList<>();
        
        String query = request.query().toLowerCase();
        
        for (Document doc : knowledge) {
            if (doc.content().toLowerCase().contains(query)) {
                results.add(doc);
            }
        }
        
        // Apply limit
        if (results.size() > request.limit()) {
            results = results.subList(0, request.limit());
        }
        
        // Extract facts
        for (Document doc : results) {
            facts.add(KnowledgeFact.of(
                "wayang",
                "knows",
                doc.content().substring(0, Math.min(50, doc.content().length()))
            ));
        }
        
        return new KnowledgeResult(
            Id.random().asString(),
            request.query(),
            results,
            facts,
            Map.of("source", "default"),
            0,
            results.isEmpty() ? 0.0 : 1.0
        );
    }
}
