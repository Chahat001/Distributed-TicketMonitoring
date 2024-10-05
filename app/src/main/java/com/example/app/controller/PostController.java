package com.example.app.controller;

import com.example.app.models.Post;
import com.example.app.models.User;
import com.example.app.repository.PostRepository;
import com.example.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class PostController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;


    /*
    Design Improvements:
    Request and Response Models with @NotNull validation build into this
    and Build and validation
     */
    @PostMapping("/v1/createPost") // better naming convention following RESTapi design guidelines
    public @ResponseBody Map<String, String> createPost(@RequestBody Map<String, String> requestBody, @RequestParam(name="userId", defaultValue = "-1") String userId){
        Map<String, String> responseBody = new HashMap<>();
        int parsedUserId;
        try{
            parsedUserId = Integer.parseInt(userId);
        }
        catch (NumberFormatException e){
            responseBody.put("Status", "Upload failed: Invalid User Id");
            return responseBody;
        }
        if(requestBody.getOrDefault("content", null) == null){
            responseBody.put("Status", "Upload failed Content is Needed");
            return responseBody;
        }

        Optional<User> user = userRepository.findById(parsedUserId);
        if(user.isEmpty()){
            responseBody.put("Status", "Upload failed: Invalid User Id");
            return responseBody;
        }

        Post post = new Post();
        post.setUnixTimeStamp(System.currentTimeMillis());
        post.setUser(user.get());
        post.setContent(requestBody.get("content"));

        postRepository.save(post);

        responseBody.put("Status", "Uploaded");
        return responseBody;
    }


    @GetMapping("/v1/getAllPosts")
    public @ResponseBody Iterable<Post> fetchAllPosts(){
        Iterable<Post> posts = postRepository.findAll();

        for(Post post : posts){
            System.out.println(post.getContent());
        }
        return null;
    }



}
