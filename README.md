# github-miner


Run the application with following commands

## 1. Build the docker
```
docker build -t github-miner .
```

## 2. Start the docker container
```
./docker_run.sh
```

## 3. Access webpage for plotted graph

It'll show docker run and logs on the console.
You can access the web-server to see the plotted graph on
```
http://localhost:8081/
```

## 4. Access couchDB for debugging
CouchDB can be viewed from container host at
```
http://localhost:5984/
```
Management console for couchDB at
```
http://localhost:5984/_utils
```
Default username and password is `admin/admin`
