---
title: First PPMP-Validator Online
date: 2017-06-29 00:00:00
tags: "validator"
---
Our first PPMP-Validator as a HTTP-server is online! 

What we already contribtuted as sourcecode to the Unide-project we've also installed on an eclipse sandbox server. Now you are able to validate your PPMP-messages through the internet without running the server on your local machine. The intention is to make the latest specification validator accessible to everyone at anytime. 

You can reach the server by sending POST-requests to the following endpoints:

<pre>
<a>http://unide.eclipse.org/rest/v2/message?validate=true</a>
<a>http://unide.eclipse.org/rest/v2/measurement?validate=true</a>
<a>http://unide.eclipse.org/rest/v2/process?validate=true</a>
</pre>

Further functions of the sandbox server will be also visualization and storaging of incoming PPMP-messages.
