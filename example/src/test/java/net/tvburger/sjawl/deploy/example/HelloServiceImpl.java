package net.tvburger.sjawl.deploy.example;

public class HelloServiceImpl implements HelloService {

    private final String template;

    public HelloServiceImpl(String template) {
        this.template = template;
    }

    @Override
    public String sayHelloTo(String name) {
        return String.format(template, name);
    }

    @Override
    public String toString() {
        return "HelloService(" + template + ")";
    }

}
