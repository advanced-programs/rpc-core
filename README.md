
# RPC服务核心框架 
 
> 基于Netty框架实现RPC服务。

## 查看服务器网络流量

`dstat -nf`

## 测试结果

### 测试环境

> 4核CentOS服务器，一个服务端，两个客户端，客户端和服务端线程数都是32，客户端每次请求数据大小为9119个字节。

### 服务端结果

> 服务端接收数据量可达115M/s，接收请求次数为11600次/秒，即QPS为11600。

### 如果客户端和服务端都在一个局域网，并且没有防火墙

> 客户端请求数据量可达50M/s~60M/s。

### 如果客户端和服务端都在一个局域网，并且有防火墙

> 客户端请求数据量可达10M/s左右。

## 注意事项

> 这里的RPC方式最多传输10240个字节。

> 局域网机器之间网络链接要正常，我在公司内部测试的时候，由于防火墙问题导致内部机器链接出现问题。