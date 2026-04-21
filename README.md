# Sistema Desktop de Identificação Biométrica Facial

Aplicação desktop em Java com Swing e OpenCV para cadastro e validação de usuários por biometria facial.

## Funcionalidades

- cadastrar usuário com nome, matrícula e cinco capturas faciais pela webcam;
- salvar os usuários localmente em arquivo e em imagem facial;
- autenticar o acesso com matrícula e reconhecimento facial;
- exibir lista dos usuários cadastrados;
- mostrar mensagens de sucesso e falha na própria interface.

## Estrutura

- `src/` contém o código-fonte Java;
- `resources/` contém recursos do OpenCV usados pela aplicação;
- `data/usuarios.csv` armazena os usuários cadastrados;
- `data/faces/` armazena a referência facial de cada matrícula;
- `target/` é o diretório gerado pelo Maven.

## Como compilar

```bash
mvn -Dmaven.repo.local=.m2/repository clean compile
```

## Como executar

```bash
mvn -Dmaven.repo.local=.m2/repository clean compile exec:java
```

## Execução completa pelo terminal

Use exatamente esta sequência dentro da pasta do projeto:

```bash
cd /Users/lira/Documents/Faculdade/APS_5
mvn -Dmaven.repo.local=.m2/repository clean compile exec:java
```

## Rebuild e execução

Se quiser recompilar tudo do zero e executar em seguida:

```bash
cd /Users/lira/Documents/Faculdade/APS_5
rm -rf target
mvn -Dmaven.repo.local=.m2/repository clean compile exec:java
```

## Execução com script

Se preferir usar os scripts criados no projeto:

```bash
cd /Users/lira/Documents/Faculdade/APS_5
./build.sh
./run.sh
```

## Observações

- a comparação facial implementada é básica, adequada para demonstração acadêmica;
- o cadastro agora exige 5 amostras faciais para melhorar o reconhecimento;
- a matrícula é tratada como identificador único do usuário;
- o projeto depende da webcam e da permissão de câmera no macOS;
- o arquivo de dados e as imagens faciais são criados automaticamente na primeira execução.
