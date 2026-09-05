# 개발 EC2 t4g.micro 전환 실행서

## 목표 구성

- Ubuntu 24.04 ARM64 / `t4g.micro`
- API, 관리자 앱, PostgreSQL 17 + PostGIS, Redis를 Docker Compose로 실행
- API는 `127.0.0.1:8080`, 관리자 앱은 `127.0.0.1:8081`에만 바인딩하고
  Nginx가 도메인별로 80/443 요청을 전달
- 배포 파일과 영속 데이터는 기존 개발 서버와 동일한 `~/saerok` 아래에 배치
- 기존 개발 서버와 동일하게 Elastic IP는 사용하지 않고, EC2의 일반 퍼블릭 IPv4를 사용
- 기존 t2 서버는 검증과 DNS 전환이 끝날 때까지 유지

일반 퍼블릭 IPv4는 인스턴스를 **중지 후 다시 시작하면 바뀔 수 있다**. 운영 중
재부팅만 하는 경우에는 보통 유지되지만, 인스턴스 유형 변경처럼 중지/시작이 필요한
작업 뒤에는 반드시 AWS 콘솔에서 주소를 다시 확인한다. 주소가 바뀌면 GitHub
`EC2_HOST`와 Route 53의 두 A 레코드를 함께 수정한다.

## 이번 변경 내용과 이유

### 애플리케이션 이미지의 ARM64 지원

[`Dockerfile`](../Dockerfile)은 빌드 단계와 실행 단계를 분리한다. Spring Boot JAR는
CPU 아키텍처에 종속되지 않으므로 Gradle 빌드는 GitHub Actions 러너의 원래
아키텍처에서 실행하고, 최종 실행 이미지만 배포 대상인 ARM64로 만든다. 이렇게 하면
ARM 에뮬레이션 안에서 Gradle 전체 빌드를 수행하는 것보다 빠르고 안정적이다.

실행 기반 이미지인 `eclipse-temurin:21-jre-jammy`는 AMD64와 ARM64를 모두
지원한다. 따라서 개발 배포는 ARM64 이미지를 만들고, 기존 운영 배포가 같은
Dockerfile로 AMD64 이미지를 만드는 것도 가능하다.

### PostgreSQL과 PostGIS의 ARM64 지원

기존 `postgis/postgis:17-3.5` 이미지는 현재 사용 방식에서 ARM64로 실행할 수 없다.
[`deploy/postgres-ko/Dockerfile`](../deploy/postgres-ko/Dockerfile)은 ARM64를 지원하는
PostgreSQL 17 공식 이미지를 기반으로 하고, PostgreSQL 패키지 저장소에 포함된
PostGIS 패키지를 설치하도록 변경했다. 기존 DB와 동일하게 `ko_KR.UTF-8` 로캘도
생성한다.

이 이미지는 GitHub Actions 러너에서 만들어 전송하지 않고 새 ARM64 EC2에서 직접
빌드한다. 따라서 결과 이미지는 서버 아키텍처와 일치한다. 빌드 후에는 PostgreSQL
17, PostGIS 확장, 한글 로캘을 모두 사용할 수 있어야 한다.

### Docker Compose 구성과 데이터 유지

[`deploy/docker-compose.dev.yml`](../deploy/docker-compose.dev.yml)의 주요 변경은 다음과
같다.

- 앱의 `8080` 포트는 `127.0.0.1`에만 연결한다. 인터넷에서 8080으로 직접 접근하지
  못하게 하고, 외부 요청은 Nginx의 80/443 포트만 통과시킨다.
- PostgreSQL의 `5432` 포트는 호스트에 공개하지 않는다. 앱은 Docker 내부
  네트워크에서 `postgres:5432`로 접속한다.
- DB 접속 주소를 `jdbc:postgresql://postgres:5432/saerok`으로 고정한다. 개발
  환경에서는 더 이상 외부 RDS 주소를 사용하지 않는다.
- PostgreSQL 데이터는 `~/saerok/data/postgres`, Redis 데이터는
  `~/saerok/data/redis`에 저장한다. 컨테이너를 삭제하거나 이미지를 교체해도 이
  디렉터리는 남는다.
- 애플리케이션 접근 로그는 `~/saerok/accesslogs`에 저장한다. 컨테이너 내부의
  `/app/accesslogs`와 연결하므로 앱 컨테이너를 다시 만들어도 로그가 유지된다.
- PostgreSQL과 Redis가 `healthy`가 된 뒤에만 앱을 시작한다. DB가 준비되기 전에
  앱이 먼저 시작되어 반복적으로 실패하는 상황을 줄인다.
- 앱에도 `/health` 기반 상태 확인을 추가했다. 컨테이너 실행 여부뿐 아니라 실제
  애플리케이션 응답 여부를 확인할 수 있다.
- Docker의 JSON 로그를 파일당 10MB, 최대 3개로 순환시킨다. 로그가 루트 디스크를
  계속 채우는 것을 방지한다.
- `init: true`를 적용해 앱 컨테이너 내부의 종료 신호와 자식 프로세스를 정상적으로
  처리한다.

절대 경로에 데이터를 저장하는 것은 **컨테이너 재생성에 대한 보호**다. EC2 또는
EBS 자체가 손실되는 상황까지 보호하지는 않는다. 현재 루트 EBS는 인스턴스 종료 시
자동 삭제하지 않는 설정이지만, 볼륨 오삭제나 AZ 장애에 대비하려면 별도의 dump와
EBS 스냅샷이 필요하다.

### t4g.micro 자원 제한

새 서버는 메모리가 약 1GiB이므로 컨테이너별 상한을 둔다.

| 컨테이너 | CPU 상한 | 메모리 예약 | 메모리 상한 |
|---|---:|---:|---:|
| 애플리케이션 | 0.75 vCPU | 320MB | 512MB |
| PostgreSQL | 0.50 vCPU | 128MB | 256MB |
| Redis | 0.25 vCPU | 64MB | 192MB |
| 관리자 앱 | 0.25 vCPU | 64MB | 256MB |

기존 개발 서버 실측값은 API 약 251MiB, PostgreSQL 약 60MiB, Redis 약 1.4MiB,
관리자 앱 약 34MiB로 합계 약 347MiB였다. 낮은 트래픽에서는 `t4g.micro`로 시작할
수 있지만 모든 컨테이너가 동시에 상한까지 사용하면 호스트 메모리를 초과한다. 현재
설정한 1GiB 비상 스왑은 순간적인 메모리 급증 때 종료 가능성을 낮출 뿐, 지속적인
메모리 부족을 해결하지는 않는다. 전체 배포 뒤 가용 메모리와 스왑을 반드시 다시
측정하고, 스왑 증가, OOM 종료 또는 응답 지연이 반복되면 `t4g.small`로 올린다.

### GitHub Actions 배포 흐름

[`deploy-to-dev-ec2-docker.yml`](../.github/workflows/deploy-to-dev-ec2-docker.yml)은
다음 순서로 동작한다.

1. QEMU와 Buildx를 준비하고 앱의 `linux/arm64` 이미지를 만든다.
2. 이미지를 GHCR에 push하고, 배포에는 변경되지 않는 digest 참조값을 사용한다.
   같은 `dev-latest` 태그가 나중에 바뀌어도 현재 실행에서 선택한 이미지는 바뀌지
   않는다.
3. Compose 파일, PostGIS Dockerfile, Nginx 설정을 `~/saerok`으로 복사한다.
4. GitHub Secrets와 Variables로 `/run/saerok/env.dev`를 만들고 권한을 제한한다.
   작업이 끝나면 이 임시 파일을 삭제한다.
5. 새 서버에서 PostGIS 이미지를 빌드하고 PostgreSQL과 Redis부터 시작한다.
6. 두 컨테이너가 정상 상태인지 확인한다.
7. 빈 DB에도 PostGIS 확장을 활성화하고 버전을 확인한다.
8. `full` 모드라면 앱 이미지를 받고 앱만 새로 생성한 뒤 `/health`를 확인한다.

수동 실행에서 `infrastructure-only`를 선택하면 6단계에서 멈춘다. 데이터 복원 전에
새 앱이 빈 DB에 접속하거나 Flyway를 먼저 실행하지 않도록 하기 위한 이전 전용
모드다. 복원이 끝난 뒤 `full`로 다시 실행하면 앱이 시작된다.

`concurrency`는 개발 배포를 한 번에 하나만 실행하고, 진행 중인 배포를 새 실행이
강제로 취소하지 않게 한다. `DEV_ARM64_AUTODEPLOY` 조건은 서버 전환 중
`develop` push가 의도하지 않은 서버로 자동 배포되는 것을 막는다. 수동 실행은 이
변수와 무관하게 가능하다.

기존 JAR 직접 배포 워크플로 `deploy-to-dev-ec2.yml`은 제거한다. 두 배포 방식이
동시에 남아 있으면 한쪽은 호스트 Java 프로세스를, 다른 쪽은 Docker 컨테이너를
실행해 포트 충돌이나 서로 다른 버전 실행을 일으킬 수 있기 때문이다.

### Nginx와 빌드 컨텍스트

[`saerok-dev-http.conf`](../deploy/nginx/saerok-dev-http.conf)는
`dev-api.saerok.app`을 `127.0.0.1:8080`으로,
`dev-admin.saerok.app`을 `127.0.0.1:8081`로 전달한다. 실제 클라이언트 IP와
프로토콜 정보, WebSocket 업그레이드 헤더를 앱에 넘기며 업로드 최대 크기는 20MB다.
첫 전환 때는 HTTP 설정으로 두 localhost 연결을 확인하고, DNS 변경 후 Certbot이
HTTPS 설정을 추가한다.

루트의 [`.dockerignore`](../.dockerignore)는 `.env`, Git 메타데이터, 빌드 결과,
문서와 로컬 DB 데이터가 Docker 빌드 컨텍스트에 포함되지 않게 한다. 비밀값이나 큰
데이터 디렉터리가 GitHub Actions의 빌드 엔진으로 전송되는 것을 막고 빌드 시간을
줄인다.

## 1. 자동 배포 잠금 확인

마이그레이션 중 `develop` 병합이 기존 서버에 자동 배포되지 않도록 워크플로가
다음 조건을 사용한다.

```text
DEV_ARM64_AUTODEPLOY == true
```

전환이 끝날 때까지 GitHub Repository variable `DEV_ARM64_AUTODEPLOY`은 만들지
않거나 `false`로 둔다.

이 브랜치의 워크플로와 Docker 설정을 먼저 `develop`에 반영한다. 이때 발생하는
`develop` push는 위 조건 때문에 자동 배포되지 않는다. 새 ARM64 서버의 주소로
`EC2_HOST`를 바꾸기 전에 반드시 이 변경부터 반영해야 기존 AMD64 전용 워크플로가
새 ARM64 서버에서 실행되는 일을 막을 수 있다.

## 2. 새 서버의 퍼블릭 IPv4 확인

Elastic IP는 연결하지 않는다. AWS EC2 콘솔에서 새 ARM64 인스턴스의 현재
`퍼블릭 IPv4 주소`를 확인해 별도로 기록한다. 아직 Route 53 레코드는 변경하지
않는다.

코드가 `develop`에 반영된 다음 GitHub `dev` environment의 `EC2_HOST`를 방금
확인한 새 퍼블릭 IPv4로 변경한다. 기존 키 페어를 재사용했으므로
`EC2_SSH_PRIVATE_KEY`는 그대로 사용할 수 있다.

이 단계부터 DNS 전환이 끝날 때까지는 새 인스턴스를 중지하지 않는다. 부득이하게
중지/시작했다면 바뀐 퍼블릭 IPv4를 다시 확인하고 `EC2_HOST`부터 수정한 후 다음
단계를 진행한다.

## 3. 새 서버의 DB와 Redis만 시작

GitHub Actions에서 `Deploy Dev (Docker Compose via GHCR)`를 수동 실행하고
`deploy_mode=infrastructure-only`를 선택한다. 실행 후 새 서버에서 확인한다.

```bash
docker ps
docker inspect --format '{{.State.Health.Status}}' saerok-postgres-dev
docker inspect --format '{{.State.Health.Status}}' saerok-redis-dev
```

두 컨테이너 모두 `healthy`여야 한다.

PostGIS도 자동 활성화되어야 한다.

```bash
docker exec saerok-postgres-dev sh -lc \
  'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Atc \
    "SELECT postgis_version();"'
```

## 4. 관리자 앱 ARM64 배포

`saerok-admin` 저장소의 ARM64 변경을 먼저 `develop`에 반영한다. 전환이 끝날
때까지 해당 저장소의 `DEV_ARM64_AUTODEPLOY`도 만들지 않거나 `false`로 둔다.
머지 작업이 `Skipped`된 것을 확인한 뒤 `dev` Environment Secret `EC2_HOST`를
새 서버의 퍼블릭 IPv4로 바꾼다.

Admin 저장소의 `Deploy Admin to Dev (ARM64 EC2 + Docker Compose)`를 수동
실행한다. 새 서버에서 확인한다.

```bash
docker inspect --format \
  'status={{.State.Status}} health={{.State.Health.Status}} ports={{json .HostConfig.PortBindings}}' \
  saerok-admin-dev
curl -fsSI http://127.0.0.1:8081/login
```

8081 포트는 `127.0.0.1`에만 바인딩되어야 한다. 전체 컨테이너의 실제 자원 사용량도
확인한다.

```bash
free -h
swapon --show
docker stats --no-stream \
  --format 'table {{.Name}}\t{{.MemUsage}}\t{{.CPUPerc}}'
```

가용 메모리가 지속적으로 150MiB 미만이거나 스왑 사용량이 계속 증가하면 최종
전환 전에 `t4g.small`로 올린다.

## 5. 무중단 시험 백업과 복원

기존 앱을 중지하기 전에 시험 dump를 만들어 새 PostgreSQL 17.11과 PostGIS
3.6에서 복원되는지 확인한다. `pg_dump`는 일관된 스냅샷을 사용하므로 시험 중에도
기존 서비스를 계속 운영할 수 있다.

기존 서버에서 실행한다.

```bash
mkdir -p ~/migration-backup

docker exec saerok-postgres-dev sh -lc \
  'pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
    -Fc --no-owner --no-privileges' \
  > ~/migration-backup/saerok-dev-trial.dump

test -s ~/migration-backup/saerok-dev-trial.dump
sha256sum ~/migration-backup/saerok-dev-trial.dump
docker exec -i saerok-postgres-dev pg_restore -l \
  < ~/migration-backup/saerok-dev-trial.dump | head
```

로컬 PC를 경유해 신규 서버로 전송한다.

```bash
scp -i dev-Saerok.pem \
  ubuntu@OLD_PUBLIC_IP:~/migration-backup/saerok-dev-trial.dump .

scp -i dev-Saerok.pem \
  saerok-dev-trial.dump \
  ubuntu@NEW_PUBLIC_IP:~/saerok/backup/
```

신규 서버에서 복원하고 확인한다.

```bash
docker exec -i saerok-postgres-dev sh -lc \
  'pg_restore -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
    --clean --if-exists --no-owner --no-privileges --exit-on-error' \
  < ~/saerok/backup/saerok-dev-trial.dump

docker exec saerok-postgres-dev sh -lc \
  'psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Atc \
    "SELECT count(*) FROM flyway_schema_history;
     SELECT postgis_version();
     SELECT pg_size_pretty(pg_database_size(current_database()));
     SELECT count(*) FROM users;
     SELECT count(*) FROM user_bird_collection;"'
```

Flyway 이력은 기존과 같은 93개여야 한다. 시험 복원이 실패해도 기존 앱과 DNS에는
영향이 없다.

## 6. 기존 PostgreSQL 최종 백업

새 DB가 준비된 뒤, 기존 t2 서버에서 애플리케이션을 중지해 추가
쓰기를 막고 dump를 만든다.

```bash
docker stop saerok-dev saerok-admin-dev
mkdir -p ~/migration-backup

docker exec saerok-postgres-dev sh -lc \
  'pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
    -Fc --no-owner --no-privileges' \
  > ~/migration-backup/saerok-dev-final.dump

test -s ~/migration-backup/saerok-dev-final.dump
sha256sum ~/migration-backup/saerok-dev-final.dump
docker exec -i saerok-postgres-dev pg_restore -l \
  < ~/migration-backup/saerok-dev-final.dump | head
```

DNS는 아직 기존 서버를 가리키므로 복원이 실패하면 아래 명령으로 기존 서비스를
즉시 되돌릴 수 있다.

```bash
docker start saerok-dev saerok-admin-dev
```

## 7. 최종 dump 전송 및 복원

로컬 PC를 경유해 기존 서버의 dump를 새 서버로 복사한다.

```bash
scp -i dev-Saerok.pem \
  ubuntu@OLD_PUBLIC_IP:~/migration-backup/saerok-dev-final.dump .

scp -i dev-Saerok.pem \
  saerok-dev-final.dump \
  ubuntu@NEW_PUBLIC_IP:~/saerok/backup/
```

여기서 `OLD_PUBLIC_IP`와 `NEW_PUBLIC_IP`는 각각 AWS 콘솔에 표시되는 기존 서버와
새 서버의 현재 퍼블릭 IPv4로 바꿔 입력한다.

새 서버에서 복원한다.

```bash
docker exec -i saerok-postgres-dev sh -lc \
  'pg_restore -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
    --clean --if-exists --no-owner --no-privileges --exit-on-error' \
  < ~/saerok/backup/saerok-dev-final.dump
```

복원 결과를 확인한다.

```bash
docker exec saerok-postgres-dev sh -lc \
  'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Atc \
    "SELECT count(*) FROM flyway_schema_history; SELECT postgis_version();"'
```

## 8. API 애플리케이션 시작

같은 GitHub Actions를 `deploy_mode=full`로 다시 실행한다. 새 서버에서 확인한다.

```bash
curl -fsS http://127.0.0.1:8080/health
curl -fsSI http://127.0.0.1:8081/login
docker compose \
  --env-file /run/saerok/env.dev \
  -p saerok \
  -f ~/saerok/docker-compose.yml \
  -f ~/saerok/docker-compose.dev.yml ps
```

배포 종료 후 `/run/saerok/env.dev`는 보안상 삭제되므로 두 번째 명령은 배포 중
진단용이다. 평상시에는 `docker ps`와 `docker logs`를 사용한다.

두 localhost 확인이 모두 성공해야 Nginx와 DNS 전환을 진행한다.

## 9. Nginx와 HTTPS 전환

새 서버에서 HTTP 설정을 설치한다.

```bash
sudo install -m 0644 \
  ~/saerok/nginx/saerok-dev-http.conf \
  /etc/nginx/sites-available/saerok-dev
sudo ln -sfn /etc/nginx/sites-available/saerok-dev \
  /etc/nginx/sites-enabled/saerok-dev
sudo nginx -t
sudo systemctl reload nginx
```

DNS 변경 전에 도메인 Host 헤더로 두 프록시를 확인한다.

```bash
curl -fsS -H 'Host: dev-api.saerok.app' http://127.0.0.1/health
curl -fsSI -H 'Host: dev-admin.saerok.app' http://127.0.0.1/login
```

AWS 콘솔에서 새 서버의 퍼블릭 IPv4가 2단계에서 기록한 값과 같은지 한 번 더
확인한다. Route 53의 다음 A 레코드를 그 퍼블릭 IPv4로 변경한다.

- `dev-api.saerok.app`
- `dev-admin.saerok.app`

DNS 전파 후 인증서를 발급한다.

```bash
sudo apt-get update
sudo apt-get install -y certbot python3-certbot-nginx
sudo certbot --nginx \
  -d dev-api.saerok.app \
  -d dev-admin.saerok.app
```

최종 확인:

```bash
curl -fsS https://dev-api.saerok.app/health
curl -sSI https://dev-admin.saerok.app | head
sudo certbot renew --dry-run
```

## 10. 전환 완료

BE와 Admin 두 저장소에 GitHub Repository variable
`DEV_ARM64_AUTODEPLOY=true`를 설정해 `develop` push 자동 배포를 다시
활성화한다. 기존 t2 서버는 즉시 삭제하지 않고 중지 상태로 3~7일 보관한다. 문제가
없으면 최종 EBS 스냅샷과 dump를 확인한 뒤 기존 인스턴스를 종료한다. 기존 루트
볼륨은 종료 시 자동 삭제되지 않으므로 보관 기간 이후 별도로 삭제한다.

Redis에는 광고 중복 방지용 단기 키와 만료되는 알림 배치가 저장된다. 개발 서버
전환에서는 기존 Redis 데이터를 옮기지 않으므로 전환 시점에 남은 임시 키와 미처리
배치는 사라질 수 있다.

향후 새 개발 서버를 중지/시작하거나 인스턴스 유형을 변경했다면 아래 항목을 한
묶음으로 처리한다.

1. EC2 콘솔에서 새 퍼블릭 IPv4 확인
2. BE와 Admin 저장소 `dev` environment의 `EC2_HOST` 수정
3. Route 53의 `dev-api.saerok.app`, `dev-admin.saerok.app` A 레코드 수정
4. 두 HTTPS 주소와 GitHub Actions 배포를 다시 확인
