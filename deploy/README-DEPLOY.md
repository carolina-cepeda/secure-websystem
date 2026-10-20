# Guía de despliegue — Enterprise Risk Lab

Esta guía detalla el despliegue en un EC2 Ubuntu con Duck DNS, Apache, Spring Boot y Let's Encrypt.

## 1) Preparativos (local)

Compilar en tu máquina local antes de copiar al servidor.

```bash
# Backend
cd backend
./mvnw clean package -DskipTests

# Frontend con URL HTTPS para producción
cd frontend
# Asegurar VITE_API_URL=https://enterprise-risk-lab.duckdns.org
npm run build
Verificar:
ls -lh backend/target/risk-api-0.0.1-SNAPSHOT.jar
ls -lh frontend/dist/
2) Copiar archivos al EC2 (desde tu PC local)
Usa scp desde la carpeta donde tengas risk-lab.pem.
chmod 400 risk-lab.pem 2>/dev/null

# Backend
scp -i risk-lab.pem backend/target/risk-api-0.0.1-SNAPSHOT.jar ubuntu@100.30.25.107:/home/ubuntu/

# Frontend
scp -i risk-lab.pem -r frontend/dist ubuntu@100.30.25.107:/home/ubuntu/

# Configuración
scp -i risk-lab.pem deploy/enterprise-risk-api.service ubuntu@100.30.25.107:/home/ubuntu/
scp -i risk-lab.pem deploy/enterprise-risk.conf ubuntu@100.30.25.107:/home/ubuntu/
Conectarse:
ssh -i risk-lab.pem ubuntu@100.30.25.107
3) Desplegar backend (EC2)
sudo mkdir -p /opt/enterprise-risk-api
sudo mv /home/ubuntu/risk-api-0.0.1-SNAPSHOT.jar /opt/enterprise-risk-api/app.jar

sudo useradd --system --no-create-home riskapi 2>/dev/null || true
sudo chown -R riskapi:riskapi /opt/enterprise-risk-api

sudo cp /home/ubuntu/enterprise-risk-api.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now enterprise-risk-api

# Comprobar
sudo systemctl status enterprise-risk-api --no-pager
curl http://127.0.0.1:8080/actuator/health
curl -i http://127.0.0.1:8080/api/risk-status  # debe dar 401 en modo jwt
journalctl -u enterprise-risk-api -n 15 --no-pager
Si ves errores de proxy al arrancar, asegúrate de que el .service tenga http_proxy, https_proxy, HTTP_PROXY, HTTPS_PROXY vacíos y NO_PROXY=localhost,127.0.0.1,.auth0.com.
4) Desplegar frontend + Apache (HTTP)
sudo mkdir -p /var/www/enterprise-risk-frontend
sudo rm -rf /var/www/enterprise-risk-frontend/*
sudo cp -r /home/ubuntu/dist/* /var/www/enterprise-risk-frontend/
sudo chown -R www-data:www-data /var/www/enterprise-risk-frontend

sudo apt install -y apache2
sudo a2enmod ssl proxy proxy_http rewrite headers
sudo cp /home/ubuntu/enterprise-risk.conf /etc/apache2/sites-available/
sudo a2ensite enterprise-risk.conf
sudo a2dissite 000-default.conf 2>/dev/null || true
sudo apachectl configtest
sudo systemctl reload apache2
Comprobar HTTP:
curl -I http://enterprise-risk-lab.duckdns.org
curl -i http://enterprise-risk-lab.duckdns.org/api/risk-status

5) HTTPS con Lets Encrypt (Certbot)

sudo apt install -y certbot python3-certbot-apache
sudo certbot --apache -d enterprise-risk-lab.duckdns.org
Al finalizar, comprueba puertos y vhosts:
sudo ss -tlnp | grep -E ':80|:443'

sudo apachectl -S
Verifica el vhost SSL: debe tener DocumentRoot /var/www/enterprise-risk-frontend, el bloque <Directory> con RewriteRule para SPA y ProxyPass /api/ hacia 127.0.0.1:8080.
Renovación:
sudo certbot renew --dry-run
6) Verificación final
curl -I https://enterprise-risk-lab.duckdns.org
curl -i https://enterprise-risk-lab.duckdns.org/api/risk-status  # 401 sin token
echo | openssl s_client -servername enterprise-risk-lab.duckdns.org -connect enterprise-risk-lab.duckdns.org:443 2>/dev/null | openssl x509 -noout -dates -subject -issuer
Abrir en navegador: https://enterprise-risk-lab.duckdns.org con candado válido. Iniciar sesión con usuario de prueba y pulsar "Llamar a la API protegida" para obtener el JSON.
7) Checklist rápido
- Backend levantado con usuario riskapi, escucha solo en 127.0.0.1:8080
- /api/risk-status devuelve 401 sin token y 200 con token válido
- Apache sirviendo frontend estático + proxy /api/
- Puerto 443 escuchando con certificado válido
- Auth0 con https://enterprise-risk-lab.duckdns.org en Callback/Logout/Web Origins y User-Delegated Access con read:risk
- Security Group permite 80/443/22 (únicamente necesario)
- certbot renew --dry-run correcto