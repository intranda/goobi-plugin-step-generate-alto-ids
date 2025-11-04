---
title: Generate ALTO IDs
identifier: intranda_step_generate_alto_ids
description: Dieses Step Plugin dient zur Generierung von fehlenden ALTO-IDs.
published: true
keywords:
    - Goobi workflow
    - Plugin
    - Step Plugin
---

## Einführung
In dieser Dokumentation wird das Plugin zur Erzeugung fehlender ALTO-IDs erläutert. Dies ist erforderlich, damit der ALTO-Editor richtig funktioniert. Einige externe OCR-Tools stellen diese ALTO-IDs nicht zur Verfügung. Dieses Plugin kann dann verwendet werden, um sie nachträglich zu erzeugen.

## Installation
Um das Plugin nutzen zu können, müssen folgende Dateien installiert werden:

```bash
/opt/digiverso/goobi/plugins/step/plugin-step-generate-alto-ids-base.jar
```

Nach der Installation des Plugins kann dieses innerhalb des Workflows für die jeweiligen Arbeitsschritte ausgewählt und somit automatisch ausgeführt werden. Ein Workflow könnte dabei beispielhaft wie folgt aussehen:

![Beispielhafter Aufbau eines Workflows](screen1_de.png)

Für die Verwendung des Plugins muss dieses in einem Arbeitsschritt ausgewählt sein:

![Konfiguration des Arbeitsschritts für die Nutzung des Plugins](screen2_de.png)


## Überblick und Funktionsweise
Beim Starten des Plugins werden alle ALTO Dateien auf fehlende IDs geprüft.
Sollten fehlende IDs gefunden werden, wird zuerst ein Backup aller OCR Ergebnisse mitsamt der ALTO Dateien erstellt.
Danach werden die fehlenden ALTO IDs in allen Dateien ergänzt.


## Konfiguration
Dieses Plugin erfordert keine Konfiguration.
