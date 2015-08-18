InterClient
===========
That is the client for old InterBase. It was originated from [FireBird
repository][firebird], but now is supported here separately.

Currently only Java connector is supported.

Building
--------

    cd packages
    ./gradlew build

Please check `README.legacy` if you're interested in building any other parts of
the package.

Usage
-----
Add the project repository to your repository list (this is a sample for maven
`pom.xml`):

    <repositories>
        <repository>
            <id>bintray.fornever</id>
            <name>fornever / maven</name>
            <url>https://dl.bintray.com/fornever/maven</url>
        </repository>
    </repositories>

And import the artifacts to your project. For example, add the following to your
`<dependencies>` in `pom.xml`:

    <dependency>
        <groupId>interbase</groupId>
        <artifactId>interclient</artifactId>
        <version>2.01</version>
    </dependency>

The 2.01 version was imported from the Linux binaries published at
https://sourceforge.net/projects/firebird/files/OldFiles/interclient_201_linux-xinetd.tar.gz

Currently it is stored in the `deploy` directory with the corresponding
`interclient-2.01.pom` file that was not published by Firebird developers and
was written specially for this project.

License
-------
All the code is distributed under the terms of [InterBase Public License][ipl].

[firebird]: http://firebird.cvs.sourceforge.net/viewvc/firebird/interclient/20/dev/
[ipl]: http://www.firebirdsql.org/en/interbase-public-license/
