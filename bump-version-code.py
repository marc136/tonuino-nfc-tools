#!/usr/bin/env python3

# The play store requires each build to have a unique `versionCode`.
# This script increases it by one.

import re
import fileinput

for n, line in enumerate(fileinput.input('app/build.gradle', inplace=True), start=1):
    m = re.match(r'(\s*versionCode)\s+(\d+)', line)
    if m:
        print(m.group(1), int(m.group(2))+1)
    else:
        print(line, end = '')

	
