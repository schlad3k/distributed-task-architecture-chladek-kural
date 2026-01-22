package com.example.pi;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.ArrayList;
import java.util.List;

/**
 * Master Actor fuer die verteilte Pi-Berechnung.
 *
 * Der Master koordiniert:
 * 1. Erstellung der Worker-Actors
 * 2. Verteilung der Arbeitspakete
 * 3. Sammlung und Aggregation der Ergebnisse
 * 4. Berechnung des finalen Pi-Wertes
 *
 * Architektur:
 * ┌─────────────────────────────────────────────────────────────┐
 * │                         MASTER                               │
 * │  - Empfaengt StartCalculation                                │
 * │  - Erstellt N Worker                                         │
 * │  - Verteilt Samples gleichmaessig                            │
 * │  - Aggregiert WorkerResults                                  │
 * │  - Berechnet Pi und sendet CalculationResult                 │
 * └─────────────────────────────────────────────────────────────┘
 */
public class PiMaster extends AbstractBehavior<PiMessages.MasterCommand> {

    // Zustand fuer aktive Berechnung
    private ActorRef<PiMessages.CalculationResult> resultReceiver;
    private int expectedResults;
    private int receivedResults;
    private long totalPointsInside;
    private long totalSamples;
    private long calculationStartTime;
    private final List<PiMessages.WorkerResult> workerResults;

    private PiMaster(ActorContext<PiMessages.MasterCommand> context) {
        super(context);
        this.workerResults = new ArrayList<>();
        reset();
    }

    public static Behavior<PiMessages.MasterCommand> create() {
        return Behaviors.setup(PiMaster::new);
    }

    @Override
    public Receive<PiMessages.MasterCommand> createReceive() {
        return newReceiveBuilder()
            .onMessage(PiMessages.StartCalculation.class, this::onStartCalculation)
            .onMessage(PiMessages.WorkerResult.class, this::onWorkerResult)
            .build();
    }

    /**
     * Startet eine neue Pi-Berechnung.
     *
     * 1. Erstellt die angeforderte Anzahl an Workers
     * 2. Berechnet Samples pro Worker
     * 3. Sendet CalculateChunk an jeden Worker
     */
    private Behavior<PiMessages.MasterCommand> onStartCalculation(PiMessages.StartCalculation msg) {
        getContext().getLog().info("Master starting calculation: {} samples with {} workers",
            msg.totalSamples, msg.workerCount);

        reset();
        this.resultReceiver = msg.replyTo;
        this.expectedResults = msg.workerCount;
        this.totalSamples = msg.totalSamples;
        this.calculationStartTime = System.nanoTime();

        // Samples gleichmaessig auf Workers verteilen
        long samplesPerWorker = msg.totalSamples / msg.workerCount;
        long remainingSamples = msg.totalSamples % msg.workerCount;

        // Workers erstellen und Arbeit verteilen
        for (int i = 0; i < msg.workerCount; i++) {
            // Worker-Actor erstellen (Child-Actor des Masters)
            ActorRef<PiMessages.WorkerCommand> worker = getContext().spawn(
                PiWorker.create(),
                "worker-" + i
            );

            // Letzer Worker bekommt eventuelle Rest-Samples
            long samples = samplesPerWorker;
            if (i == msg.workerCount - 1) {
                samples += remainingSamples;
            }

            getContext().getLog().debug("Sending {} samples to Worker {}", samples, i);

            // Arbeitsauftrag an Worker senden
            worker.tell(new PiMessages.CalculateChunk(i, samples, getContext().getSelf()));
        }

        return this;
    }

    /**
     * Verarbeitet ein Worker-Ergebnis.
     *
     * Wenn alle Ergebnisse da sind:
     * - Berechnet Pi aus aggregierten Daten
     * - Sendet finales Ergebnis an den Auftraggeber
     */
    private Behavior<PiMessages.MasterCommand> onWorkerResult(PiMessages.WorkerResult msg) {
        getContext().getLog().info("Received result from Worker {}: {} points inside from {} samples",
            msg.workerId, msg.pointsInsideCircle, msg.samplesProcessed);

        workerResults.add(msg);
        totalPointsInside += msg.pointsInsideCircle;
        receivedResults++;

        // Pruefen ob alle Ergebnisse eingetroffen sind
        if (receivedResults >= expectedResults) {
            long totalDuration = System.nanoTime() - calculationStartTime;

            // Pi berechnen: Pi = 4 * (Punkte im Kreis / Gesamtpunkte)
            double piEstimate = 4.0 * totalPointsInside / totalSamples;

            getContext().getLog().info("All workers finished. Pi estimate: {}", piEstimate);

            // Detaillierte Worker-Statistiken ausgeben
            getContext().getLog().info("=== Worker Statistics ===");
            for (PiMessages.WorkerResult wr : workerResults) {
                double workerDurationMs = wr.durationNanos / 1_000_000.0;
                double workerThroughput = wr.samplesProcessed / (workerDurationMs / 1000.0);
                getContext().getLog().info("Worker {}: {} samples, {} inside, {:.2f} ms, {:.0f} samples/sec",
                    wr.workerId, wr.samplesProcessed, wr.pointsInsideCircle,
                    workerDurationMs, workerThroughput);
            }

            // Ergebnis an Auftraggeber senden
            resultReceiver.tell(new PiMessages.CalculationResult(
                piEstimate,
                totalSamples,
                totalPointsInside,
                totalDuration,
                expectedResults
            ));

            // Workers beenden (werden automatisch gestoppt als Child-Actors)
        }

        return this;
    }

    private void reset() {
        this.resultReceiver = null;
        this.expectedResults = 0;
        this.receivedResults = 0;
        this.totalPointsInside = 0;
        this.totalSamples = 0;
        this.calculationStartTime = 0;
        this.workerResults.clear();
    }
}
