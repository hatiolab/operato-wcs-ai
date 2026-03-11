# Dashboard module

- 어플리케이션의 대표 대시보드 페이지 설정을 위한 모듈이다.

## 사용 방법

- 대시보드 기능을 사용하고자 하는 어플리케이션의 모듈 디펜던시로 @things-factory/dashboard를 설정한다.

```
$ yarn workspace @things-factory/{target-application} add @things-factory/dashboard
```

```
# package.json
dependencies: {
  ...
  "@things-factory/dashboard": "^...",
  ...
}
```

- 위와 같이 패키지 디펜던시에 추가되면, 어플리케이션의 세팅 화면에 대시보드 세팅 기능이 추가되게 된다.
  - 대시보드 설정은 '데스크탑용 대시보드'와 '모바일용 대시보드'를 설정할 수 있다.
  - '모바일용 대시보드'를 따로 설정하지 않으면, '데스크탑용 대시보드'가 같이 적용된다.
- 대시보드 클라이언트 페이지는 'dashboard' 로 지정되어있다.
- 대시보드 설정은 개개인별로 적용되지 않고, 해당 어플리케이션의 도메인별로 적용된다.
  - 대시보드 설정이 변경되면, 해당 도메인의 모든 유저의 대시보드가 변경되므로 주의하여야 한다.
