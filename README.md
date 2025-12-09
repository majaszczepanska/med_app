commend:
- run
docker run --name medapp_container -e POSTGRES_PASSWORD=admin -e POSTGRES_DB=medapp_db -p 5432:5432 -d postgres
- stop/remove container
docker rm -f medapp_container



unused dependencies:
		<dependency>
			<groupId>com.h2database</groupId>
			<artifactId>h2</artifactId>
			<scope>runtime</scope>
		</dependency>


used:
- for api
		<dependency>
    		<groupId>org.springdoc</groupId>
    		<artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    		<version>2.2.0</version>
		</dependency>