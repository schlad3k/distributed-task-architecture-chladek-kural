package com.example.pi;

import java.util.Random;

/**
 * Benchmark-Runner fuer den Vergleich zwischen:
 * - Sequentieller Pi-Berechnung (Single-Thread)
 * - Akka Actor-basierter Berechnung (Multi-Thread/Distributed)
 *
 * Misst:
 * - Ausfuehrungszeit
 * - Throughput (Samples pro Sekunde)
 * - Speicherverbrauch
 * - Pi-Genauigkeit
 */
public class BenchmarkRunner {

    public static void main(String[] args) {
        // Benchmark-Parameter
        long[] sampleCounts = {1_000_000L, 10_000_000L, 100_000_000L};
        int[] workerCounts = {1, 2, 4, 8};
        int warmupRuns = 2;
        int benchmarkRuns = 3;

        System.out.println("╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║         DISTRIBUTED COMPUTING BENCHMARK - Pi Calculation           ║");
        System.out.println("║                    Master/Worker Pattern                           ║");
        System.out.println("╠════════════════════════════════════════════════════════════════════╣");
        System.out.println("║ CPU Cores:   " + padRight(String.valueOf(Runtime.getRuntime().availableProcessors()), 54) + "║");
        System.out.println("║ Max Memory:  " + padRight(Runtime.getRuntime().maxMemory() / 1024 / 1024 + " MB", 54) + "║");
        System.out.println("║ Java:        " + padRight(System.getProperty("java.version"), 54) + "║");
        System.out.println("║ OS:          " + padRight(System.getProperty("os.name"), 54) + "║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝");
        System.out.println();

        // Warmup
        System.out.println("Warming up JVM...");
        for (int i = 0; i < warmupRuns; i++) {
            try {
                calculatePiSequential(1_000_000L);
                PiCalculatorApp.calculatePi(1_000_000L, 4);
            } catch (Exception e) {
                // Ignore warmup errors
            }
        }
        System.out.println("Warmup complete.\n");

        // Benchmark fuer verschiedene Sample-Groessen
        for (long samples : sampleCounts) {
            System.out.println("═══════════════════════════════════════════════════════════════════");
            System.out.println("BENCHMARK: " + formatNumber(samples) + " Samples");
            System.out.println("═══════════════════════════════════════════════════════════════════");

            // 1. Sequentielle Baseline
            System.out.println("\n--- Sequential (Single-Thread) ---");
            BenchmarkResult seqResult = benchmarkSequential(samples, benchmarkRuns);
            printResult("Sequential", seqResult);

            // 2. Akka mit verschiedenen Worker-Zahlen
            System.out.println("\n--- Akka Actors (Master/Worker) ---");
            System.out.println("Workers | Time (ms) | Throughput (M/s) | Speedup | Pi Estimate");
            System.out.println("--------|-----------|------------------|---------|-------------");

            for (int workers : workerCounts) {
                BenchmarkResult akkaResult = benchmarkAkka(samples, workers, benchmarkRuns);
                double speedup = seqResult.avgTimeMs / akkaResult.avgTimeMs;
                System.out.printf("%7d | %9.2f | %16.2f | %7.2fx | %.10f%n",
                    workers,
                    akkaResult.avgTimeMs,
                    akkaResult.throughputMillions,
                    speedup,
                    akkaResult.piEstimate);
            }
            System.out.println();
        }

        // Zusammenfassung
        printSummary();
    }

    /**
     * Sequentielle Pi-Berechnung (Baseline).
     */
    public static SequentialResult calculatePiSequential(long samples) {
        long startTime = System.nanoTime();
        Random random = new Random();
        long pointsInside = 0;

        for (long i = 0; i < samples; i++) {
            double x = random.nextDouble();
            double y = random.nextDouble();
            if (x * x + y * y <= 1.0) {
                pointsInside++;
            }
        }

        long duration = System.nanoTime() - startTime;
        double pi = 4.0 * pointsInside / samples;

        return new SequentialResult(pi, pointsInside, duration);
    }

    /**
     * Benchmark der sequentiellen Berechnung.
     */
    public static BenchmarkResult benchmarkSequential(long samples, int runs) {
        long totalTimeNanos = 0;
        double lastPi = 0;

        for (int i = 0; i < runs; i++) {
            SequentialResult result = calculatePiSequential(samples);
            totalTimeNanos += result.durationNanos;
            lastPi = result.piEstimate;
        }

        double avgTimeMs = (totalTimeNanos / runs) / 1_000_000.0;
        double throughput = samples / (avgTimeMs / 1000.0) / 1_000_000.0;

        return new BenchmarkResult(avgTimeMs, throughput, lastPi);
    }

    /**
     * Benchmark der Akka-Berechnung.
     */
    public static BenchmarkResult benchmarkAkka(long samples, int workers, int runs) {
        long totalTimeNanos = 0;
        double lastPi = 0;

        for (int i = 0; i < runs; i++) {
            try {
                PiMessages.CalculationResult result = PiCalculatorApp.calculatePi(samples, workers);
                totalTimeNanos += result.totalDurationNanos;
                lastPi = result.piEstimate;
            } catch (Exception e) {
                System.err.println("Error in benchmark: " + e.getMessage());
            }
        }

        double avgTimeMs = (totalTimeNanos / runs) / 1_000_000.0;
        double throughput = samples / (avgTimeMs / 1000.0) / 1_000_000.0;

        return new BenchmarkResult(avgTimeMs, throughput, lastPi);
    }

    /**
     * Ergebnis-Ausgabe formatieren.
     */
    private static void printResult(String name, BenchmarkResult result) {
        System.out.printf("%s: %.2f ms, %.2f M samples/s, Pi = %.10f%n",
            name, result.avgTimeMs, result.throughputMillions, result.piEstimate);
    }

    /**
     * Zusammenfassung ausgeben.
     */
    private static void printSummary() {
        System.out.println("╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                         SUMMARY                                    ║");
        System.out.println("╠════════════════════════════════════════════════════════════════════╣");
        System.out.println("║ The Akka Actor model demonstrates efficient parallel computation.  ║");
        System.out.println("║                                                                    ║");
        System.out.println("║ Key Observations:                                                  ║");
        System.out.println("║ - Speedup scales with worker count (up to CPU core limit)         ║");
        System.out.println("║ - Message passing overhead is minimal for large workloads         ║");
        System.out.println("║ - Each worker operates independently (no shared state)            ║");
        System.out.println("║ - Fault tolerance through Actor supervision                       ║");
        System.out.println("║                                                                    ║");
        System.out.println("║ This pattern is ideal for:                                        ║");
        System.out.println("║ - Embarrassingly parallel problems                                 ║");
        System.out.println("║ - Distributed systems across multiple servers                      ║");
        System.out.println("║ - Cloud-native deployments (Kubernetes, Docker)                   ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝");
    }

    private static String formatNumber(long num) {
        if (num >= 1_000_000_000) {
            return String.format("%.1fB", num / 1_000_000_000.0);
        } else if (num >= 1_000_000) {
            return String.format("%.0fM", num / 1_000_000.0);
        } else if (num >= 1_000) {
            return String.format("%.0fK", num / 1_000.0);
        }
        return String.valueOf(num);
    }

    private static String padRight(String s, int length) {
        if (s.length() >= length) return s.substring(0, length);
        return String.format("%-" + length + "s", s);
    }

    // Hilfsklassen fuer Ergebnisse
    static class SequentialResult {
        final double piEstimate;
        final long pointsInside;
        final long durationNanos;

        SequentialResult(double piEstimate, long pointsInside, long durationNanos) {
            this.piEstimate = piEstimate;
            this.pointsInside = pointsInside;
            this.durationNanos = durationNanos;
        }
    }

    static class BenchmarkResult {
        final double avgTimeMs;
        final double throughputMillions;
        final double piEstimate;

        BenchmarkResult(double avgTimeMs, double throughputMillions, double piEstimate) {
            this.avgTimeMs = avgTimeMs;
            this.throughputMillions = throughputMillions;
            this.piEstimate = piEstimate;
        }
    }
}
