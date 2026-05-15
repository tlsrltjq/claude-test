#!/usr/bin/env python3
"""재직증명서 자동 발급 스크립트

사용법:
  python generate.py --name 홍길동          # 단건 발급
  python generate.py --csv employees.csv   # CSV 일괄 발급
  python generate.py --all                 # 전체 직원 발급
  python generate.py --create 홍길동        # 기본 템플릿 생성
"""

import argparse
import csv
import subprocess
import sys
from datetime import datetime
from pathlib import Path

from docx import Document
from docx.shared import Pt, Cm
from docx.enum.text import WD_ALIGN_PARAGRAPH

BASE_DIR = Path(__file__).parent
EMPLOYEES_DIR = BASE_DIR / "employees"
OUTPUT_DIR = BASE_DIR / "output"

# 치환 가능한 플레이스홀더 목록
# 추후 {{소속}}, {{직위}}, {{입사일}} 등을 여기에 추가
PLACEHOLDERS = {
    "{{발급일자}}": lambda: datetime.today().strftime("%Y년 %m월 %d일"),
}


def _build_replacements() -> dict:
    return {k: v() for k, v in PLACEHOLDERS.items()}


def _replace_in_doc(doc: Document, replacements: dict) -> None:
    """DOCX 본문·표 안의 플레이스홀더를 치환한다.

    run이 분할된 경우에도 동작하도록 단락 전체 텍스트를 기준으로 치환 후
    첫 번째 run에 결과를 쓰고 나머지 run을 비운다.
    """
    def _replace_para(para):
        if not any(k in para.text for k in replacements):
            return
        full = "".join(r.text for r in para.runs)
        for key, val in replacements.items():
            full = full.replace(key, val)
        for i, run in enumerate(para.runs):
            run.text = full if i == 0 else ""

    for para in doc.paragraphs:
        _replace_para(para)
    for table in doc.tables:
        for row in table.rows:
            for cell in row.cells:
                for para in cell.paragraphs:
                    _replace_para(para)


def _convert_to_pdf(docx_path: Path) -> Path:
    """LibreOffice headless로 PDF 변환."""
    result = subprocess.run(
        [
            "libreoffice", "--headless",
            "--convert-to", "pdf",
            "--outdir", str(docx_path.parent),
            str(docx_path),
        ],
        capture_output=True,
        text=True,
    )
    if result.returncode != 0:
        raise RuntimeError(result.stderr.strip() or "libreoffice 변환 실패")
    return docx_path.parent / (docx_path.stem + ".pdf")


def generate_one(name: str) -> bool:
    """단건 발급. 성공 시 True, 미등록 직원이면 False."""
    template = EMPLOYEES_DIR / f"{name}.docx"
    if not template.exists():
        print(f"[SKIP] 등록되지 않은 직원: {name}")
        return False

    today_str = datetime.today().strftime("%Y%m%d")
    stem = f"재직증명서_{name}_{today_str}"
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    docx_out = OUTPUT_DIR / f"{stem}.docx"

    doc = Document(template)
    _replace_in_doc(doc, _build_replacements())
    doc.save(docx_out)
    print(f"[OK]   DOCX: {docx_out.name}")

    try:
        pdf_out = _convert_to_pdf(docx_out)
        print(f"[OK]   PDF : {pdf_out.name}")
    except Exception as e:
        print(f"[WARN] PDF 변환 실패 ({name}): {e}")

    return True


def _run_batch(names: list) -> tuple:
    success, failed = [], []
    for name in names:
        (success if generate_one(name) else failed).append(name)
    print(f"\n── 완료: 성공 {len(success)}건 / 미등록 {len(failed)}건 ──")
    if failed:
        print("  미등록:", ", ".join(failed))
    return success, failed


def generate_from_csv(csv_path: str) -> tuple:
    names = []
    with open(csv_path, newline="", encoding="utf-8-sig") as f:
        reader = csv.DictReader(f)
        for row in reader:
            name = row.get("이름", "").strip()
            if name:
                names.append(name)
    if not names:
        print("[INFO] CSV에서 이름을 찾을 수 없습니다. '이름' 컬럼을 확인하세요.")
        return [], []
    return _run_batch(names)


def generate_from_names(names: list) -> tuple:
    """Flask API에서 직접 이름 목록을 받아 처리하는 진입점."""
    return _run_batch(names)


def generate_all() -> tuple:
    names = [p.stem for p in EMPLOYEES_DIR.glob("*.docx")]
    if not names:
        print("[INFO] employees/ 폴더에 템플릿이 없습니다.")
        return [], []
    return _run_batch(sorted(names))


def create_template(name: str) -> Path:
    """기본 재직증명서 템플릿을 employees/{name}.docx 로 생성한다.

    {{발급일자}} 플레이스홀더만 있는 최소 구조로 생성.
    추후 부서·직위 등 필드는 이 파일을 직접 편집해 {{필드명}} 형식으로 추가.
    """
    EMPLOYEES_DIR.mkdir(parents=True, exist_ok=True)
    out_path = EMPLOYEES_DIR / f"{name}.docx"
    if out_path.exists():
        print(f"[SKIP] 이미 존재합니다: {out_path}")
        return out_path

    doc = Document()

    # 기본 폰트
    style = doc.styles["Normal"]
    style.font.name = "맑은 고딕"
    style.font.size = Pt(11)

    def add_centered(text, bold=False, size=None):
        p = doc.add_paragraph()
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        run = p.add_run(text)
        run.bold = bold
        if size:
            run.font.size = Pt(size)
        return p

    def add_line(text):
        return doc.add_paragraph(text)

    # 제목
    add_centered("재  직  증  명  서", bold=True, size=22)
    doc.add_paragraph()
    doc.add_paragraph()

    # 인적 사항 표
    table = doc.add_table(rows=4, cols=2)
    table.style = "Table Grid"
    labels = ["성    명", "소    속", "직    위", "입 사 일"]
    # 이름은 고정, 나머지는 추후 플레이스홀더로 채울 자리
    values = [name, "", "", ""]
    for i, (label, value) in enumerate(zip(labels, values)):
        table.cell(i, 0).text = label
        table.cell(i, 1).text = value
    doc.add_paragraph()
    doc.add_paragraph()

    # 본문
    body = doc.add_paragraph()
    body.alignment = WD_ALIGN_PARAGRAPH.CENTER
    body.add_run("위 사람은 당사에 재직 중임을 증명합니다.")
    doc.add_paragraph()
    doc.add_paragraph()

    # 발급일자 (플레이스홀더)
    date_p = doc.add_paragraph()
    date_p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    date_p.add_run("{{발급일자}}")
    doc.add_paragraph()
    doc.add_paragraph()

    # 서명란
    add_centered("(주) 이액티브  대표이사  (인)", bold=True)

    doc.save(out_path)
    print(f"[OK] 템플릿 생성: {out_path}")
    print("     ※ 소속·직위·입사일 등은 이 파일을 직접 편집해 {{필드명}} 형식으로 추가하세요.")
    return out_path


def main():
    parser = argparse.ArgumentParser(
        description="재직증명서 자동 발급",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__,
    )
    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument("--name",   metavar="이름",   help="단건 발급")
    group.add_argument("--csv",    metavar="파일경로", help="CSV 일괄 발급 (이름 컬럼 필요)")
    group.add_argument("--all",    action="store_true", help="전체 직원 발급")
    group.add_argument("--create", metavar="이름",   help="기본 템플릿 생성")
    args = parser.parse_args()

    if args.name:
        ok = generate_one(args.name)
        sys.exit(0 if ok else 1)
    elif args.csv:
        generate_from_csv(args.csv)
    elif args.all:
        generate_all()
    elif args.create:
        create_template(args.create)


if __name__ == "__main__":
    main()
