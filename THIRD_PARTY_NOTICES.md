# THIRD PARTY NOTICES — IZ Play V3

Componentes de terceiros distribuídos com o IZ Play V3 ou usados na sua
construção. Atualizar sempre que uma dependência for adicionada, removida ou
atualizada.

Estado desta versão: levantamento inicial da Etapa 0. **Há um item crítico em
aberto — ver "libmpv" abaixo.**

---

## ⚠️ CRÍTICO — libmpv e bibliotecas nativas

O aplicativo embarca bibliotecas nativas pré-compiladas em
`apps/android/app/src/main/jniLibs/<abi>/`:

| Arquivo | ABIs | Origem |
|---|---|---|
| `libmpv.so` | arm64-v8a, armeabi-v7a, x86_64 | mpv / media-kit |
| `libmediakitandroidhelper.so` | arm64-v8a, armeabi-v7a, x86_64 | media-kit |

Essas bibliotecas **não estão versionadas** (ver `apps/android/app/src/main/jniLibs/.gitignore`);
são produzidas por `apps/android/Vendor/libmpv-android/Makefile`.

**Obrigação em aberto, a resolver antes de qualquer distribuição pública:**

O mpv é distribuído sob **GPLv2+ ou LGPLv2.1+**, dependendo das opções de
compilação, e agrega FFmpeg (LGPLv2.1+ ou GPL conforme os codecs habilitados),
além de outras bibliotecas. As consequências são materiais:

- Se o `libmpv.so` embarcado for **GPL**, o aplicativo inteiro fica sujeito à
  GPL na distribuição — incluindo obrigação de disponibilizar o código-fonte.
- Se for **LGPL**, a distribuição é possível sem abrir o app, mas exigem-se
  o aviso de licença, o texto da LGPL, a indicação das modificações e a
  possibilidade de substituir a biblioteca (o que o `.so` separado já atende).

**Pendências obrigatórias:**

1. Determinar as flags exatas de build do `libmpv.so` em uso (`--enable-gpl`?
   quais codecs do FFmpeg?)
2. Registrar aqui a licença resultante e o conjunto de bibliotecas agregadas
3. Incluir no aplicativo uma tela ou arquivo de avisos de licença acessível ao
   usuário final
4. Preparar a oferta de código-fonte, se aplicável

Isso é herdado do Another IPTV Player e vale igualmente para ele. Não é um
problema introduzido pelo IZ Play V3, mas é responsabilidade de quem distribui.

---

## Código base

**Another IPTV Player** — MIT — https://github.com/bsogulcan/another-iptv-player

```
Copyright 2025 Another IPTV Player

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
of the Software, and to permit persons to whom the Software is furnished to do
so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

O texto integral está em `LICENSE`, na raiz, e não deve ser alterado.

---

## Dependências Android

Todas declaradas em `apps/android/gradle/libs.versions.toml`.

### Apache License 2.0

| Componente | Grupo |
|---|---|
| AndroidX Core KTX | `androidx.core` |
| AndroidX Lifecycle (runtime, runtime-compose, viewmodel-compose) | `androidx.lifecycle` |
| AndroidX Activity Compose | `androidx.activity` |
| AndroidX Navigation Compose | `androidx.navigation` |
| Jetpack Compose (BOM, ui, ui-graphics, ui-tooling, material3, material-icons-extended) | `androidx.compose.*` |
| AndroidX Room (runtime, ktx, compiler) | `androidx.room` |
| AndroidX WorkManager | `androidx.work` |
| AndroidX DocumentFile | `androidx.documentfile` |
| AndroidX Test / Espresso | `androidx.test.*` |
| Kotlin, Kotlin Serialization, KSP | `org.jetbrains.kotlin*`, `com.google.devtools.ksp` |
| kotlinx.serialization JSON | `org.jetbrains.kotlinx` |
| OkHttp + logging-interceptor | `com.squareup.okhttp3` |
| Coil Compose | `io.coil-kt` |
| Android Gradle Plugin | `com.android.tools.build` |

Texto da Apache 2.0: https://www.apache.org/licenses/LICENSE-2.0

### Eclipse Public License 1.0

| Componente | Grupo |
|---|---|
| JUnit 4 (apenas testes, não distribuído no APK) | `junit` |

---

## Fontes tipográficas

**Nenhuma fonte é empacotada no aplicativo nesta versão.**

O design system do IZ Play V2 especifica **Inter**. Se ela vier a ser
empacotada, é licenciada sob **SIL Open Font License 1.1** e passa a exigir:

- inclusão do texto da OFL junto ao aplicativo
- preservação do aviso de copyright da fonte
- proibição de vender a fonte isoladamente

Enquanto não houver empacotamento, o app usa a fonte do sistema e não há
obrigação adicional.

---

## Assets visuais

Nenhum asset de terceiros foi incorporado até aqui. Os ícones atuais são o
adaptive icon genérico do template do Android Studio.

Assets do IZ Play V2 candidatos a uso (logo, banner de TV, ícones) estão
**pendentes de auditoria de autoria** — ver `LICENSES.md`, seção 3.

---

## Não incorporado

Nenhum código, asset ou dependência do **Streamix Android** foi incorporado, em
razão da ausência de licença formal. Nenhum código funcional do **IZ Play V2**
foi incorporado.
