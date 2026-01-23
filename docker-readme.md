Docker using local version of fines (Based on your local changes)
(All other services will use the images from sdshmctspublic)
```bash / zsh
 docker compose -p opal-fines-local \
  -f docker-compose.base.yml \
  -f docker-compose.local.yml \
  -f ../opal-user-service/docker-compose.base.yml \
  -f ../opal-user-service/docker-compose.master.yml \
  -f ../opal-logging-service/docker-compose.base.yml \
  -f ../opal-logging-service/docker-compose.master.yml \
  up --build -d
```
Docker using master version of fines (Based on the code in master)
```bash / zsh
 docker compose -p opal-fines-master \
  -f docker-compose.base.yml \
  -f docker-compose.master.yml \
  -f ../opal-user-service/docker-compose.base.yml \
  -f ../opal-user-service/docker-compose.master.yml \
  -f ../opal-logging-service/docker-compose.base.yml \
  -f ../opal-logging-service/docker-compose.master.yml \
  up --build -d
```
