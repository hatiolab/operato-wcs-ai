# HELP module

사용자에게 어플리케이션 사용과 관련한 온라인 도움말을 제공하는 목적의 모듈이다.

각 도움말 컨텐츠는 계층 구조의 topic으로 작성되며, 내용은 [Marked Documentation](https://marked.js.org/) 형식으로 작성된다.

## 토픽 작성

- 온라인 헬프를 제공하고자 하는 모듈에서는 각 모듈의 루트 폴더 아래에 'helps' 폴더를 생성한다.
- 'helps' 폴더 이하의 패스이름이 도움말 'topic' 에 매핑된다. 예를 들어, '/helps/module/help.md' 파일은 topic 'module/help' 의 내용이 된다.
- 파일 확장자 .md 는 markdown 파일임을 의미하며, 그 내용은 [Marked Documentation](https://marked.js.org/) 형식으로 작성한다.
- 각 모듈별로 작성한 도움말들은 프레임워크에 의해서 최종 어플리케이션 레벨에서 통합된다. 그러므로 각 모듈에서 작성되는 도움말의 토픽이 중복되지 않도록 토픽 구조의 관습을 따르는 것을 권장한다.
- 토픽 구조
  - 'module/{module-name}'
    - 각 모듈에 대한 설명을 제공한다. 이 토픽은 주로 things-factory 프레임워크를 이해해서 어플리케이션을 개발하고자하는 개발자를 대상으로 하는 매뉴얼에 연결될 것이다.
  - '{module-name}/...'
    - 각 모듈에서 제공하는 기능을 어플리케이션 사용자를 위해 작성하는 경우에는, module-name 으로 시작하는 토픽 구조에 작성한다.
    - 각 모듈내에서는 자유로운 하위 구조로 작성한다.
    - module-name 은 심플하지만 유일한 이름을 사용하는 것을 권장하며, 모듈 뒤의 'base', 'ui' 등의 postfix를 빼는 것을 권장한다.
      - integration <= integration-base, integration-ui
      - grist <= grist-ui
      - import <= import-base, import-ui
    - scene 모듈의 경우에는 각 scene 컴포넌트의 통합된 구성을 위해서 'scene/component/{component-name}' 과 같이 만드는 것을 관습으로 정한다.
      - 'scene/component/connection-control'
      - 'scene/component/simple-switch'
- Assets
  - 각 헬프 페이지에서 참조되는 이미지 동영상 등 asset들은 /helps/{asset-type}s/{module-name}/.. 에 두도록 한다.
  - 이 파일들 역시 최종 어플리케이션 레벨에서 통합되므로, 다른 모듈의 파일들과 중복되지 않도록 module-name 폴더 아래에 위치하도록 한다.
  - 예시
    - /helps/images/integration/abc.png
    - /helps/videos/integration/xyz.mov

## 다국어 제공 (i18n)

- The help(markdown) file is selected in response to the user's language information.
- help file finding order (ie. user language is 'ko-KR' and topic is 'about/things-factory' and fallback language is 'en')
  1. /helps/about/things-factory.ko-KR.md
  2. /helps/about/things-factory.ko.md
  3. /helps/about/things-factory.en.md
  4. /helps/about/things-factory.md
