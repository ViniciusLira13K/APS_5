# Sistema Desktop de Identificação Biométrica

Aplicação desktop em Java para cadastro e validação básica de usuários com biometria simulada.

## Funcionalidades

- cadastrar usuário com nome, matrícula e código biométrico;
- salvar os usuários localmente em arquivo;
- autenticar o acesso com matrícula e código biométrico;
- exibir lista dos usuários cadastrados;
- mostrar mensagens de sucesso e falha na própria interface.

## Estrutura

- `src/` contém o código-fonte Java;
- `data/usuarios.csv` armazena os usuários cadastrados;
- `out/` é o diretório gerado na compilação.

## Como compilar

```bash
mkdir -p out
javac -d out $(find src -name "*.java")
```

## Como executar

```bash
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out br.com.aps.biometria.Main
```

## Execução completa pelo terminal

Use exatamente esta sequência dentro da pasta do projeto:

```bash
cd /Users/lira/Documents/Faculdade/APS_5
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out br.com.aps.biometria.Main
```

## Rebuild e execução

Se quiser recompilar tudo do zero e executar em seguida:

```bash
cd /Users/lira/Documents/Faculdade/APS_5
rm -rf out
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out br.com.aps.biometria.Main
```

## Execução com script

Se preferir usar os scripts criados no projeto:

```bash
cd /Users/lira/Documents/Faculdade/APS_5
./build.sh
./run.sh
```

## Observações

- a biometria é simulada por um código textual;
- a matrícula é tratada como identificador único do usuário;
- o arquivo de dados é criado automaticamente na primeira execução.
