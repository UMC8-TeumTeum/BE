FROM ubuntu:latest
LABEL authors="mac"

ENTRYPOINT ["top", "-b"]