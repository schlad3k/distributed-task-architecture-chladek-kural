# Distributed Task Architecture

Implementierung des Master/Worker Patterns mit Akka Typed Actors fuer verteilte Berechnungen.

## Was ist das Master/Worker Pattern?

Komplexe Aufgaben werden auf mehrere unabhaengige Worker aufgeteilt:

```
              ┌─────────────────────────────────────┐
              │             MASTER                  │
              │  - Verteilt Arbeitspakete           │
              │  - Sammelt Ergebnisse               │
              │  - Berechnet Endergebnis            │
              └─────────────────────────────────────┘
                    │           │           │
                    ▼           ▼           ▼
              ┌─────────┐ ┌─────────┐ ┌─────────┐
              │ Worker1 │ │ Worker2 │ │ Worker3 │
              │ 25 Mio  │ │ 25 Mio  │ │ 25 Mio  │
              └─────────┘ └─────────┘ └─────────┘
```

**Vorteile:**
- Skalierbar (mehr Worker = schneller)
- Workers koennen auf verschiedenen Servern laufen
- Fault Tolerance durch Actor Supervision

## Warum Akka?

Akka implementiert das **Actor Model**:
- Jeder Actor ist eine isolierte Einheit mit eigenem Zustand
- Kommunikation nur ueber Messages (kein shared state)
- Location Transparency - Actors koennen lokal oder remote laufen
- Automatische Fehlerbehandlung durch Supervision Hierarchies

## Projektstruktur

```
akka-example/
├── src/main/java/com/example/pi/
│   ├── PiMessages.java      # Definiert Message-Typen (StartCalculation, WorkerResult, etc.)
│   ├── PiWorker.java        # Worker Actor - fuehrt Monte-Carlo Simulation durch
│   ├── PiMaster.java        # Master Actor - koordiniert Worker, aggregiert Ergebnisse
│   ├── PiCalculatorApp.java # Hauptanwendung - erstellt ActorSystem
│   └── BenchmarkRunner.java # Vergleicht Sequential vs. Akka mit 1/2/4/8 Workers
├── pom.xml                  # Maven Dependencies (Akka 2.8.5)
├── run.bat                  # Startet Pi-Berechnung
└── benchmark.bat            # Startet Benchmark
```

## Ausfuehrung

**Pi-Berechnung (10 Mio Samples, 4 Workers):**
```cmd
cd akka-example
run.bat 10000000 4
```

**Benchmark (vergleicht 1/2/4/8 Workers):**
```cmd
cd akka-example
benchmark.bat
```

## Benchmark-Ergebnisse

Monte-Carlo Pi-Berechnung mit 100M Samples:

| Workers | Zeit | Speedup | Throughput |
|---------|------|---------|------------|
| 1 (Sequential) | ~13s | 1.0x | 7.7 M/s |
| 2 Actors | ~7s | 1.9x | 14 M/s |
| 4 Actors | ~3.5s | 3.7x | 28 M/s |
| 8 Actors | ~2s | **5.3x** | 40 M/s |

## Technologievergleich

| Technologie | Architektur | Sprachen | Haupteinsatz |
|-------------|-------------|----------|--------------|
| **Akka** | Actor Model | Java, Scala | Low-Latency, Reactive Systems |
| Apache Spark | DAG-basiert | Scala, Java, Python | Big Data Analytics |
| Ray | Distributed Runtime | Python | ML/AI Training |
| Celery | Task Queue | Python | Async Web Tasks |
| Kafka | Event Streaming | Java | High-Throughput Messaging |

Details: siehe `distributed-computing-comparison.md`

## Quellen

[1] C. Hewitt, P. Bishop, and R. Steiger, "A universal modular ACTOR formalism for artificial intelligence," *IJCAI*, 1973.

[2] J. Dean and S. Ghemawat, "MapReduce: Simplified data processing on large clusters," *OSDI*, 2004.

[3] M. Zaharia et al., "Apache Spark: A unified engine for big data processing," *Commun. ACM*, vol. 59, no. 11, 2016.

[4] Lightbend, "Akka Documentation," 2024. [Online]. Available: https://doc.akka.io/

## Autoren

Chladek, Kural
