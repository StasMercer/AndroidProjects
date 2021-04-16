package model;

/**
 * Created by Mercer on 17.03.2018.
 */

public class ResponseFromServer {

    private String response;
    private String username;
    private String token;

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
