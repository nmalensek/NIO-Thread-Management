all: compile
	@echo -e '[INFO] Done!'
clean:
	@echo -e '[INFO] Cleaning Up..'
	@-rm -rf cs455/**/**/*.class
	@-rm -rf cs455/**/**/**/*.class

compile:
	@echo -e '[INFO] Compiling the Source..'
	@javac -d . cs455/**/**/*.java
	@javac -d . cs455/**/**/**/*.java
