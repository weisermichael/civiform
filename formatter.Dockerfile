FROM adoptopenjdk/openjdk11:alpine-slim

ENV JAVA_FORMATTER_URL "https://github.com/google/google-java-format/releases/download/google-java-format-1.9/google-java-format-1.9-all-deps.jar"

RUN apk update && \
    apk add --no-cache --update bash wget npm shfmt

RUN npm install -g typescript prettier @typescript-eslint/parser @typescript-eslint/eslint-plugin
RUN wget $JAVA_FORMATTER_URL -O /fmt.jar

COPY .prettierrc.js /.prettierrc.js
COPY .prettierignore /.prettierignore

VOLUME /code

CMD ["sh", "-c", \
    "java -jar /fmt.jar --replace $(find /code -name '*.java'); \
    cd /code; \
    shfmt -bn -ci -kp -i 2 -w \
    bin/** cloud/*/bin/** \
    browser-test/bin/** **/*.sh; \
    shfmt -bn -ci -kp -i 2 -w .; \
    cd universal-application-tool-0.0.1 \
    npx prettier \
    --write --config /.prettierrc.js --ignore-path /.prettierignore /code" \
    ]
