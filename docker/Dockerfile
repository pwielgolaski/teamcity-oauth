FROM java:8

ENV TEAMCITY_VERSION 9.1.6
ENV TEAMCITY_DATA_PATH /var/lib/teamcity

# Get and install teamcity
RUN curl -L https://download.jetbrains.com/teamcity/TeamCity-$TEAMCITY_VERSION.tar.gz | tar xz -C /opt

RUN apt-get update && apt-get install -y python-pip && rm -rf /var/lib/apt/lists/*
RUN pip install j2cli

RUN mkdir -p $TEAMCITY_DATA_PATH/plugins ${TEAMCITY_DATA_PATH}/config
RUN wget -P $TEAMCITY_DATA_PATH/plugins https://bintray.com/artifact/download/pwielgolaski/generic/teamcity-oauth-0.1.1.zip

COPY auth-config.xml.j2 /auth-config.xml.j2
COPY docker-entrypoint.sh /docker-entrypoint.sh

EXPOSE  8111

ENTRYPOINT ["/docker-entrypoint.sh"]