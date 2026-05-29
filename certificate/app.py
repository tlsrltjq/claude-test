#!/usr/bin/env python3
"""Flask HTTP API — Spring Boot 관리자 패널에서 호출하는 내부 서비스."""

from flask import Flask, jsonify, request, send_file
from pathlib import Path

import generate as gen

app = Flask(__name__)

OUTPUT_DIR = gen.OUTPUT_DIR
TEMPLATE_PATH = gen.TEMPLATE_PATH


@app.route("/health")
def health():
    return jsonify({"status": "ok"})


@app.route("/generate", methods=["POST"])
def do_generate():
    """직원 정보를 받아 재직증명서를 발급한다.

    단건: {"employee": {"name": "홍길동", "team": "개발팀", "position": "사원", "address": "...", "joinDate": "..."}}
    다건: {"employees": [{...}, {...}]}
    """
    data = request.get_json(force=True, silent=True) or {}

    if "employee" in data:
        emp = data["employee"]
        name = (emp.get("name") or "").strip()
        ok = gen.generate_one(emp)
        return jsonify({
            "success": [name] if ok else [],
            "failed":  [] if ok else [name],
        })

    if "employees" in data:
        employees = [e for e in data["employees"] if (e.get("name") or "").strip()]
        success, failed = gen.generate_from_employees(employees)
        return jsonify({"success": success, "failed": failed})

    return jsonify({"error": "employee 또는 employees 파라미터 필요"}), 400


@app.route("/create", methods=["POST"])
def do_create():
    """공유 양식이 없으면 기본 양식을 생성한다."""
    path = gen.create_base_template()
    return jsonify({"status": "ok", "path": str(path)})


@app.route("/template/status")
def template_status():
    """공유 양식 존재 여부를 반환한다."""
    return jsonify({"exists": TEMPLATE_PATH.exists(), "path": str(TEMPLATE_PATH)})


@app.route("/files")
def list_files():
    if not OUTPUT_DIR.exists():
        return jsonify({"files": []})
    files_sorted = sorted(
        [f for f in OUTPUT_DIR.iterdir() if f.is_file()],
        key=lambda f: f.stat().st_mtime,
        reverse=True,
    )
    return jsonify({"files": [f.name for f in files_sorted]})


@app.route("/files/cleanup", methods=["POST"])
def cleanup_files():
    """output/ 디렉터리를 정리한다. maxFiles 초과분을 오래된 것부터 삭제."""
    data = request.get_json(force=True, silent=True) or {}
    max_files = int(data.get("maxFiles", 50))

    if not OUTPUT_DIR.exists():
        return jsonify({"deleted": [], "remaining": 0})

    all_files = sorted(
        [f for f in OUTPUT_DIR.iterdir() if f.is_file()],
        key=lambda f: f.stat().st_mtime,
        reverse=True,   # 최신 파일 먼저, 오래된 파일을 maxFiles 초과 시 삭제
    )

    deleted = []
    for f in all_files[max_files:]:
        try:
            f.unlink()
            deleted.append(f.name)
        except Exception:
            pass

    return jsonify({"deleted": deleted, "remaining": len(all_files) - len(deleted)})


@app.route("/download/<path:filename>")
def download(filename):
    file_path = OUTPUT_DIR / filename
    if not file_path.exists() or not file_path.is_file():
        return jsonify({"error": "파일 없음"}), 404
    # 경로 탈출 방지
    try:
        file_path.resolve().relative_to(OUTPUT_DIR.resolve())
    except ValueError:
        return jsonify({"error": "잘못된 경로"}), 400
    return send_file(file_path, as_attachment=True, download_name=filename)


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=False)
