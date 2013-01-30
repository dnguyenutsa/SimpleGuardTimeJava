data.txt - A text file for timestamping.
	Since text files have no provisions for embedded metadata, the timestamps
	are stored in separate files. In a practical integration secnario, it would
	be up to the host application to ensure they do not get lost.
	Note that for the sample timestamps to verify, this file MUST end with a
	single DOS-style end-of-line (CR+LF pair).

data.txt.gtts1 - An unextended timestamp for data.txt.

data.txt.gtts2 - An extended timestamp for data.txt.

ExampleCreate.java - Source code for command-line utility to create timestamps.

ExampleExtend.java - Source code for command-line utility to extend timestamps.

ExampleVerify.java - Source code for command-line utility to verify timestamps.

overview.html - Manual for command line utilities.
