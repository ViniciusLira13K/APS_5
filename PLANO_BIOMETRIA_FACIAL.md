# Plano de Implementação da Biometria Facial com OpenCV

## Objetivo

Evoluir a aplicação desktop em Java para permitir cadastro e validação por biometria facial, usando OpenCV integrado ao projeto.

A proposta será uma autenticação facial no modelo 1:1, em que o usuário informa sua matrícula e o sistema compara a face capturada no momento da autenticação apenas com as amostras faciais previamente cadastradas para aquela matrícula.

## Etapas previstas

1. Revisar a estrutura atual da aplicação e localizar os pontos de cadastro e autenticação.
2. Verificar no ambiente a disponibilidade de Java, câmera e dependências nativas necessárias para OpenCV.
3. Definir a estratégia técnica da biometria facial para a versão atual, adotando autenticação facial 1:1 por matrícula.
4. Integrar a biblioteca OpenCV ao projeto Java.
5. Criar um serviço responsável por inicializar o OpenCV, carregar as bibliotecas nativas e acessar a webcam.
6. Implementar a captura de rosto no momento do cadastro do usuário.
7. Aplicar pré-processamento nas imagens capturadas, incluindo detecção da face, recorte da região facial, conversão para tons de cinza e padronização de tamanho.
8. Armazenar localmente múltiplas amostras faciais por matrícula.
9. Implementar a leitura facial no momento da autenticação.
10. Solicitar a matrícula no fluxo de autenticação antes da validação facial.
11. Comparar a face capturada com as amostras faciais previamente cadastradas para a matrícula informada.
12. Exibir na interface mensagens claras de sucesso, falha de reconhecimento, ausência de rosto detectado e ausência de câmera.
13. Ajustar a interface desktop para incluir os botões e o fluxo visual da biometria facial.
14. Validar a compilação do projeto e documentar o novo fluxo de execução.

## Decisões técnicas iniciais

- o projeto continuará em Java desktop com Swing;
- o OpenCV será usado via Java bindings;
- a implementação buscará um fluxo simples e funcional para demonstração acadêmica;
- a autenticação será feita no formato 1:1 por matrícula;
- o armazenamento das referências faciais será local;
- cada usuário poderá possuir múltiplas amostras faciais cadastradas;
- as imagens capturadas passarão por pré-processamento antes de serem salvas e comparadas;
- a solução deverá continuar compatível com execução local no macOS.

## Estratégia técnica sugerida

- usar a webcam do computador como dispositivo de captura;
- usar detecção facial para localizar a face na imagem capturada;
- recortar apenas a região do rosto antes de salvar ou comparar;
- converter as imagens para escala de cinza;
- padronizar o tamanho das imagens faciais;
- salvar múltiplas imagens faciais por matrícula no cadastro;
- na autenticação, comparar apenas contra as amostras da matrícula informada;
- manter a solução simples, sem integração com Touch ID, biometria nativa do macOS ou serviços externos.

## Pontos que podem exigir confirmação

- instalação da dependência Java do OpenCV;
- instalação ou configuração das bibliotecas nativas do OpenCV;
- permissão de acesso à câmera no macOS;
- escolha do mecanismo de detecção facial;
- escolha do mecanismo de comparação facial;
- formato e local de armazenamento das amostras faciais;
- quantidade mínima de amostras por usuário para melhorar a validação.

## Fluxo esperado de cadastro facial

1. usuário informa os dados de cadastro, incluindo matrícula;
2. usuário aciona a opção de captura biométrica;
3. sistema abre a webcam;
4. sistema detecta o rosto na imagem;
5. sistema realiza o pré-processamento da face detectada;
6. sistema captura e salva múltiplas amostras faciais vinculadas à matrícula;
7. sistema confirma que o cadastro facial foi concluído com sucesso.

## Fluxo esperado de autenticação facial

1. usuário informa a matrícula;
2. usuário aciona a autenticação por biometria facial;
3. sistema abre a webcam;
4. sistema detecta a face na imagem atual;
5. sistema aplica o mesmo pré-processamento usado no cadastro;
6. sistema compara a face atual com as amostras faciais salvas da matrícula informada;
7. sistema retorna sucesso ou falha na autenticação.

## Resultado esperado

- cadastro de usuário com captura facial;
- armazenamento local de múltiplas amostras faciais por matrícula;
- autenticação por reconhecimento facial com comparação direcionada à matrícula informada;
- interface atualizada para suportar o fluxo de biometria facial;
- tratamento visual e funcional para erro de câmera, ausência de rosto e falha de reconhecimento;
- documentação atualizada com dependências, configuração do ambiente e comandos de execução.