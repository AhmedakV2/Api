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

## Durum yonetimi

Oturum kilidi, hiz siniri, WebSocket bileti, cihaz kaydi ve arac sonucu dagitimi
uygulama surecinin kendi belleginde tutulur. Harici bir sunucu gerekmez.

Tum kayitlar yasam suresi (TTL) tasir ve suresi dolanlar arka planda temizlenir.
Uygulama yeniden baslatildiginda bu kayitlar silinir; en belirgin etkisi aktif
hesap kilitlerinin ve hiz siniri sayaclarinin sifirlanmasidir.

Bu tasarim tek uygulama ornegi icindir. Birden fazla ornek calistirilacagi zaman
KeyValueStore, CounterStore ve ToolResultBus arayuzlerine paylasimli bir
saglayici eklenmesi gerekir; tuketici siniflar degismez.

## Sunucuda calistirma

```
svn checkout <SVN_BASE_URL>/current /opt/aft-api
cd /opt/aft-api
cp config/aft.env.example config/aft.env
# config/aft.env icindeki AFT_JWT_SECRET, AFT_DB_* ve AFT_REDIS_* degerlerini doldurun
bin/start.sh
```

`AFT_JWT_SECRET` en az 32 karakter olmalidir, aksi halde uygulama acilista durur.

## Dagitik durum deposu

Uygulama oturum kilidi, hiz siniri, WebSocket bileti, cihaz kaydi ve araclar arasi
mesajlasma icin Valkey kullanir. Valkey, Redis protokolunu konusan BSD-3 lisansli
acik kaynak bir sunucudur; Redis ve protokol uyumlu diger sunucular da calisir.

| Degisken | Aciklama |
|---|---|
| `AFT_STATE_PROVIDER` | `valkey` |
| `AFT_VALKEY_HOST` / `AFT_VALKEY_PORT` | Valkey adresi, varsayilan 127.0.0.1:6379 |
| `AFT_VALKEY_PASSWORD` | `requirepass` ile tanimlanan parola |
| `AFT_VALKEY_DB` | Veritabani indeksi, varsayilan 0 |
| `AFT_VALKEY_PREFIX` | Anahtar oneki, varsayilan `aft:` |

Kullanilan anahtar gruplari: `aft:ws-ticket:*`, `aft:ws-device:*`, `aft:login-fail:*`,
`aft:login-lock:*`, `aft:rate-limit:*` ve `aft:tool:result` kanali. Tum anahtarlar TTL
tasir, kalicilik gerekmez.

### Rocky Linux 8 kurulumu

```
sudo dnf install -y epel-release
sudo dnf search valkey
sudo dnf install -y valkey
```

Depoda valkey bulunmazsa Redis 6.2 de kullanilabilir; RHEL 8 AppStream surumu BSD-3
lisanslidir ve protokol uyumludur:

```
sudo dnf module install -y redis:6
```

Onerilen yapilandirma (`/etc/valkey/valkey.conf` veya `/etc/redis.conf`):

```
bind 127.0.0.1
requirepass <parola>
maxmemory 256mb
maxmemory-policy volatile-ttl
save ""
appendonly no
```

```
sudo systemctl enable --now valkey
```

Durdurmak icin `bin/stop.sh`. Loglar `logs/aft-api.out` altindadir.
