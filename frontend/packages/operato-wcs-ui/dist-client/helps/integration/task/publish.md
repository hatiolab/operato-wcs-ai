# publish

subscription API를 통해서 요청된 모든 Subscriber에게 데이타를 브로드캐스트하는 태스크이다.

클라이언트 어플리케이션에서 data subscription을 요청하는 방법은

- Board UI에서 subscriber를 통해서 요청할 수 있다.
- wss://[server host:port]/subscription API를 통해서 subscribe 요청할 수 있다.

## parameters

- [accessor](../concept/data-accessor)
  - 시나리오 내의 하나의 스텝이름으로, 브로드캐스트하고자 하는 데이타를 지정한다.
- tag
  - 데이타를 브로드캐스트할 때 태그를 부여해서 보내야한다.
  - subscriber들도 data를 subscribe할 때, 받고자 하는 테그를 설정하여, 해당 태그가 부여된 브로드캐스트만 받을 수 있다.
