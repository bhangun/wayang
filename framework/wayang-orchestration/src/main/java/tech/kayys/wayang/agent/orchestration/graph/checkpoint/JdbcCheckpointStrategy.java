package tech.kayys.wayang.agent.orchestration.graph.checkpoint;

import tech.kayys.wayang.agent.orchestration.graph.state.GraphState;

/**
 * A placeholder JDBC strategy for distributed/persistent execution.
 * In a production setting, this would serialize GraphState to JSON 
 * and store it in PostgreSQL.
 */
public class JdbcCheckpointStrategy implements CheckpointStrategy {

    // For a real implementation, we would inject a DataSource or Hibernate Panache Repository

    @Override
    public void save(String threadId, GraphState state) {
        // TODO: Serialize state.getData() to JSON and run UPDATE/INSERT
        throw new UnsupportedOperationException("JdbcCheckpointStrategy is not fully implemented yet.");
    }

    @Override
    public GraphState load(String threadId) {
        // TODO: Run SELECT, deserialize JSON to GraphState
        throw new UnsupportedOperationException("JdbcCheckpointStrategy is not fully implemented yet.");
    }
}
