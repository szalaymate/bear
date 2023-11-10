# Bear: error handling experiments

This is a small webserver that serves an image of a **bear** built from three different **member**s:
* head
* body
* leg

Each *member* can be loaded from three different resources:
* working directory of java process
* a configured directory
* java resource packaged into running jar

Search order is respective to the list above.
