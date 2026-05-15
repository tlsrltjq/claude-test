FROM python:3.12-slim

# LibreOffice + 한글 폰트 설치
RUN apt-get update && apt-get install -y --no-install-recommends \
        libreoffice-writer \
        fonts-noto-cjk \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY generate.py app.py ./

RUN mkdir -p employees output

EXPOSE 5000

CMD ["python", "app.py"]
