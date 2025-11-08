# deploy/postgres-ko/Dockerfile
# PostGIS + ko_KR.UTF-8
FROM postgis/postgis:17-3.5

# ko_KR.UTF-8 로캘 생성
RUN apt-get update && \
    apt-get install -y locales && \
    sed -i 's/# ko_KR.UTF-8 UTF-8/ko_KR.UTF-8 UTF-8/' /etc/locale.gen && \
    locale-gen && \
    rm -rf /var/lib/apt/lists/*

ENV LANG=ko_KR.UTF-8
ENV LC_ALL=ko_KR.UTF-8
