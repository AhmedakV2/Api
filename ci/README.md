# Derleme ve Yayin Hatti

GitLab kaynak kod deposu, Jenkins derleyici, SVN ise calisir paket deposudur.

```
GitLab (kaynak) --webhook--> Jenkins (derleme + test + paketleme) --> SVN (calisir paket)
```

## Jenkins agent gereksinimleri

Klasik bir node uzerinde calisir, Docker gerekmez.

| Gereksinim | Aciklama |
|---|---|
| JDK 25 | Manage Jenkins > Tools > JDK installations, ad: `jdk-25` |
| Maven 3.9+ | Manage Jenkins > Tools > Maven installations, ad: `maven-3.9` |
| `svn` istemcisi | Agent uzerinde PATH icinde olmali (`svn --version`) |
| `bash` | Linux agent varsayilir. Windows agent kullanilacaksa Git Bash PATH'te olmali. |

`tools` blogundaki isimler Jenkins'teki tanimlarla birebir ayni olmalidir.

## Jenkins kimlik bilgisi

| Alan | Deger |
|---|---|
| Tur | Username with password |
| ID | `aft-svn` |
| Kullanici / parola | SVN commit yetkisi olan hesap |

Farkli bir ID kullanacaksaniz `Jenkinsfile` icindeki `SVN_CREDENTIALS_ID` degerini guncelleyin.

## Jenkins is tanimi

1. New Item > Multibranch Pipeline (veya tek dal icin Pipeline).
2. Kaynak: GitLab deposu, Jenkinsfile yolu `Jenkinsfile`.
3. Build Triggers: GitLab webhook.

## GitLab webhook

1. GitLab > Settings > Webhooks.
2. URL: `https://<jenkins>/project/<is-adi>` (Multibranch icin `https://<jenkins>/multibranch-webhook-trigger/invoke?token=<token>`).
3. Tetikleyiciler: Push events, Merge request events.

## Jenkinsfile parametreleri

| Parametre | Varsayilan | Etki |
|---|---|---|
| `PUBLISH_TO_SVN` | true | Paketi SVN `current` dizinine gonderir. Yalnizca `RELEASE_BRANCH` dalinda calisir. |
| `TAG_IN_SVN` | false | SVN `tags` altinda kalici kopya olusturur. Paket ~185 MB oldugu icin yalnizca surum cikislarinda isaretleyin. |
| `RUN_INTEGRATION_TESTS` | false | Testcontainers testlerini calistirir, agent uzerinde Docker erisimi gerektirir. |

`Jenkinsfile` icindeki `SVN_BASE_URL` ve `RELEASE_BRANCH` degerlerini kendi ortaminiza gore guncelleyin.

## Test ayrimi

Testcontainers gerektiren testler `integration` etiketi tasir ve varsayilan `mvn test` kosusunda dislanir.

```
mvn test                        # 75 birim testi, Docker gerekmez
mvn test -Pintegration-tests    # birim + entegrasyon testleri, Docker gerekir
```

## SVN duzeni

```
<SVN_BASE_URL>/
  current/                       her basarili derlemede uzerine yazilir
  tags/aft-api-<versiyon>-b<N>/  TAG_IN_SVN isaretliyse olusan kalici kopya
```

Paket icerigi:

```
app.jar
BUILD-INFO.txt          versiyon, build numarasi, commit, derleme zamani
SHA256SUMS
bin/start.sh stop.sh start.bat stop.bat
config/aft.env.example
config/application-prod.yml
db/migration/*.sql
logs/
```

## Sunucuda calistirma

```
svn checkout <SVN_BASE_URL>/current /opt/aft-api
cd /opt/aft-api
cp config/aft.env.example config/aft.env
# config/aft.env icindeki AFT_JWT_SECRET, AFT_DB_* ve AFT_REDIS_* degerlerini doldurun
bin/start.sh
```

`AFT_JWT_SECRET` en az 32 karakter olmalidir, aksi halde uygulama acilista durur.

Durdurmak icin `bin/stop.sh`. Loglar `logs/aft-api.out` altindadir.
