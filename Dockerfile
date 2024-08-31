FROM tiangolo/uvicorn-gunicorn-fastapi

COPY requirements.txt .
COPY API_DB.py .
COPY .env .

RUN pip install --no-cache-dir -r requirements.txt

EXPOSE 8000

CMD ["uvicorn", "API_DB:app", "--host", "0.0.0.0", "--port", "8000", "--reload"]