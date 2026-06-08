'use strict';
// scripts/seed_documents.js
// 인력표 전 사원 개인 문서 시드 데이터 생성 + Cloudflare R2 업로드
// 실행: node scripts/seed_documents.js

const path = require('path');
const crypto = require('crypto');
const fs = require('fs');
const { execSync } = require('child_process');

require('dotenv').config({ path: path.join(__dirname, '..', '.env') });

const PDFDocument = require('pdfkit');
const { S3Client, PutObjectCommand } = require('@aws-sdk/client-s3');

const PROJECT_ROOT = path.join(__dirname, '..');
const FONT_R = 'C:/Windows/Fonts/malgun.ttf';
const FONT_B = fs.existsSync('C:/Windows/Fonts/malgunbd.ttf')
  ? 'C:/Windows/Fonts/malgunbd.ttf'
  : FONT_R;

const TODAY = new Date('2026-06-08');
const ADMIN_ID = 1;

const s3 = new S3Client({
  region: process.env.RESOURCEHUB_S3_REGION || 'auto',
  endpoint: process.env.RESOURCEHUB_S3_ENDPOINT,
  credentials: {
    accessKeyId: process.env.RESOURCEHUB_S3_ACCESS_KEY,
    secretAccessKey: process.env.RESOURCEHUB_S3_SECRET_KEY,
  },
});
const BUCKET = process.env.RESOURCEHUB_S3_BUCKET;

// ── 사용자 데이터 ───────────────────────────────────────────────────────
const USERS = [
  // 구름팀
  {id:40,name:'김태훈',email:'taehun.kim2@eactive.co.kr',team:'구름팀',pos:'STAFF',proj:0,age:28},
  {id:39,name:'남성현',email:'sunghyun.nam@eactive.co.kr',team:'구름팀',pos:'STAFF',proj:0,age:28},
  {id:44,name:'류지원',email:'jiwon.ryu@eactive.co.kr',team:'구름팀',pos:'STAFF',proj:0,age:26},
  {id:35,name:'배성준',email:'sungjun.bae@eactive.co.kr',team:'구름팀',pos:'MANAGER',proj:2,age:36},
  {id:36,name:'백재원',email:'jaewon.baek@eactive.co.kr',team:'구름팀',pos:'ASSISTANT_MANAGER',proj:0,age:33},
  {id:34,name:'손민혁',email:'minhyuk.son@eactive.co.kr',team:'구름팀',pos:'DEPUTY_GENERAL_MANAGER',proj:1,age:41},
  {id:43,name:'송수연',email:'suyeon.song@eactive.co.kr',team:'구름팀',pos:'STAFF',proj:0,age:31},
  {id:42,name:'안지수',email:'jisu.ahn@eactive.co.kr',team:'구름팀',pos:'ASSISTANT_MANAGER',proj:1,age:33},
  {id:33,name:'양재호',email:'jaeho.yang@eactive.co.kr',team:'구름팀',pos:'GENERAL_MANAGER',proj:1,age:43},
  {id:38,name:'유정호',email:'jungho.yu@eactive.co.kr',team:'구름팀',pos:'STAFF',proj:0,age:30},
  {id:37,name:'허민준',email:'minjun.heo@eactive.co.kr',team:'구름팀',pos:'ASSISTANT_MANAGER',proj:1,age:31},
  {id:41,name:'황미현',email:'mihyun.hwang@eactive.co.kr',team:'구름팀',pos:'MANAGER',proj:1,age:36},
  // 백업팀
  {id:57,name:'문수지',email:'suji.moon@eactive.co.kr',team:'백업팀',pos:'MANAGER',proj:1,age:37},
  {id:60,name:'배수연',email:'suyeon.bae@eactive.co.kr',team:'백업팀',pos:'STAFF',proj:0,age:30},
  {id:61,name:'백지원',email:'jiwon.baek@eactive.co.kr',team:'백업팀',pos:'STAFF',proj:0,age:29},
  {id:59,name:'손지수',email:'jisu.son@eactive.co.kr',team:'백업팀',pos:'ASSISTANT_MANAGER',proj:1,age:32},
  {id:58,name:'양미현',email:'mihyun.yang@eactive.co.kr',team:'백업팀',pos:'ASSISTANT_MANAGER',proj:0,age:33},
  {id:56,name:'임민준',email:'minjun.lim@eactive.co.kr',team:'백업팀',pos:'MANAGER',proj:1,age:37},
  {id:55,name:'장재호',email:'jaeho.jang@eactive.co.kr',team:'백업팀',pos:'DEPUTY_GENERAL_MANAGER',proj:1,age:40},
  {id:62,name:'허미현',email:'mihyun.heo@eactive.co.kr',team:'백업팀',pos:'STAFF',proj:0,age:26},
  // 연구소
  {id:98,name:'서진영',email:'jinyoung.seo@eactive.co.kr',team:'연구소',pos:'STAFF',proj:0,age:27},
  {id:97,name:'오소연',email:'soyeon.oh@eactive.co.kr',team:'연구소',pos:'STAFF',proj:0,age:28},
  {id:93,name:'윤경호',email:'kyungho.yun@eactive.co.kr',team:'연구소',pos:'MANAGER',proj:0,age:38},
  {id:95,name:'임나영',email:'nayoung.lim@eactive.co.kr',team:'연구소',pos:'ASSISTANT_MANAGER',proj:0,age:32},
  {id:94,name:'장수현',email:'suhyun.jang@eactive.co.kr',team:'연구소',pos:'ASSISTANT_MANAGER',proj:0,age:33},
  {id:96,name:'한미소',email:'miso.han@eactive.co.kr',team:'연구소',pos:'STAFF',proj:0,age:29},
  // 경영본부
  {id:91,name:'강미래',email:'mirae.kang@eactive.co.kr',team:'경영본부',pos:'DIRECTOR',proj:0,age:47},
  {id:89,name:'박영호',email:'youngho.park@eactive.co.kr',team:'경영본부',pos:'MANAGING_DIRECTOR',proj:0,age:49},
  {id:88,name:'이철수',email:'chulsu.lee@eactive.co.kr',team:'경영본부',pos:'EXECUTIVE_DIRECTOR',proj:0,age:52},
  {id:90,name:'정동훈',email:'donghun.jung@eactive.co.kr',team:'경영본부',pos:'DIRECTOR',proj:0,age:47},
  {id:92,name:'조영란',email:'youngran.jo@eactive.co.kr',team:'경영본부',pos:'GENERAL_MANAGER',proj:0,age:45},
  // 기술본부
  {id: 7,name:'강동훈',email:'donghun.kang@eactive.co.kr',team:'기술본부',pos:'ASSISTANT_MANAGER',proj:1,age:34},
  {id:49,name:'강재호',email:'jaeho.kang@eactive.co.kr',team:'기술본부',pos:'ASSISTANT_MANAGER',proj:1,age:31},
  {id:27,name:'고민준',email:'minjun.go@eactive.co.kr',team:'기술본부',pos:'STAFF',proj:1,age:28},
  {id:54,name:'고지현',email:'jihyun.go@eactive.co.kr',team:'기술본부',pos:'STAFF',proj:0,age:27},
  {id:20,name:'권민준',email:'minjun.kwon@eactive.co.kr',team:'기술본부',pos:'DEPUTY_GENERAL_MANAGER',proj:1,age:42},
  {id:32,name:'권지원',email:'jiwon.kwon@eactive.co.kr',team:'기술본부',pos:'STAFF',proj:0,age:28},
  {id: 3,name:'김민준',email:'minjun.kim@eactive.co.kr',team:'기술본부',pos:'GENERAL_MANAGER',proj:1,age:46},
  {id:24,name:'류재호',email:'jaeho.ryu@eactive.co.kr',team:'기술본부',pos:'ASSISTANT_MANAGER',proj:0,age:32},
  {id:28,name:'문정훈',email:'junghun.moon@eactive.co.kr',team:'기술본부',pos:'STAFF',proj:0,age:26},
  {id:46,name:'박성준',email:'sungjun.park@eactive.co.kr',team:'기술본부',pos:'MANAGER',proj:1,age:38},
  {id: 5,name:'박지훈',email:'jihun.park@eactive.co.kr',team:'기술본부',pos:'MANAGER',proj:1,age:38},
  {id:30,name:'서민지',email:'minji.seo@eactive.co.kr',team:'기술본부',pos:'ASSISTANT_MANAGER',proj:1,age:32},
  {id:14,name:'서재원',email:'jaewon.seo@eactive.co.kr',team:'기술본부',pos:'STAFF',proj:1,age:26},
  {id:77,name:'서현민',email:'hyunmin.seo@eactive.co.kr',team:'기술본부',pos:'STAFF',proj:0,age:28},
  {id:23,name:'송민수',email:'minsu.song@eactive.co.kr',team:'기술본부',pos:'ASSISTANT_MANAGER',proj:1,age:34},
  {id:19,name:'신동현',email:'donghyun.shin@eactive.co.kr',team:'기술본부',pos:'GENERAL_MANAGER',proj:1,age:44},
  {id:31,name:'신수연',email:'suyeon.shin@eactive.co.kr',team:'기술본부',pos:'STAFF',proj:1,age:30},
  {id:78,name:'신재혁',email:'jaehyuk.shin@eactive.co.kr',team:'기술본부',pos:'STAFF',proj:0,age:26},
  {id:22,name:'안재현',email:'jaehyun.ahn@eactive.co.kr',team:'기술본부',pos:'MANAGER',proj:1,age:36},
  {id:13,name:'오성민',email:'seongmin.oh@eactive.co.kr',team:'기술본부',pos:'STAFF',proj:1,age:27},
  {id:76,name:'오준서',email:'junsu.oh@eactive.co.kr',team:'기술본부',pos:'STAFF',proj:0,age:28},
  {id:29,name:'오지현',email:'jihyun.oh@eactive.co.kr',team:'기술본부',pos:'DEPUTY_GENERAL_MANAGER',proj:1,age:40},
  {id:51,name:'윤성현',email:'sunghyun.yun@eactive.co.kr',team:'기술본부',pos:'STAFF',proj:1,age:28},
  {id:80,name:'윤소현',email:'sohyun.yun@eactive.co.kr',team:'기술본부',pos:'ASSISTANT_MANAGER',proj:0,age:33},
  {id: 9,name:'윤재호',email:'jaeho.yun@eactive.co.kr',team:'기술본부',pos:'ASSISTANT_MANAGER',proj:1,age:32},
  {id:72,name:'윤태영',email:'taeyoung.yun@eactive.co.kr',team:'기술본부',pos:'MANAGER',proj:0,age:38},
  {id:45,name:'이민호',email:'minho.lee@eactive.co.kr',team:'기술본부',pos:'DEPUTY_GENERAL_MANAGER',proj:1,age:42},
  {id:15,name:'이서연',email:'seoyeon.lee@eactive.co.kr',team:'기술본부',pos:'MANAGER',proj:0,age:37},
  {id: 4,name:'이준호',email:'junho.lee@eactive.co.kr',team:'기술본부',pos:'DEPUTY_GENERAL_MANAGER',proj:2,age:42},
  {id:11,name:'임정호',email:'jungho.lim@eactive.co.kr',team:'기술본부',pos:'STAFF',proj:2,age:29},
  {id:74,name:'임준혁',email:'junhyuk.lim@eactive.co.kr',team:'기술본부',pos:'ASSISTANT_MANAGER',proj:0,age:32},
  {id:82,name:'임지윤',email:'jiyun.lim@eactive.co.kr',team:'기술본부',pos:'STAFF',proj:0,age:27},
  {id:10,name:'장민혁',email:'minhyuk.jang@eactive.co.kr',team:'기술본부',pos:'STAFF',proj:1,age:30},
  {id:81,name:'장수진',email:'sujin.jang@eactive.co.kr',team:'기술본부',pos:'STAFF',proj:0,age:29},
  {id:73,name:'장현우',email:'hyunwoo.jang@eactive.co.kr',team:'기술본부',pos:'ASSISTANT_MANAGER',proj:0,age:33},
  {id:52,name:'전미현',email:'mihyun.jeon@eactive.co.kr',team:'기술본부',pos:'MANAGER',proj:1,age:37},
  {id:25,name:'전성민',email:'seongmin.jeon@eactive.co.kr',team:'기술본부',pos:'STAFF',proj:0,age:30},
  {id:48,name:'정민준',email:'minjun.jung@eactive.co.kr',team:'기술본부',pos:'ASSISTANT_MANAGER',proj:1,age:33},
  {id: 6,name:'정재원',email:'jaewon.jung@eactive.co.kr',team:'기술본부',pos:'MANAGER',proj:1,age:38},
  {id:17,name:'정지은',email:'jieun.jung@eactive.co.kr',team:'기술본부',pos:'STAFF',proj:0,age:30},
  {id:79,name:'조미연',email:'miyeon.jo@eactive.co.kr',team:'기술본부',pos:'MANAGER',proj:0,age:37},
  {id:50,name:'조민혁',email:'minhyuk.jo@eactive.co.kr',team:'기술본부',pos:'STAFF',proj:1,age:30},
  {id: 8,name:'조성현',email:'sunghyun.jo@eactive.co.kr',team:'기술본부',pos:'ASSISTANT_MANAGER',proj:1,age:34},
  {id:71,name:'조진석',email:'jinseok.jo@eactive.co.kr',team:'기술본부',pos:'DEPUTY_GENERAL_MANAGER',proj:0,age:41},
  {id:16,name:'최수현',email:'suhyun.choi@eactive.co.kr',team:'기술본부',pos:'ASSISTANT_MANAGER',proj:0,age:32},
  {id:47,name:'최재원',email:'jaewon.choi@eactive.co.kr',team:'기술본부',pos:'MANAGER',proj:1,age:37},
  {id:18,name:'한수지',email:'suji.han@eactive.co.kr',team:'기술본부',pos:'STAFF',proj:1,age:28},
  {id:12,name:'한준서',email:'junsu.han@eactive.co.kr',team:'기술본부',pos:'STAFF',proj:0,age:27},
  {id:75,name:'한진호',email:'jinho.han@eactive.co.kr',team:'기술본부',pos:'STAFF',proj:0,age:30},
  {id:53,name:'홍수연',email:'suyeon.hong@eactive.co.kr',team:'기술본부',pos:'ASSISTANT_MANAGER',proj:2,age:33},
  {id:26,name:'홍재원',email:'jaewon.hong@eactive.co.kr',team:'기술본부',pos:'STAFF',proj:1,age:29},
  {id:21,name:'황정호',email:'jungho.hwang@eactive.co.kr',team:'기술본부',pos:'MANAGER',proj:1,age:37},
  // 영업본부
  {id:83,name:'권혁준',email:'hyukjun.kwon@eactive.co.kr',team:'영업본부',pos:'DIRECTOR',proj:0,age:49},
  {id:87,name:'송다희',email:'dahee.song@eactive.co.kr',team:'영업본부',pos:'DEPUTY_GENERAL_MANAGER',proj:0,age:41},
  {id:85,name:'안영수',email:'youngsoo.ahn@eactive.co.kr',team:'영업본부',pos:'DEPUTY_GENERAL_MANAGER',proj:0,age:42},
  {id:86,name:'최미영',email:'miyoung.choi@eactive.co.kr',team:'영업본부',pos:'GENERAL_MANAGER',proj:0,age:44},
  {id:84,name:'황성철',email:'sungchul.hwang@eactive.co.kr',team:'영업본부',pos:'GENERAL_MANAGER',proj:0,age:46},
  // 프로젝트본부
  {id:70,name:'강지원',email:'jiwon.kang@eactive.co.kr',team:'프로젝트본부',pos:'STAFF',proj:0,age:27},
  {id:65,name:'김지은',email:'jieun.kim@eactive.co.kr',team:'프로젝트본부',pos:'MANAGER',proj:2,age:38},
  {id:64,name:'남재호',email:'jaeho.nam@eactive.co.kr',team:'프로젝트본부',pos:'DEPUTY_GENERAL_MANAGER',proj:2,age:42},
  {id:66,name:'박수현',email:'suhyun.park@eactive.co.kr',team:'프로젝트본부',pos:'MANAGER',proj:1,age:37},
  {id:63,name:'유재원',email:'jaewon.yu@eactive.co.kr',team:'프로젝트본부',pos:'GENERAL_MANAGER',proj:0,age:45},
  {id:67,name:'이미현',email:'mihyun.lee@eactive.co.kr',team:'프로젝트본부',pos:'ASSISTANT_MANAGER',proj:1,age:33},
  {id:69,name:'정수연',email:'suyeon.jung@eactive.co.kr',team:'프로젝트본부',pos:'STAFF',proj:0,age:30},
  {id:68,name:'최지수',email:'jisu.choi@eactive.co.kr',team:'프로젝트본부',pos:'ASSISTANT_MANAGER',proj:1,age:32},
  // 미배정
  {id:99,name:'김도현',email:'dohyun.kim@eactive.co.kr',team:'no-team',pos:'STAFF',proj:0,age:26},
  {id:101,name:'박하늘',email:'haneul.park@eactive.co.kr',team:'no-team',pos:'STAFF',proj:0,age:24},
  {id:100,name:'이시은',email:'sieun.lee@eactive.co.kr',team:'no-team',pos:'STAFF',proj:0,age:24},
  {id:102,name:'최예린',email:'yerin.choi@eactive.co.kr',team:'no-team',pos:'STAFF',proj:0,age:24},
];

const ALL_TYPES = [
  'RESUME','CAREER_DESCRIPTION','GRADUATION_CERTIFICATE','LICENSE',
  'NATIONAL_PENSION_CERTIFICATE','HEALTH_INSURANCE_CERTIFICATE',
  'HEALTH_INSURANCE_ELIGIBILITY','PROFILE_PHOTO',
];

const NO_PROJ_SUBSETS = {
  0: ['RESUME','CAREER_DESCRIPTION','GRADUATION_CERTIFICATE','LICENSE'],
  1: ['RESUME','CAREER_DESCRIPTION','NATIONAL_PENSION_CERTIFICATE','HEALTH_INSURANCE_CERTIFICATE','HEALTH_INSURANCE_ELIGIBILITY'],
  2: ['RESUME','GRADUATION_CERTIFICATE','PROFILE_PHOTO'],
};

const PENDING_SET = new Set([
  '11_RESUME','53_CAREER_DESCRIPTION','35_HEALTH_INSURANCE_CERTIFICATE',
  '55_LICENSE','65_GRADUATION_CERTIFICATE','22_NATIONAL_PENSION_CERTIFICATE',
]);
const REJECTED_SET = new Set([
  '27_CAREER_DESCRIPTION','14_PROFILE_PHOTO','29_HEALTH_INSURANCE_ELIGIBILITY',
]);

// ── PDF 내용 생성용 참조 데이터 ─────────────────────────────────────────
const UNIVERSITIES = [
  '한양대학교','성균관대학교','고려대학교','연세대학교','서울대학교',
  '중앙대학교','경희대학교','건국대학교','세종대학교','국민대학교',
];
const DEPARTMENTS = [
  '컴퓨터공학과','소프트웨어학과','정보통신공학과','전자공학과',
  '산업공학과','전산학과','정보보안학과','AI융합학과',
];
const TECH_POOLS = [
  'Java, Spring Boot, MySQL, Docker',
  'Python, Django/Flask, PostgreSQL, Kubernetes',
  'Java, MSA 아키텍처, Redis, AWS',
  'JavaScript, React, Node.js, MongoDB',
  'Java, Spring Cloud, Kafka, GitLab CI',
];
const POS_KR = {
  STAFF:'사원', ASSISTANT_MANAGER:'대리', MANAGER:'과장',
  DEPUTY_GENERAL_MANAGER:'차장', GENERAL_MANAGER:'부장',
  DIRECTOR:'이사', MANAGING_DIRECTOR:'상무이사', EXECUTIVE_DIRECTOR:'전무이사',
};
const DEGREE_KR = { BACHELOR:'학사', MASTER:'석사', DOCTORATE:'박사', ASSOCIATE:'전문학사' };

// ── 날짜 헬퍼 ──────────────────────────────────────────────────────────
function addDays(d, n) { const r = new Date(d); r.setDate(r.getDate() + n); return r; }
function addMonths(d, n) { const r = new Date(d); r.setMonth(r.getMonth() + n); return r; }
function addYears(d, n) { const r = new Date(d); r.setFullYear(r.getFullYear() + n); return r; }
function fmtDate(d) { return d.toISOString().slice(0, 10); }
function fmtTs(d) { return d.toISOString().replace('T',' ').replace(/\.\d+Z$/,''); }
function fmtDateKr(d) {
  return `${d.getFullYear()}년 ${String(d.getMonth()+1).padStart(2,'0')}월 ${String(d.getDate()).padStart(2,'0')}일`;
}

function getExpiryInfo(userId, docType) {
  const slot = (userId * 3 + docType.length) % 4;
  if (docType === 'LICENSE') {
    const yearsAgo = [6, 2, 5, 1][slot];
    const issuedDate = addYears(TODAY, -yearsAgo);
    const expiresAt = addYears(issuedDate, 5);
    return {
      issuedDate, expiresAt,
      expiredNotice: expiresAt < TODAY ? addDays(expiresAt, 7) : null,
      expiryWarn: expiresAt >= TODAY && addDays(expiresAt, -90) < TODAY ? addDays(TODAY, -3) : null,
    };
  }
  const daysAgo = [165, 75, 35, 10][slot];
  const issuedDate = addDays(TODAY, -daysAgo);
  const expiresAt = addMonths(issuedDate, 3);
  return {
    issuedDate, expiresAt,
    expiredNotice: expiresAt < TODAY ? addDays(expiresAt, 5) : null,
    expiryWarn: expiresAt >= TODAY && addDays(expiresAt, -30) <= TODAY ? addDays(TODAY, -1) : null,
  };
}

function getDegreeType(pos) {
  return {
    EXECUTIVE_DIRECTOR:'DOCTORATE', MANAGING_DIRECTOR:'MASTER', DIRECTOR:'MASTER',
    GENERAL_MANAGER:'MASTER', DEPUTY_GENERAL_MANAGER:'BACHELOR',
  }[pos] || 'BACHELOR';
}

// ── 경로 sanitize ─────────────────────────────────────────────────────
function sanitizeFolder(s) {
  if (!s) return null;
  const c = s.replace(/[/\\:*?"<>|\x00-\x1f]/g,'').trim().replace(/\s+/g,'_');
  return c || null;
}
function teamFolder(team) { return team === 'no-team' ? 'no-team' : (sanitizeFolder(team) || 'no-team'); }
function personFolder(name, email) {
  return sanitizeFolder(name) || sanitizeFolder(email.split('@')[0]) || 'unknown';
}

// ── 이름/파일명 ─────────────────────────────────────────────────────────
function makeTitle(name, t) {
  return ({
    RESUME:`${name} 이력서`, CAREER_DESCRIPTION:`${name} SW기술자 경력증명서`,
    GRADUATION_CERTIFICATE:`${name} 졸업증명서`, LICENSE:`${name} 정보처리기사`,
    NATIONAL_PENSION_CERTIFICATE:`${name} 국민연금가입증명서`,
    HEALTH_INSURANCE_CERTIFICATE:`${name} 건강보험가입증명서`,
    HEALTH_INSURANCE_ELIGIBILITY:`${name} 건강보험자격득실확인서`,
    PROFILE_PHOTO:`${name} 증명사진`,
  })[t] || `${name} 기타서류`;
}
function makeOrigName(name, t) {
  return ({
    RESUME:`${name}_이력서.pdf`, CAREER_DESCRIPTION:`${name}_SW기술자경력증명서.pdf`,
    GRADUATION_CERTIFICATE:`${name}_졸업증명서.pdf`, LICENSE:`${name}_정보처리기사.pdf`,
    NATIONAL_PENSION_CERTIFICATE:`${name}_국민연금가입증명서.pdf`,
    HEALTH_INSURANCE_CERTIFICATE:`${name}_건강보험가입증명서.pdf`,
    HEALTH_INSURANCE_ELIGIBILITY:`${name}_건강보험자격득실확인서.pdf`,
    PROFILE_PHOTO:`${name}_증명사진.pdf`,
  })[t] || `${name}_기타서류.pdf`;
}
function makeRejectReason(t) {
  return ({
    CAREER_DESCRIPTION:'문서 내용이 불명확합니다. 공식 발급 기관 문서를 재제출해 주세요.',
    PROFILE_PHOTO:'증명사진 규격 미달 (흰색 배경 필요). 재촬영 후 제출해 주세요.',
    HEALTH_INSURANCE_ELIGIBILITY:'유효기간이 만료된 문서입니다. 최신 발급본을 제출해 주세요.',
  })[t] || '서류 내용을 확인할 수 없습니다. 재제출해 주세요.';
}

// ── PDF 생성 (PDFKit + 맑은 고딕) ──────────────────────────────────────
function hr(doc) {
  doc.moveDown(0.3);
  const y = doc.y;
  doc.save().moveTo(60, y).lineTo(535, y).lineWidth(0.5).strokeColor('#555').stroke().restore();
  doc.moveDown(0.5);
}

function labelRow(doc, label, value) {
  const y = doc.y;
  doc.font('B').fontSize(10).fillColor('#333').text(label, 60, y, { width: 130, lineBreak: false });
  doc.font('R').fontSize(10).fillColor('#111').text(value, 195, y, { width: 340 });
}

function userMeta(user) {
  const birthYear = 2026 - user.age;
  const gradYear = birthYear + 22;
  const startYear = gradYear + 1;
  const univ = UNIVERSITIES[user.id % UNIVERSITIES.length];
  const dept = DEPARTMENTS[user.id % DEPARTMENTS.length];
  const techs = TECH_POOLS[user.id % TECH_POOLS.length];
  const degree = getDegreeType(user.pos);
  const degKr = DEGREE_KR[degree] || '학사';
  const posKr = POS_KR[user.pos] || '사원';
  const licenseNo = `${String(birthYear).slice(2)}${String(user.id*37%999+100).padStart(5,'0')}-${crypto.createHash('md5').update(user.email).digest('hex').slice(0,7).toUpperCase()}`;
  return { birthYear, gradYear, startYear, univ, dept, techs, degree, degKr, posKr, licenseNo };
}

function drawContent(doc, user, docType, issuedDateStr) {
  const m = userMeta(user);
  const issDate = new Date(issuedDateStr || fmtDate(addDays(TODAY, -30)));
  const issDateKr = fmtDateKr(issDate);

  // 공통 우상단 기관명
  doc.font('R').fontSize(8).fillColor('#888')
     .text('이 액 티 브 (주)', 60, 55, { width: 475, align: 'right' });

  doc.moveDown(0.5);

  switch (docType) {
    case 'RESUME': {
      doc.font('B').fontSize(22).fillColor('#111').text('이  력  서', { align:'center' });
      hr(doc);
      labelRow(doc, '성        명', user.name);
      labelRow(doc, '생    년    도', `${m.birthYear}년생`);
      labelRow(doc, '소        속', `(주)이액티브  ${user.team}`);
      labelRow(doc, '직        위', m.posKr);
      labelRow(doc, '이    메    일', user.email);
      hr(doc);
      doc.font('B').fontSize(12).fillColor('#111').text('■ 학력사항').moveDown(0.3);
      doc.font('R').fontSize(10).fillColor('#222')
         .text(`  ${m.gradYear-4}.03 ~ ${m.gradYear}.02   ${m.univ} ${m.dept} 졸업 (${m.degKr})`);
      doc.moveDown(0.8);
      doc.font('B').fontSize(12).text('■ 경력사항').moveDown(0.3);
      doc.font('R').fontSize(10)
         .text(`  ${m.startYear}.03 ~ 현재   (주)이액티브   ${m.posKr}`);
      doc.moveDown(0.8);
      doc.font('B').fontSize(12).text('■ 보유 기술').moveDown(0.3);
      doc.font('R').fontSize(10).text(`  ${m.techs}`);
      doc.moveDown(1.5);
      hr(doc);
      doc.font('R').fontSize(9).fillColor('#666').text(`작성일 : ${issDateKr}`, { align:'right' });
      break;
    }
    case 'CAREER_DESCRIPTION': {
      doc.font('B').fontSize(20).fillColor('#111').text('SW 기술자 경력증명서', { align:'center' });
      doc.font('R').fontSize(9).fillColor('#666').text('[ 한국소프트웨어산업협회 ]', { align:'center' });
      hr(doc);
      labelRow(doc, '성        명', user.name);
      labelRow(doc, '생    년    도', `${m.birthYear}년생`);
      labelRow(doc, '소        속', `(주)이액티브  ${user.team}`);
      labelRow(doc, '직        위', m.posKr);
      const careerYears = 2026 - m.startYear;
      labelRow(doc, '총    경    력', `${careerYears}년 이상`);
      hr(doc);
      doc.font('B').fontSize(11).fillColor('#111').text('■ 주요 기술 및 역할').moveDown(0.3);
      doc.font('R').fontSize(10).fillColor('#222')
         .text(`  보유 기술 : ${m.techs}`)
         .text(`  수행 역할 : 소프트웨어 설계 · 개발 · 유지보수`);
      doc.moveDown(1.5);
      hr(doc);
      doc.font('R').fontSize(9).fillColor('#666')
         .text(`발급일 : ${issDateKr}          발급기관 : 한국소프트웨어산업협회`, { align:'right' });
      break;
    }
    case 'GRADUATION_CERTIFICATE': {
      doc.font('B').fontSize(20).fillColor('#111').text('졸  업  증  명  서', { align:'center' });
      doc.font('R').fontSize(11).fillColor('#333').text(m.univ, { align:'center' });
      hr(doc);
      labelRow(doc, '성        명', user.name);
      labelRow(doc, '학        과', `${m.dept}`);
      labelRow(doc, '학        위', m.degKr);
      labelRow(doc, '졸    업    일', `${m.gradYear}년 02월 20일`);
      hr(doc);
      doc.moveDown(1.2);
      doc.font('R').fontSize(10).fillColor('#111')
         .text('위 사람은 본 대학교 소정 교과 과정을 이수하고 졸업하였음을 증명합니다.', { align:'center' });
      doc.moveDown(2);
      doc.font('R').fontSize(10).text(issDateKr, { align:'center' });
      doc.moveDown(0.8);
      doc.font('B').fontSize(11).text(`${m.univ} 총 장`, { align:'center' });
      break;
    }
    case 'LICENSE': {
      doc.font('B').fontSize(22).fillColor('#111').text('자  격  증', { align:'center' });
      hr(doc);
      labelRow(doc, '자  격  명', '정보처리기사');
      labelRow(doc, '성        명', user.name);
      labelRow(doc, '생    년    도', `${m.birthYear}년생`);
      labelRow(doc, '등  록  번  호', m.licenseNo);
      labelRow(doc, '취    득    일', issDateKr);
      const expiry = fmtDateKr(addYears(issDate, 5));
      labelRow(doc, '유  효  기  간', `${issDateKr} ~ ${expiry}`);
      hr(doc);
      doc.moveDown(1.2);
      doc.font('R').fontSize(10).fillColor('#111')
         .text('위 사람은 「국가기술자격법」에 의거 위의 자격을 취득하였음을 증명합니다.', { align:'center' });
      doc.moveDown(2);
      doc.font('B').fontSize(11).text('한국산업인력공단 이사장', { align:'center' });
      break;
    }
    case 'NATIONAL_PENSION_CERTIFICATE': {
      doc.font('B').fontSize(20).fillColor('#111').text('국민연금 가입증명서', { align:'center' });
      hr(doc);
      labelRow(doc, '성        명', user.name);
      labelRow(doc, '가  입  종  류', '직장가입자');
      labelRow(doc, '사    업    장', '(주)이액티브');
      labelRow(doc, '자격 취득일', `${m.startYear}년 03월 01일`);
      hr(doc);
      doc.font('R').fontSize(9).fillColor('#555')
         .text('이 증명서는 국민연금 가입자의 가입사항을 증명하는 서류입니다.')
         .moveDown(0.5)
         .text(`발급일 : ${issDateKr}          발급기관 : 국민연금공단`, { align:'right' });
      break;
    }
    case 'HEALTH_INSURANCE_CERTIFICATE': {
      doc.font('B').fontSize(20).fillColor('#111').text('건강보험 가입증명서', { align:'center' });
      hr(doc);
      labelRow(doc, '성        명', user.name);
      labelRow(doc, '가  입  종  류', '직장가입자');
      labelRow(doc, '사    업    장', '(주)이액티브');
      labelRow(doc, '자격 취득일', `${m.startYear}년 03월 01일`);
      hr(doc);
      doc.font('R').fontSize(9).fillColor('#555')
         .text('이 증명서는 국민건강보험 직장가입자의 가입을 증명하는 서류입니다.')
         .moveDown(0.5)
         .text(`발급일 : ${issDateKr}          발급기관 : 국민건강보험공단`, { align:'right' });
      break;
    }
    case 'HEALTH_INSURANCE_ELIGIBILITY': {
      doc.font('B').fontSize(20).fillColor('#111').text('건강보험 자격득실확인서', { align:'center' });
      hr(doc);
      labelRow(doc, '성        명', user.name);
      hr(doc);
      doc.font('B').fontSize(10).fillColor('#333').text('■ 자격 내역').moveDown(0.3);
      doc.font('R').fontSize(10).fillColor('#222')
         .text(`  구  분 : 직장 취득`)
         .text(`  취득일 : ${m.startYear}년 03월 01일`)
         .text(`  사업장 : (주)이액티브`);
      hr(doc);
      doc.font('R').fontSize(9).fillColor('#555')
         .text(`발급일 : ${issDateKr}          발급기관 : 국민건강보험공단`, { align:'right' });
      break;
    }
    case 'PROFILE_PHOTO': {
      doc.font('B').fontSize(20).fillColor('#111').text('증  명  사  진', { align:'center' });
      hr(doc);
      doc.moveDown(0.8);
      // 사진 자리 박스
      const boxX = 211, boxY = doc.y, boxW = 130, boxH = 170;
      doc.save()
         .rect(boxX, boxY, boxW, boxH).lineWidth(1).strokeColor('#bbb').stroke()
         .restore();
      doc.font('R').fontSize(11).fillColor('#bbb')
         .text('[  사  진  ]', boxX, boxY + boxH / 2 - 8, { width: boxW, align:'center' });
      // 박스 아래로 이동
      doc.font('R').fontSize(11).fillColor('#111')
         .text(user.name, 60, boxY + boxH + 20, { width: 475, align:'center' });
      doc.font('R').fontSize(9).fillColor('#666')
         .text(`(${m.birthYear}년생)`, { align:'center' });
      doc.moveDown(1.5);
      hr(doc);
      doc.font('R').fontSize(9).fillColor('#666')
         .text(`발급일 : ${issDateKr}`, { align:'right' });
      break;
    }
    default: {
      doc.font('B').fontSize(18).fillColor('#111').text(makeTitle(user.name, docType), { align:'center' });
      hr(doc);
      labelRow(doc, '성        명', user.name);
      labelRow(doc, '소        속', `(주)이액티브 ${user.team}`);
    }
  }
}

async function buildPdf(user, docType, issuedDateStr) {
  return new Promise((resolve, reject) => {
    const doc = new PDFDocument({ size:'A4', margin:60, info:{ Title: makeTitle(user.name, docType) } });
    doc.registerFont('R', FONT_R);
    doc.registerFont('B', FONT_B);
    const chunks = [];
    doc.on('data', c => chunks.push(c));
    doc.on('end', () => resolve(Buffer.concat(chunks)));
    doc.on('error', reject);
    drawContent(doc, user, docType, issuedDateStr);
    doc.end();
  });
}

// ── R2 업로드 ──────────────────────────────────────────────────────────
async function uploadToR2(key, buf, contentType) {
  await s3.send(new PutObjectCommand({
    Bucket: BUCKET, Key: key,
    Body: buf, ContentType: contentType, ContentLength: buf.length,
  }));
}

// ── psql 헬퍼 ──────────────────────────────────────────────────────────
function psql(sql) {
  try {
    return execSync(
      `docker compose exec -T postgres psql -v ON_ERROR_STOP=1 -U resourcehub -d resourcehub`,
      { input: sql, stdio:['pipe','pipe','pipe'], cwd: PROJECT_ROOT }
    ).toString();
  } catch (e) {
    console.error('SQL 오류:\n' + (e.stderr ? e.stderr.toString() : e.message));
    throw e;
  }
}

function psqlQuery(sql) {
  let out;
  try {
    out = execSync(
      `docker compose exec -T postgres psql -v ON_ERROR_STOP=1 -U resourcehub -d resourcehub -A -F,`,
      { input: sql, stdio:['pipe','pipe','pipe'], cwd: PROJECT_ROOT, maxBuffer: 64*1024*1024 }
    ).toString();
  } catch (e) {
    console.error('SQL 오류:\n' + (e.stderr ? e.stderr.toString() : e.message));
    throw e;
  }
  const lines = out.trim().split('\n').filter(l => l && !/^\(\d+ rows?\)$/.test(l));
  if (lines.length < 2) return [];
  const headers = lines[0].split(',');
  return lines.slice(1).map(line => {
    const vals = line.split(',');
    const o = {};
    headers.forEach((h, i) => { o[h.trim()] = (vals[i] || '').trim(); });
    return o;
  });
}

function esc(s) {
  if (s == null) return 'NULL';
  return `'${String(s).replace(/'/g,"''")}'`;
}

// ── 메인 ───────────────────────────────────────────────────────────────
async function main() {
  if (!fs.existsSync(FONT_R)) { console.error(`❌ 폰트 없음: ${FONT_R}`); process.exit(1); }
  console.log(`R2 버킷 : ${BUCKET}`);
  console.log(`폰트    : ${FONT_B !== FONT_R ? 'malgun.ttf + malgunbd.ttf' : 'malgun.ttf (bold 없음)'}\n`);

  // ── Step 1: 개인 폴더 생성 ─────────────────────────────────────────
  console.log('Step 1: 개인 폴더 생성 중...');
  const folderVals = USERS.map(u =>
    `(${u.id}::bigint, ${esc(u.name+' 개인 폴더')}, 'PERSONAL'::text)`
  ).join(',\n');
  psql(`
INSERT INTO folders (owner_user_id, folder_name, created_at, updated_at, type)
SELECT v.uid, v.fname, NOW(), NOW(), v.ftype
FROM (VALUES ${folderVals}) AS v(uid, fname, ftype)
WHERE NOT EXISTS (
  SELECT 1 FROM folders f WHERE f.owner_user_id = v.uid AND f.type = 'PERSONAL'
);`);

  const folderRows = psqlQuery(
    `SELECT id, owner_user_id FROM folders WHERE type='PERSONAL'
     AND owner_user_id IN (${USERS.map(u=>u.id).join(',')}) ORDER BY owner_user_id`
  );
  const folderMap = {};
  folderRows.forEach(r => { folderMap[r.owner_user_id] = parseInt(r.id); });
  console.log(`  폴더 ${folderRows.length}개 준비 완료\n`);

  // ── Step 2: 태스크 목록 빌드 ──────────────────────────────────────
  const tasks = [];
  for (const user of USERS) {
    const folderId = folderMap[user.id];
    if (!folderId) { console.warn(`  ⚠ 폴더 없음: ${user.name}`); continue; }

    const types = user.proj >= 1 ? ALL_TYPES : (NO_PROJ_SUBSETS[user.id % 3] || ALL_TYPES);
    const tf = teamFolder(user.team);
    const pf = personFolder(user.name, user.email);

    for (const docType of types) {
      const key = `${user.id}_${docType}`;
      const isPending  = PENDING_SET.has(key);
      const isRejected = REJECTED_SET.has(key);

      let issuedDate = null, expiresAt = null, expiryWarnSentAt = null, expiredNoticeSentAt = null;
      const hasExpiry = ['LICENSE','NATIONAL_PENSION_CERTIFICATE',
        'HEALTH_INSURANCE_CERTIFICATE','HEALTH_INSURANCE_ELIGIBILITY'].includes(docType);
      if (hasExpiry) {
        const ei = getExpiryInfo(user.id, docType);
        issuedDate = fmtDate(ei.issuedDate);
        expiresAt  = fmtDate(ei.expiresAt);
        expiredNoticeSentAt = ei.expiredNotice ? fmtTs(ei.expiredNotice) : null;
        expiryWarnSentAt    = ei.expiryWarn    ? fmtTs(ei.expiryWarn)    : null;
      } else if (docType === 'GRADUATION_CERTIFICATE') {
        const m = userMeta(user);
        issuedDate = `${m.gradYear}-02-20`;
      }

      const daysAgo = 30 + (user.id * 7 + docType.length * 3) % 600;
      const uploadedAt = fmtTs(addDays(TODAY, -daysAgo));

      let reviewStatus, reviewedBy = null, reviewedAt = null, rejectReason = null;
      if (isPending)        { reviewStatus = 'PENDING_REVIEW'; }
      else if (isRejected)  {
        reviewStatus = 'REJECTED';
        reviewedBy = ADMIN_ID;
        reviewedAt = fmtTs(addDays(TODAY, -3));
        rejectReason = makeRejectReason(docType);
      } else { reviewStatus = 'APPROVED'; }

      const storedName  = crypto.randomUUID() + '.pdf';
      const storagePath = `${tf}/${pf}/${storedName}`;

      tasks.push({
        user, folderId, docType, title: makeTitle(user.name, docType),
        issuedDate, expiresAt, expiryWarnSentAt, expiredNoticeSentAt, uploadedAt,
        isPending, isRejected,
        origName: makeOrigName(user.name, docType),
        storedName, storagePath,
        contentType: 'application/pdf',
        degreeType:  docType === 'GRADUATION_CERTIFICATE' ? getDegreeType(user.pos) : null,
        certTypeMeta: docType === 'LICENSE' ? 'ENGINEER' : null,
        reviewStatus, reviewedBy, reviewedAt, rejectReason,
        fileSize: 0, checksum: '',
      });
    }
  }

  // ── Step 3: PDF 생성 + R2 업로드 ──────────────────────────────────
  console.log(`Step 2: PDF 생성 및 R2 업로드 중... (총 ${tasks.length}개)`);
  const BATCH = 10;
  for (let i = 0; i < tasks.length; i += BATCH) {
    const batch = tasks.slice(i, i + BATCH);
    await Promise.all(batch.map(async t => {
      const buf = await buildPdf(t.user, t.docType, t.issuedDate);
      await uploadToR2(t.storagePath, buf, t.contentType);
      t.fileSize = buf.length;
      t.checksum = crypto.createHash('sha256').update(buf).digest('hex');
    }));
    process.stdout.write(`  [${Math.min(i + BATCH, tasks.length)}/${tasks.length}]\r`);
  }
  console.log(`  R2 업로드 완료 (${tasks.length}개)              \n`);

  // ── Step 4: documents INSERT ───────────────────────────────────────
  console.log('Step 3: DB 레코드 삽입 중...');
  const docVals = tasks.map(t =>
    `(${t.folderId}, ${esc(t.docType)}, ${esc(t.title)}, 'ACTIVE',` +
    ` ${esc(t.uploadedAt)}, ${esc(t.uploadedAt)},` +
    ` ${t.expiresAt ? esc(t.expiresAt) : 'NULL'},` +
    ` ${t.issuedDate ? esc(t.issuedDate) : 'NULL'},` +
    ` ${t.degreeType ? esc(t.degreeType) : 'NULL'},` +
    ` ${t.certTypeMeta ? esc(t.certTypeMeta) : 'NULL'},` +
    ` ${t.expiryWarnSentAt ? esc(t.expiryWarnSentAt) : 'NULL'},` +
    ` ${t.expiredNoticeSentAt ? esc(t.expiredNoticeSentAt) : 'NULL'})`
  ).join(',\n');

  const insertedDocs = psqlQuery(`
INSERT INTO documents
  (folder_id, document_type, title, status, created_at, updated_at,
   expires_at, issued_date, degree_type, cert_type_meta,
   expiry_warn_sent_at, expired_notice_sent_at)
VALUES ${docVals}
RETURNING id, folder_id, document_type, title;`);

  console.log(`  documents ${insertedDocs.length}개 삽입`);

  const docIdMap = {};
  insertedDocs.forEach(r => {
    docIdMap[`${r.folder_id}|${r.document_type}|${r.title}`] = parseInt(r.id);
  });

  // ── Step 5: document_versions INSERT ──────────────────────────────
  const verVals = tasks.map(t => {
    const docId = docIdMap[`${t.folderId}|${t.docType}|${t.title}`];
    if (!docId) return null;
    return `(${docId}, 1, ${esc(t.origName)}, ${esc(t.storedName)}, ${esc(t.storagePath)},` +
      ` ${t.fileSize}, ${esc(t.contentType)}, ${esc(t.checksum)},` +
      ` ${t.reviewedBy ?? 'NULL'},` +
      ` ${esc(t.uploadedAt)}, ${esc(t.uploadedAt)},` +
      ` ${esc(t.reviewStatus)},` +
      ` ${t.reviewedAt ? esc(t.reviewedAt) : 'NULL'},` +
      ` ${t.rejectReason ? esc(t.rejectReason) : 'NULL'},` +
      ` ${t.user.id})`;
  }).filter(Boolean).join(',\n');

  const insertedVers = psqlQuery(`
INSERT INTO document_versions
  (document_id, version_no, original_file_name, stored_file_name, storage_path,
   file_size, content_type, checksum, reviewed_by,
   created_at, updated_at, review_status, reviewed_at, reject_reason, uploaded_by)
VALUES ${verVals}
RETURNING id, document_id, review_status;`);

  console.log(`  document_versions ${insertedVers.length}개 삽입`);

  // ── Step 6: current_version_id 업데이트 ────────────────────────────
  const approved = insertedVers.filter(v => v.review_status === 'APPROVED');
  if (approved.length > 0) {
    const caseStr = approved.map(v => `WHEN ${v.document_id} THEN ${v.id}`).join(' ');
    psql(`UPDATE documents SET current_version_id = CASE id ${caseStr} END
          WHERE id IN (${approved.map(v => v.document_id).join(',')});`);
    console.log(`  current_version_id ${approved.length}개 업데이트`);
  }

  const pending  = tasks.filter(t => t.isPending).length;
  const rejected = tasks.filter(t => t.isRejected).length;
  console.log(`
✅ 완료!
   문서 : ${insertedDocs.length}개
   버전 : ${insertedVers.length}개
   R2   : ${tasks.length}개 파일
   APPROVED: ${approved.length}  PENDING: ${pending}  REJECTED: ${rejected}
`);
}

function userMeta(user) {
  const birthYear = 2026 - user.age;
  const gradYear  = birthYear + 22;
  const startYear = gradYear + 1;
  return {
    birthYear, gradYear, startYear,
    univ:   UNIVERSITIES[user.id % UNIVERSITIES.length],
    dept:   DEPARTMENTS[user.id % DEPARTMENTS.length],
    techs:  TECH_POOLS[user.id % TECH_POOLS.length],
    degree: getDegreeType(user.pos),
    degKr:  DEGREE_KR[getDegreeType(user.pos)] || '학사',
    posKr:  POS_KR[user.pos] || '사원',
    licenseNo: `${String(2026 - user.age).slice(2)}${String(user.id*37%999+100).padStart(5,'0')}-${crypto.createHash('md5').update(user.email).digest('hex').slice(0,7).toUpperCase()}`,
  };
}

main().catch(err => { console.error(err); process.exit(1); });
