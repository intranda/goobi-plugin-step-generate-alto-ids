---
title: Generate ALTO IDs
identifier: intranda_step_generate_alto_ids
description: Dieses Step Plugin dient zur Generierung von fehlenden ALTO-IDs.
published: true
---

## Einführung
Diese Dokumentation erläutert das Plugin zur Generierung von ALTO IDs.

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
