# Der Stud.IP Client

### Information für Anwender
Alle wichtigen Informationen für Endanwender finden Sie auf unserer Website: http://studip-client.danner-web.de/ oder hier im [Wiki]( https://github.com/CollapsedDom/Stud.IP-Client/wiki/Hilfe).

### Information für Stud.IP Betreiber
Falls Sie Betreiber einer Stud.IP Instanz sind und unseren Stud.IP Client für Ihre Nutzer anbieten wollen, können wir Sie gerne in unserem Verteilungssystem ergänzen. Dazu kontaktieren Sie uns bitte unter folgender E-Mail Adresse: studipclient@danner-web.de

### Information für Entwickler
Wir arbeiten ausschließlich mit Eclipse, daher sind vordefinierte Maven und run launcher für Eclipse im Ordner `/_eclipse_launch` vorhanden.
Der "Run StudIP Client.launch" setzt das Java working directory des ausgeführten Programmes auf `/bin` und fügt sowohl alle `src` Ordner (für Debugging) als auch alle default bin Ordner (`target` Ordner unter Maven, sprich aktuelle class Dateien) hinzu. Sofern sich beteis compilierte Plugins in `/bin/plugins` befinden (mvn package), werden diese dann im StudIP Client angezeigt.

##### Projekt Checkout (Eclipse)
Genauere Infos finden Sie [hier](http://obscuredclarity.blogspot.de/2012/10/import-maven-git-project-into-eclipse.html).

1. Im `Project Explorer` rechts Klick -> `Import`
2. `Maven` -> `Check out Maven Projects from SCM` auswählen -> `next`
3. Unten rechts m2e Marketplace Link anklicken und `m2e-egit` Connector installieren
4. Schritt 1-2 wiederholen
5. SCM URL: git auswählen und URL ergänzen
6. (Optional) Workspace location ändern
7. Finish

Alterantiv kann das Projekt mit `git clone` ausgecheckt werden und dann über `Import` -> `Maven` -> `Existing Maven Project` importiert werden.

Ab Eclipse Mars ist es auch möglich, Multi-Modul Maven Projekte hierarchisch anzuzeigen. Dazu im `Project Explorer` -> `Dreieck nach unten` -> `Project Presentation` -> `Hierarchical` (möglicherweise ist ein Elipse Neustart erforderlich).

Nach einem erfolgreichen Checkout müssen noch folgende Dateien umbenannt werden:
- im Ordner: `/core/client/src/main/java/de/danner_web/studip_client/plugin/` die Datei `DefaultServer_RENAME.java` zu `DefaultServer.java`. Hier muss noch unter `getServerList()` ein passender Server ergänzt werden.

- im Ordner: `/core/client/src/main/resources` die Datei `publicCert_RENAME.cer` zu `publicCert.cer`. Sie stellt das Zertifikat dar mit welchem der Updater die Updates und der Client die Core-Plugins verifiziert. Für Testzwecke ist ein passender Keystore zum mitgelieferen Zertifikat vorhanden (siehe Maven Build Script).

- im Ordner: `/core/updater/src/main/java/de/danner_web/studip_client/model` die Datei `UpdateServer_RENAME.java` zu `UpdateServer.java`. Sie implementiert die Verbindung zu einem Updateserver. Die Beispieldatei (UpdateServer_RENAME.java) stellt die Datei currentversion_signed.jar aus dem build Ordner zur Verfügung.

Der Updater ist standardmäßig auf unseren Server konfiguriert. Die Server-seitigen Update-Scripte werden vorraussichtlich nicht Open Source veröffenlticht.

##### Maven
TODO: was wird in welcher phase mit welchem profil kompiliert, ...
