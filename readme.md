# abn-recipes

# Getting Started

Java Spring Boot application designed to accomplish the management of recipes.
Allowing the user to add, update, remove and fetch existing recipes.

More detail API documentation can be found [here](http://localhost:8080/swagger-ui/index.html#/)

Running the application:

```sh
docker build abn-recipes .
docker-compose up
```

The application is available at http://localhost:8080/api/v1/recipe

### Running tests:
Via maven:

```sh
mvn clean test
```

### Requirement
In order to successfully run this sample app you need a few things:

- Java 17
- Docker 🐳
- Docker compose

### Stack
- Java 17
- Spring boot 2.7
- Mongo
- Junit5
- Spring test

### Basic initial add recipe json

```json
{
  "name" : "10-minute couscous salad",
  "instructions" : "Tip the couscous into a large bowl and pour over the stock. Cover, then leave for 10 mins until fluffy and all the stock has been absorbed. Meanwhile, slice the onions and pepper, and dice the cucumber. Add these to the couscous, fork through the pesto, crumble in the feta, then sprinkle over pine nuts to serve.",
  "category" : "VEGETARIAN",
  "servings" : 5,
  "ingredients" : ["100g couscous", "2 spring onions","200ml hot low salt vegetable stock (from a cube is fine)" , "1 red pepper", "½ cucumber", "50g feta cheese, cubed", "2tbsp pesto", "2tbsp pine nuts"]
}
```

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.7.2/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.7.2/maven-plugin/reference/html/#build-image)
* [Spring Web](https://docs.spring.io/spring-boot/docs/2.7.2/reference/htmlsingle/#web)
* [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/2.7.2/reference/htmlsingle/#actuator)
