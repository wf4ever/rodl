Introduction
============

This is the Research Object Digital Library. The main services provided by the system 
are the  Research Object Storage and Retrieval Service (ROSRS), which allows users to 
store and retrieve Research Objects, and the Research Object Evolution Service, which 
allows users to create snapshots and releases of their Research Objects.

Documentation
=============

Brief project documentation: http://wf4ever-project.org/wiki/display/docs/Research+Objects+Digital+Library+%28including+the+ROSRS%29.

The ROSR API specification: http://wf4ever-project.org/wiki/display/docs/RO+SRS+interface+6.

The RO Evolution API specification: http://wf4ever-project.org/wiki/display/docs/RO+evolution+API.


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


Authors
=======

Raúl Palma, Piotr Hołubowicz, Filip Wiśniewski
Poznań Supercomputing and Networking Center, http://www.psnc.pl