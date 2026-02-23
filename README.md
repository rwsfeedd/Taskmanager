Programm begann als Beleg mit dem Thema:"Entwicklung einer Desktopaufgabenmanagementsoftware für die Alltagsplanung mit Planungsalgorithmen" und wird aktuell überarbeitet um meinen Ansprüchen gerechter zu werden.
Gearbeitet wird in der IDE InteliJ IDEA mit Java und JavaFX. Zur Buildautomation wird gradle genutzt.  
___
Deployment:
---
Unter build/distributions sind fertige Builds der Applikation für Windows enthalten. Die Datei muss zuerst in einer Umgebung entpackt werden, wo der ausführende Nutzer Schreib- und Leserechte hat. Danach kann in dem bin-Verzeichnis die Windows-Batchdatei ausgeführt werden um das Programm zu starten.
___
Verwendung:
---
Der Benutzer kann durch Betätigen des jeweiligen Tabs zu den verschiedenen Fenstern gelangen.
Die Fenster sind: 
- Vergangenheit -> noch nicht implementiert
- Tagesliste -> Tagesaufgaben sehen, austragen, (fertigstellen)
- Zukunft -> noch nicht implementiert
- Aufgabenliste -> alle Aufgaben jeweils sehen, erstellen, bearbeiten oder löschen
- erstellen -> Aufgabe erstellen
- ändern -> in Aufgabenliste ausgewählte Aufgabe bearbeiten
___
Roadmap:
---
Aufgabenplanimplementierung für Vergangenheit und Zukunft, TableView statt ListView implementieren, History implementieren, Zukunftsaufgabenpläne implementieren, SQLite als Datenbank, Fehlerbehandlung optimieren, Tests implementieren

