package com.singh.rupesh.reviewService.persistence;


import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

//import javax.persistence.*;

@Table("reviews")//(name = "reviews", indexes = { @Index(name= "reviews_unique_idx", unique= true, columnList = "productId, reviewId")})
@Getter
@Setter
@NoArgsConstructor
public class ReviewEntity {

    @Id
    @Column("ID")
    private int id;

   @Version
   @Column("VERSION")
   private int version;

   @Column("PRODUCTID")
    private int productId;
   @Column("REVIEWID")
    private int reviewId;
   @Column("AUTHOR")
    private String author;
   @Column("SUBJECT")
    private String subject;
   @Column("CONTENT")
    private String content;

    public ReviewEntity(int productId, int reviewId, String author, String subject, String content) {
        this.productId = productId;
        this.reviewId = reviewId;
        this.author = author;
        this.subject = subject;
        this.content = content;
    }
}