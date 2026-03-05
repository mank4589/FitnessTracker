# Docker Build Instructions for ufit

## Prerequisites
- Docker Desktop installed and running
- Windows with WSL2 enabled (for Windows users)

## Building the Docker Image

Navigate to the ufit directory and run:

```bash
docker build -t ufit:v1 .
```

To build for a specific platform:

```bash
docker build --platform linux/amd64 -t ufit:v1 .
```

## Running the Container

### Basic Run
```bash
docker run -p 8080:8080 -p 8082:8082 ufit:v1
```

### With Persistent Data Volume
```bash
docker run -p 8080:8080 -p 8082:8082 -v ufit-data:/app/data ufit:v1
```

### With JavaFX GUI Support (Linux/WSL2)
For JavaFX applications, you need X11 forwarding:

```bash
# Allow X11 connections (Linux)
xhost +local:docker

# Run with display
docker run -e DISPLAY=$DISPLAY \
  -v /tmp/.X11-unix:/tmp/.X11-unix \
  -p 8080:8080 -p 8082:8082 \
  ufit:v1
```

### Environment Variables
You can override Java options:

```bash
docker run -e JAVA_OPTS="-Xmx1024m -Xms512m" -p 8080:8080 ufit:v1
```

## Accessing the Application

- **Spring Boot Application**: http://localhost:8080
- **H2 Console**: http://localhost:8082/h2-console
  - JDBC URL: `jdbc:h2:file:/app/data/ufitdb`
  - Username: (check application.properties)
  - Password: (check application.properties)

## Important Notes

### JavaFX in Docker
This application uses JavaFX, which is a desktop GUI framework. Running GUI applications in Docker requires:
1. **X11 Forwarding** (Linux/macOS): Display GUI on host machine
2. **VNC Server**: Access GUI through web browser
3. **Headless Mode**: If the application supports running without GUI

The current Dockerfile is optimized for building and can run the Spring Boot backend. For full GUI support, additional configuration may be needed.

### Database Persistence
The H2 database is stored in `/app/data/ufitdb`. Use Docker volumes to persist data:
```bash
docker volume create ufit-data
docker run -v ufit-data:/app/data -p 8080:8080 ufit:v1
```

## Troubleshooting

### Build Fails
- Ensure you have a stable internet connection for downloading dependencies
- Check Maven wrapper permissions: `chmod +x mvnw` (on Linux/macOS)

### JavaFX Display Issues
- Error: "Could not initialize class com.sun.glass.ui.Application"
  - Solution: Use X11 forwarding or VNC
- Error: "No X11 DISPLAY variable was set"
  - Solution: Set DISPLAY environment variable properly

### Memory Issues
- If the application crashes with OutOfMemoryError, increase heap size:
  ```bash
  docker run -e JAVA_OPTS="-Xmx2048m" -p 8080:8080 ufit:v1
  ```
