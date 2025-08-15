# 🌿 UMC 8th 틈틈(teumteum) Backend
> **Micro-break × 콘텐츠 × 사회적 연결**  
> 하루의 빈틈을 가치 있는 시간으로 채우는 플랫폼

<p align="center">
  <img src="https://github.com/user-attachments/assets/f2ad2c25-4969-45d7-8bcb-cb66fb8a7381" alt="프로젝트 대표 이미지" width="80%">
</p>

**틈틈**은 매번 새로운 계획을 고민하고 실행하는 것이 번거롭게 느껴져 무의미하게 시간을 흘려보낸 사람들을 위한 서비스입니다.  
단순한 일정 관리 캘린더에서 벗어나, **나의 일정을 기반으로 하루의 빈틈을 찾아 삶의 질을 향상**시켜 보세요!  

---

<br><br>

## 🚀 Tech Stack

### **Framework & Library**
<p align="left">
  <img src="https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=OpenJDK&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring%20Data%20JPA-59666C?style=for-the-badge&logo=hibernate&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=Spring%20Security&logoColor=white"/>
  <img src="https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=white"/>
  <img src="https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=jsonwebtokens&logoColor=white"/>
  <img src="https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=JUnit5&logoColor=white"/>
  <img src="https://img.shields.io/badge/Mockito-FFCA28?style=for-the-badge&logoColor=white"/>
  <img src="https://img.shields.io/badge/Firebase%20Admin-FFCA28?style=for-the-badge&logo=firebase&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring%20WebFlux-6DB33F?style=for-the-badge&logo=spring&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring%20Actuator-6DB33F?style=for-the-badge&logo=spring&logoColor=white"/>
</p>

### **Infra & Cloud**
<p align="left">
  <img src="https://img.shields.io/badge/Ubuntu-E95420?style=for-the-badge&logo=ubuntu&logoColor=white"/>
  <img src="https://img.shields.io/badge/Amazon%20AWS-232F3E?style=for-the-badge&logo=amazon-aws&logoColor=white"/>
  <img src="https://img.shields.io/badge/Amazon%20EC2-FF9900?style=for-the-badge&logo=Amazon%20EC2&logoColor=white"/>
  <img src="https://img.shields.io/badge/Amazon%20RDS-527FFF?style=for-the-badge&logo=Amazon%20RDS&logoColor=white"/>
  <img src="https://img.shields.io/badge/Amazon%20S3-569A31?style=for-the-badge&logo=Amazon%20S3&logoColor=white"/>
  <img src="https://img.shields.io/badge/Amazon%20Route%2053-8C4FFF?style=for-the-badge&logo=Amazon%20Route%2053&logoColor=white"/>
  <img src="https://img.shields.io/badge/AWS%20Lambda-FF9900?style=for-the-badge&logo=AWS%20Lambda&logoColor=white"/>
  <img src="https://img.shields.io/badge/Amazon%20CloudWatch-FF4F8B?style=for-the-badge&logo=Amazon%20CloudWatch&logoColor=white"/>
  <img src="https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white"/>
  <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=Redis&logoColor=white"/>
  <img src="https://img.shields.io/badge/Docker-0db7ed?style=for-the-badge&logo=docker&logoColor=white"/>
  <img src="https://img.shields.io/badge/Docker%20Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white"/>
  <img src="https://img.shields.io/badge/GitHub%20Actions-2088FF?style=for-the-badge&logo=GitHub%20Actions&logoColor=white"/>
</p>

### **Monitoring**
<p align="left">
  <img src="https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=Prometheus&logoColor=white"/>
  <img src="https://img.shields.io/badge/Grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white"/>
</p>


<br><br>

## 🔧 Architecture

> **Backend Infrastructure Overview**  
> 안정적인 서비스 운영을 위해 **AWS 기반의 이중화 및 모니터링 환경**을 구성했습니다.  
> GitHub Actions를 통한 CI/CD, Bastion Host를 활용한 보안 강화, Prometheus + Grafana 모니터링을 포함합니다.

<p align="center">
  <img 
    src="https://github.com/user-attachments/assets/c002af7b-706d-4a15-9fd4-bb2af883ca32" 
    alt="Backend Architecture Diagram"
    width="85%"
    style="border-radius: 10px; box-shadow: 0 4px 15px rgba(0,0,0,0.2);"
  />
</p>

<br><br>
---


## 📂 프로젝트 구조
```plaintext
.
├── .github/                   # GitHub Actions 워크플로우 및 이슈/PR 템플릿
├── config/                    # [서브모듈] 환경별 설정 파일 (application-*.properties 등)
├── gradle/                    # Gradle Wrapper 설정
├── infra/                     # 인프라 및 모니터링 설정, 실행 스크립트
│   ├── config/                # 모니터링 도구 설정 파일
│   │   ├── grafana/datasources/
│   │   │   ├── cloudwatch.yaml
│   │   │   ├── prometheus.yaml
│   │   ├── cloudwatch-agent.json
│   │   ├── prometheus-server.yaml
│   └── scripts/               # 배포 및 환경 구성 스크립트
│       ├── 00_common.sh
│       ├── 01_cloudwatch.sh
│       ├── 02_prometheus.sh
│       ├── 03_grafana.sh
│       ├── 10_app.sh
│       ├── 11_redis.sh
│       └── run_all.sh
├── src/                       # 메인 소스코드 (도메인 기반 구조)
├── .gitmodules                # Git Submodule 설정
├── Dockerfile                 # 애플리케이션 Docker 이미지 빌드 설정
├── docker-compose.yml         # 로컬 개발 환경(MySQL, Redis) 구성
├── build.gradle               # Gradle 빌드 스크립트
├── init.sql                   # 초기 DB 스키마/데이터
└── gradlew, gradlew.bat       # Gradle Wrapper 실행 스크립트

```

## 🔐 서브모듈 도입 배경

`config` 디렉토리는 Git Submodule로 관리되며, 환경별 설정 파일(application-dev.properties, application-prod.properties 등)을 포함합니다.

### 도입 이유
- **보안** – DB URL, API Key, 비밀번호 등 민감 정보 노출 방지
- **환경 일관성** – 로컬·개발·배포 환경 설정 통합 관리
- **협업 편의성** – 설정 변경 시 팀원 간 동기화 용이


## 🚀 배포 파이프라인 (CI/CD)

본 프로젝트는 **GitHub Actions → Bastion EC2 → 내부 App EC2** 구조로 배포됩니다.

### 빌드
- develop 브랜치 푸시 시 GitHub Actions 트리거
- Gradle 빌드 → Docker 이미지 생성 → Docker Hub 푸시

### 배포
- Bastion EC2 경유하여 내부 App EC2 접속

### 모니터링
- **Prometheus + Grafana** → 애플리케이션 메트릭 수집 및 시각화
- **Amazon CloudWatch** → EC2, Lambda, RDS 로그·메트릭 수집


## 📡 모니터링 & 인프라 관리
- **Prometheus** – Spring Actuator 메트릭 수집
- **Grafana** – 대시보드 시각화
- **CloudWatch** – AWS 리소스 로그·메트릭 모니터링
- `infra/scripts` – 초기 환경 세팅 및 모니터링 도구 자동 설치 스크립트


## 📌 협업 규칙
<details>
<summary><b>🌿 브랜치 전략</b></summary>

**Git Flow 사용**
- **main** : 배포용 브랜치
- **develop** : 개발 통합 브랜치 (CI/CD 기준)
- **feature/** : 기능 개발 브랜치 → `feature/{issue-number}-{feature-name}`
- **fix/** : 버그 수정 브랜치 → `fix/{issue-number}-{bug-name}`
- **hotfix/** : 긴급 수정 브랜치 → `hotfix/{issue-number}-{issue-name}`

</details>

<details>
<summary><b>📝 커밋 컨벤션</b></summary>

**형식**
```
type: subject

body
```

- **type**
  - Feat : 새로운 기능 추가
  - Fix : 버그 수정
  - Docs : 문서 수정
  - Style : 코드 포맷팅 (로직 변경 없음)
  - Refactor : 코드 리팩토링
  - Test : 테스트 코드 작성
  - Chore : 빌드/배포/패키지 관련 수정

- **subject**
  - 한글 작성
  - 개조식 구문 사용 (간결하고 요점 중심)

- **body**
  - 변경 내용과 이유를 상세히 작성
  - 최대한 구체적으로 기술

**예시**
```
Feat: 회원 가입 기능 구현

SMS, 이메일 중복확인 API를 구현하였습니다.
```

</details>

---




<br><br>

## 👥 Team
<p align="center">
  <table align="center">
    <tr>
      <td align="center" width="200">
        <a href="https://github.com/hyeonda02">
          <img src="https://github.com/hyeonda02.png" width="140" height="140" style="border-radius:50%; box-shadow:0 4px 10px rgba(0,0,0,0.3);"><br>
          <b style="font-size:18px;">현다 / 강다현</b>
        </a>
      </td>
      <td align="center" width="200">
        <a href="https://github.com/sunninz">
          <img src="https://github.com/sunninz.png" width="140" height="140" style="border-radius:50%; box-shadow:0 4px 10px rgba(0,0,0,0.3);"><br>
          <b style="font-size:18px;">결이 / 김수민</b>
        </a>
      </td>
      <td align="center" width="200">
        <a href="https://github.com/Jinseong01">
          <img src="https://github.com/Jinseong01.png" width="140" height="140" style="border-radius:50%; box-shadow:0 4px 10px rgba(0,0,0,0.3);"><br>
          <b style="font-size:18px;">애플 / 박진성</b>
        </a>
      </td>
      <td align="center" width="200">
        <a href="https://github.com/yangjiae12">
          <img src="https://github.com/yangjiae12.png" width="140" height="140" style="border-radius:50%; box-shadow:0 4px 10px rgba(0,0,0,0.3);"><br>
          <b style="font-size:18px;">나루 / 양지애</b>
        </a>
      </td>
    </tr>
  </table>
</p>

