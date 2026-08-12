package tech.kayys.wayang.execution.journal;

import java.time.Instant;
import java.util.List;

/**
 * A ledger of what happened during an execution. 
 * Distinct from a checkpoint (which is current state), this records the historical steps taken.
 */
public interface ExecutionJournal {

    /**
     * Appends an event to the journal.
     */
    void append(String executionId, JournalEvent event);
    
    /**
     * Retrieves all events for a given execution in chronological order.
     */
    List<JournalEvent> read(String executionId);

    record JournalEvent(
        String eventId,
        String type,
        String summary,
        Instant timestamp,
        Object metadata
    ) {}
}
