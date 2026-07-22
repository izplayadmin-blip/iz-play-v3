# DESIGN SYSTEM — IZ Play V3

Fonte da identidade: **IZ Play V2** (`izplayadmin-blip/iz-play`, commit
`6f3b4b111feb5c8f51a6102f8c3a3821cd64f01c`), cuja pasta `design-system/`
contém a especificação escrita e os tokens oficiais.

Qualidade de composição inspirada conceitualmente no Streamix Android — **sem
copiar código**, ver `LICENSES.md`.

> Status: **implementado (Etapa 2)**. Os tokens e os 14 componentes existem em
> `com.izplay.v3.ui.design`, isolados das telas funcionais. Este documento
> continua sendo o contrato; a seção final lista onde cada peça mora no código.

---

## Regra de ouro

Nenhum valor visual pode ficar espalhado pelas telas. Cor, espaçamento, raio,
duração e escala de foco vêm sempre dos tokens. Proibido: cor hardcoded,
espaçamento duplicado, raio inconsistente, escala de foco divergente sem
justificativa, componente visualmente igual implementado duas vezes.

---

## Cores

### Marca e superfícies

| Token | Hex | Uso |
|---|---|---|
| `primary` | `#CC0000` | cor primária da marca, foco, ações principais |
| `primaryHover` | `#E60000` | hover/pressed da primária |
| `primarySoft` | `#FF3333` | realce suave, ícones sobre fundo escuro |
| `background` | `#000000` | fundo padrão do app |
| `backgroundDeep` | `#05070B` | fundo de áreas cinematográficas |
| `surface` | `#111111` | cards, painéis |
| `surface2` | `#181818` | superfície elevada |
| `surfaceHover` | `#1B1B1B` | superfície em hover |
| `border` | `#2A2A2A` | divisores e contornos |
| `textPrimary` | `#FFFFFF` | texto principal |
| `textSecondary` | `#A8A8A8` | texto secundário |
| `muted` | `#888888` | texto desabilitado, metadados |

### Os dois amarelos — papéis distintos e não intercambiáveis

Decisão registrada em 2026-07-21. Os tokens do V2 traziam apenas o amarelo
semântico; o amarelo de destaque foi definido para o V3.

| Token | Hex | Uso — **exclusivo** |
|---|---|---|
| `accentLive` | `#F5A623` | **destaque de conteúdo**: badge AO VIVO, marcador HOT, selo de destaque editorial |
| `warning` | `#F39C12` | **estado do sistema**: aviso, atenção, alerta não crítico |

Nunca usar `warning` para marcar conteúdo, nem `accentLive` para sinalizar
estado do sistema. Amarelo **nunca** é cor primária — a primária é `#CC0000`.

### Semânticos restantes

| Token | Hex |
|---|---|
| `success` | `#2ECC71` |
| `error` | `#E74C3C` |

---

## Tipografia

Família: **Inter** (ver obrigação de licença em `THIRD_PARTY_NOTICES.md` caso
seja empacotada). Fallback: fonte do sistema.

| Token | sp | Uso |
|---|---|---|
| `hero` | 48 | título do Hero |
| `title` | 40 | título de tela |
| `subtitle` | 24 | subtítulo |
| `category` | 20 | cabeçalho de fileira |
| `card` | 18 | título de card |
| `body` | 16 | corpo |
| `description` | 14 | descrição, sinopse |
| `caption` | 12 | metadados |

Pesos: 400 regular · 500 medium · 700 bold · 900 black.

## Espaçamento (dp)

`xxs 4` · `xs 8` · `sm 12` · `md 16` · `lg 24` · `xl 32` · `xxl 40` ·
`section 48` · `hero 64` · `screen 96`

## Raios (dp)

`sm 8` · `md 12` · `card 18` · `dialog 20` · `hero 24` · `pill` (totalmente arredondado)

## Elevação (dp)

`none 0` · `soft 4` · `medium 8` · `modal 16`

## Animação (ms)

`fast 150` · `normal 200` · `sidebar 250` · `max 300`

Evitar animações que prejudiquem TV boxes de baixo desempenho.

---

## Sistema de foco (Android TV)

**Regra principal do V2: todo item interativo tem foco visível. Foco invisível
nunca é aceitável.**

| Estado | Comportamento |
|---|---|
| Normal | sem destaque |
| **Focused** | escala maior + borda vermelha (ou realce luminoso) + contraste de texto aumentado |
| Pressed | feedback imediato |
| Disabled | opacidade reduzida; **não recebe foco** |
| Selected | indicação persistente, distinta de foco |
| Loading / Empty / Error | estados próprios, ver componentes |

### Escalas de foco

| Elemento | Escala |
|---|---|
| Card | 1.05 – 1.08 |
| Botão | 1.03 |
| Item de sidebar | 1.02 |

Divergir dessas escalas exige justificativa registrada.

---

## Sidebar

- Posição: **sempre à esquerda**. Nunca à direita, no topo ou embaixo.
- Largura expandida: **240dp** · recolhida: **72dp**
- Padding interno: **24dp**
- Transição: `sidebar` (250ms)

---

## Componentes centrais

Todos consomem os ViewModels e repositories herdados do Another. Se um
componente precisar de dados em formato diferente, cria-se um **adapter de UI** —
nunca um model de domínio, repository ou tabela duplicados.

| Componente | Papel |
|---|---|
| `IzTvCard` | card base com foco, escala e borda |
| `IzPosterCard` | pôster de filme/série, proporção 2:3 |
| `IzChannelCard` | canal ao vivo, logo + badge AO VIVO (`accentLive`) |
| `IzHero` | destaque principal com backdrop, gradiente e ações |
| `IzButton` | ação primária/secundária/terciária com estados de foco |
| `IzSidebar` | navegação lateral retrátil |
| `IzNavigationItem` | item da sidebar |
| `IzContentRow` | fileira horizontal com cabeçalho de categoria |
| `IzLoading` | carregamento e placeholder shimmer |
| `IzEmptyState` | estado vazio |
| `IzErrorState` | erro com ação de repetir |
| `IzDialog` | diálogo modal |
| `IzBadge` | selo (AO VIVO, HD, 4K, novo) |
| `IzProgressBar` | progresso de reprodução e de download |

## Especificação do Hero

A definir na implementação, com estes pontos obrigatoriamente resolvidos:
quantidade máxima de itens, critério de seleção, comportamento com D-pad,
rotação automática ou manual e intervalo, indicadores, botão assistir, botão de
detalhes, fallback sem backdrop, limite de pré-carregamento e comportamento em
aparelhos com pouca memória.

## Dados da Home

Sem dados falsos em produção. Não havendo curadoria editorial no servidor, as
fileiras usam regras locais simples, determinísticas e documentadas, baseadas
em: conteúdo recente, mais acessado localmente, metadados mais completos,
favoritos, histórico e conteúdo não concluído.

---

## Implementação (Etapa 2)

Pacote raiz: `com.izplay.v3.ui.design` — **isolado**. Não substitui o
`ui/theme/IZPlayTheme` que as telas funcionais herdadas usam; a troca do tema
das telas reais acontece tela a tela na Etapa 3.

| Arquivo | Conteúdo |
|---|---|
| `tokens/IzColor.kt` | cores da marca + os dois amarelos (`Live` / `Warning`) |
| `tokens/IzDimens.kt` | `IzSpacing`, `IzRadius`, `IzElevation`, `IzBorder`, `IzSize` |
| `tokens/IzMotion.kt` | durações, `IzFocusScale`, `IzOpacity` |
| `tokens/IzType.kt` | escala tipográfica (Inter → fonte do sistema por ora) |
| `tokens/IzGradient.kt` | scrims do Hero e placeholder |
| `IzTheme.kt` | tema Material 3 dark derivado dos tokens (para previews) |
| `focus/IzFocus.kt` | `izFocusVisuals` — escala + borda vermelha no foco de TV |
| `components/*.kt` | os 14 componentes `Iz*` |

Cada componente traz um `@Preview`. Uma galeria de inspeção existe **apenas no
source set `debug`** (`debug/.../IzGalleryActivity.kt`, não entra em release),
iniciável por `adb shell am start -n com.izplay.v3/.debug.IzGalleryActivity`.

### Estado do foco de TV

`izFocusVisuals` aplica escala animada (150ms) + borda vermelha de 2dp ao foco,
lendo a `MutableInteractionSource` do próprio item. Verificado em dispositivo
real (rk322x, API 25): o botão secundário focado exibe a borda vermelha e a
escala. Observação: a borda vermelha sobre o botão **primário** (também
vermelho) tem baixo contraste — para o primário, o realce de foco confia na
escala; avaliar um anel de foco claro para o primário na Etapa 3/4.

### Pendências herdadas da spec para a Etapa 3

- Parâmetros do carrossel do Hero (nº de itens, rotação, indicadores,
  pré-carregamento, comportamento em pouca memória) — resolver ao montar a Home.
- Empacotar a fonte Inter (obrigação SIL OFL — ver `THIRD_PARTY_NOTICES.md`) ou
  manter a fonte do sistema.
