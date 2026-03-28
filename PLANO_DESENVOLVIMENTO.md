# Plano de Desenvolvimento do Projeto

## Objetivo

Desenvolver uma aplicação desktop em Java para identificação biométrica de usuários, em versão básica, contendo:

- cadastro de usuário;
- registro biométrico simulado;
- tela de identificação/autenticação;
- validação do cadastro realizado;
- interface simples para demonstração acadêmica.

## Estratégia Técnica

- usar Java desktop sem dependências externas pesadas;
- implementar interface com Swing para facilitar execução local;
- simular a biometria por um identificador biométrico textual ou numérico;
- armazenar os cadastros localmente em arquivo;
- separar o projeto em camadas simples: interface, modelo, serviço e persistência.

## Passos de Implementação

1. Criar a estrutura base do projeto Java.
2. Definir o modelo de dados do usuário com informações básicas e biometria simulada.
3. Criar a camada de persistência para salvar e ler usuários em arquivo local.
4. Implementar o fluxo de cadastro de usuário.
5. Implementar o fluxo de autenticação biométrica com base no cadastro salvo.
6. Criar uma interface desktop simples com tela principal, formulário de cadastro e área de validação.
7. Exibir mensagens de sucesso, falha e inconsistência de dados.
8. Organizar o projeto para fácil apresentação acadêmica.
9. Documentar como executar o sistema.

## Regras Funcionais Iniciais

- o usuário poderá ser cadastrado com nome, CPF/login e identificação biométrica simulada;
- a autenticação deverá comparar a biometria informada com a biometria cadastrada;
- o sistema deve informar quando o usuário não existir;
- o sistema deve informar quando a biometria não corresponder;
- o sistema deve permitir o cadastro antes da autenticação.

## Entregáveis

- código-fonte do projeto Java desktop;
- arquivo de dados local para persistência simples;
- documentação básica de execução;
- interface mínima para cadastro e identificação.

## Observações

- como o ambiente atual não possui Java instalado, a implementação poderá ser criada normalmente, mas a compilação e o teste local dependerão da instalação de um JDK.
- caso a especificação do PDF exija algum campo adicional, o projeto poderá ser ajustado depois.
