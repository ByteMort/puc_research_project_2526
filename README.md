# Handshake PoC – QR-gebaseerde aanwezigheidsregistratie

Live demo:
https://handshake-poc.myaddr.dev

> De live demo draait via een Amazon AWS Student-account. Deze moet eerst worden
> geactiveerd en is daarna slechts tijdelijk beschikbaar. Stuur ons gerust een
> bericht, dan zetten we de demo voor je online.

---

<img width="845" height="624" alt="genereren" src="https://github.com/user-attachments/assets/e91ceb07-9b3c-4f50-b3b3-80f10e0ae2de" />

---

<img width="844" height="761" alt="scannen" src="https://github.com/user-attachments/assets/36f74984-2914-4b55-8295-4656aaf791ce" />

---

## Wat demonstreert deze PoC?

Dit project is een proof of concept voor een QR-gebaseerd aanwezigheidsregistratiesysteem.
Het idee is dat een docent of begeleider een QR-code genereert voor een specifiek
contactmoment (bijvoorbeeld een hoorcollege of werkgroep). Een student scant die code
om zijn aanwezigheid te registreren.

Het systeem is gebouwd rond een paar concrete beveiligingsprincipes:

**Anonieme tokens**
De QR-code bevat alleen een willekeurig UUID-token. Er zit geen naam, e-mailadres
of user_id in de code zelf. Persoonsgegevens worden enkel server-side bijgehouden
en zijn alleen toegankelijk via RBAC.

**Tijdgebonden geldigheid (TTL)**
Elk token heeft een instelbare geldigheidsduur (1, 5 of 15 minuten). Na het
verlopen weigert de server het token automatisch.

**Replay-aanval beveiliging**
Een token kan maar één keer worden gebruikt. Als je hetzelfde token een tweede
keer probeert in te dienen, geeft de server een foutmelding terug. In de scanner-tab
zit zelfs een knop om dit expliciet te testen.

**GDPR / dataminimalisatie**
Verlopen tokens worden elk uur automatisch verwijderd door een scheduled task.
De scan-response bevat bewust geen persoonsgegevens, enkel het contactmoment-ID
en het label.

---

## Technische opbouw

Het project bestaat uit drie onderdelen die samenwerken via Docker Compose:

- **Backend** – Spring Boot 3 (Java 21), genereert tokens en valideert scans via
  een REST API. Tokens worden in-memory opgeslagen met een ConcurrentHashMap.
  QR-codes worden server-side gegenereerd met de ZXing library.
- **Frontend** – Vue 3 met Vite. Twee tabbladen: één om QR-codes te genereren
  met een live countdown timer, en één om tokens te scannen en de server-response
  te bekijken.
- **Caddy** – Reverse proxy die HTTPS afhandelt en requests doorstuurt naar de
  juiste service.

---

## Hoe uitvoeren?

### Lokaal met Docker

Zorg dat Docker Desktop geïnstalleerd is, daarna:

```bash
docker compose up
```

De app is dan bereikbaar op `http://localhost`.

Afsluiten:

```bash
docker compose down
```

### Online deployen op een Ubuntu server

Voer onderstaand script uit op een verse Ubuntu server om Docker te installeren:

```bash
#!/bin/bash
# Update system
sudo apt update
sudo apt upgrade -y
sudo snap refresh

# Docker Compose install
sudo apt install docker-compose-v2 -y
sudo usermod -aG docker ubuntu

# Docker IPv6 configuration
sudo mkdir -p /etc/docker
echo '{"ipv6": true, "fixed-cidr-v6": "fd00::/80"}' | sudo tee /etc/docker/daemon.json > /dev/null
sudo systemctl restart docker
```

Deployment naar de server gebeurt automatisch via GitHub Actions bij elke push
naar de `main` branch. De workflow synct de bestanden via rsync en herstart de
containers met `docker compose up --build`.

[![Deploy to server](https://github.com/ByteMort/poc_research_project_2526/actions/workflows/deploy.yml/badge.svg)](https://github.com/ByteMort/poc_research_project_2526/actions/workflows/deploy.yml)

---

## Hoe de PoC testen?

1. Open de app en ga naar het tabblad **QR Genereren**
2. Kies een contactmoment en een geldigheidsduur
3. Klik op **Genereer QR-code**
4. Ga naar het tabblad **QR Scannen**
5. Plak het token in het invoerveld (of kopieer het via de knop onderaan de QR-code)
6. Klik op **Valideer token** om een succesvolle scan te simuleren
7. Klik daarna op **Replay-aanval** om te zien dat hetzelfde token geweigerd wordt

Je kan ook wachten tot de timer op 0 staat en dan pas scannen om te zien dat
een verlopen token ook geweigerd wordt.
