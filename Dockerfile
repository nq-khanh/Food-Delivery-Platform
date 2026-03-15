# Sử dụng bản PostGIS chính thức (đã có sẵn kho Repo của PostgreSQL)
FROM postgis/postgis:16-3.4

# Cài đặt pgvector trực tiếp từ gói pre-built của PostgreSQL 16
RUN apt-get update && \
    apt-get install -y postgresql-16-pgvector && \
    rm -rf /var/lib/apt/lists/*