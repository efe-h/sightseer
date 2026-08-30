FROM python:3.12-slim

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

WORKDIR /app

# Install runtime dependencies before copying source code,
# allowing Docker to cache this layer.
COPY recommendation_engine/requirements.txt requirements.txt

RUN python -m pip install \
    --no-cache-dir \
    -r requirements.txt

# Copy the recommendation package.
COPY recommendation_engine recommendation_engine

# The recommender resolves this file relative to the project root.
COPY rag-pipeline/data/london_clustered_attractions.json \
    rag-pipeline/data/london_clustered_attractions.json

# Run the API as a non-root user.
RUN useradd \
    --system \
    --uid 10001 \
    --no-create-home \
    sightseer

USER sightseer

EXPOSE 8000

CMD ["python","-m","uvicorn","recommendation_engine.api.main:app","--host","0.0.0.0","--port","8000"]