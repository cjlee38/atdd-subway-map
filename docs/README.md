## 요구사항

- [X] 동일한 이름의 지하철역이 있으면 예외 처리
- [X] 지하철 역 관리 API 구현
    - [X] 역 생성
    - [X] 역 목록 조회
    - [X] 역 조회
    - [X] 역 삭제
- [X] 지하철 노선 관리 API 구현
    - [X] 노선 생성
    - [X] 노선 목록 조회
    - [X] 노선 조회
    - [X] 노선 수정
    - [X] 노선 삭제
    
- [X] *노선* 에 대한 E2E 테스트 작성

<br>

## 기능 요구사항

- [x] 스프링 JDBC 활용하여 H2 DB에 저장하기
    - [x] Dao 객체가 아닌 DB에서 데이터를 관리하기
    - [x] DB에 접근하기 위한 spring jdbc 라이브러리를 활용하기 (JdbcTemplate 등)
- [x] H2 DB를 통해 저장된 값 확인하기
    - [x] 실제로 DB에 저장이 잘 되었는지 확인할 수 있도록 설정하기
- [x] 스프링 빈 활용하기
    - [x] 매번 생성하지 않아도 되는 객체와 싱글톤이나 static으로 구현되었던 객체들을 스프링 빈으로 관리해도 좋음

<br>

## 제약 사항 (1단계)

- [X] 스프링 빈 사용 금지(@Service, @Component 등. @Controller 제외)
- [X] 데이터 저장은 XXXDao 사용

## 3단계 지하철 구간 관리 기능 요구사항

<br>

### 노선

<br>

#### 추가
* [x] 추가: 상행, 하행 종점 id와 두 종점간의 거리 입력 (두 종점의 거리를 이용하여 구간 함께 등록)
  * [x] 예외: 존재하지 않는 지하철 역을 등록할 수 없                                                                                                  다.
  * [x] 예외: 중복된 노선 이름을 사용할 수 없다. (이름은 다른데 같은 종점을 가진 노선은 가질 수 있다.)
* [x] 조회: 노선에 포함된 구간 정보를 통해 상행 종점부터 하행 종점까지의 역 목록 응답

<br>

### 구간
> DB에는 순서없이 저장하고, 가져오면서 자동 정렬.

<br>

#### 추가
* [x] 추가: 상행, 하행역 id와 두 역간의 거리 입력 - (부가 설명) 새로 등록할 구간의 상행역과 하행역 중 노선에 이미 등록되어있는 역을 기준으로 새로운 구간을 추가.
  * [x] 입력받은 상행 혹은 하행역이 종점인경우, 새로운 지하철 역을 옆에 붙인다. (새로운 지하철 역이 종점이된다)
  * [x] 하나의 노선에는 갈래길이 허용되지 않는다. 새로운 구간이 추가되기 전에 갈래길이 생기지 않도록 기존 구간을 변경한다. (중간 삽입)
    * [x] 예외: 역 사이에 새로운 역을 등록할 경우 역 사이 길이보다 크거나 같으면 등록을 할 수 없다.
  * [x] 예외: 상행역과 하행역이 모두 이미 해당 노선에 등록되어 있는 경우 추가할 수 없다.
  * [x] 예외: 상행역과 하행역 둘 중 하나도 포함되어있지 않으면 추가할 수 없다.

> 구간 검색 기능
> * 상,하행 중 종점이 있는 경우 ( A - B )
    >   * 요청 (C - A) : A가 하행인 구간이 없는 경우
>   * 요청 (A - C) : A가 상행인 구간이 없는 경우
> * 상,하행 중 종점이 없고 중간 삽입인 경우 ( A - B - C )
    >   * 요청 (D - B) : B가 하행인 구간이 있는 경우
>   * 요청 (B - D) : B가 상행인 구간이 있는 경우

<br>

#### 제거
* [x] 제거: line에 존재하는 stationId를 통해서 section 제거.
  * [x] 종점이 제거될 경우 다음으로 오던 역이 종점이 됨.
  * [x] 중간역이 제거될 경우 재배치를 함. -> 노선에 A - B - C 역이 연결되어 있을 때 B역을 제거할 경우 A - C로 재배치 됨 -> 거리는 두 구간의 거리의 합으로 정함.
  * [x] 예외: 구간이 하나인 노선에서 마지막 구간을 제거할 수 없다.
  * [x] 예외: line에 존재하지 않는 station을 제거할 수 없다.
  