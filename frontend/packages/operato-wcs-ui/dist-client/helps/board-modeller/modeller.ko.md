# Board Modeller

## Modeller Layout

모델러의 레이아웃에 대해 설명한다.  
![모델러-레이아웃][layout]

1. 제어툴바: 되돌리기, 복사, 정렬, 반전 등 컴포넌트를 제어하는 기능이 모여있는 툴바.

1. 컴포넌트 툴바: 모델러에서 사용할 수 있는 컴포넌트가 모여있는 툴바.

1. 속성창: 컴포넌트의 속성이 모여있는 창.


[layout]: ./images/modeler-layout-01.png


## 제어툴바

현재 모델링 중인 SCENE의 정보와 모델링을 더욱 간편하게 하기위해 필요한 기능들을 나열해 놓은 툴 바.  
<span style="font-size: 13px; line-height:30px;">  (OS가 Mac인 경우는 <kbd>Ctrl</kbd>키를 <kbd class="dark">⌘</kbd>로 바꿔서 입력한다.) </span>

1. 되돌리기![되돌리기][Undo](<kbd>Ctrl</kbd> + <kbd>Z</kbd>) :  
  사용자가 마지막으로 편집한 내용을 되돌릴 수 있는 기능

1. 다시실행![다시실행][Redo](<kbd>Ctrl</kbd> + <kbd>Shift</kbd> + <kbd>Z</kbd>) :  
  되돌리기로 취소했던 작업의 내용을 다시 수행

1. 잘라내기![잘라내기][Cut](<kbd>Ctrl</kbd> + <kbd>X</kbd>) :  
  선택된 컴포넌트를 클립보드로 복사 하면서 현재의 컴포넌트는 삭제

1. 복사![복사][Copy](<kbd>Ctrl</kbd> + <kbd>C</kbd>) :  
  선택된 컴포넌트를 클립보드로 복사

1. 붙여넣기![붙여넣기][Paste](<kbd>Ctrl</kbd> + <kbd>V</kbd>) :  
  클립보드로 복사한 컴포넌트를 붙여넣음

1. 삭제![삭제][Delete](<kbd>Delete</kbd> or <kbd>Backspace</kbd>) :  
  선택된 컴포넌트를 삭제

1. 왼쪽 정렬![왼쪽 정렬][Align left](<kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>L</kbd>) :  
  선택된 컴포넌트들의 가장 왼쪽에 있는 컴포넌트를 기준으로 컴포넌트들을 왼쪽으로 정렬

1. 가운데 정렬![가운데 정렬][Align center](<kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>C</kbd>) :  
  선택된 컴포넌트들의 중앙 좌표를 기준으로 컴포넌트들의 x좌표를 가운데로 정렬

1. 오른쪽 정렬![오른쪽 정렬][Align right](<kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>R</kbd>) :  
  선택된 컴포넌트들의 가장 오른쪽에 있는 컴포넌트를 기준으로 컴포넌트들을 오른쪽으로 정렬

1. 위쪽 정렬![위쪽 정렬][Align top](<kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>T</kbd>) :  
  선택된 컴포넌트들의 가장 위쪽에 있는 컴포넌트를 기준으로 컴포넌트들을 위쪽으로 정렬

1. 중앙 정렬![중앙 정렬][Align midle](<kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>M</kbd>) :  
  선택된 컴포넌트들의 중앙 좌표를 기준으로 컴포넌트들의 y좌표를 가운데로 정렬

1. 아래쪽 정렬![아래쪽 정렬][Align bottom](<kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>B</kbd>) :  
  선택된 컴포넌트들의 가장 아래쪽에 있는 컴포넌트를 기준으로 컴포넌트들을 아래쪽으로 정렬

1. 가로 간격을 동일하게![가로 간격을 동일하게][Distribute horizontally]](<kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>H</kbd>) :  
  선택된 컴포넌트들의 가로방향 양 끝에 있는 컴포넌트를 기준으로 각 컴포넌트 사이의 폭을 같게 조절

1. 세로 간격을 동일하게![세로 간격을 동일하게][Distribute vertically](<kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>V</kbd>) :  
  선택된 컴포넌트들의 세로방향 양 끝에 있는 컴포넌트를 기준으로 각 컴포넌트 사이의 폭을 같게 조절

1. 맨 앞으로 가져오기![맨 앞으로 가져오기][Bring to front](<kbd>Ctrl</kbd> + <kbd>Shift</kbd> + <kbd>F</kbd>) :  
  선택된 컴포넌트의 순서를 맨 앞으로 보냄

1. 맨 뒤로 보내기![맨 뒤로 보내기][Send to back](<kbd>Ctrl</kbd> + <kbd>Shift</kbd> + <kbd>B</kbd>) :  
  선택된 컴포넌트의 순서를 맨 뒤로 보냄

1. 앞으로 가져오기![앞으로 가져오기][Bring forward](<kbd>Ctrl</kbd> + <kbd>F</kbd>) :  
  선택된 컴포넌트의 순서를 한 칸 앞으로 보냄

1. 뒤로 보내기![뒤로 보내기][Send backward](<kbd>Ctrl</kbd> + <kbd>B</kbd>) :  
  선택된 컴포넌트의 순서를 한 칸 뒤로 보냄

1. 좌우반전![좌우반전][Mirror horizontally](<kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>X</kbd>) :  
  선택된 컴포넌트를 가로를 기준으로 대칭

1. 상하반전![상하반전][Mirror vertically]](<kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>Y</kbd>) :  
  선택된 컴포넌트를 세로를 기준으로 대칭

1. 시계 방향으로 회전![시계 방향으로 회전][Rotate clockwise](<kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>E</kbd>) :  
  선택된 컴포넌트를 시계 방향으로 90도 회전

1. 반시계 방향으로 회전![반시계 방향으로 회전][Rotate counterclockwise](<kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>W</kbd>) :  
  선택된 컴포넌트를 반시계 방향으로 90도 회전

1. 그룹![그룹][Group](<kbd>Ctrl</kbd> + <kbd>G</kbd>) :  
  선택된 컴포넌트들을 그룹으로 묶음

1. 그룹 해제![Ungroup][그룹 해제](<kbd>Ctrl</kbd> + <kbd>Shift</kbd> + <kbd>G</kbd>) :  
  선택된 그룹을 해제

1. 텍스트 크기 증가![텍스트 크기 증가][Increase text size](<kbd>Shift</kbd> + <kbd>WheelUp</kbd>) :  
  선택된 컴포넌트의 텍스트 크기를 증가

1. 텍스트 크기 감소![텍스트 크기 감소][Decrease text size](<kbd>Shift</kbd> + <kbd>WheelDown</kbd>) :  
  선택된 컴포넌트의 텍스트 크기를 감소

1. 캔버스 크기 맞추기![캔버스 크기 맞추기][Fit scene](<kbd>Ctrl</kbd> + <kbd>D</kbd>) :  
  캔버스의 크기를 화면에 딱 맞게 리사이즈 시킴
  
1. 미리보기![미리보기][Preview](<kbd>Ctrl</kbd> + <kbd>P</kbd>) :  
  편집하고 있는 모델링 화면의 3D 컴포넌트, 애니메이션 등 효과를 미리 확인  

1. 전체 화면![전체 화면][Full screen](<kbd>F11</kbd>) :  
  툴바와 속성창들을 모두 숨기고 모델링 화면을 전체화면으로 변환

1. 속성 창 숨기기![속성 창 숨기기][Toggle property panel](<kbd>Ctrl</kbd> + <kbd>H</kbd>) :  
  속성 창을 숨기거나 보여줌

[control-toolbar]: ./images/control-toolbar.png

[Undo]: ./images/control-toolbar-02.png

[Redo]: ./images/control-toolbar-03.png

[Cut]: ./images/control-toolbar-04.png

[Copy]: ./images/control-toolbar-05.png

[Paste]: ./images/control-toolbar-06.png

[Delete]: ./images/control-toolbar-07.png

[Copy style]: ./images/control-toolbar-08.png

[Align left]: ./images/control-toolbar-09.png

[Align center]: ./images/control-toolbar-10.png

[Align right]: ./images/control-toolbar-11.png

[Align top]: ./images/control-toolbar-12.png

[Align midle]: ./images/control-toolbar-13.png

[Align bottom]: ./images/control-toolbar-14.png

[Distribute horizontally]: ./images/control-toolbar-15.png

[Distribute vertically]: ./images/control-toolbar-16.png

[Bring to front]: ./images/control-toolbar-17.png

[Send to back]: ./images/control-toolbar-18.png

[Bring forward]: ./images/control-toolbar-19.png

[Send backward]: ./images/control-toolbar-20.png

[Mirror horizontally]: ./images/control-toolbar-21.png

[Mirror vertically]: ./images/control-toolbar-22.png

[Rotate clockwise]: ./images/control-toolbar-23.png

[Rotate counterclockwise]: ./images/control-toolbar-24.png

[Group]: ./images/control-toolbar-25.png

[Ungroup]: ./images/control-toolbar-26.png

[Increase text size]: ./images/control-toolbar-27.png

[Decrease text size]: ./images/control-toolbar-28.png

[Fit scene]: ./images/control-toolbar-29.png

[Preview]: ./images/control-toolbar-32.png

[Full screen]: ./images/control-toolbar-30.png

[Toggle property panel]: ./images/control-toolbar-31.png


## 컴포넌트 툴바

모델러에서 사용가능한 컴포넌트가 모여있는 툴바. 컴포넌트 툴바에서 원하는 컴포넌트를 클릭하면 모델러에 해당 컴포넌트가 추가된다.  


- ![화살표][component-arrow] : 선택모드와 이동모드를 변경한다. <kbd>Space</kbd>를 누르고 있는 동안 이동모드로 작동한다.

- ![선][component-line] :선 컴포넌트가 모여있다.

- ![도형][component-shape] :도형 컴포넌트가 모여있다.

- ![텍스트 미디어][component-text-media] :텍스트 미디어 컴포넌트가 모여있다.

- ![차트 게이지][component-chart-gauge] :차트 게이지 컴포넌트가 모여있다.

- ![표][component-table] :표 컴포넌트가 모여있다.

- ![컨테이너][component-container] :컨테이너 컴포넌트가 모여있다.

- ![데이터소스][component-datasource] :데이터소스 컴포넌트가 모여있다.

- ![IOT][component-iot] :IOT 컴포넌트가 모여있다.

- ![3D][component-3D] :3D 컴포넌트가 모여있다.

- ![창고][component-warehouse] :창고 컴포넌트가 모여있다.

- ![폼][component-form] :폼 컴포넌트가 모여있다.

- ![기타][component-etc] :기타 컴포넌트가 모여있다.


[component-toolbar]: ./images/component-toolbar.png
[component-arrow]: ./images/component-arrow.png
[component-line]: ./images/component-line.png
[component-shape]: ./images/component-shape.png
[component-text-media]: ./images/component-text-media.png
[component-chart-gauge]: ./images/component-chart-gauge.png
[component-table]: ./images/component-table.png
[component-container]: ./images/component-container.png
[component-datasource]: ./images/component-datasource.png
[component-iot]: ./images/component-iot.png
[component-3D]: ./images/component-3D.png
[component-warehouse]: ./images/component-warehouse.png
[component-form]: ./images/component-form.png
[component-etc]: ./images/component-etc.png

## 속성창

속성창의 기능들에 대해 설명한다.  

1. ![모양속성탭][shape-property-tab]: 컴포넌트의 모양에 관련된 속성. ID, class를 부여하거나, 너비, 높이 등을 지정할 수 있다.

1. ![스타일속성탭][style-property-tab]: 컴포넌트의 스타일에 관련된 속성. 색상, 폰트 등을 지정할 수 있다.

1. ![효과속성탭][effect-property-tab]: 컴포넌트의 효과에 관련된 속성. 그림자, 애니메이션, 클릭시 이벤트를 지정할 수 있다.

1. ![특정속성탭][specific-property-tab]: 컴포넌트 고유 속성.

1. ![데이터속성탭][variable-property-tab]: Scene의 데이터에 관련된 속성. 

[property-window]: ./images/property-window.png
[shape-property-tab]: ./images/shape-property.png
[style-property-tab]: ./images/style-property.png
[effect-property-tab]: ./images/effect-property.png
[specific-property-tab]: ./images/specific-property.png
[variable-property-tab]: ./images/variable-property.png
