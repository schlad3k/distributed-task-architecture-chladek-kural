# Distributed Task Architecture

Vergleich und Implementierung von Distributed Computing Technologien mit dem Master/Worker Pattern.

## Projektstruktur

```
.
├── akka-example/           # Java/Akka Actor-basierte Implementierung
├── python-benchmark/       # Python Multiprocessing Benchmark
├── distributed-computing-comparison.md  # Technologievergleich
└── prompts.md             # Verwendete Prompts
```

## Technologievergleich

| Technologie | Sprache | Architektur | Use Case |
|-------------|---------|-------------|----------|
| Apache Spark | Scala/Java/Python | Cluster-basiert (DAG) | Big Data |
| Akka | Scala/Java | Actor Model | Low-Latency |
| Celery | Python | Task Queue | Async Tasks |
| Ray | Python | Distributed Runtime | ML/AI |
| Kafka | Java | Event Streaming | High-Throughput |
| Dask | Python | Task Graph | Scientific |

## Benchmark-Ergebnisse

Monte-Carlo Pi-Berechnung mit 100M Samples:

| Methode | Workers | Zeit | Speedup |
|---------|---------|------|---------|
| Sequential | 1 | 13.3s | 1.0x |
| Multiprocessing | 8 | 2.5s | **5.27x** |

## Ausfuehrung

**Python Benchmark:**
```bash
cd python-benchmark
python pi_benchmark.py
```

**Akka (mit Maven):**
```bash
cd akka-example
mvn compile exec:java
```

## Quellen

[1] C. Hewitt, P. Bishop, and R. Steiger, "A universal modular ACTOR formalism for artificial intelligence," in *Proc. 3rd Int. Joint Conf. Artificial Intelligence*, Stanford, CA, USA, 1973, pp. 235-245.

[2] J. Dean and S. Ghemawat, "MapReduce: Simplified data processing on large clusters," in *Proc. 6th Symp. Operating Systems Design and Implementation*, San Francisco, CA, USA, 2004, pp. 137-150.

[3] M. Zaharia et al., "Apache Spark: A unified engine for big data processing," *Commun. ACM*, vol. 59, no. 11, pp. 56-65, Nov. 2016.

[4] P. Moritz et al., "Ray: A distributed framework for emerging AI applications," in *Proc. 13th USENIX Symp. Operating Systems Design and Implementation*, Carlsbad, CA, USA, 2018, pp. 561-577.

## Autoren

Chladek, Kural
