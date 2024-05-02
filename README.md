java -jar -DPORT=2551 -DMPORT=5551 -DROLE=director  target/akka-1.0-SNAPSHOT-allinone.jar
java -jar -DPORT=2552 -DMPORT=5552 -DROLE=worker  target/akka-1.0-SNAPSHOT-allinone.jar
java -jar -DPORT=2553 -DMPORT=5553 -DROLE=worker  target/akka-1.0-SNAPSHOT-allinone.jar
