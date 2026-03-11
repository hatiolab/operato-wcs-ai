# module - Notification

- 사용자에게 알림을 전달하는 기능
- 백그라운드 알림 (Push notification)
  - 사용자가 어플리케이션을 사용하는 중이 아니더라도 알림을 전달
  - 웹푸시 기능을 사용하며, Firebase Cloud Messaging (FCM)을 사용한다.
- 클라이언트 알림 (In-app messaging)
  - 사용자가 어플리케이션을 사용하는 중에 알림을 전달
  - 웹푸시 기능을 사용할 수 없거나, 백그라운드 알림 기능 사용을 원하지 않을 때, 웹소켓을 사용하여 어플리케이션 사용 중에만 알림을 전달한다.
- 오류 알림
  - 사용자가 어플리케이션 조작 중에 발생하는 오류 등을 유지

## terminology

- notification
  - 코드 상의 notification 용어는 주로 알림과 관련한 데이타 형식을 의미한다.
- notify
  - 사용자에게 알림을 전달하는 행위를 의미한다.

## techonologies

- web push notification
- fcm
- service worker

## middlewares

- notification-middleware
  - koa-context의 state에 notify 라는 함수를 전달한다.
  - 코드 내에서는 notify함수를 사용해서 알림을 전달할 수 있다.
  - notify 기능의 표준화를 위해서 제공된다.
- directive-notification
  - resolver 수행 이후에 자동으로 해당 도메인의 사용자들에게 알림을 전달한다.
  - TODO more descriptions about directive notification

## security

- 매우 강력한 보안 기준을 만족해야 pubsub 채널에 조인할 수 있다.
- websocket은 tenancy 인증을 완료한 사용자로부터의 연결 요청만을 허가한다.
- 해당 tenancy(domain)의 jwt-authentication을 가지고 있어야만 pubsub 그룹에 조인할 수 있다.

## common elements

### notification-setting-let

![notification-setting-let](./notification-images/element-notification-setting-let.png)

### notification-badge

![notification-badge](./notification-images/element-notification-badge.png)

### notification-list

![notification-list](./notification-images/element-notification-list.png)

## usages

### configuration

- config.(development|production).js

```
  notification: {
    fcm: {
      serviceAccount: {
        project_id: "----",
        private_key: "-----BEGIN PRIVATE KEY-----\nMIIEvQIB-----END PRIVATE KEY-----\n",
        client_email: "-----"
      },
      appConfig: {
        apiKey: '-----',
        projectId: '-----',
        messagingSenderId: '-----',
        appId: '-----'
      }
    },
    serverKey: '-----',
    vapidKey: {
      subject: 'mailto:abc@def.xyz',
      publicKey: '-----',
      privateKey: '-----'
    }
  }
```

- you can get serviceAccount configuration from firebase console

![get service account config](./notification-images/config-service-account.png)

- you can get app configuration from firebase console

![get app config menu](./notification-images/config-app-1.png)
![get app config](./notification-images/config-app-2.png)

- you can get serverKey from firebase console

![get server key config](./notification-images/config-server-key.png)

- you can get vapidKey from firebase console

![get vapidkey menu](./notification-images/config-vapidkey-1.png)
![get vapid public key](./notification-images/config-vapidkey-2-get-public-key.png)
![get vapid private key](./notification-images/config-vapidkey-3-get-private-key.png)

## test

### test on chrome dev

- push test on chrome samples (outside url)

```
{
  "notification": {
    "title":"Congratulations on your performance",
    "body": "Daily Acheivement",
    "image": "https://d2mvzyuse3lwjc.cloudfront.net/doc/en/UserGuide/images/Pie_Of_Pie_Chart/Pie_Of_Pie_Chart.png?v=83478",
    "url": "https://github.com/klaude416/wms-notebook/blob/master/step5_shipping.ipynb"
  }
}
```

- inside url sample

```
{
  "notification": {
    "title":"Congratulations on your performance",
    "body": "Daily Acheivement",
    "image": "https://d2mvzyuse3lwjc.cloudfront.net/doc/en/UserGuide/images/Pie_Of_Pie_Chart/Pie_Of_Pie_Chart.png?v=83478",
    "url": "http://board.localhost:3000/system"
  }
}
```

- sample with actions

```
{
  "notification": {
    "title":"Congratulations on your performance",
    "body": "Daily Acheivement",
    "image": "https://d2mvzyuse3lwjc.cloudfront.net/doc/en/UserGuide/images/Pie_Of_Pie_Chart/Pie_Of_Pie_Chart.png?v=83478",
    "url": "https://github.com/klaude416/wms-notebook/blob/master/step5_shipping.ipynb",
    "actions": [{
      "action": "save", "title": "save"
    }, {
      "action": "delete", "title": "delete"
    }]
  }
}
```

![push test on chrome](./notification-images/push-test-on-chrome-1.png)
![push test on chrome result](./notification-images/push-test-on-chrome-2.png)

### test on firebase

![push test on firebase](./notification-images/push-test-on-firebase-1.png)
![push test on firebase](./notification-images/push-test-on-firebase-2.png)
![push test on firebase](./notification-images/push-test-on-firebase-3.png)
![push test on firebase result](./notification-images/push-test-on-firebase-4.png)
