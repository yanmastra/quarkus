#!/bin/zsh

jps -l | grep ${1:-unknown-svc} | cut -d" " -f1 | xargs kill -9