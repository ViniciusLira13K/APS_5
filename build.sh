#!/bin/zsh

set -e

cd "$(dirname "$0")"

mkdir -p out
javac -d out $(find src -name "*.java")

echo "Compilação concluída com sucesso."
