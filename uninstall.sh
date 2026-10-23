#!/usr/bin/env bash
IP="${1:-192.168.50.231}"; DEV="$IP:5555"
adb connect "$DEV"
adb -s "$DEV" shell settings put secure enabled_accessibility_services null
adb -s "$DEV" uninstall com.hector.cleanlauncher
