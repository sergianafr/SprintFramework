# SprintFramework
Projet création framework web dynamique

Java version: "17.0.8" 2023-07-18 LTS

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

- Add the packages containing you controllers to the context-param

        <context-param>
            <param-name>Controllers</param-name> 
            <param-value>src.controller</param-value>
        </context-param>

## Annotations
- Annotate your controllers classes with src.annotations.Controller
- Annotate your controller methods with src.annotations.Get(url="method's url").