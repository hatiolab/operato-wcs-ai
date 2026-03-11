# OOPS Progress 컴포넌트

긴 시간 진행되는 작업의 진행 상황을 표현해주기 위해 서버에 진행율을 Subscribe하는 클라이언트 컴포넌트이다.
이 컴포넌트는 자체적으로 시각적 요소를 포함하지 않지만, 시각적 표현을 위해서 자식 노드(Light DOM)를 포함할 수는 있다.

## properties

- tag: String 서브스크립션 태그이며, 서버의 'publishProgress'의 tag와 일치시키도록 한다.
- subscription: String (readonly) 이 속성이 값을 가지고 있으면, 서브스크립션 중임을 의미한다.

## methods

- startSubscribe() 진행율 구독을 시작한다.
- stopSubscribe() 진행율 구독을 종료한다.(서버쪽 작업의 종료를 의미하지 않으며, 클라이언트에서 구독을 종료한다는 의미임.)

## event

- progress(event) 서버로부터 진행율이 Subscribe 되었을 때 발생한다.
  - event.detail : progress 값을 갖는다.
- finish(event) 진행율이 100% 도달하면 발생한다.

## 예시

### 클라이언트 사이드

```
  <oops-progress
    .tag='progress-pending-job'
    @progress=${e => {
      this.progress = e.detail
    }}
    @finish=${() => {
      console.log('complete')
    }}
    ?hidden=${progress < 0 || progress > 100}
  >
    <div>
      <mwc-linear-progress .progress=${progress / 100}></mwc-linear-progress>
      <span>Progress : ${progress} % (${message})</span>
    </div>
  </oops-progress>

```

### 서버사이드

```
import { sleep } from '@things-factory/utils'

...

@Mutation(returns => String, { description: 'To reference of pending job progress' })
async referencePendingJob(
  @Root() _,
  @Ctx() context: any
) : Promise<string> {

  const { domain } = context.state

  for(var i = 0;i <= 100;i++) {
    await sleep(100)

    publishProgress({
      domain,
      tag: 'progress-pending-job',
      progress: i,
      message: `${i * 10} / 1000`
    })
  }

  return 'success'
}
```
