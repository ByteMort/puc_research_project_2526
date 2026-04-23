Live demo:
https://handshake-poc.myaddr.dev

# 1. Deployment

## 1.1 Lokaal met Docker:

Opstarten:

```
docker compose up
```

Afsluiten:

```
docker compose down
```

## 1.2 Online met Docker:

Ubuntu server met Docker installatie:
```
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

GitHub Actions Deploy:

[![Deploy to server](https://github.com/ByteMort/poc_research_project_2526/actions/workflows/deploy.yml/badge.svg)](https://github.com/ByteMort/poc_research_project_2526/actions/workflows/deploy.yml)
