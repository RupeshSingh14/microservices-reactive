DROP TABLE IF EXISTS reviews;
CREATE TABLE reviews (
    id INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    version INT,
    productId INT NOT NULL,
    reviewId INT NOT NULL,
    author VARCHAR(255),
    subject VARCHAR(255),
    content VARCHAR(255),
    UNIQUE INDEX reviews_unique_idx (productId, reviewId)
);