# Distributed Task Architecture

Implementierung des Master/Worker Patterns mit Akka Typed Actors.

## Projektstruktur

```
.
├── akka-example/                    # Java/Akka Implementierung
│   ├── src/main/java/com/example/pi/
│   │   ├── PiMessages.java          # Message-Protokoll
│   │   ├── PiWorker.java            # Worker Actor
│   │   ├── PiMaster.java            # Master Actor
│   │   ├── PiCalculatorApp.java     # Hauptanwendung
│   │   └── BenchmarkRunner.java     # Benchmark
│   └── pom.xml
├── distributed-computing-comparison.md
└── prompts.md
```

## Technologievergleich

| Technologie | Sprache | Architektur | Use Case |
|-------------|---------|-------------|----------|
| Apache Spark | Scala/Java/Python | Cluster (DAG) | Big Data |
| **Akka** | Scala/Java | Actor Model | Low-Latency |
| Celery | Python | Task Queue | Async Tasks |
| Ray | Python | Distributed | ML/AI |
| Kafka | Java | Event Streaming | High-Throughput |
| Dask | Python | Task Graph | Scientific |

## Ausfuehrung

**Pi-Berechnung:**
```cmd
cd akka-example
run.bat 10000000 4
```

**Benchmark:**
```cmd
cd akka-example
benchmark.bat
```

## Benchmark-Ergebnisse

Monte-Carlo Pi-Berechnung mit 100M Samples (Akka):

| Workers | Zeit | Speedup |
|---------|------|---------|
| 1 (Sequential) | ~13s | 1.0x |
| 2 | ~7s | 1.9x |
| 4 | ~3.5s | 3.7x |
| 8 | ~2s | **5x** |

## Quellen

[1] C. Hewitt, P. Bishop, and R. Steiger, "A universal modular ACTOR formalism for artificial intelligence," in *Proc. 3rd Int. Joint Conf. Artificial Intelligence*, Stanford, CA, USA, 1973, pp. 235-245.

[2] J. Dean and S. Ghemawat, "MapReduce: Simplified data processing on large clusters," in *Proc. 6th Symp. Operating Systems Design and Implementation*, San Francisco, CA, USA, 2004, pp. 137-150.

[3] M. Zaharia et al., "Apache Spark: A unified engine for big data processing," *Commun. ACM*, vol. 59, no. 11, pp. 56-65, Nov. 2016.

[4] Lightbend, "Akka Documentation," 2024. [Online]. Available: https://doc.akka.io/

## Autoren

Chladek, Kural
