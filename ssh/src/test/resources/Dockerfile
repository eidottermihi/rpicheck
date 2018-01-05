FROM alpine:3.7
ENTRYPOINT ["/entrypoint.sh"]
EXPOSE 22
COPY entrypoint.sh /entrypoint.sh

RUN apk add --no-cache openssh \
  && sed -i s/#PermitRootLogin.*/PermitRootLogin\ yes/ /etc/ssh/sshd_config \
  && echo "root:root" | chpasswd

ADD id_rsa.pub /home/sshj/.ssh/authorized_keys

ADD test-container/ssh_host_ecdsa_key     /etc/ssh/ssh_host_ecdsa_key
ADD test-container/ssh_host_ecdsa_key.pub /etc/ssh/ssh_host_ecdsa_key.pub
ADD test-container/vcgencmd /usr/bin/vcgencmd

RUN \
  apk add --update bash && \
  apk add --update dos2unix && \
  rm -rf /var/cache/apk/* \

RUN \
  echo "root:smile" | chpasswd && \
  adduser -D -s /bin/bash sshj && \
  passwd -u sshj && \
  chmod 600 /home/sshj/.ssh/authorized_keys && \
  chmod 600 /etc/ssh/ssh_host_ecdsa_key && \
  chmod 644 /etc/ssh/ssh_host_ecdsa_key.pub && \
  chmod 755 /usr/bin/vcgencmd && \
  dos2unix /usr/bin/vcgencmd && \
  dos2unix /entrypoint.sh && \
  chmod 777 /entrypoint.sh && \
  chown -R sshj:sshj /home/sshj