FROM debian
WORKDIR /github-miner
COPY . .
RUN apt-get update
RUN apt-get install -y openjdk-11-jdk maven curl apt-transport-https gnupg nano lsb-release debconf-utils vim python3 python3-pip python3-venv apache2
RUN mvn clean compile assembly:single
# couchdb couchdb/adminpass       password
# couchdb couchdb/adminpass_again password
# couchdb couchdb/bindaddress     string
# couchdb couchdb/adminpass_mismatch      error
# couchdb couchdb/mode    select  standalone
# couchdb couchdb/nodename        string  couchdb@localhost
# couchdb couchdb/error_setting_password  error
# couchdb couchdb/no_cookie_monsters      error
# couchdb couchdb/no_cookie       error
# couchdb couchdb/have_1x_databases       note
# couchdb couchdb/cookie  string  github-miner
# couchdb couchdb/postrm_remove_databases boolean false
RUN echo "couchdb couchdb/mode    select  standalone" | debconf-set-selections
RUN echo "couchdb couchdb/bindaddress  string 127.0.0.1" | debconf-set-selections
RUN echo "couchdb couchdb/cookie  string  github-miner" | debconf-set-selections
RUN echo "couchdb couchdb/adminpass       password admin" | debconf-set-selections
RUN echo "couchdb couchdb/adminpass_again password admin" | debconf-set-selections
RUN curl https://couchdb.apache.org/repo/keys.asc | gpg --dearmor | tee /usr/share/keyrings/couchdb-archive-keyring.gpg >/dev/null 2>&1
RUN echo "deb [signed-by=/usr/share/keyrings/couchdb-archive-keyring.gpg] https://apache.jfrog.io/artifactory/couchdb-deb/ `lsb_release -cs` main" \ | tee /etc/apt/sources.list.d/couchdb.list >/dev/null
RUN apt-get update
RUN apt-get install -y couchdb
RUN chown -R couchdb:couchdb /opt/couchdb
RUN chown -R couchdb:couchdb /var/log/couchdb
RUN chown -R couchdb:couchdb /etc/couchdb
RUN chown -R couchdb:couchdb /var/lib/couchdb
# EXPOSE 5984/tcp
# EXPOSE 8081/tcp
COPY docker_conf/ports.conf /etc/apache2/ports.conf
COPY docker_conf/local.ini /opt/couchdb/etc/local.ini
RUN touch /var/www/html/index.html
# RUN mvn clean compile assembly:single
# CMD service couchdb start

WORKDIR /github-miner
ENTRYPOINT /bin/bash /github-miner/docker-start.sh
# ENTRYPOINT ["/bin/bash"]
# CMD ["Docker"]
