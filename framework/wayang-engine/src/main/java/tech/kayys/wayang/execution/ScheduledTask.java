package tech.kayys.wayang.execution;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents a scheduled task.
 */
public final class ScheduledTask {

    private final String id;
    private final String name;
    private final Runnable task;
    private final Instant scheduledAt;
    private final Duration interval;
    private volatile boolean cancelled;
    private volatile Instant lastRunAt;
    private volatile Instant nextRunAt;
    private volatile int runCount;

    public ScheduledTask(String name, Runnable task, Instant scheduledAt, Duration interval) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.task = task;
        this.scheduledAt = scheduledAt;
        this.interval = interval;
        this.cancelled = false;
        this.runCount = 0;
        this.nextRunAt = scheduledAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public Duration getInterval() {
        return interval;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public Instant getLastRunAt() {
        return lastRunAt;
    }

    public Instant getNextRunAt() {
        return nextRunAt;
    }

    public int getRunCount() {
        return runCount;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public void run() {
        if (cancelled) {
            return;
        }
        this.lastRunAt = Instant.now();
        this.runCount++;
        this.task.run();
        if (!cancelled && interval != null) {
            this.nextRunAt = Instant.now().plus(interval);
        }
    }

    public boolean isDue() {
        return !cancelled && nextRunAt != null && Instant.now().isAfter(nextRunAt);
    }

    public static ScheduledTask once(String name, Runnable task, Instant scheduledAt) {
        return new ScheduledTask(name, task, scheduledAt, null);
    }

    public static ScheduledTask recurring(String name, Runnable task, Instant scheduledAt, Duration interval) {
        return new ScheduledTask(name, task, scheduledAt, interval);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private Runnable task;
        private Instant scheduledAt;
        private Duration interval;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder task(Runnable task) {
            this.task = task;
            return this;
        }

        public Builder scheduledAt(Instant scheduledAt) {
            this.scheduledAt = scheduledAt;
            return this;
        }

        public Builder interval(Duration interval) {
            this.interval = interval;
            return this;
        }

        public ScheduledTask build() {
            if (scheduledAt == null) {
                scheduledAt = Instant.now();
            }
            return new ScheduledTask(name, task, scheduledAt, interval);
        }
    }
}
