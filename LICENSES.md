# LICENSES — IZ Play V3

Situação de licenciamento de cada projeto envolvido e as obrigações que o IZ
Play V3 assume. Avisos de dependências de terceiros ficam em
`THIRD_PARTY_NOTICES.md`.

---

## 1. Another IPTV Player — MIT ✅ base do código

- Repositório: https://github.com/bsogulcan/another-iptv-player
- Licença: **MIT formal**, arquivo `LICENSE` presente na raiz deste repositório
- Copyright: `Copyright 2025 Another IPTV Player`
- Commit-base adotado: `f71a552ff158be95639a9afe6dc00dd6fabb43d8`

**Obrigações assumidas:**

1. O arquivo `LICENSE` na raiz **é preservado sem alteração**. Ele não foi
   sobrescrito nem terá o copyright removido.
2. O aviso de copyright e a permissão MIT acompanham qualquer redistribuição do
   IZ Play V3, inclusive em binários.
3. Arquivos derivados mantêm seus cabeçalhos de origem. A renomeação de pacote
   para `com.izplay.v3` **não remove autoria** — o histórico Git preserva a
   linhagem completa e o remote `upstream` permanece configurado.

O IZ Play V3 é uma obra derivada do Another IPTV Player. Isso é permitido pela
MIT, inclusive para uso comercial e sublicenciamento, desde que o aviso acima
seja mantido.

## 2. Streamix Android — ⚠️ SEM licença efetiva

- Repositório: https://github.com/gabrielmaialva33/streamix-android
- Commit verificado: `05325803e9867579852931a12241d2e0b9f029e1`

**Verificação feita em 2026-07-21 (clone read-only):** o `README.md` exibe um
badge "license MIT" que aponta para `./LICENSE` e o rodapé diz "MIT — Part of
the Streamix project", **mas o arquivo `LICENSE` não existe no repositório**. O
link do badge está quebrado. O GitHub não detecta licença.

**Tratamento adotado: obra sem licença.** Sem concessão expressa, o padrão legal
é "todos os direitos reservados".

Portanto, **é proibido no IZ Play V3**:

- copiar arquivos, trechos substanciais de código ou assets
- copiar nomes, marcas ou identidade visual
- importar módulos ou declarar dependência

**É permitido:** estudar o projeto e reimplementar comportamentos e ideias com
código próprio. Ideias, layouts e conceitos funcionais não são protegidos por
direito autoral — a expressão específica em código é.

Se o autor adicionar um `LICENSE` formal ou conceder autorização expressa por
escrito, esta seção deve ser revista e a autorização arquivada.

## 3. IZ Play V2 — projeto próprio, sem licença declarada

- Repositório: https://github.com/izplayadmin-blip/iz-play
- Commit verificado: `6f3b4b111feb5c8f51a6102f8c3a3821cd64f01c`
- Não há arquivo `LICENSE`; o GitHub não detecta licença

Por ser projeto do mesmo titular, a documentação de design e os assets internos
podem servir como fonte da identidade do V3.

⚠️ **Ressalva importante:** não presumir que todo conteúdo do V2 foi criado
internamente. Antes de reutilizar qualquer asset, fonte, ícone ou dependência,
verificar autoria, origem, licença, permissão de distribuição e obrigação de
atribuição.

**Auditoria pendente** — os clones foram rasos (`--depth 1`), o que impede
rastrear autoria pelo histórico. Itens a auditar antes do uso:

| Asset | Situação |
|---|---|
| `tv_banner.png` (320×180) | autoria a confirmar |
| `izplay_logo_login.png` (420×141) | autoria a confirmar |
| `ic_launcher_foreground.png` (432×432) e mipmaps | autoria a confirmar |
| Fonte **Inter** | referenciada por nome; nenhum arquivo de fonte no repo. Se empacotada no Android, é SIL OFL 1.1 e exige aviso — ver `THIRD_PARTY_NOTICES.md` |

Tokens numéricos (cores, espaçamentos, raios, durações) não são obra protegível
e podem ser adotados livremente.

## 4. Prysm — MIT

- Repositório: https://github.com/dereferencex/prysm
- Commit: `99b815c92fd87ca451cb949adfb31ee3728d2c2c`

Licença MIT formal. Ainda assim será usado **somente como referência conceitual**
de D-pad e Android TV: é React Native/Expo/TypeScript e nada dele é importável
para um projeto Compose nativo. Se algum trecho vier a ser adaptado, o aviso MIT
correspondente deve ser adicionado a `THIRD_PARTY_NOTICES.md`.

## 5. IZ Play V3 — licença a definir

O V3 é obra derivada de um projeto MIT e **deve declarar sua própria licença
antes da primeira distribuição pública**. Enquanto isso não for decidido, o
projeto permanece sem licença própria declarada e a obrigação MIT do Another
continua valendo integralmente.

Decisão pendente do responsável pelo projeto.

---

## Procedimento antes de copiar qualquer código ou asset

1. verificar a licença do repositório de origem
2. registrar as obrigações neste arquivo
3. preservar copyrights e atribuições existentes
4. identificar dependências transitivas relevantes
5. atualizar `THIRD_PARTY_NOTICES.md`

Sem licença formal: não copiar arquivos, usar apenas como referência,
reimplementar com código próprio, solicitar autorização quando necessário.
