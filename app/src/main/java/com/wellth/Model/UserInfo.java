package com.wellth.Model;

import com.parse.ParseUser;

import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UserInfo {
    public String username;
    public String email;
    public String password;
    public String country;
    public int age;
    public String token;
    public List<String> issueList;
    public File photo;
    public String type;
    public String photoUrl;

    public ArrayList<ParseUser> friendList;     // using for push notification when added to posts or questions in home feed.

    public UserInfo() {

    }
}
