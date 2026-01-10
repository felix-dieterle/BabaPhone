# BabaPhone Backend - Quick Start Guide

## Schnellstart für lokale Entwicklung

### 1. PHP-Server starten

```bash
cd backend/babyphone
php -S localhost:8080
```

### 2. API testen

Öffnen Sie http://localhost:8080 im Browser, um die API-Dokumentation zu sehen.

### 3. Geräte registrieren (mit curl)

**Kind-Gerät registrieren:**
```bash
curl -X POST http://localhost:8080/api/register.php \
  -H "Content-Type: application/json" \
  -d '{
    "device_id": "child-device-123",
    "device_type": "child",
    "device_name": "Baby Phone"
  }'
```

**Eltern-Gerät registrieren:**
```bash
curl -X POST http://localhost:8080/api/register.php \
  -H "Content-Type: application/json" \
  -d '{
    "device_id": "parent-device-456",
    "device_type": "parent",
    "device_name": "Parent Phone"
  }'
```

### 4. Verfügbare Geräte anzeigen

```bash
curl http://localhost:8080/api/discover.php?device_type=child
```

### 5. In der App konfigurieren

1. Öffnen Sie die BabaPhone App
2. Gehen Sie zu Einstellungen (⚙)
3. Aktivieren Sie "Mobile Daten-Modus aktivieren"
4. Geben Sie die Backend-URL ein: `http://YOUR-IP:8080`
   - Für lokale Tests: Verwenden Sie die IP-Adresse Ihres Computers im lokalen Netzwerk
   - Beispiel: `http://192.168.1.100:8080`
   - **Produktiv:** `http://your-server.com/babyphone` (wenn unter `/var/www/html/babyphone` deployed)
5. Speichern Sie die Einstellungen

**Wichtig:** Für lokale Tests müssen sich alle Geräte im gleichen Netzwerk befinden.

## Produktions-Deployment

Die Backend-Struktur `backend/babyphone/` ermöglicht es, mehrere Apps auf demselben Server zu platzieren:
- `/var/www/html/babyphone/` - BabaPhone Backend
- `/var/www/html/other-app/` - Andere Anwendung

Siehe [Backend README](README.md) für detaillierte Deployment-Anweisungen auf Apache/Nginx mit HTTPS.

## API-Endpunkte Übersicht

| Methode | Endpunkt | Beschreibung |
|---------|----------|--------------|
| POST | `/api/register.php` | Gerät registrieren |
| PUT | `/api/register.php` | Heartbeat senden |
| DELETE | `/api/register.php` | Gerät abmelden |
| GET | `/api/discover.php` | Geräte suchen |
| POST | `/api/signal.php` | Signal senden |
| GET | `/api/signal.php` | Signale abrufen |
| POST | `/api/relay.php` | Audio senden |
| GET | `/api/relay.php` | Audio empfangen |

## Fehlersuche

### "Connection refused"
- Stellen Sie sicher, dass der PHP-Server läuft
- Überprüfen Sie die Firewall-Einstellungen
- Verwenden Sie die richtige IP-Adresse (nicht localhost in der App)

### "data/ directory not writable"
```bash
chmod 750 babyphone/data
chown www-data:www-data babyphone/data  # Auf Linux/Apache
```

### CORS-Fehler
- Überprüfen Sie die ALLOWED_ORIGINS in `config/config.php`
- Stellen Sie sicher, dass CORS-Header gesetzt sind

## Weitere Hilfe

Siehe die vollständige [Backend-Dokumentation](README.md) für:
- Erweiterte Konfiguration
- Sicherheitseinstellungen
- Produktions-Deployment
- Datenbank-Integration
