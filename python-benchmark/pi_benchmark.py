#!/usr/bin/env python3
"""
Distributed Pi Calculation Benchmark - Master/Worker Pattern

Dieses Skript demonstriert das Master/Worker Pattern für verteilte Berechnungen
und vergleicht verschiedene Implementierungen:

1. Sequential - Single-threaded Baseline
2. Multiprocessing - Python's multiprocessing Pool
3. ThreadPool - Thread-basierte Parallelisierung
4. Concurrent Futures - ProcessPoolExecutor

Die Monte-Carlo-Methode zur Pi-Berechnung ist ideal für diesen Vergleich:
- Perfekt parallelisierbar (embarrassingly parallel)
- Keine Abhängigkeiten zwischen Workers
- Einfache Aggregation der Ergebnisse
"""

import time
import random
import multiprocessing
from concurrent.futures import ProcessPoolExecutor, ThreadPoolExecutor, as_completed
import os
import platform
import sys
from dataclasses import dataclass
from typing import List, Tuple


@dataclass
class BenchmarkResult:
    """Ergebnis eines Benchmark-Durchlaufs."""
    method: str
    workers: int
    samples: int
    pi_estimate: float
    duration_ms: float
    throughput_million_per_sec: float
    points_inside: int


def monte_carlo_chunk(args: Tuple[int, int]) -> Tuple[int, int, float]:
    """
    Worker-Funktion: Berechnet Monte-Carlo-Samples für Pi.

    Args:
        args: Tuple von (worker_id, samples)

    Returns:
        Tuple von (worker_id, points_inside, duration)
    """
    worker_id, samples = args
    start_time = time.perf_counter()

    points_inside = 0
    for _ in range(samples):
        x = random.random()
        y = random.random()
        if x * x + y * y <= 1.0:
            points_inside += 1

    duration = time.perf_counter() - start_time
    return worker_id, points_inside, duration


def calculate_pi_sequential(total_samples: int) -> BenchmarkResult:
    """
    Sequentielle Pi-Berechnung (Baseline).

    Ein einzelner Thread verarbeitet alle Samples.
    """
    start_time = time.perf_counter()

    points_inside = 0
    for _ in range(total_samples):
        x = random.random()
        y = random.random()
        if x * x + y * y <= 1.0:
            points_inside += 1

    duration_ms = (time.perf_counter() - start_time) * 1000
    pi_estimate = 4.0 * points_inside / total_samples
    throughput = total_samples / (duration_ms / 1000) / 1_000_000

    return BenchmarkResult(
        method="Sequential",
        workers=1,
        samples=total_samples,
        pi_estimate=pi_estimate,
        duration_ms=duration_ms,
        throughput_million_per_sec=throughput,
        points_inside=points_inside
    )


def calculate_pi_multiprocessing(total_samples: int, worker_count: int) -> BenchmarkResult:
    """
    Parallele Pi-Berechnung mit multiprocessing Pool.

    Master/Worker Pattern:
    - Master: Verteilt Samples auf Workers
    - Workers: Berechnen unabhängig ihre Chunks
    - Master: Aggregiert die Ergebnisse
    """
    start_time = time.perf_counter()

    # Samples auf Workers verteilen
    samples_per_worker = total_samples // worker_count
    remaining = total_samples % worker_count

    work_packages = []
    for i in range(worker_count):
        samples = samples_per_worker + (remaining if i == worker_count - 1 else 0)
        work_packages.append((i, samples))

    # Workers ausführen
    with multiprocessing.Pool(processes=worker_count) as pool:
        results = pool.map(monte_carlo_chunk, work_packages)

    # Ergebnisse aggregieren
    total_inside = sum(r[1] for r in results)

    duration_ms = (time.perf_counter() - start_time) * 1000
    pi_estimate = 4.0 * total_inside / total_samples
    throughput = total_samples / (duration_ms / 1000) / 1_000_000

    return BenchmarkResult(
        method="Multiprocessing",
        workers=worker_count,
        samples=total_samples,
        pi_estimate=pi_estimate,
        duration_ms=duration_ms,
        throughput_million_per_sec=throughput,
        points_inside=total_inside
    )


def calculate_pi_threadpool(total_samples: int, worker_count: int) -> BenchmarkResult:
    """
    Parallele Pi-Berechnung mit ThreadPoolExecutor.

    Hinweis: Aufgrund des Python GIL (Global Interpreter Lock) ist Threading
    für CPU-intensive Aufgaben weniger effizient als Multiprocessing.
    """
    start_time = time.perf_counter()

    # Samples auf Workers verteilen
    samples_per_worker = total_samples // worker_count
    remaining = total_samples % worker_count

    work_packages = []
    for i in range(worker_count):
        samples = samples_per_worker + (remaining if i == worker_count - 1 else 0)
        work_packages.append((i, samples))

    total_inside = 0

    with ThreadPoolExecutor(max_workers=worker_count) as executor:
        futures = [executor.submit(monte_carlo_chunk, pkg) for pkg in work_packages]
        for future in as_completed(futures):
            _, points, _ = future.result()
            total_inside += points

    duration_ms = (time.perf_counter() - start_time) * 1000
    pi_estimate = 4.0 * total_inside / total_samples
    throughput = total_samples / (duration_ms / 1000) / 1_000_000

    return BenchmarkResult(
        method="ThreadPool",
        workers=worker_count,
        samples=total_samples,
        pi_estimate=pi_estimate,
        duration_ms=duration_ms,
        throughput_million_per_sec=throughput,
        points_inside=total_inside
    )


def calculate_pi_process_executor(total_samples: int, worker_count: int) -> BenchmarkResult:
    """
    Parallele Pi-Berechnung mit ProcessPoolExecutor.

    Ähnlich wie multiprocessing.Pool, aber mit modernerem concurrent.futures API.
    """
    start_time = time.perf_counter()

    # Samples auf Workers verteilen
    samples_per_worker = total_samples // worker_count
    remaining = total_samples % worker_count

    work_packages = []
    for i in range(worker_count):
        samples = samples_per_worker + (remaining if i == worker_count - 1 else 0)
        work_packages.append((i, samples))

    total_inside = 0

    with ProcessPoolExecutor(max_workers=worker_count) as executor:
        futures = [executor.submit(monte_carlo_chunk, pkg) for pkg in work_packages]
        for future in as_completed(futures):
            _, points, _ = future.result()
            total_inside += points

    duration_ms = (time.perf_counter() - start_time) * 1000
    pi_estimate = 4.0 * total_inside / total_samples
    throughput = total_samples / (duration_ms / 1000) / 1_000_000

    return BenchmarkResult(
        method="ProcessPool",
        workers=worker_count,
        samples=total_samples,
        pi_estimate=pi_estimate,
        duration_ms=duration_ms,
        throughput_million_per_sec=throughput,
        points_inside=total_inside
    )


def print_header():
    """Druckt den Benchmark-Header."""
    print("+" + "=" * 70 + "+")
    print("|" + " DISTRIBUTED COMPUTING BENCHMARK - Pi Calculation ".center(70) + "|")
    print("|" + " Master/Worker Pattern ".center(70) + "|")
    print("+" + "=" * 70 + "+")
    print(f"| CPU Cores:   {os.cpu_count():<57}|")
    print(f"| Python:      {platform.python_version():<57}|")
    print(f"| OS:          {platform.system()} {platform.release():<47}|")
    print("+" + "=" * 70 + "+")
    print()


def print_results_table(results: List[BenchmarkResult], baseline_ms: float):
    """Druckt die Ergebnis-Tabelle."""
    print("\n" + "-" * 80)
    print(f"{'Method':<15} {'Workers':>8} {'Time (ms)':>12} {'Throughput':>14} {'Speedup':>10} {'Pi':>16}")
    print(f"{'':15} {'':>8} {'':>12} {'(M/s)':>14} {'':>10} {'':>16}")
    print("-" * 80)

    for r in results:
        speedup = baseline_ms / r.duration_ms if r.duration_ms > 0 else 0
        print(f"{r.method:<15} {r.workers:>8} {r.duration_ms:>12.2f} {r.throughput_million_per_sec:>14.2f} "
              f"{speedup:>9.2f}x {r.pi_estimate:>16.10f}")

    print("-" * 80)


def run_benchmark():
    """Führt den kompletten Benchmark durch."""
    print_header()

    # Benchmark-Parameter
    sample_counts = [1_000_000, 10_000_000, 100_000_000]
    worker_counts = [1, 2, 4, 8]
    cpu_count = os.cpu_count() or 4

    # Limitiere Worker auf verfügbare CPUs
    worker_counts = [w for w in worker_counts if w <= cpu_count * 2]

    print("Warming up JVM/Python interpreter...")
    _ = calculate_pi_sequential(100_000)
    _ = calculate_pi_multiprocessing(100_000, 2)
    print("Warmup complete.\n")

    for samples in sample_counts:
        print("=" * 80)
        print(f" BENCHMARK: {samples:,} Samples ".center(80))
        print("=" * 80)

        all_results = []

        # 1. Sequentielle Baseline
        print("\n[1/4] Running Sequential baseline...")
        seq_result = calculate_pi_sequential(samples)
        all_results.append(seq_result)
        baseline_ms = seq_result.duration_ms
        print(f"      Sequential: {seq_result.duration_ms:.2f} ms, Pi = {seq_result.pi_estimate:.10f}")

        # 2. Multiprocessing mit verschiedenen Worker-Zahlen
        print("\n[2/4] Running Multiprocessing Pool...")
        for workers in worker_counts:
            if workers > 1:
                result = calculate_pi_multiprocessing(samples, workers)
                all_results.append(result)
                print(f"      {workers} workers: {result.duration_ms:.2f} ms")

        # 3. ThreadPool
        print("\n[3/4] Running ThreadPool (for comparison)...")
        for workers in worker_counts:
            if workers > 1:
                result = calculate_pi_threadpool(samples, workers)
                all_results.append(result)
                print(f"      {workers} workers: {result.duration_ms:.2f} ms")

        # 4. ProcessPoolExecutor
        print("\n[4/4] Running ProcessPoolExecutor...")
        for workers in worker_counts:
            if workers > 1:
                result = calculate_pi_process_executor(samples, workers)
                all_results.append(result)
                print(f"      {workers} workers: {result.duration_ms:.2f} ms")

        # Ergebnis-Tabelle
        print_results_table(all_results, baseline_ms)

    # Zusammenfassung
    print("\n+" + "=" * 70 + "+")
    print("|" + " SUMMARY ".center(70) + "|")
    print("+" + "=" * 70 + "+")
    print("| Key Observations:                                                    |")
    print("| - Multiprocessing provides true parallelism (bypasses GIL)           |")
    print("| - ThreadPool is limited by Python's GIL for CPU-bound tasks          |")
    print("| - Speedup scales with worker count up to CPU core limit              |")
    print("| - Message passing overhead is minimal for large workloads            |")
    print("|                                                                      |")
    print("| This pattern mirrors the Akka Actor model:                           |")
    print("| - Master distributes work packages                                   |")
    print("| - Workers compute independently (no shared state)                    |")
    print("| - Master aggregates results                                          |")
    print("+" + "=" * 70 + "+")


if __name__ == "__main__":
    multiprocessing.freeze_support()  # Windows support
    run_benchmark()
