FROM eclipse-temurin:21-jdk-jammy

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y --no-install-recommends \
        maven \
        git \
        nginx \
        openssh-server \
        php-cli \
        python3 \
        default-mysql-client \
    && rm -rf /var/lib/apt/lists/*

# --- SSH (root login, password auth for humans + key auth for Ansible) ---
RUN mkdir -p /var/run/sshd \
    && echo 'root:Hello@123' | chpasswd \
    && sed -ri 's/^#?PermitRootLogin.*/PermitRootLogin yes/' /etc/ssh/sshd_config \
    && sed -ri 's/^#?PasswordAuthentication.*/PasswordAuthentication yes/' /etc/ssh/sshd_config \
    && mkdir -p /root/.ssh && chmod 700 /root/.ssh
COPY ansible/id_ansible.pub /root/.ssh/authorized_keys
RUN chmod 600 /root/.ssh/authorized_keys

# --- NGINX reverse proxy (8080 -> 127.0.0.1:8081) ---
COPY docker/nginx.conf /etc/nginx/nginx.conf

# --- Clone and build the Spring Boot project with Git + Maven ---
ARG REPO_URL=https://github.com/pharoth9999/final-devOps.git
ARG REPO_BRANCH=main
RUN git clone --branch ${REPO_BRANCH} --depth 1 ${REPO_URL} /build \
    && cd /build \
    && mvn -q -DskipTests package \
    && cp target/*.jar /app.jar

COPY docker/start.sh /start.sh
RUN chmod +x /start.sh

ENV DB_HOST=mysql
ENV DB_PORT=3306

EXPOSE 8080 22

CMD ["/start.sh"]
