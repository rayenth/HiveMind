FROM python:3.12-slim

WORKDIR /app

# Install dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir flask flask-cors ollama requests chardet

# Copy application code
COPY app.py .
COPY detect_anomaly.py .

# Create necessary directories
RUN mkdir -p logs archive results

# Expose port
EXPOSE 5001

# Environment variables
ENV PORT=5001
ENV HOST=0.0.0.0
ENV FLASK_ENV=production

# Run the application
CMD ["python", "app.py"]
