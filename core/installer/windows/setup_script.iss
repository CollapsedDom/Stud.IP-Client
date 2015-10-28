; Script generated by the Inno Setup Script Wizard.
; SEE THE DOCUMENTATION FOR DETAILS ON CREATING INNO SETUP SCRIPT FILES!

#define MyAppName "Stud.IP Client"
#define MyAppVersion "1.0"
#define MyAppPublisher "Dominik and Philipp Danner"
#define MyAppURL "http://studip-client.danner-web.de"
#define MyAppExeName "StudIP Client.exe"

[Setup]
; NOTE: The value of AppId uniquely identifies this application.
; Do not use the same AppId value in installers for other applications.
; (To generate a new GUID, click Tools | Generate GUID inside the IDE.)
AppId={{6CAACE61-BC14-42BE-8242-F46A1DFF77C0}
AppName={#MyAppName}
AppVerName={#MyAppName}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
DefaultDirName={userpf}\{#MyAppName}
DefaultGroupName={#MyAppName}
AllowNoIcons=yes
OutputDir=..\..\..\build
OutputBaseFilename=setup
SetupIconFile=..\studip.ico
UninstallDisplayIcon={app}\{#MyAppExeName}
Compression=lzma
SolidCompression=yes
DisableDirPage=yes
MinVersion=6.0

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"
Name: "german"; MessagesFile: "compiler:Languages\German.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
Source: "..\..\..\build\StudIP Client.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\..\..\build\currentversion\updater.jar"; DestDir: "{app}"; Flags: ignoreversion
; NOTE: Don't use "Flags: ignoreversion" on any shared system files

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{commondesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent

[InstallDelete]
Type: filesandordirs; Name: "{userpf}\{#MyAppName}"

[UninstallDelete]
Type: filesandordirs; Name: "{userpf}\{#MyAppName}"
Type: files; Name: "{userstartup}\StudIP Client.lnk"

[Registry]
Root: HKCU; Subkey: "Software\JavaSoft\Prefs\de\danner_web\studip_client"; Flags: deletekey uninsdeletekey

[Code]
function IsAppRunning: Boolean;
var
  FWMIService: Variant;
  FSWbemLocator: Variant;
  FWbemObjectSet: Variant;
begin
  Result := false;
  FSWbemLocator := CreateOleObject('WBEMScripting.SWBEMLocator');
  FWMIService := FSWbemLocator.ConnectServer('', 'root\CIMV2', '', '');
  FWbemObjectSet := FWMIService.ExecQuery('SELECT * FROM Win32_Process WHERE CommandLine like "java -Dname=StudIP_client -jar%"');  
  Result := (FWbemObjectSet.Count > 0);
  FWbemObjectSet := Unassigned;
  FWMIService := Unassigned;
  FSWbemLocator := Unassigned;
end;

function InitializeSetup: Boolean;
begin
  Result := not IsAppRunning();
  if not Result then
  MsgBox('{#MyAppName} is running. Please close the application before running the installer ', mbError, MB_OK);
end;

function InitializeUninstall: Boolean;
begin
  Result := not IsAppRunning();
  if not Result then
  MsgBox('{#MyAppName} is running. Please close the application before running the installer ', mbError, MB_OK);
end;