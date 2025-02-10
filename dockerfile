FROM amazoncorretto:17-alpine-jdk

# 创建目录
RUN mkdir /barklb
RUN mkdir /barklb-data
WORKDIR /barklb

# 复制 Spring Boot JAR 文件和配置文件到容器中
COPY ./target/barklb-1.0.0.jar /barklb

# 暴露端口
EXPOSE 8081

ENV BARK_NODES https://api.day.app

# 设置启动命令
CMD ["java", "-Xmx512m", "-Xms512m", "-Duser.timezone=GMT+08", "-Duser.dir=/barklb-data", "-jar", "/barklb/barklb-1.0.0.jar", \
 "--bark.remote.urls=${BARK_NODES}" \
]
