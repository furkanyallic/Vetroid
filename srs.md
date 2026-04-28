





&#x20;                                   























BM314-YAZILIM MÜHENDİSLİĞİ 







VETROID  

Android Otomasyon Uygulaması SRS Raporu









&#x09;

22118080025 - Bedirhan Yiğit

22118080070 - Furkan Yallıç











Revision Table



Versiyon	Tarih	Açıklama	Yazar

1.0	09.03.2026	Proje Yönetim Planı (SPMP) taslağı oluşturuldu; projenin kapsamı, teknik altyapısı ve görev dağılımı tanımlandı.	Bedirhan Yiğit,Furkan Yallıç

1.1	08.04.2026	Yazılım Gereksinim Belgesi (SRS) oluşturuldu; fonksiyonel gereksinimler, dış arayüzler ve sistem modları detaylandırıldı.	Bedirhan Yiğit,Furkan Yallıç

&#x09;		































İçindekiler

1\. GEREKSİNİMLER	5

1.1 Gerekli Durum ve Modlar	5

1.2 YKE Fonksiyonel Gereksinimleri	5

1.2.1 Senaryo Yönetimi Uygulama:	5

1.2.2 Tetikleyici (Trigger) İzleme Fonksiyonelliği	6

1.2.3 Kısıtlama (Constraint) Kontrol Fonksiyonelliği	6

1.2.4 Eylem (Action) Yürütme Fonksiyonelliği Doğrulanan	6

1.3 YKE Dış Arayüz Gereksinimleri	7

1.3.1 Arayüz Tanımlaması ve Diyagramları	7

1.3.2 IF-01 Kullanıcı Arayüzü (UI) Gereksinimleri	8

1.3.3 IF-02 Android İşletim Sistemi API Gereksinimleri	9

1.3.4 IF-03 Konum ve Harita Servis Gereksinimleri	9

1.3.5 IF-04 Mesajlaşma Servisi	9

1.4 YKE Dahili Arayüz Gereksinimleri	10

1.4.1 Kullanıcı Arayüzü ve İş Mantığı Haberleşmesi:	10

1.4.2 Tetikleyici ve Senaryo Motoru Entegrasyonu:	10

1.4.3 Senaryo Motoru ve Yürütücü Modül Haberleşmesi:	10

1.4.4 Veri Erişim Arayüzü:	10

1.5 YKE Dahili Veri Gereksinimleri	10

•	1.5.1 Senaryo Veritabanı Yapısı	10

1.5.2 Olay Kayıtları (Log Data	10

1.5.3 Veri Bütünlüğü ve İzolasyonu	10

1.6 Uyarlama Gereksinimleri	11

1.6.1 Kullanıcı Tanımlı Parametreler	11

1.6.2 Donanım ve Sürüm Uyumluluğu	11

1.6.3 Dil ve Bölge Ayarları	11

1.7 Emniyet Gereksinimleri	11

1.7.1 Kaynak Tüketimi ve Isınma Kontrolü	11

1.7.2 Kararsız Durum Koruması	11

1.7.3 Acil Durum Esnekliği	11

1.8 Güvenlik ve Gizlilik Gereksinimleri	11

1.9 YKE Ortam Gereksinimleri	12

1.10 Bilgisayar Kaynak Gereksinimleri	12

1.10.3 Bilgisayar Yazılım Gereksinimleri	13

1.10.4 Bilgisayar İletişim Gereksinimleri	13

1.11 Yazılım Kalite Faktörleri	13

1.12 Tasarım ve Uygulama Kısıtlamaları	13

1.13 Personelle İlgili Gereksinimler	13

1.14 Eğitimle İlgili Gereksinimler	13

1.15 Lojistikle İlgili Gereksinimler	14

1.16 Diğer Gereksinimler	14

1.17 Ambalajlama Gereksinimleri	14

1.18 Gereksinimlerin Önceliği ve Kritikliği	14































1\. GEREKSİNİMLER

1.1 Gerekli Durum ve Modlar

Vetroid, Android işletim sistemi üzerinde sürekli çalışan bir arka plan servisi mimarisine sahiptir. Uygulama, kullanıcının etkileşim düzeyine ve sistem kaynaklarının durumuna göre aşağıdaki dört ana moda sahiptir:

Mod / Durum	Açıklama	Geçiş Koşulu

Bekleme (Idle)	Uygulama ön planda açık değil; arka plan servisi aktif ve tetikleyicileri dinliyor.	Uygulama arka plana alındığında veya ekran kapandığında otomatik girilir.

Aktif (Active)	Kullanıcı arayüzüne erişim sağlanıyor; senaryo oluşturma, düzenleme ve listeleme işlemleri yapılabilir.	Kullanıcı uygulamayı açtığında girilir.

Yürütme (Executing)	Bir tetikleyici koşulu karşılandı,ilgili aksiyon zinciri sisteme uygulanır.	Tetikleyici algılama servisi koşusu onayladığında otomatik girilir.

Durdurulmuş (Suspended)	Sistem kaynak kısıtlaması veya kullanıcı tarafından manuel durdurma nedeniyle servis geçici olarak devre dışı.	Kullanıcı senaryo akışını  durdurduğunda veya işletim sistemi servisi sonlandirdığında girilir.



Her gereksinim veya gereksinim grubu, bu dört moddan biriyle ilişkilendirilmiştir. Modlar arası geçişler, Android işletim sistemi yaşam döngüsü olaylarından (onResume, onPause, Foreground Service bildirimleri) ve kullanıcı eylemlerinden tetiklenir.

1.2 YKE Fonksiyonel Gereksinimleri

Bu bölüm, Vetroid uygulamasının beş temel modülü kapsayan fonksiyonel gereksinimlerini tanımlar. Her alt başlık bir fonksiyonellik grubuna karşılık gelir.

1.2.1 Senaryo Yönetimi Uygulama:Kullanıcının "Eğer X olursa Y yap" mantığında kural tabanlı senaryolar oluşturmasını, düzenlemesini ve silmesini sağlayan merkezi bir yönetim birimi sunacaktır.

Kullanıcı, en az bir tetikleyici ve en az bir aksiyon seçerek yeni bir senaryo oluşturabilmelidir.

Oluşturulan her senaryo; ad, tetikleyici listesi, koşul listesi (opsiyonel) ve aksiyon listesini içerecek şekilde Room/SQLite veritabanında kalıcı olarak saklanmalıdır.

Kullanıcı mevcut senaryoları listeleyebilmeli, düzenleyebilmeli ve tek tıkla silebilmelidir.

Bir senaryo devre dışı bırakılabilmeli; bu durumda tetikleyicileri dinlenmemeli ancak veritabanında saklanmaya devam etmelidir.

Aynı anda birden fazla aktif senaryo bulunabilir; çakışan aksiyonlar öncelik sıralamasına göre çözümlenmelidir

1.2.2 Tetikleyici (Trigger) İzleme Fonksiyonelliği Sistem, aşağıdaki olayları gerçek zamanlı veya periyodik olarak izlemekle yükümlüdür:

Zaman ve Takvim: Belirli saat dilimleri, haftalık tekrarlar veya özel tarih periyotlarının takibi.

Konum Servisleri: Coğrafi koordinatlar üzerinden belirlenen dairesel bölgelere giriş ve çıkış anlarının tespiti.

Cihaz Etkinlikleri: Yeni bildirim alınması veya cihazın ses profilinin değiştirilmesi gibi yazılımsal olayların yakalanması.

Pil ve Güç Yönetimi: Pil yüzdesinin belirli eşiklere ulaşması veya şarj cihazı bağlantı durumunun izlenmesi.

Bağlantı ve Donanım: Kulaklık takılması/çıkarılması ve Bluetooth cihaz eşleşme durumlarının tespiti.

Sensör Verileri (Opsiyonel): Cihazın hareket veya ışık sensörlerinden gelen verilerin eşik değerlerle karşılaştırılması.

1.2.3 Kısıtlama (Constraint) Kontrol Fonksiyonelliği Bir tetikleyici oluştuğunda, sistem eyleme geçmeden önce aşağıdaki mantıksal doğrulamaları yapmalıdır:

Zaman ve Tarih Kısıtları: Eylemin sadece belirli günlerde veya saat aralıklarında çalışmasının kontrolü.

Konum Kontrolü: Kullanıcının o an belirli bir bölgede olup olmadığının teyidi.

Medya ve İletişim Durumu (Opsiyonel): Kulaklık takılı olması veya aktif bir telefon görüşmesi yapılıyor olması gibi durumların kontrolü.

Cihaz Modu Kontrolü: Telefonun mevcut ses modu veya pil seviyesinin eylem için uygunluğunun sorgulanması.

1.2.4 Eylem (Action) Yürütme Fonksiyonelliği Doğrulanan senaryolar dahilinde sistem şu eylemleri gerçekleştirmelidir:

Haberleşme: Tanımlı durumlarda otomatik SMS gönderimi.

Üçüncü Taraf Uygulama Entegrasyonu (Opsiyonel): Belirli mesajlaşma uygulamalarının (örn: WhatsApp) ilgili kişiye odaklanarak başlatılması.

Uygulama Yönetimi: Belirlenen uygulamaların otomatik olarak başlatılması veya ana ekrana yönlendirme yapılması.



Sistem Ayarları: Wi-Fi ve Bluetooth durumlarının değiştirilmesi; ses seviyesi ve ekran parlaklığının dinamik ayarlanması.

Kullanıcı Bilgilendirme: Ekranda anlık mesaj (Toast/Dialog) gösterilmesi



1.3 YKE Dış Arayüz Gereksinimleri

1.3.1 Arayüz Tanımlaması ve Diyagramları

Vetroid Yazılım Konfigürasyon Elemanı (YKE), işlevlerini yerine getirmek için kullanıcılar, cihaz donanım bileşenleri ve harici yazılım servisleri ile etkileşim halindedir. Bu etkileşimler; veri sağlama, komut gönderme ve sistem olaylarını dinleme prensiplerine dayanır.

Sistemin dış dünya ile olan veri alışverişini ve kullanıcı rollerini gösteren diyagramlar aşağıda sunulmuştur:

&#x20;

Diyagram 1: Sistem Bağlam Diyagramı

&#x20;

Diyagram 2: Kullanım Durumu Diyagramı 



Vetroid'in harici arayüzleri aşağıdaki tabloda tanımlanmıştır:

Arayuz ID	Arayuz Adi	Elaman Turu	Aciklama

IF-01	Kullanici Arayuzu (UI)	Insan-Sistem	Kullanicinin senaryo olusturup yonettigi Android arayuzu

IF-02	Android OS API'leri	Yazilim-Sistem	AlarmManager, BroadcastReceiver, Foreground Service, UsageStatsManager

IF-03	Konum Servisi	Yazilim-Donanim	GPS/Wi-Fi hibrit konum ve Geofencing API

IF-04	Mesajlasma Servisleri	Yazilim-Dis Sistem	Android SMS API ve WhatsApp Intent mekanizmasi

IF-05	Room / SQLite DB	Yazilim-Veri	Senaryolarin ve gecmisin kalici olarak depolandigi yerel veritabani



1.3.2 IF-01 Kullanıcı Arayüzü (UI) Gereksinimleri

Bu arayüz, kullanıcının sistemle etkileşime girdiği grafiksel arabirimi temsil eder.

•	a. Öncelik: Yüksek. Sistemin tüm yönetim işlevleri bu arayüze bağımlıdır.

•	b. Arayüz Tipi: Gerçek zamanlı grafik kullanıcı arayüzü (GUI).

•	c. Veri Elemanı Özellikleri:

Senaryo Adı: Karakter dizisi (Alfanümerik), maks. 50 karakter.

Tetikleyici/Aksiyon Parametreleri: Kullanıcı tarafından seçilen liste elemanları ve metin girişleri.

•	d. Görsel Özellikler: Kullanıcıyı yönlendiren ikonlar, durum bildirimleri (Toast) ve senaryo listeleri.

1.3.3 IF-02 Android İşletim Sistemi API Gereksinimleri

YKE'nin arka plan servisleri ve sistem olaylarını yakalamak için kullandığı teknik arayüzdür.

a. Öncelik: Kritik. Uygulamanın tetiklenme mekanizmasını sağlar.

b. Arayüz Tipi: API çağrısı ve Broadcast (yayın) dinleme.

e. İletişim Yöntemleri: Android Intent mekanizması ve sistem servisleri (AudioManager, WifiManager).

f. Protokoller: Android SDK standart iletişim protokolleri. \[Kapsanmayan eleman] Android OS bir sistem olayı (pil düşük vb.) fırlatırsa, YKE tanımlı senaryoyu kontrol edecektir.

1.3.4 IF-03 Konum ve Harita Servis Gereksinimleri

Konum tabanlı otomasyonlar için donanım seviyesinde veri sağlayan arayüzdür.

a. Öncelik: Yüksek (Konum tabanlı senaryolar için).

c. Bireysel Veri Elemanları:

o	Koordinat: Enlem ve boylam (Double), 6 ondalık basamak kesinlik.

o	Yarıçap: Metre cinsinden tam sayı (Örn: 100-500m).

e. İletişim: Google Play Services Geofencing API üzerinden asenkron callback (geri çağırma) yöntemi.

1.3.5 IF-04 Mesajlaşma Servisi 

SMS gönderimi ve WhatsApp gibi harici uygulamalara veri aktarımı sağlayan arayüzdür.

a. Öncelik: Orta (Opsiyonel aksiyonlar).

c. Veri Elemanları: Telefon numarası (E.164 formatı), Mesaj içeriği (String).

&#x20; e. İletişim Yöntemi: SMS için SmsManager API'si; WhatsApp için Intent mekanizması. \[Kapsanmayan eleman] WhatsApp uygulaması kurulu değilse, YKE kullanıcıya hata bildirimi yapacaktır.



1.4 YKE Dahili Arayüz Gereksinimleri

Bu bölüm, uygulamanın katmanlı mimarisini oluşturan alt modüllerin birbirleriyle olan veri paylaşımını ve haberleşme yöntemlerini belirtir.

1.4.1 Kullanıcı Arayüzü ve İş Mantığı Haberleşmesi: Kullanıcı eylemleri (senaryo oluşturma, silme, düzenleme), arayüz katmanından iş mantığı katmanına (ViewModel) asenkron olarak iletilmelidir. Sonuçlar, arayüz bileşenlerine reaktif veri yapıları aracılığıyla geri bildirilmelidir.

1.4.2 Tetikleyici ve Senaryo Motoru Entegrasyonu: Algılanan her tetikleyici olayı (zaman, konum veya sistem olayı), ilgili tetikleyici modülü tarafından merkezi senaryo motoruna (Scenario Engine) iletilmelidir. İletilen veri; tetikleyici türü ve ilgili senaryo tanımlayıcısını içermelidir.

1.4.3 Senaryo Motoru ve Yürütücü Modül Haberleşmesi: Tanımlı koşulların doğrulanması durumunda, senaryo motoru yürütülecek aksiyon listesini yürütücü modüle (Action Executor) aktarmalıdır. Yürütücü modül, işlemin sonucunu (başarı/hata) kayıt yönetimi birimine (Log Manager) bildirmekle yükümlüdür.

1.4.4 Veri Erişim Arayüzü: Tüm modüller, kalıcı verilere erişmek için merkezi bir veri deposu (Repository) ve veri erişim nesneleri (DAO) üzerinden haberleşmelidir. Bu haberleşme, ana iş parçacığını (Main Thread) bloke etmeyecek şekilde asenkron olarak gerçekleştirilmelidir.



1.5 YKE Dahili Veri Gereksinimleri

Bu paragraf, uygulamanın kendi bünyesinde tuttuğu veritabanı ve dosya yapılarına yönelik gereksinimleri belirtir.

•	1.5.1 Senaryo Veritabanı Yapısı: Kullanıcı tarafından tanımlanan otomasyon kuralları, ilişkisel bir veritabanı (Room/SQLite) yapısında tutulmalıdır. Her kayıt şu temel veri elemanlarını içermelidir:

Senaryo Tanımlayıcı: Benzersiz anahtar (ID).

Parametreler: Tetikleyici tipi, kısıtlama değerleri ve eylem listesi.

Durum Bilgisi: Senaryonun aktiflik durumu (Boolean).

1.5.2 Olay Kayıtları (Log Data): Çalıştırılan her otomasyonun zaman damgası, senaryo kimliği ve işlem sonucu veritabanında "Geçmiş" tablosunda saklanmalıdır.

1.5.3 Veri Bütünlüğü ve İzolasyonu: Uygulama içi veriler sadece ilgili uygulama paketine özel (private) kalmalı ve Android veri izolasyonu standartlarına uygun şekilde korunmalıdır.





1.6 Uyarlama Gereksinimleri 

Bu kısım, uygulamanın farklı cihaz özelliklerine ve kullanıcı tercihlerine göre nasıl farklılık göstereceğini belirtir.

1.6.1 Kullanıcı Tanımlı Parametreler: Uygulama, kurulum aşamasında sabit değerler yerine kullanıcının cihaz başında belirlediği parametrelerle çalışacaktır. Bu kapsamda; kullanıcının seçtiği konum koordinatları (enlem/boylam), hedef pil yüzdesi ve otomasyona dahil edilecek uygulama listesi cihaza özgü olarak yerel veritabanında saklanacaktır.

1.6.2 Donanım ve Sürüm Uyumluluğu:Uygulama, yüklü olduğu cihazın Android sürümünü kontrol ederek gerekli izinleri (konum, bildirim vb.) talep edecektir. Cihazın donanım özellikleri veya Android sürümü belirli bir işlevi desteklemiyorsa, ilgili otomasyon eylemi o cihazda çalışamayacaktır.

1.6.3 Dil ve Bölge Ayarları: Uygulama arayüzü tamamen Türkçe olarak tasarlanmıştır ve cihazın sistem dilinden bağımsız olarak Türkçe dilinde hizmet verecektir. Tarih ve saat formatları, Türkiye'de kullanılan standartlara uygun olarak sunulacaktır.

1.7 Emniyet Gereksinimleri 

Bu paragraf, uygulamanın çalışması esnasında kullanıcıya veya cihaza gelebilecek istenmeyen zararları önlemek adına alınan önlemleri içerir.

1.7.1 Kaynak Tüketimi ve Isınma Kontrolü: Uygulama, arka planda aşırı işlemci (CPU) kullanımını engellemek için tetikleyicilerin çalışma sıklığına bir limit getirecektir. Bu sayede cihazın aşırı ısınması veya pilin beklenmedik şekilde tükenmesi gibi emniyet riskleri önlenecektir.

1.7.2 Kararsız Durum Koruması: Birbirine zıt komutlar içeren (örneğin aynı anda telefonun sesini hem açan hem kısan) kuralların çakışması durumunda, sistem kararsız kalmamak adına öncelikli kuralı işletecek veya işlemi iptal edecektir.

1.7.3 Acil Durum Esnekliği: Kullanıcı cihaz üzerinden acil bir çağrı gerçekleştiriyorsa veya sistem düzeyinde kritik bir uyarı alınıyorsa, uygulamanın o anki eylemleri (ses kısma vb.) bu kritik süreci engellememek için geçici olarak durdurulacaktır.

1.8 Güvenlik ve Gizlilik Gereksinimleri

Bu paragraf, Vetroid uygulamasının veri gizliliği ve güvenliğine yönelik gereksinimlerini tanımlar. 

•	Veri Yerelliği ve Gizlilik: Uygulama, kullanıcının konum geçmişi, mesaj içerikleri veya oluşturduğu senaryoları hiçbir harici sunucuya iletmeyecektir; tüm veriler sadece kullanıcının kendi cihazındaki izole veritabanında (Internal Storage) tutulacaktır. 

•	Güvenlik Riskleri ve Önlemler: \* Risk: Yetkisiz erişim ile senaryoların değiştirilmesi. 

Önlem: Uygulama, hassas sistem izinlerini (Konum, SMS, Bildirim Erişimi) Android'in çalışma zamanı izin (Runtime Permissions) modeliyle talep edecek ve bu izinlerin kötüye kullanımını engelleyen kumanda havuzu (sandbox) mimarisinde çalışacaktır. 

•	Sorumluluk ve Sertifikasyon: Uygulama, Android işletim sisteminin sağladığı temel güvenlik sertifikasyonlarını ve uygulama imzalama (app signing) standartlarını karşılayacaktır. 

Not: Kullanıcı verilerinin güvenliği için Android'in 'Scoped Storage' yapısı kullanılacak, böylece diğer uygulamaların Vetroid'in veritabanına erişmesi engellenecektir.

1.9 YKE Ortam Gereksinimleri

Vetroid, yalnızca Android işletim sistemi üzerinde çalışan bir mobil uygulamadır. Uygulamanın doğru biçimde çalışabilmesi için aşağıdaki ortam koşullarının sağlanmış olması gerekmektedir.

İşletim Sistemi: Minimum Android 8.0 (Oreo, API düzeyi 26). Hedef ve tam destek sürümü Android 14 (API düzeyi 34)'tür. iOS veya masaüstü işletim sistemleri desteklenmemektedir.

Donanım: GPS/konum donanımı, Geofencing tetikleyicisinin kullanılabilmesi için zorunludur. Bluetooth adaptörü, yalnızca Bluetooth tetikleyicisinin kullanılması durumunda gereklidir. SIM kart takılı olması, SMS gönderme aksiyonu için zorunludur; diğer modüller SIM bağımsız çalışır.

Ağ: Uygulama internet bağlantısı gerektirmez ve talep etmez. Tüm işlemler cihaz üzerinde yerel olarak yürütülür. Wi-Fi ve mobil veri yalnızca ilgili aksiyonların (Wi-Fi aç/kapat gibi) hedefi olarak kullanılır, uygulamanın kendisi için gerekli değildir.

Geliştirme Ortamı: Android Studio (Hedgehog veya üzeri), JDK 17, Gradle 8.x. Uygulama Kotlin diliyle geliştirilmektedir.



1.10 Bilgisayar Kaynak Gereksinimleri

1.10.1 Bilgisayar Donanım Gereksinimleri Sistemin çalışabilmesi için standart bir Android akıllı telefon donanımı yeterlidir. Konum tabanlı tetikleyiciler (Geofencing) için cihazda işlevsel bir GPS modülü ve internet/Wi-Fi anteni bulunması gerekmektedir.

1.10.2 Bilgisayar Donanımı Kaynak Kullanımı Gereksinimleri Uygulama arka planda sürekli olarak tetikleyicileri (zaman, konum, sistem olayları) dinleyeceği için pil tüketimi ve RAM kullanımı optimize edilmelidir. GPS sinyalinin yüksek pil tüketimini önlemek adına hibrit konum sağlayıcılar (Wi-Fi ve baz istasyonu verileri) kullanılacaktır. Ayrıca görevlerin işletim sistemi tarafından sonlandırılmasını engellemek için sistem kaynakları "Ön Plan Servisi (Foreground Service)" mimarisiyle kullanılacaktır. Sistemden özel izinler alınması gerekecektir.



1.10.3 Bilgisayar Yazılım Gereksinimleri

•	İşletim Sistemi: Android OS.

•	Veritabanı: Kullanıcı senaryolarının (kuralların) cihazda kalıcı olarak saklanması için Room/SQLite veritabanı kütüphanesi kullanılacaktır.

•	Kütüphaneler: Zamanlama işlemleri için Android SDK'nın standart zamanlama servisleri (AlarmManager, WorkManager), konum işlemleri için Location SDK kullanılacaktır.

1.10.4 Bilgisayar İletişim Gereksinimleri Uygulamanın dış bir sunucu ile sürekli iletişim kurmasına gerek yoktur; çevrimdışı çalışabilmelidir. Sadece konum tabanlı modül (Geofencing) harita verilerini çekmek ve konum doğrulaması yapmak için GPS uyduları ve internet bağlantısı ile iletişim kuracaktır.

1.11 Yazılım Kalite Faktörleri

•	Güvenilirlik: Cihaz yeniden başlatıldığında (boot) zamanlanmış görevlerin ve kuralların sürekliliği sağlanmalı, kayıp yaşanmamalıdır. Kuralların çakışmasını önleyen bir algoritma ile sistem tutarlılığı korunacaktır.

•	Kullanılabilirlik: Uygulama, teknik bilgi gerektirmeden "Eğer X olursa Y yap" mantığı ile kolayca öğrenilip kullanılabilmelidir.

•	Erişilebilirlik (Availability): Cihazın güç tasarrufu modlarında bile zamanlanmış görevlerin aksamaması için kesin zamanlı (exact alarm) metotları kullanılarak erişilebilirlik garanti altına alınacaktır.

1.12 Tasarım ve Uygulama Kısıtlamaları

•	İzin Kısıtlamaları: Uygulamanın çalışabilmesi için kullanıcının "Sistem Ayarlarını Değiştirme (Write Settings)", "Her zaman konum erişimi" ve "Erişilebilirlik/Kullanım Erişimi" gibi özel izinleri manuel olarak onaylaması gerekmektedir.

•	Üretici Kısıtlamaları: Xiaomi, Samsung gibi farklı üreticilerin Android işletim sistemi üzerine eklediği özel güvenlik ve pil kısıtlamalarına uyumlu bir tasarım yapılmalıdır.

1.13 Personelle İlgili Gereksinimler

Vetroid uygulamasının hedef kitlesi son kullanıcılardır. Sistemi kullanacak kişilerin herhangi bir yazılım, otomasyon veya teknik altyapı bilgisine sahip olması gerekmemektedir.

1.14 Eğitimle İlgili Gereksinimler

Uygulama arayüzü sezgisel olarak tasarlandığı için resmi bir eğitime ihtiyaç duyulmamaktadır. Ancak üreticilerin özel izinleri (Erişilebilirlik, Sistem Ayarları) nasıl onaylanacağı konusunda kullanıcılara uygulama içi görsel rehberler sunulacaktır. Ayrıca uygulamanın kullanımı ve senaryo oluşturma adımlarını anlatan bir PDF Kullanım Kılavuzu hazırlanacaktır.



1.15 Lojistikle İlgili Gereksinimler

Yazılımın kaynak kodları, sürüm takibi ve ortak geliştirme süreçleri tamamen GitHub üzerinden yürütülecektir.

1.16 Diğer Gereksinimler

Bu aşamada önceki paragraflarda kapsanmayan ek bir gereksinim bulunmamaktadır.

1.17 Ambalajlama Gereksinimleri

Yazılım ürünü, Android cihazlara doğrudan kurulup çalıştırılabilecek formatta olan Android APK dosyası olarak paketlenip teslim edilecektir.

1.18 Gereksinimlerin Önceliği ve Kritikliği

Proje takvimi ve bağımlılıklar göz önüne alındığında kritiklik sırası şu şekildedir:

1\.	Kritik Öncelik (Altyapı): Arka plan servis iskeletinin, "Ön Plan Servisi" onaylarının ve Room veritabanının (Senaryo Motoru) kurulması. Diğer tüm modüller buraya bağlıdır.

2\.	Yüksek Öncelik: Zaman (AlarmManager) ve Konum (Geofencing) tabanlı tetikleyicilerin entegrasyonu.

3\.	Orta Öncelik: Uygulama durumu izleyici (App Monitor) ve sistem olayları (şarj, kulaklık vb.) dinleyicilerinin sisteme dahil edilmesi.































Kaynakça

https://developer.android.com/topic/architecture.

https://m3.material.io/.

https://developer.android.com/guide/background.

https://developer.android.com/guide/topics/permissions/overview.

https://app.diagrams.net/.





