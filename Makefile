all: RSSI_graphApp.class

clean:
	rm -f $@
	rm -f *.class
	rm -f *Msg.java
	echo "Clean done"

RssiMsg.class: RssiMsg.java
	javac $<

PingMsg.class: PingMsg.java
	javac $<

MultiPingMsg.class: MultiPingMsg.java
	javac $<

MultiPingResponseMsg.class: MultiPingResponseMsg.java
	javac $<

MultiPingResponseReportMsg.class: MultiPingResponseReportMsg.java
	javac $<

MultiPingResponseTinyReportMsg.class: MultiPingResponseTinyReportMsg.java
	javac $<

CommandMsg.class: CommandMsg.java
	javac $<
	
RSSI_graphApp.class: RssiMsg.java PingMsg.java MultiPingMsg.java \
	MultiPingResponseMsg.java MultiPingResponseReportMsg.java \
	MultiPingResponseTinyReportMsg.java \
	CommandMsg.java
	javac $^

RssiMsg.java: nesC/RssiDemoMessages.h
	mig java -target=null -java-classname=RssiMsg $< RssiMsg -o $@

PingMsg.java: nesC/RssiDemoMessages.h
	mig java -target=null -java-classname=PingMsg $< PingMsg -o $@

MultiPingMsg.java: nesC/RssiDemoMessages.h
	mig java -target=null -java-classname=MultiPingMsg $< MultiPingMsg -o $@

MultiPingResponseMsg.java: nesC/RssiDemoMessages.h
	mig java -target=null -java-classname=MultiPingResponseMsg $< MultiPingResponseMsg -o $@

MultiPingResponseReportMsg.java: nesC/RssiDemoMessages.h
	mig java -target=null -java-classname=MultiPingResponseReportMsg $< MultiPingResponseReportMsg -o $@

MultiPingResponseTinyReportMsg.java: nesC/RssiDemoMessages.h
	mig java -target=null -java-classname=MultiPingResponseTinyReportMsg $< MultiPingResponseTinyReportMsg -o $@

CommandMsg.java: nesC/RssiDemoMessages.h
	mig java -target=null -java-classname=CommandMsg $< CommandMsg -o $@


#mig java -I $APPDIR -target=mica2 -java-classname=net.tinyos.ident.IdentMsg $APPDIR/App.nc IdentMsg -o IdentMsg.java

