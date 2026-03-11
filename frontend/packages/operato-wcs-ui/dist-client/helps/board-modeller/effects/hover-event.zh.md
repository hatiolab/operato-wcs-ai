# 悬停事件

定义将鼠标悬停在组件上时的行为。

## 동작

### 강조

产生突出显示组件边框的效果。

### 동작

- popup target board
  - 타겟으로 지정한 대상 보드를 팝업창에 보인다.
  - 팝업창의 위치는 팝업 대상 보드의 크기와 호버된 컴포넌트의 위치를 감안해서 다음의 위치중 한 곳에 보여진다.
    - top-left, top-right, bottom-left, bottom-right, center
  - 팝업창의 크기는 현재 보드의 크기보다 커서는 안된다. 팝업 대상 보드의 크기가 현재 보드보다 크면, 팝업 대상 보드의 모든 내용을 보기 힘들게 된다.
  - 팝업창을 닫기 위해서는 팝업창 우측 상단의 'X' 클로즈 버튼을 누르거나, 대상 보드의 특정 컴포넌트의 탭이벤트가 'close current board'로 설정되어있다면, 해당 컴포넌트를 클릭해서 닫을 수 있다.
  - 'restore on leave(나갈 때 원상태로 복구)' 설정에 영향을 받지 않는다.
  - 파라미터
    - target : 팝업창으로 보여줄 대상 보드를 보드목록에서 지정한다.
- open infowindow
  - 'info-window' 컴포넌트에 설정된 내용으로 호버된 포인트에 근접한 위치에 인포윈도우를 띄워준다.
  - 'restore on leave(나갈 때 원상태로 복구)' 설정이 되어있으면, 마우스 포인트가 컴포넌트에서 벗어날 때 인포윈도우도 클로즈된다.
  - 파라미터
    - target : 대상 인포윈도우의 아이디를 지정한다.
- toggle target component data
  - 대상 컴포넌트의 현재 데이타를 기준으로 불린값 true/false 순서로 데이타를 토글시킨다.
  - 파라미터
    - target : 데이타를 토글시킬 대상 컴포넌트
- tristate(0/1/2) target component data
  - 대상 컴포넌트의 현재 데이타를 기준으로 숫자 0/1/2 순서로 데이타를 순환시킨다.
  - 파라미터
    - target : 데이타를 순환시킬 대상 컴포넌트
- set value to target component data
  - 설정한 값으로 대상 컴포넌트의 'data'를 변경한다.
  - 보통 컴포넌트의 data를 변화시키는 것은 대상 컴포넌트에 지정된 데이타 스프레드를 트리거링하는 효과를 발휘한다.
  - 파라미터
    - target : data를 변경할 대상 컴포넌트
    - value : 대상 컴포넌트에 설정될 값 (문자열, 숫자 또는 오브젝트 타입을 지정할 수 있다)
- set value to target component value
  - 설정한 값으로 대상 컴포넌트의 'value'를 변경한다.
  - 보통 컴포넌트의 value를 변화시키는 것은 대상 컴포넌트의 지정된 동작을 트리거링하는 효과를 발휘한다.
  - 파라미터
    - target : value를 변경할 대상 컴포넌트
    - value : 대상 컴포넌트에 설정될 값 (문자열, 숫자 또는 오브젝트 타입을 지정할 수 있다)

## restore on leave (나갈 때 원상태로 복구)

마우스가 해당 컴포넌트를 벗어날 때, 다음의 효과는 취소되게 하는 설정이다.

- 강조
- open infowindow
- toggle target component data
- tristate(0/1/2) target component data
