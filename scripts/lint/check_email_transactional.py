#!/usr/bin/env python3
"""
emailSender 호출이 @Transactional 컨텍스트에 있을 때
같은 메서드 내 try { ... } 블록 안에 있는지 검사한다.

기존 security-lint.sh [10] 의 파일 단위 휴리스틱(같은 파일에 try 가 있기만
하면 PASS)을 메서드 단위로 정밀화한다.

휴리스틱:
- 클래스/메서드/try/일반 블록을 brace 깊이로 추적.
- @Transactional 어노테이션은 직후의 class 또는 method 에 적용된다고 본다.
- emailSender.xxx() 호출 위치에서 enclosing method (또는 클래스 @Transactional)
  가 트랜잭션 컨텍스트인지 확인하고, 호출이 method 내부의 try 블록 안에
  있는지 확인.

종료 코드:
  0 — 위반 없음
  1 — 한 건이라도 위반 발견 (stderr 에 출력)
사용:
  python3 scripts/lint/check_email_transactional.py <file1.java> <file2.java> ...
"""
from __future__ import annotations

import re
import sys
from pathlib import Path

TX_ANNOTATION = re.compile(r"@Transactional\b")
CLASS_HEADER = re.compile(r"\bclass\s+\w+")
# 단일 라인 메서드 시그니처(... methodName(...) {) — 본 저장소 컨벤션
METHOD_HEADER = re.compile(
    r"^\s*(?:@\w+(?:\([^)]*\))?\s+)*"
    r"(?:public|protected|private)\s+"
    r"(?:static\s+|final\s+|synchronized\s+|abstract\s+|default\s+)*"
    r"[\w<>\[\]\?\s,]+\s+"
    r"\w+\s*\([^)]*\)\s*"
    r"(?:throws\s+[\w.,\s]+)?\s*\{"
)
TRY_TAIL = re.compile(r"\btry\s*$")
EMAIL_CALL = re.compile(r"\bemailSender\.\w+")


def _strip_noise(line: str) -> str:
    """라인 주석·문자열 리터럴 제거. 위치를 유지하기 위해 공백으로 치환."""
    # 라인 주석
    line = re.sub(r"//[^\n]*", lambda m: " " * len(m.group(0)), line)
    # 문자열 리터럴 (multi-line 문자열은 본 저장소에 없음)
    line = re.sub(
        r'"(?:\\.|[^"\\])*"',
        lambda m: " " * len(m.group(0)),
        line,
    )
    return line


def scan(path: Path) -> list[tuple[int, str]]:
    src = path.read_text(encoding="utf-8", errors="replace")
    # 블록 주석 제거 — 줄바꿈은 보존
    src = re.sub(
        r"/\*[\s\S]*?\*/",
        lambda m: "".join(c if c == "\n" else " " for c in m.group(0)),
        src,
    )

    issues: list[tuple[int, str]] = []
    stack: list[dict] = []  # frames: {kind: class|method|try|block, tx: bool}
    pending_tx = False

    for lineno, raw in enumerate(src.splitlines(), 1):
        line = _strip_noise(raw)
        stripped = line.strip()

        if TX_ANNOTATION.search(stripped):
            pending_tx = True
            # 라인에 다른 토큰이 없으면 다음 라인으로
            if not re.search(r"[{}]|emailSender", stripped):
                continue

        # 라인 안의 events 위치순으로 처리
        events: list[tuple[int, str]] = []
        for m in re.finditer(r"\{|\}", line):
            events.append((m.start(), m.group(0)))
        for m in EMAIL_CALL.finditer(line):
            events.append((m.start(), "CALL"))
        events.sort(key=lambda e: e[0])

        # 메서드/클래스 헤더는 라인 단위로 한 번만 매칭 (행 안에 { 가 여러 개여도
        # 첫 번째 { 가 헤더용일 가능성이 높음)
        method_consumed = False
        class_consumed = False

        for pos, tok in events:
            if tok == "{":
                prefix = line[:pos]
                if (
                    not class_consumed
                    and CLASS_HEADER.search(line)
                    and CLASS_HEADER.search(line).end() <= pos
                ):
                    stack.append({"kind": "class", "tx": pending_tx})
                    pending_tx = False
                    class_consumed = True
                elif not method_consumed and METHOD_HEADER.search(line):
                    stack.append({"kind": "method", "tx": pending_tx})
                    pending_tx = False
                    method_consumed = True
                elif TRY_TAIL.search(prefix.rstrip()):
                    stack.append({"kind": "try", "tx": False})
                else:
                    stack.append({"kind": "block", "tx": False})
            elif tok == "}":
                if stack:
                    stack.pop()
            elif tok == "CALL":
                in_try = False
                tx_active = False
                for frame in reversed(stack):
                    if frame["kind"] == "try":
                        in_try = True
                    if frame["kind"] == "method":
                        tx_active = frame["tx"] or tx_active
                        # 메서드 frame 위에 클래스 frame 이 있을 수 있음
                        # 클래스 레벨 @Transactional 도 체크
                        cls_frame = next(
                            (f for f in reversed(stack) if f["kind"] == "class"),
                            None,
                        )
                        if cls_frame and cls_frame["tx"]:
                            tx_active = True
                        break
                    if frame["kind"] == "class":
                        # 메서드 frame 없이 class 만난 경우(초기화 블록 등)
                        tx_active = frame["tx"]
                        break
                if tx_active and not in_try:
                    issues.append((lineno, raw.strip()))

    return issues


def main(argv: list[str]) -> int:
    files = [Path(p) for p in argv[1:]]
    if not files:
        print("usage: check_email_transactional.py <file.java> ...", file=sys.stderr)
        return 2

    total = 0
    for f in files:
        if not f.exists():
            continue
        for lineno, snippet in scan(f):
            print(
                f"  {f}:{lineno}  {snippet}",
                file=sys.stderr,
            )
            total += 1
    return 1 if total else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
