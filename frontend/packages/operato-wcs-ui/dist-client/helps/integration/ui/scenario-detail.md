# Scenario Detail

시나리오 상세 정의 화면에서는 시나리오를 구성하는 스텝 리스트를 구성한다.

## step

스텝은 시나리오의 진행 순서대로 구성되는 단위 태스크이다.
각 스텝의 이름(name)은 전체 시나리오의 생명주기동안 관리되는 컨텍스트 데이타를 접근하는 이름이다.
시나리오를 구성하는 스텝은 시나리오엔진에 의해서 순서대로 수행되게되며, 특별히 플로우 컨트롤 태스크에 의해서 실행순서가 변경될 수 있다.

## flow control task

- end
- goto
- switch (switch-goto)
- switch-range (switch-range-goto)
- throw

## properties

- name
- description
- skip
- log
- connection
- task
- parameter

## operations

- add step
- remove step
- sequence move up / down
- save
- delete
