# 시놀로지 NAS 스토리지 전환 가이드

> 작성일: 2026-05-15  
> 현황: R2(Cloudflare) 또는 로컬 스토리지 → 시놀로지 NAS 전환 방법  
> 코드 수정 없음 — 환경변수 또는 마운트 설정만으로 전환 가능

---

## 방법 선택

| 방법 | 난이도 | 권장 상황 |
|------|--------|----------|
| [방법 1 — MinIO (S3 API)](#방법-1--minio-s3-api-권장) | 중간 | 운영 환경, 안정성 우선 |
| [방법 2 — NFS 마운트](#방법-2--nfs-마운트) | 쉬움 | 소규모, 사내망 전용 |

---

## 방법 1 — MinIO (S3 API) (권장)

NAS 위에 MinIO를 Docker로 올려 S3 호환 API를 제공한다.  
앱 코드는 현재 `S3FileStorage`를 그대로 사용하므로 **코드 수정 없음**.

### 1단계 — 시놀로지 DSM에서 MinIO Docker 실행

DSM → **Container Manager** → **레지스트리** → `minio/minio` 검색 후 다운로드.

또는 SSH로 직접:

```bash
docker run -d \
  --name minio \
  --restart unless-stopped \
  -p 9000:9000 \
  -p 9001:9001 \
  -v /volume1/resourcehub-minio:/data \
  -e MINIO_ROOT_USER=admin \
  -e MINIO_ROOT_PASSWORD=변경할비밀번호 \
  minio/minio server /data --console-address ":9001"
```

- `/volume1/resourcehub-minio` — NAS 공유 폴더 경로 (DSM에서 미리 생성)
- 포트 9000: S3 API, 포트 9001: 웹 콘솔

### 2단계 — MinIO 버킷 생성

브라우저로 `http://시놀로지IP:9001` 접속 → 로그인 후:

1. **Buckets** → **Create Bucket**
2. 버킷 이름: `eactive-uploads`
3. 나머지 기본값 유지 → **Create**

또는 MinIO CLI(`mc`)로:

```bash
mc alias set nas http://시놀로지IP:9000 admin 변경할비밀번호
mc mb nas/eactive-uploads
```

### 3단계 — 앱 환경변수 변경

서버의 `.env` 파일 수정:

```env
RESOURCEHUB_STORAGE_TYPE=s3
RESOURCEHUB_S3_ENDPOINT=http://시놀로지IP:9000
RESOURCEHUB_S3_ACCESS_KEY=admin
RESOURCEHUB_S3_SECRET_KEY=변경할비밀번호
RESOURCEHUB_S3_BUCKET=eactive-uploads
```

> `RESOURCEHUB_S3_REGION`은 MinIO에서 무시되므로 설정하지 않아도 됩니다.

### 4단계 — 재배포

```bash
bash scripts/deploy.sh
```

### 기존 파일 마이그레이션 (R2 → MinIO)

R2에 이미 파일이 있는 경우 MinIO CLI로 복사:

```bash
# R2 alias 등록
mc alias set r2 https://[계정ID].r2.cloudflarestorage.com [R2_ACCESS_KEY] [R2_SECRET_KEY]

# MinIO alias 등록
mc alias set nas http://시놀로지IP:9000 admin 변경할비밀번호

# 버킷 간 복사
mc cp --recursive r2/eactive-uploads/ nas/eactive-uploads/
```

---

## 방법 2 — NFS 마운트

NAS 폴더를 서버에 마운트해 로컬 디렉토리처럼 사용한다.  
`LocalFileStorage` 그대로 동작 — **코드 수정 없음**.

### 1단계 — 시놀로지 NFS 활성화

DSM → **제어판** → **파일 서비스** → **NFS** 탭:

1. **NFS 서비스 활성화** 체크
2. **NFS 프로토콜**: NFSv4.1 권장

### 2단계 — 공유 폴더 NFS 권한 설정

DSM → **제어판** → **공유 폴더** → 대상 폴더 선택 → **편집** → **NFS 권한** 탭:

1. **만들기** 클릭
2. **호스트명 또는 IP**: 앱 서버 IP (예: `192.168.1.100`)
3. **권한**: 읽기/쓰기
4. **Squash**: No mapping
5. **보안**: sys
6. 저장 후 **NFS 경로** 메모 (예: `/volume1/resourcehub-uploads`)

### 3단계 — 앱 서버에 NFS 마운트

```bash
# 마운트 포인트 생성
sudo mkdir -p /mnt/nas/resourcehub-uploads

# 즉시 마운트 (테스트용)
sudo mount -t nfs 시놀로지IP:/volume1/resourcehub-uploads /mnt/nas/resourcehub-uploads

# 재부팅 후에도 유지 (/etc/fstab 등록)
echo "시놀로지IP:/volume1/resourcehub-uploads /mnt/nas/resourcehub-uploads nfs defaults,_netdev 0 0" | sudo tee -a /etc/fstab
```

마운트 확인:

```bash
df -h | grep nas
```

### 4단계 — 앱 환경변수 변경

서버의 `.env` 파일 수정:

```env
RESOURCEHUB_STORAGE_TYPE=local
RESOURCEHUB_UPLOAD_BASE_DIR=/mnt/nas/resourcehub-uploads
```

`docker-compose.prod.yml`의 볼륨 설정도 수정:

```yaml
# 변경 전
volumes:
  - resourcehub_uploads:/data/uploads

# 변경 후
volumes:
  - /mnt/nas/resourcehub-uploads:/data/uploads
```

### 5단계 — 재배포

```bash
bash scripts/deploy.sh
```

### 기존 파일 마이그레이션 (로컬 → NAS)

```bash
# 기존 Docker 볼륨에서 NAS로 복사
docker run --rm \
  -v resourcehub_uploads:/src \
  -v /mnt/nas/resourcehub-uploads:/dst \
  alpine cp -a /src/. /dst/
```

---

## 방법별 장단점 비교

| 항목 | MinIO (S3 API) | NFS 마운트 |
|------|---------------|-----------|
| 설정 복잡도 | 중간 (Docker 필요) | 낮음 |
| 안정성 | 높음 (SDK 재시도 내장) | NFS 끊기면 앱 오류 |
| 고아 파일 GC | 동작 안 함 (`listAll` no-op) | 정상 동작 |
| 외부 공개 가능 | 가능 (포트 포워딩) | 어려움 |
| R2 대비 비용 | NAS 전기료 외 무료 | 동일 |
| 백업 | MinIO 볼륨 백업 | NAS 자체 백업 |

---

## 주의사항

- NAS가 꺼지면 파일 서비스 전체 중단 — UPS 연결 권장
- 외부 인터넷에서 접근이 필요하다면 방화벽/포트포워딩 설정 필요
- MinIO 방식 사용 시 `backup-uploads.sh`의 대상 경로를 MinIO 볼륨 디렉토리로 변경 필요
