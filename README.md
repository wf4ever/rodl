[![Build Status](https://buildhive.cloudbees.com/job/wf4ever/job/rodl/badge/icon)](https://buildhive.cloudbees.com/job/wf4ever/job/rodl/)

Introduction
============

This is the Research Object Digital Library. The main services provided by the system 
are the  Research Object Storage and Retrieval Service (ROSRS), which allows users to 
store and retrieve Research Objects, and the Research Object Evolution Service, which 
allows users to create snapshots and releases of their Research Objects.

Documentation
=============

The key RODL paper for citation purposes is:
Palma, R., Corcho, O., Hotubowicz, P., Pérez, S., Page, K., and Mazurek, C. 2013. Digital libraries for the preservation of research methods and associated artifacts. In Proceedings of the 1st International Workshop on Digital Preservation of Research Methods and Artifacts (DPRMA '13). ACM, New York, NY, USA, 8-15. DOI=10.1145/2499583.2499589 http://doi.acm.org/10.1145/2499583.2499589

Brief project documentation: http://wf4ever-project.org/wiki/display/docs/Research+Objects+Digital+Library+%28including+the+ROSRS%29.

A detailed information about the APIs implemented by RODL is available at:
De Roure, D., Hotubowicz, P., Page, P., Palma, R. 2013. D1.3v2 Wf4Ever Architecture – Phase II. Wf4Ever project Deliverable. Available at
Also available online at: http://www.wf4ever-project.org/wiki/display/docs/Wf4Ever+service+APIs

Installation
============

You can either download a .war file from http://maven.man.poznan.pl/repository/repo/pl/psnc/dl/wf4ever/rosrs/ 
or you can download the source code and build the digital library yourself (see below). In any case, the deployment
and configuration procedure is the same.

1. Deploy the .war file to a servlet container, such as Apache Tomcat.
2. Edit the rosrs/WEB-INF/classes/connection.properties file:
	1. dlibra=[true|false] use this variable to indicate whether you want a standard installation
	using dLibra as storage backend or a lightweight version using the server's filesystem. 
	The following properties (host, port, workspacesDir, collectionId) can be left empty if
	dLibra is not used.
	2. host=example.org the host at which dLibra is running 
	3. port=10051 the port on which dLibra is running
	4. workspacesDir=3 the directory in the dLibra registry which stores the Research Objects
	5. collectionId=4 the collection in dLibra to which the published Research Objects will be assigned
	6. filesystemBase=/tmp/dl the absolute path to the folder in which the data will be stored
	7. store.directory=/tmp/tdb the absolute path to the folder in which the metadata will be stored
3. Edit the rosrs/WEB-INF/classes/hibernate.cfg.xml to point to a database for RODL.
4. Create a file rosrs/WEB-INF/classes/profiles.properties:
	1. Create a property adminToken which should contain the MD5 checksum of the admin OAuth Bearer token.
	2. In the standard installation, create a property adminUser which should contain the admin user in dLibra.
	3. In the standard installation, create a property adminPassword which should contain the admin password in dLibra (not encrypted).
5. Reload the application in the servlet container to load the configuration changes.


Building the digital library
============================

1. Download the project source code from git://github.com/wf4ever/rosrs.git
2. Run `mvn package`.


Testing the digital library
===========================

If you want to run RODL from the source on your local machine, run `mvn jetty:run`. RODL will be available at http://localhost:8080.
If you want to change the default port, for example to 9999, run `mvn -Djetty.port=9999 jetty:run`. 

The 'test' phase of the maven build runs only unit tests. The 'integration-tests' phase runs tests of the REST API. For this to work,
you must use the following maven properties:
* jersey.test.containerFactory=com.sun.jersey.test.framework.spi.container.external.ExternalTestContainerFactory
* jersey.test.port=8082
* jersey.test.host=localhost
* jetty.port=8082

The 2nd and 4th properties must point to the same port. The last property is not necessary if the port is 8080.  


Authors
=======

Raúl Palma, Piotr Hołubowicz, Filip Wiśniewski

Poznań Supercomputing and Networking Center, http://www.psnc.pl

Support
=======
If you have problems or questions related to this software, please create an issue at Jira (https://jira.man.poznan.pl/jira/browse/WFE) 
and/or contact RODL support email (rpalma@man.poznan.pl).
