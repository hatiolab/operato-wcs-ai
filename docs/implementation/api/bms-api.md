# BMS 모듈 API 목록 (BMS Module API List)

## 추천 박스 API (`RecommandBoxController`)
* **기본 경로(Base Path)**: `/rest/bms`
* **설명**: 상품 크기 및 체적을 기반으로 가장 적합한 포장 박스를 추천하는 API

| HTTP Method | 경로 (Path) | 기능 설명 | 파라미터 (Parameters) | 리턴 타입 (Return Type) |
|---|---|---|---|---|
| POST | `/recommand/etc` | 기타 상품 추천 박스 조회 | [Body] `BmsRequest bmsRequest` | `Map<String, BmsOrder>` |
