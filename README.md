# Taxi Service
This web app designed to simulate simple taxi service app.
Using this app you can:
* track all available cars and working drivers
* register new drivers
* add new cars and their manufacturers
* delete cars, drivers and manufacturers
* add drivers to cars
* track your cars as a driver

### Implementation details
Project based on famous 3-layered architecture:
1. Data access layer (DAO)
1. Application layer (service)
1. Presentation layer (controllers)

### Technologies:
* Apache Tomcat (v9.0.50)
* MySQL
* JDBC
* Servlet
* JSP
* JSTL
* HTML, CSS
* Maven
* Maven Checkstyle Plugin

### Recommendation for setup
1. Configure Apache Tomcat for your IDE
1. Install MySQL and MySQL Workbench
1. Create a schema by using the script from resources/init_db.sql in MySQL Workbench
1. In the /util/ConnectionUtil.java change the URL, USERNAME and PASSWORD properties to the ones you specified when installing MySQL or you can use the ones that are already present
1. After starting tomcat go to http://localhost:your port that you specified while configuring tomcat, click on "Register" to add a new driver.
1. Also, if you want to use logger change full path in log4j.xml file 
