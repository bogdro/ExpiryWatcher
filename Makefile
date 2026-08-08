# ExpiryWatcher Makefile
#
# Copyright (C) 2026 Bogdan 'bogdro' Drozdowski, bogdro (at) users . sourceforge . net
#
# This file is part of ExpiryWatcher, an application for monitoring expiry dates.
#
# This program is free software; you can redistribute it and/or
#  modify it under the terms of the GNU General Public License
#  as published by the Free Software Foundation; either version 3
#  of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
#  along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

NAME = ExpiryWatcher

RMDIR = /bin/rm -fr
# when using '-p', no error is generated when the directory exists
MKDIR = /bin/mkdir -p
COPY = /bin/cp -pRf
GREP = /bin/grep
PERL = /usr/bin/perl

VER = $(shell $(GREP) versionName app/build.gradle |$(PERL) -pe 's#\s*versionName\s+"([^"]+)".*#$$1#')

# Use the GNU tar format
# ifneq ($(shell tar --version | grep -i bsd),)
# PACK1_GNUOPTS = --format gnutar
# endif
PACK = /bin/tar $(PACK1_GNUOPTS) -vzcf
PACK_EXT = tar.gz

#PACK2 = /usr/bin/gzip -9
#PACK2_EXT = .gz

GRADLE = ./gradlew

SUBDIRS = app gradle
BUILD_FILES = Makefile build.gradle gradle.properties gradlew gradlew.bat \
	settings.gradle
BUILD_FILES_APP = app/build.gradle app/proguard-rules.pro
BUILD_OUTPUT = app/build/outputs/apk

EXTRA_DIST = AUTHORS ChangeLog COPYING keystore.properties.example \
	local.properties INSTALL-*.txt NEWS README.md $(BUILD_FILES)

FILE_ARCH_SRC = $(NAME)-$(VER)-src.$(PACK_EXT)

all:	pack

dist:	pack clean $(FILE_ARCH_SRC)

$(FILE_ARCH_SRC): $(EXTRA_DIST) \
		$(shell find $(SUBDIRS) -type f)
	$(RMDIR) $(NAME)-$(VER)
	$(MKDIR) $(NAME)-$(VER)
	$(COPY) $(EXTRA_DIST) $(SUBDIRS) $(NAME)-$(VER)
	$(PACK) $(FILE_ARCH_SRC) $(NAME)-$(VER)
	$(RMDIR) $(NAME)-$(VER)

pack:	pack-debug pack-release
pack-debug: $(BUILD_OUTPUT)/debug/$(NAME)-debug.apk
pack-release: $(BUILD_OUTPUT)/release/$(NAME)-release.apk

$(BUILD_OUTPUT)/release/$(NAME)-release.apk: $(shell find app/src -type f) \
	$(BUILD_FILES) $(BUILD_FILES_APP)
	$(GRADLE) assembleRelease
	$(COPY) $@ $(NAME)-$(VER)-release.apk

$(BUILD_OUTPUT)/debug/$(NAME)-debug.apk: $(shell find app/src -type f) \
	$(BUILD_FILES) $(BUILD_FILES_APP)
	$(GRADLE) assembleDebug
	$(COPY) $@ $(NAME)-$(VER)-debug.apk

clean:
	$(GRADLE) clean

test:
	$(GRADLE) clean test

.PHONY: all clean dist pack pack-debug pack-release test
