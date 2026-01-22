package com.example.pi;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Hauptanwendung fuer die verteilte Pi-Berechnung mit Akka.
 *
 * Demonstriert das Master/Worker Pattern:
 * 1. Erstellt ein ActorSystem
 * 2. Startet einen Master-Actor
 * 3. Sendet Berechnungsauftrag an Master
 * 4. Master erstellt Worker und verteilt Arbeit
 * 5. Aggregiert Ergebnisse und berechnet Pi
 *
 * Verwendung:
 *   java -jar akka-pi-calculator.jar [samples] [workers]
 *
 *   samples: Anzahl der Monte-Carlo-Samples (default: 100.000.000)
 *   workers: Anzahl der Worker-Actors (default: Anzahl CPU-Kerne)
 */
public class PiCalculatorApp {

    public static void main(String[] args) {
        // Parameter parsen
        long totalSamples = 100_000_000L;
        int workerCount = Runtime.getRuntime().availableProcessors();

        if (args.length >= 1) {
            try {
                totalSamples = Long.parseLong(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid samples count: " + args[0]);
                System.exit(1);
            }
        }

        if (args.length >= 2) {
            try {
                workerCount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid worker count: " + args[1]);
                System.exit(1);
            }
        }

        System.out.println("========================================");
        System.out.println("  Akka Pi Calculator - Master/Worker");
        System.out.println("========================================");
        System.out.println("Samples:     " + String.format("%,d", totalSamples));
        System.out.println("Workers:     " + workerCount);
        System.out.println("CPU Cores:   " + Runtime.getRuntime().availableProcessors());
        System.out.println("Max Memory:  " + Runtime.getRuntime().maxMemory() / 1024 / 1024 + " MB");
        System.out.println("========================================\n");

        // Pi-Berechnung ausfuehren
        try {
            PiMessages.CalculationResult result = calculatePi(totalSamples, workerCount);
            System.out.println("\n" + result.toString());
        } catch (Exception e) {
            System.err.println("Calculation failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Fuehrt die Pi-Berechnung mit Akka Actors durch.
     */
    public static PiMessages.CalculationResult calculatePi(long totalSamples, int workerCount)
            throws InterruptedException, ExecutionException, TimeoutException {

        // CompletableFuture fuer das Ergebnis
        CompletableFuture<PiMessages.CalculationResult> resultFuture = new CompletableFuture<>();

        // Guardian-Behavior, das den Master erstellt und koordiniert
        Behavior<Void> guardianBehavior = Behaviors.setup(context -> {
            // Master erstellen
            ActorRef<PiMessages.MasterCommand> master = context.spawn(
                PiMaster.create(),
                "pi-master"
            );

            // Result-Handler erstellen (empfaengt das Endergebnis)
            ActorRef<PiMessages.CalculationResult> resultHandler = context.spawn(
                Behaviors.receive(PiMessages.CalculationResult.class)
                    .onMessage(PiMessages.CalculationResult.class, result -> {
                        resultFuture.complete(result);
                        return Behaviors.stopped();
                    })
                    .build(),
                "result-handler"
            );

            // Berechnung starten
            master.tell(new PiMessages.StartCalculation(
                totalSamples,
                workerCount,
                resultHandler
            ));

            return Behaviors.empty();
        });

        // ActorSystem erstellen und starten
        ActorSystem<Void> system = ActorSystem.create(guardianBehavior, "pi-calculator-system");

        try {
            // Auf Ergebnis warten (Timeout: 10 Minuten)
            PiMessages.CalculationResult result = resultFuture.get(10, TimeUnit.MINUTES);
            return result;
        } finally {
            // System herunterfahren
            system.terminate();
            system.getWhenTerminated().toCompletableFuture().get(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Benchmark-Methode: Fuehrt mehrere Durchlaeufe durch und misst die Performance.
     */
    public static void runBenchmark(long totalSamples, int[] workerCounts, int iterations) {
        System.out.println("\n========================================");
        System.out.println("         BENCHMARK MODE");
        System.out.println("========================================");
        System.out.println("Samples per run: " + String.format("%,d", totalSamples));
        System.out.println("Iterations:      " + iterations);
        System.out.println("========================================\n");

        System.out.println("Workers | Avg Time (ms) | Throughput (M samples/s) | Pi Estimate");
        System.out.println("--------|---------------|--------------------------|-------------");

        for (int workerCount : workerCounts) {
            long totalTimeNanos = 0;
            double lastPi = 0;

            for (int i = 0; i < iterations; i++) {
                try {
                    PiMessages.CalculationResult result = calculatePi(totalSamples, workerCount);
                    totalTimeNanos += result.totalDurationNanos;
                    lastPi = result.piEstimate;
                } catch (Exception e) {
                    System.err.println("Benchmark failed for " + workerCount + " workers: " + e.getMessage());
                }
            }

            double avgTimeMs = (totalTimeNanos / iterations) / 1_000_000.0;
            double throughput = totalSamples / (avgTimeMs / 1000.0) / 1_000_000.0;

            System.out.printf("%7d | %13.2f | %24.2f | %.10f%n",
                workerCount, avgTimeMs, throughput, lastPi);
        }

        System.out.println("\nBenchmark completed.");
    }
}
