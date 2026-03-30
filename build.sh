#!/bin/zsh

set -e

cd "$(dirname "$0")"

mvn -Dmaven.repo.local=.m2/repository clean compile

echo "Build Maven concluído com sucesso."
