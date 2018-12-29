#!/bin/bash
sudo rm /opt/lights-controller/*.log
sudo java -Dtinylog.configuration=/opt/lights-controller/tinylog.properties -jar lights-controller-0.0.1.jar