# BabaPhone Backend

Backend server für die BabaPhone Android App zur Unterstützung von Verbindungen über mobile Daten.

## Funktionen

- **Geräte-Registrierung**: Registrierung von Eltern- und Kind-Geräten
- **Geräteerkennung**: Erkennung verfügbarer Geräte
- **Signaling**: Vermittlung von Verbindungsinformationen zwischen Geräten
- **Audio-Relay**: Fallback-Relay für Audio-Streaming, wenn direkte P2P-Verbindung fehlschlägt
- **Automatische Bereinigung**: Entfernung inaktiver Geräte und alter Daten

## Anforderungen

- PHP 7.4 oder höher
- Webserver (Apache, Nginx, etc.)
- Schreibrechte für das `data/` Verzeichnis

## Installation

### 1. Lokale Entwicklung

```bash
cd backend
php -S localhost:8080
```

Öffnen Sie http://localhost:8080 im Browser, um die API-Dokumentation zu sehen.

### 2. Deployment auf einem Webserver

#### Apache

1. Kopieren Sie das `backend/` Verzeichnis auf Ihren Webserver:
```bash
scp -r backend/ user@server:/var/www/html/babaphone-backend/
```

2. Stellen Sie sicher, dass das `data/` Verzeichnis schreibbar ist:
```bash
ssh user@server
cd /var/www/html/babaphone-backend
mkdir -p data
chmod 750 data
chown www-data:www-data data
```

3. Konfigurieren Sie Apache (optional - `.htaccess` Datei):
```apache
# backend/.htaccess
<IfModule mod_rewrite.c>
    RewriteEngine On
    RewriteCond %{REQUEST_FILENAME} !-f
    RewriteCond %{REQUEST_FILENAME} !-d
    RewriteRule ^api/(.*)$ api/$1.php [L]
</IfModule>

# Schützen Sie das data-Verzeichnis
<Directory "data">
    Require all denied
</Directory>
```

#### Nginx

Nginx-Konfiguration:

```nginx
server {
    listen 80;
    server_name babaphone.example.com;
    
    root /var/www/html/babaphone-backend;
    index index.php;
    
    location / {
        try_files $uri $uri/ /index.php?$query_string;
    }
    
    location /api/ {
        try_files $uri $uri.php =404;
    }
    
    location ~ \.php$ {
        fastcgi_pass unix:/var/run/php/php7.4-fpm.sock;
        fastcgi_index index.php;
        fastcgi_param SCRIPT_FILENAME $document_root$fastcgi_script_name;
        include fastcgi_params;
    }
    
    # Schützen Sie das data-Verzeichnis
    location /data/ {
        deny all;
    }
}
```

### 3. HTTPS aktivieren (Produktiv erforderlich)

Für Produktionsumgebungen **muss** HTTPS verwendet werden:

```bash
# Mit Let's Encrypt (certbot)
sudo apt install certbot python3-certbot-apache  # oder python3-certbot-nginx
sudo certbot --apache  # oder --nginx
```

Aktualisieren Sie dann `config/config.php`:
```php
define('REQUIRE_HTTPS', true);
```

## Konfiguration

Bearbeiten Sie `config/config.php` für:

- **Datenbank-Einstellungen** (optional - aktuell dateibasiert)
- **Server-Ports**
- **Timeout-Werte**
- **CORS-Einstellungen**
- **Sicherheitsoptionen**

### Umgebungsvariablen

Sie können auch Umgebungsvariablen verwenden:

```bash
export DB_HOST=localhost
export DB_NAME=babaphone
export SERVER_PORT=8080
export REQUIRE_HTTPS=true
```

## API-Endpunkte

### Geräte-Registrierung

**POST /api/register.php**
```json
{
  "device_id": "unique-device-id",
  "device_type": "parent|child",
  "device_name": "Gerätename"
}
```

**PUT /api/register.php** (Heartbeat)
```json
{
  "device_id": "unique-device-id"
}
```

**DELETE /api/register.php**
```json
{
  "device_id": "unique-device-id"
}
```

### Geräteerkennung

**GET /api/discover.php?device_type=child**

Optional: `device_type` (parent|child)

### Signaling

**POST /api/signal.php**
```json
{
  "from_device_id": "sender-id",
  "to_device_id": "receiver-id",
  "signal_type": "connect|disconnect|offer|answer",
  "data": {}
}
```

**GET /api/signal.php?device_id=your-device-id**

### Audio-Relay (Fallback)

**POST /api/relay.php**
```json
{
  "from_device_id": "sender-id",
  "to_device_id": "receiver-id",
  "audio_data": "base64-encoded-audio"
}
```

**GET /api/relay.php?device_id=your-device-id**

## Wartung

### Automatische Bereinigung

Richten Sie einen Cron-Job ein, um alte Daten zu bereinigen:

```bash
# Bearbeiten Sie crontab
crontab -e

# Fügen Sie diese Zeile hinzu (alle 5 Minuten)
*/5 * * * * php /var/www/html/babaphone-backend/cleanup.php
```

### Manuelle Bereinigung

```bash
php cleanup.php
```

## Datenverzeichnis-Struktur

```
data/
├── devices/        # Registrierte Geräte
├── signals/        # Signaling-Nachrichten
├── audio/          # Audio-Pakete (temporär)
└── pairings/       # Gerätepaarungen
```

## Sicherheit

### Produktions-Checkliste

- [ ] HTTPS aktiviert
- [ ] `data/` Verzeichnis vor Webzugriff geschützt
- [ ] PHP `display_errors` auf `Off` setzen
- [ ] API-Key-Authentifizierung implementieren (optional)
- [ ] Rate-Limiting aktivieren
- [ ] Firewall-Regeln konfigurieren
- [ ] Regelmäßige Backups einrichten
- [ ] Logs überwachen

### Empfohlene PHP-Einstellungen

```ini
# php.ini
display_errors = Off
log_errors = On
error_log = /var/log/php/error.log
max_execution_time = 30
memory_limit = 128M
post_max_size = 10M
upload_max_filesize = 10M
```

## Troubleshooting

### Problem: "data/" Verzeichnis nicht beschreibbar

```bash
sudo chown -R www-data:www-data data/
sudo chmod 750 data/
```

### Problem: CORS-Fehler

Aktualisieren Sie `config/config.php`:
```php
define('ALLOWED_ORIGINS', 'https://your-app-domain.com');
```

### Problem: API gibt "500 Internal Server Error" zurück

Überprüfen Sie die PHP-Fehlerprotokolle:
```bash
tail -f /var/log/php/error.log  # oder
tail -f /var/log/apache2/error.log
```

## Leistungsoptimierung

Für höhere Last:

1. **Verwenden Sie eine echte Datenbank** (MySQL/PostgreSQL)
2. **Implementieren Sie Redis** für Session-Storage
3. **Aktivieren Sie PHP OpCache**
4. **Verwenden Sie einen CDN** für statische Assets
5. **Skalieren Sie horizontal** mit mehreren Backend-Servern

## Lizenz

MIT License - siehe Haupt-Repository
