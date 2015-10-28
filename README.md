###Development
Wir arbeiten ausschließlich mit Eclipse, daher sind vordefinierte maven und run launcher für eclipse unter /_eclipse_launch vorhanden.
Der "Run StudIP Client.launch" setzt das Java working directory des ausgeführten Programmes auf /bin und fügt sowohl alle src Ordner (für Debugging) als auch alle default bin Ordner (aktuelle class Dateien) hinzu. Sofern sich beteis compilierte Plugins in /bin/plugins befinden (mvn package), werden dieses dann im StudIP Client angezeigt.

####Projekt Checkout (Eclipse)
1. Im Project Explorer rechts Klick -> Import
2. Maven -> Check out Maven Projects from SCM auswählen -> next
3. Unten rechts m2e Marketplace Link anklicken und "m2e-egit" Connector installieren
4. Schritt 1-2 wiederholen
5. SCM URL: git auswählen und URL ergänzen
6. (Optional) Workspace location ändern
7. Finish

Ab Eclipse Mars ist es auch möglich, multi-module Maven Projekte hierarchisch anzuzeigen. Dazu im Project Explorer -> Dreieck nach utnen -> Project Presentation -> Hierarchical

####Maven
TODO: was wird in welcher phase mit welchem profil kompiliert, ...
