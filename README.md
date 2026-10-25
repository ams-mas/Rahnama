# Rahnama — Afghan Real Estate Management

Starter foundation for a Persian/Dari RTL real-estate office system for Afghanistan.

## Stack
- PHP 8.2+ REST API
- MySQL 8+
- Web admin UI and future Android/iOS app connected through the same API
- Jalali calendar at the UI layer; store timestamps consistently in MySQL
- Default currency AFN; optional USD
- No online payments or payment gateway

## Structure
- `config.example.yaml`: application behavior and feature flags
- `.env.example`: secret/environment variable template (never commit real credentials)
- `database/migrations/001_initial_schema.sql`: database schema
- `backend/public/index.php`: API entry point
- `backend/src/`: small API skeleton
- `README.md`: setup notes

## Initial setup
1. Create a MySQL database and user.
2. Copy `.env.example` to `.env` and set database credentials. Do not commit `.env`.
3. Import `database/migrations/001_initial_schema.sql`.
4. Configure the PHP web server document root to `backend/public`.
5. Start with `GET /api/health` to check that the API entry point is responding.

This is a starter foundation, not a production-ready application. Authentication, authorization, validation, rate limiting, migrations runner, and the UI must be completed and tested before deployment.
