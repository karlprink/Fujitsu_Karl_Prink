FROM ubuntu:latest
LABEL authors="prink"

ENTRYPOINT ["top", "-b"]