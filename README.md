# sjawl-deploy
Standard Java Deployment Library

## Introduction

When your code is in need for Service Discovery, or needs Worker Coordination, this library helps you out.

## Service Discovery

- Obtain a DeploymentContext
- Register your service type
- Register your service implementation
- Obtain the service from the client code
- Clean up the services of a specific type if needed

```
    // Obtain a DeploymentContext
    DeploymentContext context = LocalDeploymentContext.Factory.create();
        
    // Register your service type
    ServicesAdministrator administrator = context.getServicesAdministrator();
    administrator.registerServiceType(HelloService.class, DeploymentStrategyProvider.getFirstAvailableStrategy());
    
    // Register your service implementation    
    ServiceRegistry proxyServiceRegistry = context.getServiceRegistry();
    proxyServiceRegistry.registerService(HelloService.class, new EnglishHelloService(), LanguageSpecification.Factory.create("EN"));
    proxyServiceRegistry.registerService(HelloService.class, new FrenchHelloService(), LanguageSpecification.Factory.create("FR"));
    
    // Obtain the service from the client code
    ServiceProvider provider = context.getServiceProvider();
    HelloService service = provider.getService(HelloService.class, LanguageSpecification.Factory.create("FR"));
    service.sayHello();
    
    // Clean up the services of specific type if needed    
    administrator.unregisterServiceType(HelloService.class);

```

## Worker Coordination

- Obtain a DeploymentContext
- Register your type of work
- Deploy workers
- Clean up the workers when needed

```
    // Obtain a DeploymentContext
    DeploymentContext context = DefaultDeploymentContext.Factory.create();
    
    // Register your type of work
    WorkersAdministrator administrator = context.getWorkersAdministrator();
    administrator.registerWorkerType(TestWorker.class, DeploymentStrategyProvider.getActiveActiveStrategy());
    
    // Deploy workers
    MyTestWorkerActivator activator = new TestWorkActivator();
    
    WorkerDeployer deployer = context.getWorkerDeployer();
    deployer.deployWorker(TestWorker.class, new TestWorker(), activator);
    deployer.deployWorker(TestWorker.class, new TestWorker(), activator);
    
    // Clean up the workers if needed
    administrator.unregisterWorkerType(TestWorker.class);
```


## Distributed Services

This contains distributed computing using ZooKeeper. The distributed module defines how you can register
your own Site-to-Site protocol and Datastore technology.

modules/{distributed,protocol,zookeeper}

## More Examples
See: example/src/test/java/net/tvburger/sjawl/deploy/example/ExampleTest.java
See: example/src/test/java/net/tvburger/sjawl/deploy/example/DistributedExampleTest.java


# Contact
tvburger@gmail.com, http://www.tvburger.net, https://github.com/tvburger/sjawl-deploy   