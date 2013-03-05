Introduction
============

This is the Research Object Digital Library. The main service of the library is the Research Object Storage and Retrieval Service (ROSRS), which 
allows users to store and retrieve Research Objects. Users can also create snapshots and releases of their Research Objects using the 
Research Object Evolution Service.

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
	a. dlibra=[true|false] use this variable to indicate whether you want a standard installation
	using dLibra as storage backend or a lightweight version using the server's filesystem. 
	The following properties (host, port, workspacesDir, collectionId) can be left empty if
	dLibra is not used.
	b. host=example.org the host at which dLibra is running 
	c. port=10051 the port on which dLibra is running
	d. workspacesDir=3 the directory in the dLibra registry which stores the Research Objects
	e. collectionId=4 the collection in dLibra to which the published Research Objects will be assigned
	f. filesystemBase=/tmp/dl the absolute path to the folder in which the data will be stored
	g. store.directory=/tmp/tdb the absolute path to the folder in which the metadata will be stored
3. Edit the rosrs/WEB-INF/classes/hibernate.cfg.xml to point to a database for RODL.
4. Create a file rosrs/WEB-INF/classes/profiles.properties:
	a. Create a property adminToken which should contain the MD5 checksum of the admin OAuth Bearer token.
	b. If dLibra is used, create a property adminUser which should contain the admin user in dLibra.
	c. If dLibra is used, create a property adminPassword which should contain the admin password in dLibra (not encrypted).
5. Reload the application in the servlet container to load the configuration changes.


Building the digital library
============================

1. Download the project source code from git://github.com/wf4ever/rosrs.git
2. Run `mvn package`.


Authors
=======

Raúl Palma, Piotr Hołubowicz, Filip Wiśniewski
Poznań Supercomputing and Networking Center, http://www.psnc.pl