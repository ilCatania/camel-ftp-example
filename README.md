camel-ftp-example
=================

An example FTP upload/download component developed with Apache Camel.

The download component polls the FTP folders at regular intervals and
downloads all files found into a local folder.

The upload component polls a local folder and uploads all files found
into an FTP folder.

In case of connectivity loss, the download process will stop consuming
until the connectivity is resumed, and a message file will be created in
the "down events" folder. When the connectivity resumes (i.e. a connection
attempt by the download process is successful), another message file
will be created in the "up events" folder.

During downtime, no uploads will be attempted.
