# BabaPhone

Eine einfache Android Babyphone-App mit folgenden Funktionen:

**Package Name:** `de.felixdieterle.babaphone`

## Features

- **WLAN-Unterstützung** (Standard): Direkte Verbindung zwischen Geräten über WiFi ✅
- **Automatischer Hotspot**: Automatische Hotspot-Erstellung im Kind-Modus wenn kein WLAN verfügbar ✅
- **Verbindungserkennung**: Automatische Erkennung des besten Verbindungsmodus ✅
- **Mobile Daten**: Unterstützung für Verbindung über mobile Daten (mit Backend) ✅ *Neu implementiert*
- **Mehrere Kindgeräte**: Unterstützt mehrere Baby-Einheiten gleichzeitig ✅
- **Standard Babyphone-Funktionalität**: Audio-Überwachung ohne Kamera ✅
- **Eltern- und Kind-Modus**: Wählen Sie, ob das Gerät als Empfänger (Eltern) oder Sender (Kind) fungiert ✅
- **Visuelle Modi-Unterscheidung**: Verschiedene Symbole für Kind-Modus (📱👶) und Eltern-Modus (📱👨‍👩‍👧) ✅
- **Einstellungen-Menü**: Einfacher Zugriff auf Empfindlichkeit und Lautstärke über das Menü ✅
- **Persistente Einstellungen**: Einstellungen werden automatisch gespeichert und beim nächsten Start wiederhergestellt ✅

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
4. **Einstellungen anpassen** (optional):
   - Tippen Sie auf das Einstellungen-Symbol (⚙) in der Menüleiste
   - Passen Sie die Empfindlichkeit an (wie leicht soll Audio übertragen werden)
   - Passen Sie die Lautstärke an (wie laut soll das Audio abgespielt werden)
   - Einstellungen werden automatisch gespeichert

### Funktionsweise

- **Automatische Geräteerkennung**: Die App verwendet Network Service Discovery (NSD/mDNS), um Geräte im gleichen WLAN-Netzwerk automatisch zu finden
- **Geräte-Identifikation**: Jedes Kind-Gerät wird mit seinem Gerätenamen identifiziert (z.B. "Samsung Galaxy S21")
- **Audio-Streaming**: Wenn der Geräuschpegel die eingestellte Empfindlichkeit überschreitet, wird das Audio über TCP/IP an das Eltern-Gerät gestreamt
- **Live-Feedback**: Im Kind-Modus sehen Sie einen visuellen Indikator für den aktuellen Geräuschpegel

### Wichtige Hinweise

- Beide Geräte müssen im **gleichen WLAN-Netzwerk** verbunden sein
- Das Kind-Gerät muss **zuerst** gestartet werden, damit es vom Eltern-Gerät gefunden werden kann
- Die Audio-Übertragung erfolgt nur, wenn der Geräuschpegel die eingestellte Empfindlichkeit überschreitet

### Verbindungsmodi

**Aktuell verfügbar:**
- **WLAN/WiFi**: Beide Geräte im gleichen Netzwerk (Standard) ✅
- **Mobiler Hotspot**: Automatische Erstellung eines Hotspots wenn kein WLAN verfügbar ist ✅
  - **Automatisch im Kind-Modus**: Wenn das Kind-Gerät kein WLAN findet, erstellt es automatisch einen Hotspot
  - **API 26+ erforderlich**: Hotspot-Modus funktioniert ab Android 8.0 (Oreo)
  - **Einfache Verbindung**: SSID und Passwort werden in der App angezeigt
- **Mobile Daten Modus**: Verbindung über mobile Daten mit Backend-Server ✅ **Neu!**
  - **Backend erforderlich**: Benötigt einen PHP-Backend-Server (siehe `backend/babyphone/` Verzeichnis)
  - **Signaling und Relay**: Der Server vermittelt Verbindungen und kann als Audio-Relay dienen
  - **Einstellungen**: Aktivieren Sie den Modus in den App-Einstellungen und konfigurieren Sie die Backend-URL
  - **Mehrere Apps möglich**: Die Backend-Struktur ermöglicht das Hosting mehrerer Apps auf einem Server

### Mobile Daten Modus einrichten

1. **Backend-Server bereitstellen**:
   - Siehe [Backend README](backend/README.md) für Installations- und Deployment-Anweisungen
   - Hosting auf einem Server mit PHP-Unterstützung erforderlich
   - HTTPS wird für Produktionsumgebungen dringend empfohlen
   - Deploy nach `/var/www/html/babyphone/` für Produktion

2. **App konfigurieren**:
   - Öffnen Sie die Einstellungen in der App (⚙ Symbol)
   - Aktivieren Sie "Mobile Daten-Modus aktivieren"
   - Geben Sie die Backend-Server-URL ein:
     - Lokal: `http://192.168.1.100:8080` (IP Ihres Computers)
     - Produktiv: `https://ihr-server.de/babyphone`
   - Speichern Sie die Einstellungen

3. **Verbindung herstellen**:
   - Beide Geräte müssen mit dem Internet verbunden sein (WiFi oder mobile Daten)
   - Das Kind-Gerät registriert sich automatisch beim Backend
   - Das Eltern-Gerät findet das Kind-Gerät über den Backend-Server
   - Verbindung läuft primär über direkte P2P, mit Server-Relay als Fallback

### Wie funktioniert der Hotspot-Modus?

Der Hotspot-Modus wird **automatisch** aktiviert, wenn:
1. Sie den **Kind-Modus** auswählen
2. **Kein WLAN verfügbar** ist
3. Sie "Start Monitoring" drücken

**Prozess:**
1. **Kind-Gerät** (beim Baby):
   - Erkennt automatisch, dass kein WLAN vorhanden ist
   - Erstellt einen mobilen Hotspot mit Namen "BabaPhone-[Gerätename]"
   - Zeigt SSID und Passwort in der App an
   - Wartet auf Verbindung des Eltern-Geräts

2. **Eltern-Gerät**:
   - Manuell mit dem angezeigten Hotspot verbinden (in den Geräte-Einstellungen)
   - Zurück zur BabaPhone App wechseln
   - "Eltern-Modus" wählen
   - Kind-Gerät erscheint automatisch in der Liste
   - "Start Monitoring" drücken

**Hinweise:**
- Der Hotspot wird automatisch beendet, wenn die Überwachung gestoppt wird
- Hotspot-Modus kann den Akku schneller entleeren
- Funktioniert nur auf Android 8.0 (API 26) oder höher

Weitere Details finden Sie in der [Hotspot-Konzept-Dokumentation](HOTSPOT_CONCEPT.md).

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

**Umfassende Test-Suite:**
```bash
# Android Unit-Tests
./gradlew test

# Android Tests mit Coverage
./gradlew test jacocoTestReport

# Backend Tests
cd backend && composer test

# Alle Tests
./gradlew test && cd backend && composer test
```

**Weitere Informationen:**
- [Vollständige Test-Strategie](TESTING.md)
- [Tests ausführen](RUNNING_TESTS.md)
- [Test-Infrastruktur](TEST_INFRASTRUCTURE.md)

**Test-Abdeckung:**
- Android Unit-Tests: > 70% der Business-Logik
- Backend Unit-Tests: > 80% der API-Logik
- Integration Tests: Vollständige kritische Workflows

### Backend-Server (für Mobile Daten-Modus)

Siehe [Backend README](backend/README.md) für:
- Installationsanweisungen
- Deployment auf verschiedenen Servern (Apache, Nginx)
- Konfiguration und Sicherheit
- API-Dokumentation

## CI/CD

Das Projekt verwendet GitHub Actions für:

- **Continuous Integration**: 
  - Automatische Unit-Tests (Android & Backend) bei jedem Pull Request
  - Lint-Analyse für Code-Qualität
  - Code-Coverage-Berichte (Jacoco für Android, PHPUnit für Backend)
  - Integration-Tests für Backend-APIs
- **Automatische Releases**: Erstellung einer neuen Version bei jedem Merge in main
- **Test-Artefakte**: Upload von Test-Ergebnissen und Coverage-Berichten

Siehe `.github/workflows/android-ci.yml` für Details.

## Lizenz

MIT License

## Hinweis

Dies ist eine grundlegende Implementierung. Für Produktionsumgebungen sollten zusätzliche Features wie Ende-zu-Ende-Verschlüsselung, verbesserte Verbindungsstabilität und weitere Sicherheitsmaßnahmen implementiert werden.