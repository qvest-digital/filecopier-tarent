#!/bin/sh
for BUNDLE in src/main/resources/ch/fhnw/filecopier/i18n/Strings*
do
	sort ${BUNDLE} > tmp
	mv tmp ${BUNDLE}
done
