# FreeMyTV LDZ

**Uma versão personalizada e não oficial do Free My TV**, um launcher para Google TV e Android TV. O projeto original de [simbasjungles/free-my-tv](https://github.com/simbasjungles/free-my-tv) foi a inspiração e a base do código. Este projeto é mantido separadamente por Ludez e não é afiliado, patrocinado ou endossado pelos mantenedores do original.

> Free My TV é o projeto original de simbasjungles. “FreeMyTV LDZ” identifica esta compilação comunitária personalizada; não reivindicamos a marca ou o logotipo do projeto original.

## Capturas de tela

Capturas reais da versão 1.5 em uma TV TCL com Android TV. Use as setas do controle para navegar pelas categorias do menu.

<table>
  <tr>
    <td align="center"><strong>Tela inicial</strong><br><img src="docs/screenshots/home.png" alt="Home da FreeMyTV LDZ, com previsão do tempo, aplicativos e atalhos" width="420"></td>
    <td align="center"><strong>Tela de descanso</strong><br><img src="docs/screenshots/tela-descanso.png" alt="Tela de descanso com paisagem, hora, data e clima" width="420"></td>
  </tr>
  <tr>
    <td align="center"><strong>Ajustes · Tela inicial</strong><br><img src="docs/screenshots/ajustes-tela-inicial.png" alt="Categoria Tela inicial do menu de ajustes" width="420"></td>
    <td align="center"><strong>Ajustes · Aparência</strong><br><img src="docs/screenshots/ajustes-aparencia.png" alt="Categoria Aparência do menu de ajustes" width="420"></td>
  </tr>
  <tr>
    <td align="center"><strong>Ajustes · Clima</strong><br><img src="docs/screenshots/ajustes-clima.png" alt="Categoria Clima do menu de ajustes" width="420"></td>
    <td align="center"><strong>Ajustes · Sistema</strong><br><img src="docs/screenshots/ajustes-sistema.png" alt="Categoria Sistema do menu de ajustes" width="420"></td>
  </tr>
</table>

## Baixar

- **FreeMyTV LDZ 1.5:** baixe `FreeMyTV-LDZ-1.5.apk` nos [Releases](https://github.com/ludezam/FreeMyTV-LDZ/releases/latest).
- **Pacote com os três apps:** baixe `LDZ-TV-Apps.zip` nos Releases. Ele contém FreeMyTV LDZ, Tempo LDZ e Quadro LDZ como APKs independentes.
- Tempo LDZ: [projeto e código](https://github.com/ludezam/tempoLDZ).
- Quadro LDZ: [projeto, instruções e releases](https://github.com/ludezam/Quadro-LDZ).

O pacote não combina os aplicativos em um só: instale somente os APKs que quiser usar. Tempo 1.4 e Quadro 0.3.0 são builds de teste; consulte os repositórios de cada app para as limitações e instruções específicas.

## O que muda nesta versão

- Menu de configurações reorganizado nas categorias **Tela inicial**, **Aparência**, **Clima** e **Sistema**.
- Relógio fixo no topo da tela inicial.
- Remoção das opções Destaques, Widget tocando agora e Minhas fotos.
- Remoção do ícone decorativo de Wi‑Fi na tela de ajustes.
- Fundos de paisagem e ilustrações do clima incluídos no APK e disponíveis offline.
- Compatível com Android TV 9 ou superior; testado em TVs TCL com Android 9 e Android 11.

## Instalar ou atualizar

O APK é destinado a TVs Android/Google TV. Com depuração ADB ativada e a TV e o computador na mesma rede:

```powershell
adb connect IP_DA_TV:5555
adb install -r FreeMyTV-LDZ-1.5.apk
```

Na primeira instalação, selecione FreeMyTV LDZ no menu de apps e escolha **Free My TV** em **Tela inicial**. Se o Android perguntar, permita a depuração USB.

As versões LDZ são assinadas com uma chave privada de distribuição que não está neste repositório. APKs assinados por outra chave não atualizam esta instalação diretamente. O menu **Atualizações** consulta as releases oficiais de Free My TV, não as releases LDZ; atualizações LDZ são baixadas manualmente nesta página.

## Compilar o código

O build Android usa JDK 17, Android SDK com `platforms;android-34` e `build-tools;34.0.0`. Em Bash, na raiz do repositório:

```bash
./build.sh --generic --custom
```

O comando cria um APK genérico personalizado em `FreeMyTV.apk`. Uma compilação local usa uma chave própria; ela não substitui o APK publicado a menos que seja assinada com a mesma chave privada. **Nunca envie arquivos `.keystore` ou chaves privadas ao GitHub.**

## Créditos e licenças

- **Free My TV**, de [simbasjungles](https://github.com/simbasjungles/free-my-tv): código sob MIT. O aviso e a licença originais estão em [`LICENSE`](LICENSE).
- **Paisagens e ilustrações do clima:** geradas com IA pelo autor desta versão para o FreeMyTV LDZ. Consulte [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md) para o escopo das permissões dos recursos.
- **Inter:** licença SIL Open Font License 1.1 em `app/assets/fonts/OFL.txt`.
- **Material Symbols:** licença Apache 2.0 em `app/assets/symbols/LICENSE-material-symbols.txt`.

Veja também os projetos independentes [Tempo LDZ](https://github.com/ludezam/tempoLDZ) e [Quadro LDZ](https://github.com/ludezam/Quadro-LDZ).
