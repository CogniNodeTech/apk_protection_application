"""
Database setup and session helpers using SQLAlchemy.
Supports MySQL as production database, and SQLite fallback for local development/testing.
"""
from __future__ import annotations

import os
from contextlib import contextmanager
from typing import Generator

from sqlalchemy import create_engine
from sqlalchemy.orm import declarative_base, sessionmaker

# Database URL from environment, fallback to SQLite file locally
DATABASE_URL = os.environ.get(
    "DATABASE_URL",
    "mysql+pymysql://root:password@localhost/safeguard_db"
).strip()

# Configure engine arguments based on dialect
engine_args = {}
if DATABASE_URL.startswith("sqlite"):
    # SQLite-specific flags for multithreaded FastAPI requests
    engine_args["connect_args"] = {"check_same_thread": False}
else:
    # MySQL production-grade connection pooling configs
    engine_args.update({
        "pool_size": 10,
        "max_overflow": 20,
        "pool_recycle": 3600,
        "pool_pre_ping": True,
    })

engine = create_engine(DATABASE_URL, **engine_args)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()


def init_db() -> None:
    """Create all tables defined by SQLAlchemy models if they do not exist."""
    Base.metadata.create_all(bind=engine)


@contextmanager
def get_db_session() -> Generator[SessionLocal, None, None]:
    """Scoped session manager (with resource cleanup/rollback on exceptions)."""
    db = SessionLocal()
    try:
        yield db
        db.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


def get_db() -> Generator[SessionLocal, None, None]:
    """FastAPI Dependency Injection database session provider."""
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
