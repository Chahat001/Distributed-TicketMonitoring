package com.example.app.repository;

import com.example.app.models.Post;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/*
 CrudRepository<EntityToBeHandled, @IDReperesentingPrimaryKey>

 */
public interface PostRepository extends CrudRepository<Post, Integer> {

    @Query("SELECT post FROM Post post where post.unixTimeStamp <= ?1")
    public Iterable<Post> findAllWhereTimeStampLessThan(Long unixTimeStamp);
}
