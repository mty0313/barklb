# Bark Load Balancer

## 介绍

[Bark推送服务](https://github.com/Finb/bark-server)通常来说是单节点运行的, `BarkLB`可以通过配置多个服务器地址尽可能保证推送通知送达.

## 快速开始

需配合已有bark原版服务器使用, 并确保barklb可以访问原版bark服务.

通过Docker运行: 

```shell
docker run -d --restart=always \
  --name barklb \
  -p 18089:8089 \
  -e BARK_NODES="https://bark-origin-server1:port,https://bark-origin-server2:port"
  -v ${HOME}/barklb-data:/barklb-data \
  martin0313/barklb:latest
```

启动成功后在bark app中添加服务器, 地址为barklb运行的地址和端口. 若服务端添加成功推送key会显示为`LB-xxxxxxxx`.

随后其余使用方式与原版bark无异.

    BARK_NODES 参数指定了两个原版bark服务节点. barklb会在两个节点中随机选择节点发送通知. 若节点下线则尝试采用其他节点. 

    可以添加配置`https://api.day.app`, 这是bark官方提供的服务器地址.

## 功能与特性

### 目前仅对接了原版bark服务端的post body推送形式

1. 负载均衡策略为简单的随机策略, 主要解决的问题为尽可能保证高可用.
2. 健康检查会及时更新在线服务器列表.
3. 一个推送key: `LB-xxxxxxx` 即可使用多个bark服务后端. 原版服务器的注册,删除,配置变更等均由`barklb`自动操作.
4. bark服务节点配置变更: 修改启动参数`BARK_NODES`, 服务端重启即可, 手机app无需重新注册和其他额外操作.

## 链接

Github: `https://github.com/mty0313/barklb`

DockerHub: `https://hub.docker.com/r/martin0313/barklb`
