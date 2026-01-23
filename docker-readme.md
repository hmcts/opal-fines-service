Docker using local version of fines (Based on your local changes)
(All other services will use the images from sdshmctspublic)
```bash / zsh
 docker compose -p opal-fines-local -f docker-compose.base.yml -f docker-compose.local.yml up --build -d
```
Docker using master version of fines (Based on the code in master)
```bash / zsh
 docker compose -p opal-fines-master  -f docker-compose.base.yml -f docker-compose.master.yml up --build -d
```
