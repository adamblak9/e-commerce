Docker Migration: Complete containerization of ShopEase platform

🔄 Architecture Changes:
- Application now runs entirely in Docker containers
- Tomcat (port 8080) + SQL Server (port 1433) orchestrated via docker-compose
- WAR deployed as ROOT.war (no /ecommerce context path)

🔧 Code Updates:
- Frontend: Changed all fetch() URLs from 'http://localhost:8080/ecommerce/api/*' to '/api/*'
- Backend: persistence.xml JDBC URL updated from 'localhost' to 'commerce-sqlserver-1'
- JwtFilter: Public routes properly configured (/api/auth/login, /api/auth/signup)

📦 New Files:
- Dockerfile: Multi-stage build for Tomcat deployment ..
- docker-compose.yml: Service orchestration with automatic networking

✅ Benefits:
- No local Tomcat or SQL Server installation required
- One command setup: docker-compose up --build
- Consistent environment across all team members
- Easy onboarding for new developers

🚀 Quick Start:
1. docker-compose up --build
2. Visit http://localhost:8080/login.html
