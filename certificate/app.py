#!/usr/bin/env python3
"""Flask HTTP API — Spring Boot 관리자 패널에서 호출하는 내부 서비스."""

from flask import Flask, jsonify, request, send_file
from pathlib import Path

import generate as gen

app = Flask(__name__)

OUTPUT_DIR = gen.OUTPUT_DIR
EMPLOYEES_DIR = gen.EMPLOYEES_DIR


@app.route("/health")
def health():
    return jsonify({"status": "ok"})


@app.route("/generate", methods=["POST"])
def do_generate():
    data = request.get_json(force=True, silent=True) or {}

    if "name" in data:
        name = data["name"].strip()
        ok = gen.generate_one(name)
        return jsonify({
            "success": [name] if ok else [],
            "failed":  [] if ok else [name],
        })

    if "names" in data:
        names = [n.strip() for n in data["names"] if n.strip()]
        success, failed = gen.generate_from_names(names)
        return jsonify({"success": success, "failed": failed})

    if data.get("all"):
        success, failed = gen.generate_all()
        return jsonify({"success": success, "failed": failed})

    return jsonify({"error": "name, names, 또는 all 파라미터 필요"}), 400


@app.route("/create", methods=["POST"])
def do_create():
    data = request.get_json(force=True, silent=True) or {}
    name = data.get("name", "").strip()
    if not name:
        return jsonify({"error": "이름 필요"}), 400
    path = gen.create_template(name)
    return jsonify({"status": "ok", "path": str(path)})


@app.route("/templates")
def list_templates():
    names = sorted(p.stem for p in EMPLOYEES_DIR.glob("*.docx")) if EMPLOYEES_DIR.exists() else []
    return jsonify({"templates": names})


@app.route("/files")
def list_files():
    if not OUTPUT_DIR.exists():
        return jsonify({"files": []})
    files = sorted(
        (f.name for f in OUTPUT_DIR.iterdir() if f.is_file()),
        reverse=True,
    )
    return jsonify({"files": files})


@app.route("/files/cleanup", methods=["POST"])
def cleanup_files():
    """output/ 디렉터리를 정리한다. maxFiles 초과분을 오래된 것부터 삭제."""
    data = request.get_json(force=True, silent=True) or {}
    max_files = int(data.get("maxFiles", 50))

    if not OUTPUT_DIR.exists():
        return jsonify({"deleted": [], "remaining": 0})

    all_files = sorted(
        [f for f in OUTPUT_DIR.iterdir() if f.is_file()],
        reverse=True,   # 파일명 내림차순 = 날짜 내림차순 (최신 먼저)
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
