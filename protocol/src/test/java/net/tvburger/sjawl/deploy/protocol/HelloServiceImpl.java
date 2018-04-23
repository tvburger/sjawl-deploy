package net.tvburger.sjawl.deploy.protocol;

public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello() {
        return "hello world!";
    }

    @Override
    public String sayHello(String name) {
        return "Hello, " + name + "!";
    }

}
