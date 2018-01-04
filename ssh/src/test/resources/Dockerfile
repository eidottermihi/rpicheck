FROM sickp/alpine-sshd:7.5

ADD id_rsa.pub /home/sshj/.ssh/authorized_keys

ADD test-container/ssh_host_ecdsa_key     /etc/ssh/ssh_host_ecdsa_key
ADD test-container/ssh_host_ecdsa_key.pub /etc/ssh/ssh_host_ecdsa_key.pub
ADD test-container/vcgencmd /usr/bin/vcgencmd

RUN \
  apk add --update bash && \
  rm -rf /var/cache/apk/* \

RUN \
  echo "root:smile" | chpasswd && \
  adduser -D -s /bin/bash sshj && \
  passwd -u sshj && \
  chmod 600 /home/sshj/.ssh/authorized_keys && \
  chmod 600 /etc/ssh/ssh_host_ecdsa_key && \
  chmod 644 /etc/ssh/ssh_host_ecdsa_key.pub && \
  chmod 755 /usr/bin/vcgencmd && \
  chown -R sshj:sshj /home/sshj