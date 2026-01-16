# HMCTS Dev Test Backend
This will be the backend for the brand new HMCTS case management system. As a potential candidate we are leaving
this in your hands. Please refer to the brief for the complete list of tasks! Complete as much as you can and be
as creative as you want.

You should be able to run `./gradlew build` to start with to ensure it builds successfully. Then from that you
can run the service in IntelliJ (or your IDE of choice) or however you normally would.

There is an example endpoint provided to retrieve an example of a case. You are free to add/remove fields as you
wish.

## Creative Effort
Initial created TaskTest and model class through a TDD approach, then adding Spring MVC with Eclipse Store.
Spring typically drives one to use JPA for persistence, but I choose to use Eclipse Store, which might be
more suitable for small Serverless backend services, which I think this Task backend application likely
would be.

![JPA versus Eclipse Store](img/jpa_vs_eclipsestore.webp)

That proofed to be a learning curve, especially with testing and combination with Spring Boot.
With the Spring annotations and persistence requirements for Eclipse Store framework, the TDD approach
changed to Behavioural Driven Tests and a separation of types with Spring annotations in a package.
I have added a "life cycle" (Create, Retrieve, Update, Delete) functional test, using RestAssured, which
was new to me.

Then focussed on documentation and had to make a change in the sample framework to get my Controller classes
scanned by Swagger, by setting a property in application.yaml.
I also added an Eclipse Store property that specifies the directory for the data repository.

To illustrate that Eclipse Store offers a wider range of repository types is in this
[screenshot](img/eclipsestore-targets.png) from the Eclipse Store reference manual.


