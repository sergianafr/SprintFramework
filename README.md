# SprintFramework
Projet création framework web dynamique

Java version: "19.0.8" 2023-07-18 LTS

Tomcat version: Tomcat 10.1

## Configuration:
- You must add this following configuration to your project's web.xml :

        <servlet>
            <servlet-name>FrontController</servlet-name>
            <servlet-class>
                src.mg.itu.prom16.FrontController
            </servlet-class>
        </servlet>
        <servlet-mapping>
            <servlet-name>FrontController</servlet-name>
            <url-pattern>/</url-pattern>
        </servlet-mapping>

- Add the packages containing your controllers to the context-param

        <context-param>
            <param-name>Controllers</param-name> 
            <param-value>src.controller</param-value>
        </context-param>

- Add the session keys containing the boolean Authenticated and the role of the user 

        <context-param>
            <param-name>session_authenticated</param-name> 
            <param-value>authenticated</param-value>
        </context-param>
        <context-param>
            <param-name>session_role</param-name> 
            <param-value>role</param-value>
        </context-param>

## Annotations
### Controller Annotations:
- The class should be annotated with: @Controller
- Endpoint methods:
    - annotated with @Endpoint(url=*the url redirecting to the method*)
    - Method get: @Get
    - Method post: @Post
    - Restapi method: @Restapi
    - all the parameters of the methods must be annotated with @Param(name=*Parameter's name*),
        the name must correspond to the name from a form input from where you retrieve its value
    - Files must be annotated with @File annotation
- To control methods and controllers access, use the following annotations:
    - @Public to allow everyone
    - @Authenticated({role1, role2...}) to limit by roles 

## Session
To manipulate sessions, you must import the package src.classes.CustomSession
    Method void add(String name, Object value) adds value to the session
    Method void remove(String name) removes value from the session
    Method Object get(String name) return the value corresponding to the given name from the session