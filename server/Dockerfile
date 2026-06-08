# Use an official Python slim runtime as a parent image
FROM python:3.11-slim

# Set environment variables
# Prevent Python from writing .pyc files to disk
ENV PYTHONDONTWRITEBYTECODE=1
# Prevent Python from buffering stdout/stderr
ENV PYTHONUNBUFFERED=1

# Set the working directory in the container
WORKDIR /app

# Install system dependencies if any are needed
# (cryptography wheels are prebuilt, but curl is useful for health checks)
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Copy the requirements file first to leverage Docker build cache
COPY requirements.txt /app/

# Install the Python dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy the rest of the application code
COPY . /app/

# Create a non-privileged user and switch to it for security hardening
RUN useradd -u 10001 -m appuser && chown -R appuser:appuser /app
USER appuser

# Expose the port the server listens on
EXPOSE 3000

# Run the FastAPI server using uvicorn
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "3000"]
