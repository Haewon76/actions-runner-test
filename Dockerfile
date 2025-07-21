# 베이스 이미지로 nginx 사용
FROM nginx:alpine

# 인덱스 HTML 파일 복사
COPY ./index.html /usr/share/nginx/html/index.html

# 포트 노출 (기본 80)
EXPOSE 80