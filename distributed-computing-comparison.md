# Distributed Computing - Technologievergleich

## Übersicht Master/Worker Pattern

Das **Master/Worker Pattern** (auch Master-Slave oder Map-Reduce Pattern) ist ein fundamentales Architekturmuster für verteilte Systeme. Der Master koordiniert die Aufgabenverteilung, während Worker die eigentliche Berechnung durchführen.

```
┌─────────────────────────────────────────────────────────────┐
│                         MASTER                               │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │ Task Queue  │  │ Coordinator │  │  Aggregator │          │
│  └─────────────┘  └─────────────┘  └─────────────┘          │
└─────────────────────────────────────────────────────────────┘
         │                │                │
         ▼                ▼                ▼
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│  WORKER 1   │  │  WORKER 2   │  │  WORKER N   │
│  (Server A) │  │  (Server B) │  │  (Server X) │
└─────────────┘  └─────────────┘  └─────────────┘
```

---

## Technologievergleich

### 1. Apache Spark

| Kriterium | Beschreibung |
|-----------|--------------|
| **Architektur** | Cluster-basiert mit Driver (Master) und Executors (Workers). Verwendet DAG (Directed Acyclic Graph) für Optimierung. |
| **Programmiersprachen** | Scala, Java, Python, R |
| **Datenverteilung** | RDDs (Resilient Distributed Datasets), DataFrames, Datasets. In-Memory Computing mit Partitionierung. |
| **Gemeinsamer Speicher** | Broadcast Variables, Accumulators. Kein echter shared memory - stattdessen immutable distributed data. |
| **Performance (Fokus)** | Batch Processing, ML, Graph Processing. Bis zu 100x schneller als Hadoop MapReduce durch In-Memory. |
| **Notifikation** | Event-basiert über SparkListener. Callbacks für Job/Stage/Task Completion. |

**Vorteile:**
- Exzellent für Big Data Analytics
- Unified API für Batch und Streaming
- Große Community und Ecosystem

**Nachteile:**
- Hoher Speicherbedarf
- Komplexe Konfiguration für optimale Performance

---

### 2. Akka (Actor Model)

| Kriterium | Beschreibung |
|-----------|--------------|
| **Architektur** | Actor-basiert. Jeder Actor ist eine eigenständige Einheit mit Mailbox. Hierarchische Supervision. |
| **Programmiersprachen** | Scala, Java |
| **Datenverteilung** | Message Passing zwischen Actors. Keine geteilten Daten - nur immutable Messages. |
| **Gemeinsamer Speicher** | Akka Cluster Distributed Data, Akka Persistence für Event Sourcing |
| **Performance (Fokus)** | Low-Latency, High-Throughput Messaging. Millionen von Actors pro JVM möglich. |
| **Notifikation** | Direkte Actor-to-Actor Kommunikation. Ask Pattern für Request/Response. Pub/Sub via EventStream. |

**Vorteile:**
- Leichtgewichtige Actors (ca. 300 Bytes)
- Location Transparency (lokal/remote transparent)
- Fault Tolerance durch Supervision
- Akka Cluster für verteilte Systeme

**Nachteile:**
- Steile Lernkurve
- JVM-beschränkt

---

### 3. Celery (Python)

| Kriterium | Beschreibung |
|-----------|--------------|
| **Architektur** | Task Queue basiert. Verwendet Message Broker (RabbitMQ, Redis). Workers holen Tasks aus Queue. |
| **Programmiersprachen** | Python (primär) |
| **Datenverteilung** | Task Serialization (JSON, Pickle). Result Backend für Ergebnisse. |
| **Gemeinsamer Speicher** | Über externe Backends (Redis, Database). Keine native shared memory. |
| **Performance (Fokus)** | Asynchrone Task Execution. Gut für I/O-bound Tasks. |
| **Notifikation** | Callbacks, Signals, Result Polling. Canvas für komplexe Workflows (chains, groups, chords). |

**Vorteile:**
- Einfache Integration in Python-Projekte
- Flexible Backend-Optionen
- Gute Monitoring Tools (Flower)

**Nachteile:**
- Python GIL limitiert CPU-bound Tasks
- Single Language

---

### 4. Ray

| Kriterium | Beschreibung |
|-----------|--------------|
| **Architektur** | Distributed Computing Framework mit Global Control Store (GCS). Driver + Workers + Object Store. |
| **Programmiersprachen** | Python (primär), Java, C++ |
| **Datenverteilung** | Plasma Object Store für Zero-Copy Shared Memory. Arrow Format für effiziente Serialisierung. |
| **Gemeinsamer Speicher** | Shared Memory Object Store. Objekte werden zwischen Workers geteilt ohne Kopieren. |
| **Performance (Fokus)** | ML/AI Workloads, Reinforcement Learning. Sub-Millisekunden Task Scheduling. |
| **Notifikation** | ray.wait() für Ergebnisse. Async Execution via Futures. Actor-basierte Kommunikation möglich. |

**Vorteile:**
- Exzellent für ML/AI
- Einfache Python API (@ray.remote)
- Effizientes Memory Management

**Nachteile:**
- Relativ neu (weniger Dokumentation)
- Primär Python-fokussiert

---

### 5. Apache Kafka + Kafka Streams

| Kriterium | Beschreibung |
|-----------|--------------|
| **Architektur** | Distributed Streaming Platform. Brokers, Producers, Consumers. Partitioned Log. |
| **Programmiersprachen** | Java, Scala (native), Clients für viele Sprachen |
| **Datenverteilung** | Topic Partitioning. Consumer Groups für parallele Verarbeitung. |
| **Gemeinsamer Speicher** | State Stores (RocksDB) für lokalen State. Global Tables für shared state. |
| **Performance (Fokus)** | High-Throughput Streaming. Millionen Messages/Sekunde. |
| **Notifikation** | Event-driven via Topics. Consumer Polling mit Backpressure. |

**Vorteile:**
- Extrem hoher Durchsatz
- Persistente Message Storage
- Exactly-once Semantics möglich

**Nachteile:**
- Komplex für einfache Use Cases
- Ops-Overhead

---

### 6. Dask (Python)

| Kriterium | Beschreibung |
|-----------|--------------|
| **Architektur** | Scheduler + Workers. Scheduler erstellt Task Graph, Workers führen aus. |
| **Programmiersprachen** | Python |
| **Datenverteilung** | Dask Arrays, DataFrames (ähnlich NumPy/Pandas). Lazy Evaluation. |
| **Gemeinsamer Speicher** | Distributed Memory via Scheduler. Workers können Daten austauschen. |
| **Performance (Fokus)** | Scientific Computing, Parallel Pandas. Skaliert von Laptop bis Cluster. |
| **Notifikation** | Futures, as_completed(), Progress Callbacks, Dashboard. |

**Vorteile:**
- NumPy/Pandas-kompatible API
- Einfacher Einstieg für Data Scientists
- Dynamisches Task Scheduling

**Nachteile:**
- Weniger geeignet für Low-Latency
- Python-only

---

## Vergleichsmatrix

| Feature | Spark | Akka | Celery | Ray | Kafka | Dask |
|---------|-------|------|--------|-----|-------|------|
| **Primärsprache** | Scala | Scala/Java | Python | Python | Java | Python |
| **Multi-Language** | ✅ | ❌ | ❌ | ✅ | ✅ | ❌ |
| **In-Memory** | ✅ | ✅ | ❌ | ✅ | ❌ | ✅ |
| **Streaming** | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ |
| **ML/AI Focus** | ✅ | ❌ | ❌ | ✅ | ❌ | ✅ |
| **Latency** | Medium | Low | Medium | Low | Medium | Medium |
| **Cloud Native** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Kubernetes** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Lernkurve** | Steil | Steil | Flach | Flach | Steil | Flach |

---

## Beispiel: Pi-Berechnung mit Monte-Carlo-Methode

Die Monte-Carlo-Methode zur Pi-Berechnung eignet sich hervorragend als Benchmark für verteilte Systeme, da sie:
- Perfekt parallelisierbar ist (embarrassingly parallel)
- Variablen Workload erlaubt
- Einfach zu implementieren ist

### Konzept

```
┌────────────────────────────────────────────────────────────┐
│                    Monte-Carlo Pi                          │
│                                                            │
│    1. Generiere zufällige Punkte (x,y) im Quadrat [0,1]   │
│    2. Prüfe ob Punkt im Viertelkreis: x² + y² ≤ 1        │
│    3. Pi ≈ 4 * (Punkte im Kreis / Gesamtpunkte)          │
│                                                            │
│         ┌─────────────┐                                   │
│         │  ·  ·  ·    │                                   │
│         │·   ╭───╮  · │  ← Viertelkreis                  │
│         │  · │   │·   │                                   │
│         │·   │   │  · │                                   │
│         │    ╰───╯    │                                   │
│         └─────────────┘                                   │
│                                                            │
└────────────────────────────────────────────────────────────┘
```

### Verteilungsarchitektur

```
┌─────────────────────────────────────────────────────────────┐
│                         MASTER                               │
│                                                              │
│  Input: 100.000.000 Samples                                 │
│  Split: 10 Workers × 10.000.000 Samples                     │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              Task Distribution                        │   │
│  │  [W1: 10M] [W2: 10M] [W3: 10M] ... [W10: 10M]        │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            │
    ┌───────────────────────┼───────────────────────┐
    ▼                       ▼                       ▼
┌─────────┐           ┌─────────┐           ┌─────────┐
│ Worker1 │           │ Worker2 │           │ WorkerN │
│         │           │         │           │         │
│ Samples │           │ Samples │           │ Samples │
│ 10M     │           │ 10M     │           │ 10M     │
│         │           │         │           │         │
│ Result: │           │ Result: │           │ Result: │
│ 7853981 │           │ 7854012 │           │ 7853999 │
│ (hits)  │           │ (hits)  │           │ (hits)  │
└─────────┘           └─────────┘           └─────────┘
    │                       │                       │
    └───────────────────────┼───────────────────────┘
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                     AGGREGATION                              │
│                                                              │
│  Total Hits: 78.539.816                                     │
│  Total Samples: 100.000.000                                 │
│  Pi ≈ 4 × (78539816 / 100000000) = 3.14159264              │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## Implementierungsbeispiele

### Akka Typed Actors (Java) - Master/Worker Pattern

Das vollstaendige Beispiel befindet sich im Verzeichnis `akka-example/`.

#### Message-Protokoll

```java
// PiMessages.java - Immutable Messages fuer Actor-Kommunikation
public class PiMessages {

    // Befehl an den Master, die Berechnung zu starten
    public static final class StartCalculation implements MasterCommand {
        public final long totalSamples;
        public final int workerCount;
        public final ActorRef<CalculationResult> replyTo;
    }

    // Arbeitsauftrag an einen Worker
    public static final class CalculateChunk implements WorkerCommand {
        public final int workerId;
        public final long samples;
        public final ActorRef<MasterCommand> replyTo;
    }

    // Worker-Ergebnis zurueck an Master
    public static final class WorkerResult implements MasterCommand {
        public final int workerId;
        public final long samplesProcessed;
        public final long pointsInsideCircle;
    }
}
```

#### Worker Actor

```java
// PiWorker.java - Monte-Carlo Berechnung
public class PiWorker extends AbstractBehavior<PiMessages.WorkerCommand> {

    private final Random random = new Random();

    public static Behavior<PiMessages.WorkerCommand> create() {
        return Behaviors.setup(PiWorker::new);
    }

    private Behavior<PiMessages.WorkerCommand> onCalculateChunk(CalculateChunk msg) {
        long pointsInside = 0;

        // Monte-Carlo-Simulation
        for (long i = 0; i < msg.samples; i++) {
            double x = random.nextDouble();
            double y = random.nextDouble();
            if (x * x + y * y <= 1.0) {
                pointsInside++;
            }
        }

        // Ergebnis an Master senden
        msg.replyTo.tell(new WorkerResult(msg.workerId, msg.samples, pointsInside));
        return this;
    }
}
```

#### Master Actor

```java
// PiMaster.java - Koordination und Aggregation
public class PiMaster extends AbstractBehavior<PiMessages.MasterCommand> {

    private Behavior<MasterCommand> onStartCalculation(StartCalculation msg) {
        long samplesPerWorker = msg.totalSamples / msg.workerCount;

        // Workers erstellen und Arbeit verteilen
        for (int i = 0; i < msg.workerCount; i++) {
            ActorRef<WorkerCommand> worker = getContext().spawn(
                PiWorker.create(), "worker-" + i
            );
            worker.tell(new CalculateChunk(i, samplesPerWorker, getContext().getSelf()));
        }
        return this;
    }

    private Behavior<MasterCommand> onWorkerResult(WorkerResult msg) {
        totalPointsInside += msg.pointsInsideCircle;
        receivedResults++;

        if (receivedResults >= expectedResults) {
            // Pi = 4 * (Punkte im Kreis / Gesamtpunkte)
            double piEstimate = 4.0 * totalPointsInside / totalSamples;
            resultReceiver.tell(new CalculationResult(piEstimate, ...));
        }
        return this;
    }
}
```

### Python Multiprocessing - Master/Worker Pattern

Das Benchmark-Skript befindet sich in `python-benchmark/pi_benchmark.py`.

```python
# Master/Worker Pattern mit multiprocessing Pool
def monte_carlo_chunk(args):
    """Worker-Funktion: Berechnet Monte-Carlo-Samples."""
    worker_id, samples = args
    points_inside = 0

    for _ in range(samples):
        x = random.random()
        y = random.random()
        if x * x + y * y <= 1.0:
            points_inside += 1

    return worker_id, points_inside

def calculate_pi_multiprocessing(total_samples, worker_count):
    """Master: Verteilt Arbeit und aggregiert Ergebnisse."""
    samples_per_worker = total_samples // worker_count
    work_packages = [(i, samples_per_worker) for i in range(worker_count)]

    # Workers ausfuehren
    with multiprocessing.Pool(processes=worker_count) as pool:
        results = pool.map(monte_carlo_chunk, work_packages)

    # Ergebnisse aggregieren
    total_inside = sum(r[1] for r in results)
    pi_estimate = 4.0 * total_inside / total_samples

    return pi_estimate
```

### Verzeichnisse:
- `akka-example/` - Vollstaendige Akka Actor-Implementierung (Java)
- `python-benchmark/` - Python Benchmark mit Multiprocessing

---

## Benchmark-Ergebnisse

Die Benchmarks wurden mit folgenden Parametern durchgefuehrt:
- **Hardware:** Windows 10, 8 CPU Cores
- **Python:** 3.11.4
- **Java:** 22.0.2

### Kleine Workloads (1.000.000 Samples)

Bei kleinen Workloads ueberwiegt der Overhead der Prozesserstellung:

| Methode | Workers | Zeit (ms) | Throughput (M/s) | Speedup | Pi Estimate |
|---------|---------|-----------|------------------|---------|-------------|
| Sequential | 1 | 128.15 | 7.80 | 1.00x | 3.1422720000 |
| Multiprocessing | 4 | 163.52 | 6.12 | 0.78x | 3.1404760000 |
| ThreadPool | 4 | 143.82 | 6.95 | 0.89x | 3.1393800000 |

### Mittlere Workloads (10.000.000 Samples)

| Methode | Workers | Zeit (ms) | Throughput (M/s) | Speedup | Pi Estimate |
|---------|---------|-----------|------------------|---------|-------------|
| Sequential | 1 | 1296.85 | 7.71 | 1.00x | 3.1412540000 |
| Multiprocessing | 2 | 772.95 | 12.94 | 1.68x | 3.1411432000 |
| Multiprocessing | 4 | 479.13 | 20.87 | 2.71x | 3.1416896000 |
| Multiprocessing | 8 | 416.55 | 24.01 | 3.11x | 3.1407544000 |
| ThreadPool | 8 | 1483.98 | 6.74 | 0.87x | 3.1413656000 |

### Grosse Workloads (100.000.000 Samples)

| Methode | Workers | Zeit (ms) | Throughput (M/s) | Speedup | Pi Estimate |
|---------|---------|-----------|------------------|---------|-------------|
| Sequential | 1 | 13303.96 | 7.52 | 1.00x | 3.1416137200 |
| Multiprocessing | 2 | 7046.50 | 14.19 | 1.89x | 3.1413084800 |
| Multiprocessing | 4 | 3865.27 | 25.87 | 3.44x | 3.1415241200 |
| **Multiprocessing** | **8** | **2522.94** | **39.64** | **5.27x** | 3.1414597600 |
| ThreadPool | 8 | 14945.52 | 6.69 | 0.89x | 3.1416076400 |
| ProcessPool | 8 | 2638.40 | 37.90 | 5.04x | 3.1412846000 |

### Erkenntnisse

1. **Speedup skaliert mit Worker-Anzahl** bis zur CPU-Kern-Grenze
2. **ThreadPool ist durch Python GIL limitiert** fuer CPU-intensive Aufgaben
3. **Overhead bei kleinen Workloads** - Parallelisierung lohnt sich erst ab ~10M Samples
4. **Multiprocessing erreicht ~5.3x Speedup** bei 8 Workers auf 8 Kernen

---

## Cloud Deployment Optionen

### Kubernetes Deployment

```yaml
# Beispiel für Akka Cluster auf Kubernetes
apiVersion: apps/v1
kind: Deployment
metadata:
  name: akka-cluster
spec:
  replicas: 3
  selector:
    matchLabels:
      app: akka-worker
  template:
    metadata:
      labels:
        app: akka-worker
    spec:
      containers:
      - name: akka-worker
        image: akka-pi-calculator:latest
        ports:
        - containerPort: 2551
        env:
        - name: CLUSTER_IP
          valueFrom:
            fieldRef:
              fieldPath: status.podIP
```

### Docker Compose (lokales Testing)

```yaml
version: '3.8'
services:
  master:
    build: ./master
    ports:
      - "8080:8080"
    environment:
      - WORKER_COUNT=4
  
  worker:
    build: ./worker
    deploy:
      replicas: 4
    depends_on:
      - master
```

---

## Fazit

| Use Case | Empfohlene Technologie |
|----------|----------------------|
| Big Data Analytics | Apache Spark |
| Low-Latency Messaging | Akka |
| Python Async Tasks | Celery |
| ML/AI Workloads | Ray |
| Event Streaming | Kafka |
| Scientific Computing | Dask |

### Warum Akka fuer Master/Worker?

Akka eignet sich besonders gut fuer das Master/Worker Pattern:

1. **Location Transparency** - Workers koennen auf verschiedenen Servern laufen
2. **Fault Tolerance** - Supervision Hierarchies ermoeglichen automatische Recovery
3. **Message-basierte Kommunikation** - Keine geteilten Zustaende, keine Race Conditions
4. **Cloud-native Deployment** - Akka Cluster fuer Kubernetes/Docker

### Benchmark-Zusammenfassung

```
+======================================================================+
|                          BENCHMARK RESULTS                           |
+======================================================================+
| Configuration: 8 CPU Cores, 100M Samples                             |
+----------------------------------------------------------------------+
| Method          | Workers | Time (s) | Speedup | Throughput (M/s)   |
|-----------------|---------|----------|---------|---------------------|
| Sequential      |    1    |  13.30   |  1.00x  |        7.52         |
| Multiprocessing |    8    |   2.52   |  5.27x  |       39.64         |
+======================================================================+
```

### Projektstruktur

```
distributed-task-architecture/
|-- akka-example/                    # Java/Akka Implementierung
|   |-- src/main/java/com/example/pi/
|   |   |-- PiMessages.java          # Message-Protokoll
|   |   |-- PiWorker.java            # Worker Actor
|   |   |-- PiMaster.java            # Master Actor
|   |   |-- PiCalculatorApp.java     # Hauptanwendung
|   |   |-- BenchmarkRunner.java     # Benchmark
|   |-- pom.xml                      # Maven Konfiguration
|
|-- python-benchmark/                # Python Implementierung
|   |-- pi_benchmark.py              # Benchmark mit Multiprocessing
|
|-- distributed-computing-comparison.md  # Diese Dokumentation
```

### Ausfuehrung

**Python Benchmark:**
```bash
cd python-benchmark
python pi_benchmark.py
```

**Akka (mit Maven):**
```bash
cd akka-example
mvn compile exec:java -Dexec.mainClass="com.example.pi.PiCalculatorApp"
```

