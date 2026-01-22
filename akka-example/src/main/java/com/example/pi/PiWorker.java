package com.example.pi;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.Random;

/**
 * Worker Actor fuer die Monte-Carlo Pi-Berechnung.
 *
 * Jeder Worker:
 * - Empfaengt einen Arbeitsauftrag (CalculateChunk) vom Master
 * - Fuehrt die Monte-Carlo-Simulation fuer seine Samples durch
 * - Sendet das Ergebnis (WorkerResult) zurueck an den Master
 *
 * Das Actor-Pattern garantiert:
 * - Keine geteilten Zustaende (shared state)
 * - Thread-Sicherheit durch Message-Passing
 * - Unabhaengige Ausfuehrung auf verschiedenen Threads/Servern
 */
public class PiWorker extends AbstractBehavior<PiMessages.WorkerCommand> {

    private final Random random;

    private PiWorker(ActorContext<PiMessages.WorkerCommand> context) {
        super(context);
        // Jeder Worker hat seinen eigenen Random-Generator
        this.random = new Random();
    }

    public static Behavior<PiMessages.WorkerCommand> create() {
        return Behaviors.setup(PiWorker::new);
    }

    @Override
    public Receive<PiMessages.WorkerCommand> createReceive() {
        return newReceiveBuilder()
            .onMessage(PiMessages.CalculateChunk.class, this::onCalculateChunk)
            .build();
    }

    /**
     * Verarbeitet einen Arbeitsauftrag.
     *
     * Monte-Carlo-Methode:
     * 1. Generiere zufaellige Punkte (x, y) im Einheitsquadrat [0, 1]
     * 2. Pruefe ob x^2 + y^2 <= 1 (Punkt liegt im Viertelkreis)
     * 3. Pi ~ 4 * (Punkte im Kreis / Gesamtpunkte)
     */
    private Behavior<PiMessages.WorkerCommand> onCalculateChunk(PiMessages.CalculateChunk msg) {
        getContext().getLog().info("Worker {} started processing {} samples",
            msg.workerId, msg.samples);

        long startTime = System.nanoTime();
        long pointsInside = 0;

        // Monte-Carlo-Simulation
        for (long i = 0; i < msg.samples; i++) {
            double x = random.nextDouble();
            double y = random.nextDouble();

            // Pruefe ob Punkt im Viertelkreis liegt
            if (x * x + y * y <= 1.0) {
                pointsInside++;
            }
        }

        long duration = System.nanoTime() - startTime;

        getContext().getLog().info("Worker {} finished: {} points inside circle ({}%)",
            msg.workerId, pointsInside,
            String.format("%.2f", (double) pointsInside / msg.samples * 100));

        // Sende Ergebnis an Master zurueck
        msg.replyTo.tell(new PiMessages.WorkerResult(
            msg.workerId,
            msg.samples,
            pointsInside,
            duration
        ));

        return this;
    }
}
