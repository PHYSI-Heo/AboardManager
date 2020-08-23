# Aboard Manager

* Web Server setting
1. Mysql 설치 
2. Node js(LTS version) 설치 - https://nodejs.org/ko/
3. [Server] Folder -> [myDB.js] File -> MYSQLID, MYSQLPWD 변경 (Mysql 설치 시 입력 ID/Password)
4. Cmd(명령 프롬프트) 실행 -> [Server] Folder 경로 이동 -> "node app" 입력 & 엔터 
- Example >> C:\Users\HEO\Desktop\Server>node app

* Parent App
1. [./http/HttpPacket.java] File -> BASEURL 정보 변경 (개인서버 IP 주소) 
2. [AndroidManifest.xml] File -> Google Map API Key 정보 변경 (line 41)
ref - https://developers.google.com/maps/documentation/android-sdk/get-api-key?hl=ko

* Manager App
1. [./http/HttpPacket.java] File -> BASEURL 정보 변경 (개인서버 IP 주소) 
