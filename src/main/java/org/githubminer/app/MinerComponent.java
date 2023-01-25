package org.githubminer.app;


import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class MinerComponent {
    final String GITHUB_API_BASE_URL = "https://api.github.com";
    final String REPOSITORIES_ENDPOINT = "/search/repositories";

    Response response;
    static List<String> javaWords;
    List<String> pythonWords;
    final String clientId = "5cb6962d9b716cea5e8e";
    final String secretId = "fc091d5cbc12527618b5ab1ca9bd5c3041799a64";
    final String token = "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";

    private final CouchDbInterface dbInterface;

    // Creates a new instance of the CouchDbInterface class and assigns it to the dbInterface field
    public MinerComponent() {
        this.dbInterface = new CouchDbInterface("localhost", 5984);
    }

    //Method for mining data from the GitHub API using the search function to find repositories written in Python and Java, sorted by stars and pagination
    public void mineGitHub() throws InterruptedException {
        int page = 1;

        while (page < 100) {

            RequestSpecification request = RestAssured.given().auth().preemptive().basic(clientId, secretId).when()
                .param("q", "language:Python,language:Java").param("page", page).param("sort", "stars")
                .param("direction", "desc");
            response = request.get(GITHUB_API_BASE_URL + REPOSITORIES_ENDPOINT);

            if (response.statusCode() != 200) {
                System.out.println(
                        "Error: received non-200 response: " + response.statusCode() + " - " + response.statusLine());
                return;
            }

            JSONObject json = null;
            JSONArray items = null;
            try {
                json = new JSONObject(response.getBody().asString());
                items = json.getJSONArray("items");

                for (int i = 0; i < items.length(); i++) {
                    JSONObject obj = items.getJSONObject(i);
                    String login = obj.getJSONObject("owner").getString("login");
                    userRepos(login);
                }
            } catch (JSONException e) {
                System.out.println("Error parsing json repsonse error: " + e);
                return;
            }

            if (items == null || items.length() == 0) {
                break;
            }
            page++;
        }

    }

    // This code retrieves and processes a user's GitHub repositories
    public void userRepos(String login) throws InterruptedException {
        List<Map<String, ?>> repos = getRepos(login);

        if (repos == null) {
            System.out.println("Error: received a null value for the repository name. Skipping this repository.");
            return;
        }

        for (Map<String, ?> repo : repos) {

            String name = (String) repo.get("name");

            if (name == null) {
                continue;
            }

            String language = repo.get("language") != null ? (String) repo.get("language") : "null";
            Object defaultBranch = repo.get("default_branch");

            try {
                List<Word> words = processRepo(login, name, defaultBranch, language);
                if (words != null) {
                    System.out.println("Adding words to db");
                    this.dbInterface.addWords(words);
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

    }

    // Returns list of repositories of the user.
    private List<Map<String, ?>> getRepos(String login) throws InterruptedException {

        response = RestAssured.given().auth().preemptive().basic(clientId, secretId)
            .get(GITHUB_API_BASE_URL + "/users/" + login + "/repos");

        // check if response status code is not 200
        if (response.statusCode() != 200) {
            System.out.println(
                    "Error: received non-200 response: " + response.statusCode() + " - " + response.statusLine());

            return null;
        }

        List<Map<String, ?>> repos = response.jsonPath().getList("");

        // check if repos is null
        if (repos == null) {
            System.out.println("Error: received a null value for the repository list. Skipping this repository.");

            return null;
        }

        return repos;
    }

    // This code retrieves and processes a specific repository's git tree and code,and return the list of extracted words
    private List<Word> processRepo(String login, String name, Object defaultBranch, String Lang) throws Exception {

        List<Word> words = null;

        // check if Lang is not Python or Java
        if (!Lang.equalsIgnoreCase("Python") && !Lang.equalsIgnoreCase("Java")) {
            System.err.println("Error: received non-Python or non-Java language: " + Lang);

        }

        // make a GET request to the API
        response = RestAssured.given().auth().oauth2(token).get(GITHUB_API_BASE_URL + "/repos/" + login + "/" + name
                + "/git/trees/" + defaultBranch + "?recursive=all");


        // check if response status code is not 200
        if (response.statusCode() != 200) {
            System.err.println("Error: received non-200 response from the processRepo API: " + response.statusCode()
                    + " - " + response.statusLine());
            return null;
        }

        JSONObject object1 = new JSONObject(response.getBody().asString());
        JSONArray treeArray = object1.getJSONArray("tree");

        for (int m = 0; m < treeArray.length(); m++) {

            JSONObject treeObject = treeArray.getJSONObject(m);
            String path = treeObject.getString("path");

            // check if path is null
            if (path.endsWith(".py") || path.endsWith(".java")) {
                response = RestAssured.given().auth().oauth2(token)
                    .get(GITHUB_API_BASE_URL + "/repos/" + login + "/" + name + "/contents/" + path);

                if (response.statusCode() == 200) {
                    JSONObject jsonObject = new JSONObject(response.getBody().asString());
                    String downloadUrl = jsonObject.getString("download_url");
                    String code = getCode(downloadUrl);
                    System.out.println(downloadUrl);
                    if (downloadUrl.endsWith(".py")) {
                        pythonWords = new ArrayList<String>();
                        pythonWords = extractPythonWords(code);
                        words = pythonWords.stream().map(s -> new Word(s, WordType.WORD_PYTHON, name)).collect(Collectors.toList());
                    } else {
                        javaWords = new ArrayList<String>();
                        javaWords = extractJavaMethodNames(code);
                        words = javaWords.stream().map(s -> new Word(s, WordType.WORD_JAVA, name)).collect(Collectors.toList());
                    }

                } else {
                    System.err.println("download: Error: received non-200 response: " + response.statusCode() + " - "
                            + response.statusLine());
                }
            }
        }
        return (words != null)? words : null;
    }

    // This code retrieves the code of a specific repository by making a GET request to the download URL
    private String getCode(String downloadUrl) throws InterruptedException {
        try {
            response = RestAssured.get(downloadUrl);
            // check if response status code is not 200
            if (response.statusCode() != 200) {
                System.err.println("Error: received non-200 response from the getCode API: " + response.statusCode()
                        + " - " + response.statusLine());
                return null;
            }

            String code = response.asString();

            // check if code is null
            if (code == null) {
                System.err.println("Error: received a null value for the code. Skipping this code.");
                return null;
            }

            return code;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

        return null;
    }

    // Extracts words from method names of Java files using the StaticJavaParser library, it navigates the abstract syntax tree of the code and for each method found, it extracts the name of the method and adds it to a list. It returns the list of extracted words
    public static List<String> extractJavaMethodNames(String code) throws FileNotFoundException {
        StaticJavaParser.parse(code).accept(new VoidVisitorAdapter<Object>() {

            public void visit(MethodDeclaration md, Object arg) {
                super.visit(md, arg);
                String methodName = md.getName().asString();
                String[] parts = methodName.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");

                for (String part : parts) {
                    javaWords.add(part);

                }
            }

        }, null);
        return javaWords;
    }

    //Extracts words from function names of Python files using regular expressions and following Python naming conventions and It returns the list of extracted words.
    private List<String> extractPythonWords(String code) {

        if (code == null) {
            System.out.println("Error: received a null value for the code. Skipping this code.");
            return pythonWords;
        }

        final Pattern PYTHON_FUNCTION_NAME_PATTERN = Pattern.compile("def\\s+([a-zA-Z_][a-zA-Z_0-9]*)\\s*");
        try {
            Matcher matcher = PYTHON_FUNCTION_NAME_PATTERN.matcher(code);

            if (matcher.groupCount() == 0) {
                System.out.println("No matches found for the regular expression pattern in extractPyhonWords method.");
                return pythonWords;
            }


            while (matcher.find()) {
                String functionName = matcher.group(1);
                String[] parts = functionName.split("[^a-zA-Z0-9]");

                for (String part : parts) {
                    if (!part.isEmpty() && part.matches("^[a-zA-Z0-9]+$")) {
                        pythonWords.add(part);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error in extractpythonWords: " + e.getMessage());
        }

        return pythonWords;
    }

    public static void main(String[] args) throws InterruptedException {
        MinerComponent mc = new MinerComponent();
        mc.mineGitHub();
    }
}

