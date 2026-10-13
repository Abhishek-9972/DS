# CREATIONAL

## SINGLETON 
Singleton design pattern defines a class that has only one instance and provide global access to it.

## FACTORY
A Factory Pattern or Factory Method Pattern says that just define an interface or abstract class for creating an object but let the subclasses decide which class to instantiate.
- Shape

## BUILDER
Builder is a creational design pattern that lets you construct complex objects step by step

# STRUCTURAL

The patterns include Adapter, which allows incompatible interfaces to work together; Bridge, decoupling abstraction from implementation; Composite, treating objects and compositions uniformly;
Decorator, adding functionality to objects dynamically; Facade, providing a unified interface to a complex subsystem; Flyweight, conserving memory by sharing objects; and Proxy, controlling access to objects.

## ADAPTER
An Adapter Pattern says that just "converts the interface of a class into another interface that a client wants".
- Student

## FLYWEIGHT
A Flyweight Pattern says that just "to reuse already existing similar kind of objects by storing them and create new object when no matching object is found".
- Map<String,Employee>

## DECORATOR
A Decorator Pattern says that just "attach a flexible additional responsibilities to an object dynamically".
- Coffee



## FACADE
A Facade Pattern says that just "just provide a unified and simplified interface to a set of interfaces in a subsystem, therefore it hides the complexities of the subsystem from the client".
- MobileShop

## PROXY
According to GoF, a Proxy Pattern "provides the control for accessing the original object".
- Database Executor

# BEHAVIORAL

##OBSERVER
An Observer Pattern says that "just define a one-to-one dependency so that when one object changes state, all its dependents are notified and updated automatically".
- Producer Subject Consumer Observers

##STRATEGY
It allows you to dynamically change the behavior of an object by encapsulating it into different strategies
- SocialMediaStrategy - FacebookStrategy