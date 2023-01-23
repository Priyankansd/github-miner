#!/bin/bash -ex

service couchdb start 
service apache2 start
java -cp target/github-miner-1.0-SNAPSHOT-jar-with-dependencies.jar org.githubminer.app.MinerComponent &
MINER_PID=$i
cd /github-miner/plotter
python3 -m venv .
source env/bin/activate
pip install -r requirements.txt
python3 plotData.py
cd /github-miner

wait $MINER_PID
