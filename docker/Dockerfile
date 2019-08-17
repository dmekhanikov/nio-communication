FROM openjdk:8

RUN apt-get update

#Add authorized SSH keys
ADD authorized_keys /root/.ssh/

#Install SSH
RUN apt-get install ssh -y

#Permit SSH root login
RUN sed -i 's/#PermitRootLogin prohibit-password/PermitRootLogin without-password/g' /etc/ssh/sshd_config

#Create /run/sshd
RUN mkdir /run/sshd

#Start SSH DAEMON
CMD ["/usr/sbin/sshd", "-D"]
