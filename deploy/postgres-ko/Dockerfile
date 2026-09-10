# syntax=docker/dockerfile:1

# postgis/postgis:17-3.5 이미지는 amd64 전용이다. 아래 PostgreSQL 공식 이미지와
# PGDG PostGIS 패키지는 amd64와 arm64를 모두 지원한다.
FROM postgres:17-bookworm

RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
      locales \
      postgresql-17-postgis-3 \
      postgresql-17-postgis-3-scripts && \
    sed -i 's/^# *\(ko_KR.UTF-8 UTF-8\)/\1/' /etc/locale.gen && \
    locale-gen ko_KR.UTF-8 && \
    rm -rf /var/lib/apt/lists/*

ENV LANG=ko_KR.UTF-8
ENV LC_ALL=ko_KR.UTF-8
