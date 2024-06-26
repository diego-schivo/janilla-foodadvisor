# Janilla FoodAdvisor

This is a (partial) porting of [Strapi FoodAdvisor Demo](https://github.com/strapi/foodadvisor).

### How you can get started

> **_Note:_**  if you are unfamiliar with the terminal, you can set up the project in an IDE (section below).

Make sure you have Java SE Platform (JDK 21) and [Apache Maven](https://maven.apache.org/install.html) installed.

From the project root, run the following command to run the application:

```shell
mvn compile exec:java -pl api
```

```shell
mvn compile exec:java -pl client
```

Then open a browser and navigate to <http://localhost:3000/> (the API port is 1337).

> **_Note:_**  consider checking the Disable Cache checkbox in the Network tab of the Web Developer Tools.

### Set up the project in an IDE

[Step-by-step Video Tutorial](https://youtu.be/yx6-zZT5IuQ) available on our [YouTube Channel](https://www.youtube.com/@janilla).

So far the project has been developed with [Eclipse IDE](https://eclipseide.org/):

1. download the [Eclipse Installer](https://www.eclipse.org/downloads/packages/installer)
2. install the package for Enterprise Java and Web Developers with JRE 21
3. launch the IDE and choose Import projects from Git (with smart import)
4. select GitHub as the repository source, then search for `janilla-foodadvisor` and complete the wizard
5. select the project and launch Debug as Java Application
6. open a browser and navigate to <http://localhost:3000/> (the API port is 1337)

### Where you can get help

Please visit [www.janilla.com](https://janilla.com/) for more information.

You can use [GitHub Issues](https://github.com/diego-schivo/janilla-foodadvisor/issues) to give or receive feedback.
