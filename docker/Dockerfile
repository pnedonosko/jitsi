# Docker container for Jitsi
#
# Build : docker build -t exoplatform/jitsi .
# Run   : docker run -ti --name jitsi exoplatform/jitsi:latest bash

FROM exoplatform/base-jdk:jdk8

ENV TERM xterm

# Adding Jitsi debian repo
RUN echo 'deb http://download.jitsi.org/nightly/deb unstable/' > /etc/apt/sources.list.d/jitsi.list && \
    wget -qO - https://download.jitsi.org/nightly/deb/unstable/archive.key | apt-key add -

# Install Jitsi packages
RUN apt-get -qq update
#RUN apt-get -qq -y upgrade ${_APT_OPTIONS}
RUN apt-get -qq -y install ${_APT_OPTIONS} jitsi-meet

#RUN apt-get -qq update && \
#  apt-get -qq -y upgrade ${_APT_OPTIONS} && \
#  apt-get -qq -y install ${_APT_OPTIONS} jitsi-meet && \
#  apt-get -qq -y autoremove && \
#  apt-get -qq -y clean && \
#  rm -rf /var/lib/apt/lists/*

COPY configure.sh /configure.sh
COPY run.sh /run.sh
RUN chmod 700 /run.sh

expose 80 443

CMD ["/run.sh]
