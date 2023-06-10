# Shitboy-Spring-CQ

[Shitboy(Mirai-Console)](https://github.com/Lawaxi/Shitboy)

[Spring-CQ](https://github.com/lz1998/Spring-CQ)

[gocq-http](https://github.com/Mrs4s/go-cqhttp)

mirai无法发送群消息（code45造成只能手表登录）时暂时性填补用，配合gocq-http

使用异步wb通讯实现，不能在第一时间读取机器人并初始化，需要管理员输入load

- 如果您遇到同样问题，需要使用本项目，需修改下列内容再编译以适配
  - 管理员QQ号com.example.demo.plugin.ShitBoyPlugin
  - 端口application.yml
  - 配置net.lawaxi.util.ConfigOperator