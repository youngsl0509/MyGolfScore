# My Golf Score Android 앱 설정 및 APK 빌드 가이드

이 프로젝트는 패키지명 `com.mygolfscore.app` 기준으로 구성되어 있으며, 구글 드라이브의 `GolfScore` 폴더(`myGolfScore.json` 다운로드) 및 `GolfScore/Image` 폴더(스코어카드 이미지 업로드)와 직접 연동됩니다.

---

## 1. Google Cloud Console OAuth 설정 (필수)

안드로이드 앱이 본인 구글 드라이브에 접근하려면 OAuth 2.0 Android 클라이언트 ID가 필요합니다.

1. [Google Cloud Console](https://console.cloud.google.com/)에 접속해 새 프로젝트(예: `MyGolfScore`)를 만듭니다.
2. **API 및 서비스 > 라이브러리**에서 **Google Drive API**를 검색해 **사용(Enable)**을 클릭합니다.
3. **API 및 서비스 > OAuth 동의 화면**으로 이동해:
   - User Type: **외부(External)** 선택 후 앱 이름(`My Golf Score`), 이메일 입력
   - 테스트 사용자(Test users)에 **본인 Gmail 주소** 추가 (중요!)
4. **API 및 서비스 > 사용자 인증 정보 > + 사용자 인증 정보 만들기 > OAuth 클라이언트 ID** 선택:
   - 애플리케이션 유형: **Android**
   - 패키지 이름: `com.mygolfscore.app`
   - SHA-1 인증서 디지털 지문: 아래 명령어로 확인한 디버그 키 지문 입력
     ```bash
     # macOS / Linux
     keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android | grep SHA1

     # Windows (PowerShell / CMD)
     keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
     ```
5. 생성을 누르면 설정 완료입니다. (Android 앱은 패키지명 + SHA-1 지문으로 자동 인증되므로 별도 클라이언트 시크릿 파일을 넣지 않아도 됩니다.)

---

## 2. Android Studio에서 APK 빌드 및 설치

1. 압축을 푼 `MyGolfScoreApp` 폴더를 **Android Studio**에서 **Open**으로 엽니다.
2. Gradle 동기화가 완료되면 상단 메뉴에서 **Build > Build Bundle(s) / APK(s) > Build APK(s)**를 클릭합니다.
3. 빌드가 끝나면 우측 하단 알림의 **locate** 링크를 눌러 `app/build/outputs/apk/debug/app-debug.apk` 파일을 확인합니다.
4. 안드로이드 폰을 USB로 연결해 **Run(▶)** 버튼으로 바로 설치하거나, 생성된 `app-debug.apk` 파일을 폰으로 옮겨 설치합니다.

---

## 3. 앱 주요 기능

- **요약 탭**: 전체 누적(라운딩 수, 평균 타수, 파 대비, 최근 5경기 평균, 최저 타수 라베), 올해(2026년) 요약, 누적 스코어 분포 막대 차트 표시
- **기록 탭**: 최신순 라운딩 목록 및 각 경기 클릭 시 전반/후반 홀별 상세 스코어카드 확인
- **업로드 탭**: 갤러리에서 스코어카드 이미지를 선택해 구글 드라이브 `GolfScore/Image` 폴더로 바로 업로드
- **내장 샘플 모드**: 구글 로그인 전에도 `app/src/main/assets/myGolfScore.json`의 최신 12경기 데이터를 바로 미리볼 수 있습니다.
