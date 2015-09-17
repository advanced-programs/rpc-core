

## 创建接口 com.hello.Foo

		public interface Foo{
			String sayHello(String bar);
		}
		

## 在server端创建实现org.hello.FooImpl

		public class FooImpl implements Foo{
			public String sayHello(String bar){
				return "Hello " + bar;
			}
		}

## 在类路径下创建文件conf.properties，并且添加一行

	com.hello.Foo=org.hello.FooImpl

## 然后启动server

		new Server().start(9090);

## 启动client并且调用Foo.sayHello

		Client client = new Client("127.0.0.1", 9090);
		Foo foo = client.createProxy(Foo.class);
		System.out.println(foo.sayHello("bar"));
	
> 如果客户端打印出Hello bar,Congratulations.