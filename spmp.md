





&#x20;                                   





















BM314-YAZILIM MÜHENDİSLİĞİ 





VETROID  

Android Otomasyon Uygulaması SPMP Raporu









22118080025 - Bedirhan Yiğit

22118080070 - Furkan Yallıç













Revision Table



Versiyon	Tarih	Açıklama	Yazar

1.0	09.03.2026	Proje Yönetim Planı (SPMP) taslağı oluşturuldu; projenin kapsamı, teknik altyapısı ve görev dağılımı tanımlandı.	Bedirhan Yiğit,Furkan Yallıç

&#x09;		

&#x09;		

&#x09;		





























İÇİNDEKİLER



1.GİRİŞ	5

1.1 Proje Açıklaması	5

1.2 Proje Çıktıları	5

2\. Proje Organizasyonu	5

2.1 Yazılım Süreç Modeli	5

2.2 Roller ve Sorumluluklar	5

2.3 Araçlar ve Teknikler	6

3\. PROJE YÖNETİM PLANI (PROJECT MANAGEMENT PLAN)	6

3.1 Tasks (Proje İş Paketleri)	6

3.1.1 Zaman Tabanlı Tetikleyici Modülü (Time-Based Trigger Module)	7

•	3.1.1.1 Tanım	7

•	3.1.1.2 Çıktılar ve Kilometre Taşları	7

•	3.1.1.3 Gerekli Kaynaklar	7

•	3.1.1.4 Bağımlılıklar ve Kısıtlamalar	7

•	3.1.1.5 Riskler ve Önlemler	7

3.1.2 Konum Tabanlı Kontrol Modülü (Geofencing Module)	7

•	3.1.2.1 Tanım	7

•	3.1.2.2 Çıktılar ve Kilometre Taşları	7

•	3.1.2.3 Gerekli Kaynaklar	7

•	3.1.2.4 Bağımlılıklar ve Kısıtlamalar	7

•	3.1.2.5 Riskler ve Önlemler	7

3.1.3 Uygulama ve Sistem Durumu İzleyici (App \& System Event Monitor)	8

•	3.1.3.1 Tanım	8

•	3.1.3.2 Çıktılar ve Kilometre Taşları	8

•	3.1.3.3 Gerekli Kaynaklar	8

•	3.1.3.5 Riskler ve Önlemler	8

3.1.4 Aksiyon Yürütme Motoru (Action Execution Module)	8

•	3.1.4.1 Tanım	8

•	3.1.4.2 Çıktılar ve Kilometre Taşları	8

•	3.1.4.3 Gerekli Kaynaklar	8

•	3.1.4.4 Bağımlılıklar ve Kısıtlamalar	8

•	3.1.4.5 Riskler ve Önlemler	8

3.1.5 Senaryo Yönetimi ve Mantık Motoru (Scenario Engine \& Rule Logic)	9

•	3.1.5.1 Tanım	9

•	3.1.5.2 Çıktılar ve Kilometre Taşları	9

•	3.1.5.3 Gerekli Kaynaklar	9

•	3.1.5.4 Bağımlılıklar ve Kısıtlamalar	9

•	3.1.5.5 Riskler ve Önlemler	9

3.2 Atamalar (Assignments)	9

3.3 Timetable (Zaman Çizelgesi / Takvim)	10































1.GİRİŞ

1.1 Proje Açıklaması

Vetroid, Android işletim sistemi üzerinde çalışan, kullanıcı tanımlı tetikleyici-aksiyon kurallarına dayalı bir mobil otomasyon uygulamasıdır. Uygulama; zaman, konum, sistem olayları ve uygulama durumu gibi farklı tetikleyici türlerini destekleyerek kullanıcının cihazını otomatik olarak yönetmesine olanak tanır.

Günlük hayatta sıkça karşılaşılan tekrarlayan işlemler (örneğin eve gelince Wi-Fi açılması, toplantı saatinde telefonun sessiz moda geçmesi veya şarj bağlandığında ekran parlaklığının artması) bu uygulama aracılığıyla otomatikleştirilebilir. Kullanıcı, herhangi bir teknik bilgiye sahip olmaksızın "Eğer X olursa Y yap" mantığıyla kendi senaryolarını oluşturabilir, düzenleyebilir ve silebilir.

Uygulama, mevcut otomasyon araçlarından ilham almaktadır. Proje kapsamında beş temel modül geliştirilecektir: zaman tabanlı tetikleyici, konum tabanlı tetikleyici (Geofencing), uygulama ve sistem durumu izleyici, aksiyon yürütme motoru ve senaryo yönetim sistemi.



1.2 Proje Çıktıları

Yazılım Dokümanları: SPMP, SRS, SDD ve STD belgeleri.

Yazılım Ürünü: Çalıştırılabilir Android APK dosyası.

Kaynak Kod: GitHub üzerinde barındırılan tüm proje kodları.

Kullanım Kılavuzu: Uygulamanın nasıl kurulacağını ve senaryo oluşturulacağını anlatan PDF dokümanı.

&#x20;

2\. Proje Organizasyonu

2.1 Yazılım Süreç Modeli

Seçilen Model: Iterative (Yinelemeli) Geliştirme Modeli: Her yinelemede yukarıda belirlediğimiz 5 fonksiyondan birini (örneğin önce zaman tabanlı tetikleyiciyi) tamamlayıp test ederek ilerlemek, riskleri erkenden görmemizi sağlar.

2.2 Roller ve Sorumluluklar

Furkan Yallıç — Proje Yöneticisi \& Arka Plan Servisleri Geliştirici

Toplantı organizasyonu, iş takib ve yönetimi ve dönem boyunca teslim edilecek dokümanların (SRS, SDD, STD) koordinasyonundan sorumludur. Teknik tarafta ise uygulamanın sistem düzeyindeki altyapısını üstlenir.

Teknik sorumlulukları:

•	Task 1 – Zaman Tabanlı Tetikleyici: AlarmManager ve WorkManager entegrasyonu, cihaz yeniden başlatılmasında görev sürekliliğinin sağlanması.

•	Task 2 – Konum Tabanlı Tetikleyici (Geofencing): Harita arayüzü entegrasyonu, bölge tanımlama ekranı ve konum servisi bağlantıları.

•	Task 4 – Sistem Olayları İzleyici: Şarj durumu, kulaklık bağlantısı ve sistem bildirimleri için BroadcastReceiver yapılarının kurulması.

•	Altyapı: Tüm arka plan servislerinde kullanılacak ortak BroadcastReceiver ve Foreground Service mimarisinin tasarlanması

Bedirhan Yiğit — Android Geliştirici \& Arayüz Tasarımcısı

Uygulamanın kullanıcıya dokunan her şeyden ve merkezi iş mantığından sorumludur. Geliştirdiği senaryo motoru, diğer tüm modülleri birbirine bağlayan çekirdek bileşen olduğundan entegrasyon sürecinde de koordinasyon rolü üstlenir.

Teknik sorumlulukları:

•	Task 3 – Uygulama Durumu İzleyici: Kullanım istatistikleri API'si aracılığıyla uygulama açılış/kapanış olaylarının izlenmesi ve tetikleyici olarak tanımlanabilmesi.

•	Task 5 – Senaryo Motoru \& Veritabanı: "Eğer X olursa Y yap" kurallarını saklayan Room/SQLite veritabanı mimarisi ve çakışma yönetim algoritması.

•	Aksiyon Modülü: Wi-Fi, ses profili, ekran parlaklığı gibi sistem ayarlarını değiştiren aksiyon kütüphanesinin geliştirilmesi.

•	Kullanıcı Arayüzü (UI): Senaryo oluşturma, listeleme ve düzenleme ekranlarının tasarımı ve implementasyonu.



2.3 Araçlar ve Teknikler

Geliştirme Ortamı: Android Studio.

Versiyon Kontrol: Git \& GitHub.

3\. PROJE YÖNETİM PLANI (PROJECT MANAGEMENT PLAN)

3.1 Tasks (Proje İş Paketleri)

3.1.1 Zaman Tabanlı Tetikleyici Modülü (Time-Based Trigger Module)

•	3.1.1.1 Tanım: Kullanıcının belirli saatlerde, günlerde veya periyotlarda (örneğin hafta içi her gün) otomasyon başlatabilmesini sağlayan kullanıcı arayüzü ve arka plan zamanlama servisinin geliştirilmesi.

•	3.1.1.2 Çıktılar ve Kilometre Taşları: Zamanlayıcı mantığını içeren modül kodları ve zamanlanmış görevlerin cihaz yeniden başlatılsa bile (boot) sürekliliğini koruyan sistem kayıt yapısı.

•	3.1.1.3 Gerekli Kaynaklar: Android SDK standart zamanlama ve görev yönetimi kütüphaneleri.

•	3.1.1.4 Bağımlılıklar ve Kısıtlamalar: Cihazın sistem saati doğruluğu ve kullanıcının işletim sistemi üzerinden "Tam alarm ve hatırlatıcı" iznini onaylamış olması.

•	3.1.1.5 Riskler ve Önlemler:

o	Risk: Cihazın güç tasarrufu moduna girmesiyle zamanlanmış görevlerin gecikmesi veya çalışmaması.

o	Önlem: İşletim sisteminin pil optimizasyonunu aşan "kesin zamanlı" (exact alarm) metodolojilerinin kullanılması.

3.1.2 Konum Tabanlı Kontrol Modülü (Geofencing Module)

•	3.1.2.1 Tanım: Harita üzerinde belirlenen dairesel bir bölgeye (ev, okul, iş vb.) giriş yapıldığında veya bu bölgeden çıkıldığında sistemin otomatik bir aksiyon başlatmasını sağlayan modül.

•	3.1.2.2 Çıktılar ve Kilometre Taşları: Harita üzerinde bölge seçimini sağlayan arayüz entegrasyonu ve konum tabanlı servis bağlantılarının tamamlanması.

•	3.1.2.3 Gerekli Kaynaklar: Konum servisleri yazılım geliştirme kiti (Location SDK) ve harita servis anahtarı (API Key).

•	3.1.2.4 Bağımlılıklar ve Kısıtlamalar: Kullanıcının GPS servislerini açık tutması ve uygulamaya "Her zaman konum erişimi" yetkisi vermesi.

•	3.1.2.5 Riskler ve Önlemler:

o	Risk: Kapalı alanlarda GPS sinyalinin zayıflığı veya sürekli konum takibinin yüksek pil tüketimi.

o	Önlem: Pil tasarrufu sağlamak adına Wi-Fi ve baz istasyonu verilerinden destek alan hibrit konum sağlayıcıların kullanılması.





3.1.3 Uygulama ve Sistem Durumu İzleyici (App \& System Event Monitor)

•	3.1.3.1 Tanım: Belirli uygulamaların başlatılması/kapatılması, pil durum değişiklikleri (şarj olma vb.) veya donanımsal değişikliklerin (kulaklık takılması gibi) sistem tarafından dinlenmesi.

•	3.1.3.2 Çıktılar ve Kilometre Taşları: Sistem olaylarını anlık yakalayan servis yapısı ve kullanıcının tetikleyici olacak uygulamaları seçebileceği uygulama listeleme arayüzü.

•	3.1.3.3 Gerekli Kaynaklar: Android kullanım istatistikleri ve sistem olay bildirim yapıları (Intent Filters).

•	3.1.3.4 Bağımlılıklar ve Kısıtlamalar: Android "Erişilebilirlik" veya "Kullanım Erişimi" gibi kritik sistem izinlerinin kullanıcı tarafından onaylanması.

•	3.1.3.5 Riskler ve Önlemler:

o	Risk: Modern Android sürümlerinin arka plan süreçlerini kaynak tasarrufu için sonlandırması.

o	Önlem: İzleme servisinin bir "Ön Plan Servisi" (Foreground Service) olarak yapılandırılarak sistem tarafından kapatılmasının önlenmesi.

3.1.4 Aksiyon Yürütme Motoru (Action Execution Module)

•	3.1.4.1 Tanım: Tetikleyiciler aktif olduğunda gerçekleştirilecek fiziksel eylemlerin (Ses profilini değiştirme, bağlantı ayarları, ekran parlaklığı vb.) sisteme uygulanmasını sağlayan modül.

•	3.1.4.2 Çıktılar ve Kilometre Taşları: Sistem ayarlarını güvenli şekilde değiştiren merkezi yönetim sınıfı ve desteklenen tüm aksiyonların bir kütüphane olarak sunulması.

•	3.1.4.3 Gerekli Kaynaklar: Android Sistem Ayarları ve Donanım Yönetimi uygulama programlama arayüzleri (APIs).

•	3.1.4.4 Bağımlılıklar ve Kısıtlamalar: Uygulamanın "Sistem Ayarlarını Değiştirme" (Write Settings) yetkisine sahip olması.

•	3.1.4.5 Riskler ve Önlemler:

o	Risk: Farklı cihaz üreticilerinin (Xiaomi, Samsung vb.) Android üzerine eklediği özel güvenlik kısıtlamaları.

o	Önlem: Kullanıcıya ihtiyaç duyulan özel izinlerin nasıl verileceğine dair uygulama içi görsel rehberlerin sunulması.



3.1.5 Senaryo Yönetimi ve Mantık Motoru (Scenario Engine \& Rule Logic)

•	3.1.5.1 Tanım: "Eğer Tetikleyici gerçekleşirse Aksiyon yap " mantığını birbirine bağlayan; kullanıcıya bu kuralları oluşturma, düzenleme ve silme imkanı tanıyan merkezi yönetim birimi.

•	3.1.5.2 Çıktılar ve Kilometre Taşları: Kuralların kalıcı olarak saklanacağı veritabanı mimarisi ve kullanıcı dostu senaryo yönetim ekranı.

•	3.1.5.3 Gerekli Kaynaklar: Veritabanı yönetim kütüphanesi (Room/SQLite).

•	3.1.5.4 Bağımlılıklar ve Kısıtlamalar: Diğer 4 temel modülün (Tetikleyiciler ve Aksiyonlar) birbiriyle veri alışverişi yapabilecek seviyede hazır olması.

•	3.1.5.5 Riskler ve Önlemler:

o	Risk: Birden fazla senaryonun aynı anda çalışarak çakışması (Örn: Bir kuralın Wi-Fi açarken diğerinin kapatması).

o	Önlem: Senaryolara öncelik sırası atanması ve çakışma durumlarını denetleyen bir kontrol algoritmasının geliştirilmesi.



3.2 Atamalar (Assignments)

Furkan Yallıç

•	Task 1 (Zaman): Alarmları ve periyodik görevleri kurar.

•	Task 2 (Konum): Harita ve Geofencing kısmını halleder.

•	Task 4 (Sistem Olayları): Şarj, kulaklık ve bildirimleri dinler.

•	Ek Sorumluluk: Bu kişi uygulamanın "Giriş Servislerini" (BroadcastReceiver ve Service yapılarını) kurar.

Bedirhan Yigit

•	Task 3 (Uygulama Durumu): Uygulama açılışlarını izler (Bu kısım UI ile çok ilişkilidir).

•	Task 5 (Senaryo Motoru \& Veritabanı): "Eğer X olursa Y yap" mantığını ve Room DB'yi kurar.

•	Aksiyonlar (Fiziksel Eylemler): Wi-Fi açma, ses kısma gibi "aksiyon" kodlarını yazar.

•	Arayüz (UI): Senaryo oluşturma ekranlarını ve ana listeyi tasarlar.



3.3 Timetable (Zaman Çizelgesi / Takvim)



Hafta	Aşama / Dokümantasyon	Yapılacak İşler (İş Paketleri)	Sorumlu

1\. Hafta	Analiz \& SPMP	Kapsamın netleştirilmesi ve SPMP belgesinin tamamlanması.	Ortak

2\. Hafta	Gereksinimler (SRS)	Fonksiyonel ve fonksiyonel olmayan gereksinimlerin yazılması (SRS).	Ortak

3\. Hafta	Altyapı \& Core	1. Kişi: İzin yönetimi ve Arka plan servis iskeleti. 2. Kişi: Room DB kurulumu ve Ana UI iskeleti.	Ayrı

4\. Hafta	Geliştirme - I	1. Kişi: Zaman ve Konum tetikleyicileri (Task 1 \& 2). 2. Kişi: Senaryo motoru ve Aksiyon kütüphanesi (Task 5 \& 6).	Ayrı

5\. Hafta	Geliştirme - II	1. Kişi: Sistem olayları (Task 4). 2. Kişi: Uygulama durumu izleyici (Task 3) ve UI detayları.	Ayrı

6\. Hafta	Tasarım (SDD).	Yazılım mimarisi, Room DB şeması ve UI taslaklarının (Mockup) hazırlanması (SDD).	Ortak

7\. Hafta	Entegrasyon	İki kişinin yazdığı modüllerin birleştirilmesi ve kural motoruyla bağlanması.	Ortak

8\. Hafta	Test (STD)	Birim ve entegrasyon testlerinin yapılması, hataların (bug) giderilmesi (STD).	Ortak

9\. Hafta	Final \& Teslim	Kullanım kılavuzunun hazırlanması, APK çıktısı ve projenin hocaya sunumu.	Ortak

&#x20;

&#x20;

 

Kaynakça:



https://ase.in.tum.de/stars.globalse.org/stars1/docs/SPMP/Examples/Examples.html



https://www.academia.edu/28118705/Example\_of\_Software\_Project\_Management\_Plan\_SPMP\_



https://github.com/droidrun/droidrun



https://www.geeksforgeeks.org/software-engineering/top-8-software-development-models-used-in-industry/





