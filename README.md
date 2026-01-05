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

### Schnellstart

1. Starten Sie die App auf beiden Geräten
2. **Auf dem Kind-Gerät** (beim Baby):
   - Wählen Sie "Kind-Modus"
   - Drücken Sie "Start Monitoring"
   - Das Gerät registriert sich automatisch im Netzwerk
   - Sie sehen einen Live-Audio-Level-Indikator
3. **Auf dem Eltern-Gerät**:
   - Wählen Sie "Eltern-Modus"
   - Warten Sie, bis das Kind-Gerät in der Liste erscheint
   - Tippen Sie auf das gewünschte Kind-Gerät, um es auszuwählen
   - Drücken Sie "Start Monitoring"
4. Passen Sie die Empfindlichkeit und Lautstärke nach Bedarf an

### Funktionsweise

- **Automatische Geräteerkennung**: Die App verwendet Network Service Discovery (NSD/mDNS), um Geräte im gleichen WLAN-Netzwerk automatisch zu finden
- **Geräte-Identifikation**: Jedes Kind-Gerät wird mit seinem Gerätenamen identifiziert (z.B. "Samsung Galaxy S21")
- **Audio-Streaming**: Wenn der Geräuschpegel die eingestellte Empfindlichkeit überschreitet, wird das Audio über TCP/IP an das Eltern-Gerät gestreamt
- **Live-Feedback**: Im Kind-Modus sehen Sie einen visuellen Indikator für den aktuellen Geräuschpegel

### Wichtige Hinweise

- Beide Geräte müssen im **gleichen WLAN-Netzwerk** verbunden sein
- Das Kind-Gerät muss **zuerst** gestartet werden, damit es vom Eltern-Gerät gefunden werden kann
- Die Audio-Übertragung erfolgt nur, wenn der Geräuschpegel die eingestellte Empfindlichkeit überschreitet

## Berechtigungen

Die App benötigt folgende Berechtigungen:

- **Mikrofon**: Zum Aufnehmen von Audio vom Baby
- **Netzwerk**: Zum Verbinden der Geräte
- **Benachrichtigungen**: Für den Vordergrund-Dienst während der Überwachung
- **Standort**: Benötigt von Android für WiFi Direct (wird nicht zur Ortung verwendet)

**Hinweis**: Die Standortberechtigung wird nur für WiFi Direct benötigt und nicht zur Verfolgung des Gerätestandorts verwendet. Die App verfolgt nicht Ihren Standort.

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
- **UI Screenshot Tests**: Automatische Screenshots der App aus verschiedenen Ansichten (Parent/Child Mode, Landscape/Portrait)
- **Automatische Releases**: Erstellung einer neuen Version bei jedem Merge in main

Weitere Details zu den Screenshot-Tests finden Sie in [SCREENSHOT_TESTING.md](SCREENSHOT_TESTING.md).

## Lizenz

MIT License

## Hinweis

Dies ist eine grundlegende Implementierung. Für Produktionsumgebungen sollten zusätzliche Features wie Ende-zu-Ende-Verschlüsselung, verbesserte Verbindungsstabilität und weitere Sicherheitsmaßnahmen implementiert werden.