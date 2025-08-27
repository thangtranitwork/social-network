FROM maven:3.9.9-amazoncorretto-17 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn package -DskipTests

# Runtime image
FROM amazoncorretto:17.0.12-alpine3.17
WORKDIR /app

# Copy jar từ stage build
COPY --from=build /app/target/*.jar app.jar

# Tạo thư mục /data
RUN mkdir /data

# Nếu bạn muốn mount volume để giữ dữ liệu, có thể thêm:
VOLUME ["/data"]

ENTRYPOINT ["java", "-jar", "app.jar"]
