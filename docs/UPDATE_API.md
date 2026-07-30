# API de atualização — IZ Play

## Endpoint consumido pelo Android Mobile

```http
GET /api/client/updates/android-mobile?channel=production&versionCode=30000
```

O endpoint é público para leitura, não recebe credenciais do cliente e deve
responder apenas por HTTPS. O backend escolhe a versão mais recente publicada
para a combinação `platform + channel`.

## Resposta sem atualização

```json
{
  "updateAvailable": false,
  "platform": "android-mobile",
  "channel": "production"
}
```

## Resposta com atualização

```json
{
  "updateAvailable": true,
  "mandatory": false,
  "platform": "android-mobile",
  "channel": "production",
  "versionCode": 30001,
  "versionName": "3.0.1",
  "minimumVersionCode": 30000,
  "downloadUrl": "https://updates.izplay.tv/android-mobile/IZPlay-Mobile-3.0.1.apk",
  "sha256": "64-caracteres-hexadecimais-em-minusculas",
  "fileSize": 123456789,
  "releaseNotes": "Melhorias de estabilidade e correções no player."
}
```

## Política de versões

- `3.0.0` → `versionCode 30000`
- `3.0.1` → `versionCode 30001`
- `3.1.0` → `versionCode 30100`
- Nunca reutilizar ou reduzir um `versionCode` já distribuído.
- `mandatory=true` somente para falha grave de segurança ou incompatibilidade.
- `minimumVersionCode` define a menor versão ainda permitida.

## Publicação

1. Gerar APK `release` com a chave definitiva do IZ Play.
2. Calcular SHA-256 do arquivo final, sem modificá-lo depois.
3. Enviar o APK para armazenamento HTTPS.
4. Criar a versão no painel como rascunho.
5. Conferir tamanho, hash, certificado, canal e plataforma.
6. Publicar primeiro em `internal`, depois `reseller` e por último `production`.

## Validações feitas pelo aplicativo

- resposta pertence a `android-mobile` e ao canal configurado;
- `versionCode` é maior que o instalado;
- URL final usa HTTPS;
- SHA-256 tem formato válido e confere com o arquivo;
- o APK tem `applicationId com.izplay.mobile`;
- a versão interna do APK confere com o manifesto;
- o certificado Android é igual ao do aplicativo instalado.

O APK não é transportado pela API nem pelo Nostr. O manifesto apenas aponta
para o arquivo hospedado.
