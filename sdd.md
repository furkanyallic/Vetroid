&#x20;



BM314-YAZILIM MÜHENDİSLİĞİ







VETROID 

&#x20;Android Otomasyon Uygulaması SDD Raporu









22118080025 - Bedirhan Yiğit

22118080070 - Furkan Yallıç

















Revizyon Tablosu

&#x20;

Versiyon	Tarih	Açıklama	Yazar

1.0	09.03.2026	Proje Yönetim Planı (SPMP) taslağı oluşturuldu; projenin kapsamı, teknik altyapısı ve görev dağılımı tanımlandı.	Bedirhan Yiğit, Furkan Yallıç

1.1	08.04.2026	Yazılım Gereksinim Belgesi (SRS) oluşturuldu; fonksiyonel gereksinimler, dış arayüzler ve sistem modları detaylandırıldı.	Bedirhan Yiğit, Furkan Yallıç

1.2	21.04.2026	Yazılım Tasarım Dokümanı (SDD) oluşturuldu; mimari kararlar, YKE bileşenleri ve arayüz tasarımları tanımlandı.	Bedirhan Yiğit, Furkan Yallıç



































İçindekiler

1 KAPSAM	4

1.1 Tanım	4

1.2 Sisteme Genel Bakış:	4

1.3 Dokümana Genel Bakış	4

2\. İLGİLİ DOKÜMANLAR	4

3\. YKE ÇAPINDA TASARIM KARARLARI	5

3.1 YKE Girdi ve Çıktılarına İlişkin Tasarım Kararları	5

3.2 YKE Davranışına İlişkin Tasarım Kararları	5

3.3 Veritabanı ve Veri Görünümü ile İlgili Tasarım Kararları	5

3.4 Emniyet, Güvenlik ve Gizlilik Gereksinimlerini Karşılamak İçin Seçilen Yaklaşım	6

3.5 Esneklik, Elde Edilebilirlik ve İdame Ettirilebilirliğe Yönelik Kararlar	6

4\. YKE'NİN YAPISAL TASARIMI	6

4.1 YKE Bileşenleri	6

4.2 Genel Çalıştırma (Execution) Kavramı	8

4.3 Arayüz Tasarımı	8

5\. YKE DETAYLI PLANI	9

5.1  Veritabanı Şeması ve Veri Yapıları	9

5.2 TriggerManagerService Mantığı	10

5.3 ScenarioEngine Algoritması	10

5.4 ActionExecutor ve Hata Yönetimi	11

6\. Gereksinimlerin İzlenebilirliği	12

7\. NOTLAR	14

7.1 Kısaltmalar ve Tanımlar	14

7.2 Genel Açıklamalar	14













1 KAPSAM

1.1 Tanım

•	Tanımlama Numarası / Başlık: VETROID Android Otomasyon Uygulaması SDD Raporu

•	Platform: Android İşletim Sistemi (Minimum API 26, Hedef API 34)

•	Versiyon Numarası: 1.2

•	Kısaltmalar: YKE (Yazılım Konfigürasyon Elemanı), UI (Kullanıcı Arayüzü), API (Uygulama Programlama Arayüzü), DB (Veri Tabanı), SDD (Yazılım Tasarım Dokümanı)

1.2 Sisteme Genel Bakış

VETROID, kullanıcıların "Eğer X olursa Y yap" mantığıyla kural tabanlı senaryolar oluşturmasını sağlayan, Android işletim sistemi üzerinde sürekli çalışan bir arka plan servisi mimarisine sahip mobil otomasyon uygulamasıdır. Sistemin amacı; zaman, konum, cihaz etkinlikleri, pil durumu ve donanım bağlantıları gibi tetikleyicileri izleyerek bu koşullar sağlandığında sistem ayarlarını değiştirmek veya SMS göndermek gibi eylemleri otomatik olarak yürütmektir. Geliştirme ekibi (Bedirhan Yiğit ve Furkan Yallıç), uygulamayı çevrimdışı çalışabilen , kullanıcı verilerini yalnızca yerel cihazdaki Room/SQLite veritabanında izole bir şekilde saklayan güvenli bir mimari üzerine inşa etmiştir.

1.3 Dokümana Genel Bakış

&#x09;Bu doküman (SDD), VETROID sisteminin yapısal tasarımını ve YKE çapında alınan mimari kararları özetler. Doküman; sistemin dış arayüzlerle (Android OS API'leri, Konum Servisleri vb.) nasıl entegre olacağını , veritabanı yapısını ve modüller (senaryo motoru, tetikleyici izleyici, aksiyon yürütücü vb.) arası veri akışını detaylandırır. Ayrıca, uygulamanın arka planda izinsiz kaynak tüketimini önlemek amacıyla uygulanan "Ön Plan Servisi" mimarisi ve veri gizliliği gibi güvenlik hususlarını içerir

2\. İLGİLİ DOKÜMANLAR

Proje İçi Dokümanlar:

•	VETROID Yazılım Proje Yönetim Planı (SPMP): Versiyon 1.0, 09.03.2026. Kaynak: Vetroid Geliştirme Ekibi.

•	 VETROID Yazılım Gereksinim Spesifikasyonu (SRS): Versiyon 1.1, 08.04.2026. Kaynak: Vetroid Geliştirme Ekibi.

Dış Referanslar ve API Dokümantasyonları:

•	Android Developers - AlarmManager API Guide: API Düzeyi 34 Güncellemeleri, 2024. Kaynak: Google Android Geliştirici Portalı (https://developer.android.com/reference/android/app/AlarmManager). (Zaman modülü tasarımı için referans alınmıştır.)

•	Android Developers - WorkManager Guide: Sürüm 2.9.x, 2024. Kaynak: Google Android Geliştirici Portalı. 

•	Google Play Services - Geofencing API: Kaynak: Google Developers (https://developer.android.com/training/location/geofencing). 

•	Android Developers - Room Persistence Library: Kaynak: Google Android Geliştirici Portalı. 

•	Material Design 3 (M3) Guidelines: Kaynak: Google Design (https://m3.material.io/). 

3\. YKE ÇAPINDA TASARIM KARARLARI

Bu bölüm, VETROID sisteminin kullanıcının bakış açısından nasıl davranacağını ve yazılım birimlerinin tasarımını etkileyen genel mimari kararları tanımlar.

3.1 YKE Girdi ve Çıktılarına İlişkin Tasarım Kararları

•	Girdiler: Sistem, donanım katmanından asenkron girdiler (GPS/Wi-Fi konum verisi, batarya yüzdesi değişimi, kulaklık takılması vb.) ve zamanlanmış işletim sistemi girdilerini (AlarmManager) kabul edecek şekilde tasarlanmıştır. Kullanıcı arayüzü (UI) girdileri ise sadece senaryo oluşturma, düzenleme ve silme süreçlerinde kabul edilir.

•	Çıktılar: Uygulamanın temel çıktıları; cihaz donanım durumunu değiştiren işletim sistemi komutları (Wi-Fi/Bluetooth açma/kapama, ses profili ve parlaklık ayarı), kullanıcıya verilen arayüz bildirimleri (Toast mesajları) ve mesajlaşma API'si üzerinden tetiklenen SMS gönderimleridir.

3.2 YKE Davranışına İlişkin Tasarım Kararları

•	Durum ve Mod Yönetimi: Sistem; kullanıcı etkileşimi ve donanım olaylarına göre "Bekleme (Idle)", "Aktif (Active)", "Yürütme (Executing)" ve "Durdurulmuş (Suspended)" olmak üzere dört farklı modda çalışacak şekilde tasarlanmıştır.

•	Koşul ve Kısıtlama Mantığı: Bir tetikleyici (örneğin cihaza kulaklık takılması) algılandığında, sistem doğrudan aksiyona geçmez. Öncelikle "Kısıtlama (Constraint) Kontrolü" yapılarak işlemin zaman, konum veya cihaz durumu açısından uygunluğu denetlenir.

•	Çakışma Çözümleme (Kararsız Durum Koruması): Aynı anda tetiklenen ve birbirine zıt komutlar içeren senaryoların (örneğin sesi hem açan hem kısan) sistemi kararsızlığa sürüklememesi için öncelik atamasına dayalı bir çakışma çözüm algoritması tasarlanmıştır.

3.3 Veritabanı ve Veri Görünümü ile İlgili Tasarım Kararları

•	Kullanıcı tarafından oluşturulan tüm kurallar ve sistem olay geçmişi (log), harici bir sunucu kullanılmaksızın cihaz üzerinde yerel olarak Room/SQLite veritabanında saklanacaktır.

•	Kullanıcı arayüzünde veritabanı tabloları teknik bir formatta değil; okunabilir "Senaryolarım" listesi ve "İşlem Geçmişi" şeklinde kullanıcı dostu kartlar (CardView/RecyclerView) olarak gösterilecektir.

3.4 Emniyet, Güvenlik ve Gizlilik Gereksinimlerini Karşılamak İçin Seçilen Yaklaşım

•	Gizlilik ve İzolasyon: Uygulama tamamen çevrimdışı çalışacak şekilde tasarlanmış olup, konum ve iletişim gibi hassas veriler hiçbir dış sunucuya gönderilmeyecektir. Veritabanı dosyaları, Android'in Kapsamlı Depolama mimarisi kullanılarak diğer uygulamaların erişimine kapatılmıştır.

•	Güvenlik: Kötüye kullanımı engellemek amacıyla; SMS, Konum ve Sistem Ayarlarını Değiştirme gibi kritik izinler Android Çalışma Zamanı İzinleri standartlarına uygun olarak kullanıcıdan açıkça talep edilecektir.

•	Emniyet (Isınma ve Acil Durum Kontrolü): Aşırı işlemci (CPU) kullanımı ve cihazın ısınmasını önlemek için arka plan tetikleyicilerinin çalışma sıklığına bir limit getirilmiştir. Ayrıca, acil çağrı veya kritik sistem süreçleri sırasında uygulamanın eylemleri otomatik olarak askıya alınacaktır (Durdurulmuş mod).

3.5 Esneklik, Elde Edilebilirlik ve İdame Ettirilebilirliğe Yönelik Kararlar

•	Erişilebilirlik (Availability): Modern Android sürümlerinin batarya tasarrufu sebebiyle uygulamayı uyutmasını engellemek için, ana dinleyici servisler bir "Ön Plan Servisi" olarak yapılandırılmış ve kritik zamanlamalar için kesin zamanlı (exact alarm) metodolojisi seçilmiştir.

•	Donanım Optimizasyonu: Konum takibi esnasında GPS'in pili hızla tüketmemesi için, Wi-Fi ve baz istasyonu verilerini harmanlayan "Hibrit Konum Sağlayıcılar" tercih edilerek enerji verimliliği maksimize edilmiştir.



4\. YKE'NİN YAPISAL TASARIMI

Bu bölüm, VETROID uygulamasının mimari yapısını, yazılım birimlerini, bu birimlerin bilgisayar kaynaklarını nasıl kullandığını ve kendi aralarındaki dinamik çalışma kavramını tanımlar.

4.1 YKE Bileşenleri

&#x09;VETROID Yazılım Konfigürasyon Elemanı (YKE), projeye özgü tanımlayıcılarla isimlendirilmiş 6 temel yazılım biriminden (modülden) oluşmaktadır:

•	VET\_MOD\_TIME (Zaman Tabanlı Tetikleyici Modülü):

o	Amacı: Kullanıcının belirlediği gün ve saatlerde otomasyon senaryolarını tetiklemek.

o	Geliştirme Türü: Yeni tasarım.

o	Kaynak Kullanımı: Zamanlayıcı uyku modunda kalacağı için CPU ve RAM kullanımı asgaridir. İşletim sisteminin pil optimizasyonunu aşmak için kesin zamanlı (exact alarm) yetkileri ile işlemciyi anlık olarak uyandırır.

•	VET\_MOD\_GEO (Konum Tabanlı Kontrol Modülü):

o	Amacı: Harita üzerinde belirlenen dairesel bir bölgeye (Geofence) giriş veya çıkış yapıldığını tespit etmek.

o	Geliştirme Türü: Yeni tasarım.

o	Kaynak Kullanımı: GPS sinyalinin yüksek pil tüketimini önlemek amacıyla, Wi-Fi ve baz istasyonu verilerinden de destek alan hibrit konum sağlayıcılar (Fused Location Provider) kullanarak batarya dostu bir donanım kullanımı sergiler.

•	VET\_MOD\_EVENT (Sistem ve Uygulama İzleyici Modülü):

o	Amacı: Şarj durumu, kulaklık bağlantısı ve diğer uygulamaların başlatılıp/kapatılması gibi sistem düzeyindeki değişiklikleri dinlemek.

o	Geliştirme Türü: Yeni tasarım.

o	Kaynak Kullanımı: Modern Android sürümlerinin arka plan süreçlerini kapatmasını engellemek için "Ön Plan Servisi" (Foreground Service) olarak yapılandırılır. Sürekli ancak düşük seviyeli RAM tahsisi kullanır.

•	VET\_MOD\_ACTION (Aksiyon Yürütme Motoru):

o	Amacı: Tetiklenen senaryolara karşılık gelen fiziksel eylemleri (ekran parlaklığı değiştirme, Wi-Fi/Bluetooth açma/kapama, SMS gönderme vb.) sisteme uygulamak.

o	Geliştirme Türü: Yeni tasarım.

o	Kaynak Kullanımı: Sadece "Yürütme (Executing)" moduna geçildiğinde anlık olarak CPU kaynaklarını kullanır.

•	VET\_MOD\_SCENARIO (Senaryo ve Mantık Motoru):

o	Amacı: "Eğer X olursa Y yap" mantığındaki kuralları kaydetmek, kısıtlamaları (zaman/konum kısıtları) doğrulamak ve zıt komutlar içeren kural çakışmalarını önlemek.

o	Geliştirme Türü: Yeni tasarım.

o	Kaynak Kullanımı: Cihaz içi disk okuma/yazma (I/O) işlemleri için anlık bellek kullanımı gerektirir.







•	VET\_UI\_CORE (Kullanıcı Arayüzü Modülü):

o	Amacı: Kullanıcıların senaryo oluşturduğu, düzenlediği ve listelediği interaktif ekranları sunmak.

o	Geliştirme Türü: Yeni tasarım.

o	Kaynak Kullanımı: Sadece uygulama ön planda (Aktif modda) açıkken ekran kartı (GPU) ve CPU kaynaklarını kullanır.

4.2 Genel Çalıştırma (Execution) Kavramı 

&#x09;VETROID sisteminin yazılım birimleri arasındaki çalışma akışı, "Olay Güdümlü Mimari (Event-Driven Architecture)" prensiplerine göre tasarlanmıştır. Sistem, dinamik olarak şu durumlardan geçerek çalışır:

1\.	Bekleme (Idle) Durumu: Uygulama arka plana alındığında, VET\_MOD\_TIME, VET\_MOD\_GEO ve VET\_MOD\_EVENT birimleri Ön Plan Servisi (Foreground Service) çatısı altında sistemi dinlemeye başlar. Bu aşamada kaynak kullanımı minimumdadır.

2\.	Olay Yakalama ve Doğrulama: Dinleyici birimler bir sistem olayı yakaladığında (örneğin cihaza kulaklık takılması), bu veriyi anında VET\_MOD\_SCENARIO (Mantık Motoru) birimine asenkron olarak iletir.

3\.	Kısıtlama Kontrolü: Mantık motoru işlemi hemen onaylamaz; veritabanını (Room DB) sorgulayarak o kural için belirlenmiş bir saat, gün veya konum kısıtlaması (Constraint) olup olmadığını denetler. Ayrıca, kararsız durum koruması kapsamında çakışan başka bir senaryo olup olmadığı kontrol edilir.

4\.	Yürütme (Executing) Durumu: Kısıtlamaları aşan ve onaylanan senaryo eylemi, VET\_MOD\_ACTION (Aksiyon Yürütücü) modülüne iletilir. Bu modül cihaz ayarlarını değiştirir (veya SMS gönderir).

5\.	Kayıt ve Sonlandırma: Gerçekleşen işlem, zaman damgasıyla birlikte "İşlem Geçmişi" tablosuna yazılarak döngü tamamlanır ve sistem tekrar "Bekleme" moduna döner. Kritik durumlarda (örneğin acil çağrı anında) süreç Durdurulmuş (Suspended) moda alınarak işlem yarıda kesilir.

4.3 Arayüz Tasarımı 

VETROID yazılım birimlerinin dış dünya ve kendi aralarındaki veri etkileşimi, SRS dokümanında belirtilen spesifik arayüz tanımlayıcıları ile yapılandırılmıştır:

•	\[IF-01] Kullanıcı Arayüzü (UI): VET\_UI\_CORE tarafından yönetilir. Kullanıcının senaryo parametrelerini (alfanümerik isimler, hedef uygulamalar vb.) sisteme girdiği GUI arayüzüdür.

•	\[IF-02] Android OS API'leri: VET\_MOD\_TIME, VET\_MOD\_EVENT ve VET\_MOD\_ACTION birimlerinin, cihazın donanım sensörleri ve işletim sistemi (AlarmManager, BroadcastReceiver vb.) ile haberleştiği, Intent tabanlı sistem arayüzüdür.

•	\[IF-03] Konum Servisi Arayüzü: VET\_MOD\_GEO modülünün Google Play Services Geofencing API ile haberleştiği arayüzdür. Enlem, boylam ve yarıçap verileri bu arayüz üzerinden asenkron callback yöntemiyle alınır.

•	\[IF-04] Mesajlaşma Servisleri: VET\_MOD\_ACTION modülünün, tanımlı durumlarda dış sistemlerle iletişim kurarak SMS (SmsManager API) veya WhatsApp mesajı (Intent mekanizması) tetiklediği dış arayüzdür.

•	\[IF-05] Room / SQLite DB Arayüzü: VET\_MOD\_SCENARIO modülünün, cihazın yerel hafızasındaki verilerle Data Access Object (DAO) sınıfları üzerinden okuma/yazma (CRUD) işlemleri yaptığı dahili veri erişim arayüzüdür.





5\. YKE DETAYLI PLANI

Bu bölüm, Vetroid uygulamasını oluşturan yazılım birimlerinin veri yapılarını, algoritmik mantığını ve hata yönetim süreçlerini teknik ayrıntılarıyla tanımlar.



5.1  Veritabanı Şeması ve Veri Yapıları 

Uygulama verileri Room Persistence Library ile SQLite üzerinde yönetilir. Esneklik sağlamak amacıyla bileşen bazlı tablo yapısı benimsenmiştir:

•	Tablo İlişkileri: Scenario ana tablosu; Trigger, Condition, Action ve EventLog tablolarına scenarioId yabancı anahtarı (FK) ile 1-N biçiminde bağlıdır.

•	Dinamik Parametreler: Tetikleyici ve aksiyon detayları params (JSON) alanında saklanır.

o	Örnek (Zaman): {"saat": 22, "dakika": 30, "gunler": \[1,2,3,4,5]}

o	Örnek (Konum): {"lat": 39.92, "lng": 32.85, "yaricap": 200}

•	Öncelik Yönetimi: Aynı anda tetiklenen senaryolarda, priority değeri düşük olan (yüksek öncelikli) kayıt önce işletilir.

•	 

Şekil 1:Veritabaı ER Diyagramı





5.2 TriggerManagerService Mantığı

Foreground Service mimarisiyle çalışan bu birim, sistem kaynaklarını dinler ve olay-tabanlı tetiklemeleri yönetir:

•	Kayıt Mekanizması: Servis başladığında aktif senaryoları yükler; AlarmManager (zaman), GeofencingClient (konum) ve BroadcastReceiver (sistem olayları) kayıtlarını oluşturur.

•	Olay İletimi: Bir eşleşme sağlandığında, servis scenarioId bilgisini ScenarioEngine birimine asenkron olarak iletir.



5.3 ScenarioEngine Algoritması

Tetiklenen senaryonun yürütülüp yürütülmeyeceğine karar veren merkezi mantıksal birimdir.Aşağıda bulunan karar verme süreci ardışıllık diyagramında adımlar detaylı olarak belirtilmiştir.

Çalışma Adımları:

1\.	İlgili senaryonun isActive durumu doğrulanır.

2\.	Bağlı tüm Condition kayıtları çekilir ve ConditionChecker üzerinden anlık sistem sorgusu yapılır.

3\.	Tüm koşullar "MANTIKSAL VE" (AND) operatörüyle denetlenir.

4\.	Doğrulama başarılı ise ActionExecutor tetiklenir; aksi halde işlem durdurulup loglanır.





5.4 ActionExecutor ve Hata Yönetimi

Doğrulanan aksiyon listesini order alanındaki sıraya göre icra eden yürütme birimidir.

•	Hata İzolasyonu: Her aksiyon bağımsız try-catch blokları içinde yürütülür. Bir aksiyonun başarısız olması (örn: izin reddi) zinciri bozmaz, sadece ilgili hata loglanır ve sıradaki aksiyona geçilir.

•	Kritik Hata Bildirimi: Sistem seviyesinde bir kısıtlama (kapalı GPS vb.) tespit edilirse kullanıcıya bildirim (Notification) gönderilir.



Şekil 3:Aksiyon Yürütme ve Hata Yönetimi Durum Diyagramı











6\. Gereksinimlerin İzlenebilirliği

Bu bölüm, SRS (Yazılım Gereksinim Belgesi) ile SDD (Yazılım Tasarım Dokümanı) arasındaki bağı kurarak her gereksinimin bir tasarım karşılığı olduğunu garanti eder.



a. Yazılım Birimlerinden Gereksinimlere İzlenebilirlik

Yazılım Birimi (SDD)	İlgili SRS Gereksinimi	Açıklama

Veritabanı Şeması	1.2.1, 1.5.1 	Senaryo yönetimi ve kalıcı veri saklama gereksinimlerini karşılar.  

TriggerManagerService	1.2.2, 1.3.3 	Sistem olaylarının izlenmesi ve Android API entegrasyonunu sağlar.  

ScenarioEngine	1.2.3, 1.4.2 	Kısıtlama kontrolü ve mantıksal doğrulama süreçlerini yönetir.  

ActionExecutor	1.2.4, 1.3.5 	Eylem yürütme ve harici servis (SMS/WP) haberleşmesini icra eder.  

LogManager	1.05.2002	Olay kayıtlarının (EventLog) tutulması gereksinimini karşılar.  





b. Gereksinimlerden Yazılım Birimlerine İzlenebilirlik

•	Fonksiyonel Gereksinimler: SRS 1.2 başlığı altındaki tüm maddeler , SDD Bölüm 5'teki ilgili mantıksal birimlere (Engine, Executor vb.) tam olarak tahsis edilmiştir. 

•	Arayüz Gereksinimleri: SRS 1.3'te tanımlanan tüm dış arayüzler , SDD 4.3 (Arayüz Tasarımı) ve 5.2 (TriggerManager) bölümlerinde teknik olarak detaylandırılmıştır



7\. NOTLAR

Bu bölüm, Vetroid Yazılım Tasarım Dokümanı'nın (SDD) daha iyi anlaşılmasını sağlamak amacıyla kullanılan teknik terimlerin, kısaltmaların ve kavramsal tanımların listesini içermekte, ayrıca projenin teknik geçmişine dair kısa bir özet sunmaktadır.

7.1 Kısaltmalar ve Tanımlar

YKE (Yazılım Konfigürasyon Elemanı): Projenin ana nesnesi olan Vetroid Android uygulamasını ifade eder. 

SRS (Software Requirements Specification): Bu dokümana temel teşkil eden ve sistemin gereksinimlerini belirleyen Yazılım Gereksinim Belgesi'ni temsil eder. 

SDD (Software Design Description): Elinizdeki bu belgeyi, yani sistemin teknik tasarım detaylarını açıklayan Yazılım Tasarım Dokümanı'nı ifade eder. 

FK (Foreign Key - Yabancı Anahtar): Room/SQLite veritabanı tabloları arasında mantıksal ilişki kuran ve veri bütünlüğünü sağlayan anahtar alanıdır. 

PK (Primary Key - Birincil Anahtar): Veritabanındaki her bir satırın benzersiz olarak tanımlanmasını sağlayan kimlik numarasıdır. 

JSON (JavaScript Object Notation): Tetikleyici ve aksiyon parametrelerinin veritabanında esnek ve hafif bir yapıda saklanmasını sağlayan veri değişim formatıdır. 

Geofencing: Cihazın GPS verilerini kullanarak, harita üzerinde belirlenen sanal sınırlara (çitlere) girilme veya çıkılma durumunu takip eden teknolojidir. 

BroadcastReceiver: Android işletim sisteminden gelen (şarj durumu, kulaklık bağlantısı, uçak modu vb.) sistem düzeyi yayınları dinleyen ve yakalayan bileşendir. 

Foreground Service (Ön Plan Servisi): Uygulama arka planda olsa dahi Android sisteminin süreci sonlandırmasını engelleyen ve kullanıcıya bir bildirim aracılığıyla aktif olduğunu gösteren servis yapısıdır. 

7.2 Genel Açıklamalar

&#x20;Vetroid projesi, Android işletim sisteminin modern arka plan kısıtlamalarına tam uyumlu olması amacıyla Foreground Service mimarisi üzerine inşa edilmiştir. Tasarımda kullanılan JSON tabanlı parametre yapısı, uygulamanın ileride yeni tetikleyici veya eylem türleri eklenerek genişletilmesine (ölçeklenebilirlik) olanak tanır. Sistemin tüm işleyişi yerel (local) kaynaklarla sağlandığından, kullanıcı gizliliği en üst düzeyde tutulmuştur.













Kaynakça

https://developer.android.com/?hl=tr  (Erişim Tarihi :20.04.2026)

https://www.techtarget.com/searchSoftwareQuality/tip/A-guide-to-software-design-documentation-and-specifications  (Erişim Tarihi :20.04.2026)

https://app.diagrams.net/ (Erişim Tarihi :21.04.2026)



