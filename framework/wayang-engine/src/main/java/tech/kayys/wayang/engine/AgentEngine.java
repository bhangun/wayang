package tech.kayys.wayang.engine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import tech.kayys.wayang.agent.AgentPhase;
import tech.kayys.wayang.execution.ExecutionContext;
import tech.kayys.wayang.execution.ExecutionState;
import tech.kayys.wayang.spi.service.EventService;

/**
 * Extensible Agent Engine
 */
public class AgentEngine {
    
    private final List<AgentPhase> phases;
    private final EventService eventService;
    
    public AgentEngine(List<AgentPhase> phases, EventService eventService) {
        this.phases = phases.stream()
            .sorted(Comparator.comparingInt(p -> p.order()))
            .toList();
        this.eventService = eventService;
    }
    
    public ExecutionContext execute(ExecutionContext context) throws Exception {
        for (AgentPhase phase : phases) {
            if (phase.supports(context)) {
                context = phase.execute(context);
                if (context.state() == ExecutionState.FAILED ||
                    context.state() == ExecutionState.CANCELLED) {
                    break;
                }
            }
        }
        return context;
    }
    
    public static class Builder {
        private final List<AgentPhase> phases = new ArrayList<>();
        private EventService eventService;
        
        public Builder phase(AgentPhase phase) {
            this.phases.add(phase);
            return this;
        }
        
        public Builder phases(List<AgentPhase> phases) {
            this.phases.addAll(phases);
            return this;
        }
        
        public Builder eventService(EventService eventService) {
            this.eventService = eventService;
            return this;
        }
        
        public AgentEngine build() {
            return new AgentEngine(phases, eventService);
        }
    }
}