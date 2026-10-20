#!/usr/bin/env bash
# Guía para el servidor Ubuntu
# Ejecutar con sudo según corresponda

# 1) Paquetes
# sudo apt update
# sudo apt install -y apache2 certbot python3-certbot-apache openjdk-21-jre

# 2) Backend
# sudo mkdir -p /opt/enterprise-risk-api
# sudo cp ../backend/target/risk-api-0.0.1-SNAPSHOT.jar /opt/enterprise-risk-api/app.jar
# sudo useradd --system --no-create-home riskapi
# sudo chown -R riskapi:riskapi /opt/enterprise-risk-api
# sudo cp enterprise-risk-api.service /etc/systemd/system/
# sudo systemctl daemon-reload
# sudo systemctl enable --now enterprise-risk-api
# curl http://127.0.0.1:8080/actuator/health

# 3) Frontend
# sudo mkdir -p /var/www/enterprise-risk-frontend
# sudo cp -r ../frontend/dist/* /var/www/enterprise-risk-frontend/

# 4) Apache
# sudo a2enmod ssl proxy proxy_http rewrite headers
# sudo cp enterprise-risk.conf /etc/apache2/sites-available/
# sudo a2ensite enterprise-risk.conf
# sudo a2dissite 000-default.conf 2>/dev/null || true
# sudo apachectl configtest
# sudo systemctl reload apache2

# 5) DNS + firewall
# Crear subdominio en Duck DNS apuntando a la IP pública
# Abrir solo 80/443 (nunca 8080)
