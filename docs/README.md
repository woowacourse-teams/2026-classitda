# Pheeeew 법적 고지 배포 체크리스트

이 디렉터리는 GitHub Pages에 그대로 게시할 수 있는 정적 HTML입니다. JavaScript, 로그인, 편집 기능 및 PDF가 없어 Google Play 개인정보처리방침 URL 요건에 맞는 형태입니다.

## 공개 전 필수 작업

1. Play Console 개발자명이 개인정보 처리방침의 운영 주체명인 `Pheeew 운영팀`과 일치하는지 확인합니다.
2. 개인정보 문의 주소 `contact@pheeeew.com`에서 실제로 메일을 수신할 수 있는지 확인합니다.
3. 저장소 Settings > Pages에서 배포 소스를 기본 브랜치의 `/docs`로 지정합니다.
4. 아래 URL을 로그인하지 않은 브라우저와 시크릿 창에서 확인합니다.
   - `https://woowacourse-teams.github.io/2026-pheeeew/privacy-policy.html`
   - `https://woowacourse-teams.github.io/2026-pheeeew/open-source-licenses.html`
5. 첫 번째 URL을 Play Console의 정책 및 프로그램 > 앱 콘텐츠 > 개인정보처리방침에 입력합니다.
6. 앱의 설정 > 개인정보 처리방침 및 오픈소스 라이선스 메뉴가 각 URL을 여는지 릴리스 빌드에서 확인합니다.
7. Play Console 데이터 보안 답변이 실제 출시 빌드와 이 처리방침에 일치하는지 다시 검토합니다.

## 출시 빌드가 바뀔 때

- 서버에 한숨 좌표를 등록하거나 공유하는 기능이 활성화되면 위치 정보의 전송 대상, 목적, 보유 기간, 삭제 방법을 방침과 데이터 보안 양쪽에 추가해야 합니다.
- 분석, 크래시 수집, 광고, 푸시, 계정, 문의 SDK를 추가하면 해당 SDK의 데이터 처리까지 반영해야 합니다.
- OpenFreeMap 또는 다른 지도 제공자를 바꾸면 제3자 제공·국외 처리 내용과 지도 출처를 갱신해야 합니다.
- 라이브러리 또는 글꼴을 추가·변경하면 `open-source-licenses.html`의 버전과 라이선스를 갱신해야 합니다.
