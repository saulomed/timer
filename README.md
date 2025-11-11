# Timer - Aplicativo de Temporizador para Treinos Funcionais

## Descrição do Projeto

O **Timer** é um aplicativo Android nativo, desenvolvido em Kotlin com Jetpack Compose, projetado para atletas e praticantes de treinos funcionais. Ele oferece uma interface moderna e intuitiva para gerenciar diversos tipos de temporizadores de treino, desde os mais comuns como AMRAP e TABATA, até um sistema completo para a criação de circuitos personalizados.

O projeto foi construído com uma arquitetura MVVM robusta, utilizando componentes modernos do Android Jetpack como Room para persistência de dados, ViewModel para gerenciamento de estado e Jetpack Navigation para a navegação entre as telas.

## Funcionalidades Implementadas

- **5 Modos de Treino:**
  - **AMRAP (As Many Rounds As Possible):** Contagem regressiva com um contador de rounds manual.
  - **FOR TIME:** Cronômetro crescente, com a opção de definir um tempo máximo (cap time).
  - **TABATA:** Ciclos configuráveis de trabalho e descanso.
  - **EMOM (Every Minute On the Minute):** Alertas a cada minuto com um temporizador interno.
  - **Circuito Personalizado:** Crie, salve, edite e execute treinos complexos com múltiplos exercícios, tempos de trabalho (1 e 2), descanso e número de rounds.

- **Interface de Usuário Otimizada para Treinos:**
  - **Modo Imersivo:** As telas de treino entram em modo de tela cheia, ocultando as barras de sistema para uma experiência focada.
  - **Timer Circular:** Um grande anel de progresso visual indica o tempo restante, facilitando a leitura à distância.
  - **Botões Grandes com Ícones:** Controles de "Play", "Pause" e "Stop" são grandes e fáceis de acionar durante um exercício.

- **Persistência e Histórico:**
  - **Biblioteca de Circuitos:** Salve seus circuitos personalizados para reutilizá-los e editá-los quando quiser.
  - **Histórico de Treinos:** O aplicativo registra automaticamente cada treino que você realiza, com data, tipo e duração, permitindo que você acompanhe seu progresso.

- **Alertas e Feedback:**
  - **Sons e Vibração:** Alertas sonoros e de vibração indicam o início e o fim de cada intervalo, bem como a conclusão do treino.
  - **Tela Sempre Ligada:** A tela do dispositivo permanece ativa durante todo o treino, evitando interrupções.

---

## Guia de Build e Instalação

Siga os passos abaixo para compilar o projeto e instalá-lo em um emulador ou dispositivo físico.

### Pré-requisitos

- **Android Studio:** Versão mais recente recomendada (ex: Hedgehog ou superior).
- **JDK:** Versão 11 ou superior.

### 1. Processo de Build

1.  **Abra o Projeto:** Abra o Android Studio e selecione `File > Open`, navegue até a pasta raiz do projeto `Timer` e clique em `Open`.
2.  **Sincronização do Gradle:** Aguarde o Android Studio sincronizar o projeto com os arquivos Gradle. Isso pode levar alguns minutos e irá baixar todas as dependências necessárias.
3.  **Compilar o Projeto (Build):**
    - Vá para o menu `Build > Make Project` (ou use o atalho `Ctrl+F9`).
    - Alternativamente, você pode abrir o terminal integrado do Android Studio e executar o comando `./gradlew assembleDebug` para gerar o APK de depuração.

Se o processo for concluído sem erros, o aplicativo está pronto para ser instalado.

### 2. Instalação em um Emulador Android

1.  **Configure o Emulador (AVD):**
    - No Android Studio, vá para `Tools > AVD Manager`.
    - Se você não tiver um emulador, clique em `Create Virtual Device...` e siga o assistente. Recomenda-se uma imagem de sistema com **API 26 (Android 8.0)** ou superior.
2.  **Inicie o Emulador:** Na lista de dispositivos do AVD Manager, clique no ícone de "Play" ao lado do emulador desejado.
3.  **Selecione o Emulador:** Na barra de ferramentas principal do Android Studio, selecione o emulador que você acabou de iniciar no menu suspenso de dispositivos.
4.  **Execute o Aplicativo:** Clique no botão "Run 'app'" (o ícone de "Play" verde) ou use o atalho `Shift+F10`. O Android Studio irá compilar, instalar e iniciar o aplicativo automaticamente no emulador.

### 3. Instalação em um Dispositivo Físico

1.  **Ative as Opções de Desenvolvedor no Dispositivo:**
    - Vá para `Configurações > Sobre o telefone`.
    - Toque repetidamente na opção `Número da versão` (geralmente 7 vezes) até ver a mensagem "Você agora é um desenvolvedor!".
2.  **Ative a Depuração USB:**
    - Volte para o menu principal de `Configurações` e procure por `Opções do desenvolvedor` (pode estar dentro de `Sistema`).
    - Dentro das Opções do desenvolvedor, encontre e ative a opção `Depuração USB`.
3.  **Conecte o Dispositivo:** Conecte seu dispositivo Android ao computador usando um cabo USB.
4.  **Autorize a Conexão:** No seu dispositivo, uma caixa de diálogo "Permitir depuração USB?" aparecerá. Marque a opção "Sempre permitir neste computador" e toque em `Permitir`.
5.  **Selecione o Dispositivo:** No Android Studio, seu dispositivo físico deve aparecer no menu suspenso de dispositivos.
6.  **Execute o Aplicativo:** Clique no botão "Run 'app'" (o ícone de "Play" verde). O Android Studio irá compilar, instalar e iniciar o aplicativo diretamente no seu dispositivo.
