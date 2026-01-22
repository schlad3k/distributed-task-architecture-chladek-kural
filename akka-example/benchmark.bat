@echo off
cd /d "%~dp0"
set MAVEN_HOME=C:\Program Files\JetBrains\IntelliJ IDEA 2023.3.4\plugins\maven\lib\maven3
"%MAVEN_HOME%\bin\mvn.cmd" exec:java -Dexec.mainClass="com.example.pi.BenchmarkRunner"
