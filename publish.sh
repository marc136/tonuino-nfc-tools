#!/usr/bin/env bash

if [[ $# -ne 1 ]]; then
	echo "Run this with a play store track (e.g. alpha or beta)"
	echo "Example"
	echo " $0 beta"
	exit 1
fi

export PLAY_STORE_TRACK="$1"

echo "Publish a build to the Google Play Store Track '$PLAY_STORE_TRACK'"

source fastlane/CONFIG

bundle exec fastlane publish
