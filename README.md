# Distributed Task Architecture

Implementierung des Master/Worker Patterns mit Akka Typed Actors und Apache Spark fuer verteilte Berechnungen.

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

## Warum Apache Spark?

Apache Spark ist ein verteiltes Datenverarbeitungs-Framework, das auf dem Master/Worker Pattern basiert:

**Spark Architektur:**
```
┌─────────────────────────────────────────────┐
│           SPARK MASTER (Driver)             │
│  - Resource Management                      │
│  - Task Scheduling                          │
│  - Job Coordination                         │
└─────────────────────────────────────────────┘
      │                │                │
      ▼                ▼                ▼
┌───────────┐    ┌───────────┐    ┌───────────┐
│  Worker 1 │    │  Worker 2 │    │  Worker 3 │
│ Executor  │    │ Executor  │    │ Executor  │
│  Tasks    │    │  Tasks    │    │  Tasks    │
└───────────┘    └───────────┘    └───────────┘
```

**Spark Vorteile:**
- In-Memory Processing (bis zu 100x schneller als Hadoop MapReduce)
- Lazy Evaluation - optimiert Ausfuehrungsplaene automatisch
- Resilient Distributed Datasets (RDDs) mit automatischer Fehlerbehandlung
- Unterstuezt Batch- und Stream-Processing
- Rich API fuer komplexe Datenverarbeitungen

**Use Cases:**
- Big Data Analytics (Petabyte-Scale)
- Machine Learning Pipelines
- Graph Processing
- Stream Processing (Spark Streaming)

## Projektstruktur

```
distributed-task-architecture-chladek-kural/
├── akka-example/
│   ├── src/main/java/com/example/pi/
│   │   ├── PiMessages.java      # Definiert Message-Typen (StartCalculation, WorkerResult, etc.)
│   │   ├── PiWorker.java        # Worker Actor - fuehrt Monte-Carlo Simulation durch
│   │   ├── PiMaster.java        # Master Actor - koordiniert Worker, aggregiert Ergebnisse
│   │   ├── PiCalculatorApp.java # Hauptanwendung - erstellt ActorSystem
│   │   └── BenchmarkRunner.java # Vergleicht Sequential vs. Akka mit 1/2/4/8 Workers
│   ├── pom.xml                  # Maven Dependencies (Akka 2.8.5)
│   ├── run.bat                  # Startet Pi-Berechnung
│   └── benchmark.bat            # Startet Benchmark
│
├── src/main/java/at/schule/spark/
│   └── SparkPI.java             # Spark Monte-Carlo Pi-Berechnung
│
├── build.gradle                 # Gradle Build-Konfiguration (Spark 3.5.0)
├── docker-compose.yaml          # Docker Setup fuer Spark Cluster
├── Dockerfile                   # Custom Spark Image mit Java 21
└── README.md                    # Diese Datei
```

## Apache Spark Setup

### Voraussetzungen
- Docker & Docker Compose
- Java 21 (JDK)
- Gradle

### Spark Cluster starten

Das Projekt verwendet ein Docker-basiertes Spark Cluster mit:
- 1 Spark Master (Port 8080 fuer Web UI, Port 7077 fuer Cluster Communication)
- 2 Spark Workers

**Cluster starten:**
```bash
docker-compose up -d
```

**Cluster Status pruefen:**
```bash
docker-compose ps
docker logs spark-master
```

**Spark Web UI oeffnen:**
```
http://localhost:8080
```

### SparkPI Anwendung ausfuehren

**1. JAR-Datei bauen:**
```bash
./gradlew clean build
```

**2. JAR in Container kopieren:**
```bash
docker cp build/libs/distributed-task-architecture-chladek-kural-1.0.jar spark-master:/opt/spark/work-jars/
```

**3. Spark Job submitten:**
```bash
docker exec spark-master /opt/spark/bin/spark-submit \
  --class at.schule.spark.SparkPI \
  --master spark://spark-master:7077 \
  /opt/spark/work-jars/distributed-task-architecture-chladek-kural-1.0.jar
```

**Erwartete Ausgabe:**
```
Pi ist etwa: 3.14159...
```

### Spark Cluster Management

**Worker hinzufuegen:**
```yaml
# In docker-compose.yaml
spark-worker-3:
  build: .
  container_name: spark-worker-3
  depends_on:
    - spark-master
  command: bash -c "sleep 5 && /opt/spark/bin/spark-class org.apache.spark.deploy.worker.Worker spark://spark-master:7077"
  volumes:
    - ./build/libs:/opt/spark/work-jars
```

**Cluster herunterfahren:**
```bash
docker-compose down
```

**Logs anzeigen:**
```bash
docker logs spark-master
docker logs spark-worker-1
docker logs spark-worker-2
```

### Troubleshooting

**Problem: "ClassNotFoundException: org.apache.spark.launcher.Main"**
- Loesung: Verwende Custom Dockerfile mit manueller Spark Installation

**Problem: "UnsupportedClassVersionError"**
- Loesung: Java-Version im Container muss mit Compile-Version uebereinstimmen (Java 21)

**Problem: "Local jar does not exist"**
- Loesung: Volume-Mapping pruefen oder JAR manuell mit `docker cp` kopieren

**Problem: Worker verbinden sich nicht mit Master**
- Loesung: Container-Netzwerk pruefen, `depends_on` in docker-compose verwenden

## Akka Example - Ausfuehrung

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

### Akka Actors
Monte-Carlo Pi-Berechnung mit 100M Samples auf Deniz HP Windows 10 Laptop:

| Workers | Zeit | Speedup | Throughput |
|---------|------|---------|------------|
| 1 (Sequential) | ~13s | 1.0x | 7.7 M/s |
| 2 Actors | ~7s | 1.9x | 14 M/s |
| 4 Actors | ~3.5s | 3.7x | 28 M/s |
| 8 Actors | ~2s | **5.3x** | 40 M/s |

### Apache Spark
Monte-Carlo Pi-Berechnung mit 1M Samples (10 Slices) aujf Simons Debian 13 Laptop:

| Setup | Zeit | Throughput |
|-------|------|------------|
| 1 Worker | ~4.2s | 27 M/s |
| 2 Workers | ~2.7s | 41 M/s |

*Fuehre eigene Benchmarks durch und trage Werte ein*

## Technologievergleich

| Technologie | Architektur | Sprachen | Haupteinsatz | Skalierung |
|-------------|-------------|----------|--------------|------------|
| **Akka** | Actor Model | Java, Scala | Low-Latency, Reactive Systems | Horizontal (Cluster) |
| **Apache Spark** | DAG-basiert | Scala, Java, Python | Big Data Analytics | Horizontal (YARN/K8s) |
| Ray | Distributed Runtime | Python | ML/AI Training | Horizontal (Auto-scaling) |
| Celery | Task Queue | Python | Async Web Tasks | Horizontal (Workers) |
| Kafka | Event Streaming | Java | High-Throughput Messaging | Horizontal (Partitions) |

### Wann welche Technologie?

**Akka:**
- Echtzeit-Anwendungen mit niedriger Latenz
- Komplexe Zustandsverwaltung
- Microservices mit Message-Passing
- IoT und Reactive Systems

**Apache Spark:**
- Batch-Verarbeitung von grossen Datenmengen (GB-PB)
- ETL Pipelines
- Machine Learning auf grossen Datasets
- Log-Analyse und Business Intelligence

**Hybrid-Ansaetze:**
- Akka fuer Orchestrierung + Spark fuer Datenverarbeitung
- Kafka fuer Event Streaming + Spark fuer Analytics

Details: siehe `distributed-computing-comparison.md`

## Technische Details

### Spark Konfiguration

**build.gradle:**
```gradle
dependencies {
    implementation 'org.apache.spark:spark-core_2.12:3.5.0'
    implementation 'org.apache.spark:spark-sql_2.12:3.5.0'
}

sourceCompatibility = '11'  // Java 11 oder 21
targetCompatibility = '11'
```

**Docker Image:**
- Base: Eclipse Temurin 21 JRE
- Spark: 3.5.0 mit Hadoop 3
- Installation: Manual download von Apache Archive

### Spark SparkPI Implementierung

Die `SparkPI.java` verwendet Monte-Carlo Simulation:

1. **Parallelisierung:** 1M Samples werden auf 10 Slices verteilt
2. **Map:** Jeder Worker generiert Random Points (x,y) in [-1,1]
3. **Filter:** Prueft ob Point im Einheitskreis liegt (x² + y² ≤ 1)
4. **Reduce:** Zaehlt Treffer und berechnet π ≈ 4 * (Treffer/Gesamt)

**Mathematischer Hintergrund:**
```
Flaeche Quadrat = 4
Flaeche Kreis = π
Verhaeltnis = π/4
→ π = 4 * (Punkte im Kreis / Gesamtpunkte)
```

## Quellen

[1] C. Hewitt, P. Bishop, and R. Steiger, "A universal modular ACTOR formalism for artificial intelligence," *IJCAI*, 1973.

[2] J. Dean and S. Ghemawat, "MapReduce: Simplified data processing on large clusters," *OSDI*, 2004.

[3] M. Zaharia et al., "Apache Spark: A unified engine for big data processing," *Commun. ACM*, vol. 59, no. 11, 2016.

[4] Lightbend, "Akka Documentation," 2024. [Online]. Available: https://doc.akka.io/

[5] Apache Software Foundation, "Apache Spark Documentation," 2024. [Online]. Available: https://spark.apache.org/docs/latest/

[6] M. Zaharia, "Resilient Distributed Datasets: A Fault-Tolerant Abstraction for In-Memory Cluster Computing," *NSDI*, 2012.

## Weiterführende Ressourcen

- **Spark Programming Guide:** https://spark.apache.org/docs/latest/rdd-programming-guide.html
- **Akka Typed Actors:** https://doc.akka.io/docs/akka/current/typed/index.html
- **Docker Compose Networking:** https://docs.docker.com/compose/networking/
- **Monte-Carlo Methods:** https://en.wikipedia.org/wiki/Monte_Carlo_method

## Autoren

Chladek, Kural

---

**Letzte Aktualisierung:** Januar 2026  
**Version:** 1.0  
**Lizenz:** Educational Use
