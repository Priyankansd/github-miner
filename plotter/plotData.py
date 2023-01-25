import couchdb
import time
import plotly.express as px

DB_HOST = "localhost"
DB_PORT = 5984
DB_USER = "admin"
DB_PASS = "admin"
DB_TABLE = "github-miner"
REFRESH_TIME = 30

class CouchDBHandler:

    def __init__(self, host, port, table):
        self.db = couchdb.Server("http://{0}:{1}@{2}:{3}".format(
            DB_USER,
            DB_PASS,
            host,
            port))
        self.tableData = self.db[table]

    def refresh_data(self):
        self.tableData = self.db[table]

    def getWords(self):
        self.words = [ id for id in self.tableData ]
        return self.words

    def getCounts(self):
        self.counts = [ self.tableData[w]['count'] for w in self.words]
        return self.counts

    def getWordCountMap(self):
        tuples = [(key, value) for i, (key, value) in 
                  enumerate(zip(self.getWords(), self.getCounts()))]
        wordCountDict = dict(tuples)
        orderedDict = {k: v for k, v in sorted(wordCountDict.items(), key=lambda item:
                                               -1 * item[1])}
        return orderedDict

def refresh_graph():
    dbHandler = CouchDBHandler(DB_HOST, DB_PORT, DB_TABLE)
    wordCountMap = dbHandler.getWordCountMap()
    print("wordCount: {}".format(wordCountMap))
    fig = px.bar(x=wordCountMap.keys(), y=wordCountMap.values())
    # fig.show()
    fig.write_html('/var/www/html/index.html')
       

def main():
    while True:
        try:
            refresh_graph()
            print("Plotted graph. Will refresh in 30 seconds")
        except:
            print("Failed to plot. Refresh after 30 seconds")
        time.sleep(REFRESH_TIME)


if __name__ == "__main__":
    main()

