package com.example.pi;

import akka.actor.typed.ActorRef;

/**
 * Message-Protokoll fuer die Pi-Berechnung mit dem Master/Worker Pattern.
 * Alle Messages sind immutable (unveraenderlich).
 */
public class PiMessages {

    // Marker-Interface fuer Master-Commands
    public interface MasterCommand {}

    // Marker-Interface fuer Worker-Commands
    public interface WorkerCommand {}

    /**
     * Befehl an den Master, die Pi-Berechnung zu starten.
     */
    public static final class StartCalculation implements MasterCommand {
        public final long totalSamples;
        public final int workerCount;
        public final ActorRef<CalculationResult> replyTo;

        public StartCalculation(long totalSamples, int workerCount, ActorRef<CalculationResult> replyTo) {
            this.totalSamples = totalSamples;
            this.workerCount = workerCount;
            this.replyTo = replyTo;
        }
    }

    /**
     * Worker-Ergebnis, das an den Master zurueckgemeldet wird.
     */
    public static final class WorkerResult implements MasterCommand {
        public final int workerId;
        public final long samplesProcessed;
        public final long pointsInsideCircle;
        public final long durationNanos;

        public WorkerResult(int workerId, long samplesProcessed, long pointsInsideCircle, long durationNanos) {
            this.workerId = workerId;
            this.samplesProcessed = samplesProcessed;
            this.pointsInsideCircle = pointsInsideCircle;
            this.durationNanos = durationNanos;
        }
    }

    /**
     * Arbeitsauftrag an einen Worker.
     */
    public static final class CalculateChunk implements WorkerCommand {
        public final int workerId;
        public final long samples;
        public final ActorRef<MasterCommand> replyTo;

        public CalculateChunk(int workerId, long samples, ActorRef<MasterCommand> replyTo) {
            this.workerId = workerId;
            this.samples = samples;
            this.replyTo = replyTo;
        }
    }

    /**
     * Endergebnis der Pi-Berechnung.
     */
    public static final class CalculationResult {
        public final double piEstimate;
        public final long totalSamples;
        public final long totalPointsInside;
        public final long totalDurationNanos;
        public final int workerCount;

        public CalculationResult(double piEstimate, long totalSamples, long totalPointsInside,
                                 long totalDurationNanos, int workerCount) {
            this.piEstimate = piEstimate;
            this.totalSamples = totalSamples;
            this.totalPointsInside = totalPointsInside;
            this.totalDurationNanos = totalDurationNanos;
            this.workerCount = workerCount;
        }

        @Override
        public String toString() {
            double durationMs = totalDurationNanos / 1_000_000.0;
            double throughput = totalSamples / (durationMs / 1000.0);
            return String.format(
                "=== Pi Calculation Result ===%n" +
                "Pi Estimate:     %.10f%n" +
                "Actual Pi:       %.10f%n" +
                "Error:           %.10f%%%n" +
                "Total Samples:   %,d%n" +
                "Points Inside:   %,d%n" +
                "Worker Count:    %d%n" +
                "Duration:        %.2f ms%n" +
                "Throughput:      %,.0f samples/sec%n" +
                "============================",
                piEstimate,
                Math.PI,
                Math.abs(piEstimate - Math.PI) / Math.PI * 100,
                totalSamples,
                totalPointsInside,
                workerCount,
                durationMs,
                throughput
            );
        }
    }
}
