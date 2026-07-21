# BUILD — IZ Play V3 (Android)

## Pré-requisitos

| Componente | Versão verificada em 2026-07-21 |
|---|---|
| JDK | OpenJDK 21 — o `jbr` que acompanha o Android Studio serve |
| Android SDK | platform `android-36`, build-tools 36.1.0 |
| NDK | 27.0.12077973 |
| CMake | 3.22.1 (fixado em `app/build.gradle.kts`) |

O build nativo é obrigatório: o bridge JNI do MPV é compilado por CMake a partir
de `app/src/main/cpp/CMakeLists.txt`.

## Configuração local

`local.properties` é ignorado pelo Git e precisa ser criado uma vez em
`apps/android/`:

```properties
sdk.dir=C:/Users/<usuario>/AppData/Local/Android/Sdk
```

Use **barras normais**. Caminhos com `\` cru quebram o parser de properties do
Gradle (`java.io.IOException: Invalid file path`), porque `\U` é lido como
escape unicode inválido.

`JAVA_HOME` precisa apontar para um JDK. Se o sistema não tiver Java no PATH,
use o do Android Studio sem instalar nada:

```bash
export JAVA_HOME="C:\\Program Files\\Android\\Android Studio\\jbr"
```

## Bibliotecas nativas do MPV — atenção

`app/src/main/jniLibs/<abi>/libmpv.so` e `libmediakitandroidhelper.so`
**não estão no Git** (ver o `.gitignore` da pasta). São geradas por
`apps/android/Vendor/libmpv-android/Makefile`, que exige uma toolchain pesada.

Sem esses arquivos o build empacota um APK que **falha ao carregar o player em
runtime**. Um clone novo não compila um app funcional sem eles.

Mantenha um backup externo. Os hashes SHA-256 das cópias em uso estão em
`STATUS.md`.

## Comandos

```bash
cd apps/android

# APK debug
./gradlew.bat :app:assembleDebug

# testes unitários
./gradlew.bat :app:testDebugUnitTest

# ambos
./gradlew.bat :app:assembleDebug :app:testDebugUnitTest
```

Saída: `apps/android/app/build/outputs/apk/debug/IZPlay-V3-debug.apk`

## Configuração do produto

| Item | Valor |
|---|---|
| Nome do app | IZ Play |
| Nome do projeto | IZPlayV3 |
| Application ID / namespace | `com.izplay.v3` |
| Nome do artefato | `IZPlay-V3-<variant>.apk` |
| versionName | 3.0.0 |
| minSdk / targetSdk / compileSdk | 26 / 36 / 36 |
| ABIs | arm64-v8a, armeabi-v7a, x86_64 |

## Assinatura

Durante o desenvolvimento usa-se **apenas a assinatura debug**, gerada
automaticamente pelo Gradle.

**Não existe chave release neste projeto e nenhuma deve ser criada sem
autorização explícita.** Keystores, senhas e credenciais **nunca** entram no
Git. Quando chegar a fase de publicação, definir antes: responsável pela chave,
armazenamento seguro, plano de backup, proteção das senhas e configuração
segura do CI/CD.

## Compilar o upstream puro para comparação

Nunca sobrescreva o V3 para testar o upstream. Use um worktree separado:

```bash
git worktree add --detach C:/Users/<usuario>/izplay-base/up f71a552
# copie os .so do backup para apps/android/app/src/main/jniLibs/
# crie o local.properties do worktree
cd C:/Users/<usuario>/izplay-base/up/apps/android
./gradlew.bat :app:assembleDebug
```

Use um caminho **curto**. O monorepo contém caminhos longos em
`apps/ios/Vendor/libmpv/Frameworks/` que estouram o limite de 260 caracteres do
Windows e fazem o checkout falhar com `Filename too long`.

## Instalação

```bash
adb devices
adb install -r apps/android/app/build/outputs/apk/debug/IZPlay-V3-debug.apk
```

Instalar apenas em dispositivo autorizado. Testes de reprodução exigem servidor
e credenciais autorizados pelo usuário — ver `TESTING.md`.
