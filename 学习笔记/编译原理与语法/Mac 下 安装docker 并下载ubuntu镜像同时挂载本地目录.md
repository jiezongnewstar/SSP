1.把Mac里的目录映射到Docker下ubuntu容器里,这样就可以在Ubuntu容器里操作Mac上的文件
# docker run -it -v /Users/xxx/working:/home ubuntu:14.04