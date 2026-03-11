1. "/" Route로 접근 시 우선 Auth-base의 siteRootRouter내 "/"로 접근
   ```
   siteRootRouter.get('/', findAuth, domainMiddleware, async (context, next)
   ```
   이 후 domainMiddleware -> findAuth를 순차적으로 호출함
2. 문제점
   --> 인증 화면을 표현하려면 현재 기준으로 Root path에 대한 구현을 변경이 필요 함
   --> Auth Base내 일부 로직 처리를 Spring으로 전달하여 처리할 수 있도록 설정 필요
3. 