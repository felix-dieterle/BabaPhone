# Hotspot und Mobile Daten Modus - Konzept

## Übersicht

Dieses Dokument beschreibt das Konzept für drei Verbindungsmodi in der BabaPhone App:
1. **WiFi-Modus** (Standard)
2. **Hotspot-Modus** (Automatisch)
3. **Mobile Daten-Modus** (Zukünftig mit Backend)

## 1. WiFi-Modus (Standard)

### Funktionsweise
- Beide Geräte sind mit demselben WLAN-Netzwerk verbunden
- Automatische Geräteerkennung über NSD (Network Service Discovery)
- Direkte TCP/IP-Verbindung zwischen den Geräten

### Wann wird es verwendet?
- Standard-Szenario zu Hause
- Beide Geräte haben Zugriff auf dasselbe WLAN

## 2. Hotspot-Modus (Automatisch)

### Funktionsweise

#### Szenario A: Kind-Gerät erstellt Hotspot
1. **Automatische Erkennung**: Kind-Gerät erkennt, dass keine WLAN-Verbindung verfügbar ist
2. **Hotspot-Erstellung**: Kind-Gerät erstellt automatisch einen mobilen Hotspot
   - SSID: "BabaPhone-[GeräteName]"
   - Passwort: Wird in der UI angezeigt
3. **Service-Registrierung**: Registriert sich über den Hotspot für NSD
4. **Eltern-Verbindung**: Eltern-Gerät verbindet sich manuell mit dem Hotspot
5. **Automatische Erkennung**: Nach Verbindung funktioniert NSD wie im WiFi-Modus

#### Szenario B: Eltern-Gerät erstellt Hotspot
1. **Manuelle Aktivierung**: Eltern-Gerät kann manuell einen Hotspot erstellen
2. **Kind-Verbindung**: Kind-Gerät verbindet sich mit dem Hotspot
3. **Standard-Betrieb**: Funktioniert dann wie WiFi-Modus

### Wann wird es automatisch aktiviert?
- **Kind-Modus**: Automatisch wenn:
  - Kein WLAN verfügbar ist
  - Benutzer startet "Monitoring"
  - Hotspot-Modus nicht deaktiviert wurde

### UI/UX
- Anzeige des Hotspot-Status
- Anzeige von SSID und Passwort
- Option zum manuellen Aktivieren/Deaktivieren
- Warnung über Batterieverbrauch

### Technische Details
- Verwendet `WifiManager.LocalOnlyHotspotConfiguration` (API 26+)
- Oder `WifiManager.startLocalOnlyHotspot()` für neuere Versionen
- Fallback auf ältere APIs für API < 26

## 3. Mobile Daten-Modus (Implementiert) ✅

### Funktionsweise
1. **Signaling-Server**: Backend-Server vermittelt Verbindungen (PHP-basiert)
2. **Geräte-Registrierung**: Beide Geräte registrieren sich beim Server über HTTPS
3. **Geräteerkennung**: Server stellt Listen verfügbarer Geräte bereit
4. **Signaling-Protokoll**: Server vermittelt Verbindungsinformationen zwischen Geräten
5. **Audio-Relay**: Server kann als Relay dienen, wenn P2P nicht möglich

### Wann wird es verwendet?
- Geräte sind an verschiedenen Standorten
- Keine gemeinsame WLAN-Verfügbarkeit
- Beide Geräte haben Internetverbindung (WiFi oder mobile Daten)
- Mobile Daten-Modus in den Einstellungen aktiviert

### Implementierte Features
- ✅ PHP Backend-Server mit REST API
- ✅ Geräte-Registrierung und Heartbeat
- ✅ Device Discovery durch Backend
- ✅ Signaling für Verbindungsaufbau
- ✅ Audio-Relay als Fallback
- ✅ MobileDataManager in Android App
- ✅ Einstellungen-UI für Backend-URL
- ✅ Automatische Bereinigung alter Daten

### UI/UX
- ✅ Aktivierung in Einstellungen
- ✅ Backend-URL-Konfiguration
- ✅ Hinweis auf Datenverbrauch (in Settings-Beschreibung)
- Server-Verbindungsstatus (kann noch erweitert werden)

## Automatische Modus-Auswahl

### Priorität (von hoch nach niedrig)
1. **WiFi-Modus**: Wenn beide Geräte im selben WLAN sind
2. **Hotspot-Modus**: Wenn kein WLAN verfügbar (nur Kind-Gerät)
3. **Mobile Daten-Modus**: Als manuelle Fallback-Option

### Entscheidungslogik

```
START Monitoring
  |
  ├─> WiFi verfügbar?
  |     ├─> JA: WiFi-Modus verwenden
  |     └─> NEIN: ↓
  |
  ├─> Kind-Modus?
  |     ├─> JA: Hotspot automatisch erstellen
  |     |       └─> UI zeigt Hotspot-Infos
  |     └─> NEIN (Eltern-Modus): 
  |           └─> UI zeigt Anleitung zum Verbinden
  |
  └─> (Zukünftig) Mobile Daten-Option in UI
```

## Berechtigungen

### Zusätzlich benötigte Berechtigungen für Hotspot
- `ACCESS_FINE_LOCATION` (bereits vorhanden)
- `CHANGE_WIFI_STATE` (bereits vorhanden)
- `CHANGE_NETWORK_STATE` (bereits vorhanden)
- Für API 33+: `NEARBY_WIFI_DEVICES` (bereits vorhanden)

### Zusätzlich für Mobile Daten
- Keine zusätzlichen Android-Berechtigungen
- Benötigt nur INTERNET (bereits vorhanden)

## Sicherheitsüberlegungen

### Hotspot-Modus
- Automatisch generiertes, sicheres Passwort (WPA2/WPA3)
- Hotspot nur während des Monitorings aktiv
- Hotspot automatisch beenden bei Stop

### Mobile Daten-Modus
- Ende-zu-Ende-Verschlüsselung erforderlich
- Server-Authentifizierung
- Keine Speicherung von Audio auf Server

## Implementierungsphasen

### Phase 1: Hotspot-Modus (Abgeschlossen) ✅
- [x] ConnectionManager für Netzwerkzustandserkennung
- [x] HotspotManager für Hotspot-Steuerung
- [x] Automatische Hotspot-Erstellung im Kind-Modus
- [x] UI für Hotspot-Status und -Informationen
- [x] Integration mit bestehendem NSD

### Phase 2: Mobile Daten-Modus (Abgeschlossen) ✅
- [x] Backend-Server-Implementierung (PHP)
- [x] REST API für Registrierung und Discovery
- [x] Signaling-Protokoll über HTTP
- [x] Audio-Relay-Fallback
- [x] MobileDataManager in Android App
- [x] UI für Backend-Konfiguration
- [x] Settings-Integration

### Phase 3: Optimierungen (Optional/Zukünftig)
- [ ] WebSocket für Echtzeit-Signaling (statt HTTP-Polling)
- [ ] STUN/TURN-Server für besseres NAT-Traversal
- [ ] Ende-zu-Ende-Verschlüsselung für Audio
- [ ] Optimierung des Audio-Relay für niedrigere Latenz
- [ ] Erweiterte Verbindungsstatistiken in UI

## Benutzererfahrung

### Kind-Gerät beim Baby
1. App öffnen
2. "Kind-Modus" wählen
3. "Start Monitoring" drücken
4. **Wenn kein WiFi**: 
   - App zeigt: "Hotspot wird erstellt..."
   - Zeigt SSID und Passwort an
   - Hinweis: "Verbinden Sie das Eltern-Gerät mit diesem Hotspot"
5. **Wenn WiFi vorhanden**: 
   - Normale Registrierung

### Eltern-Gerät
1. App öffnen
2. "Eltern-Modus" wählen
3. **Wenn Kind-Gerät Hotspot erstellt hat**:
   - Manuell mit Hotspot verbinden (Einstellungen → WiFi)
   - Zurück zur App
   - Kind-Gerät erscheint automatisch in Liste
4. **Wenn beide im WiFi**:
   - Kind-Gerät erscheint automatisch
5. Kind-Gerät auswählen
6. "Start Monitoring" drücken

## Zusammenfassung

Diese Implementierung bietet:
- ✅ **Flexibilität**: Funktioniert mit oder ohne WiFi
- ✅ **Automatisierung**: Kind-Gerät erstellt automatisch Hotspot wenn nötig
- ✅ **Einfachheit**: Minimale Benutzerinteraktion erforderlich
- ✅ **Erweiterbarkeit**: Vorbereitet für zukünftigen Mobile Daten-Modus
- ✅ **Benutzerfreundlichkeit**: Klare Anweisungen und Status-Anzeigen
