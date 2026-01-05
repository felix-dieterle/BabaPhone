# BabaPhone

Eine einfache Android Babyphone-App mit folgenden Funktionen:

## Features

- **WLAN-Unterstützung** (Standard): Direkte Verbindung zwischen Geräten über WiFi
- **Mobiler Hotspot**: Verbindung über einen mobilen Hotspot
- **Mobile Daten**: Unterstützung für Verbindung über mobile Daten (mit Backend)
- **Mehrere Kindgeräte**: Unterstützt mehrere Baby-Einheiten gleichzeitig
- **Standard Babyphone-Funktionalität**: Audio-Überwachung ohne Kamera
- **Eltern- und Kind-Modus**: Wählen Sie, ob das Gerät als Empfänger (Eltern) oder Sender (Kind) fungiert

## Installation

1. Laden Sie die APK aus den [Releases](https://github.com/felix-dieterle/BabaPhone/releases) herunter
2. Installieren Sie die APK auf Ihren Android-Geräten
3. Erteilen Sie die erforderlichen Berechtigungen (Mikrofon, Netzwerk)

## Verwendung

1. Starten Sie die App auf beiden Geräten
2. Wählen Sie auf einem Gerät "Eltern-Modus" (Empfänger)
3. Wählen Sie auf dem anderen Gerät "Kind-Modus" (Sender)
4. Drücken Sie "Start Monitoring" auf beiden Geräten
5. Passen Sie die Empfindlichkeit und Lautstärke nach Bedarf an

## Berechtigungen

Die App benötigt folgende Berechtigungen:

- **Mikrofon**: Zum Aufnehmen von Audio vom Baby
- **Netzwerk**: Zum Verbinden der Geräte
- **Benachrichtigungen**: Für den Vordergrund-Dienst während der Überwachung

## Entwicklung

### Voraussetzungen

- Android Studio Arctic Fox oder neuer
- JDK 17
- Android SDK 34

### Build

```bash
./gradlew assembleDebug
```

### Tests

```bash
./gradlew test
```

## CI/CD

Das Projekt verwendet GitHub Actions für:

- **Continuous Integration**: Automatische Tests bei jedem Pull Request
- **Automatische Releases**: Erstellung einer neuen Version bei jedem Merge in main

## Lizenz

MIT License

## Hinweis

Dies ist eine grundlegende Implementierung. Für Produktionsumgebungen sollten zusätzliche Features wie Ende-zu-Ende-Verschlüsselung, verbesserte Verbindungsstabilität und weitere Sicherheitsmaßnahmen implementiert werden.